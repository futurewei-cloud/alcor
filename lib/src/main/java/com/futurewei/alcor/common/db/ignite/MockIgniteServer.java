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
package com.futurewei.alcor.common.db.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class MockIgniteServer {
    private static Ignite igniteServer;

    @BeforeClass
    public static void init() {
        try {
            igniteServer = Ignition.start();
        } catch (IgniteException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void close() {
        if (igniteServer != null) {
            igniteServer.close();
        }
    }
}
