
package tech.blacksource.blacknectar.service.operations.ebt;

import com.google.gson.JsonArray;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.*;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.ebt.balance.StateWebsiteFactory;
import tech.blacksource.blacknectar.service.data.MediaTypes;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.json.EBTJsonSerializer;
import tech.blacksource.blacknectar.service.json.JSON;

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

    private Aroma aroma;
    private final EBTJsonSerializer jsonSerializer;
    private final StateWebsiteFactory stateWebsites;

    @Inject
    GetStatesOperation(Aroma aroma, EBTJsonSerializer jsonSerializer, StateWebsiteFactory stateWebsites)
    {
        checkThat(aroma, jsonSerializer, stateWebsites)
            .are(notNull());

        this.aroma = aroma;
        this.jsonSerializer = jsonSerializer;
        this.stateWebsites = stateWebsites;
    }

    @Override
    public JsonArray handle(Request request, Response response) throws Exception
    {
        checkThat(request, response)
            .throwing(BadArgumentException.class)
            .are(notNull());

        response.type(MediaTypes.APPLICATION_JSON);

        JsonArray results = stateWebsites.getSupportedStates()
                                         .stream()
                                         .map(jsonSerializer::serializeState)
                                         .collect(JSON.collectArray());
        
        makeNoteOfResults(request, results);
        
        return results;
    }

    private void makeNoteOfResults(Request request, JsonArray results)
    {
        String message = "Found {} states for request {} from IP [{}]";
        
        LOG.debug(message, results.size(), request, request.ip());
        
        aroma.sendLowPriorityMessage("Get States Called", message, results.size(), request, request.ip());
    }

}
