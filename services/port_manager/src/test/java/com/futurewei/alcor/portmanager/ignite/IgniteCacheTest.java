/*
Copyright 2019 The Alcor Authors.

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
package com.futurewei.alcor.portmanager.ignite;

import com.futurewei.alcor.portmanager.entity.Organization;
import com.futurewei.alcor.portmanager.entity.Person;
import com.futurewei.alcor.web.entity.port.AllowAddressPair;
import com.futurewei.alcor.web.entity.port.FixedIp;
import com.futurewei.alcor.web.entity.port.PortEntity;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.query.Query;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.client.ClientCache;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.lang.IgniteBiPredicate;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.multicast.TcpDiscoveryMulticastIpFinder;
import org.junit.Ignore;
import org.junit.Test;
import javax.cache.Cache;
import javax.cache.CacheException;
import java.util.*;

public class IgniteCacheTest  {
    private static final String PORT_CACHE = "PortEntities";
    private static final String PERSON_CACHE = IgniteCacheTest.class.getSimpleName() + "Persons";

    private IgniteClient igniteClient;
    private Ignite ignite;


    private void igniteConnect() {
        /*
        ClientConfiguration cfg = new ClientConfiguration()
                .setAddresses("192.168.131.131:10800");
        igniteClient = Ignition.startClient(cfg);
    */
        TcpDiscoveryMulticastIpFinder ipFinder = new TcpDiscoveryMulticastIpFinder();
        //ipFinder.setMulticastGroup("228.10.10.157");
        ipFinder.setAddresses(Arrays.asList("127.0.0.1:10800"));

        TcpDiscoverySpi spi = new TcpDiscoverySpi();
        spi.setIpFinder(ipFinder);

        org.apache.ignite.configuration.IgniteConfiguration cfg = new org.apache.ignite.configuration.IgniteConfiguration();
        cfg.setClientMode(true);
        cfg.setDiscoverySpi(spi);
        cfg.setPeerClassLoadingEnabled(true);
        //cfg.setLocalHost("192.168.131.1");
        ignite = Ignition.start(cfg);
    }

    public static PortEntity newPortEntity(String portId, int revisionNumber) {
        List<FixedIp> fixedIps = new ArrayList<>();
        fixedIps.add(new FixedIp("subnet1", "10.10.10.1"));

        List<String> securityGroups = new ArrayList<>();
        securityGroups.add("securitygroup1");

        List<AllowAddressPair> allowedAddressPairs = new ArrayList<>();
        allowedAddressPairs.add(new AllowAddressPair("11.11.11.1", "mac1"));

        PortEntity portEntity = new PortEntity();
        portEntity.setId(portId);
        portEntity.setName("port");
        portEntity.setRevisionNumber(revisionNumber);
        portEntity.setVpcId("vpc1");
        portEntity.setProjectId("project1");
        portEntity.setTenantId("tenant1");
        portEntity.setFixedIps(fixedIps);
        portEntity.setMacAddress("mac1");
        portEntity.setBindingHostId("node1");
        portEntity.setSecurityGroups(securityGroups);
        portEntity.setAllowedAddressPairs(allowedAddressPairs);

        return portEntity;
    }

    @Test
    @Ignore
    public void scanQueryTest() {
        igniteConnect();

        PortEntity portEntity1 = newPortEntity("port1", 1);
        PortEntity portEntity2 = newPortEntity("port2", 2);

        IgniteCache<String, PortEntity> cache1 = ignite.getOrCreateCache(PORT_CACHE).withKeepBinary();
        cache1.put(portEntity1.getId(), portEntity1);
        cache1.put(portEntity2.getId(), portEntity2);

        //IgniteCache<String, BinaryObject> cache2 = ignite
        //        .getOrCreateCache(PORT_CACHE).withKeepBinary();

        ScanQuery<String, BinaryObject> scan = new ScanQuery<>(
                new IgniteBiPredicate<String, BinaryObject>() {
                    @Override public boolean apply(String key, BinaryObject port) {
                        return port.<String>field("id").equals("port1");
                    }
                }
        );

        List<Cache.Entry<String, BinaryObject>> all = cache1.query(scan).getAll();
        System.out.println(all);
    }

    @Test
    public void filterTest() throws CacheException {
        igniteConnect();

        ClientCache<String, PortEntity> cache1 = igniteClient.getOrCreateCache(PORT_CACHE);

        PortEntity portEntity1 = newPortEntity("port1", 1);
        PortEntity portEntity2 = newPortEntity("port2", 2);

        cache1.put(portEntity1.getId(), portEntity1);
        cache1.put(portEntity2.getId(), portEntity2);

        /*
        Map<String, PortEntity> items = portRepository.findItems((id, port) -> {
            //Add your filter here
            return "port1".equals(port.getId());
        });

        System.out.println(items);
*/
        Map<String, PortEntity> items;
        //TestQueryFilter filter = new TestQueryFilter();

        IgniteBiPredicate<String, PortEntity> filter = (portId,  portEntity) ->{
            if (portEntity.getFixedIps() != null) {
                for (FixedIp fixedIp : portEntity.getFixedIps()) {
                    if ("11.11.11.100".equals(fixedIp.getIpAddress())) {
                        return true;
                    }
                }
            }

            return false;
        };

        Query<Cache.Entry<String, PortEntity>> qry = new ScanQuery<>(filter);

        List<Cache.Entry<String, PortEntity>> all = cache1.query(qry).getAll();

        System.out.println(all);
    }

    @Test
    public void sqlTest() throws CacheException {
        igniteConnect();

        Organization org1 = new Organization("ApacheIgnite");
        Organization org2 = new Organization("Other");

        Person p1 = new Person(org1, "John", "Doe", 2000, "John Doe has Master Degree.");
        Person p2 = new Person(org1, "Jane", "Doe", 1000, "Jane Doe has Bachelor Degree.");
        Person p3 = new Person(org2, "John", "Smith", 1000, "John Smith has Bachelor Degree.");
        Person p4 = new Person(org2, "Jane", "Smith", 2000, "Jane Smith has Master Degree.");

        CacheConfiguration<Long, Person> personCacheCfg = new CacheConfiguration<>(PERSON_CACHE);

        personCacheCfg.setCacheMode(CacheMode.PARTITIONED); // Default.
        personCacheCfg.setIndexedTypes(Long.class, Person.class);

        IgniteCache<Long, Person> personCache = ignite.getOrCreateCache(personCacheCfg);
        //ClientCache<String, PortEntity> personCache = igniteClient.getOrCreateCache(PORT_CACHE);
        personCache.put(p1.id, p1);
        personCache.put(p2.id, p2);
        personCache.put(p3.id, p3);
        personCache.put(p4.id, p4);

        /*String sql = "select * from Person where salary > ? and salary <= ?";
        SqlFieldsQuery sqlFieldsQuery = new SqlFieldsQuery(sql).setArgs(0, 1000);
        List<List<?>> all = personCache.query(sqlFieldsQuery).getAll();
        System.out.println(all);

         */

        IgniteBiPredicate<Long, Person> filter = (id,  person) ->{
            return person.salary == 2000;
        };

        //QueryCursor<Cache.Entry<Long, Person>> qry = new ScanQuery<>(filter);
        Query<Cache.Entry<Long, Person>> qry = new ScanQuery<>(filter);
        List<Cache.Entry<Long, Person>> allPerson = personCache.query(qry).getAll();

        System.out.println(allPerson);

    }
}
