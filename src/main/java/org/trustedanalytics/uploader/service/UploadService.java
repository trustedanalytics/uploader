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
package org.trustedanalytics.uploader.service;

import org.trustedanalytics.uploader.rest.UploadCompleted.UploadCompletedBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
public class UploadService {

    private final Function<InputStream, InputStream> streamDecoder;

    private final BiConsumer<InputStream, UploadCompletedBuilder> streamConsumer;

    @Autowired
    public UploadService(Function<InputStream, InputStream> streamDecoder,
        BiConsumer<InputStream, UploadCompletedBuilder> streamConsumer) {
        this.streamDecoder = streamDecoder;
        this.streamConsumer = streamConsumer;
    }

    public void upload(InputStream stream, UploadCompletedBuilder builder) {
        try (InputStream input = streamDecoder.apply(stream)) {
            streamConsumer.accept(input, builder);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
