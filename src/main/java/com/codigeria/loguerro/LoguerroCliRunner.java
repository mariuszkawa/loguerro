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

    @VisibleForTesting
    static final int STATUS_ERROR_INVALID_USAGE = 254;
    @VisibleForTesting
    static final int STATUS_ERROR_ENGINE_FAILURE = 255;

    private final List<String> arguments;
    private final ConsolePrinter consolePrinter;
    private final ApplicationKiller applicationKiller;

    private final Logger logger;

    private Engine engine;

    LoguerroCliRunner(List<String> arguments)
    {
        this(
                arguments,
                new DefaultConsolePrinter(),
                System::exit,
                LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
        );
    }

    @VisibleForTesting
    LoguerroCliRunner(List<String> arguments,
                      ConsolePrinter consolePrinter,
                      ApplicationKiller applicationKiller,
                      Logger logger)
    {
        this.arguments = Collections.unmodifiableList(checkNotNull(arguments));
        this.consolePrinter = checkNotNull(consolePrinter);
        this.applicationKiller = checkNotNull(applicationKiller);
        this.logger = checkNotNull(logger);
    }

    void run()
    {
        logger.debug("Running CLI runner...");
        if (arguments.size() == 1) {
            interpretSingleArgument(arguments.get(0));
        } else {
            reportInvalidNumberOfParameters();
        }
    }

    private void interpretSingleArgument(String argument)
    {
        if ("--help".equals(argument) || "-h".equals(argument)) {
            printUsage();
        } else {
            runEngine(argument);
        }
    }

    private void runEngine(String filePath)
    {
        try {
            createEngine(filePath);
            consolePrinter.log("The result will be written to a file called 'event.db' " +
                    "in the current working directory. See 'loguerro.log' for application logs.");
            consolePrinter.log(String.format("Reading the file from '%s'...", filePath));
            engine.run();
        } catch (EngineException e) {
            logger.error("An exception caught while running the Loguerro engine", e);
            consolePrinter.error("An exception caught while running the Loguerro engine - see 'loguerro.log'.");
            applicationKiller.kill(STATUS_ERROR_ENGINE_FAILURE);
        }
    }

    private void reportInvalidNumberOfParameters()
    {
        consolePrinter.error("Invalid number of parameters.");
        printUsage();
        applicationKiller.kill(STATUS_ERROR_INVALID_USAGE);
    }

    private void printUsage()
    {
        consolePrinter.error("Usage: loguerro <input_file_path>\n");
        consolePrinter.error("    where:\n");
        consolePrinter.error("    <input_file_path> - a path pointing to a JSON file containing logged events");
    }

    private void createEngine(String argument)
    {
        if (engine == null) {
            engine = new FlinkEngine(new FlinkEngine.ConfigurationImpl(DEFAULT_ENGINE_NAME, argument));
        }
    }

    @VisibleForTesting
    void setEngine(Engine engine)
    {
        this.engine = engine;
    }

    @VisibleForTesting
    interface ConsolePrinter
    {
        void log(String message);

        void error(String message);
    }

    @VisibleForTesting
    static final class DefaultConsolePrinter implements ConsolePrinter
    {
        @Override
        public void log(String message)
        {
            System.out.println(message);
        }

        @Override
        public void error(String message)
        {
            System.err.println(message);
        }
    }

    @VisibleForTesting
    @FunctionalInterface
    interface ApplicationKiller
    {
        void kill(int statusCode);

    }
}
