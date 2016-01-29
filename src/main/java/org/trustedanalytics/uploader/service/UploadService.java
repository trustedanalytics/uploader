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

import org.trustedanalytics.uploader.core.stream.consumer.TriConsumer;
import org.trustedanalytics.uploader.rest.UploadCompleted.UploadCompletedBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.auth.login.LoginException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Function;

@Service
public class UploadService {

    private final Function<InputStream, InputStream> streamDecoder;

    private final TriConsumer<InputStream, UploadCompletedBuilder, UUID> streamConsumer;

    @Autowired
    public UploadService(Function<InputStream, InputStream> streamDecoder,
            TriConsumer<InputStream, UploadCompletedBuilder, UUID> streamConsumer) {
        this.streamDecoder = streamDecoder;
        this.streamConsumer = streamConsumer;
    }

    public void upload(InputStream stream, UploadCompletedBuilder builder, UUID orgUUID) {
        try (InputStream input = streamDecoder.apply(stream)) {
            streamConsumer.accept(input, builder, orgUUID);
        } catch (IOException | LoginException | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
