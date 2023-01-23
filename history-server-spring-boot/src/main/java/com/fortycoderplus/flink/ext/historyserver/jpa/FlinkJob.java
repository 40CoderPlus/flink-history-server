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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fortycoderplus.flink.ext.historyserver.domain.JobState;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flink_ext_job")
public class FlinkJob extends AbstractPersistable<UUID> {

    private String jid;
    private String name;

    @Enumerated(EnumType.STRING)
    private JobState state;

    private long startTime;
    private long endTime;
    private long duration;
    private long lastModification;

    private FlinkTask task;

    @OneToMany(mappedBy = "job", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JsonIgnore
    private List<FlinkJobXJson> xJsons;

    public void addJobXJson(FlinkJobXJson xJson) {
        if (xJsons == null) {
            xJsons = new ArrayList<>();
        }
        xJsons.add(xJson);
    }
}
