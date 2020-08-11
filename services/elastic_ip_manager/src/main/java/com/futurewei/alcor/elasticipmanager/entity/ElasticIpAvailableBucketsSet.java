package com.futurewei.alcor.elasticipmanager.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.BitSet;


public class ElasticIpAvailableBucketsSet {
    @JsonProperty("range_id")
    private String rangeId;

    @JsonProperty("available_buckets_bitset")
    private BitSet AvailableBucketsBitset;

    public ElasticIpAvailableBucketsSet(String rangeId, BitSet availableBucketsBitset) {
        this.rangeId = rangeId;
        AvailableBucketsBitset = availableBucketsBitset;
    }

    public String getRangeId() {
        return rangeId;
    }

    public void setRangeId(String rangeId) {
        this.rangeId = rangeId;
    }

    public BitSet getAvailableBucketsBitset() {
        return AvailableBucketsBitset;
    }

    public void setAvailableBucketsBitset(BitSet availableBucketsBitset) {
        AvailableBucketsBitset = availableBucketsBitset;
    }

    @Override
    public String toString() {
        return "ElasticIpAvailableBucketsSet{" +
                "rangeId='" + rangeId + '\'' +
                ", AvailableBucketsBitset=" + AvailableBucketsBitset +
                '}';
    }
}
