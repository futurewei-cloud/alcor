package com.futurewei.alcor.nodemanager;

import com.futurewei.alcor.controller.app.onebox.OneBoxConfig;
import com.futurewei.alcor.controller.app.onebox.OneBoxUtil;
import com.futurewei.alcor.controller.model.HostInfo;
import com.futurewei.alcor.controller.resourcemgr.physical.nodemgmt.DataCenterConfig;
import com.futurewei.alcor.controller.resourcemgr.physical.nodemgmt.DataCenterConfigLoader;
import com.futurewei.alcor.controller.resourcemgr.physical.nodemgmt.NodeManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.logging.Level;

@SpringBootApplication
public class NodeManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(NodeManagerApplication.class, args);
    }

}
