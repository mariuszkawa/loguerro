/*
 * MIT License
 *
 * Copyright (c) 2018 Mariusz Kawa
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.codigeria.loguerro;

import com.codigeria.loguerro.engine.Engine;
import com.codigeria.loguerro.engine.EngineException;
import com.codigeria.loguerro.engine.FlinkEngine;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

final class LoguerroCliRunner
{
    private static final String DEFAULT_ENGINE_NAME = "Loguerro Streaming Engine";
    private static final String DEFAULT_FILE_PATH = "logfile.log";

    private final List<String> arguments;

    private final Engine engine;

    private final Logger logger;

    LoguerroCliRunner(List<String> arguments)
    {
        this(
                arguments,
                new FlinkEngine(new FlinkEngine.ConfigurationImpl(DEFAULT_ENGINE_NAME, DEFAULT_FILE_PATH)),
                LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
        );
    }

    @VisibleForTesting
    LoguerroCliRunner(List<String> arguments, Engine engine, Logger logger)
    {
        this.arguments = Collections.unmodifiableList(checkNotNull(arguments));
         this.engine = checkNotNull(engine);
        this.logger = checkNotNull(logger);
    }

    void run()
    {
        logger.debug("Running CLI runner...");
        try {
            engine.run();
        } catch (EngineException e) {
            logger.error("An exception caught while running the Loguerro engine", e);
        }
    }
}
