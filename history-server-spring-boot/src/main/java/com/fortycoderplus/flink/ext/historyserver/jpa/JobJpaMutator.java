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
import com.fortycoderplus.flink.ext.historyserver.jpa.mapper.JobMapper;
import com.fortycoderplus.flink.ext.historyserver.jpa.mapper.JobXJsonMapper;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JobJpaMutator implements Consumer<Job> {
    private final JpaJobRepository jobRepository;

    @Override
    public void accept(Job job) {
        if (!jobRepository.existsByJid(job.getJid())) {
            JpaJob entity = JobMapper.INSTANCE.toJpaEntity(job);
            job.getXJsons().stream()
                    .map(xJson -> {
                        JpaJobXJson result = JobXJsonMapper.INSTANCE.toJpaEntity(xJson);
                        result.setJob(entity);
                        return result;
                    })
                    .forEach(entity::addJobXJson);
            jobRepository.save(entity);
        }
    }
}
