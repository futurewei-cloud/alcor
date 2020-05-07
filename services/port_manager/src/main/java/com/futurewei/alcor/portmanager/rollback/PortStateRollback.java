package com.futurewei.alcor.portmanager.rollback;


public interface PortStateRollback {
    void doRollback() throws Exception;
}
