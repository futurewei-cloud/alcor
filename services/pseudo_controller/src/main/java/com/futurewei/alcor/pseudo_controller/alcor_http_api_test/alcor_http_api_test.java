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

package com.futurewei.alcor.pseudo_controller.alcor_http_api_test;

import com.google.common.util.concurrent.RateLimiter;
import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressSeqRange;
import inet.ipaddr.IPAddressString;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class alcor_http_api_test {
    @Value("${vpm_ip:192.168.0.0}")
    String vpm_ip;
    @Value("${vpm_port:1234}")
    String vpm_port;
    @Value("${snm_ip:192.168.0.0}")
    String snm_ip;
    @Value("${snm_port:1234}")
    String snm_port;
    @Value("${pm_ip:192.168.0.0}")
    String pm_ip;
    @Value("${pm_port:1234}")
    String pm_port;
    /*
        vpc_cidr_slash, the number after the slash in the vpc CIDR, decides how big the VPC is,
        such as 10.0.0.0/16 or 10.0.0.0/8.
    */
    @Value("${vpc_cidr_slash:16}")
    int vpc_cidr_slash;
    /*
        tenant_amount = concurrency when calling APIs.
    */
    @Value("${tenant_amount:1}")
    int tenant_amount;
    /*
        project_amount_per_tenant, each tenant can have multiple projects.
    */
    @Value("${project_amount_per_tenant:1}")
    int project_amount_per_tenant;
    /*
        vpc_amount_per_project, each project can have multiple VPCs.
        each VPC can have the same CIDR
    */
    @Value("${vpc_amount_per_project:1}")
    int vpc_amount_per_project;
    /*
        subnet_amount_per_vpc, each VPC can have multiple subnets.
    */
    @Value("${subnet_amount_per_vpc:2}")
    int subnet_amount_per_vpc;
    /*
        port_amount_per_subnet, each subnet can have multiple ports.
    */
    @Value("${port_amount_per_subnet:3}")
    int port_amount_per_subnet;
    @Value("${test_vpc_api:false}")
    Boolean test_vpc_api;
    @Value("${test_subnet_api:false}")
    Boolean test_subnet_api;
    @Value("${test_port_api:false}")
    Boolean test_port_api;
    @Value("${call_api_rate:1}")
    int call_api_rate;
    public alcor_http_api_test(){}

    public void run_test_against_alcor_apis(){
        System.out.println("Beginning of alcor API test, need to generate: "
                + tenant_amount + " tenants, \n"
                + project_amount_per_tenant + " projects for each tenant, \n"
                + vpc_amount_per_project + " VPCs for each project, \n"
                + subnet_amount_per_vpc + " subnets for each VPC, \n"
                + port_amount_per_subnet + " ports for each subnet.");
        ArrayList<String> tenant_uuids = new ArrayList<>();
        SortedMap<String, ArrayList<String>> tenant_projects = new TreeMap<>();
        SortedMap<String, ArrayList<JSONObject>> project_vpcs = new TreeMap<>();
        SortedMap<String, ArrayList<JSONObject>> vpc_subnets = new TreeMap<>();
        SortedMap<String, ArrayList<JSONObject>> subnet_ports = new TreeMap<>();

        ArrayList<String> vpc_port_ips = null;
        int subnet_port_amount = 1;
        for (int i = 0 ; i < tenant_amount ; i ++){
            String current_tenant_uuid = UUID.randomUUID().toString();
            tenant_uuids.add(current_tenant_uuid);
            ArrayList<String> current_tenant_projects = new ArrayList<>();
            for (int j = 0 ; j < project_amount_per_tenant ; j++){
                String current_project_id = UUID.randomUUID().toString();
                current_tenant_projects.add(current_project_id);
                ArrayList<JSONObject> vpcs_inside_a_project = new ArrayList<>();
                for (int k = 0 ; k < vpc_amount_per_project ; k ++){
                    /*
                        If you set it to /8 you will get a out-of-memory error.
                        /12 gives you more than 2 ^ 20 ports in a VPC, which is
                        1,048,576, without causing the out-of-memory error.
                    */
                    String vpc_cidr = "10.0.0.0/" + vpc_cidr_slash;
                    JSONObject vpc_payload = new JSONObject();
                    JSONObject network = new JSONObject();
                    String current_vpc_id = UUID.randomUUID().toString();
                    network.put("admin_state_up", true);
                    network.put("revision_number", 0);
                    network.put("cidr", vpc_cidr);
                    network.put("default", true);
                    network.put("description", "vpc-"+k);
                    network.put("dns_domain", "test-dns-domain");
                    network.put("id", current_vpc_id);
                    network.put("is_default", true);
                    network.put("mtu", 1400);
                    network.put("name", "vpc-"+k);
                    network.put("port_security_enabled", true);
                    network.put("project_id", current_project_id);
                    vpc_payload.put("network", network);
                    vpcs_inside_a_project.add(vpc_payload);

                    /*
                        1. Generate all port IPs from VPC CIDR range.
                        2. If port_amount_per_subnet > len(port_ips), port_amount_per_subnet = len(port_ips)
                        3. Divide port IPs into groups based on subnet_amount_per_vpc;
                        4. Each group is a subnet, calculate subnet CIDR and form its subnet payload and ports payload
                    */
                    if (null == vpc_port_ips){
                        try {
                            System.out.println("Need to generate port IPs for the first time.");
                            IPAddressString whole_vpc_address = new IPAddressString(vpc_cidr);
                            IPAddressSeqRange whole_vpc_address_range = whole_vpc_address.toSequentialRange();
                            Iterator<IPAddress> range_iterator = (Iterator<IPAddress>) whole_vpc_address_range.stream().iterator();

                            vpc_port_ips = new ArrayList<>();
                            while (range_iterator.hasNext()){
                                vpc_port_ips.add(range_iterator.next().toString());
                            }
                            subnet_port_amount = (vpc_port_ips.size() / subnet_amount_per_vpc);
                            System.out.println("Finished generating port IPs. Each subnet should have " + subnet_port_amount + " ports");
                        } catch (AddressStringException e) {
                            e.printStackTrace();
                        }
                    }
                    /*
                        Create subnet payload based on vpc payload
                    */
                    if (test_subnet_api){
                        ArrayList<JSONObject> current_vpc_subnets = new ArrayList<>();
                        System.out.println("Generating subnets");
                        for (int l = 0 ; l < subnet_amount_per_vpc ; l ++){
                            String subnet_start_ip = vpc_port_ips.get((l * subnet_port_amount) + 0);
                            String subnet_end_ip = vpc_port_ips.get((l * subnet_port_amount) + subnet_port_amount - 1);
                            IPAddressString subnet_start_ip_address_string = new IPAddressString(subnet_start_ip);
                            IPAddressString subnet_end_ip_address_string = new IPAddressString(subnet_end_ip);
                            IPAddress subnet_start_ip_address = subnet_start_ip_address_string.getAddress();
                            IPAddress subnet_end_ip_address = subnet_end_ip_address_string.getAddress();
                            IPAddressSeqRange subnet_range = subnet_start_ip_address.toSequentialRange(subnet_end_ip_address);
                            IPAddress blocks[] = subnet_range.spanWithPrefixBlocks();
                            String subnet_cidr = blocks[0].toString();
                            System.out.println("Subnet cidr = " + subnet_cidr);
                            String current_subnet_id = UUID.randomUUID().toString();
                            JSONObject subnet_payload = new JSONObject();
                            JSONObject subnet = new JSONObject();
                            subnet.put("cidr", subnet_cidr);
                            subnet.put("id", current_subnet_id);
                            subnet.put("ip_version", 4);
                            subnet.put("network_id", current_vpc_id);
                            subnet.put("name", "subnet"+l);
                            subnet_payload.put("subnet", subnet);
                            current_vpc_subnets.add(subnet_payload);
                            if (test_port_api){
                                List<String> subnet_port_ips = vpc_port_ips.subList((l * subnet_port_amount) + 0, (l * subnet_port_amount) + subnet_port_amount);
                                ArrayList<JSONObject> current_subnet_ports = new ArrayList<>();
//                                System.out.println("Generating ports for current subnet, it has " + subnet_port_ips.size() + " ports");
                                for(String port_ip_in_subnet : subnet_port_ips){
                                    JSONObject port_payload = new JSONObject();
                                    JSONObject port = new JSONObject();
                                    port.put("admin_state_up", true);
                                    port.put("description", "test_port");
                                    port.put("device_id", "test_device_id");
                                    port.put("device_owner", "compute:nova");
                                    port.put("fast_path", true);
                                    JSONArray fixed_ips = new JSONArray();
                                    JSONObject subnet_fixed_ip = new JSONObject();
                                    subnet_fixed_ip.put("ip_address", port_ip_in_subnet);
                                    subnet_fixed_ip.put("subnet_id", current_subnet_id);
                                    fixed_ips.add(subnet_fixed_ip);
                                    port.put("fixed_ips", fixed_ips);
                                    port.put("id", UUID.randomUUID().toString());
                                    port.put("mac_learning_enabled", true);
                                    port.put("network_id", current_vpc_id);
                                    port.put("securi_enabled", true);
                                    port.put("project_id", current_project_id);
                                    port.put("revision_number", 0);
                                    port.put("tenant_id", current_tenant_uuid);
                                    port.put("uplink_status_propagation", true);

                                    port_payload.put("port", port);
                                    current_subnet_ports.add(port_payload);
                                }
//                                System.out.println("Finished generating ports for subnet.");
                                subnet_ports.put(current_subnet_id, current_subnet_ports);
                            }

                        }
                        System.out.println("Finished generating subnets for vpc.");
                        vpc_subnets.put(current_vpc_id, current_vpc_subnets);
                    }

                }
                project_vpcs.put(current_project_id, vpcs_inside_a_project);
            }
            tenant_projects.put(current_tenant_uuid, current_tenant_projects);
        }

        System.out.println("Created JSON payloads for " + tenant_uuids.size() + " tenants, \neach tenant has "
                + tenant_projects.get(tenant_projects.firstKey()).size() + " projects, \neach project has "
                + project_vpcs.get(project_vpcs.firstKey()).size() + " vpcs, \neach vpc has "
                + (test_subnet_api ? vpc_subnets.get(vpc_subnets.firstKey()).size() : 0) + " subnets, \neach subnet has "
                + (test_port_api ? subnet_ports.get(subnet_ports.firstKey()).size() : 0) + " ports.");

        System.out.println("Time to call those APIs! Calling APIs at " + call_api_rate + "/second");
        // Maximum 100 API calls per second.
        RateLimiter rateLimiter = RateLimiter.create(call_api_rate);
        // Create a thread pool that has the same amount of threads as the rateLimiter
        ExecutorService concurrent_create_resource_thread_pool = Executors.newFixedThreadPool(call_api_rate);

        if (test_vpc_api){
            System.out.println("Time to test VPC API!");
            ArrayList<JSONObject> create_vpc_jobs = new ArrayList<>();
            for (String project_id : project_vpcs.keySet()) {
                create_vpc_jobs.addAll(project_vpcs.get(project_id));
            }

            int vpc_call_amount = create_vpc_jobs.size();
            CountDownLatch latch = new CountDownLatch(vpc_call_amount);
            int latch_wait_seconds = (vpc_call_amount / call_api_rate) + 1;
            System.out.println("This VPC test will call createVPC API " + vpc_call_amount +
                    " times, at the rate of " + call_api_rate + "/second, it will wait at most "
                    + latch_wait_seconds + " seconds");
            AtomicInteger create_vpc_success_count = new AtomicInteger(0);
            long call_vpc_api_start_time = System.currentTimeMillis();
            for (JSONObject vpc_job : create_vpc_jobs) {
                rateLimiter.acquire();
                String current_project_id = (String)((JSONObject)vpc_job.get("network")).get("project_id");
                String create_vpc_url = "http://" + vpm_ip + ":" + vpm_port + "/project/" + current_project_id + "/vpcs";
                concurrent_create_resource_thread_pool.execute(() -> {
                    JSONObject create_vpc_response = call_post_api_with_json(create_vpc_url, vpc_job);
                    if (null != create_vpc_response && create_vpc_response.containsKey("network")){
//                                System.out.println("Created VPC successfully");
                        create_vpc_success_count.incrementAndGet();
                    }
                    latch.countDown();
                });
            }
            try {
                if (test_subnet_api || test_port_api){
                    /*
                        If we are testing subnet API or port API, we need to wait until the VPC is created.
                    */
                    latch.await(600, TimeUnit.SECONDS);
                }else{
                    /* we actually don't need to wait latch_wait_seconds
                        because if we start the wait after the last call, we should actually wait for the last call.
                        So we will be waiting only 1 second at most.
                    */
                    latch.await(/*latch_wait_seconds*/ 1, TimeUnit.SECONDS);
                }
                long call_vpc_api_end_time = System.currentTimeMillis();
                System.out.println("Total amount of calling createVPC API " + vpc_call_amount +
                        " times, finished "+ ( vpc_call_amount - latch.getCount())
                        + " times, succeeded " + create_vpc_success_count.get() + " times, at the rate of "
                        + call_api_rate + "/second, it took "
                        + (call_vpc_api_end_time - call_vpc_api_start_time) + " milliseconds");
            } catch (InterruptedException e) {
                System.err.println("Waited 60 seconds but can't get VPC response!");
                e.printStackTrace();
            }
        }

        if (test_subnet_api){
            System.out.println("Time to test subnet API!");
            ArrayList<JSONObject> create_subnet_jobs = new ArrayList<>();
            for (String vpc_id : vpc_subnets.keySet()) {
                create_subnet_jobs.addAll(vpc_subnets.get(vpc_id));
            }
            CountDownLatch latch = new CountDownLatch(create_subnet_jobs.size());
            int subnet_call_amount = create_subnet_jobs.size();
            int latch_wait_seconds = (subnet_call_amount / call_api_rate) + 1;
            System.out.println("This subnet test will call createSubnet API " + subnet_call_amount +
                    " times, at the rate of " + call_api_rate + "/second, it will wait at most "
                    + latch_wait_seconds + " seconds");
            AtomicInteger create_subnet_success_count = new AtomicInteger(0);
            long call_subnet_api_start_time = System.currentTimeMillis();
            for (JSONObject subnet_job : create_subnet_jobs) {
                rateLimiter.acquire();
                String current_project_id = (String)((JSONObject)subnet_job.get("subnet")).get("project_id");
                String create_subnet_url = "http://" + snm_ip + ":" + snm_port + "/project/" + current_project_id + "/subnets";
                concurrent_create_resource_thread_pool.execute(() -> {
                    JSONObject create_vpc_response = call_post_api_with_json(create_subnet_url, subnet_job);
                    if (null != create_vpc_response && create_vpc_response.containsKey("subnet")){
//                                System.out.println("Created VPC successfully");
                        create_subnet_success_count.incrementAndGet();
                    }
                    latch.countDown();
                });
            }
            try {
                if (test_port_api){
                    /*
                        If we are testing port API, we need to wait until the VPC is created.
                    */
                    latch.await(600, TimeUnit.SECONDS);
                }else{
                    /* we actually don't need to wait latch_wait_seconds
                        because if we start the wait after the last call, we should actually wait for the last call.
                        So we will be waiting only 1 second at most.
                    */
                    latch.await(/*latch_wait_seconds*/ 1, TimeUnit.SECONDS);
                }
                long call_subnet_api_end_time = System.currentTimeMillis();
                System.out.println("Total amount of calling createSubnet API " + subnet_call_amount +
                        " times, finished "+ ( subnet_call_amount - latch.getCount())
                        + " times, succeeded " + create_subnet_success_count.get() + " times, at the rate of "
                        + call_api_rate + "/second, it took "
                        + (call_subnet_api_end_time - call_subnet_api_start_time) + " milliseconds");
            } catch (InterruptedException e) {
                System.err.println("Waited 60 seconds but can't get subnet response!");
                e.printStackTrace();
            }
        }

        if (test_port_api){
            System.out.println("Time to test port API!");
            ArrayList<JSONObject> create_port_jobs = new ArrayList<>();
            for (String subnet_id : subnet_ports.keySet()) {
                ArrayList<JSONObject> current_subnet_ports = subnet_ports.get(subnet_id);
                //remove the first and last IP, maybe those are reserved.
                current_subnet_ports.remove(current_subnet_ports.size() - 1);
                current_subnet_ports.remove(0);
                create_port_jobs.addAll(current_subnet_ports);
            }
            CountDownLatch latch = new CountDownLatch(create_port_jobs.size());
            int port_call_amount = create_port_jobs.size();
            int latch_wait_seconds = (port_call_amount / call_api_rate) + 1;
            System.out.println("This port test will call createPort API " + port_call_amount +
                    " times, at the rate of " + call_api_rate + "/second, it will wait at most "
                    + latch_wait_seconds + " seconds");
            AtomicInteger create_port_success_count = new AtomicInteger(0);
            long call_port_api_start_time = System.currentTimeMillis();
            for (JSONObject port_job : create_port_jobs) {
                rateLimiter.acquire();
                String current_project_id = (String)((JSONObject)port_job.get("port")).get("project_id");
                String create_port_url = "http://" + pm_ip + ":" + pm_port + "/project/" + current_project_id + "/ports";
                concurrent_create_resource_thread_pool.execute(() -> {
                    JSONObject create_vpc_response = call_post_api_with_json(create_port_url, port_job);
                    if (null != create_vpc_response && create_vpc_response.containsKey("port")){
//                                System.out.println("Created VPC successfully");
                        create_port_success_count.incrementAndGet();
                    }
                    latch.countDown();
                });
            }
            try {
                /* we actually don't need to wait latch_wait_seconds
                    because if we start the wait after the last call, we should actually wait for the last call.
                    So we will be waiting only 1 second at most.
                */
                latch.await(/*latch_wait_seconds*/ 1, TimeUnit.SECONDS);
                long call_port_api_end_time = System.currentTimeMillis();
                System.out.println("Total amount of calling createPort API " + port_call_amount +
                        " times, finished "+ ( port_call_amount - latch.getCount())
                        + " times, succeeded" + create_port_success_count.get() + " times, at the rate of "
                        + call_api_rate + "/second, it took "
                        + (call_port_api_end_time - call_port_api_start_time) + " milliseconds");
            } catch (InterruptedException e) {
                System.err.println("Waited 60 seconds but can't get port response!");
                e.printStackTrace();
            }
        }
        /*
        System.out.println("Try to call API to create a VPC!");
        JSONObject example_vpc_payload = new JSONObject();
        String project_id = "12345";
        String vpc_id = "54321";
        Boolean admin_state_up = true;
        int revision_number = 0;
        String cidr = "10.0.0.0/16";
        Boolean network_default = true;
        String description = "test_description";
        String dns_domain = "test_dns_domain";
        Boolean is_default = true;
        int mtu = 1400;
        String name = "test_vpc";
        Boolean port_security_enabled = true;

        JSONObject network = new JSONObject();
        network.put("admin_state_up", admin_state_up);
        network.put("revision_number", revision_number);
        network.put("cidr", cidr);
        network.put("default", network_default);
        network.put("description", description);
        network.put("dns_domain", dns_domain);
        network.put("id", vpc_id);
        network.put("is_default", is_default);
        network.put("mtu", mtu);
        network.put("name", name);
        network.put("port_security_enabled", port_security_enabled);
        network.put("project_id", project_id);
        example_vpc_payload.put("network", network);

        String create_vpc_url = "http://" + vpm_ip + ":" + vpm_port + "/project/" + project_id + "/vpcs";
        JSONObject create_vpc_response = call_post_api_with_json(create_vpc_url, example_vpc_payload);
        if (null != create_vpc_response){
            System.out.println("Create VPC response: \n" + create_vpc_response);
        }

        String subnet_id = "112233";
        String subnet_cidr = "10.0.1.0/24";
        int subnet_ip_version = 4;
        String network_id = vpc_id;
        String subnet_name = "test_subnet";

        JSONObject example_subnet_payload = new JSONObject();
        JSONObject subnet = new JSONObject();
        subnet.put("cidr", subnet_cidr);
        subnet.put("id", subnet_id);
        subnet.put("ip_version", subnet_ip_version);
        subnet.put("network_id", network_id);
        subnet.put("name", subnet_name);

        example_subnet_payload.put("subnet", subnet);
        String create_subnet_url = "http://" + snm_ip + ":" + snm_port + "/project/" + project_id + "/subnets";
        JSONObject create_subnet_response = call_post_api_with_json(create_subnet_url, example_subnet_payload);
        String subnet_start_ip = null;
        String subnet_end_ip = null;
        JSONObject subnet_content = null;
        JSONArray subnet_allocation_pools = null;
        ArrayList<String> port_ips = new ArrayList<String>();
        if (null != create_subnet_response){
            System.out.println("Create subnet response: \n" + create_subnet_response);
            subnet_content = (JSONObject) create_subnet_response.get("subnet");
            subnet_allocation_pools = (JSONArray) subnet_content.get("allocation_pools");
            JSONObject subnet_allocations_pool_first_element = (JSONObject) subnet_allocation_pools.get(0);
            subnet_start_ip = (String) subnet_allocations_pool_first_element.get("start");
            subnet_end_ip = (String) subnet_allocations_pool_first_element.get("end");
            IPAddressString start = new IPAddressString(subnet_start_ip);
            IPAddressString end = new IPAddressString(subnet_end_ip);
            IPAddress start_addr = start.getAddress();
            IPAddress end_addr = end.getAddress();
            IPAddressSeqRange range = start_addr.toSequentialRange(end_addr);
            Iterator<IPAddress> range_iterator = (Iterator<IPAddress>) range.stream().iterator();
            while (range_iterator.hasNext()){
                IPAddress port_ip_address = range_iterator.next();
                System.out.println("Current port IP: " + port_ip_address.toString());
                port_ips.add(port_ip_address.toString());
            }
            ArrayList<JSONObject> ports_json_objects = new ArrayList<>();
            for (String port_ip_address : port_ips){
                JSONObject example_port_payload = new JSONObject();
                JSONObject port = new JSONObject();
                port.put("admin_state_up", admin_state_up);
                port.put("description", "test_port");
                port.put("device_id", "test_device_id");
                port.put("device_owner", "compute:nova");
                port.put("fast_path", true);
                JSONArray fixed_ips = new JSONArray();
                JSONObject subnet_fixed_ip = new JSONObject();
                subnet_fixed_ip.put("ip_address", port_ip_address);
                subnet_fixed_ip.put("subnet_id", subnet_id);
                fixed_ips.add(subnet_fixed_ip);
                port.put("fixed_ips", fixed_ips);
                port.put("id", UUID.randomUUID().toString());
                port.put("mac_learning_enabled", true);
                port.put("network_id", vpc_id);
                port.put("securi_enabled", true);
                port.put("project_id", project_id);
                port.put("revision_number", 0);
                port.put("tenant_id", project_id);
                port.put("uplink_status_propagation", true);

                example_port_payload.put("port", port);
                ports_json_objects.add(example_port_payload);
            }
            String create_port_url = "http://" + pm_ip + ":"+ pm_port + "/project/" + project_id + "/ports";
            if(ports_json_objects.size() > 0 ){
                JSONObject create_port_response = call_post_api_with_json(create_port_url, ports_json_objects.get(0));
                if (null != create_port_response){
                    System.out.println("Create Port response: \n" + create_port_response);
                }
            }
        }
        */
    }

    private JSONObject call_post_api_with_json(String url, JSONObject parameter){
//        System.out.println("Calling URL: " + url);
        JSONObject response_json = null;
        HttpClient c = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        post.setHeader("Content-Type", "application/json");
        String result = "";
        try {
            StringEntity s = new StringEntity(parameter.toString(), "utf-8");
            s.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
                    "application/json"));
            post.setEntity(s);
            HttpResponse httpResponse = c.execute(post);

            // Get the input stream
            HttpEntity response_entity = httpResponse.getEntity();

            String json_string = EntityUtils.toString(response_entity);

            response_json = (JSONObject) new JSONParser().parse(json_string);

        }catch (Exception e){
            e.printStackTrace();
        }
        return response_json;
    }

}
