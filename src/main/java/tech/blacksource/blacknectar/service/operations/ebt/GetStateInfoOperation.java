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

package tech.blacksource.blacknectar.service.operations.ebt;

import javax.inject.Inject;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.*;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.ebt.balance.*;
import tech.blacksource.blacknectar.service.data.MediaTypes;
import tech.blacksource.blacknectar.service.exceptions.UnsupportedStateException;
import tech.blacksource.blacknectar.service.json.JSON;
import tech.blacksource.blacknectar.service.operations.Parameters;

import static tech.blacksource.blacknectar.service.BlackNectarAssertions.supportedState;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * @author SirWellington
 */
public class GetStateInfoOperation implements Route
{
    private final static Logger LOG = LoggerFactory.getLogger(GetStateInfoOperation.class);

    private final Aroma aroma;
    private final StateWebsiteFactory websiteFactory;

    @Inject
    GetStateInfoOperation(Aroma aroma, StateWebsiteFactory websiteFactory)
    {
        checkThat(aroma, websiteFactory).are(notNull());

        this.aroma = aroma;
        this.websiteFactory = websiteFactory;
    }

    @Override
    public JsonElement handle(Request request, Response response) throws Exception
    {
        checkThat(request, response).are(notNull());

        State state = Parameters.EBT.getStateFrom(request);

        checkThat(state)
                .throwing(ex -> new UnsupportedStateException(ex, state))
                .is(supportedState(websiteFactory));

        StateWebsite stateWebsite = websiteFactory.getConnectionToState(state);

        JsonArray result = getFeaturesFor(stateWebsite);

        makeNoteOfFeatures(state, result);
        response.type(MediaTypes.APPLICATION_JSON);

        return result;
    }

    private JsonArray getFeaturesFor(StateWebsite stateWebsite)
    {
        return stateWebsite.getFeatures()
                           .stream()
                           .map(StateWebsite.Feature::toString)
                           .map(JsonPrimitive::new)
                           .collect(JSON.collectArray());
    }

    private void makeNoteOfUnknownStateAbbreviation(String stateParameter)
    {
        aroma.sendMediumPriorityMessage("Unknown State Abbreviation", "Trying to parse {} as full-name instead", stateParameter);
    }

    private void makeNoteOfFeatures(State state, JsonArray result)
    {
        String message = "Found {} features for state '{}': {}";
        LOG.debug(message, result.size(), state.getTitleCased(), result);
        aroma.sendLowPriorityMessage("Get State Features", message, result.size(), state.getTitleCased(), result);
    }

}
