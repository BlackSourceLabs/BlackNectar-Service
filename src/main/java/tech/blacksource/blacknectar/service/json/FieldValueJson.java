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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.blacksource.blacknectar.ebt.balance.Field;
import tech.blacksource.blacknectar.ebt.balance.FieldValue;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.blacksource.blacknectar.service.BlackNectarAssertions.hasField;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * @author SirWellington
 */
final class FieldValueJson implements AsJson
{
    private final static Logger LOG = LoggerFactory.getLogger(FieldValueJson.class);

    private final String name;
    private final String value;
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
                .is(hasField(Keys.NAME))
                .is(hasField(Keys.VALUE));

        String name = json.get(Keys.NAME).getAsString();
        String value = json.get(Keys.VALUE).getAsString();

        return new FieldValueJson(name, value);
    }

    FieldValueJson(@NonEmpty String name, @NonEmpty String value)
    {
        checkThat(name, value).are(nonEmptyString());

        this.name = name;
        this.value = value;
        this.json = toJson();
    }

    private JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        json.addProperty(Keys.NAME, name);
        json.addProperty(Keys.VALUE, value);

        return json;
    }

    FieldValue toNative()
    {
        Field field = new Field(name, Field.FieldType.OTHER);

        return new FieldValue(field, value);
    }

    @Override
    public JsonObject asJson()
    {
        return json;
    }

    static class Keys
    {
        static final String NAME = "name";
        static final String VALUE = "value";
    }
}
