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

package com.codigeria.loguerro.engine;

import com.codigeria.loguerro.model.Event;
import com.codigeria.loguerro.model.EventAction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class FlinkEngineTest
{
    @Mock
    FlinkEngine.Configuration configuration;
    @Mock
    StreamExecutionEnvironment environment;
    @Mock
    MapFunction<String, EventAction> jsonDeserializer;
    @Mock
    KeySelector<EventAction, String> keySelector;
    @Mock
    FlatMapFunction<EventAction, Event> eventComposer;
    @Mock
    SinkFunction<Event> sinkFunction;
    @Mock
    DataStreamSource<String> dataSourceStream;
    @Mock
    SingleOutputStreamOperator<EventAction> singleOutputStreamOpertator;
    @Mock
    SingleOutputStreamOperator<Event> singleOutputStreamOpertatorEvent;
    @Mock
    KeyedStream<Object, Tuple> keyedStream;
    @Mock
    Logger logger;

    @InjectMocks
    FlinkEngine flinkEngine;

    @Test
    void run___given_properly_configured__when_run_the_engine___then_behaves_properly() throws Exception
    {
        // Given
        String engineName = String.format("Test Engine for Unit Test: %s",
                MethodHandles.lookup().lookupClass());
        String filePath = "test.log";
        setupMocks(engineName, filePath);

        // When
        assertThatCode(
                () -> flinkEngine.run()
        ).doesNotThrowAnyException();

        // Then
        verify(environment, times(1)).readTextFile(eq(filePath));
        verify(singleOutputStreamOpertatorEvent, times(1)).addSink(eq(sinkFunction));
        verify(environment, times(1)).execute(eq(engineName));
        verify(logger, times(1)).info(
                contains("Starting Flink execution environment"),
                contains(engineName),
                contains(filePath)
        );
    }

    @Test
    void run___given_improperly_configured__when_the_engine_raises_an_exception___then_wraps_and_rethrows_it()
            throws Exception
    {
        // Given
        String engineName = String.format("Broken Test Engine for Unit Test: %s",
                MethodHandles.lookup().lookupClass());
        String filePath = "dummy-logfile.log";
        setupMocks(engineName, filePath);
        when(environment.execute(anyString())).thenThrow(new IllegalStateException("Bad State!"));

        // When
        Throwable thrown = catchThrowable(() -> flinkEngine.run());

        // Then
        assertThat(thrown)
                .isInstanceOf(EngineException.class)
                .hasMessageContaining("An exception caught while running Flink execution environment")
                .hasMessageContaining(engineName)
                .hasCauseInstanceOf(IllegalStateException.class);
        verify(logger, times(1)).error(
                contains("An exception caught while running Flink execution environment"),
                any(IllegalStateException.class)
        );
    }

    void setupMocks(String engineName, String filePath)
    {
        doReturn(engineName).when(configuration).getEngineName();
        doReturn(filePath).when(configuration).getFilePath();
        doReturn(dataSourceStream).when(environment).readTextFile(eq(filePath));
        doReturn(singleOutputStreamOpertator).when(dataSourceStream).map(any());
        doReturn(keyedStream).when(singleOutputStreamOpertator).keyBy(any(KeySelector.class));
        doReturn(singleOutputStreamOpertatorEvent).when(keyedStream).flatMap(any());
    }
}