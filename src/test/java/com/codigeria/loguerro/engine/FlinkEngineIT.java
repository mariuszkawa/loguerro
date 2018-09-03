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

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    static List<String> values = Collections.synchronizedList(new ArrayList<>());

    @Test
    void reads_data_from_the_input_file()
            throws URISyntaxException, InterruptedException, ExecutionException, TimeoutException
    {
        Class<?> clazz = MethodHandles.lookup().lookupClass();
        String engineName = String.format("IT Test: %s", clazz);
        String filePath = Paths.get(clazz.getResource("logfile").toURI())
                .toAbsolutePath()
                .toString();
        SinkFunction<String> sinkFunction = new TestSinkFunction();
        FlinkEngine.ConfigurationImpl configuration = new FlinkEngine.ConfigurationImpl(engineName, filePath);
        FlinkEngine engine = new FlinkEngine(
                configuration,
                StreamExecutionEnvironment.getExecutionEnvironment(),
                sinkFunction
        );
        Future<Boolean> finished = Executors.newSingleThreadExecutor().submit(() -> {
            engine.run();
            return true;
        });
        assertThat(finished.get(3, SECONDS)).isTrue();
        await().atMost(5, SECONDS).until(() -> values.size() == 6);
        assertThat(values).hasSize(6);
    }

    static class TestSinkFunction implements SinkFunction<String>
    {
        @Override
        public void invoke(String value, Context context)
        {
            values.add(value);
        }
    }
}
