/*
MIT License
Copyright(c) 2020 Futurewei Cloud

    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction,
    including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons
    to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.futurewei.alcor.common.db.ignite;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteException;

import java.util.logging.Level;

import static org.apache.ignite.transactions.TransactionConcurrency.OPTIMISTIC;
import static org.apache.ignite.transactions.TransactionConcurrency.PESSIMISTIC;
import static org.apache.ignite.transactions.TransactionIsolation.SERIALIZABLE;
import static org.apache.ignite.transactions.TransactionIsolation.REPEATABLE_READ;

public class IgniteTransaction implements Transaction {
    private static final Logger logger = LoggerFactory.getLogger();

    private final Ignite client;
    private org.apache.ignite.transactions.Transaction transaction;

    public IgniteTransaction(Ignite client) {
        this.client = client;
    }

    @Override
    public Transaction start() throws CacheException {
        transaction = client.transactions().txStart(OPTIMISTIC, REPEATABLE_READ);
        return this;
    }

    @Override
    public void commit() throws CacheException {
        try {
            transaction.commit();
        } catch (IgniteException e) {
            //*?// PERF_NO_LOG logger.log(Level.WARNING, "IgniteTransaction commit error:" + e.getMessage());
            throw new CacheException("IgniteTransaction commit error:" + e.getMessage());
        }
    }

    @Override
    public void rollback() throws CacheException {
        try {
            transaction.rollback();
        } catch (IgniteException e) {
            //*?// PERF_NO_LOG logger.log(Level.WARNING, "IgniteTransaction rollback error:" + e.getMessage());
            throw new CacheException("IgniteTransaction rollback error:" + e.getMessage());
        }
    }

    @Override
    public void close() throws CacheException {
        if (transaction != null) {
            try {
                transaction.close();
            } catch (IgniteException e) {
                //*?// PERF_NO_LOG logger.log(Level.WARNING, "IgniteTransaction close error: " + e.getMessage());
                throw new CacheException("IgniteTransaction close error: " + e.getMessage());
            }
        }
    }
}
