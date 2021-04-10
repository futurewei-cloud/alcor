/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.futurewei.alcor.macmanager.service.implement;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.ICacheFactory;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.exception.DistributedLockException;
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ParameterUnexpectedValueException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.macmanager.dao.MacRangeMappingRepository;
import com.futurewei.alcor.macmanager.dao.MacRangeRepository;
import com.futurewei.alcor.macmanager.dao.MacStateRepository;
import com.futurewei.alcor.macmanager.exception.*;
import com.futurewei.alcor.macmanager.pool.MacPoolApi;
import com.futurewei.alcor.macmanager.service.MacService;
import com.futurewei.alcor.macmanager.utils.MacManagerConstant;
import com.futurewei.alcor.web.entity.mac.MacAddress;
import com.futurewei.alcor.web.entity.mac.MacRange;
import com.futurewei.alcor.web.entity.mac.MacState;
import com.futurewei.alcor.web.entity.mac.MacStateBulkJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import java.util.*;

@Service
public class MacServiceImpl implements MacService {
    private static final Logger logger = LoggerFactory.getLogger(MacServiceImpl.class);
    private static final int BULK_MULTIPLE_MARGINS = 5;

    @Autowired
    private MacRangeRepository macRangeRepository;

    @Autowired
    private MacStateRepository macStateRepository;

    @Autowired
    private MacRangeMappingRepository macRangeMappingRepository;

    @Autowired
    private MacPoolApi macPoolApi;

    @Value("${macmanager.oui}")
    private String oui;

    private final ICacheFactory iCacheFactory;

    @Autowired
    public MacServiceImpl(ICacheFactory iCacheFactory) {
        this.iCacheFactory = iCacheFactory;
    }

    /**
     * get MacState
     *
     * @param macAddress
     * @return MAC address allocation state
     * @throws ParameterNullOrEmptyException          parameter macAddress is null or empty
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     */

