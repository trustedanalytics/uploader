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
package org.trustedanalytics.uploader.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

import lombok.Data;

@Data
public class Transfer {
    @JsonProperty("source")
    private String source;
    @JsonProperty("orgUUID")
    private String orgUUID;
    @JsonProperty("title")
    private String title;
    @JsonProperty("category")
    private String category;
    @JsonProperty("publicAccess")
    private boolean publicAccess;
    // this field is fill during write to HDFS operation
    @JsonProperty("objectStoreId")
    private String objectStoreId;
    // this field is fill during write to HDFS operation
    @JsonProperty("idInObjectStore")
    private String savedObjectId;

    public Transfer(UploadMetadata metadata) {
        this.source = Objects.requireNonNull(metadata.getSource(), "source is required");
        this.orgUUID = Objects.requireNonNull(metadata.getOrgUUID(), "orgUUID is required");
        this.title = Objects.requireNonNull(metadata.getTitle(), "title is required");
        this.category = Objects.requireNonNull(metadata.getCategory(), "category is required");
        this.publicAccess = Objects.requireNonNull(metadata.getPublicAccess(), "publicAccess is required");
    }

    public Transfer(Transfer transfer) {
        this.source = Objects.requireNonNull(transfer.getSource(), "source is required");
        this.orgUUID = Objects.requireNonNull(transfer.getOrgUUID(), "orgUUID is required");
        this.title = Objects.requireNonNull(transfer.getTitle(), "title is required");
        this.category = Objects.requireNonNull(transfer.getCategory(), "category is required");
        this.publicAccess = Objects.requireNonNull(transfer.isPublicAccess(), "publicAccess is required");
        this.objectStoreId = Objects.requireNonNull(transfer.getObjectStoreId(), "objectStoreId is required");
        this.savedObjectId = Objects.requireNonNull(transfer.getSavedObjectId(), "savedObjectId is required");
    }

    public Transfer setTitle(String title) {
        this.title = title;
        return this;
    }
}
