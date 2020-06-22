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

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.apache.ignite.Ignite;
import org.apache.ignite.client.ClientException;
import org.apache.ignite.internal.client.thin.ClientServerError;

import java.util.logging.Level;

import static org.apache.ignite.transactions.TransactionConcurrency.PESSIMISTIC;
import static org.apache.ignite.transactions.TransactionIsolation.SERIALIZABLE;

public class IgniteTransaction implements Transaction {
    private static final Logger logger = LoggerFactory.getLogger();

    private Ignite client;
    private org.apache.ignite.transactions.Transaction transaction;

    public IgniteTransaction(Ignite client) {
        this.client = client;
    }

    @Override
    public Transaction start() throws CacheException {
        try{
            transaction = client.transactions().txStart(PESSIMISTIC, SERIALIZABLE);
        } catch (ClientServerError e) {
            logger.log(Level.WARNING, "IgniteTransaction start error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        } catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteTransaction start error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }

        return this;
    }

    @Override
    public void commit() throws CacheException {
        try {
            transaction.commit();
        } catch (ClientServerError e) {
            logger.log(Level.WARNING, "IgniteTransaction commit error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        } catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteTransaction commit error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public void rollback() throws CacheException {
        try {
            transaction.rollback();
        } catch (ClientServerError e) {
            logger.log(Level.WARNING, "IgniteTransaction rollback error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        } catch (ClientException e) {
            logger.log(Level.WARNING, "IgniteTransaction rollback error:" + e.getMessage());
            throw new CacheException(e.getMessage());
        }
    }

    @Override
    public void close() {
        if (transaction != null) {
            transaction.close();
        }
    }
}
