package com.futurewei.alcor.subnet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@AutoConfigureMockMvc
@SpringBootTest
class SubnetApplicationTests {

    @Test
    void contextLoads() {
    }

}
