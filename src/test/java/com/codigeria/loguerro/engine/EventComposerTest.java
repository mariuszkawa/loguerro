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
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Tag("unit")
class EventComposerTest
{
    @Mock
    Configuration configuration;
    @Mock
    MapState<String, EventAction> state;
    @Mock
    Collector<Event> collector;

    EventComposer eventComposer;

    @BeforeEach
    void setup() throws Exception
    {
        eventComposer = new EventComposer();
        eventComposer.setState(state);
        eventComposer.open(configuration);
    }

    @Test
    void flatMap___given_an_event_action_with_invalid_state___when_flat_map___then_does_nothing() throws Exception
    {
        // Given
        EventAction eventAction = new EventAction("test_id", "INVALID_STATE", 1536088354300L);

        // When
        eventComposer.flatMap(eventAction, collector);

        // Then
        verifyZeroInteractions(state, collector);
    }

    @Test
    void flatMap___given_started_event_action_already_in_state___when_flat_map___then_does_not_add_it_again()
            throws Exception
    {
        // Given
        EventAction eventAction = new EventAction("test_id", "STARTED", 1536088354300L);
        when(state.contains(eq("STARTED"))).thenReturn(true);

        // When
        eventComposer.flatMap(eventAction, collector);

        // Then
        verify(state, only()).contains(eq("STARTED"));
        verifyNoMoreInteractions(state);
        verifyZeroInteractions(collector);
    }

    @Test
    void flatMap___given_started_event_action_not_yet_in_state___when_flat_map___then_adds_it() throws Exception
    {
        // Given
        EventAction eventAction = new EventAction("test_id", "STARTED", 1536088354300L);

        // When
        eventComposer.flatMap(eventAction, collector);

        // Then
        verify(state, times(2)).contains(eq("STARTED"));
        verify(state, times(1)).put(eq("STARTED"), eq(eventAction));
        verifyNoMoreInteractions(state);
        verifyZeroInteractions(collector);
    }

    @Test
    void flatMap___given_both_event_actions_in_state___when_flat_map___then_collects_the_result() throws Exception
    {
        // Given
        AtomicBoolean answer = new AtomicBoolean(false);
        EventAction startedEventAction = new EventAction("test_id", "STARTED", 1536088354300L,
                "some_type", "some_host");
        EventAction finishedEventAction = new EventAction("test_id", "FINISHED", 1536088426190L,
                "some_type", "some_host");
        doReturn(true).when(state).contains(eq("STARTED"));
        doAnswer(invocation -> answer.get()).when(state).contains(eq("FINISHED"));
        doAnswer(invocation -> {
            answer.set(true);
            return null;
        }).when(state).put(eq("FINISHED"), any(EventAction.class));
        doReturn(startedEventAction).when(state).get(eq("STARTED"));
        doReturn(finishedEventAction).when(state).get(eq("FINISHED"));
        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);

        // When
        eventComposer.flatMap(finishedEventAction, collector);

        // Then
        verify(state, times(1)).contains(eq("STARTED"));
        verify(state, times(2)).contains(eq("FINISHED"));
        verify(state, times(1)).put(eq("FINISHED"), eq(finishedEventAction));
        verify(collector, times(1)).collect(captor.capture());
        verifyNoMoreInteractions(state, collector);
        Assertions.assertThat(captor.getValue())
                .isNotNull()
                .isEqualToComparingFieldByField(Event.newBuilder()
                        .eventId("test_id")
                        .eventDuration(71890L)
                        .alert(true)
                        .type("some_type")
                        .host("some_host")
                        .build());
    }
}