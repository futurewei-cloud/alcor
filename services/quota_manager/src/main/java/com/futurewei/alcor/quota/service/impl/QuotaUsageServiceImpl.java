/*
 *
 * Copyright 2019 The Alcor Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *         you may not use this file except in compliance with the License.
 *         You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *         See the License for the specific language governing permissions and
 *         limitations under the License.
 * /
 */

package com.futurewei.alcor.quota.service.impl;

import com.futurewei.alcor.quota.dao.ApplyRepository;
import com.futurewei.alcor.quota.dao.QuotaIUsageRepository;
import com.futurewei.alcor.quota.dao.ReservationRepository;
import com.futurewei.alcor.quota.exception.QuotaException;
import com.futurewei.alcor.quota.service.QuotaUsageService;
import com.futurewei.alcor.web.entity.quota.ApplyInfo;
import com.futurewei.alcor.web.entity.quota.ReservationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuotaUsageServiceImpl implements QuotaUsageService {

    @Autowired
    private ApplyRepository applyRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private QuotaIUsageRepository quotaIUsageRepository;

    @Override
    public ReservationInfo makeReservation(String projectId, String resourceName, int amount) throws QuotaException {
        return null;
    }

    @Override
    public String commitReservation(String reservationId) throws QuotaException {
        return null;
    }

    @Override
    public String cancelReservation(String reservationId) throws QuotaException {
        return null;
    }

    @Override
    public ApplyInfo allocateQuota(String projectId, String resourceName, int amount) throws QuotaException {
        return null;
    }

    @Override
    public String cancelQuota(String applyId) throws QuotaException {
        return null;
    }
}
