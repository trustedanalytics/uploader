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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.trustedanalytics.uploader.rest.UploadCompleted;
import org.trustedanalytics.uploader.rest.UploadCompleted.UploadCompletedBuilder;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.UUID;

@RunWith(Parameterized.class)
public class UploadRequestTest {

    @Parameterized.Parameters(name = "{index} - {0}")
    public static Iterable<Object[]> data() {
        // @formatter:off
        return Arrays.asList(
            new Object[][] {
                {"test without category specified", UploadCompleted.builder()
                    .setOrgUUID(UUID.randomUUID().toString())
                    .setPublicAccess(true)
                    .setSource("source")
                    .setTitle("title")
                    .setObjectStoreId("objectStoreId")
                    .setSavedObjectId("savedObjectId"), containsString("other")},

                {"test with category specified", UploadCompleted.builder()
                    .setOrgUUID(UUID.randomUUID().toString())
                    .setCategory("finance")
                    .setPublicAccess(true)
                    .setSource("source")
                    .setTitle("title")
                    .setObjectStoreId("objectStoreId")
                    .setSavedObjectId("savedObjectId"), containsString("finance")},

                {"test without public access specified", UploadCompleted.builder()
                    .setOrgUUID(UUID.randomUUID().toString())
                    .setCategory("finance")
                    .setSource("source")
                    .setTitle("title")
                    .setObjectStoreId("objectStoreId")
                    .setSavedObjectId("savedObjectId"), containsString("false")},

                {"test with public access specified", UploadCompleted.builder()
                    .setOrgUUID(UUID.randomUUID().toString())
                    .setPublicAccess(true)
                    .setCategory("finance")
                    .setSource("source")
                    .setTitle("title")
                    .setObjectStoreId("objectStoreId")
                    .setSavedObjectId("savedObjectId"), containsString("true")}
            });
        // @formatter:on
    }

    @Parameterized.Parameter(0)
    public String testName;

    @Parameterized.Parameter(1)
    public UploadCompletedBuilder builder;

    @Parameterized.Parameter(2)
    public Matcher<String> matcher;

    @Test
    public void testUploadRequest() {
        assertThat(builder.build().toString(), matcher);
    }
}
