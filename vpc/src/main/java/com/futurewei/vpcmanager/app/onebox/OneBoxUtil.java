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

package com.futurewei.vpcmanager.app.onebox;

import com.futurewei.vpcmanager.model.*;

import java.util.ArrayList;
import java.util.List;

// NOTE: This file is only used for demo purpose.
//       Please don't use it in production
public class OneBoxUtil {

    private static final int THREADS_LIMIT = 100;
    private static final int TIMEOUT = 600;


    public static List<HostInfo> LoadNodes(List<HostInfo> hosts) {
        List<HostInfo> hostInfoList = new ArrayList<>(hosts);
        for (int i = 0; i < hostInfoList.size(); i++) {
            HostInfo host = hostInfoList.get(i);
            host.setGRPCServerPort(OneBoxConfig.GRPC_SERVER_PORT + i);
        }

        return hostInfoList;
    }

    // This function generates port state solely based on the container host

    private static String GenereateMacAddress(int index) {
        return "0e:73:ae:c8:" + Integer.toHexString((index + 6) / 256) + ":" + Integer.toHexString((index + 6) % 256);
    }

    private static String GenereateIpAddress(int index) {
        return "10.0." + (index + 6) / 256 + "." + (index + 6) % 256;
    }

}
