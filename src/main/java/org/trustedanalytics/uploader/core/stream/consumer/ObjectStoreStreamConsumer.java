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

import com.google.common.base.Throwables;
import org.trustedanalytics.store.ObjectStore;
import org.trustedanalytics.uploader.rest.UploadCompleted.UploadCompletedBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public class ObjectStoreStreamConsumer
    implements TriConsumer<InputStream, UploadCompletedBuilder, UUID> {

    private final Function<UUID, ObjectStore> objectStoreFactory;

    public ObjectStoreStreamConsumer(Function<UUID, ObjectStore> objectStoreFactory) {
        this.objectStoreFactory = Objects.requireNonNull(objectStoreFactory);
    }

    @Override
    public void accept(InputStream inputStream, UploadCompletedBuilder builder, UUID orgUUID) {
        ObjectStore objectStore = objectStoreFactory.apply(orgUUID);
        try {
            builder.setObjectStoreId(objectStore.getId());
            builder.setSavedObjectId(objectStore.save(inputStream));
        } catch (IOException ex) {
            Throwables.propagate(ex);
        }
    }
}
