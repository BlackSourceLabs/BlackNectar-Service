package tech.blacksource.blacknectar.service.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import tech.blacksource.blacknectar.ebt.balance.State;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * Contains the Path Parameters used for the REST Service.
 */
public final class Parameters
{

    /**
     * Parameters specifically used by the EBT API.
     */
    public static final class EBT
    {
        private static final Logger LOG = LoggerFactory.getLogger(EBT.class);

        public static final String STATE = ":state";

        public static State getStateFrom(@Required Request request) throws BadArgumentException
        {
            String stateParameter = request.params(STATE);

            checkThat(stateParameter)
                    .throwing(BadArgumentException.class)
                    .usingMessage("State parameter missing")
                    .is(nonEmptyString());

            State state = State.fromAbbreviatedText(stateParameter);

            if (state == null)
            {
                makeNoteOfUnknownStateAbbreviation(stateParameter);
                state = State.fromText(stateParameter);
            }

            checkThat(state)
                    .throwing(BadArgumentException.class)
                    .usingMessage("Unknown State: " + stateParameter)
                    .is(notNull());

            return state;
        }

        private static void makeNoteOfUnknownStateAbbreviation(String stateParameter)
        {
            LOG.info("Unknown state abbreviation: {}, trying to parse as full-name", stateParameter);
        }
    }
}
