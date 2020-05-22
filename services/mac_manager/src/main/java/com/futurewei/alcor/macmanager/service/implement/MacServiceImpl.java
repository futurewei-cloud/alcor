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
import com.futurewei.alcor.common.exception.ParameterNullOrEmptyException;
import com.futurewei.alcor.common.exception.ParameterUnexpectedValueException;
import com.futurewei.alcor.common.exception.ResourceNotFoundException;
import com.futurewei.alcor.macmanager.dao.MacPoolRepository;
import com.futurewei.alcor.macmanager.dao.MacRangeRepository;
import com.futurewei.alcor.macmanager.dao.MacStateRepository;
import com.futurewei.alcor.web.entity.mac.MacAddress;
import com.futurewei.alcor.web.entity.mac.MacRange;
import com.futurewei.alcor.macmanager.exception.InvalidMacRangeException;
import com.futurewei.alcor.macmanager.exception.RetryLimitExceedException;
import com.futurewei.alcor.macmanager.exception.UniquenessViolationException;
import com.futurewei.alcor.macmanager.service.MacService;
import com.futurewei.alcor.macmanager.utils.MacUtil;
import com.futurewei.alcor.web.entity.mac.MacState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MacServiceImpl implements MacService {
    private static final Logger logger = LoggerFactory.getLogger(MacServiceImpl.class);
    static public Hashtable<String, MacRange> activeMacRanges = new Hashtable<String, MacRange>();
    final String DELIMITER = "/";

    @Autowired
    private MacRangeRepository macRangeRepository;

    @Autowired
    private MacStateRepository macStateRepository;

    @Autowired
    private MacPoolRepository macPoolRepository;

    @Value("${macmanager.oui}")
    private String oui;

    @Value("${macmanager.pool.size}")
    private long nMacPoolSize;

    @Value("${macmanager.retrylimit}")
    private long nRetryLimit;

    /**
     * get MacState
     *
     * @param macAddress
     * @return MAC address allocation state
     * @throws ParameterNullOrEmptyException          parameter macAddress is null or empty
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     */
    public MacState getMacStateByMacAddress(String macAddress) throws ParameterNullOrEmptyException, MacRepositoryTransactionErrorException, MacAddressInvalidException {
        MacState macState = null;
        if (macAddress == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        try {
            macState = macStateRepository.findItem(macAddress);
        } catch (CacheException e) {
            logger.error("MacService getMacStateByMacAddress() exception:", e);
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        } catch (Exception e) {
            logger.error("MacService getMacStateByMacAddress() exception:", e);
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
        String rangeId = MacManagerConstant.DEFAULT_RANGE;
        try {
            macState = createMacStateInRange(rangeId, macState);
        } catch (CacheException e) {
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION);
        } catch (MacRangeInvalidException | MacAddressUniquenessViolationException | MacAddressFullException | MacAddressRetryLimitExceedException e) {
            throw e;
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
                    createMacRange(range);
                } else {
                    throw new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_NOT_EXISTING);
                }
            } else if (range.getState().equals(MacManagerConstant.MAC_RANGE_STATE_ACTIVE) == false) {
                throw new MacRangeInvalidException(MacManagerConstant.MAC_EXCEPTION_RANGE_NOT_ACTIVE);
            }
            if (macPoolRepository.getSize(rangeId) < (nMacPoolSize - (MacManagerConstant.MAC_PREGENERATE_SIZE / 2))) {
                CompletableFuture<Long> completableFuture = CompletableFuture.supplyAsync(() -> {
                    long n = 0;
                    try {
                        n = generateMacInPool(rangeId, MacManagerConstant.MAC_PREGENERATE_SIZE);
                    } catch (Exception e) {
                        logger.error("MacService createMacState() exception:", e);
                    }
                    return n;
                });
                long l = completableFuture.get();
                completableFuture.thenAccept(System.out::println);
                completableFuture.join();
                logger.info("{} New MAC addresses were created.", l);
            }
            String strMacAddress = allocateMacState(rangeId, macState);
            if (strMacAddress != null) {
                macState.setMacAddress(strMacAddress);
                macStateRepository.addItem(macState);
            } else {
                Vector<Long> vtNic = generateNic(rangeId, 1);
                if (vtNic != null) {
                    if (vtNic.size() > 0) {
                        macAddress.setOui(oui);
                        long l = vtNic.firstElement();
                        String nic = MacAddress.longToNic(l, macAddress.getNicLength());
                        macAddress.setNic(nic);
                        macState.setMacAddress(macAddress.getMacAddress());
                        MacState macState2 = macStateRepository.findItem(macAddress.getMacAddress());
                        if (macStateRepository.findItem(macAddress.getMacAddress()) != null)
                            throw (new MacAddressUniquenessViolationException(MacManagerConstant.MAC_EXCEPTION_UNIQUENESSSS_VILOATION + macAddress.getMacAddress() + macState2.getProjectId()));
                        else
                            macStateRepository.addItem(macState);
                    }
                }
            }
        } catch (CacheException e) {
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION);
        } catch (MacAddressFullException | MacAddressRetryLimitExceedException e) {
            throw e;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("MacService generateMacInPool() exception:", e);
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
        if (macAddress.equals(macState.getMacAddress()) == false)
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
        String strMacAddress = null;
        if (macAddress == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        try {
            MacState macState = macStateRepository.findItem(macAddress);
            if (macState == null) {
                throw (new ResourceNotFoundException(MacManagerConstant.MAC_EXCEPTION_MAC_NOT_EXISTING));
            } else {
                try {
                    macStateRepository.deleteItem(macAddress);
                    inactivateMacAddressBit(macAddress);

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
    public Map<String, MacRange> getAllMacRanges() throws MacRepositoryTransactionErrorException {
        Hashtable<String, MacRange> macRanges = new Hashtable<String, MacRange>();
        try {
            macRanges.putAll(macRangeRepository.findAllItems());
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
        if (isValidRange(macRange) == false)
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
        if (isValidRange(macRange) == false)
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
        } catch (Exception e) {
            logger.error("MacService deleteMacRange() exception:", e);
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }
        return rangeId;
    }

    /**
     * allocate a MAC address to a port
     *
     * @param rangeId MAC range id
     * @return allocated MAC address
     * @throws ParameterNullOrEmptyException parameter rangeId is null or empty
     * @macState MAC state what contains port information
     */
    private String allocateMacState(String rangeId, MacState macState) throws ParameterNullOrEmptyException {
        String strMacAddress = null;
        if (macState == null)
            throw (new ParameterNullOrEmptyException(MacManagerConstant.MAC_EXCEPTION_PARAMETER_NULL_EMPTY));
        try {
            strMacAddress = macPoolRepository.getRandomItem(rangeId);
        } catch (Exception e) {
            logger.error("MacService allocateMacState() exception:", e);
        }
        return strMacAddress;
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
        BitSet bitSet = new BitSet((int) nNicLength);
        MacRange defaultRange = new MacRange(rangeId, from, to, state, bitSet);
        try {
            macRangeRepository.addItem(defaultRange);
        } catch (CacheException e) {
            logger.error("MacService createDefaultRange() exception:", e);
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }
        return defaultRange;
    }

    /**
     * generate MAC addresses in MAC pool in advance
     *
     * @param rangeId MAC range id
     * @param n       the number of MAC addresses to generate
     * @return the number of MAC addresses generated
     * @throws MacAddressRetryLimitExceedException MAC address generation is tried more than limit
     */
    private long generateMacInPool(String rangeId, int n) throws MacAddressRetryLimitExceedException {
        HashSet<String> hsMacAddress = new HashSet<String>();
        MacAddressRetryLimitExceedException exception = null;
        long nReturn = 0;
        if (n < 1) return nReturn;
        try {
            Vector<Long> vtNic = generateNic(rangeId, n);
            MacAddress macAddress = new MacAddress(oui, null);
            int nNicLength = macAddress.getNicLength();
            if (vtNic != null) {
                if (vtNic.size() > 0) {
                    for (int i = 0; i < vtNic.size(); i++) {
                        long nic = vtNic.elementAt(i);
                        macAddress.setOui(oui);
                        macAddress.setNic(MacAddress.longToNic(nic, nNicLength));
                        String strMacAddress = macAddress.getMacAddress();
                        MacState macState = macStateRepository.findItem(strMacAddress);
                        if (macState == null) {
                            hsMacAddress.add(strMacAddress);
                            nReturn++;
                        }
                    }
                    macPoolRepository.addItem(rangeId, hsMacAddress);
                }
            }
        } catch (MacAddressRetryLimitExceedException e) {
            exception = e;
        } catch (Exception e) {
            logger.error("MacService generateMacInPool() exception:", e);
        }
        if (exception != null)
            throw exception;
        return nReturn;
    }

    private Vector<Long> generateNic(String rangeId, int n) throws MacRepositoryTransactionErrorException, MacAddressFullException, MacAddressRetryLimitExceedException {
        Vector<Long> vtNic = new Vector<Long>();
        String nic = null;
        MacAddress macAddress = new MacAddress(oui, null);
        Long from = (long) 0;
        Long to = (long) 0;
        int nNicLength = macAddress.getNicLength();
        long randomNic = -1;
        MacRange macRange = null;
        try {
            macRange = macRangeRepository.findItem(rangeId);
        } catch (CacheException e) {
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        }
        if (macRange != null) {
            from = MacAddress.macToLong(new MacAddress(macRange.getFrom()).getNic());
            to = MacAddress.macToLong(new MacAddress(macRange.getTo()).getNic());
        }
        long nAavailableMac = availableMac(from, to);
        if (nAavailableMac == 0) {
            throw new MacAddressFullException(MacManagerConstant.MAC_EXCEPTION_MACADDRESS_FULL);
        } else if (nAavailableMac < (long) n) {
            n = (int) nAavailableMac;
        } else {
            int i = 0;
            int nTry = 0;
            while (i < n && nAavailableMac > 0) {
                long randomNum = ThreadLocalRandom.current().nextLong(0, nAavailableMac);
                randomNic = getRandomNicFromBitSet(from, randomNum);
                vtNic.add(randomNic);
                nAavailableMac--;
                i++;
            }
        }
        return vtNic;
    }

    /**
     * update tracking information of avaialble MAC addresses
     *
     * @param nic    network interface card id of a MAC address
     * @param bValue true if used, false otherwise
     * @return
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     */
    private void updateBitSet(long nic, boolean bValue) throws MacRepositoryTransactionErrorException {
        try {
            MacRange deafultRange = getMacRangeByMacRangeId(MacManagerConstant.DEFAULT_RANGE);
            BitSet bitSet = deafultRange.getBitSet();
            if (bValue)
                bitSet.set((int) nic);
            else
                bitSet.clear((int) nic);
            deafultRange.setBitSet(bitSet);
            macRangeRepository.addItem(deafultRange);
        } catch (CacheException e) {
            logger.error("MacService getRandomNicFromBitSet() exception:", e);
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        } catch (Exception e) {
            logger.error("MacService getRandomNicFromBitSet() exception:", e);
        }
    }

    /**
     * provide how many MAC addresses
     *
     * @param from start MAC address to compute
     * @param to   end MAC address to compute
     * @return the number of available MAC addresses between from and to
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     */
    private long availableMac(long from, long to) throws MacRepositoryTransactionErrorException {
        long nAvailable = 0;
        String rangeId = MacManagerConstant.DEFAULT_RANGE;
        try {
            MacRange range = getMacRangeByMacRangeId(rangeId);
            if (range == null)
                range = this.createDefaultRange(oui);
            if (range.getBitSet() == null) {
                MacAddress macAddress = new MacAddress(oui, null);
                range.setBitSet(new BitSet(macAddress.getNicLength()));
            }
            BitSet bitSet = range.getBitSet();
            BitSet bitSet2 = bitSet.get((int) from, (int) to);
            long nTotal = to - from;
            nAvailable = nTotal - bitSet2.cardinality();
        } catch (CacheException e) {
            logger.error("MacService availableMac() exception:", e);
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        } catch (Exception e) {
            logger.error("MacService availableMac() exception:", e);
        }
        return nAvailable;
    }

    /**
     * pick a MAC address randomly among available MAC addresses
     *
     * @param from start MAC address
     * @param n    nth MAC address to pick
     * @return MAC address picked
     * @throws MacRepositoryTransactionErrorException error during repository transaction
     */
    private synchronized long getRandomNicFromBitSet(long from, long n) throws MacRepositoryTransactionErrorException {
        long nRandomBit = 0;
        String rangeId = MacManagerConstant.DEFAULT_RANGE;
        try {
            MacRange range = getMacRangeByMacRangeId(rangeId);
            BitSet bitSet = range.getBitSet();
            if (bitSet == null) {
                bitSet = createDefaultRangeBitSet();
            }

            nRandomBit = bitSet.nextClearBit((int) (from + n));
            bitSet.set((int) nRandomBit);
            range.setBitSet(bitSet);
            macRangeRepository.addItem(range);
        } catch (CacheException e) {
            logger.error("MacService getRandomNicFromBitSet() exception:", e);
            throw new MacRepositoryTransactionErrorException(MacManagerConstant.MAC_EXCEPTION_REPOSITORY_EXCEPTION, e);
        } catch (Exception e) {
            logger.error("MacService getRandomNicFromBitSet() exception:", e);
        }
        return nRandomBit;
    }

    private BitSet createDefaultRangeBitSet() {
        MacAddress macAddress = new MacAddress(oui, null);
        BitSet bitSet = new BitSet(macAddress.getNicLength());
        return bitSet;
    }

    private void inactivateMacAddressBit(String macAddress) throws MacRepositoryTransactionErrorException {
        MacAddress mac = new MacAddress(macAddress);
        long nic = MacAddress.nicToLong(mac.getNic());
        try {
            updateBitSet(nic, false);
        } catch (CacheException e) {
            throw new MacRepositoryTransactionErrorException(e);
        }
    }
}