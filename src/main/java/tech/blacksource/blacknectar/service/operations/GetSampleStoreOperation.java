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


package tech.blacksource.blacknectar.service.operations;


import javax.inject.Inject;

import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.*;
import tech.aroma.client.Aroma;
import tech.aroma.client.Priority;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.blacksource.blacknectar.service.data.MediaTypes.APPLICATION_JSON;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * Returns a sample BlackNectar store to use for testing purposes.
 *
 * @author SirWellington
 */
public class GetSampleStoreOperation implements Route
{
    private final static Logger LOG = LoggerFactory.getLogger(GetSampleStoreOperation.class);

    private final Aroma aroma;

    @Inject
    public GetSampleStoreOperation(@Required Aroma aroma)
    {
        checkThat(aroma).is(notNull());
        this.aroma = aroma;
    }

    @Override
    public JsonArray handle(Request request, Response response) throws Exception
    {
        checkThat(request, response)
                .usingMessage("Received null arguments")
                .throwing(BadArgumentException.class)
                .are(notNull());

        LOG.info("Received GET request to GET a Sample Store from IP [{}]", request.ip());

        aroma.begin().titled("Request Received")
             .withBody("Request to get sample store from IP [{}]", request.ip())
             .withPriority(Priority.LOW)
             .send();

        response.status(200);
        response.type(APPLICATION_JSON);

        try
        {
            Store store = Store.SAMPLE_STORE;

            JsonArray json = new JsonArray();
            json.add(store.asJSON());
            return json;
        }
        catch (Exception ex)
        {
            aroma.begin().titled("Request Failed")
                 .withBody("Could not load Store, {}", ex)
                 .withPriority(Priority.HIGH)
                 .send();

            return new JsonArray();
        }
    }

}
