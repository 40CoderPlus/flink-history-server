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

package com.fortycoderplus.flink.ext.historyserver.jpa.mapper;

import com.fortycoderplus.flink.ext.historyserver.domain.Job;
import com.fortycoderplus.flink.ext.historyserver.domain.Job.Tasks;
import com.fortycoderplus.flink.ext.historyserver.domain.JobState;
import com.fortycoderplus.flink.ext.historyserver.domain.JobXJson;
import com.fortycoderplus.flink.ext.historyserver.jpa.FlinkJob;
import com.fortycoderplus.flink.ext.historyserver.jpa.FlinkTask;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FlinkJobMapperTest {

    @Test
    void fromJpaEntity() {
        long now = System.currentTimeMillis();
        FlinkJob source = FlinkJob.builder()
                .jid("1")
                .name("test")
                .startTime(now)
                .endTime(now + 1000L)
                .state(JobState.FINISHED)
                .duration(1000L)
                .lastModification(now + 1000L)
                .task(FlinkTask.builder()
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
                .build();

        Job target = FlinkJobMapper.INSTANCE.fromJpaEntity(source);
        Assertions.assertEquals(1, target.getTasks().getCanceling());
        Assertions.assertEquals(1000L, target.getEndTime() - target.getStartTime());
    }

    @Test
    void toJpaEntity() {
        long now = System.currentTimeMillis();
        Job source = Job.builder()
                .jid("1")
                .name("test")
                .startTime(now)
                .endTime(now + 1000L)
                .state(JobState.CANCELED)
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
                .xJsons(List.of(
                        JobXJson.builder().jid("1").json("{}").path("test").build()))
                .build();

        FlinkJob target = FlinkJobMapper.INSTANCE.toJpaEntity(source);
        Assertions.assertEquals(1, target.getTask().getCanceling());
        Assertions.assertEquals(1000L, target.getEndTime() - target.getStartTime());
        Assertions.assertEquals(JobState.CANCELED, target.getState());
        Assertions.assertNull(target.getXJsons());
    }
}
