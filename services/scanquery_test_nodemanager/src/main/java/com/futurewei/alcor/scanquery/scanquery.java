package com.futurewei.alcor.scanquery;
import com.futurewei.alcor.common.db.ignite.query.MapPredicate;
import com.futurewei.alcor.common.db.ignite.query.ScanQueryBuilder;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheKeyConfiguration;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.ClientCacheConfiguration;
import org.apache.ignite.client.ClientTransaction;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.transactions.Transaction;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.lang.IgniteClosure;
import com.futurewei.alcor.web.entity.node.NodeInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import javax.cache.Cache;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.function.BinaryOperator;

public class scanquery {
    static int[] ipAddress = new int[4];
    static int[] macAddress = new int[6];
    static String igniteAddress;
    static String nodePrefix = "node";
    static String ncmPrefix  = "ncm";
    static int    ncmNum = 0;
    static int ncmMax = 1000;
    static boolean generateData = false;

    public scanquery(String uuid) {
    }

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("need input: json_output_filename number_of_entries number_of_queries [-g(enerate data)] [ignite ipaddress]");
            System.exit(-1);
        }
        final String tblName = "nmm_nodeinfo_cache";
        final String schName = "alcor";

        int exitCode = -1;

        String jsonFile = args[0];
        int    entryCount = Integer.valueOf(args[1]);
        int    qryCount = Integer.valueOf(args[2]);
        if (args.length > 3 && args[3].equals("-g"))
            generateData = true;

        if (args.length > 4)
            igniteAddress = args[4];
        else
            igniteAddress = "127.0.0.1";

        ipAddress[0] = ipAddress[1] = ipAddress[2] = ipAddress[3] = 1;
        macAddress[0] = macAddress[1] = macAddress[2] = macAddress[3] = macAddress[4] = macAddress[5] = 1;
        ClientConfiguration clientCfg = new ClientConfiguration();
        clientCfg.setPartitionAwarenessEnabled(true);
        clientCfg.setAddresses(igniteAddress + ":10800");

        System.out.println("ARGUMENTS");
        System.out.println("jsonFile = " + jsonFile);
        System.out.println("entryCount = " + entryCount);
        System.out.println("qryCount = " + qryCount);
        System.out.println("generateData = " + generateData);
        System.out.println("igniteAddress = " + igniteAddress);
        IgniteClient client = null;

        try {
            client = Ignition.startClient(clientCfg);
        }
        catch (Exception e) {
            System.out.println("Cannot connect to server at : " +
                    igniteAddress + ", Error: " + e.getMessage());
            System.exit(exitCode);
        }

        System.out.println("Testing Scanquery from ThinClient");

        try {

            ClientCacheConfiguration nodeInfoCacheConfig = new ClientCacheConfiguration();
            nodeInfoCacheConfig.setName(tblName);
            nodeInfoCacheConfig.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
            ClientCache<String, NodeInfo> nodeInfoClientCache = client.getOrCreateCache(nodeInfoCacheConfig);

            int ncmSequene = 0;
            int batchCount = 0;
            int qrySequence = 0;
            long insBegin = 0, insEnd = 0;

            String[] queryNames = new String[qryCount];
            if (generateData) {
                // insert data, commit every 10000 entries
                int commitSize = 10000;
                BufferedWriter outFile = new BufferedWriter(new FileWriter(jsonFile));
                ClientTransaction tx = null;
                boolean inTxn = false;
                try {
                    insBegin = System.nanoTime();
                    for (int e = 0; e < entryCount; ++e) {
                        NodeInfo nodeInfo = createNodeInfo(nodePrefix, ncmPrefix, e, ncmNum);
                        if (!inTxn) {
                            tx = client.transactions().txStart();
                            inTxn = true;
                        }
                        nodeInfoClientCache.put(nodeInfo.getId(), nodeInfo);
                        if (batchCount++ >= commitSize) {
                            tx.commit();
                            inTxn = false;
                            batchCount = 0;
                        }

                        if (ncmSequene++ >= ncmMax)
                            ncmSequene = 0;

                        outFile.write(nodeInfo.getId() + "|" + nodeInfo.getName() + "|" +
                                nodeInfo.getNcmId() + "|" + nodeInfo.getLocalIp() + "|" +
                                nodeInfo.getMacAddress() + "\n");
                        if (qrySequence < qryCount)
                            queryNames[qrySequence++] = nodeInfo.getName();
                    }
                    insEnd = System.nanoTime();
                    if (inTxn)
                        tx.commit();
                    outFile.close();
                }
                catch (Exception e) {
                    System.out.println("Failed to insert: " + e.getMessage());
                    System.exit(-1);
                }
            }
            else {
                BufferedReader infile = new BufferedReader(new FileReader(jsonFile));
                while (qrySequence < qryCount) {
                    String line = infile.readLine();
                    String[] fields = line.split("|");
                    queryNames[qrySequence++] = new String(fields[1]);
                }
                infile.close();
            }

            System.out.println("DONE INSERTING");

            System.out.println("Running ScanQuery, Alcor version");

            long[] qryTime = new long[qryCount];
            long[] curTime = new long[qryCount];
            int recCount = 0;
            try {
                long qbegin, qend, cbegin, cend = 0;
                for (int i = 0; i < qryCount; ++i) {
                    String nodeNameIn = queryNames[i];
                    qbegin = System.nanoTime();
                    Map<String, Object[]> queryParams = new HashMap<>();
                    Object[] values = new Object[1];
                    values[0] = nodeNameIn;
                    queryParams.put("name", values);
                    IgniteBiPredicate<String, BinaryObject> pred = MapPredicate.getInstance(queryParams);
                    QueryCursor<Cache.Entry<String, BinaryObject>> cursor = nodeInfoClientCache.withKeepBinary().query(
                            ScanQueryBuilder.newScanQuery(pred));
                    try {
                        qbegin = System.nanoTime();
                        List<Cache.Entry<String, BinaryObject>> result = cursor.getAll();
                        qend = System.nanoTime();
                        qryTime[i] = (qend - qbegin) / 1000;
                        if (result.isEmpty())
                            continue;
                        cbegin = System.nanoTime();
                        BinaryObject obj = result.get(0).getValue();
                        cend = System.nanoTime();
                        curTime[i] = (cend - cbegin) / 1000;
                        if (obj instanceof BinaryObject) {
                            ++recCount;
                            BinaryObject binObj = (BinaryObject) obj;
                            NodeInfo node = (NodeInfo) binObj.deserialize();
                            assert(node.getName().equals(nodeNameIn));
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Scan Query cursor failed " + e.getMessage());
                        break;
                    }
                }
                exitCode = 0;
            }
            catch (Exception e) {
                System.out.println("Scan Query instantiaon failed: " + e.getMessage());
            }

            System.out.println("INSERT_TIME " + (insEnd - insBegin) / 1000 + " us");
            System.out.println("REC_COUNT = " + recCount);

            for (int i = 0; i < qryCount; ++i) {
                System.out.println(qryTime[i] + "\t" + curTime[i]);
            }
        } catch (Exception e) {
            System.out.println("Failed to instantiate PersonCache : " + e.getMessage());
        }
        System.exit(exitCode);
    }

    static NodeInfo createNodeInfo(String nodePrefix, String ncmPrefix, int nodeNumber, int ncmNumber)
    {
        String nodeId = UUID.randomUUID().toString();
        String nodeName = nodePrefix + "_" + String.format("%07d", nodeNumber);
        String nodeIp   = getNextIp();
        String nodeMac   = getNextMAC();
        String ncmId    = ncmPrefix + "_" + String.format("%03d", ncmNumber);

        NodeInfo newNode = new NodeInfo(nodeId, nodeName, nodeIp, nodeMac);
        newNode.setNcmId(ncmId);

        return  newNode;
    }

    static String getNextIp() {
        if (ipAddress[0]++ > 254) {
            ipAddress[0] = 0;
            if (ipAddress[1]++ > 254) {
                ipAddress[1] = 0;
                if (ipAddress[2]++ > 254) {
                    ipAddress[2] = 0;
                    if (ipAddress[3]++ > 254) {
                        System.out.println("IP Address out of bounds");
                        System.exit(-1);
                    }
                }
            }
        }

        String newIp = String.format("%d", ipAddress[3]) + "." +
                            String.format("%d", ipAddress[2]) + "." +
                            String.format("%d", ipAddress[1]) + "." +
                            String.format("%d", ipAddress[0]);

        return newIp;
    }

    static String getNextMAC()
    {
        if (macAddress[0]++ > 255) {
            macAddress[0] = 0;
            if (macAddress[1]++ > 255) {
                macAddress[1] = 0;
                if (macAddress[2]++ > 255) {
                    macAddress[2] = 0;
                    if (macAddress[3]++ > 255) {
                        macAddress[3] = 0;
                        if (macAddress[4]++ > 255) {
                            if (macAddress[5]++ > 255) {
                                System.out.println("MAC out of range");
                                System.exit(-1);
                            }
                        }
                    }
                }
            }
        }
        String newMac = String.format("%02x", macAddress[5]) + ":" +
                String.format("%02x", macAddress[4]) + ":" +
                String.format("%02x", macAddress[3]) + ":" +
                String.format("%02x", macAddress[2]) + ":" +
                String.format("%02x", macAddress[1]) + ":" +
                String.format("%02x", macAddress[0]);

        return newMac;
    }
}