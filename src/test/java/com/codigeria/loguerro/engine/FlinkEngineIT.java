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
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("it")
class FlinkEngineIT
{
    static List<Event> values = Collections.synchronizedList(new ArrayList<>());

    @Test
    void reads_data_from_the_input_file()
            throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
    {
        Class<?> clazz = MethodHandles.lookup().lookupClass();
        String engineName = String.format("IT Test: %s", clazz);
        String filePath = Paths.get(clazz.getResource("logfile").toURI())
                .toAbsolutePath()
                .toString();
        int expectedSize = 3;
        SinkFunction<Event> sinkFunction = new TestSinkFunction(new HsqlDbSinkFunction());
        FlinkEngine.ConfigurationImpl configuration = new FlinkEngine.ConfigurationImpl(engineName, filePath);
        FlinkEngine engine = new FlinkEngine(
                configuration,
                StreamExecutionEnvironment.getExecutionEnvironment(),
                new JsonDeserializer(),
                EventAction::getId,
                new EventComposer(),
                sinkFunction
        );
        Future<Boolean> finished = Executors.newSingleThreadExecutor().submit(() -> {
            engine.run();
            return true;
        });
        assertThat(finished.get(3, SECONDS)).isTrue();
        await().atMost(5, SECONDS).until(() -> values.size() == expectedSize);
        assertThat(values)
                .hasSize(expectedSize)
                .hasSameElementsAs(Arrays.asList(
                        Event.newBuilder()
                                .eventId("scsmbstgra")
                                .eventDuration(5L)
                                .alert(true)
                                .type("APPLICATION_LOG")
                                .host("12345")
                                .build(),
                        Event.newBuilder()
                                .eventId("scsmbstgrb")
                                .eventDuration(3L)
                                .alert(false)
                                .build(),
                        Event.newBuilder()
                                .eventId("scsmbstgrc")
                                .eventDuration(8L)
                                .alert(true)
                                .build()
                ));

    }

    static class TestSinkFunction extends RichSinkFunction<Event>
    {
        final HsqlDbSinkFunction hsqlDbSinkFunction;

        TestSinkFunction(HsqlDbSinkFunction hsqlDbSinkFunction)
        {
            this.hsqlDbSinkFunction = hsqlDbSinkFunction;
        }

        @Override
        public void open(Configuration parameters) throws Exception
        {
            super.open(parameters);
            hsqlDbSinkFunction.open(parameters);
        }

        @Override
        public void close() throws Exception
        {
            super.close();
            hsqlDbSinkFunction.close();
        }

        @Override
        public void invoke(Event event, Context context)
        {
            values.add(event);
            hsqlDbSinkFunction.invoke(event, context);
        }
    }
}
