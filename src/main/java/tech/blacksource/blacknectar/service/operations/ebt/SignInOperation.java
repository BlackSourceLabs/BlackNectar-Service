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
package tech.blacksource.blacknectar.service.operations.ebt;

import java.util.List;

import javax.inject.Inject;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import spark.*;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.ebt.balance.*;
import tech.blacksource.blacknectar.service.Responses;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.exceptions.UnsupportedStateException;
import tech.blacksource.blacknectar.service.json.EBTJsonSerializer;
import tech.blacksource.blacknectar.service.json.OperationResultJson;
import tech.blacksource.blacknectar.service.operations.Parameters;

import static tech.blacksource.blacknectar.service.BlackNectarAssertions.supportedState;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * @author SirWellington
 */
public class SignInOperation implements Route
{
    private final static Logger LOG = LoggerFactory.getLogger(SignInOperation.class);

    private final Aroma aroma;
    private final EBTJsonSerializer jsonSerializer;
    private final StateWebsiteFactory websiteFactory;

    @Inject
    SignInOperation(Aroma aroma, EBTJsonSerializer jsonSerializer, StateWebsiteFactory websiteFactory)
    {
        checkThat(aroma, jsonSerializer, websiteFactory)
                .are(notNull());

        this.aroma = aroma;
        this.jsonSerializer = jsonSerializer;
        this.websiteFactory = websiteFactory;
    }

    @Override
    public JsonObject handle(Request request, Response response) throws Exception
    {
        checkThat(request, response)
                .throwing(BadArgumentException.class)
                .are(notNull());

        State state = Parameters.EBT.getStateFrom(request);

        checkThat(state)
                .throwing(BadArgumentException.class)
                .usingMessage("Could not parse state from: " + request.pathInfo())
                .is(notNull());

        checkThat(state)
                .throwing(ex -> new UnsupportedStateException(ex, state))
                .is(supportedState(websiteFactory));

        makeNoteThatRequestReceived(request, state);

        String requestBody = request.body();

        List<FieldValue> fieldValues = jsonSerializer.deserializeFieldValues(requestBody);

        if (fieldValues.isEmpty())
        {
            makeNoteThatNoFieldValuesFound(requestBody);
            return Responses.badArgument("Field values missing from request", response);
        }

        OperationResultJson result = tryToSignIntoState(state, fieldValues);

        return result.asJson();
    }

    private OperationResultJson tryToSignIntoState(State state, List<FieldValue> fieldValues)
    {

        StateWebsite stateWebsite = websiteFactory.getConnectionToState(state);
        Account account = new Account(Sets.copyOf(fieldValues));

        stateWebsite.signIn(account);

        boolean isSignedIn = stateWebsite.isSignedIn();

        if (isSignedIn)
        {
            return new OperationResultJson("Successfully signed in", isSignedIn, Responses.StatusCodes.OK);
        }
        else
        {
            return new OperationResultJson("Failed to sign in", isSignedIn, Responses.StatusCodes.OK);
        }
    }

    private void makeNoteThatRequestReceived(Request request, State state)
    {
        String message = "Received request to sign into state: {} from [{]]";
        LOG.debug(message, state, request.ip());
        aroma.sendLowPriorityMessage("[Sign In] Called", message, state, request.ip());
    }

    private void makeNoteThatNoFieldValuesFound(String requestBody)
    {
        String message = "No Field Values found in request: {}";
        LOG.warn(message, requestBody);
        aroma.sendMediumPriorityMessage("Bad Argument", message, requestBody);
    }
}
