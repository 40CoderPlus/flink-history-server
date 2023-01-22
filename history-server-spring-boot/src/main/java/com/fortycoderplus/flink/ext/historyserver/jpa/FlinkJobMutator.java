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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortycoderplus.flink.ext.historyserver.HistoryServerArchivedJson;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FlinkJobMutator implements Consumer<List<HistoryServerArchivedJson>> {

    private static final String JOBS_OVERVIEW = "/jobs/overview";
    private static final ObjectMapper mapper = new ObjectMapper();

    private final FlinkJobRepository flinkJobRepository;

    @Override
    public void accept(List<HistoryServerArchivedJson> archivedJsons) {
        FlinkJob flinkJob = archivedJsons.stream()
                .filter(j -> j.getPath().equals(JOBS_OVERVIEW))
                .findFirst()
                .map(j -> {
                    try {
                        return mapper.readValue(j.getJson(), FlinkJob.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElseGet(() ->
                        FlinkJob.builder().jid(archivedJsons.get(0).getJobId()).build());
        archivedJsons.stream()
                .filter(j -> !j.getPath().equals(JOBS_OVERVIEW))
                .forEach(j -> flinkJob.addJobXJson(FlinkJobXJson.builder()
                        .json(j.getJson())
                        .path(j.getPath())
                        .jid(j.getJobId())
                        .build()));
        flinkJobRepository.save(flinkJob);
    }
}
