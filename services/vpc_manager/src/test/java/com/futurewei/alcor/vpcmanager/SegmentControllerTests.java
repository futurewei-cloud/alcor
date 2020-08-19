package com.futurewei.alcor.vpcmanager;

import com.futurewei.alcor.vpcmanager.config.ConstantsConfig;
import com.futurewei.alcor.vpcmanager.config.UnitTestConfig;
import com.futurewei.alcor.vpcmanager.dao.VlanRangeRepository;
import com.futurewei.alcor.vpcmanager.dao.VlanRepository;
import com.futurewei.alcor.vpcmanager.exception.NetworkKeyNotEnoughException;
import com.futurewei.alcor.vpcmanager.service.SegmentDatabaseService;
import com.futurewei.alcor.vpcmanager.service.VpcDatabaseService;
import com.futurewei.alcor.web.entity.vpc.SegmentEntity;
import com.futurewei.alcor.web.entity.route.RouteWebJson;

import com.futurewei.alcor.web.entity.vpc.VpcEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"httpbin=http://localhost:${wiremock.server.port}"})
@AutoConfigureMockMvc
public class SegmentControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SegmentDatabaseService segmentDatabaseService;

    @MockBean
    private VpcDatabaseService vpcDatabaseService;

    @MockBean
    private VlanRangeRepository vlanRangeRepository;

    @MockBean
    private VlanRepository vlanRepository;

    private String getByIdUri = "/project/" + UnitTestConfig.projectId + "/segments/" + UnitTestConfig.segmentId;
    private String createUri = "/project/" + UnitTestConfig.projectId + "/segments";
    private String updateUri = "/project/" + UnitTestConfig.projectId + "/segments/" + UnitTestConfig.segmentId;
    private String deleteUri = "/project/" + UnitTestConfig.projectId + "/segments/" + UnitTestConfig.segmentId;
    private String getByProjectIdUri = "/project/" + UnitTestConfig.projectId + "/segments";

    @Test
    public void vpcGetById_canFindSegment_pass () throws Exception {
        Mockito.when(segmentDatabaseService.getBySegmentId(UnitTestConfig.segmentId))
                .thenReturn(new SegmentEntity(UnitTestConfig.projectId,
                        UnitTestConfig.segmentId, UnitTestConfig.name,
                        UnitTestConfig.cidr, UnitTestConfig.vpcId));
        this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.segment.id").value(UnitTestConfig.segmentId));
    }

    @Test
    public void vpcGetById_canNotFindSegment_notPass () throws Exception {
        Mockito.when(segmentDatabaseService.getBySegmentId(UnitTestConfig.segmentId)).thenReturn(null);
        String response = this.mockMvc.perform(get(getByIdUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println("-----json returned = " + response);
        assertEquals("{\"segment\":null}", response);
    }

    @Test
    public void createSegment_create_pass () throws Exception {
        RouteWebJson routeWebJson = new RouteWebJson();
        Mockito.when(segmentDatabaseService.getBySegmentId(UnitTestConfig.segmentId))
                .thenReturn(new SegmentEntity(UnitTestConfig.projectId,
                        UnitTestConfig.segmentId, UnitTestConfig.name,
                        UnitTestConfig.cidr, UnitTestConfig.vpcId));
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(new VpcEntity(UnitTestConfig.projectId,
                        UnitTestConfig.segmentId, UnitTestConfig.name,
                        UnitTestConfig.cidr, null));
//        Mockito.when(vpcDatabaseService.getByVpcId(null))
//                .thenReturn(new VpcEntity(UnitTestConfig.projectId,
//                        UnitTestConfig.segmentId, UnitTestConfig.name,
//                        UnitTestConfig.cidr, null));
        this.mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.segmentResource))
                .andDo(print())
                .andExpect(status().is(201))
                .andExpect(MockMvcResultMatchers.jsonPath("$.segment.id").value(UnitTestConfig.segmentId));
    }

    @Test
    public void createSegment_create_keyNotEnough () throws Exception {
        RouteWebJson routeWebJson = new RouteWebJson();
        Mockito.when(segmentDatabaseService.getBySegmentId(UnitTestConfig.segmentId))
                .thenReturn(new SegmentEntity(UnitTestConfig.projectId,
                        UnitTestConfig.segmentId, UnitTestConfig.name,
                        UnitTestConfig.cidr, UnitTestConfig.vpcId));
        Mockito.when(vpcDatabaseService.getByVpcId(UnitTestConfig.vpcId))
                .thenReturn(new VpcEntity(UnitTestConfig.projectId,
                        UnitTestConfig.segmentId, UnitTestConfig.name,
                        UnitTestConfig.cidr, null));
        Mockito.when(vlanRangeRepository.allocateVlanKey(anyString()))
                .thenReturn(ConstantsConfig.keyNotEnoughReturnValue);
        try {
            this.mockMvc.perform(post(createUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.segmentResource))
                    .andDo(print())
                    .andExpect(status().is(412));
        } catch (NetworkKeyNotEnoughException ex) {
            assertEquals("Key is not enough to be allocated111", ex.getMessage());
        }

    }

    @Test
    public void updateSegmentByVpcId_noUpdate_pass () throws Exception {
        Mockito.when(segmentDatabaseService.getBySegmentId(UnitTestConfig.segmentId))
                .thenReturn(new SegmentEntity(UnitTestConfig.projectId,
                        UnitTestConfig.segmentId, UnitTestConfig.name,
                        UnitTestConfig.cidr, UnitTestConfig.vpcId));
        this.mockMvc.perform(put(updateUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.segmentResource))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.segment.id").value(UnitTestConfig.segmentId));
    }

    @Test
    public void updateSegmentBySegmentId_update_pass () throws Exception {
        Mockito.when(segmentDatabaseService.getBySegmentId(UnitTestConfig.segmentId))
                .thenReturn(new SegmentEntity(UnitTestConfig.projectId,
                        UnitTestConfig.segmentId, UnitTestConfig.name,
                        UnitTestConfig.cidr, UnitTestConfig.vpcId))
                .thenReturn(new SegmentEntity(UnitTestConfig.projectId,
                        UnitTestConfig.segmentId, UnitTestConfig.updateName,
                        UnitTestConfig.cidr, UnitTestConfig.vpcId));
        this.mockMvc.perform(put(updateUri).contentType(MediaType.APPLICATION_JSON).content(UnitTestConfig.segmentResource))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.segment.name").value(UnitTestConfig.updateName));
    }

    @Test
    public void deleteSegmentBySegmentId_deleteWhenIdExist_pass () throws Exception {
        Mockito.when(segmentDatabaseService.getBySegmentId(UnitTestConfig.segmentId))
                .thenReturn(new SegmentEntity(UnitTestConfig.projectId,
                        UnitTestConfig.segmentId, UnitTestConfig.name,
                        UnitTestConfig.cidr, UnitTestConfig.vpcId){{setSegmentationId(1);}});
        this.mockMvc.perform(delete(deleteUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(UnitTestConfig.segmentId));
    }

    @Test
    public void deleteSegmentBySegmentId_deleteWhenIdNotExist_pass () throws Exception {
        Mockito.when(segmentDatabaseService.getBySegmentId(UnitTestConfig.segmentId))
                .thenReturn(null);
        String response = this.mockMvc.perform(delete(deleteUri))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        System.out.println("-----json returned = " + response);
        assertEquals("{\"id\":null}", response);
    }

    @Test
    public void getSegmentByProjectId_getMap_pass () throws Exception {
        Map<String, SegmentEntity> segments = new HashMap<>();
        SegmentEntity segmentEntity =new SegmentEntity(UnitTestConfig.projectId,
                UnitTestConfig.segmentId, UnitTestConfig.name,
                UnitTestConfig.cidr, UnitTestConfig.vpcId);
        segments.put("SegmentWebResponseObject", segmentEntity);
        Mockito.when(segmentDatabaseService.getAllSegments()).thenReturn(segments);
        this.mockMvc.perform(get(getByProjectIdUri)).andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void getSegmentByProjectId_getEmptyMap_pass () throws Exception {
        Map<String, SegmentEntity> segments = new HashMap<>();
        Mockito.when(segmentDatabaseService.getAllSegments()).thenReturn(segments);
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
