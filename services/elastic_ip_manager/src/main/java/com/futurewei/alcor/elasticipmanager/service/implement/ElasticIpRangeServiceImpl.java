package com.futurewei.alcor.elasticipmanager.service.implement;

import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.elasticipmanager.dao.ElasticIpAllocator;
import com.futurewei.alcor.elasticipmanager.dao.ElasticIpRangeRepo;
import com.futurewei.alcor.elasticipmanager.dao.ElasticIpRepo;
import com.futurewei.alcor.elasticipmanager.exception.ElasticIpInternalErrorException;
import com.futurewei.alcor.elasticipmanager.exception.elasticiprange.ElasticIpRangeExistsException;
import com.futurewei.alcor.elasticipmanager.exception.elasticiprange.ElasticIpRangeInUseException;
import com.futurewei.alcor.elasticipmanager.exception.elasticiprange.ElasticIpRangeNotFoundException;
import com.futurewei.alcor.elasticipmanager.exception.elasticiprange.ElasticIpRangeParameterException;
import com.futurewei.alcor.elasticipmanager.service.ElasticIpRangeService;
import com.futurewei.alcor.web.entity.elasticip.ElasticIp;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpRange;
import com.futurewei.alcor.web.entity.elasticip.ElasticIpRangeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ElasticIpRangeServiceImpl implements ElasticIpRangeService {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticIpRangeServiceImpl.class);

    @Autowired
    ElasticIpRangeRepo elasticIpRangeRepo;

    @Autowired
    ElasticIpRepo elasticIpRepo;

    @Autowired
    ElasticIpAllocator elasticIpAllocator;

    /**
     * Create an elastic ip range.
     * @param request elastic ip range configuration
     * @return Elastic ip range information
     * @throws ElasticIpRangeExistsException The elastic ip range already exists
     * @throws ElasticIpInternalErrorException Internal process (database / lock etc.) error
     */
    @DurationStatistics
    public ElasticIpRangeInfo createElasticIpRange(ElasticIpRangeInfo request) throws Exception {
        LOG.debug("Create an elastic ip range, request: {}", request);

        if (request.getId() == null) {
            request.setId(UUID.randomUUID().toString());;
        } else if (elasticIpRangeRepo.findItem(request.getId()) != null) {
            throw new ElasticIpRangeExistsException();
        }

        ElasticIpRange elasticIpRange = new ElasticIpRange(request);

        elasticIpRangeRepo.addItem(elasticIpRange);

        elasticIpAllocator.elasticIpRangedUpdate(request.getId(), request.getIpVersion(),
                request.getAllocationRanges());

        request.setUsed_ip_count(0L);

        LOG.debug("Create an elastic ip range success, request: {}", request);

        return request;
    }

    /**
     * Delete an elastic ip range.
     * @param elasticIpRangeId Uuid of the elastic ip range
     * @throws ElasticIpRangeNotFoundException The elastic ip range does not exist
     * @throws ElasticIpRangeInUseException There is existed elastic ip that has assigned an address belongs
     *                                      to this elastic ip range
     * @throws ElasticIpInternalErrorException Internal process (database / lock etc.) error
     */
    @DurationStatistics
    public void deleteElasticIpRange(String elasticIpRangeId) throws Exception {
        LOG.debug("Delete an elastic ip range, requestId: {}", elasticIpRangeId);

        ElasticIpRange range = elasticIpRangeRepo.findItem(elasticIpRangeId);
        if (range == null) {
            throw new ElasticIpRangeNotFoundException();
        }

        elasticIpAllocator.elasticIpRangedDelete(elasticIpRangeId, range.getIpVersion());

        elasticIpRangeRepo.deleteItem(elasticIpRangeId);

        LOG.debug("Delete an elastic ip range success, requestId: {}", elasticIpRangeId);
    }

    /**
     * Update an elastic ip range.
     * @param request elastic ip range configuration
     * @return Elastic ip range information
     * @throws ElasticIpRangeNotFoundException The elastic ip range already exists
     * @throws ElasticIpRangeParameterException Not allowed to modify the parameter (ip_version etc.)
     * @throws ElasticIpRangeInUseException There is existed elastic ip that has been assigned an address belongs
     *                                      to the range which will be removed after the update
     * @throws ElasticIpInternalErrorException Internal process (database / lock etc.) error
     */
    @DurationStatistics
    public ElasticIpRangeInfo updateElasticIpRange(ElasticIpRangeInfo request) throws Exception {
        LOG.debug("Update an elastic ip range, request: {}", request);

        String rangeId = request.getId();
        ElasticIpRange elasticIpRange = elasticIpRangeRepo.findItem(rangeId);
        if (elasticIpRange == null) {
            throw new ElasticIpRangeNotFoundException();
        }

        if (request.getIpVersion() != null && !request.getIpVersion().equals(elasticIpRange.getIpVersion())) {
            LOG.debug("Modify IP version of an elastic ip range is not allowed, request: {}", request);
            throw new ElasticIpRangeParameterException();
        }

        if (request.getDescription() != null) {
            elasticIpRange.setDescription(request.getDescription());
        }

        if (request.getName() != null) {
            elasticIpRange.setName(request.getName());
        }

        if (request.getAllocationRanges() != null) {
            elasticIpAllocator.elasticIpRangedUpdate(rangeId, request.getIpVersion(), request.getAllocationRanges());

            elasticIpRange.setAllocationRanges(request.getAllocationRanges());
        }

        elasticIpRangeRepo.addItem(elasticIpRange);

        LOG.debug("Update an elastic ip range success, request: {}", request);

        return getElasticIpRange(rangeId);
    }

    /**
     * Get an elastic ip range.
     * @param elasticIpRangeId Uuid of the elastic ip range
     * @return Elastic ip range information
     * @throws ElasticIpRangeNotFoundException The elastic ip range does not exist
     * @throws ElasticIpInternalErrorException Internal process (database etc.) error
     */
    @DurationStatistics
    public ElasticIpRangeInfo getElasticIpRange(String elasticIpRangeId) throws Exception {
        LOG.debug("get an elastic ip range, requestId: {}", elasticIpRangeId);

        ElasticIpRange elasticIpRange = elasticIpRangeRepo.findItem(elasticIpRangeId);
        if (elasticIpRange == null) {
            throw new ElasticIpRangeNotFoundException();
        }

        ElasticIpRangeInfo result = new ElasticIpRangeInfo(elasticIpRange);

        long allocatedCount = 0;
        Map<String, ElasticIp> elasticIpMap = elasticIpRepo.findAllItems();
        for (ElasticIp item: elasticIpMap.values()) {
            if (item.getRangeId().equals(elasticIpRangeId)) {
                allocatedCount += 1;
            }
        }
        result.setUsed_ip_count(allocatedCount);

        return result;
    }

    /**
     * Get a list of elastic ip ranges.
     * @return A list of elastic ip ranges information
     * @throws ElasticIpInternalErrorException Internal process (database etc.) error
     */
    @DurationStatistics
    public List<ElasticIpRangeInfo> getElasticIpRanges() throws Exception {
        LOG.debug("get all elastic ip ranges");

        Map<String, ElasticIpRangeInfo> resultMap = new HashMap<>();
        Map<String, ElasticIpRange> elasticIpRangeMap = elasticIpRangeRepo.findAllItems();
        for (ElasticIpRange range: elasticIpRangeMap.values()) {
            ElasticIpRangeInfo resultItem = new ElasticIpRangeInfo(range);
            resultMap.put(range.getId(), resultItem);
        }

        Map<String, ElasticIp> elasticIpMap = elasticIpRepo.findAllItems();
        for (ElasticIp item: elasticIpMap.values()) {
            ElasticIpRangeInfo resultItem = resultMap.get(item.getRangeId());
            if (resultItem != null) {
                resultItem.setUsed_ip_count(resultItem.getUsed_ip_count() + 1);
            }
        }

        return new ArrayList<>(resultMap.values());
    }
}
