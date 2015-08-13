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
package org.trustedanalytics.uploader.client;

import feign.slf4j.Slf4jLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ScramblingSlf4jLogger extends Slf4jLogger {

    public static final String SCRAMBLE_STRING = "[PRIVATE DATA HIDDEN]";
    private final List<Consumer<Object[]>> scramblers = new ArrayList<>();

    public ScramblingSlf4jLogger(Class<?> clazz) {
        super(clazz);

        scramblers.add(authorizationScrambler());
    }

    @Override
    protected void log(String configKey, String format, Object... args) {
        scramblers.forEach(scrambler -> scrambler.accept(args));
        super.log(configKey, format, args);
    }

    private Consumer<Object[]> authorizationScrambler() {
        return args -> {
            if (isHeader(args) && ("Authorization".equalsIgnoreCase(args[0].toString()))) {
                args[1] = SCRAMBLE_STRING;
            }
        };
    }

    private boolean isHeader(Object[] args) {
        return (args != null) && (args.length == 2);
    }
}
