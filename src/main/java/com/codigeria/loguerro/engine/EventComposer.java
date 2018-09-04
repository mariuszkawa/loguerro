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
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

final class EventComposer extends RichFlatMapFunction<EventAction, Event>
{
    private static final String STATE = "state";

    private transient MapState<String, EventAction> state;

    @Override
    public void open(final Configuration parameters) throws Exception
    {
        super.open(parameters);
        if (state == null) {
            MapStateDescriptor<String, EventAction> descriptor = new MapStateDescriptor<>(
                    STATE,
                    String.class,
                    EventAction.class
            );
            state = getRuntimeContext().getMapState(descriptor);
        }
    }

    @Override
    public void flatMap(EventAction eventAction, Collector<Event> collector) throws Exception
    {
        if (!state.contains(eventAction.getState())) {
            state.put(eventAction.getState(), eventAction);
        }
        if (state.contains("STARTED") && state.contains("FINISHED")) {
            EventAction started = state.get("STARTED");
            EventAction finished = state.get("FINISHED");
            collector.collect(new Event(started.getId(), finished.getTimestamp() - started.getTimestamp()));
        }
    }

    @VisibleForTesting
    void setState(final MapState<String, EventAction> state)
    {
        this.state = state;
    }
}
