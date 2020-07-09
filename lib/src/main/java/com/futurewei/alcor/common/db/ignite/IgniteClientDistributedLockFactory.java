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

import com.futurewei.alcor.common.db.IDistributedLock;
import com.futurewei.alcor.common.db.IDistributedLockFactory;
import org.apache.ignite.client.IgniteClient;

public class IgniteClientDistributedLockFactory implements IDistributedLockFactory {

    private final IgniteClient igniteClient;
    private final int tryLockInterval;
    private final int expireTime;

    public IgniteClientDistributedLockFactory(IgniteClient igniteClient, int interval, int expire) {
        this.igniteClient = igniteClient;
        this.tryLockInterval = interval;
        this.expireTime = expire;
    }

    @Override
    public <T> IDistributedLock getDistributedLock(Class<T> t) {
        return new IgniteClientDistributedLock(this.igniteClient, t.getName(), this.tryLockInterval, this.expireTime);
    }
}
