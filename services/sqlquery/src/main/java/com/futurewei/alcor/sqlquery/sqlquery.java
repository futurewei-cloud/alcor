package com.futurewei.alcor.sqlquery;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheKeyConfiguration;
import org.apache.ignite.cache.QueryEntity;
import org.apache.ignite.cache.QueryIndex;
import org.apache.ignite.cache.query.QueryCursor;
import org.apache.ignite.cache.query.SqlFieldsQuery;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.ClientCacheConfiguration;
import org.apache.ignite.client.ClientTransaction;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.apache.ignite.transactions.Transaction;
import org.json.simple.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Node;
import com.futurewei.alcor.web.entity.node.NodeInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class sqlquery {
    static int[] ipAddress = new int[4];
    static int[] macAddress = new int[6];
    static String igniteAddress;
    static String nodePrefix = "node";
    static String ncmPrefix  = "ncm";
    static int    ncmNum = 0;
    static int ncmMax = 1000;
    static boolean generateData = false;

    public sqlquery(String uuid) {
    }

    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("need input: json_output_filename number_of_entries number_of_queries [-g(enerate data)]");
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
            System.out.println("Cannot connect to Local server: " + e.getMessage());
            System.exit(exitCode);
        }

        System.out.println("Testing QueryEntity, Indexing, SQL Access to ClientCache from ThinClient");

        try {

            ClientCacheConfiguration nodeInfoCacheConfig = new ClientCacheConfiguration();
            nodeInfoCacheConfig.setName(tblName);
            nodeInfoCacheConfig.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
            CacheKeyConfiguration keyConf = new CacheKeyConfiguration();

            QueryEntity qryEnt = new QueryEntity();
            qryEnt.setValueType(NodeInfo.class.getName());
            LinkedHashMap<String, String> qryFields = new LinkedHashMap<>();

            qryFields.put("id", String.class.getName());
            qryFields.put("name", String.class.getName());

            qryEnt.setFields(qryFields);

            qryEnt.setIndexes(Arrays.asList(
                    new QueryIndex("id"),
                    new QueryIndex("name")));
            nodeInfoCacheConfig.setQueryEntities(qryEnt).setSqlSchema(schName);

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

            SqlFieldsQuery sql = new SqlFieldsQuery("select _key, _val from " + schName +
                    "." + tblName + " where name = ?");

            int i;
            long[] qryTime = new long[qryCount];
            long[] curTime = new long[qryCount];
            int recCount = 0;
            try {

                long qbegin, qend, cbeign, cend = 0;
                for (i = 0; i < qryCount; ++i) {
                    String nodeNameIn = queryNames[i];
                    sql.setArgs(nodeNameIn);
                    qbegin = System.nanoTime();
                    QueryCursor<List<?>> cursor = nodeInfoClientCache.query(sql);
                    qend = System.nanoTime();
                    qryTime[i] = (qend - qbegin) / 1000;
                    String nodeId = null, nodeName = null;
                    cbeign = qend;
                    try {
                        for (List<?> row : cursor) {
                            ++recCount;
                            cbeign = System.nanoTime();
                            nodeId = row.get(0).toString();
                            NodeInfo node = (NodeInfo)row.get(1);
                            nodeName = node.getName();
                            cend = System.nanoTime();
                            assert(nodeNameIn.equals(nodeName));
                            curTime[i] = (cend - cbeign) / 1000;
                        }
                    }
                    catch (Exception e) {
                        System.out.println("Cursor failed: " + e.getMessage());
                        continue;
                    }

                    System.out.println("SQL: " + sql + " args: " + sql.getArgs().toString());
                    exitCode = 0;
                }

                System.out.println("INSERT_TIME " + (insEnd - insBegin) / 1000 + " us");
                System.out.println("REC_COUNT = " + recCount);

                for (int j = 0; j < i; ++j) {
                    System.out.println(qryTime[j] + "\t" + curTime[j]);
                }
            } catch (Exception e) {
                System.out.println("SQL Query failed: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Failed to instantiate PersonCache : " + e.getMessage());
        } finally {
            try {
                if (client != null)
                    client.close();
            } catch (Exception e) {
            }
            System.exit(exitCode);
        }
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
