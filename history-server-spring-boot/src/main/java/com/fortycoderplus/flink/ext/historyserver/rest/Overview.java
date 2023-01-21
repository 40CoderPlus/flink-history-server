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

package com.fortycoderplus.flink.ext.historyserver.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Overview {

    @JsonProperty("flink-version")
    private String flinkVersion;

    @JsonProperty("flink-commit")
    private String flinkCommit;

    private int taskmanagers;

    @JsonProperty("slots-total")
    private int slotsTotal;

    @JsonProperty("slots-available")
    private int slotsAvailable;

    @JsonProperty("jobs-running")
    private int jobsRunning;

    @JsonProperty("jobs-cancelled")
    private int jobsCancelled;

    @JsonProperty("jobs-failed")
    private int jobsFailed;

    @JsonProperty("jobs-finished")
    private int jobsFinished;
}
