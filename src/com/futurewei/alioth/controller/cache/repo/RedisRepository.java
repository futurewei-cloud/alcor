package com.futurewei.alioth.controller.cache.repo;

import com.futurewei.alioth.controller.model.VpcState;

import java.util.Map;

public interface RedisRepository {

    VpcState findVpc(String id);

    Map<String, VpcState> findAllVpcs();

    void add(VpcState vpcState);

    void delete(String id);
}
