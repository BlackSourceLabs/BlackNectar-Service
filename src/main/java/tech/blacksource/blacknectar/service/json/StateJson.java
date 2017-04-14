package tech.blacksource.blacknectar.service.json;

import java.util.Objects;

import com.google.gson.JsonObject;
import tech.blacksource.blacknectar.ebt.balance.State;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * Created by Commander on 4/14/2017.
 */
final class StateJson
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

        object.addProperty("id", state.getAbbreviation()
                                      .name());
        object.addProperty("name", state.getTitleCased());

        return object;
    }

    JsonObject asJson()
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


        StateJson stateJson = (StateJson) o;
        return state == stateJson.state &&
                Objects.equals(json, stateJson.json);
    }

    @Override
    public int hashCode()
    {
        int result = state.hashCode();
        result = 31 * result + (json != null ? json.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "StateJson{" +
                "state=" + state +
                ", json=" + json +
                '}';
    }
}
