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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fortycoderplus.flink.ext.historyserver.domain.Job;
import com.fortycoderplus.flink.ext.historyserver.domain.JobXJson;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.JobID;
import org.apache.flink.core.fs.FSDataInputStream;
import org.apache.flink.core.fs.FileStatus;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.apache.flink.util.IOUtils;

@Slf4j
public class HistoryServerArchiveFetcher {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule((new Jdk8Module()))
            .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    public static final Consumer<HistoryServerJobArchive> defaultConsumerAfterFetch =
            HistoryServerArchiveFetcher::deleteArchive;

    private static final String JOBS_OVERVIEW = "/jobs/overview";
    private static final String ARCHIVE = "archive";
    private static final String PATH = "path";
    private static final String JSON = "json";

    private final Consumer<Job> archivedJobConsumer;
    private final Consumer<HistoryServerJobArchive> consumerAfterFetch;

    public HistoryServerArchiveFetcher(Consumer<Job> archivedJobConsumer) {
        this.archivedJobConsumer = archivedJobConsumer;
        consumerAfterFetch = defaultConsumerAfterFetch;
    }

    public HistoryServerArchiveFetcher(
            Consumer<Job> archivedJobConsumer, Consumer<HistoryServerJobArchive> consumerAfterFetch) {
        this.archivedJobConsumer = archivedJobConsumer;
        this.consumerAfterFetch = consumerAfterFetch;
    }

    // fetch flink job history
    public void fetchArchives(List<HistoryServerRefreshLocation> refreshDirs) {
        refreshDirs.stream()
                .flatMap(dir -> {
                    FileStatus[] jobArchives = new FileStatus[0];
                    try {
                        jobArchives = listArchives(dir.getFs(), dir.getPath());
                    } catch (IOException e) {
                        logger.error("Failed to access job archive location for path {}.", dir, e);
                    }
                    return Stream.of(jobArchives).map(archive -> HistoryServerJobArchive.builder()
                            .fileStatus(archive)
                            .fs(dir.getFs())
                            .rootPath(dir.getPath())
                            .build());
                })
                .forEach(archive -> {
                    Path jobArchivePath = archive.getFileStatus().getPath();
                    String jobID = jobArchivePath.getName();
                    if (!isValidJobID(jobID, archive.getRootPath().getPath())) {
                        deleteArchive(archive, false);
                    } else {
                        logger.info("Processing archive {}.", jobArchivePath);
                        try {
                            archivedJobConsumer.accept(getArchivedJob(jobID, jobArchivePath));
                        } catch (IOException ignore) {
                        } catch (Exception ex) {
                            logger.error("Consume archived jsons from path {} failed.", jobArchivePath, ex);
                        }
                        consumerAfterFetch.accept(archive);
                    }
                });
    }

    /**
     * @param refreshFS flink filesystem. such as hdfs/s3
     * @param refreshDir the path to get flink job history
     * @return FileStatus[] the array of job history content
     * @throws IOException io exception
     */
    private static FileStatus[] listArchives(FileSystem refreshFS, Path refreshDir) throws IOException {
        // contents of /:refreshDir
        FileStatus[] jobArchives = refreshFS.listStatus(refreshDir);
        // the entire refreshDirectory was removed
        return Objects.requireNonNullElseGet(jobArchives, () -> new FileStatus[0]);
    }

    /**
     * copy from flink
     *
     * @param jobId flink job id
     * @param refreshDir flink history archive directory
     * @return boolean.
     */
    private static boolean isValidJobID(String jobId, String refreshDir) {
        try {
            JobID.fromHexString(jobId);
            return true;
        } catch (IllegalArgumentException iae) {
            logger.error(
                    "Archive directory {} contained file with unexpected name {}. Ignoring file.",
                    refreshDir,
                    jobId,
                    iae);
            return false;
        }
    }

    /**
     * Reads the given archive file and returns a {@link Job}.
     *
     * @param file archive to extract
     * @return Job an archived job
     * @throws IOException if the file can't be opened, read or doesn't contain valid json
     */
    private static Job getArchivedJob(String jobId, Path file) throws IOException {
        try (FSDataInputStream input = file.getFileSystem().open(file);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            IOUtils.copyBytes(input, output);

            try {
                JsonNode archive = mapper.readTree(output.toByteArray());
                List<JobXJson> archivedJsons = StreamSupport.stream(
                                archive.get(ARCHIVE).spliterator(), false)
                        .map(part -> {
                            String path = part.get(PATH).asText();
                            String json = part.get(JSON).asText();
                            return JobXJson.builder()
                                    .jid(jobId)
                                    .path(path)
                                    .json(json)
                                    .build();
                        })
                        .collect(Collectors.toList());
                Job job = archivedJsons.stream()
                        .filter(archiveJson -> archiveJson.getPath().equals(JOBS_OVERVIEW))
                        .findAny()
                        .map(archiveJson -> {
                            try {
                                JsonNode overview =
                                        mapper.readTree(archiveJson.getJson()).get("jobs");
                                return mapper.convertValue(overview.get(0), Job.class);
                            } catch (JsonProcessingException ex) {
                                throw new RuntimeException(ex);
                            }
                        })
                        .orElseThrow(() -> new IllegalArgumentException("Job overview not found"));
                job.setXJsons(archivedJsons.stream()
                        .filter(j -> !j.getPath().equals(JOBS_OVERVIEW))
                        .map(j -> JobXJson.builder()
                                .json(j.getJson())
                                .path(j.getPath())
                                .jid(j.getJid())
                                .build())
                        .collect(Collectors.toList()));
                return job;
            } catch (NullPointerException npe) {
                // occurs if the archive is empty or any of the expected fields are not present
                throw new IOException("Job archive (" + file.getPath() + ") did not conform to expected format.");
            }
        }
    }

    private static void deleteArchive(HistoryServerJobArchive archive, boolean valid) {
        try {
            archive.getFs().delete(archive.getFileStatus().getPath(), false);
        } catch (IOException ex) {
            if (valid) {
                logger.error(
                        "Archive file {} delete failed.",
                        archive.getFileStatus().getPath(),
                        ex);
            } else {
                logger.warn(
                        "Invalid archive file {} delete failed.",
                        archive.getFileStatus().getPath(),
                        ex);
            }
        }
    }

    public static void deleteArchive(HistoryServerJobArchive archive) {
        deleteArchive(archive, true);
    }
}
