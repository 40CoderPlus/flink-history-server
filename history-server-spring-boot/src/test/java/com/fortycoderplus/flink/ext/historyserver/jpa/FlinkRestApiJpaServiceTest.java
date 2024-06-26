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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fortycoderplus.flink.ext.historyserver.HistoryServerProperties;
import com.fortycoderplus.flink.ext.historyserver.domain.Job;
import com.fortycoderplus.flink.ext.historyserver.domain.Overview;
import com.fortycoderplus.flink.ext.historyserver.rest.FlinkRestApiService;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@TestPropertySource(locations = {"classpath:application.yml"})
@DataJpaTest
@EnableJpaRepositories(basePackages = {"com.fortycoderplus.flink.ext.*"})
@EntityScan(basePackages = "com.fortycoderplus.flink.ext.*")
class FlinkRestApiJpaServiceTest extends BaseTest {

    FlinkRestApiService flinkRestApiService;
    JobJpaMutator jobJpaMutator;

    @Resource
    JpaJobRepository jobRepository;

    @Resource
    JobXJsonRepository jobXJsonRepository;

    @Test
    void overview() {
        Overview overview = flinkRestApiService.overview();
        assertEquals(1L, overview.getJobsFailed());
        assertEquals(1L, overview.getJobsCancelled());
        assertEquals(1L, overview.getJobsFinished());
    }

    @Test
    void latest() {
        List<Job> top1 = flinkRestApiService.latest(1).getJobs();
        List<Job> top2 = flinkRestApiService.latest(2).getJobs();
        List<Job> top3 = flinkRestApiService.latest(3).getJobs();

        assertEquals(1L, top1.size());
        assertEquals("3", top1.get(0).getJid());
        assertEquals(2L, top2.size());
        assertEquals("3", top2.get(0).getJid());
        assertEquals("2", top2.get(1).getJid());
        assertEquals(3L, top3.size());
        assertEquals("3", top3.get(0).getJid());
        assertEquals("1", top3.get(2).getJid());
    }

    @Test
    void json() {
        Optional<JpaJobXJson> empty = jobXJsonRepository.findByJidAndPath("x", "y");
        assertTrue(empty.isEmpty());
        Optional<JpaJobXJson> exists = jobXJsonRepository.findByJidAndPath("1", "test");
        assertTrue(exists.isPresent());
    }

    @BeforeEach
    void setUp() {
        flinkRestApiService =
                new FlinkRestApiJpaService(new HistoryServerProperties(), jobRepository, jobXJsonRepository);
        jobJpaMutator = new JobJpaMutator(jobRepository);
        jobs.forEach(jobJpaMutator);
    }

    @AfterEach
    void tearDown() {
        jobRepository.deleteAll();
    }
}
