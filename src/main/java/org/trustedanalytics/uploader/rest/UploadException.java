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


import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class UploadException extends RuntimeException {
    private final List<Transfer> files;

    public UploadException(Collection<Transfer> files, Throwable cause) {
        super(Objects.requireNonNull(cause, "cause"));

        this.files = ImmutableList.copyOf(files);
    }

    @Override
    public String getMessage() {
        return Optional.ofNullable(getCause())
                .map(Throwable::getMessage)
                .map(msg -> StringUtils.stripEnd(msg, ".") + ".")
                .map(msg -> msg + " Following files has been successfully uploaded: ")
                .map(msg -> msg + files.stream().map(Transfer::getSource).collect(Collectors.joining(", ")) + ".")
                .orElse(getCause().getMessage());
    }
}
