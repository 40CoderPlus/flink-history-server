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

package com.fortycoderplus.flink.ext.historyserver.jpa;

import com.fortycoderplus.flink.ext.historyserver.domain.JobXJson;
import com.fortycoderplus.flink.ext.historyserver.domain.JobsOverview;
import com.fortycoderplus.flink.ext.historyserver.domain.Overview;
import com.fortycoderplus.flink.ext.historyserver.domain.Overview.OverviewBuilder;
import com.fortycoderplus.flink.ext.historyserver.jpa.mapper.FlinkJobMapper;
import com.fortycoderplus.flink.ext.historyserver.rest.FlinkRestApiService;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.flink.runtime.util.EnvironmentInformation;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

@AllArgsConstructor
public class FlinkRestApiJpaService implements FlinkRestApiService {

    private final FlinkJobRepository flinkJobRepository;
    private final FlinkJobXJsonRepository flinkJobXJsonRepository;

    @Override
    public Overview overview() {
        List<FlinkJobSummary> summaries = flinkJobRepository.findFlinkJobSummaryGroupByState();
        OverviewBuilder builder = Overview.builder()
                .flinkVersion(EnvironmentInformation.getVersion())
                .flinkCommit(EnvironmentInformation.getGitCommitId())
                .taskmanagers(0)
                .slotsTotal(0)
                .slotsAvailable(0);
        summaries.forEach(summary -> {
            switch (summary.getState()) {
                case FAILED:
                    builder.jobsFailed(summary.getCount());
                case CANCELED:
                    builder.jobsCancelled(summary.getCount());
                case FINISHED:
                    builder.jobsFinished(summary.getCount());
                default:
                    builder.jobsRunning(0);
            }
        });
        return builder.build();
    }

    @Override
    public JobsOverview latest(int n) {
        return JobsOverview.builder()
                .jobs(flinkJobRepository.findBy(PageRequest.of(0, n, Sort.by(Direction.DESC, "endTime"))).stream()
                        .map(FlinkJobMapper.INSTANCE::fromJpaEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public JobXJson json(String jid, String path) {
        Optional<FlinkJobXJson> xJson = flinkJobXJsonRepository.findByJidAndPath(jid, path);
        return JobXJson.builder()
                .json(xJson.map(FlinkJobXJson::getJson).orElse("{}"))
                .build();
    }
}
