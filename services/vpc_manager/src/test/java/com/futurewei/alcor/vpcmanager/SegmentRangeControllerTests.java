package com.futurewei.alcor.vpcmanager;

import com.futurewei.alcor.vpcmanager.config.UnitTestConfig;
import com.futurewei.alcor.vpcmanager.service.SegmentRangeDatabaseService;
import com.futurewei.alcor.web.entity.NetworkSegmentRangeWebResponseObject;
import com.futurewei.alcor.web.entity.RouteWebJson;
import com.futurewei.alcor.web.entity.SegmentWebResponseObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"httpbin=http://localhost:${wiremock.server.port}"})
@AutoConfigureMockMvc
public class SegmentRangeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SegmentRangeDatabaseService segmentRangeDatabaseService;

    private String getByIdUri = "/project/" + UnitTestConfig.projectId + "/network_segment_ranges/" + UnitTestConfig.segmentRangeId;
    private String createUri = "/project/" + UnitTestConfig.projectId + "/network_segment_ranges";
    private String updateUri = "/project/" + UnitTestConfig.projectId + "/network_segment_ranges/" + UnitTestConfig.segmentRangeId;
    private String deleteUri = "/project/" + UnitTestConfig.projectId + "/network_segment_ranges/" + UnitTestConfig.segmentRangeId;
    private String getByProjectIdUri = "/project/" + UnitTestConfig.projectId + "/network_segment_ranges";

    @Test
    public void vpcGetById_canFindSegmentRange_pass () throws Exception {
        Mockito.when(segmentRangeDatabaseService.getBySegmentRangeId(UnitTestConfig.segmentRangeId))
                .thenReturn(new NetworkSegmentRangeWebResponseObject(UnitTestConfig.projectId,
                        UnitTestConfig.segmentRangeId, UnitTestConfig.name,
                        UnitTestConfig.cidr, UnitTestConfig.networkType));
        this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network_segment_range.id").value(UnitTestConfig.segmentRangeId));
    }

    @Test
    public void vpcGetById_canNotFindSegment_notPass () throws Exception {
        Mockito.when(segmentRangeDatabaseService.getBySegmentRangeId(UnitTestConfig.segmentRangeId)).thenReturn(null);
        String response = this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println("-----json returned = " + response);
        assertEquals("{\"network_segment_range\":null}", response);
    }

    @Test
    public void createSegment_create_pass () throws Exception {
        RouteWebJson routeWebJson = new RouteWebJson();
        Mockito.when(segmentRangeDatabaseService.getBySegmentRangeId(UnitTestConfig.segmentRangeId))
                .thenReturn(new NetworkSegmentRangeWebResponseObject(UnitTestConfig.projectId,
                        UnitTestConfig.segmentRangeId, UnitTestConfig.name,
                        UnitTestConfig.cidr, UnitTestConfig.networkType));
        this.mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.segmentRangeResource))
                .andDo(print())
                .andExpect(status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.network_segment_range.id").value(UnitTestConfig.segmentRangeId));
    }

    @Test
    public void updateSegmentRangeBySegmentRangeId_noUpdate_pass () throws Exception {
        Mockito.when(segmentRangeDatabaseService.getBySegmentRangeId(UnitTestConfig.segmentRangeId))
                .thenReturn(new NetworkSegmentRangeWebResponseObject(UnitTestConfig.projectId,
                        UnitTestConfig.segmentRangeId, UnitTestConfig.name,
                        UnitTestConfig.cidr, UnitTestConfig.networkType));
        this.mockMvc.perform(put(updateUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.segmentRangeResource))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network_segment_range.id").value(UnitTestConfig.segmentRangeId));
    }

    @Test
    public void updateSegmentBySegmentRangeId_update_pass () throws Exception {
        Mockito.when(segmentRangeDatabaseService.getBySegmentRangeId(UnitTestConfig.segmentRangeId))
                .thenReturn(new NetworkSegmentRangeWebResponseObject(UnitTestConfig.projectId,
                        UnitTestConfig.segmentRangeId, UnitTestConfig.name,
                        UnitTestConfig.cidr, UnitTestConfig.networkType))
                .thenReturn(new NetworkSegmentRangeWebResponseObject(UnitTestConfig.projectId,
                        UnitTestConfig.segmentRangeId, UnitTestConfig.updateName,
                        UnitTestConfig.cidr, UnitTestConfig.networkType));
        this.mockMvc.perform(put(updateUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.segmentRangeResource))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.network_segment_range.name").value(UnitTestConfig.updateName));
    }

    @Test
    public void deleteSegmentBySegmentId_deleteWhenIdExist_pass () throws Exception {
        Mockito.when(segmentRangeDatabaseService.getBySegmentRangeId(UnitTestConfig.segmentRangeId))
                .thenReturn(new NetworkSegmentRangeWebResponseObject(UnitTestConfig.projectId,
                        UnitTestConfig.segmentRangeId, UnitTestConfig.updateName,
                        UnitTestConfig.cidr, UnitTestConfig.networkType));
        this.mockMvc.perform(delete(deleteUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(UnitTestConfig.segmentRangeId));
    }

    @Test
    public void deleteSegmentBySegmentId_deleteWhenIdNotExist_pass () throws Exception {
        Mockito.when(segmentRangeDatabaseService.getBySegmentRangeId(UnitTestConfig.segmentRangeId))
                .thenReturn(null);
        String response = this.mockMvc.perform(delete(deleteUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println("-----json returned = " + response);
        assertEquals("{\"id\":null}", response);
    }

    @Test
    public void getSegmentRangeByProjectId_getMap_pass () throws Exception {
        Map<String, NetworkSegmentRangeWebResponseObject> segments = new HashMap<>();
        NetworkSegmentRangeWebResponseObject segmentRangeWebResponseObject =new NetworkSegmentRangeWebResponseObject(UnitTestConfig.projectId,
                UnitTestConfig.segmentRangeId, UnitTestConfig.name,
                UnitTestConfig.cidr, UnitTestConfig.networkType);
        segments.put("NetworkSegmentRangeWebResponseObject", segmentRangeWebResponseObject);
        Mockito.when(segmentRangeDatabaseService.getAllSegmentRanges()).thenReturn(segments);
        this.mockMvc.perform(get(getByProjectIdUri)).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getSegmentRangeByProjectId_getEmptyMap_pass () throws Exception {
        Map<String, NetworkSegmentRangeWebResponseObject> segmentRanges = new HashMap<>();
        Mockito.when(segmentRangeDatabaseService.getAllSegmentRanges()).thenReturn(segmentRanges);
        this.mockMvc.perform(get(getByProjectIdUri)).andDo(print())
                .andExpect(status().isOk());
    }

    @Before
    public void init() throws IOException {
        System.out.println("Start Test-----------------");
    }

    @After
    public void after() {
        System.out.println("End Test-----------------");
    }

}
