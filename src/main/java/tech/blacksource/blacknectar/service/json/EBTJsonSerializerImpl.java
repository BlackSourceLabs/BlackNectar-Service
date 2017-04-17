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

import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.ebt.balance.*;
import tech.blacksource.blacknectar.service.exceptions.*;
import tech.sirwellington.alchemy.arguments.Checks;

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
        checkThat(json)
                .usingMessage("json cannot be missing")
                .is(nonEmptyString());

        JsonElement parsedJson = tryToParseJson(json);

        if (parsedJson == null || parsedJson.isJsonNull() || !parsedJson.isJsonObject())
        {
            return null;
        }

        JsonObject jsonObject = parsedJson.getAsJsonObject();

        Field result = gson.fromJson(jsonObject, Field.class);

        if (result == null)
        {
            makeNoteThatCouldNotExtractFieldFrom(jsonObject);
        }

        return result;
    }

    @Override
    public JsonObject serializeFieldValue(FieldValue fieldValue) throws BlackNectarAPIException
    {
        checkThat(fieldValue).is(notNull());

        JsonElement result = gson.toJsonTree(fieldValue);

        if (!result.isJsonObject())
        {
            throw new OperationFailedException("Could not serialize: " + fieldValue);
        }

        return result.getAsJsonObject();
    }

    @Override
    public FieldValue deserializeFieldValue(String json) throws BlackNectarAPIException
    {
        checkThat(json)
                .usingMessage("json cannot be missing")
                .is(nonEmptyString());

        JsonElement parsedJson = tryToParseJson(json);

        checkThat(parsedJson)
                .throwing(BadArgumentException.class)
                .usingMessage("Could not parse json as Object: " + json)
                .is(instanceOf(JsonObject.class));

        JsonObject jsonObject = parsedJson.getAsJsonObject();

        return tryToLoadFieldValueFrom(jsonObject);
    }

    private FieldValue tryToLoadFieldValueFrom(JsonObject jsonObject)
    {

        try
        {
            return gson.fromJson(jsonObject, FieldValue.class);
        }
        catch (RuntimeException ex)
        {
            makeNoteThatCouldNotExtractFieldValueFrom(jsonObject);
            throw new OperationFailedException(ex);
        }
    }

    @Override
    public List<FieldValue> deserializeFieldValues(String json) throws BlackNectarAPIException
    {
        List<FieldValue> result = Lists.create();

        if (Checks.isNullOrEmpty(json))
        {
            makeNoteThatReceivedEmptyString();
            return result;
        }

        JsonElement jsonElement = tryToParseJson(json);

        if (!jsonElement.isJsonArray())
        {
            makeNoteThatJsonIsNotArray(json);
            return result;
        }

        JsonArray array = jsonElement.getAsJsonArray();

        if (array == null || array.size() == 0)
        {
            makeNoteThatArrayOfFieldValueIsEmpty(json);
            return result;
        }

        List<JsonElement> elements = Lists.create();
        array.iterator().forEachRemaining(elements::add);

        elements.stream()
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .filter(Objects::nonNull)
                .map(FieldValueJson::fromJson)
                .filter(Objects::nonNull)
                .map(FieldValueJson::toNative)
                .forEach(result::add);

        return result;
    }

    private JsonElement tryToParseJson(String json)
    {
        try
        {
            return gson.fromJson(json, JsonElement.class);
        }
        catch (JsonParseException ex)
        {
            makeNoteOfInvalidJson(json);
            throw new BadArgumentException("Invalid JSON: " + json, ex);
        }
    }

    private void makeNoteOfInvalidJson(String json)
    {
        String message = "Received invalid JSON: {}";
        LOG.error(message, json);
        aroma.sendHighPriorityMessage("Json Parse Failed", message, json);
    }

    private void makeNoteThatJsonIsNotArray(String json)
    {
        String message = "Expected JSON Array, but is intead: {}";
        LOG.warn(message, json);
        aroma.sendMediumPriorityMessage("Json Parse Failed", message, json);
    }

    private void makeNoteThatArrayOfFieldValueIsEmpty(String json)
    {
        String message = "Array is unexpectedly empty: {}";
        LOG.warn(message, json);
        aroma.sendMediumPriorityMessage("Json Parse Failed", message, json);
    }

    private void makeNoteThatCouldNotExtractFieldFrom(JsonObject jsonObject)
    {
        String message = "Could not extract Field from JSON: {}";
        LOG.warn(message, jsonObject);
        aroma.sendMediumPriorityMessage("Json Parse Failed", message, jsonObject);
    }

    private void makeNoteThatCouldNotExtractFieldValueFrom(JsonObject jsonObject)
    {
        String message = "Could not extract FieldValue from JSON: {}";
        LOG.warn(message, jsonObject);
        aroma.sendMediumPriorityMessage("Json Parse Failed", message, jsonObject);
    }

    private void makeNoteThatReceivedEmptyString()
    {
        String message = "Received unexpected empty string. Recovering.";
        LOG.warn(message);
        aroma.sendMediumPriorityMessage("Json Parse Failed", message);
    }
}
