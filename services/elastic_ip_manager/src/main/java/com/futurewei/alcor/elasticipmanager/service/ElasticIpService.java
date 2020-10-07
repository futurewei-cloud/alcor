package com.futurewei.alcor.elasticipmanager.service;

import com.futurewei.alcor.web.entity.elasticip.*;

import java.util.List;
import java.util.Map;

public interface ElasticIpService {

    ElasticIpInfo createElasticIp(ElasticIpInfo request) throws Exception;

    void deleteElasticIp(String projectId, String elasticIpId) throws Exception;

    ElasticIpInfo getElasticIp(String projectId, String elasticIpId) throws Exception;

    List<ElasticIpInfo> getElasticIps(String projectId, Map<String, Object[]> queryParams) throws Exception;

    ElasticIpInfo updateElasticIp(ElasticIpInfo request) throws Exception;

    List<ElasticIpInfo> createElasticIps(List<ElasticIpInfo> bulkRequest) throws Exception;

    void deleteElasticIps(String projectId, List<String> bulkRequest) throws Exception;
}
