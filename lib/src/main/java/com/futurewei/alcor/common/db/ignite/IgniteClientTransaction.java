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
import org.apache.ignite.client.ClientException;
import org.apache.ignite.client.ClientTransaction;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.internal.client.thin.ClientServerError;
import org.springframework.context.annotation.Description;

import java.util.logging.Level;

import static org.apache.ignite.transactions.TransactionConcurrency.PESSIMISTIC;
import static org.apache.ignite.transactions.TransactionIsolation.SERIALIZABLE;

public class IgniteClientTransaction implements Transaction {
    private static final Logger logger = LoggerFactory.getLogger();

    private final IgniteClient igniteClient;
    private ClientTransaction clientTransaction;

    public IgniteClientTransaction(IgniteClient igniteClient) {
        this.igniteClient = igniteClient;
    }

    @Override
    public Transaction start() throws CacheException {
        try{
            clientTransaction = igniteClient.transactions().txStart(PESSIMISTIC, SERIALIZABLE);
        } catch (ClientServerError | ClientException e) {
            logger.log(Level.WARNING, "IgniteTransaction start error:" + e.getMessage());
            throw new CacheException("IgniteTransaction start error:" + e.getMessage());
        }

        return this;
    }

    @Override
    public void commit() throws CacheException {
        try{
            clientTransaction.commit();
        } catch (ClientServerError | ClientException e) {
            logger.log(Level.WARNING, "IgniteTransaction commit error:" + e.getMessage());
            throw new CacheException("IgniteTransaction commit error:" + e.getMessage());
        }
    }

    @Override
    public void rollback() throws CacheException {
        try{
            clientTransaction.rollback();
        } catch (ClientServerError | ClientException e) {
            logger.log(Level.WARNING, "IgniteTransaction rollback error:" + e.getMessage());
            throw new CacheException("IgniteTransaction rollback error:" + e.getMessage());
        }
    }

    @Override
    public void close() {
        if (clientTransaction != null) {
            clientTransaction.close();
        }
    }
}
