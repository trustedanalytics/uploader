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
package org.trustedanalytics.uploader.core.stream.decoder;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableSet;

import org.apache.tika.Tika;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.function.Function;

public abstract class StreamDecoder implements Function<InputStream, InputStream> {
    private final Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
    private final Set<String> supportedMediaTypes;
    private final Detector detector;

    public StreamDecoder(String... supportedMediaTypes) {
        this(new Tika().getDetector(), supportedMediaTypes);
    }

    public StreamDecoder(Detector detector, String... supportedMediaTypes) {
        checkNotNull(detector);
        checkArgument((supportedMediaTypes != null) && (supportedMediaTypes.length > 0));

        this.detector = detector;
        this.supportedMediaTypes = ImmutableSet.copyOf(supportedMediaTypes);
    }

    @Override
    public final InputStream apply(InputStream in) {
        try {
            final BufferedInputStream bis = new BufferedInputStream(in);
            return isMediaTypeSupported(bis) ? decode(bis) : bis;
        } catch (IOException e) {
            throw new IllegalStateException("Decode exception", e);
        }
    }

    private boolean isMediaTypeSupported(InputStream in) throws IOException {
        final MediaType mediaType = detector.detect(in, new Metadata());
        logger.info("MediaType: {}", mediaType);

        return supportedMediaTypes.contains(mediaType.toString());
    }

    protected abstract InputStream decode(InputStream in) throws IOException;

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
