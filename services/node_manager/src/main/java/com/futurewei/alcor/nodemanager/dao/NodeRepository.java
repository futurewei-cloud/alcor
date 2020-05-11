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
import com.futurewei.alcor.common.repo.ICacheRepository;
import com.futurewei.alcor.nodemanager.entity.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Repository
@ComponentScan(value = "com.futurewei.alcor.common.db")
public class NodeRepository implements ICacheRepository<NodeInfo> {
    private static final Logger logger = LoggerFactory.getLogger(NodeRepository.class);
    private ICache<String, NodeInfo> cache;

    @Autowired
    public NodeRepository(CacheFactory cacheFactory) {
        cache = cacheFactory.getCache(NodeInfo.class);
    }

    public ICache<String, NodeInfo> getCache() {
        return cache;
    }

    @PostConstruct
    private void init() {
        logger.info("NodeRepository init completed");
    }

    @Override
    public NodeInfo findItem(String id) throws CacheException {
        return cache.get(id);
    }

    @Override
    public Map findAllItems() throws CacheException {
        return cache.getAll();
    }

//    @Override
//    public void addItem(NodeInfo nodeInfo) throws CacheException {
//        logger.info("Add node, Node Id:" + nodeInfo.getId());
//        cache.put(nodeInfo.getId(), nodeInfo);
//    }
//
//    @Override
//    public void deleteItem(String id) throws CacheException {
//        logger.info("Delete node, Node Id:" + id);
//        cache.remove(id);
//    }

    /**
     * add a new node info to node repository
     *
     * @param nodeInfo new node information
     * @return void
     * @throws Exception Db or cache operation exception
     */
    @Override
    public void addItem(NodeInfo nodeInfo) throws CacheException {
        logger.info("Add node, Node Id:" + nodeInfo.getId());
        try (Transaction tx = cache.getTransaction().start()) {
            cache.put(nodeInfo.getId(), nodeInfo);
            tx.commit();
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
        }
    }

    /**
     * add a new node info to node repository
     *
     * @param nodes new nodes list
     * @return void
     * @throws Exception Db or cache operation exception
     */
    public void addItemBulkTransaction(List<NodeInfo> nodes) throws CacheException {
        logger.info("Add nodes: " + nodes.size());
        try (Transaction tx = cache.getTransaction().start()) {
            for (NodeInfo node : nodes) {
                cache.put(node.getId(), node);
            }
            tx.commit();
        } catch (CacheException e) {
            throw e;
        }catch (Exception e) {

        }
    }

    @Override
    public void deleteItem(String id) throws CacheException {
        logger.info("Delete node, Node Id:" + id);
        try (Transaction tx = cache.getTransaction().start()) {
            cache.remove(id);
            tx.commit();
        } catch (CacheException e) {
            throw e;
        }catch(Exception e) {

        }
    }
}
