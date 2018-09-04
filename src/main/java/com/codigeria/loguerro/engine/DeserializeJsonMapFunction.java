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

import com.codigeria.loguerro.model.EventAction;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

final class DeserializeJsonMapFunction extends RichMapFunction<String, EventAction>
{
    private transient Gson gson;

    private transient Logger logger;

    @Override
    public void open(Configuration parameters) throws Exception
    {
        super.open(parameters);
        if (gson == null) {
            gson = new Gson();
        }
        if (logger == null) {
            logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
        }
    }

    @Override
    public EventAction map(String value)
    {
        logger.debug("Deserializing JSON object: {}", value);
        return gson.fromJson(value, EventAction.class);
    }

    @VisibleForTesting
    void setGson(Gson gson)
    {
        this.gson = gson;
    }

    @VisibleForTesting
    void setLogger(Logger logger)
    {
        this.logger = logger;
    }
}
