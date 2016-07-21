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

import com.google.common.base.Throwables;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.Function;

@RunWith(Parameterized.class)
public class DecoderTest {

    @Parameterized.Parameters(name = "{index} {0}({1})")
    public static Iterable<Object[]> data() {
        // @formatter:off
        return Arrays.asList(
            new Object[][] {
                {new ZipStreamDecoder(), "upload_html/test.zip", "test"},
                {new GzipStreamDecoder(), "upload_html/test.gz", "test"},
                {new ZipStreamDecoder().compose(new GzipStreamDecoder()), "upload_html/test.zip", "test"},
                {new ZipStreamDecoder().compose(new GzipStreamDecoder()), "upload_html/test.gz", "test"}
            });
        // @formatter:on
    }

    @Parameterized.Parameter(0)
    public Function<InputStream, InputStream> decoder;

    @Parameterized.Parameter(1)
    public String archive;

    @Parameterized.Parameter(2)
    public String payload;

    @Test
    public void testDecoder() {
        try(InputStream in = decoder.apply(getClass().getClassLoader().getResourceAsStream(archive))) {
            Assert.assertEquals(payload, IOUtils.toString(in, "UTF-8").trim());
        } catch (IOException ex) {
            Throwables.propagate(ex);
        }
    }

}
