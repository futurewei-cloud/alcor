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

package com.futurewei.alcor.quota.controller;

import com.futurewei.alcor.common.entity.ResponseId;
import com.futurewei.alcor.web.entity.quota.ApplyInfo;
import com.futurewei.alcor.web.entity.quota.QuotaDetailWebJson;
import com.futurewei.alcor.web.entity.quota.ReservationInfo;
import io.lettuce.core.dynamic.annotation.CommandNaming;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * this rest apis for alcor inner micro services
 */
@Controller
public class QuotaInnerController {


    @GetMapping("/project/{projectId}/resource/{resourceName}/reservation/{amount}")
    public ReservationInfo makeReservation(@PathVariable String projectId,
                                           @PathVariable String resourceName,
                                           @PathVariable int amount) {
        return null;
    }

    @GetMapping("/reservation/{reservationId}/commit")
    public ResponseId commitReservation(@PathVariable String reservationId) {
        return null;
    }

    @GetMapping("/reservation/{reservationId/cancel}")
    public ResponseId cancelReservation(@PathVariable String reservationId) {
        return null;
    }

    @GetMapping("/project/{projectId}/resource/{resourceName}/apply/{amount}")
    public ApplyInfo allocateQuotaDirectly(@PathVariable String projectId,
                                           @PathVariable String resourceName,
                                           @PathVariable int amount) {
        return null;
    }

    @GetMapping("/project/{projectId}/resource/{resourceName}/cancel/{amount}")
    public ResponseId cancelQuotaDirectly(@PathVariable String projectId,
                                                 @PathVariable String resourceName,
                                                 @PathVariable int amount) {
        return null;
    }

}
