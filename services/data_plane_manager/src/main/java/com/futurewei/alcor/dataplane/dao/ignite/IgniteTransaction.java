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

package com.futurewei.alcor.dataplane.dao.ignite;

import com.futurewei.alcor.common.exception.CacheException;
import com.futurewei.alcor.common.repo.Transaction;
import org.apache.ignite.client.ClientException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.apache.ignite.client.ClientTransaction;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.internal.client.thin.ClientServerError;

import java.util.logging.Level;

public class IgniteTransaction implements Transaction {
    private static final Logger logger = LoggerFactory.getLogger();

    private IgniteClient igniteClient;
    private ClientTransaction clientTransaction;

    public IgniteTransaction(IgniteClient igniteClient) {
        this.igniteClient = igniteClient;
    }

    public void start() throws CacheException {
        try {
            clientTransaction = igniteClient.transactions().txStart();
        } catch (ClientServerError e) {
            logger.log(Level.WARNING, "IgniteTransaction start error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        } catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteTransaction start error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    public void commit() throws CacheException {
        try {
            clientTransaction.commit();
        } catch (ClientServerError e) {
            logger.log(Level.WARNING, "IgniteTransaction commit error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        } catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteTransaction commit error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    public void rollback() throws CacheException {
        try {
            clientTransaction.rollback();
        } catch (ClientServerError e) {
            logger.log(Level.WARNING, "IgniteTransaction rollback error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        } catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteTransaction rollback error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    public void close() {
        clientTransaction.close();
    }
}
