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
