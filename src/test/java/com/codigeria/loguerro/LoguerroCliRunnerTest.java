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

import com.codigeria.loguerro.LoguerroCliRunner.ApplicationKiller;
import com.codigeria.loguerro.engine.Engine;
import com.codigeria.loguerro.engine.EngineException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.util.List;

import static com.codigeria.loguerro.LoguerroCliRunner.ConsolePrinter;
import static com.codigeria.loguerro.LoguerroCliRunner.STATUS_ERROR_ENGINE_FAILURE;
import static com.codigeria.loguerro.LoguerroCliRunner.STATUS_ERROR_INVALID_USAGE;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class LoguerroCliRunnerTest
{
    @Mock
    List<String> arguments;
    @Mock
    Engine engine;
    @Mock
    ConsolePrinter consolePrinter;
    @Mock
    ApplicationKiller applicationKiller;
    @Mock
    Logger logger;

    @InjectMocks
    LoguerroCliRunner loguerroCliRunner;

    @BeforeEach
    void setup()
    {
        loguerroCliRunner.setEngine(engine);
    }

    @Test
    void run___given_empty_arg_list___when_run_CLI_runner___then_report_invalid_number_of_args()
    {
        // Given
        // (empty list of arguments)

        // When
        loguerroCliRunner.run();

        // Then
        verify(logger, times(1)).debug(eq("Running CLI runner..."));
        verify(consolePrinter).error(eq("Invalid number of parameters."));
        verifyThatUsageWasPrinted();
        verify(applicationKiller).kill(eq(STATUS_ERROR_INVALID_USAGE));
        verifyNoMoreInteractions(logger, engine, consolePrinter, applicationKiller);
    }

    @Test
    void run___given_three_args_provided___when_run_CLI_runner___then_report_invalid_number_of_args()
    {
        // Given
        doReturn(3).when(arguments).size();

        // When
        loguerroCliRunner.run();

        // Then
        verify(logger, times(1)).debug(eq("Running CLI runner..."));
        verify(consolePrinter).error(eq("Invalid number of parameters."));
        verifyThatUsageWasPrinted();
        verify(applicationKiller).kill(eq(STATUS_ERROR_INVALID_USAGE));
        verifyNoMoreInteractions(logger, engine, consolePrinter, applicationKiller);
    }

    @Test
    void run___given_help_short_arg_provided___when_run_CLI_runner___then_show_usage()
    {
        // Given
        doReturn(1).when(arguments).size();
        doReturn("-h").when(arguments).get(eq(0));

        // When
        loguerroCliRunner.run();

        // Then
        verify(logger, times(1)).debug(eq("Running CLI runner..."));
        verifyThatUsageWasPrinted();
        verifyNoMoreInteractions(logger, engine, consolePrinter, applicationKiller);
    }

    @Test
    void run___given_file_path_provided___when_run_CLI_runner___then_run_the_engine() throws EngineException
    {
        // Given
        doReturn(1).when(arguments).size();
        doReturn("log_file.log").when(arguments).get(eq(0));

        // When
        loguerroCliRunner.run();

        // Then
        verify(logger, times(1)).debug(eq("Running CLI runner..."));
        verify(engine, times(1)).run();
        verifyNoMoreInteractions(logger, engine, consolePrinter, applicationKiller);
    }

    @Test
    void run___given_the_engine_throws_an_exception___when_run_CLI_runner___then_an_exception_is_raised()
            throws EngineException
    {
        // Given
        doReturn(1).when(arguments).size();
        doReturn("log_file.log").when(arguments).get(eq(0));
        EngineException exception = new EngineException("Illegal state!", new IllegalStateException());
        doThrow(exception).when(engine).run();

        // When
        loguerroCliRunner.run();

        // Then
        verify(logger, times(1)).debug(eq("Running CLI runner..."));
        verify(engine, times(1)).run();
        verify(logger, times(1)).error(
                eq("An exception caught while running the Loguerro engine"),
                eq(exception)
        );
        verify(applicationKiller).kill(eq(STATUS_ERROR_ENGINE_FAILURE));
        verifyNoMoreInteractions(logger, engine, consolePrinter, applicationKiller);
    }

    private void verifyThatUsageWasPrinted()
    {
        verify(consolePrinter).error(eq("Usage: loguerro <input_file_path>\n"));
        verify(consolePrinter).error(eq("    where:\n"));
        verify(consolePrinter).error(eq("    <input_file_path> - a path pointing to a JSON file " +
                "containing logged events"));
    }
}