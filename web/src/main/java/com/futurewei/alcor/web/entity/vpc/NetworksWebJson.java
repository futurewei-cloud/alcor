/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.web.entity.vpc;

import java.util.ArrayList;
import java.util.List;

public class NetworksWebJson {

    private ArrayList<VpcEntity> networks;

    public NetworksWebJson() {
    }

    public NetworksWebJson(List<VpcEntity> networks) {
        this.networks = new ArrayList<>(networks);
    }

    public ArrayList<VpcEntity> getNetworks() {
        return networks;
    }

    public void setNetworks(ArrayList<VpcEntity> networks) {
        this.networks = networks;
    }
}
