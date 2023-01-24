/*
 * (c) Copyright 2023 40CoderPlus. All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fortycoderplus.flink.ext.historyserver.rest;

import com.fortycoderplus.flink.ext.historyserver.domain.Config;
import com.fortycoderplus.flink.ext.historyserver.domain.Config.Features;
import com.fortycoderplus.flink.ext.historyserver.domain.JobXJson;
import com.fortycoderplus.flink.ext.historyserver.domain.JobsOverview;
import com.fortycoderplus.flink.ext.historyserver.domain.Overview;
import java.time.ZonedDateTime;
import org.apache.flink.runtime.rest.messages.DashboardConfiguration;

/**
 * Notice: just for Flink History Server Rest API
 */
public interface FlinkRestApiService {

    /**
     * Flink Dashboard Config. Default Config is only enable history.
     *
     * @return Config
     */
    default Config config() {
        return config(10000L);
    }

    default Config config(long refreshInterval) {
        DashboardConfiguration configuration =
                DashboardConfiguration.from(refreshInterval, ZonedDateTime.now(), false, false, true);
        return Config.builder()
                .refreshInterval(configuration.getRefreshInterval())
                .flinkVersion(configuration.getFlinkVersion())
                .flinkRevision(configuration.getFlinkRevision())
                .timeZoneName(configuration.getTimeZoneName())
                .timeZoneOffset(configuration.getTimeZoneOffset())
                .features(Features.builder()
                        .webCancelEnabled(configuration.getFeatures().isWebCancelEnabled())
                        .webSubmitEnabled(configuration.getFeatures().isWebSubmitEnabled())
                        .isHistoryServer(configuration.getFeatures().isHistoryServer())
                        .build())
                .build();
    }

    /**
     * For Flink Rest Endpoint : /overview
     *
     * @return Overview
     */
    Overview overview();

    /**
     * For Flink Rest Endpoint : /jobs/overview
     * Order by job end time
     * @return List<Job>
     */
    default JobsOverview latest() {
        return latest(50);
    }

    /**
     * For Flink Rest Endpoint : /jobs/overview
     * Order by job end time
     * @param size number of page size.
     * @return List<Job>
     */
    JobsOverview latest(int size);

    /**
     * For Flink Rest Endpoint : /jobs/{id}/**
     * @param jid  Job ID, path variable of {id}
     * @param path the path of suffix /jobs/{id}
     * @return JobXJson
     */
    JobXJson json(String jid, String path);
}
