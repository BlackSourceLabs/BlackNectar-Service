/*
 * Copyright 2016 BlackWholeLabs.
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

 
package tech.blackhole.blacknectar.service.api.operations;


import com.google.gson.JsonArray;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blackhole.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.blackhole.blacknectar.service.Server.AROMA;
import static tech.blackhole.blacknectar.service.api.MediaTypes.APPLICATION_JSON;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
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
    public Object handle(Request request, Response response) throws Exception
    {
        LOG.info("Received GET request to GET a Sample Store from IP [{}]", request.ip());

        AROMA.begin().titled("Request Received")
            .text("Request to get sample store from IP [{}]", request.ip())
            .withUrgency(Urgency.LOW)
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
            AROMA.begin().titled("Request Failed")
                .text("Could not load Store, {}", ex)
                .withUrgency(Urgency.HIGH)
                .send();

            return new JsonArray();
        }
    }

}
