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
import org.trustedanalytics.uploader.rest.UploadResponse.UploadResponseBuilder;

import org.apache.hadoop.security.AccessControlException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Function;

import javax.security.auth.login.LoginException;

@Service
public class UploadService {

    private final Function<InputStream, InputStream> streamDecoder;

    private final TriConsumer<InputStream, UploadResponseBuilder, UUID> streamConsumer;

    @Autowired
    public UploadService(Function<InputStream, InputStream> streamDecoder,
            TriConsumer<InputStream, UploadResponseBuilder, UUID> streamConsumer) {
        this.streamDecoder = streamDecoder;
        this.streamConsumer = streamConsumer;
    }

    public void upload(InputStream stream, UploadResponseBuilder builder, UUID orgUUID) {
        try (InputStream input = streamDecoder.apply(stream)) {
            streamConsumer.accept(input, builder, orgUUID);
        } catch (AccessControlException ex) {
            throw new AccessDeniedException("Permission denied", ex);
        } catch (IOException | LoginException | InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
