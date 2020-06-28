package com.futurewei.alcor.elasticipmanager.service;

import com.futurewei.alcor.web.entity.elasticip.*;

import java.util.List;


public interface ElasticIpRangeService {

    ElasticIpRangeInfo createElasticIpRange(ElasticIpRangeInfo request) throws Exception;

    void deleteElasticIpRange(String elasticIpRangeId) throws Exception;

    ElasticIpRangeInfo updateElasticIpRange(ElasticIpRangeInfo request) throws Exception;

    ElasticIpRangeInfo getElasticIpRange(String elasticIpRangeId) throws Exception;

    List<ElasticIpRangeInfo> getElasticIpRanges() throws Exception;
}
