package tech.blacksource.blacknectar.service.json;/*
 * 
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

import com.google.gson.JsonObject;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat
public class FieldValueJsonTest
{
    @GenerateString
    private String name;

    @GenerateString
    private String value;

    private JsonObject expected;

    private FieldValueJson instance;


    @Before
    public void setUp() throws Exception
    {
        setupData();
        instance = new FieldValueJson(name, value);
    }

    @DontRepeat
    @Test
    public void testConstructorWithBadArgs() throws Exception
    {
        assertThrows(() -> new FieldValueJson("", value));
        assertThrows(() -> new FieldValueJson(name, ""));
    }

    @Test
    public void asJson() throws Exception
    {
        JsonObject result = instance.asJson();
        Assert.assertThat(result, Matchers.notNullValue());
        Assert.assertThat(result, Matchers.is(expected));
    }

    private void setupData()
    {
        expected = new JsonObject();
        expected.addProperty(FieldValueJson.Keys.NAME, name);
        expected.addProperty(FieldValueJson.Keys.VALUE, value);
    }

    private void setupMocks()
    {

    }
}