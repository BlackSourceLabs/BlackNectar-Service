package tech.blacksource.blacknectar.service.operations.ebt;

import java.util.Set;

import javax.inject.Inject;

import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.*;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.ebt.balance.*;
import tech.blacksource.blacknectar.service.JSON;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.exceptions.UnsupportedStateException;
import tech.blacksource.blacknectar.service.operations.Parameters;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.collectionContaining;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * Created by Commander on 4/8/2017.
 */
final class GetStateInfoOperation implements Route
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

        String stateParameter = request.params(Parameters.EBT.STATE);
        checkThat(stateParameter)
                .throwing(BadArgumentException.class)
                .usingMessage("State parameter missing")
                .is(nonEmptyString());

        State state = State.fromAbbreviatedText(stateParameter);

        if (state == null)
        {
            LOG.info("Unknown state abbreviation: {}, trying to parse as full-name");
            state = State.fromText(stateParameter);
        }

        checkThat(state)
                .throwing(BadArgumentException.class)
                .usingMessage("Unknown State: " + stateParameter)
                .is(notNull());

        checkThat(state)
                .throwing(UnsupportedStateException.class)
                .is(supportedState());

        StateWebsite stateWebsite = websiteFactory.getConnectionToState(state);

        return stateWebsite.getFeatures()
                           .stream()
                           .map(StateWebsite.Feature::toString)
                           .map(JsonPrimitive::new)
                           .collect(JSON.collectArray());
    }

    private AlchemyAssertion<State> supportedState()
    {
        return state ->
        {
            Set<State> supportedStates = websiteFactory.getSupportedStates();

            checkThat(supportedStates)
                    .usingMessage("State is unsupported: " + state)
                    .is(collectionContaining(state));
        };
    }

}
