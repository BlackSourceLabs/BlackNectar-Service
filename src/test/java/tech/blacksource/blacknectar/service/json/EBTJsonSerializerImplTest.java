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
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.ebt.balance.*;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.sirwellington.alchemy.generator.StringGenerators;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.fieldValues;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.jsonObjects;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;


/**
 * @author SirWellington
 */
@Repeat
@RunWith(AlchemyTestRunner.class)
public class EBTJsonSerializerImplTest
{

    private Aroma aroma;

    private Gson gson = new Gson();

    @GenerateEnum
    private State state;

    private Field field;

    private FieldValue fieldValue;

    private EBTJsonSerializerImpl instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new EBTJsonSerializerImpl(aroma, gson);
    }


    private void setupData() throws Exception
    {
        this.fieldValue = one(fieldValues());
        this.field = fieldValue.getField();
    }

    private void setupMocks() throws Exception
    {
        aroma = Aroma.createNoOpInstance();
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new EBTJsonSerializerImpl(null, gson));
        assertThrows(() -> new EBTJsonSerializerImpl(aroma, null));
    }

    @Test
    public void testSerializeState()
    {
        JsonObject expected = new StateJson(state).asJson();
        JsonObject result = instance.serializeState(state);

        assertThat(result, is(expected));
    }

    @DontRepeat
    @Test
    public void testSerializeStateWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.serializeState(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSerializeField() throws Exception
    {
        JsonObject result = instance.serializeField(field);
        JsonObject expected = gson.toJsonTree(field).getAsJsonObject();

        assertThat(result, is(expected));
    }


    @Test
    public void testSerializedFieldWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.serializeField(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testDeserializeField() throws Exception
    {
        JsonObject json = instance.serializeField(field);
        String jsonString = gson.toJson(json);

        Field result = instance.deserializeField(jsonString);
        assertThat(result, notNullValue());
        assertThat(result, is(field));
    }

    @DontRepeat
    @Test
    public void testDeserializeFieldWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deserializeField("")).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> instance.deserializeField(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSerializeFieldValue() throws Exception
    {
        JsonObject result = instance.serializeFieldValue(fieldValue);
        JsonObject expected = gson.toJsonTree(fieldValue).getAsJsonObject();

        assertThat(result, is(expected));
    }


    @Test
    public void testSerializedFieldValueWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.serializeFieldValue((null))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testDeserializeFieldValue() throws Exception
    {
        JsonObject json = instance.serializeFieldValue(fieldValue);
        String jsonString = gson.toJson(json);

        FieldValue result = instance.deserializeFieldValue(jsonString);
        assertThat(result, notNullValue());
        assertThat(result, is(fieldValue));
    }

    @DontRepeat
    @Test
    public void testDeserializeFieldValueWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deserializeFieldValue("")).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> instance.deserializeFieldValue(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testDeserializeFieldValues() throws Exception
    {
        List<FieldValueJson> fieldValues = listOf(fieldValues()).stream()
                                                                .map(this::fieldValueToJsonCounterPart)
                                                                .collect(Collectors.toList());

        List<FieldValue> expected = fieldValues.stream()
                                               .map(FieldValueJson::toNative)
                                               .collect(Collectors.toList());

        String json = fieldValues.stream()
                                 .map(FieldValueJson::asJson)
                                 .collect(JSON.collectArray())
                                 .toString();


        List<FieldValue> result = instance.deserializeFieldValues(json);
        assertThat(result, is(expected));
    }

    private FieldValueJson fieldValueToJsonCounterPart(FieldValue fieldValue)
    {
        return new FieldValueJson(fieldValue.getField().getName(),
                                  fieldValue.getValue(),
                                  fieldValue.getField().getType());
    }

    @Test
    public void testDeserializeFieldValuesWithJsonObject() throws Exception
    {
        JsonObject object = one(jsonObjects());
        String json = object.toString();

        List<FieldValue> result = instance.deserializeFieldValues(json);
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }

    @Test
    public void testDeserializeFieldValuesWithJsonNotArray() throws Exception
    {
        String badJson = one(StringGenerators.hexadecimalString(10));

        List<FieldValue> result = instance.deserializeFieldValues(badJson);
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }

    @Test
    public void testDeserializeFieldValuesWithBadJson() throws Exception
    {
        String unterminatedJson = "{" + one(StringGenerators.strings());

        assertThrows(() -> instance.deserializeFieldValues(unterminatedJson))
                .isInstanceOf(BadArgumentException.class);
    }

    @DontRepeat
    @Test
    public void testDeserializeFieldValuesWithBadArgs() throws Exception
    {
        List<FieldValue> result = instance.deserializeFieldValues(null);
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));

        result = instance.deserializeFieldValues("");
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }
}