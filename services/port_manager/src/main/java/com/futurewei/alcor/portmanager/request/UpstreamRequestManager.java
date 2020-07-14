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
package com.futurewei.alcor.portmanager.request;

import java.util.HashMap;
import java.util.Map;

public class UpstreamRequestManager {
    private static Map<Class, UpstreamRequest> upstreamRequestMap = new HashMap<>();

    public static void registerUpstreamRequest(UpstreamRequest upstreamRequest) {
        upstreamRequestMap.put(upstreamRequest.getClass(), upstreamRequest);
    }

    public static UpstreamRequest getUpstreamRequest(Class tClass) {
        return upstreamRequestMap.get(tClass);
    }
}
