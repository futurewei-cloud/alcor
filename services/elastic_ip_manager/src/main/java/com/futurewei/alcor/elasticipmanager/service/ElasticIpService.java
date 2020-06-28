package com.futurewei.alcor.privateipmanager.service;

import com.futurewei.alcor.web.entity.elasticip.*;

import java.util.List;

public interface ElasticIpService {

    ElasticIpInfo createElasticIp(ElasticIpInfo request) throws Exception;

    void deleteElasticIp(String projectId, String elasticIpId) throws Exception;

    ElasticIpInfo getElasticIp(String projectId, String elasticIpId) throws Exception;

    List<ElasticIpInfo> getElasticIps(String projectId) throws Exception;

    ElasticIpInfo updateElasticIp(ElasticIpInfo request) throws Exception;

    List<ElasticIpInfo> createElasticIps(List<ElasticIpInfo> bulkRequest) throws Exception;

    void deleteElasticIps(String projectId, List<String> bulkRequest) throws Exception;
}
