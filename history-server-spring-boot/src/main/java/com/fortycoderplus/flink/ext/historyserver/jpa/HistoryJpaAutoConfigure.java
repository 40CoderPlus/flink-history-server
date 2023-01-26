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

import com.fortycoderplus.flink.ext.historyserver.HistoryServerProperties;
import com.fortycoderplus.flink.ext.historyserver.domain.Job;
import com.fortycoderplus.flink.ext.historyserver.rest.FlinkRestApiService;
import java.util.function.Consumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.support.ClasspathScanningPersistenceUnitPostProcessor;

@Configuration
@ConditionalOnClass(ClasspathScanningPersistenceUnitPostProcessor.class)
@EnableJpaRepositories(basePackages = "com.fortycoderplus.flink.ext.historyserver.jpa")
@EntityScan(basePackages = "com.fortycoderplus.flink.ext.historyserver.jpa")
public class HistoryJpaAutoConfigure {

    @Bean("archivedJobConsumer")
    @ConditionalOnMissingBean
    public Consumer<Job> archivedJobConsumer(JpaJobRepository jobRepository) {
        return new JobJpaMutator(jobRepository);
    }

    @Bean
    public FlinkRestApiService flinkRestApiService(
            HistoryServerProperties historyServerProperties,
            JpaJobRepository jobRepository,
            JobXJsonRepository jobXJsonRepository) {
        return new FlinkRestApiJpaService(historyServerProperties, jobRepository, jobXJsonRepository);
    }
}
