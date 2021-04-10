/*
MIT License
Copyright(c) 2020 Futurewei Cloud
    Permission is hereby granted,
    free of charge, to any person obtaining a copy of this software and associated documentation files(the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and / or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
    The above copyright notice and this permission notice shall be included in all copies
    or
    substantial portions of the Software.
    THE SOFTWARE IS PROVIDED "AS IS",
    WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
    DAMAGES OR OTHER
    LIABILITY,
    WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
*/

package com.futurewei.alcor.quota.controller;

import com.futurewei.alcor.common.db.IDistributedLock;
import com.futurewei.alcor.quota.config.DefaultQuota;
import com.futurewei.alcor.quota.dao.ApplyRepository;
import com.futurewei.alcor.quota.dao.QuotaRepository;
import com.futurewei.alcor.quota.dao.QuotaUsageRepository;
import com.futurewei.alcor.quota.service.impl.QuotaServiceImpl;
import com.futurewei.alcor.quota.utils.QuotaUtils;
import com.futurewei.alcor.web.entity.quota.ApplyInfo;
import com.futurewei.alcor.web.entity.quota.QuotaEntity;
import com.futurewei.alcor.web.entity.quota.QuotaUsageEntity;
import com.futurewei.alcor.web.entity.quota.ResourceDelta;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static com.futurewei.alcor.quota.config.QuotaTestConstants.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ComponentScan(value = "com.futurewei.alcor.common.test.config")
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class QuotaControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DefaultQuota defaultQuota;

    @MockBean
    private ApplyRepository applyRepository;

    @MockBean
    private QuotaRepository quotaRepository;

    @MockBean
    private QuotaUsageRepository quotaUsageRepository;

    @Autowired
    private QuotaServiceImpl quotaService;

    @Test
    public void getAllQuotasTest() throws Exception {
        Map<String, QuotaEntity> map = buildProjectQuota(PROJECT_ID);
        buildProjectQuota(PROJECT_ID2, map);
        buildProjectQuota(PROJECT_ID3, map);
        when(quotaRepository.findAllItems()).thenReturn(map);
        mockMvc.perform(get(String.format(COLLECT_URL_TEMP, PROJECT_ID)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quotas[0].network").value(NETWORK_LIMIT));
    }

    @Test
    public void getAllQuotas_EmptyTest() throws Exception {
        when(quotaRepository.findAllItems()).thenReturn(Collections.emptyMap());
        mockMvc.perform(get(String.format(COLLECT_URL_TEMP, PROJECT_ID)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quotas").isEmpty());
    }

    @Test
    public void getQuotaTest() throws Exception {
        when(quotaRepository.findProjectQuotas(PROJECT_ID)).thenReturn(buildProjectQuota(PROJECT_ID));
        mockMvc.perform(get(String.format(RESOURCE_URL_TEMP, PROJECT_ID, PROJECT_ID)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quota.network").value(NETWORK_LIMIT));
    }

    @Test
    public void getQuota_EmptyTest() throws Exception {
        when(quotaRepository.findProjectQuotas(EMPTY_PROJECT_ID)).thenReturn(Collections.emptyMap());
        mockMvc.perform(get(String.format(RESOURCE_URL_TEMP, EMPTY_PROJECT_ID, EMPTY_PROJECT_ID)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quota.network").value(defaultQuota.getDefaults().get(NETWORK)));
    }

    @Test
    public void updateQuotaTest() throws Exception {
        mockLock();
        when(quotaRepository.findItem(anyString())).thenReturn(null);
        when(quotaUsageRepository.findItem(anyString())).thenReturn(null);
        doNothing().when(quotaRepository).addItem(any(QuotaEntity.class));
        doNothing().when(quotaUsageRepository).addItem(any(QuotaUsageEntity.class));
        mockMvc.perform(put(String.format(RESOURCE_URL_TEMP, PROJECT_ID, PROJECT_ID)).content(updateQuota)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quota.network").value(17));
    }

    @Test
    public void updateQuota_ExistTest() throws Exception {
        mockLock();
        when(quotaRepository.findItem(QuotaUtils.getCombineId(PROJECT_ID, NETWORK)))
                .thenReturn(new QuotaEntity(PROJECT_ID, NETWORK, NETWORK_LIMIT));
        when(quotaUsageRepository.findItem(QuotaUtils.getCombineId(PROJECT_ID, NETWORK)))
                .thenReturn(new QuotaUsageEntity(PROJECT_ID, NETWORK, 0, NETWORK_LIMIT, 0));
        doNothing().when(quotaRepository).addItem(any(QuotaEntity.class));
        doNothing().when(quotaUsageRepository).addItem(any(QuotaUsageEntity.class));
        mockMvc.perform(put(String.format(RESOURCE_URL_TEMP, PROJECT_ID, PROJECT_ID)).content(updateQuota)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quota.network").value(17));
    }

    @Test
    public void deleteQuotaTest() throws Exception {
        mockLock();
        when(quotaRepository.findItem(QuotaUtils.getCombineId(PROJECT_ID, NETWORK)))
                .thenReturn(new QuotaEntity(PROJECT_ID, NETWORK, NETWORK_LIMIT));
        when(quotaUsageRepository.findItem(QuotaUtils.getCombineId(PROJECT_ID, NETWORK)))
                .thenReturn(new QuotaUsageEntity(PROJECT_ID, NETWORK, 0, NETWORK_LIMIT, 0));
        Map<String, QuotaEntity> resourceQuota = new HashMap<>();
        resourceQuota.put(NETWORK, new QuotaEntity(PROJECT_ID, NETWORK, NETWORK_LIMIT));
        resourceQuota.put(SUBNET, new QuotaEntity(PROJECT_ID, SUBNET, SUBNET_LIMIT));
        resourceQuota.put(PORT, new QuotaEntity(PROJECT_ID, PORT, PORT_LIMIT));
        when(quotaRepository.findProjectQuotas(PROJECT_ID)).thenReturn(resourceQuota);
        doNothing().when(quotaUsageRepository).addItem(any(QuotaUsageEntity.class));
        doNothing().when(quotaRepository).deleteItem(anyString());
        mockMvc.perform(delete(String.format(RESOURCE_URL_TEMP, PROJECT_ID, PROJECT_ID)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void deleteQuota_EmptyTest() throws Exception {
        when(quotaRepository.findProjectQuotas(PROJECT_ID)).thenReturn(null);
        mockMvc.perform(delete(String.format(RESOURCE_URL_TEMP, PROJECT_ID, PROJECT_ID)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void getDefaultQuotaForProjectTest() throws Exception {
        mockMvc.perform(get(String.format(GET_DEFAULT_URL_TEMP, PROJECT_ID, PROJECT_ID)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quota.network").value(defaultQuota.getDefaults().get("network")));
    }

    @Test
    public void getProjectQuotaDetailTest() throws Exception {
        Map<String, QuotaUsageEntity> quotaUSageMap = new HashMap<>();
        quotaUSageMap.put(QuotaUtils.getCombineId(PROJECT_ID, NETWORK),
                new QuotaUsageEntity(PROJECT_ID, NETWORK, 1, NETWORK_LIMIT, 0));
        quotaUSageMap.put(QuotaUtils.getCombineId(PROJECT_ID, SUBNET),
                new QuotaUsageEntity(PROJECT_ID, SUBNET, 10, SUBNET_LIMIT, 0));
        quotaUSageMap.put(QuotaUtils.getCombineId(PROJECT_ID, PORT),
                new QuotaUsageEntity(PROJECT_ID, PORT, 0, PORT_LIMIT, 0));
        when(quotaUsageRepository.findProjectQuotas(PROJECT_ID)).thenReturn(quotaUSageMap);
        mockMvc.perform(get(String.format(GET_DETAIL_URL_TEMP, PROJECT_ID, PROJECT_ID)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quota.network.used").value(1))
                .andExpect(jsonPath("$.quota.network.limit").value(NETWORK_LIMIT))
                .andExpect(jsonPath("$.quota.network.reserved").value(0))
                .andExpect(jsonPath("$.quota.subnet.used").value(10))
                .andExpect(jsonPath("$.quota.subnet.limit").value(SUBNET_LIMIT))
                .andExpect(jsonPath("$.quota.subnet.reserved").value(0))
                .andExpect(jsonPath("$.quota.port.used").value(0))
                .andExpect(jsonPath("$.quota.port.limit").value(PORT_LIMIT))
                .andExpect(jsonPath("$.quota.port.reserved").value(0));
    }

    @Test
    public void getProjectQuotaDetail_EmptyTest() throws Exception {
        when(quotaUsageRepository.findProjectQuotas(EMPTY_PROJECT_ID)).thenReturn(null);
        mockMvc.perform(get(String.format(GET_DETAIL_URL_TEMP, EMPTY_PROJECT_ID, EMPTY_PROJECT_ID)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void allocateQuotaTest() throws Exception {
        mockLock();
        when(quotaUsageRepository.findItem(QuotaUtils.getCombineId(PROJECT_ID, NETWORK))).thenReturn(
                new QuotaUsageEntity(PROJECT_ID, NETWORK, 0, NETWORK_LIMIT, 0)
        );
        when(quotaUsageRepository.findItem(QuotaUtils.getCombineId(PROJECT_ID, SUBNET))).thenReturn(
                new QuotaUsageEntity(PROJECT_ID, SUBNET, 0, SUBNET_LIMIT, 0)
        );
        doNothing().when(quotaUsageRepository).addItem(any(QuotaUsageEntity.class));
        doNothing().when(applyRepository).addItem(any(ApplyInfo.class));
        mockMvc.perform(post(String.format(QUOTA_APPLY_TEMP, PROJECT_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(applyQuota))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apply_id").value(APPLY_ID));
    }

    @Test
    public void allocateQuota_OverLimitTest() throws Exception {
        mockLock();
        when(quotaUsageRepository.findItem(QuotaUtils.getCombineId(PROJECT_ID, NETWORK))).thenReturn(
                new QuotaUsageEntity(PROJECT_ID, NETWORK, NETWORK_LIMIT - 1, NETWORK_LIMIT, 0)
        );
        when(quotaUsageRepository.findItem(QuotaUtils.getCombineId(PROJECT_ID, SUBNET))).thenReturn(
                new QuotaUsageEntity(PROJECT_ID, SUBNET, SUBNET_LIMIT - 1, SUBNET_LIMIT, 0)
        );
        doNothing().when(quotaUsageRepository).addItem(any(QuotaUsageEntity.class));
        doNothing().when(applyRepository).addItem(any(ApplyInfo.class));
        mockMvc.perform(post(String.format(QUOTA_APPLY_TEMP, PROJECT_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(applyQuota))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void allocateQuota_NoApplyBodyTest() throws Exception {
        mockMvc.perform(post(String.format(QUOTA_APPLY_TEMP, PROJECT_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(applyEmptyDeltaQuota))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void cancelQuotaTest() throws Exception {
        mockLock();
        List<ResourceDelta> deltas = new ArrayList<>();
        deltas.add(new ResourceDelta(NETWORK, 2));
        deltas.add(new ResourceDelta(SUBNET, 2));
        when(applyRepository.findItem(APPLY_ID))
                .thenReturn(new ApplyInfo(APPLY_ID, PROJECT_ID, deltas));
        when(quotaUsageRepository.findItem(QuotaUtils.getCombineId(PROJECT_ID, NETWORK))).thenReturn(
                new QuotaUsageEntity(PROJECT_ID, NETWORK, NETWORK_LIMIT - 1, NETWORK_LIMIT, 0)
        );
        when(quotaUsageRepository.findItem(QuotaUtils.getCombineId(PROJECT_ID, SUBNET))).thenReturn(
                new QuotaUsageEntity(PROJECT_ID, SUBNET, SUBNET_LIMIT - 1, SUBNET_LIMIT, 0)
        );
        doNothing().when(quotaUsageRepository).addItem(any(QuotaUsageEntity.class));
        doNothing().when(applyRepository).deleteItem(APPLY_ID);
        mockMvc.perform(delete(String.format(QUOTA_CANCEL_APPLY_TEMP, APPLY_ID)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    public void cancelQuota_NoApplyTest() throws Exception {
        when(applyRepository.findItem(EMPTY_APPLY_ID))
                .thenReturn(null);
        mockMvc.perform(delete(String.format(QUOTA_CANCEL_APPLY_TEMP, APPLY_ID)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void cancelQuota_NoDeltaBodyTest() throws Exception {
        when(applyRepository.findItem(APPLY_ID))
                .thenReturn(new ApplyInfo(APPLY_ID, PROJECT_ID, null));
        mockMvc.perform(delete(String.format(QUOTA_CANCEL_APPLY_TEMP, APPLY_ID)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void cancelQuota_DeltaBodyEmptyTest() throws Exception {
        when(applyRepository.findItem(APPLY_ID))
                .thenReturn(new ApplyInfo(APPLY_ID, PROJECT_ID, Collections.emptyList()));
        mockMvc.perform(delete(String.format(QUOTA_CANCEL_APPLY_TEMP, APPLY_ID)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    public void cancelQuotaTest_NoQuotaUsage() throws Exception {
        mockLock();
        List<ResourceDelta> deltas = new ArrayList<>();
        deltas.add(new ResourceDelta(NETWORK, 2));
        deltas.add(new ResourceDelta(SUBNET, 2));
        when(applyRepository.findItem(APPLY_ID))
                .thenReturn(new ApplyInfo(APPLY_ID, PROJECT_ID, deltas));
        when(quotaUsageRepository.findItem(QuotaUtils.getCombineId(PROJECT_ID, NETWORK))).thenReturn(null);
        when(quotaUsageRepository.findItem(QuotaUtils.getCombineId(PROJECT_ID, SUBNET))).thenReturn(null);
        doNothing().when(quotaUsageRepository).addItem(any(QuotaUsageEntity.class));
        doNothing().when(applyRepository).deleteItem(APPLY_ID);
        mockMvc.perform(delete(String.format(QUOTA_CANCEL_APPLY_TEMP, APPLY_ID)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    private void mockLock() throws Exception {
        IDistributedLock lock = mock(IDistributedLock.class);
        doNothing().when(lock).lock(anyString());
        doNothing().when(lock).unlock(anyString());
        ReflectionTestUtils.setField(quotaService, "lock", lock);
    }
}
