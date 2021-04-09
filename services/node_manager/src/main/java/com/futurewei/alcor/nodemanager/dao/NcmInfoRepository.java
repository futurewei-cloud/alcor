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
import com.futurewei.alcor.web.entity.node.NcmInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Node Manager maintains a mapping of NCM information by NcmId.
 * We need to know which node belongs to a given NCM.
 */
@Repository
public class NcmInfoRepository {
    private static final Logger logger = LoggerFactory.getLogger(NcmInfoRepository.class);
    // Map of NcmId and NcmInfo
    private ICache<String, NcmInfo> cache;

    @Autowired
    public NcmInfoRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(NcmInfo.class);
    }

    public ICache<String, NcmInfo> getCache() {
        return cache;
    }

    @PostConstruct
    private void init() {
        logger.info("NcmInfoRepository init completed");
    }

    /**
     * find an NcmInfo item from the repository.
     *
     * @param id NcmId
     * @return NcmInfo
     * @throws CacheException exception or DbException
     */
    public NcmInfo findNcmInfo(String id) throws CacheException {
        return cache.get(id);
    }

    /**
     * find information about all NCM
     *
     * @return NcmInfo
     * @throws CacheException or DbException
     */
    public Map findAllNcmInfo() throws CacheException {
        return cache.getAll();
    }

    /**
     * add a new Ncm info to repository
     *
     * @param ncmInfo new Ncm information
     * @throws CacheException or DbException
     */
    public void addNcmInfo(NcmInfo ncmInfo) throws CacheException {
        String ncmId = ncmInfo.getId();
        logger.info("Add an NcmInfo entry, NCM Id:" + ncmId);

        // TODO: Push Transaction out of DAO.
        // Transaction should cover DPM, NCM and local store.
        // This means NMM will have to maintain "Transaction context" covering DPM, NCM and local
        // changes.
        // Having to initiate a transaction in DPM, NCM and local store prior to modification
        // and following it by a commit or rollback is very much like Two Phase Commit: tricky
        // and expensive.
        // There is also the problem of "obtaining" a "transaction context" from DPM and NCM which
        // are physically separate processes possibly running on different machines. This is a roadmap
        // item.
        try (Transaction tx = cache.getTransaction().start()) {
            cache.put(ncmId, ncmInfo);
            tx.commit();
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Add an ncmInfo entry error: "+e.getMessage());
        }
    }

    /**
     * Append a new nodes to an NCM.
     *
     * @param ncmId, NCM to which new nodes are being appended
     * @param nodeIds, the new nodes
     * @throws CacheException or DbException
     */
    public void appendNodes(String ncmId,  List<String> nodeIds) throws CacheException {
        logger.info("Append an nodes to NCM entry, NCM Id:" + ncmId);

        try (Transaction tx = cache.getTransaction().start()) {
            NcmInfo ncmInfo = cache.get(ncmId);
            ncmInfo.appendNodes(nodeIds);
            cache.put(ncmId, ncmInfo);
            tx.commit();
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Add an NcmInfo entry error: "+e.getMessage());
        }
    }

    /**
     * @param ncmId
     * @param nodeIds
     * @throws CacheException or DbException
     */
    public void removeNodes(String ncmId,  List<String> nodeIds) throws CacheException {
        logger.info("Remove nodes from NCM, NCM Id:" + ncmId);
        try (Transaction tx = cache.getTransaction().start()) {
            NcmInfo ncmInfo = cache.get(ncmId);
            ncmInfo.removeNodes(nodeIds);
            cache.put(ncmId, ncmInfo);
            tx.commit();
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Add an NcmInfo entry error: "+e.getMessage());
        }
    }

    /**
     * delete a node from repository
     *
     * @param id NcmId
     * @throws CacheException or DbException
     */
    public void deleteNcmInfo(String id) throws CacheException{
        logger.info("Delete NcmInfo, NcmId:" + id);
        try (Transaction tx = cache.getTransaction().start()) {
            cache.remove(id);
            tx.commit();
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            logger.error("delete an NcmInfo error: "+e.getMessage());
        }
    }
}