/*
 * (c) Copyright 2017-2023 40CoderPlus. All rights reserved.
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

package com.fortycoderplus.flink.ext.historyserver.embedded.interfaces;

import com.fortycoderplus.flink.ext.historyserver.domain.Config;
import com.fortycoderplus.flink.ext.historyserver.domain.Job;
import com.fortycoderplus.flink.ext.historyserver.domain.JobXJson;
import com.fortycoderplus.flink.ext.historyserver.domain.Overview;
import com.fortycoderplus.flink.ext.historyserver.rest.FlinkRestApiService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Slf4j
@AllArgsConstructor
@RestController
public class FlinkRestEndpoint {

    private FlinkRestApiService flinkRestApiService;

    @RequestMapping(method = RequestMethod.GET, path = "/config")
    public Config dashboardConfiguration() {
        return flinkRestApiService.config();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/overview")
    public Overview overview() {
        return flinkRestApiService.overview();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/jobs/overview")
    public Jobs jobsOverview() {
        return Jobs.builder().jobs(flinkRestApiService.latest()).build();
    }

    @RequestMapping(method = RequestMethod.GET, path = "/jobs/{jid}/**")
    public JobXJson path(@PathVariable String jid) {
        return flinkRestApiService.json(
                jid, ServletUriComponentsBuilder.fromCurrentRequest().build().getPath());
    }

    @Builder
    @Data
    public static class Jobs {
        private List<Job> jobs;
    }
}
