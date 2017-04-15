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
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * @author SirWellington
 */
final class OperationResultJson implements AsJson
{
    private final static Logger LOG = LoggerFactory.getLogger(OperationResultJson.class);

    private final String message;
    private final boolean success;
    private final JsonObject json;


    OperationResultJson(@Required String message, boolean success)
    {
        checkThat(message).is(nonEmptyString());

        this.message = message;
        this.success = success;
        this.json = toJson();
    }

    private JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        json.addProperty(Keys.MESSAGE, message);
        json.addProperty(Keys.SUCCESS, success);

        return json;
    }

    @Override
    public JsonObject asJson()
    {
        return json;
    }

    static class Keys
    {
        static final String MESSAGE = "message";
        static final String SUCCESS = "success";
    }
}
