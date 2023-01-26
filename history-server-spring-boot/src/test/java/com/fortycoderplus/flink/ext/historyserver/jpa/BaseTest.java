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

import com.fortycoderplus.flink.ext.historyserver.domain.Job;
import com.fortycoderplus.flink.ext.historyserver.domain.Job.Tasks;
import com.fortycoderplus.flink.ext.historyserver.domain.JobState;
import com.fortycoderplus.flink.ext.historyserver.domain.JobXJson;
import java.util.List;

class BaseTest {

    static final long now = System.currentTimeMillis();
    static final List<Job> jobs = List.of(
            Job.builder()
                    .jid("1")
                    .name("test1")
                    .startTime(now)
                    .endTime(now + 1000L)
                    .state(JobState.FINISHED)
                    .duration(1000L)
                    .lastModification(now + 1000L)
                    .tasks(Tasks.builder()
                            .canceled(1)
                            .created(1)
                            .deploying(1)
                            .running(1)
                            .total(1)
                            .canceling(1)
                            .failed(1)
                            .finished(1)
                            .initializing(1)
                            .reconciling(1)
                            .scheduled(1)
                            .build())
                    .xJsons(List.of(JobXJson.builder()
                            .jid("1")
                            .json("{'foo':'bar'}")
                            .path("test")
                            .build()))
                    .build(),
            Job.builder()
                    .jid("2")
                    .name("test2")
                    .startTime(now)
                    .endTime(now + 2000L)
                    .state(JobState.CANCELED)
                    .duration(2000L)
                    .lastModification(now + 2000L)
                    .tasks(Tasks.builder()
                            .canceled(1)
                            .created(1)
                            .deploying(1)
                            .running(1)
                            .total(1)
                            .canceling(1)
                            .failed(1)
                            .finished(1)
                            .initializing(1)
                            .reconciling(1)
                            .scheduled(1)
                            .build())
                    .xJsons(List.of(JobXJson.builder()
                            .jid("2")
                            .json("{'zoo':'top'}")
                            .path("test")
                            .build()))
                    .build(),
            Job.builder()
                    .jid("3")
                    .name("test3")
                    .startTime(now)
                    .endTime(now + 3000L)
                    .state(JobState.FAILED)
                    .duration(3000L)
                    .lastModification(now + 3000L)
                    .tasks(Tasks.builder()
                            .canceled(1)
                            .created(1)
                            .deploying(1)
                            .running(1)
                            .total(1)
                            .canceling(1)
                            .failed(1)
                            .finished(1)
                            .initializing(1)
                            .reconciling(1)
                            .scheduled(1)
                            .build())
                    .xJsons(List.of(
                            JobXJson.builder().jid("3").json("{}").path("test").build()))
                    .build());
}
