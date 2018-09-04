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
import com.google.gson.Gson;
import org.apache.flink.configuration.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class DeserializeJsonMapFunctionTest
{
    @Mock
    Configuration configuration;

    DeserializeJsonMapFunction deserializeJsonMapFunction;

    @BeforeEach
    void setup() throws Exception
    {
        deserializeJsonMapFunction = new DeserializeJsonMapFunction();
        deserializeJsonMapFunction.open(configuration);
    }

    @Test
    void map___given_JSON_string___when_map___then_produces_a_proper_Java_object()
    {
        EventAction eventAction = new EventAction(
                "scsmbstgra",
                "FINISHED",
                1491377495217L,
                "APPLICATION_LOG",
                "12345"
        );
        String json = new Gson().toJson(eventAction);


        EventAction result = deserializeJsonMapFunction.map(json);

        assertThat(result)
                .isNotNull()
                .isEqualToComparingFieldByField(eventAction);
    }
}