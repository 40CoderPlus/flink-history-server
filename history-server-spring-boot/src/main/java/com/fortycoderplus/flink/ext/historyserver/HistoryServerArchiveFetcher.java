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
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.util.IOUtils;
import org.apache.flink.util.jackson.JacksonMapperFactory;

@Slf4j
public class HistoryServerArchiveFetcher {

    private static final ObjectMapper mapper = JacksonMapperFactory.createObjectMapper();
    private static final Consumer<HistoryServerJobArchive> defaultConsumerAfterFetch =
            HistoryServerArchiveFetcher::deleteArchive;
    private static final String ARCHIVE = "archive";
    private static final String PATH = "path";
    private static final String JSON = "json";

    private final Consumer<List<HistoryServerArchivedJson>> archivedJsonConsumer;
    private final Consumer<HistoryServerJobArchive> consumerAfterFetch;

    public HistoryServerArchiveFetcher(Consumer<List<HistoryServerArchivedJson>> archivedJsonConsumer) {
        this.archivedJsonConsumer = archivedJsonConsumer;
        consumerAfterFetch = defaultConsumerAfterFetch;
    }

    public HistoryServerArchiveFetcher(
            Consumer<List<HistoryServerArchivedJson>> archivedJsonConsumer,
            Consumer<HistoryServerJobArchive> consumerAfterFetch) {
        this.archivedJsonConsumer = archivedJsonConsumer;
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
                            archivedJsonConsumer.accept(getArchivedJsons(jobID, jobArchivePath));
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
     * Reads the given archive file and returns a {@link List} of contained {@link
     * HistoryServerArchivedJson}.
     *
     * @param file archive to extract
     * @return collection of archived jsons
     * @throws IOException if the file can't be opened, read or doesn't contain valid json
     */
    private static List<HistoryServerArchivedJson> getArchivedJsons(String jobId, Path file) throws IOException {
        try (FSDataInputStream input = file.getFileSystem().open(file);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            IOUtils.copyBytes(input, output);

            try {
                JsonNode archive = mapper.readTree(output.toByteArray());
                return StreamSupport.stream(archive.get(ARCHIVE).spliterator(), false)
                        .map(part -> {
                            String path = part.get(PATH).asText();
                            String json = part.get(JSON).asText();
                            return HistoryServerArchivedJson.builder()
                                    .jobId(jobId)
                                    .path(path)
                                    .json(json)
                                    .build();
                        })
                        .collect(Collectors.toList());
            } catch (NullPointerException npe) {
                // occurs if the archive is empty or any of the expected fields are not present
                throw new IOException("Job archive (" + file.getPath() + ") did not conform to expected format.");
            }
        }
    }

    private static void deleteArchive(HistoryServerJobArchive archive) {
        deleteArchive(archive, true);
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
}
