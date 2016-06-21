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

import feign.Logger.Level;
import org.trustedanalytics.store.ObjectStoreFactory;
import org.trustedanalytics.uploader.client.DataAcquisitionClient;
import org.trustedanalytics.uploader.client.ScramblingSlf4jLogger;
import org.trustedanalytics.uploader.client.UserManagementClient;
import org.trustedanalytics.uploader.core.stream.consumer.ObjectStoreStreamConsumer;
import org.trustedanalytics.uploader.core.stream.consumer.TriConsumer;
import org.trustedanalytics.uploader.rest.FeignErrorDecoder;
import org.trustedanalytics.uploader.rest.Transfer;
import org.trustedanalytics.uploader.security.OrgPermissionVerifier;

import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;

import java.io.InputStream;
import java.util.UUID;
import java.util.function.Function;

@Configuration
@Profile("cloud")
public class UploaderConfigurationCloud {

    @Value("${services.dataacquisition.url}")
    private String dataAcquisitionUrl;

    @Value("${services.user-management.url}")
    private String userManagementUrl;

    @Bean
    public TriConsumer<InputStream, Transfer, UUID> streamConsumer(
            ObjectStoreFactory<UUID> store) {
        return new ObjectStoreStreamConsumer(store);
    }

    @Bean
    public OrgPermissionVerifier orgPermissionVerifier(UserManagementClient userManagementClient,
            Function<Authentication, String> tokenExtractor) {
        return new OrgPermissionVerifier(userManagementClient, tokenExtractor);
    }

    @Bean
    public DataAcquisitionClient dataAcquisitionClient() {
        return getClient(DataAcquisitionClient.class, dataAcquisitionUrl, Level.FULL);
    }

    @Bean
    public UserManagementClient userManagementClient() {
        return getClient(UserManagementClient.class, userManagementUrl, Level.BASIC);
    }

    private <T> T getClient(Class<T> clientType, String url, Level logLevel) {
        return Feign.builder()
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logger(new ScramblingSlf4jLogger(clientType))
                .logLevel(logLevel)
                .errorDecoder(new FeignErrorDecoder())
                .target(clientType, url);
    }
}
