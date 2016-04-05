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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.trustedanalytics.store.ObjectStore;
import org.trustedanalytics.uploader.core.stream.consumer.ObjectStoreStreamConsumer;
import org.trustedanalytics.uploader.rest.UploadResponse.UploadResponseBuilder;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Function;

public class UploadServiceTest {
    private static final String OBJECT_STORE_ID = "object_store_id";
    private static final String SAVED_OBJECT_ID = "saved_object_id";

    @Test
    public void testUpload() throws IOException {
        // given
        final UploadResponseBuilder builder = mock(UploadResponseBuilder.class);
        when(builder.setObjectStoreId(OBJECT_STORE_ID)).thenReturn(builder);
        when(builder.setSavedObjectId(SAVED_OBJECT_ID)).thenReturn(builder);

        final InputStream inputStream = mock(InputStream.class);
        final ObjectStore objectStore = mock(ObjectStore.class);
        when(objectStore.save(inputStream)).thenReturn(SAVED_OBJECT_ID);
        when(objectStore.getId()).thenReturn(OBJECT_STORE_ID);

        final UploadService service = new UploadService(Function.identity(),
                new ObjectStoreStreamConsumer((x) -> objectStore));

        // when
        UUID orgGuid = UUID.randomUUID();
        service.upload(inputStream, builder, orgGuid);

        // then
        verify(objectStore).save(inputStream);
        verify(objectStore).getId();
        verify(builder).setObjectStoreId(OBJECT_STORE_ID);
        verify(builder).setSavedObjectId(SAVED_OBJECT_ID);

        verifyNoMoreInteractions(objectStore, builder);
    }

}
