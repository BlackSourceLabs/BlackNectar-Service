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
package tech.blacksource.blacknectar.service;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Response;
import tech.blacksource.blacknectar.service.json.OperationResultJson;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;
import tech.sirwellington.alchemy.annotations.arguments.Required;

/**
 * @author SirWellington
 */
@NonInstantiable
public final class Responses
{
    private final static Logger LOG = LoggerFactory.getLogger(Responses.class);

    private Responses() throws IllegalAccessException
    {
        throw new IllegalAccessException("Cannot instantiate");
    }

    /**
     * Use when the client passes a bad argument.
     */
    public static JsonObject badArgument(String message, @Required Response response)
    {
        message = Strings.nullToEmpty(message);
        int statusCode = StatusCodes.BAD_ARGUMENT;

        response.status(statusCode);

        return new OperationResultJson(message, false, statusCode).asJson();
    }


    public static class StatusCodes
    {
        public static final int OK = 200;
        public static final int NO_CONTENT = 202;
        public static final int BAD_ARGUMENT = 400;
        public static final int SERVER_ERROR = 500;
        public static final int NOT_IMPLEMENTED = 501;
    }
}
