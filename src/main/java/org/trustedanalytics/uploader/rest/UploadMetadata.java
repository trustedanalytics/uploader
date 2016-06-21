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
package org.trustedanalytics.uploader.rest;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class UploadMetadata {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadMetadata.class);

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

    private UploadMetadata(UploadMetadataBuilder builder) {
        this.source = Objects.requireNonNull(builder.source, "source is required");
        this.orgUUID = Objects.requireNonNull(builder.orgUUID, "organization guid is required");
        this.title = Objects.requireNonNull(builder.title, "title is required");
        this.category = Objects.requireNonNull(builder.category, "category is required");
        this.publicAccess = builder.publicAccess;
    }

    public String getSource() {
        return source;
    }

    public String getOrgUUID() {
        return orgUUID;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public boolean getPublicAccess() {
        return publicAccess;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("source", source)
                .add("orgUUID", orgUUID)
                .add("title", title)
                .add("category", category)
                .add("publicAccess", publicAccess)
                .toString();
    }

    public static UploadMetadataBuilder builder() {
        return new UploadMetadataBuilder();
    }

    public static class UploadMetadataBuilder {

        private String source;
        private String orgUUID;
        private String title;
        private String category = "other";
        private boolean publicAccess = false;

        public UploadMetadataBuilder setSource(String name) {
            this.source = name;
            return this;
        }

        public UploadMetadataBuilder setOrgUUID(String orgUUID) {
            this.orgUUID = orgUUID;
            return this;
        }

        public UploadMetadataBuilder setTitle(String title) {
            this.title = title;
            return this;
        }

        public UploadMetadataBuilder setCategory(String category) {
            this.category = category;
            return this;
        }

        public UploadMetadataBuilder setPublicAccess(boolean publicAccess) {
            this.publicAccess = publicAccess;
            return this;
        }

        public UploadMetadataBuilder setProperty(String key, String value) {
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

        public UploadMetadata build() {
            return new UploadMetadata(this);
        }
    }
}
