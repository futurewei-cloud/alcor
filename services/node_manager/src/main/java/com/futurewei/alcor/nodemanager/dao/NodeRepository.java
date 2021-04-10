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
import com.futurewei.alcor.web.entity.node.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
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

    /**
     * add a new node info to node repository
     *
     * @param id node id
     * @return node information
     * @throws Exception Db or cache operation exception
     */
    @Override
    public NodeInfo findItem(String id) throws CacheException {
        return cache.get(id);
    }

    /**
     * find all nodes' information
     *
     * @param
     * @return nodes map
     * @throws Exception Db or cache operation exception
     */
    @Override
    public Map findAllItems() throws CacheException {
        return cache.getAll();
    }

    /**
     * find all nodes' information by filter
     * @param queryParams url request params
     * @return
     * @throws CacheException
     */
    @Override
    public Map<String, NodeInfo> findAllItems(Map<String, Object[]> queryParams) throws CacheException {
        return cache.getAll(queryParams);
    }

    /**
     * add a new node info to node repository
     *
     * @param nodeInfo new node information
     * @return void
     * @throws Exception Db or cache operation exception
     */
    @Override
    public void addItem(NodeInfo nodeInfo) throws CacheException {
        logger.info("Add a node, Node Id:" + nodeInfo.getId());
        try (Transaction tx = cache.getTransaction().start()) {
            cache.put(nodeInfo.getId(), nodeInfo);
            tx.commit();
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Add a node error: "+e.getMessage());
        }
    }

    @Override
    @DurationStatistics
    public void addItems(List<NodeInfo> items) throws CacheException {
        Map<String, NodeInfo> nodeInfoMap = items.stream().collect(Collectors.toMap(NodeInfo::getId, Function.identity()));
        cache.putAll(nodeInfoMap);
    }

    /**
     * add multiple nodes' information to node repository
     *
     * @param nodes new nodes list
     * @return void
     * @throws Exception Db or cache operation exception
     */
    public void addItemBulkTransaction(List<NodeInfo> nodes) throws CacheException {
        // Mysterious code path:
        // The log lines addItemBulkTransaction: after GET and addItemBulkTransaction: after COMMIT do not execute
        // but c = a + b does.
        // Just for the record these debug log entries are left in
        // but commented out in this commit. They will be removed in the next commit.

        // logger.info("addItemBulkTransaction: ENTRY");

        logger.info("Add nodes: " + nodes.size());

        // int a = 101, b = 202, c = 0;
        // NodeInfo g1 = null, g2 = null;

        try (Transaction tx = cache.getTransaction().start()) {

            // logger.info("addItemBulkTransaction: after TXN");

            for (NodeInfo node : nodes) {

                // logger.info("addItemBulkTransaction: before PUT: " + node.getId());

                cache.put(node.getId(), node);

                // logger.info("addItemBulkTransaction: after PUT: " + node.getId());
                // g1 = cache.get(node.getId());
                // logger.info("addItemBulkTransaction: after GET: " + node.getId() + " " + g1 != null ? g1.toString() : "-null-"); // does not execute
                // c = a + b; // executes
            }
            tx.commit();

            // for (NodeInfo node : nodes) {
            //    g2 = cache.get(node.getId());
            //    logger.info("addItemBulkTransaction: after COMMIT: " + node.getId() + " " + g2 != null ? g2.toString() : "-null-"); // does not execute
            // }
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Add bulk nodes error: "+e.getMessage());
        }

        // logger.info("addItemBulkTransaction: EXIT c = " + c + ", g1 = " + (g1 != null ? g1.toString() : "-null-") + " g2 = " + (g2 != null ? g2.toString() : "-null-") );
    }

    /**
     * delete a node from repository
     *
     * @param id   node's id
     * @return void
     * @throws Exception Db or cache operation exception
     */
    @Override
    public void deleteItem(String id) throws CacheException{
        logger.info("Delete node, Node Id:" + id);
        try (Transaction tx = cache.getTransaction().start()) {
            cache.remove(id);
            tx.commit();
        } catch (CacheException e) {
            throw e;
        } catch (Exception e) {
            logger.error("delete a node error: "+e.getMessage());
        }
    }
}
