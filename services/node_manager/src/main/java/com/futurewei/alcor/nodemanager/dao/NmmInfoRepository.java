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
package com.futurewei.alcor.nodemanager.dao;

import com.futurewei.alcor.common.db.CacheException;
import com.futurewei.alcor.common.db.CacheFactory;
import com.futurewei.alcor.common.db.ICache;
import com.futurewei.alcor.common.db.Transaction;
import com.futurewei.alcor.common.db.repo.ICacheRepository;
import com.futurewei.alcor.common.stats.DurationStatistics;
import com.futurewei.alcor.web.entity.node.NmmInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Repository
public class NmmInfoRepository implements ICacheRepository<NmmInfo> {
    private static final Logger logger = LoggerFactory.getLogger(NmmInfoRepository.class);
    private ICache<String, NmmInfo> cache;

    @Autowired
    public NmmInfoRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(NmmInfo.class);
    }

    public ICache<String, NmmInfo> getCache() {
        return cache;
    }

    @PostConstruct
    private void init() {
        logger.info("NmmInfoRepository init completed");
    }

    /**
     * find an NmmInfo item from the repository.
     *
     * @param id NcmId
     * @return NmmInfo
     * @throws CacheException exception or DbException
     */
    @Override
    public NmmInfo findItem(String id) throws CacheException {
        return cache.get(id);
    }

    /**
     * find information about all NCM
     *
     * @return NmmInfo
     * @throws CacheException or DbException
     */
    @Override
    public Map findAllItems() throws CacheException {
        return cache.getAll();
    }

    /**
     * find all NcmInfo information by filter: Not needed and not supported.
     * @param queryParams url request params
     * @return Map of NmmInfo items
     * @throws CacheException exception
     */
    @Override
    public Map<String, NmmInfo> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        String error = "NCM query by filter is invalid operation";
        logger.error(error);
        throw new CacheException(error);
    }

    /**
     * add a new NMM info to repository
     *
     * @param nmmInfo new NMM information
     * @throws CacheException or DbException
     */
    @Override
    public void addItem(NmmInfo nmmInfo) throws CacheException {
        String ncmId = nmmInfo.getNcmId();
        logger.info("Add an NmmInfo entry, NCM Id:" + ncmId);

        // TODO: Push Transaction out of DAO.
        // Transaction should cover DPM, NCM and local store.
        // This means NMM will have to maintain "Transaction context" covering DPM, NCM and local
        // changes.
        // Having to initiate a transaction in DPM, NCM and local store prior to modification
        // and following it by a commit or rollback is very much like Two Phase Commit: tricky
        // and expensive.
        // There is also the problem of "obtaining" a "transaction context" from DPM and NCM which
        // are physically seperate processes possibly running on different machines. This is a roadmap
        // item.
        try (Transaction tx = cache.getTransaction().start()) {
            cache.put(ncmId, nmmInfo);
            tx.commit();
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Add an NmmInfo entry error: "+e.getMessage());
        }
    }

    /**
     * Not needed and not supported.
     * @param items List of NmmInfo to be added.
     */
    @Override
    @DurationStatistics
    public void addItems(List<NmmInfo> items) throws CacheException {
        String error = "NMM does not support bulk inserts";
        throw new CacheException(error);
    }

    /**
     * Not needed and not supported.
     * add multiple nodes' information to node repository
     *
     * @param nodes new nodes list
     * @throws CacheException or DbException
     */
    public void addItemBulkTransaction(List<NmmInfo> nodes) throws CacheException {
        String error = "NMM does not support bulk transactions";
        logger.error(error);
        throw new CacheException(error);
    }

    /**
     * delete a node from repository
     *
     * @param id NcmId
     * @throws CacheException or DbException
     */
    @Override
    public void deleteItem(String id) throws CacheException{
        logger.info("Delete NmmInfo, NcmId:" + id);
        try (Transaction tx = cache.getTransaction().start()) {
            cache.remove(id);
            tx.commit();
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            logger.error("delete an NmmInfo error: "+e.getMessage());
        }
    }
}