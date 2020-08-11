package com.futurewei.alcor.dataplane.controller;

// import com.futurewei.alcor.portmanager.config.UnitTestConfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GSControllerTest {
  @Autowired private MockMvc mockMvc;

  @Test
  public void createPortWithFixedIpsTest() throws Exception {
    String createPortUrl = "/v4/port";
    this.mockMvc
        .perform(post(createPortUrl).contentType(MediaType.APPLICATION_JSON).content(""))
        .andDo(print())
        .andExpect(status().is4xxClientError());
  }
}
