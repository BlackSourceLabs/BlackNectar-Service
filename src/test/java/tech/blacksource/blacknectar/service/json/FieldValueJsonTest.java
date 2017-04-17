/*
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
package tech.blacksource.blacknectar.service.json;

import com.google.gson.JsonObject;
import org.hamcrest.Matchers;
import org.junit.*;
import org.junit.runner.RunWith;
import tech.blacksource.blacknectar.ebt.balance.Field;
import tech.blacksource.blacknectar.ebt.balance.FieldValue;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
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

    @GenerateEnum
    private Field.FieldType fieldType;

    private JsonObject expected;

    private FieldValueJson instance;


    @Before
    public void setUp() throws Exception
    {
        setupData();
        instance = new FieldValueJson(name, value, fieldType);
    }

    @DontRepeat
    @Test
    public void testConstructorWithBadArgs() throws Exception
    {
        assertThrows(() -> new FieldValueJson(null, value, fieldType));
        assertThrows(() -> new FieldValueJson(name, null, fieldType));
        assertThrows(() -> new FieldValueJson(name, value, null));

        assertThrows(() -> new FieldValueJson("", value, fieldType));
        assertThrows(() -> new FieldValueJson(name, "", fieldType));
    }

    @Test
    public void asJson() throws Exception
    {
        JsonObject result = instance.asJson();
        assertThat(result, Matchers.notNullValue());
        assertThat(result, is(expected));
    }

    @Test
    public void testAsNativeType() throws Exception
    {
        FieldValue result = instance.toNative();

        assertThat(result.getValue(), is(value));
        assertThat(result.getField().getName(), is(name));
        assertThat(result.getField().getType(), is(fieldType));
    }

    private void setupData()
    {
        expected = new JsonObject();
        expected.addProperty(FieldValueJson.Keys.NAME, name);
        expected.addProperty(FieldValueJson.Keys.VALUE, value);
        expected.addProperty(FieldValueJson.Keys.TYPE, fieldType.toString());
    }

    private void setupMocks()
    {

    }
}