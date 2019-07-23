package com.futurewei.alioth.controller.cache.repo;

import com.futurewei.alioth.controller.model.VpcState;

import java.util.Map;

public class RedisRepositoryImpl implements RedisRepository {

    @Override
    public VpcState findVpc(String id) {
        return null;
    }

    @Override
    public Map<String, VpcState> findAllVpcs() {
        return null;
    }

    @Override
    public void add(VpcState vpcState) {

    }

    @Override
    public void delete(String id) {

    }
}
