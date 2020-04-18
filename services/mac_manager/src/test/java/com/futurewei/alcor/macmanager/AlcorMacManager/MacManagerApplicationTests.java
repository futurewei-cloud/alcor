package com.futurewei.alcor.macmanager.AlcorMacManager;

<<<<<<< HEAD
=======
import com.fasterxml.jackson.databind.ObjectMapper;
import com.futurewei.alcor.macmanager.entity.MacState;
>>>>>>> 0817ce8198b91bfaca7c43abedc8995c5a3746a6
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
<<<<<<< HEAD
=======
import org.springframework.http.MediaType;
>>>>>>> 0817ce8198b91bfaca7c43abedc8995c5a3746a6
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
<<<<<<< HEAD
=======
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
>>>>>>> 0817ce8198b91bfaca7c43abedc8995c5a3746a6
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
class MacManagerApplicationTests {

<<<<<<< HEAD
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
=======
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

//	@Test
//	public void test_createMacState() throws Exception {
//		MacState macState = new MacState("", "project1", "vpc1", "port1");
//		ObjectMapper objectMapper = new ObjectMapper();
//		String json = objectMapper.writeValueAsString(macState);
//		this.mvc.perform(post("/macs")
//				.contentType(MediaType.APPLICATION_JSON)
//				.content(json))
//				.andExpect(status().isOk())
//				.andReturn();
//	}
//
//	@Test
//	public void test_getMacStateByMacAddress() throws Exception {
//		this.mvc.perform(get("/macs/00-00-00-00-00-00"))
//				.andDo(print())
//				.andExpect(status().isOk());
//	}
//
//	@Test
//	public void test_deleteMacStateByMacAddress() throws Exception {
//		this.mvc.perform(get("/macs/00-00-00-00-00-00"))
//				.andDo(print())
//				.andExpect(status().isOk());
//	}
>>>>>>> 0817ce8198b91bfaca7c43abedc8995c5a3746a6
}
