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
        cache = cacheFactory.getCache(NcmInfo.class, "nmm_ncminfo_cache");
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
     * find information about all NCM.
     * @return list of NcmInfo
     * @throws Exception;
     */
    public NcmInfo getNcmInfoById(String ncmId) throws Exception {
        return cache.get(ncmId);
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
     * add a new Ncm info to repository
     *
     * @param ncmInfo new Ncm information
     * @throws CacheException or DbException
     */
    public void updateNcmInfo(NcmInfo ncmInfo) throws CacheException {
        String ncmId = ncmInfo.getId();
        logger.info("Update an NcmInfo entry, NCM Id:" + ncmId);

        try (Transaction tx = cache.getTransaction().start()) {
            cache.put(ncmId, ncmInfo);
            tx.commit();
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Update an ncmInfo entry error: "+e.getMessage());
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

    public long ncmCount()
    {
        return cache.size();
    }
}