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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Throwables;
import org.apache.tomcat.util.http.fileupload.FileItemIterator;
import org.apache.tomcat.util.http.fileupload.FileItemStream;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.store.InFolderObjectStore;
import org.trustedanalytics.uploader.client.DataAcquisitionClient;
import org.trustedanalytics.uploader.core.listener.FileUploadListener;
import org.trustedanalytics.uploader.core.stream.consumer.ObjectStoreStreamConsumer;
import org.trustedanalytics.uploader.rest.UploadRequest;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.function.Function;

@RunWith(MockitoJUnitRunner.class)
public class UploadServiceTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    private DataAcquisitionClient client;

    @Mock
    private FileItemIterator fileItemIterator;

    @Mock
    private FileItemStream fileItemStream;

    private UploadService sut;

    private UUID orgGuid = UUID.randomUUID();

    @Before
    public void before() throws Exception {
        sut = new UploadService(Function.identity(),
                new ObjectStoreStreamConsumer((x) -> new InFolderObjectStore(folder.getRoot().toString())), client, auth -> "token");
    }

    @Test
    public void testUploadFile() throws IOException, FileUploadException {

        // given
        final UploadRequest request = new UploadRequest(UUID.randomUUID(), new FileUploadListener());

        // when
        mockFileItemIterator("title");

        // then
        assertThat(sut.processUpload(fileItemIterator, request, false),  Matchers.hasItem(allOf(hasProperty("category", is("category")),
                                                                                                hasProperty("title", is("title")),
                                                                                                hasProperty("source", is("source")),
                                                                                                hasProperty("orgUUID", is(orgGuid.toString())),
                                                                                                hasProperty("publicAccess", is(false)),
                                                                                                hasProperty("objectStoreId", is("file://" + folder.getRoot().toString() + "/")))));
    }

    @Test
    public void testUploadFiles() throws IOException, FileUploadException {

        // given
        UploadRequest request = new UploadRequest(UUID.randomUUID(), new FileUploadListener());

        // when
        mockFileItemIterator("source");

        // then
        assertThat(sut.processUpload(fileItemIterator, request, true),  Matchers.hasItem(allOf(hasProperty("category", is("category")),
                                                                                               hasProperty("title", is("source")),
                                                                                               hasProperty("source", is("source")),
                                                                                               hasProperty("orgUUID", is(orgGuid.toString())),
                                                                                               hasProperty("publicAccess", is(false)),
                                                                                               hasProperty("objectStoreId", is("file://" + folder.getRoot().toString() + "/")))));
    }

    private void mockFileItemIterator(String title) throws IOException, FileUploadException {

        final FileItemStream categoryField = mockFileItemStream(true, "category", "category");
        final FileItemStream orgUUIDField = mockFileItemStream(true, "orgUUID", orgGuid.toString());
        final FileItemStream titleField = mockFileItemStream(true, "title", title);
        final FileItemStream accessField = mockFileItemStream(true, "publicRequest", "false");
        final FileItemStream sourceField = mockFileItemStream(false, "source", "source");

        // mocking multipart requests
        when(fileItemIterator.hasNext()).thenReturn(true, true, true, true, true, false);
        when(fileItemIterator.next()).thenReturn(categoryField, orgUUIDField, titleField, accessField, sourceField);
    }

    private FileItemStream mockFileItemStream(boolean isFormField, String fieldName, String fieldValue) {
        FileItemStream stream = mock(FileItemStream.class);
        when(stream.isFormField()).thenReturn(isFormField);
        when(stream.getFieldName()).thenReturn(fieldName);
        when(stream.getName()).thenReturn(fieldValue);
        try {
            when(stream.openStream()).thenReturn(new ByteArrayInputStream(fieldValue.getBytes()));
        } catch (IOException e) {
            Throwables.propagate(e);
        }
        return stream;
    }
}
