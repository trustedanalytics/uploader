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
package org.trustedanalytics.uploader;

import org.trustedanalytics.store.InFolderObjectStore;
import org.trustedanalytics.store.ObjectStoreConfiguration;
import org.trustedanalytics.uploader.client.DataAcquisitionClient;
import org.trustedanalytics.uploader.core.stream.consumer.ObjectStoreStreamConsumer;
import org.trustedanalytics.uploader.core.stream.consumer.TriConsumer;
import org.trustedanalytics.uploader.rest.Transfer;
import org.trustedanalytics.uploader.security.PermissionVerifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Configuration
@Profile("local")
@Import(ObjectStoreConfiguration.class)
public class UploaderConfigurationLocal {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploaderConfiguration.class);

    @Bean
    public TriConsumer<InputStream, Transfer, UUID> streamConsumer(
            InFolderObjectStore store) {
        return new ObjectStoreStreamConsumer(x -> store);
    }

    @Bean
    public InFolderObjectStore inFolderObjectStore() {
        Path path = Paths.get(System.getProperty("user.dir"), "target", "uploads");
        path.toFile().mkdirs();
        return new InFolderObjectStore(path.toString());
    }

    @Bean
    public PermissionVerifier permissionVerifier() {
        return (org, auth) -> LOGGER.info("user-management: {}", org);
    }

    @Bean
    public DataAcquisitionClient dataAcquisitionClient() {
        return (message, token) -> LOGGER.info("data-acquisition: {}", message.toString());
    }
}
