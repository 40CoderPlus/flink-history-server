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

package com.fortycoderplus.flink.ext.historyserver.embedded;

import com.fortycoderplus.flink.ext.historyserver.HistoryServerArchiveFetcher;
import com.fortycoderplus.flink.ext.historyserver.HistoryServerRefreshLocation;
import com.fortycoderplus.flink.ext.historyserver.domain.Job;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class FlinkHistoryServerEmbedded {

    public static void main(String[] args) {
        SpringApplication.run(FlinkHistoryServerEmbedded.class, args);
    }

    @AllArgsConstructor
    @Component
    @Profile("dev")
    public static class RunOnDevMode implements CommandLineRunner {

        private Consumer<Job> archivedJobConsumer;

        @Override
        public void run(String... args) {
            HistoryServerArchiveFetcher fetcher = new HistoryServerArchiveFetcher(archivedJobConsumer, archive -> {});
            FileSystem fs = FileSystem.getLocalFileSystem();
            fetcher.fetchArchives(List.of(HistoryServerRefreshLocation.builder()
                    .fs(fs)
                    .path(new Path(fs.getWorkingDirectory().getParent(), "data"))
                    .build()));
        }
    }
}
