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
import java.util.List;
import java.util.function.Consumer;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.junit.jupiter.api.Test;

class HistoryServerArchiveFetcherTest {

    @Test
    void fetchArchives() {
        FileSystem fs = FileSystem.getLocalFileSystem();
        Consumer<Job> testConsumer = job -> assertEquals(
                1L, job.getXJsons().stream().map(JobXJson::getJid).distinct().count());
        HistoryServerArchiveFetcher fetcher = new HistoryServerArchiveFetcher(testConsumer, archive -> {});
        fetcher.fetchArchives(List.of(HistoryServerRefreshLocation.builder()
                .fs(fs)
                .path(new Path(fs.getWorkingDirectory().getParent().getParent(), "data"))
                .build()));
    }
}