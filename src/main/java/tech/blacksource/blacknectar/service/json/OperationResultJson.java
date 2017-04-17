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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.blacksource.blacknectar.service.BlackNectarAssertions.hasField;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * @author SirWellington
 */
public final class OperationResultJson implements AsJson
{
    private final static Logger LOG = LoggerFactory.getLogger(OperationResultJson.class);

    private final String message;
    private final boolean success;
    private final int statusCode;
    private final JsonObject json;

    public static OperationResultJson fromJson(@Required JsonObject object)
    {
        checkThat(object)
                .is(notNull())
                .is(hasField(Keys.MESSAGE))
                .is(hasField(Keys.STATUS_CODE))
                .is(hasField(Keys.SUCCESS));

        String message = object.get(Keys.MESSAGE).getAsString();
        boolean success = object.get(Keys.SUCCESS).getAsBoolean();
        int statusCode = object.get(Keys.STATUS_CODE).getAsInt();

        return new OperationResultJson(message, success, statusCode);
    }

    public OperationResultJson(@Required String message, boolean success, int statusCode)
    {
        checkThat(message).is(nonEmptyString());

        this.message = message;
        this.success = success;
        this.statusCode = statusCode;
        this.json = toJson();
    }

    private JsonObject toJson()
    {
        JsonObject json = new JsonObject();
        json.addProperty(Keys.MESSAGE, message);
        json.addProperty(Keys.SUCCESS, success);
        json.addProperty(Keys.STATUS_CODE, statusCode);

        return json;
    }

    public String getMessage()
    {
        return message;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    @Override
    public JsonObject asJson()
    {
        return json;
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
        OperationResultJson that = (OperationResultJson) o;
        return success == that.success &&
                statusCode == that.statusCode &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(message, success, statusCode);
    }

    @Override
    public String toString()
    {
        return "OperationResultJson{" +
                "message='" + message + '\'' +
                ", success=" + success +
                ", statusCode=" + statusCode +
                '}';
    }

    static class Keys
    {
        static final String MESSAGE = "message";
        static final String SUCCESS = "success";
        static final String STATUS_CODE = "status_code";
    }
}
