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

package com.fortycoderplus.flink.ext.historyserver.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class Config {
    @JsonProperty("refresh-interval")
    private final long refreshInterval;

    @JsonProperty("timezone-name")
    private final String timeZoneName;

    @JsonProperty("timezone-offset")
    private final int timeZoneOffset;

    @JsonProperty("flink-version")
    private final String flinkVersion;

    @JsonProperty("flink-revision")
    private final String flinkRevision;

    @JsonProperty("features")
    private final Features features;

    @Builder
    @AllArgsConstructor
    @Data
    public static class Features {
        @JsonProperty("web-submit")
        private final boolean webSubmitEnabled;

        @JsonProperty("web-cancel")
        private final boolean webCancelEnabled;

        @JsonProperty("web-history")
        private final boolean isHistoryServer;
    }
}
