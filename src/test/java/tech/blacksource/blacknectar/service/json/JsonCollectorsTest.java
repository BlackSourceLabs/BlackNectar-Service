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

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AlchemyTestRunner.class)
@Repeat
public class JsonCollectorsTest
{

    @GenerateList(String.class)
    private List<String> strings;

    @GenerateList(Integer.class)
    private List<Integer> numbers;

    @Test
    public void testCollectArrayWithStrings() throws Exception
    {
        JsonArray expected = new JsonArray();
        strings.forEach(expected::add);

        JsonArray result = strings.stream()
                                  .map(JsonPrimitive::new)
                                  .collect(JsonCollectors.collectArray());

        assertThat(result, is(expected));
    }

    @Test
    public void testCollectArrayWithNumbers() throws Exception
    {
        JsonArray expected = new JsonArray();
        numbers.forEach(expected::add);

        JsonArray result = numbers.stream()
                                  .map(JsonPrimitive::new)
                                  .collect(JsonCollectors.collectArray());

        assertThat(result, is(expected));
    }

    @DontRepeat
    @Test
    public void testCollectArrayWithEmpty() throws Exception
    {
        JsonArray expected = new JsonArray();

        JsonArray result = Lists.<String>emptyList()
                                .stream()
                                .map(JsonPrimitive::new)
                                .collect(JsonCollectors.collectArray());

        assertThat(result, is(expected));
    }


}