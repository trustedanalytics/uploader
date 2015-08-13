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

import com.google.common.base.Stopwatch;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.http.fileupload.ProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class FileUploadListener implements ProgressListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadListener.class);
    private final Stopwatch stopwatch;
    private final long interval = 5;

    private long lastTimestamp = 0;
    private long lastTimestampBytes = 0;

    public FileUploadListener() {
        this(Stopwatch.createUnstarted());
    }

    public FileUploadListener(Stopwatch stopwatch) {
        this.stopwatch = stopwatch.reset().start();
    }

    @Override
    public void update(long pBytesRead, long pContentLength, int pItems) {
        long timestamp = stopwatch.elapsed(TimeUnit.SECONDS);
        if ((timestamp != lastTimestamp) && (timestamp % interval == 0)) {
            log(pBytesRead, pContentLength);
            lastTimestampBytes = pBytesRead;
            lastTimestamp = timestamp;
        }
    }

    private void log(long pBytesRead, long pContentLength) {
        final String speed = FileUtils.byteCountToDisplaySize((pBytesRead - lastTimestampBytes) / interval);
        final String uploaded = FileUtils.byteCountToDisplaySize(pBytesRead);
        if (pContentLength != -1) {
            String progress = String.valueOf(pBytesRead * 100 / pContentLength);
            LOGGER.info("Uploaded: {}, Speed: {}/s, Progress: {}%", uploaded, speed, progress);
        } else {
            LOGGER.info("Uploaded: {}, Speed: {}/s", uploaded, speed);
        }
    }
}
