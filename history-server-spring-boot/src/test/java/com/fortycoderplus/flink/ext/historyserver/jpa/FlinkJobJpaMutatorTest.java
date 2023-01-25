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

import javax.annotation.Resource;
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
class FlinkJobJpaMutatorTest extends BaseTest {

    FlinkJobJpaMutator flinkJobJpaMutator;

    @Resource
    JpaJobRepository jobRepository;

    @Resource
    JobXJsonRepository jobXJsonRepository;

    @Test
    void accept() {
        jobs.forEach(flinkJobJpaMutator);
        assertEquals(3L, jobRepository.count());
        jobRepository.findAll().forEach(job -> assertEquals(1L, job.getXJsons().size()));
        assertEquals(
                "{'foo':'bar'}",
                jobXJsonRepository.findByJidAndPath("1", "test").get().getJson());
    }

    @BeforeEach
    void setUp() {
        flinkJobJpaMutator = new FlinkJobJpaMutator(jobRepository);
    }

    @AfterEach
    void tearDown() {
        jobRepository.deleteAll();
    }
}
