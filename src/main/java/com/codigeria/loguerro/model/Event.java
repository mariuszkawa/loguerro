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

package com.codigeria.loguerro.model;

import com.google.common.base.MoreObjects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Entity
@Table(name = "event")
public class Event
{
    public static Builder newBuilder()
    {
        return new Builder();
    }

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "event_duration")
    private long eventDuration;

    @Column(name = "type")
    private String type;

    @Column(name = "host")
    private String host;

    @Column(name = "alert")
    private boolean alert;

    Event()
    {
    }

    private Event(String eventId,
                  long eventDuration,
                  String type,
                  String host,
                  boolean alert)
    {
        this.eventId = eventId;
        this.eventDuration = eventDuration;
        this.type = type;
        this.host = host;
        this.alert = alert;
    }

    public String getEventId()
    {
        return eventId;
    }

    public long getEventDuration()
    {
        return eventDuration;
    }

    public String getType()
    {
        return type;
    }

    public String getHost()
    {
        return host;
    }

    public boolean isAlert()
    {
        return alert;
    }

    void setEventId(String eventId)
    {
        this.eventId = eventId;
    }

    void setEventDuration(long eventDuration)
    {
        this.eventDuration = eventDuration;
    }

    void setType(String type)
    {
        this.type = type;
    }

    void setHost(String host)
    {
        this.host = host;
    }

    void setAlert(boolean alert)
    {
        this.alert = alert;
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("eventId", eventId)
                .add("eventDuration", eventDuration)
                .add("type", type)
                .add("host", host)
                .add("alert", alert)
                .toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(eventId, event.eventId);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(eventId);
    }

    public Builder toBuilder()
    {
        return new Builder(
                eventId,
                eventDuration,
                type,
                host,
                alert
        );
    }

    public static final class Builder
    {
        private String eventId;
        private long eventDuration = -1L;
        private String type;
        private String host;
        private boolean alert;

        private Builder()
        {
        }

        private Builder(String eventId, long eventDuration, String type, String host, boolean alert)
        {
            this.eventId = eventId;
            this.eventDuration = eventDuration;
            this.type = type;
            this.host = host;
            this.alert = alert;
        }

        public Builder eventId(String eventId)
        {
            checkArgument(isNotEmpty(eventId));
            this.eventId = eventId;
            return this;
        }

        public Builder eventDuration(long eventDuration)
        {
            checkArgument(eventDuration >= 0L);
            this.eventDuration = eventDuration;
            return this;
        }

        public Builder type(String type)
        {
            checkArgument(isNotEmpty(type));
            this.type = type;
            return this;
        }

        public Builder host(String host)
        {
            checkArgument(isNotEmpty(host));
            this.host = host;
            return this;
        }

        public Builder alert(boolean alert)
        {
            this.alert = alert;
            return this;
        }

        public Event build()
        {
            checkArgument(isNotEmpty(eventId));
            checkArgument(eventDuration >= 0L);
            return new Event(eventId, eventDuration, type, host, alert);
        }

        @Override
        public String toString()
        {
            return MoreObjects.toStringHelper(this)
                    .add("eventId", eventId)
                    .add("eventDuration", eventDuration)
                    .add("type", type)
                    .add("host", host)
                    .add("alert", alert)
                    .toString();
        }
    }
}
