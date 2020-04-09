package com.futurewei.alcor.subnet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)//运行测试的类。 不写这个的情况下，会//直接用JUnit去跑
@SpringBootTest
//@ActiveProfiles //指定测试用的application.properties
@AutoConfigureMockMvc// 测试RestAPI的时候会用到。 封装了TestRestTemplate的相关功能
public class SubnetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testSubnetGet() throws Exception {
        String uri = "/project/3dda2801-d675-4688-a63f-dcda8d327f50/subnets/9192a4d4-ffff-4ece-b3f0-8d36e3d88000";
        this.mockMvc.perform(get(uri).param("projectid", "3dda2801-d675-4688-a63f-dcda8d327f50")
                .param("subnetId", "9192a4d4-ffff-4ece-b3f0-8d36e3d88000"))
                .andDo(print()).andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.subnet").value(null));
    }

}
