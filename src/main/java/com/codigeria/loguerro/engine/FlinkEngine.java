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
import com.google.common.annotations.VisibleForTesting;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public final class FlinkEngine implements Engine
{
    private final Configuration configuration;

    private final StreamExecutionEnvironment environment;
    private final MapFunction<String, EventAction> jsonDeserializer;
    private final KeySelector<EventAction, String> keySelector;
    private final FlatMapFunction<EventAction, Event> eventComposer;
    private final SinkFunction<Event> sinkFunction;

    private final Logger logger;

    public FlinkEngine(Configuration configuration)
    {
        this(
                configuration,
                StreamExecutionEnvironment.getExecutionEnvironment(),
                new JsonDeserializer(),
                EventAction::getId,
                new EventComposer(),
                new HsqlDbSinkFunction()
        );
    }

    @VisibleForTesting
    FlinkEngine(Configuration configuration,
                StreamExecutionEnvironment executionEnvironment,
                MapFunction<String, EventAction> jsonDeserializer,
                KeySelector<EventAction, String> keySelector,
                FlatMapFunction<EventAction, Event> eventComposer,
                SinkFunction<Event> sinkFunction)
    {
        this(
                configuration,
                executionEnvironment,
                jsonDeserializer,
                keySelector,
                eventComposer,
                sinkFunction,
                LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
        );
    }

    @VisibleForTesting
    FlinkEngine(Configuration configuration,
                StreamExecutionEnvironment environment,
                MapFunction<String, EventAction> jsonDeserializer,
                KeySelector<EventAction, String> keySelector,
                FlatMapFunction<EventAction, Event> eventComposer,
                SinkFunction<Event> sinkFunction,
                Logger logger)
    {
        this.configuration = checkNotNull(configuration);
        this.environment = checkNotNull(environment);
        this.jsonDeserializer = checkNotNull(jsonDeserializer);
        this.keySelector = checkNotNull(keySelector);
        this.eventComposer = checkNotNull(eventComposer);
        this.sinkFunction = checkNotNull(sinkFunction);
        this.logger = checkNotNull(logger);
    }

    @Override
    public void run() throws EngineException
    {
        environment.readTextFile(configuration.getFilePath())
                .map(jsonDeserializer)
                .keyBy(keySelector)
                .flatMap(eventComposer)
                .addSink(sinkFunction);
        logger.info("Starting Flink execution environment for the engine named '{}', reading from file '{}'...",
                configuration.getEngineName(), configuration.getFilePath());
        try {
            environment.execute(configuration.getEngineName());
        } catch (Exception e) {
            String message = String.format("An exception caught while running Flink execution environment " +
                    "for engine named %s", configuration.getEngineName());
            logger.error(message, e);
            throw new EngineException(message, e);
        }
    }

    public interface Configuration
    {
        String getEngineName();

        String getFilePath();
    }

    public static final class ConfigurationImpl implements Configuration
    {
        private final String engineName;
        private final String filePath;

        public ConfigurationImpl(String engineName, String filePath)
        {
            checkArgument(isNotEmpty(engineName));
            checkArgument(isNotEmpty(filePath));
            this.engineName = engineName;
            this.filePath = filePath;
        }

        @Override
        public String getEngineName()
        {
            return engineName;
        }

        @Override
        public String getFilePath()
        {
            return filePath;
        }
    }
}
