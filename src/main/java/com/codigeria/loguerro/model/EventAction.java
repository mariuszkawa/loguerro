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

public final class EventAction
{
    private final String id;
    private final String state;
    private final long timestamp;
    private final String type;
    private final String host;

    public EventAction(String id, String state, long timestamp)
    {
        this(id, state, timestamp, null, null);
    }

    public EventAction(String id, String state, long timestamp, String type, String host)
    {
        this.id = id;
        this.state = state;
        this.timestamp = timestamp;
        this.type = type;
        this.host = host;
    }

    public String getId()
    {
        return id;
    }

    public String getState()
    {
        return state;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public String getType()
    {
        return type;
    }

    public String getHost()
    {
        return host;
    }
}
