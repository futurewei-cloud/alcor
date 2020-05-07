package com.futurewei.alcor.web.allocator;

import java.util.List;

public interface KeyAllocator {

    public Long allocate() throws Exception;

    void release(Long key) throws Exception;

    boolean validate(Long key);

}
