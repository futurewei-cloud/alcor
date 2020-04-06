/*Copyright 2019 The Alcor Authors.

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
package com.futurewei.alcor.macmanager.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class OuiState implements Serializable {
    String ou;  //project_id+"/"+vpc_id
    String oui;

    public OuiState() {

    }

    public OuiState(OuiState state) {
        this(state.ou, state.oui);
    }

    public OuiState(String ou, String oui) {
        this.ou = ou;
        this.oui = oui;
    }
}
