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

import javax.inject.Inject;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.ebt.balance.*;
import tech.blacksource.blacknectar.service.exceptions.BlackNectarAPIException;
import tech.blacksource.blacknectar.service.exceptions.OperationFailedException;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.instanceOf;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * @author SirWellington
 */
final class EBTJsonSerializerImpl implements EBTJsonSerializer
{
    private final static Logger LOG = LoggerFactory.getLogger(EBTJsonSerializerImpl.class);

    private final Aroma aroma;
    private final Gson gson;

    @Inject
    EBTJsonSerializerImpl(Aroma aroma, Gson gson)
    {
        checkThat(aroma, gson).are(notNull());

        this.aroma = aroma;
        this.gson = gson;
    }

    @Override
    public JsonObject serializeState(State state)
    {
        checkThat(state)
                .usingMessage("State cannot be null")
                .is(notNull());

        final StateJson stateJson = new StateJson(state);
        final JsonObject result = stateJson.asJson();

        LOG.debug("Serialized {} to {}", state, result);

        return result;
    }

    @Override
    public JsonObject serializeField(Field field) throws BlackNectarAPIException
    {
        checkThat(field).is(notNull());

        JsonElement json = gson.toJsonTree(field);

        checkThat(json)
                .throwing(OperationFailedException.class)
                .usingMessage("Could not serialize field to JSON: " + json)
                .is(instanceOf(JsonObject.class));

        return json.getAsJsonObject();
    }

    @Override
    public Field deserializeField(String json) throws BlackNectarAPIException
    {
        checkThat(json).is(nonEmptyString());

        JsonObject jsonObject = gson.fromJson(json, JsonObject.class);

        if (jsonObject == null || jsonObject.isJsonNull())
        {
            return null;
        }

        Field result = gson.fromJson(jsonObject, Field.class);

        if (result == null)
        {
            makeNoteThatCouldNotExtractFieldValueFrom(jsonObject);
        }

        return result;
    }

    private void makeNoteThatCouldNotExtractFieldValueFrom(JsonObject jsonObject)
    {
        String message = "Could not extract FieldValue from JSON: {}";
        LOG.warn(message, jsonObject);
        aroma.sendMediumPriorityMessage("Json Parse Failed", message, jsonObject);
    }
}
