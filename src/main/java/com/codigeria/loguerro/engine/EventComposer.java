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
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

final class EventComposer extends RichFlatMapFunction<EventAction, Event>
{
    private static final String STATE = "state";

    private static final String STARTED = "STARTED";
    private static final String FINISHED = "FINISHED";

    private transient MapState<String, EventAction> state;

    private transient Logger logger;

    @Override
    public void open(Configuration parameters) throws Exception
    {
        super.open(parameters);
        logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
        if (state == null) {
            MapStateDescriptor<String, EventAction> descriptor = new MapStateDescriptor<>(
                    STATE,
                    String.class,
                    EventAction.class
            );
            state = getRuntimeContext().getMapState(descriptor);
            logger.debug("A new map state named '{}' initialized", STATE);
        } else {
            logger.debug("A map state provided externally");
        }
    }

    @Override
    public void flatMap(EventAction eventAction, Collector<Event> collector) throws Exception
    {
        if (!STARTED.equals(eventAction.getState()) && !FINISHED.equals(eventAction.getState())) {
            logger.debug("Invalid incoming state of event action - the state was '{}' - skipping the event",
                    eventAction.getState());
            return;
        }
        logger.debug("Event action came in with the state '{}', id '{}'",
                eventAction.getState(), eventAction.getId());
        if (!state.contains(eventAction.getState())) {
            handleIncomingNewEventAction(eventAction, collector);
        } else {
            logger.debug("Event action with the state '{}', id '{}' rejected - already included in the map state",
                    eventAction.getState(), eventAction.getId());
        }
    }

    private void handleIncomingNewEventAction(EventAction eventAction, Collector<Event> collector) throws Exception
    {
        state.put(eventAction.getState(), eventAction);
        if (state.contains(STARTED) && state.contains(FINISHED)) {
            EventAction started = state.get(STARTED);
            EventAction finished = state.get(FINISHED);
            long eventDuration = finished.getTimestamp() - started.getTimestamp();
            boolean eventDurationLongerThan_4ms = eventDuration > 4L;
            Event.Builder eventBuilder = Event.newBuilder()
                    .eventId(started.getId())
                    .eventDuration(eventDuration)
                    .alert(eventDurationLongerThan_4ms);
            if (StringUtils.isNotEmpty(started.getHost())) {
                eventBuilder.host(started.getHost());
            }
            if (StringUtils.isNotEmpty(started.getType())) {
                eventBuilder.type(started.getType());
            }
            Event event = eventBuilder.build();
            if (logger.isDebugEnabled()) {
                logger.debug("Collecting a new event: {}", event);
            }
            collector.collect(event);
        }
    }

    @VisibleForTesting
    void setState(MapState<String, EventAction> state)
    {
        this.state = state;
    }
}
