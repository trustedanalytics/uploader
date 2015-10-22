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
import com.google.common.base.MoreObjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class UploadCompleted {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadCompleted.class);

    @JsonProperty("source")
    private final String source;
    @JsonProperty("orgUUID")
    private final String orgUUID;
    @JsonProperty("title")
    private final String title;
    @JsonProperty("category")
    private final String category;
    @JsonProperty("publicRequest")
    private final boolean publicAccess;
    @JsonProperty("objectStoreId")
    private final String objectStoreId;
    @JsonProperty("idInObjectStore")
    private final String savedObjectId;

    private UploadCompleted(UploadCompletedBuilder uploadCompletedBuilder) {
        this.source = Objects.requireNonNull(uploadCompletedBuilder.source);
        this.orgUUID = Objects.requireNonNull(uploadCompletedBuilder.orgUUID);
        this.title = Objects.requireNonNull(uploadCompletedBuilder.title);
        this.category = Objects.requireNonNull(uploadCompletedBuilder.category);
        this.objectStoreId = Objects.requireNonNull(uploadCompletedBuilder.objectStoreId);
        this.savedObjectId = Objects.requireNonNull(uploadCompletedBuilder.savedObjectId);
        this.publicAccess = uploadCompletedBuilder.publicAccess;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("source", source)
            .add("orgUUID", orgUUID)
            .add("title", title)
            .add("category", category)
            .add("publicAccess", publicAccess)
            .add("objectStoreId", objectStoreId)
            .add("savedObjectId", savedObjectId)
            .toString();
    }

    public static UploadCompletedBuilder builder() {
        return new UploadCompletedBuilder();
    }

    public static class UploadCompletedBuilder {
        private String source;
        private String orgUUID;
        private String title;
        private String category;
        private boolean publicAccess;
        private String objectStoreId;
        private String savedObjectId;

        public UploadCompletedBuilder setSource(String name) {
            this.source = name;
            return this;
        }

        public UploadCompletedBuilder setOrgUUID(String orgUUID) {
            this.orgUUID = orgUUID;
            return this;
        }

        public UploadCompletedBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public UploadCompletedBuilder setCategory(String category) {
            this.category = category;
            return this;
        }

        public UploadCompletedBuilder setPublicAccess(boolean publicAccess) {
            this.publicAccess = publicAccess;
            return this;
        }

        public UploadCompletedBuilder setObjectStoreId(String objectStoreId) {
            this.objectStoreId = objectStoreId;
            return this;
        }

        public UploadCompletedBuilder setSavedObjectId(String savedObjectId) {
            this.savedObjectId = savedObjectId;
            return this;
        }

        public UploadCompletedBuilder setProperty(String key, String value) {
            switch (key.toLowerCase()) {
                case "title":
                    return setTitle(value);
                case "category":
                    return setCategory(value);
                case "publicrequest":
                    return setPublicAccess(Boolean.valueOf(value));
                case "orguuid":
                    return setOrgUUID(value);
                default:
                    LOGGER.warn("{} : {} is not known", key, value);
                    return this;
            }
        }

        public UploadCompleted build() {
            return new UploadCompleted(this);
        }
    }
}
