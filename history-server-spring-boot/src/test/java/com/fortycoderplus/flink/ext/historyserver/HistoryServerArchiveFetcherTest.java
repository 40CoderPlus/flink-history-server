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

package com.fortycoderplus.flink.ext.historyserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fortycoderplus.flink.ext.historyserver.domain.Job;
import com.fortycoderplus.flink.ext.historyserver.domain.JobXJson;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.junit.jupiter.api.Test;

class HistoryServerArchiveFetcherTest {

    @Test
    void fetchArchives() {
        FileSystem fs = FileSystem.getLocalFileSystem();
        JobHolderConsumer consumer = new JobHolderConsumer();
        HistoryServerArchiveFetcher fetcher = new HistoryServerArchiveFetcher(consumer, archive -> {});
        fetcher.fetchArchives(List.of(HistoryServerRefreshLocation.builder()
                .fs(fs)
                .path(new Path(fs.getWorkingDirectory().getParent().getParent(), "data"))
                .build()));

        assertEquals(6, consumer.getJobs().size());
        consumer.getJobs()
                .forEach(job -> assertEquals(
                        1L,
                        job.getXJsons().stream()
                                .map(JobXJson::getJid)
                                .distinct()
                                .count()));
        assertEquals(
                List.of(
                        "019656defad3d1cf1ef40906389d2764",
                        "536183bc448b0136867dfc4fadb69cb8",
                        "939b749e4c3d0dacb209c2b77a7b33c6",
                        "e7ac49cbd6a887ef69a482ba46cd4ca8",
                        "ea71b9a630c2d7102310c234cfb85933",
                        "f756069678a3aa03fee576cc8c5ac409"),
                consumer.getJobs().stream().map(Job::getJid).sorted().collect(Collectors.toList()));

        assertEquals(
                2, consumer.getJobs().stream().map(Job::getState).distinct().count());
    }

    static class JobHolderConsumer implements Consumer<Job> {

        private final List<Job> jobs = new ArrayList<>();

        @Override
        public void accept(Job job) {
            jobs.add(job);
        }

        public List<Job> getJobs() {
            return jobs;
        }
    }
}
