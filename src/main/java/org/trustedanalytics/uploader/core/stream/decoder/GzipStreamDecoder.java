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

import org.apache.tika.detect.MagicDetector;
import org.apache.tika.mime.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class GzipStreamDecoder extends StreamDecoder {
    private final static byte[] GZIP_MAGIC = {(byte) 0x1f, (byte) 0x08b};

    public GzipStreamDecoder() {
        super(new MagicDetector(MediaType.application("gzip"), GZIP_MAGIC), "application/gzip");
    }

    @Override
    protected InputStream decode(InputStream in) throws IOException {
        return new GZIPInputStream(in);
    }
}
