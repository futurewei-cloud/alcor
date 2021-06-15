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

package com.futurewei.alcor.common.service;

import com.futurewei.alcor.common.exception.CacheException;
import com.futurewei.alcor.common.logging.Logger;
import com.futurewei.alcor.common.logging.LoggerFactory;
import com.futurewei.alcor.common.repo.Transaction;
import org.apache.ignite.client.ClientException;
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
