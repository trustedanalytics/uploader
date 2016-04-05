/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.trustedanalytics.uploader.core.listener;

import static org.apache.commons.io.FileUtils.byteCountToDisplaySize;

import com.google.common.base.Stopwatch;

import org.apache.tomcat.util.http.fileupload.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class FileUploadListener implements ProgressListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadListener.class);

    private final long interval;
    private final TimeUnit intervalTimeUnit;

    private final Stopwatch stopwatch;
    private final AtomicReference<String> filename;

    private long lastSnapshotSize;
    private long lastSnapshotTime;

    public FileUploadListener() {
        this(5, TimeUnit.SECONDS, Stopwatch.createUnstarted());
    }

    public FileUploadListener(long interval, TimeUnit intervalTimeUnit) {
        this(interval, intervalTimeUnit, Stopwatch.createUnstarted());
    }

    public FileUploadListener(long interval, TimeUnit intervalTimeUnit, Stopwatch stopwatch) {
        this.interval = interval;
        this.intervalTimeUnit = intervalTimeUnit;
        this.lastSnapshotSize = 0;
        this.lastSnapshotTime = 0;
        this.stopwatch = stopwatch.reset().start();
        this.filename = new AtomicReference<>();
    }

    public void setFilename(String filename) {
        this.filename.set(filename);
    }

    @Override
    public void update(long pBytesRead, long pContentLength, int pItems) {
        long snapshotTime = stopwatch.elapsed(intervalTimeUnit);
        if(snapshotTime >= (lastSnapshotTime + interval)) {
            snapshotProgress(pBytesRead, pContentLength, snapshotTime - lastSnapshotTime);
            lastSnapshotSize = pBytesRead;
            lastSnapshotTime = snapshotTime;
        }
    }

    private void snapshotProgress(long pBytesRead, long pContentLength, long window) {
        String speed = byteCountToDisplaySize((pBytesRead - lastSnapshotSize) / window);
        String uploaded = byteCountToDisplaySize(pBytesRead);
        String progress = (pContentLength != -1) ? String.valueOf(pBytesRead * 100 / pContentLength) : "--";
        LOGGER.info("File: {}, Uploaded: {}, Speed: {}/s, Progress: {}%",
            Optional.ofNullable(filename.get()).orElse("--"), uploaded, speed, progress);
    }
}
