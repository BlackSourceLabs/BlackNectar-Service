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

import java.util.Objects;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.blacksource.blacknectar.ebt.balance.Field;
import tech.blacksource.blacknectar.ebt.balance.FieldValue;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.arguments.*;
import tech.sirwellington.alchemy.arguments.Checks;

import static tech.blacksource.blacknectar.service.BlackNectarAssertions.objectWithField;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * This is an internal representation of {@link FieldValue}.
 * This shortened version which is received from clients contains just the bare minimum
 * information required.
 *
 * @author SirWellington
 */
@Internal
final class FieldValueJson implements AsJson
{
    private final static Logger LOG = LoggerFactory.getLogger(FieldValueJson.class);

    private final String name;
    private final String value;
    private final Field.FieldType fieldType;
    private final JsonObject json;

    /**
     * Creates a {@link FieldValueJson} from {@linkplain JsonObject JSON}.
     *
     * @param json The Json to parse.
     * @return
     */
    static FieldValueJson fromJson(@Required JsonObject json)
    {
        checkThat(json)
                .is(notNull())
                .is(objectWithField(Keys.NAME))
                .is(objectWithField(Keys.VALUE));

        String name = json.get(Keys.NAME).getAsString();
        String value = json.get(Keys.VALUE).getAsString();

        JsonPrimitive fieldTypeElement = json.getAsJsonPrimitive(Keys.TYPE);

        Field.FieldType fieldType = extractFieldTypeFrom(fieldTypeElement);

        return new FieldValueJson(name, value, fieldType);
    }

    FieldValueJson(@NonEmpty String name, @NonEmpty String value, @Required Field.FieldType fieldType)
    {
        checkThat(name, value).are(nonEmptyString());
        checkThat(fieldType).is(notNull());

        this.name = name;
        this.value = value;
        this.fieldType = fieldType;

        this.json = toJson();
    }

    private JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        json.addProperty(Keys.NAME, name);
        json.addProperty(Keys.VALUE, value);
        json.addProperty(Keys.TYPE, fieldType.toString());

        return json;
    }

    FieldValue toNative()
    {
        Field field = new Field(name, fieldType);

        return new FieldValue(field, value);
    }

    public String getName()
    {
        return name;
    }

    public String getValue()
    {
        return value;
    }

    public Field.FieldType getFieldType()
    {
        return fieldType;
    }

    @Override
    public JsonObject asJson()
    {
        return json;
    }


    private static Field.FieldType extractFieldTypeFrom(JsonPrimitive fieldTypeElement)
    {
        if (fieldTypeElement == null || fieldTypeElement.isJsonNull())
        {
            return Field.FieldType.OTHER;
        }

        String fieldTypeString = fieldTypeElement.getAsString();

        if (Checks.isNullOrEmpty(fieldTypeString))
        {
            return Field.FieldType.OTHER;
        }

        try
        {
            return Field.FieldType.valueOf(fieldTypeString);
        }
        catch (RuntimeException ex)
        {
            return Field.FieldType.OTHER;
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        FieldValueJson that = (FieldValueJson) o;

        return Objects.equals(name, that.name) &&
                Objects.equals(value, that.value) &&
                fieldType == that.fieldType;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, value, fieldType);
    }

    static class Keys
    {
        static final String NAME = "name";
        static final String VALUE = "value";
        static final String TYPE = "field_type";
    }
}
