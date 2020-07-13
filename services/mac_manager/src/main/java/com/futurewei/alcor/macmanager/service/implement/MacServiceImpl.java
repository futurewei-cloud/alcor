/*Copyright 2019 The Alcor Authors.

Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
*/
package com.futurewei.alcor.macmanager.service.implement;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.IDistributedLock;
import com.futurewei.alcor.common.db.IDistributedLockFactory;
import com.futurewei.alcor.common.exception.DistributedLockException;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ParameterUnexpectedValueException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.macmanager.dao.MacRangeMappingRepository;
import com.futurewei.alcor.macmanager.dao.MacRangeRepository;
import com.futurewei.alcor.macmanager.dao.MacStateRepository;
import com.futurewei.alcor.macmanager.generate.Generator;
import com.futurewei.alcor.web.entity.mac.MacAddress;
import com.futurewei.alcor.web.entity.mac.MacAllocate;
import com.futurewei.alcor.web.entity.mac.MacRange;
import com.futurewei.alcor.macmanager.service.MacService;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.macmanager.exception.*;
import com.futurewei.alcor.macmanager.utils.MacManagerConstant;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MacServiceImpl implements MacService {
    private static final Logger logger = LoggerFactory.getLogger(MacServiceImpl.class);

    private static final int MUTIL_QUERY_THRESHOLD = 50;

    @Autowired
    private MacRangeRepository macRangeRepository;

    @Autowired
    private MacStateRepository macStateRepository;

    @Autowired
    private MacRangeMappingRepository macRangeMappingRepository;

    @Autowired
    private Generator generator;

    @Value("${macmanager.oui}")
    private String oui;


    /**
     * get MacState
     *
     * @param macAddress
     * @return MAC address allocation state
     * @throws ParameterNullOrEmptyException          parameter macAddress is null or empty
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     */
    public MacState getMacStateByMacAddress(String macAddress) throws ParameterNullOrEmptyException, MacRepositoryTransactionErrorException, MacAddressInvalidException {
        MacState macState;
        if (macAddress == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        try {
            macState = macStateRepository.findItem(macAddress);
        } catch (CacheException e) {
            logger.error("MacService getMacStateByMacAddress() exception:", e);
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }
        return macState;
    }

    /**
     * create a MacState in default MAC range
     *
     * @param macState MAC allocation state
     * @return MAC allocation state with an allocated MAC address
     * @throws ParameterNullOrEmptyException          parameter macAddress is null or empty
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     * @throws MacRangeInvalidException               mac range to create a new MAC address is null or not existing     *
     * @throws MacAddressUniquenessViolationException MAC address is not unique. MAC address should be allocated to only one port
     * @throws MacAddressFullException                All MAC addresses are created.
     * @throws MacAddressRetryLimitExceedException    MAC addresss creation is tried more than limit
     */
    public MacState createMacState(MacState macState) throws ParameterNullOrEmptyException, MacRepositoryTransactionErrorException, MacRangeInvalidException, MacAddressUniquenessViolationException, MacAddressFullException, MacAddressRetryLimitExceedException {
        if (macState == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));

        String macAddress = macState.getMacAddress();
        if(!StringUtils.isEmpty(macAddress)){
            try {
                MacState dbMacState = macStateRepository.findItem(macAddress);
                if(dbMacState != null){
                    throw new MacAddressUniquenessViolationException(MacManagerConstant.MAC_EXCEPTION_UNIQUENESSSS_VILOATION);
                }
                macStateRepository.addItem(macState);
                return macState;
            } catch (CacheException e) {
                throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION);
            }
        }

        String rangeId = MacManagerConstant.DEFAULT_RANGE;
        try {
            macState = createMacStateInRange(rangeId, macState);
        } catch (CacheException e) {
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION);
        }
        return macState;
    }

    /**
     * create a MacState
     *
     * @param macState MAC allocation state
     * @param rangeId  MAC address range to create
     * @return MAC allocation state with an allocated MAC address
     * @throws ParameterNullOrEmptyException          parameter macAddress is null or empty
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     * @throws MacRangeInvalidException               mac range to create a new MAC address is null or not existing
     * @throws MacAddressUniquenessViolationException MAC address is not unique. MAC address should be allocated to only one port
     * @throws MacAddressFullException                All MAC addresses are created.
     * @throws MacAddressRetryLimitExceedException    MAC addresss creation is tried more than limit
     */
    @Override
    public MacState createMacStateInRange(String rangeId, MacState macState) throws ParameterNullOrEmptyException, MacRepositoryTransactionErrorException, MacRangeInvalidException, MacAddressUniquenessViolationException, MacAddressRetryLimitExceedException, MacAddressFullException {
        if (macState == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        MacAddress macAddress = new MacAddress();
        if (macState.getState() == null)
            macState.setState(MacManagerConstant.MAC_STATE_ACTIVE);
        else if (macState.getState().trim().length() == 0)
            macState.setState(MacManagerConstant.MAC_STATE_ACTIVE);
        try {
            MacRange range = macRangeRepository.findItem(rangeId);
            /* Default MAC range: MAC address is generated in a MAC range betwen from ~ to, if there is no active user defined MAC range, by default default MAC range is applied.
             *  Default MAC range: name is defined MacManagerConstant class, range is all oui range. e.g.)
             * if oui=AA-BB-CC then default MAC range is AA-BB-CC-00-00-00 ~ AA-BB-CC-FF-FF-FF. */
            if (range == null) {
                if (rangeId.equals(MacManagerConstant.DEFAULT_RANGE)) {
                    range = createDefaultRange(oui);
                } else {
                    throw new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_NOT_EXISTING);
                }
            } else if (!range.getState().equals(MacManagerConstant.MAC_RANGE_STATE_ACTIVE)) {
                throw new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_NOT_ACTIVE);
            }

            final String realRangeId = range.getRangeId();
            macState.setRangeId(realRangeId);

            long used = macRangeMappingRepository.size(realRangeId);
            generator.allocateMac(oui, range, used, allocatedMacs -> {
                boolean flag = false;
                int len = allocatedMacs.length;
                if (len < MUTIL_QUERY_THRESHOLD){
                    for(String mac: allocatedMacs){
                        macState.setMacAddress(mac);
                        if(flag = trySaveMac(realRangeId, mac, macState)){
                            break;
                        }
                    }
                }else{
                    // get too many result change to bulk handle
                    Set<String> keys = Sets.newHashSet(allocatedMacs);
                    Map<String, MacState> map = null;
                    try {
                        map = macStateRepository.findAllItems(keys);
                    } catch (CacheException e) {
                        map = new HashMap<>();
                    }
                    for(String mac: allocatedMacs){
                        if(!map.containsKey(mac)){
                            macState.setMacAddress(mac);
                            if(flag = trySaveMac(realRangeId, mac, macState)){
                                break;
                            }
                        }
                    }
                }

                return flag;
            });

        } catch (CacheException e) {
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION);
        }
        return macState;
    }

    /**
     * update a MacState
     *
     * @param macAddress MAC address
     * @param macState   MAC allocation state with new data
     * @return MAC allocation state
     * @throws ParameterNullOrEmptyException          parameter macAddress is null or empty
     * @throws ParameterUnexpectedValueException      macAddress is not equal to macState mac address
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     * @throws ResourceNotFoundException              there is not mac state with macAddress
     */
    @Override
    public MacState updateMacState(String macAddress, MacState macState) throws ParameterNullOrEmptyException, ParameterUnexpectedValueException, MacRepositoryTransactionErrorException, ResourceNotFoundException {
        if (macAddress == null || macState == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        if (!macAddress.equals(macState.getMacAddress()))
            throw (new ParameterUnexpectedValueException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_INVALID));
        try {
            if (macStateRepository.findItem(macAddress) != null) {
                macStateRepository.addItem(macState);
            } else {
                ResourceNotFoundException e = new ResourceNotFoundException(MacManagerConstant.MAC_EXCEPTION_MAC_NOT_EXISTING);
                logger.error("MacService updateMacState() exception:", e);
                throw (e);
            }
        } catch (CacheException e) {
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION);
        }
        return macState;
    }

    /**
     * release MAC address from an allocation and put it back to MAC address pool
     *
     * @param macAddress MAC address
     * @return MAC address
     * @throws ParameterNullOrEmptyException          parameter macAddress is null or empty
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     * @throws ResourceNotFoundException              there is not mac state to release macAddress
     */
    @Override
    public String releaseMacState(String macAddress) throws ParameterNullOrEmptyException, MacRepositoryTransactionErrorException, ResourceNotFoundException {
        String strMacAddress;
        if (macAddress == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        try {
            MacState macState = macStateRepository.findItem(macAddress);
            if (macState == null) {
                throw (new ResourceNotFoundException(MacManagerConstant.MAC_EXCEPTION_MAC_NOT_EXISTING));
            } else {
                try {
                    macStateRepository.deleteItem(macAddress);
                    macRangeMappingRepository.releaseMac(macState.getRangeId(), macAddress);
                } catch (CacheException e) {
                    throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION);
                }
            }
            strMacAddress = macState.getMacAddress();
        } catch (CacheException e) {
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION);
        }
        return strMacAddress;
    }

    /**
     * get MAC range data
     *
     * @param macRangeId MAC range id
     * @return MAC range
     * @throws ParameterNullOrEmptyException          parameter macAddress is null or
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     */
    @Override
    public MacRange getMacRangeByMacRangeId(String macRangeId) throws ParameterNullOrEmptyException, MacRepositoryTransactionErrorException {
        if (macRangeId == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        MacRange macRange = null;
        try {
            macRange = macRangeRepository.findItem(macRangeId);
        } catch (CacheException e) {
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION);
        }
        return macRange;
    }

    /**
     * get all MAC ranges
     *
     * @param
     * @return MAC allocation state with an allocated MAC address
     * @throws ParameterNullOrEmptyException          parameter macAddress is null or empty
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     */
    @Override
    public Map<String, MacRange> getAllMacRanges(Map<String, Object[]> queryParams) throws MacRepositoryTransactionErrorException {
        Map<String, MacRange> macRanges = null;
        String rangeIdName = "rangeId";
        try {
            if(queryParams.size() == 0) {
                macRanges = macRangeRepository.findAllItems();
            }else if(queryParams.size() == 1 && queryParams.containsKey(rangeIdName)) {
                Object[] rangeIds = queryParams.get(rangeIdName);
                Set<String> keys = new HashSet<>(rangeIds.length);
                for(Object rangeId: rangeIds){
                    keys.add((String)rangeId);
                }
                macRanges = macRangeRepository.findAllItems(keys);
            }else{
                macRanges = macRangeRepository.findAllItems(queryParams);
            }
        } catch (CacheException e) {
            logger.error("MacService getAllMacRanges() exception:", e);
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }
        return macRanges;
    }

    /**
     * create a new MAC range
     *
     * @param macRange MAC range
     * @return new MAC range
     * @throws ParameterNullOrEmptyException          parameter macAddress is null or empty
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     * @throws MacRangeInvalidException               macRange's from should be less than macRange's to
     */
    @Override
    public MacRange createMacRange(MacRange macRange) throws ParameterNullOrEmptyException, MacRepositoryTransactionErrorException, MacRangeInvalidException {
        if (macRange == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        if (!isValidRange(macRange))
            throw (new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_VALUE_INVALID));
        try {
            macRangeRepository.addItem(macRange);
        } catch (CacheException e) {
            logger.error("MacService createMacRange() exception:", e);
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }
        return macRange;
    }

    /**
     * update an existing MAC ranges
     *
     * @param macRange MAC range
     * @return updated MAC range
     * @throws ParameterNullOrEmptyException          parameter macAddress is null or empty
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     * @throws MacRangeInvalidException               macRange's from should be less than macRange's to
     */
    @Override
    public MacRange updateMacRange(MacRange macRange) throws ParameterNullOrEmptyException, MacRepositoryTransactionErrorException, MacRangeInvalidException {
        if (macRange == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        if (!isValidRange(macRange))
            throw (new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_VALUE_INVALID));
        try {
            macRangeRepository.deleteItem(macRange.getRangeId());
            macRangeRepository.addItem(macRange);
        } catch (CacheException e) {
            logger.error("MacService updateMacRange() exception:", e);
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }
        return macRange;
    }

    /**
     * delete an existing MAC ranges
     *
     * @param rangeId MAC range id
     * @return deleted MAC range id
     * @throws ParameterNullOrEmptyException          parameter rangeId is null or empty
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     * @throws MacRangeDeleteNotAllowedException      default mac range is prohibited to delete
     */
    @Override
    public String deleteMacRange(String rangeId) throws ParameterNullOrEmptyException, MacRepositoryTransactionErrorException, MacRangeDeleteNotAllowedException {
        if (rangeId == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        if (rangeId.equals(MacManagerConstant.DEFAULT_RANGE))
            throw (new MacRangeDeleteNotAllowedException(MacManagerConstant.MAC_EXCEPTION_DELETE_DEFAULT_RANGE));
        try {
            macRangeRepository.deleteItem(rangeId);
            macRangeMappingRepository.removeRange(rangeId);
        } catch (Exception e) {
            logger.error("MacService deleteMacRange() exception:", e);
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }
        return rangeId;
    }

    /**
     * verify MAC range values
     *
     * @param macRange MAC range data
     * @return true if macRange is valid, false otherwise
     * @throws
     */
    private boolean isValidRange(MacRange macRange) {
        String strFrom = macRange.getFrom();
        String strTo = macRange.getTo();
        long from = MacAddress.macToLong(new MacAddress(strFrom).getNic());
        long to = MacAddress.macToLong(new MacAddress(strTo).getNic());
        return from < to;
    }

    /**
     * create a default MAC range
     *
     * @param oui unique id of an organization
     * @return default MAC range
     * @throws
     */
    private MacRange createDefaultRange(String oui) throws CacheException {
        String rangeId = MacManagerConstant.DEFAULT_RANGE;
        MacAddress macAddress = new MacAddress(oui, null);
        long nNicLength = (long) Math.pow(2, macAddress.getNicLength());
        String strFrom = MacAddress.longToNic(0, macAddress.getNicLength());
        String strTo = MacAddress.longToNic(nNicLength - 1, macAddress.getNicLength());
        String from = new MacAddress(oui, strFrom).getMacAddress();
        String to = new MacAddress(oui, strTo).getMacAddress();
        String state = MacManagerConstant.MAC_RANGE_STATE_ACTIVE;
        MacRange defaultRange = new MacRange(rangeId, from, to, state);
        try {
            macRangeRepository.addItem(defaultRange);
        } catch (CacheException e) {
            logger.error("MacService createDefaultRange() exception:", e);
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }
        return defaultRange;
    }

    private boolean trySaveMac(String rangeId, String macAddress, MacState macState){
        boolean flag = false;
        try {
            flag = macStateRepository.putIfAbsent(macState);
            if(flag){
                macRangeMappingRepository.addItem(rangeId, new MacAllocate(rangeId, macAddress));
            }
        } catch (CacheException e) {
            logger.error(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }
        return flag;
    }
}



