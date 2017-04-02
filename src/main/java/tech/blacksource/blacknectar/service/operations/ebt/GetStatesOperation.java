package tech.blacksource.blacknectar.service.operations.ebt;

import javax.inject.Inject;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.*;
import tech.blacksource.blacknectar.ebt.balance.State;
import tech.blacksource.blacknectar.ebt.balance.StateWebsiteFactory;
import tech.blacksource.blacknectar.service.JSON;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * Responsible for retrieving all of the states supported by the EBT API.
 *
 * @author SirWellington
 */
public class GetStatesOperation implements Route
{
    private final static Logger LOG = LoggerFactory.getLogger(GetStatesOperation.class);

    private final StateWebsiteFactory stateWebsites;

    @Inject
    GetStatesOperation(StateWebsiteFactory stateWebsites)
    {
        checkThat(stateWebsites).is(notNull());

        this.stateWebsites = stateWebsites;
    }

    @Override
    public JsonArray handle(Request request, Response response) throws Exception
    {
        checkThat(request, response)
                .throwing(BadArgumentException.class)
                .are(notNull());

        return stateWebsites.getSupportedStates()
                            .stream()
                            .map(StateJson::new)
                            .map(StateJson::asJson)
                            .collect(JSON.collectArray());
    }

    static class StateJson
    {
        private final State state;
        private final JsonObject json;

        StateJson(State state)
        {
            checkThat(state).is(notNull());

            this.state = state;
            this.json = toJson();
        }

        private JsonObject toJson()
        {
            JsonObject object = new JsonObject();

            object.addProperty("id", state.getAbbreviation().name());
            object.addProperty("name", state.getTitleCased());

            return object;
        }

        JsonObject asJson()
        {
            return json;
        }
    }
}
