/*
 * Copyright 2017 BlackSource, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tech.blacksource.blacknectar.service.json;

import java.util.function.*;
import java.util.stream.Collector;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;

/**
 * @author SirWellington
 */
@NonInstantiable
public final class JSON
{

    private final static Logger LOG = LoggerFactory.getLogger(JSON.class);

    public static Collector<JsonElement, JsonArray, JsonArray> collectArray()
    {
        Supplier<JsonArray> supplier = JsonArray::new;
        BiConsumer<JsonArray, JsonElement> accumulator = JsonArray::add;
        BinaryOperator<JsonArray> combiner = (first, second) ->
        {
            JsonArray result = new JsonArray();
            result.addAll(first);
            result.addAll(second);
            return result;
        };

        Collector<JsonElement, JsonArray, JsonArray> collector = Collector.of(supplier,
                                                                              accumulator,
                                                                              combiner,
                                                                              Collector.Characteristics.CONCURRENT);
        return collector;
    }
}
