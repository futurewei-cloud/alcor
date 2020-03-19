package com.futurewei.alcor.controller.db.ignite;

import com.futurewei.alcor.controller.exception.CacheException;
import com.futurewei.alcor.controller.db.Transaction;
import com.futurewei.alcor.controller.logging.Logger;
import com.futurewei.alcor.controller.logging.LoggerFactory;
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
