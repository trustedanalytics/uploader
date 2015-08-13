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
package org.trustedanalytics.uploader.core.stream.consumer;

import org.trustedanalytics.store.ObjectStore;
import org.trustedanalytics.uploader.rest.UploadCompleted.UploadCompletedBuilder;

import com.google.common.base.Throwables;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.function.BiConsumer;

@Component
public class ObjectStoreStreamConsumer implements BiConsumer<InputStream, UploadCompletedBuilder> {
    private final ObjectStore objectStore;

    @Autowired
    public ObjectStoreStreamConsumer(ObjectStore objectStore) {
        this.objectStore = Objects.requireNonNull(objectStore);
    }

    @Override
    public void accept(InputStream inputStream, UploadCompletedBuilder builder) {
        try {
            builder.setObjectStoreId(objectStore.getId());
            builder.setSavedObjectId(objectStore.save(inputStream));
        } catch (IOException ex) {
            Throwables.propagate(ex);
        }
    }
}
