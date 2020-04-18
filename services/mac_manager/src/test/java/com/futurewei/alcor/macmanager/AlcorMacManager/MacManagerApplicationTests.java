package com.futurewei.alcor.macmanager.AlcorMacManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
class MacManagerApplicationTests {

    @Autowired
    public MockMvc mvc;

    @Test
    void contextLoads() {
    }

    @Test
    public void test_index() throws Exception {
        this.mvc.perform(get("/start.html"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("MacManager")));
    }
}
