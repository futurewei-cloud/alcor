package com.futurewei.alcor.subnet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.futurewei.alcor.subnet.dao.SubnetRedisRepository;
import com.futurewei.alcor.subnet.entity.SubnetState;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ai.grakn.redismock.RedisServer;

import java.io.IOException;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"httpbin=http://localhost:${wiremock.server.port}"})
@AutoConfigureMockMvc
public class SubnetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubnetRedisRepository subnetRedisRepository;

    private static RedisServer server = null;

    private SubnetState subnetState;

    private String getUri = "/project/3dda2801-d675-4688-a63f-dcda8d327f50/subnets/9192a4d4-ffff-4ece-b3f0-8d36e3d88000";
    private String deleteUri = "project/dda2801-d675-4688-a63f-dcda8d327f50/vpcs/9192a4d4-ffff-4ece-b3f0-8d36e3d88038/subnets/9192a4d4-ffff-4ece-b3f0-8d36e3d88000";

    @Test
    public void test () throws Exception {
        Assert.assertEquals("test", "test");
    }

    @Test
    public void testSubnetGET () throws Exception {
        this.mockMvc.perform(get(getUri)).andDo(print())
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.subnet").value(null));
    }

    @Test
    public void testSubnetDELETE () throws Exception {
        this.mockMvc.perform(get(deleteUri)).andDo(print())
                .andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.id").value(null));
    }

    @Before
    public void init() throws IOException {
        server = RedisServer.newRedisServer(6379);  // bind to a random port
        server.start();
        String h = server.getHost();//0.0.0.0 bind host
        subnetState = new SubnetState("3dda2801-d675-4688-a63f-dcda8d327f50","9192a4d4-ffff-4ece-b3f0-8d36e3d88038", "9192a4d4-ffff-4ece-b3f0-8d36e3d88000", "test_subnet","10.0.0.0/16");
        this.subnetRedisRepository.addItem(subnetState);
        System.out.println("Start Test-----------------");
    }

    @After
    public void after() {
        this.subnetRedisRepository.deleteItem("9192a4d4-ffff-4ece-b3f0-8d36e3d88000");
        server.stop();
        server = null;
        System.out.println("End Test-----------------");
    }
}
