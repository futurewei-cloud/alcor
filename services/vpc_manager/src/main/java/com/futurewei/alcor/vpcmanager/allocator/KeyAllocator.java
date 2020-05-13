package com.futurewei.alcor.vpcmanager.allocator;

public interface KeyAllocator {

    public Long allocate() throws Exception;

    void release(Long key) throws Exception;

    boolean validate(Long key);

}
