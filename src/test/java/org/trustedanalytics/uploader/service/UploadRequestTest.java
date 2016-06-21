/**
 * Copyright (c) 2016 Intel Corporation
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


import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.trustedanalytics.uploader.rest.UploadMetadata;
import org.trustedanalytics.uploader.rest.UploadMetadata.UploadMetadataBuilder;

import java.util.Arrays;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

@RunWith(Parameterized.class)
public class UploadRequestTest {
    @Parameterized.Parameters(name = "{index} - {0}")
    public static Iterable<Object[]> data() {
        // @formatter:off
        return Arrays.asList(
                new Object[][]{
                        {"test without category specified", UploadMetadata.builder()
                                .setOrgUUID(UUID.randomUUID().toString())
                                .setPublicAccess(true)
                                .setSource("source")
                                .setTitle("title"), containsString("other")},

                        {"test with category specified", UploadMetadata.builder()
                                .setOrgUUID(UUID.randomUUID().toString())
                                .setCategory("finance")
                                .setPublicAccess(true)
                                .setSource("source")
                                .setTitle("title"), containsString("finance")},

                        {"test without public access specified", UploadMetadata.builder()
                                .setOrgUUID(UUID.randomUUID().toString())
                                .setCategory("finance")
                                .setSource("source")
                                .setTitle("title"), containsString("false")},

                        {"test with public access specified", UploadMetadata.builder()
                                .setOrgUUID(UUID.randomUUID().toString())
                                .setPublicAccess(true)
                                .setCategory("finance")
                                .setSource("source")
                                .setTitle("title"),  containsString("true")},

                        {"test with form fields", UploadMetadata.builder()
                                .setSource("source")
                                .setProperty("orguuid", UUID.randomUUID().toString())
                                .setProperty("publicRequest", "true")
                                .setProperty("category", "finance")
                                .setProperty("title", "title"), allOf(containsString("title"),
                                containsString("true"),
                                containsString("finance"))
                        }
                });
        // @formatter:on
    }

    @Parameterized.Parameter(0)
    public String testName;

    @Parameterized.Parameter(1)
    public UploadMetadataBuilder builder;

    @Parameterized.Parameter(2)
    public Matcher<String> matcher;

    @Test
    public void testUploadRequest() {
        assertThat(builder.build().toString(), matcher);
    }
}