    @DurationStatistics
    public MacState getMacStateByMacAddress(String macAddress) throws Exception {
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
    @DurationStatistics
    public MacState createMacState(MacState macState) throws Exception {
        String rangeId = MacManagerConstant.DEFAULT_RANGE;
        macState = createMacStateInRange(rangeId, macState);
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
    @DurationStatistics
    public MacState createMacStateInRange(String rangeId, MacState macState) throws Exception {
        if (macState == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        if (StringUtils.isEmpty(macState.getState()))
            macState.setState(MacManagerConstant.MAC_STATE_ACTIVE);

        // check macState if have mac
        String macAddress = macState.getMacAddress();
        if(!StringUtils.isEmpty(macAddress)){
            try {
                MacState dbMacState = macStateRepository.findItem(macAddress);
                if(dbMacState != null){
                    throw new MacAddressUniquenessViolationException(MacManagerConstant.MAC_EXCEPTION_UNIQUENESSSS_VILOATION);
                }
                // if put mac failed return mac address in use
                macState.setRangeId(rangeId);
                if(!macStateRepository.putIfAbsent(macState)){
                    throw new MacAddressUniquenessViolationException(MacManagerConstant.MAC_EXCEPTION_UNIQUENESSSS_VILOATION);
                }
                macPoolApi.markMac(rangeId, oui, macState.getMacAddress());
                return macState;
            } catch (CacheException | DistributedLockException e) {
                throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION);
            }
        }

        try {
            MacRange range = ensureRange(rangeId);
            final String realRangeId = range.getRangeId();

            boolean flag = false;
            try(Transaction tx = iCacheFactory.getTransaction().start()) {
                while (!flag) {
                    String mac = macPoolApi.allocate(oui, range);
                    macState.setMacAddress(mac);
                    flag = trySaveMac(realRangeId, macState);
                }
                tx.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    @DurationStatistics
    public MacState updateMacState(String macAddress, MacState macState) throws Exception {
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
    @DurationStatistics
    public String releaseMacState(String macAddress) throws Exception {
        String strMacAddress;
        if (macAddress == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));

        try {
            MacState macState = macStateRepository.findItem(macAddress);
            if (macState == null) {
                throw (new ResourceNotFoundException(MacManagerConstant.MAC_EXCEPTION_MAC_NOT_EXISTING));
            }

            String rangeId = getMacRangeId(macState);
            macStateRepository.deleteItem(macAddress);
            macPoolApi.release(rangeId, oui, macAddress);
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
    @DurationStatistics
    public MacRange getMacRangeByMacRangeId(String macRangeId) throws Exception {
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
     * @param queryParams filter params
     * @return MAC allocation state with an allocated MAC address
     * @throws ParameterNullOrEmptyException          parameter macAddress is null or empty
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     */
    @Override
    @DurationStatistics
    public Map<String, MacRange> getAllMacRanges(Map<String, Object[]> queryParams) throws Exception {
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
    @DurationStatistics
    public MacRange createMacRange(MacRange macRange) throws Exception {
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
    @DurationStatistics
    public MacRange updateMacRange(MacRange macRange) throws Exception {
        if (macRange == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        if (!isValidRange(macRange))
            throw (new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_VALUE_INVALID));
        try {
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
    @DurationStatistics
    public String deleteMacRange(String rangeId) throws Exception {
        if (rangeId == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        if (rangeId.equals(MacManagerConstant.DEFAULT_RANGE))
            throw (new MacRangeDeleteNotAllowedException(MacManagerConstant.MAC_EXCEPTION_DELETE_DEFAULT_RANGE));
        try {
            try(Transaction tx = iCacheFactory.getTransaction().start()) {
                macRangeRepository.deleteItem(rangeId);
                macRangeMappingRepository.removeRange(rangeId);
                tx.commit();
            }
        } catch (Exception e) {
            logger.error("MacService deleteMacRange() exception:", e);
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }
        return rangeId;
    }

    /**
     * create bulk MacState in default MAC range
     *
     * @param macStateBulkJson MAC allocation states
     * @return MAC allocation state with an allocated MAC address
     * @throws Exception create Exceptions
     */
    @Override
    @DurationStatistics
    public MacStateBulkJson createMacStateBulk(MacStateBulkJson macStateBulkJson) throws Exception {
        return createMacStateBulkInRange(MacManagerConstant.DEFAULT_RANGE, macStateBulkJson);
    }

    /**
     * create bulk MacState in range
     *
     * @param macStateBulkJson MAC allocation state
     * @param rangeId  MAC address range to create
     * @return MAC allocation state with an allocated MAC address
     * @throws Exception create Exceptions
     */
    @Override
    @DurationStatistics
    public MacStateBulkJson createMacStateBulkInRange(String rangeId, MacStateBulkJson macStateBulkJson) throws Exception {
        // handle exist macs
        List<MacState> macStateList = macStateBulkJson.getMacStates();
        Iterator<MacState> macStateIterator = macStateList.iterator();

        // ensure range is created
        MacRange range = ensureRange(rangeId);

        int needSize = macStateList.size() + BULK_MULTIPLE_MARGINS;
        final String realRangeId = range.getRangeId();

        try(Transaction tx = iCacheFactory.getTransaction().start()) {
            Map<String, MacState> newMacStates = new HashMap<>();
            while (macStateIterator.hasNext()) {
                Set<String> macs = macPoolApi.allocateBulk(oui, range, needSize);
                Iterator<String> macsIt = macs.iterator();

                MacState macState = macStateIterator.next();
                while (macsIt.hasNext() && macState != null) {
                    if (!StringUtils.isEmpty(macState.getMacAddress())) {
                        if (!trySaveMac(rangeId, macState)) {
                            macStateIterator.remove();
                        } else {
                            macPoolApi.markMac(rangeId, oui, macState.getMacAddress());
                            macState = macStateIterator.hasNext() ? macStateIterator.next() : null;
                        }
                        continue;
                    }

                    String mac = macsIt.next();
                    // range id is need for identity this mac allocated by which range
                    macState.setRangeId(realRangeId);
                    macState.setMacAddress(mac);
                    newMacStates.put(mac, macState);
                    macState = macStateIterator.hasNext() ? macStateIterator.next() : null;
                }
                macStateRepository.addAllItem(newMacStates);

            }
            tx.commit();
        }
        return macStateBulkJson;
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
     * ensure range is created
     * @param rangeId the range id
     * @return MacRange
     * @throws CacheException
     * @throws MacRangeInvalidException
     */
    private MacRange ensureRange(String rangeId) throws CacheException, MacRangeInvalidException {
        MacRange range = macRangeRepository.findItem(rangeId);

        if(range != null){
            if (!range.getState().equals(MacManagerConstant.MAC_RANGE_STATE_ACTIVE)){
                throw new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_NOT_ACTIVE);
            }
            return range;
        }
        /* Default MAC range: MAC address is generated in a MAC range betwen from ~ to, if there is no active user defined MAC range, by default default MAC range is applied.
         *  Default MAC range: name is defined MacManagerConstant class, range is all oui range. e.g.)
         * if oui=AA-BB-CC then default MAC range is AA-BB-CC-00-00-00 ~ AA-BB-CC-FF-FF-FF. */
        if (rangeId.equals(MacManagerConstant.DEFAULT_RANGE)) {
            return createDefaultRange(oui);
        } else {
            throw new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_NOT_EXISTING);
        }
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
            macRangeRepository.putIfAbsent(defaultRange);
        } catch (CacheException e) {
            logger.error("MacService createDefaultRange() exception:", e);
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }
        return defaultRange;
    }

    private boolean trySaveMac(String rangeId, MacState macState){
        try {
            macState.setRangeId(rangeId);
            return macStateRepository.putIfAbsent(macState);
        } catch (CacheException e) {
            logger.error(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
            macPoolApi.release(rangeId, oui, macState.getMacAddress());
            return false;
        }
    }

    private String getMacRangeId(MacState macState){
        String rangeId = macState.getRangeId();
        if(StringUtils.isEmpty(rangeId)){
            return MacManagerConstant.DEFAULT_RANGE;
        }
        return rangeId;
    }
}



