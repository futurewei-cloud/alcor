/*
Copyright 2019 The Alcor Authors.

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

package com.futurewei.alcor.elasticipmanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.web.entity.elasticip.ElasticIp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ElasticIpRepo extends RepoResource<ElasticIp> {

    @Autowired
    public ElasticIpRepo(CacheFactory cacheFactory) {
        super(cacheFactory);
    }

    @Override
    public Class<ElasticIp> getResourceClass() {
        return ElasticIp.class;
    }

    @Override
    public void addItems(List<ElasticIp> items) throws CacheException {

    }
}
