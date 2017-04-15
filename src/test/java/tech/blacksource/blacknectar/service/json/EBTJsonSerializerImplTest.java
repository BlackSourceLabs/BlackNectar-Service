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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.ebt.balance.*;
import tech.blacksource.blacknectar.ebt.balance.states.California;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;


/**
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class EBTJsonSerializerImplTest
{

    private Aroma aroma;

    private Gson gson = new Gson();

    @GenerateEnum
    private State state;

    private Field field;

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
        this.field = Lists.oneOf(California.Fields.INSTANCE.getAll());
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

        FieldValue result = instance.deserializeFieldValue(jsonString);
        assertThat(result, notNullValue());
        assertThat(result, is(field));
    }

    @DontRepeat
    @Test
    public void testDeserializeFieldWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deserializeFieldValue("")).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> instance.deserializeFieldValue(null)).isInstanceOf(IllegalArgumentException.class);
    }
}