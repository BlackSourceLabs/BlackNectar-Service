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

package tech.blacksource.blacknectar.service.json;

import java.util.Objects;

import com.google.gson.JsonObject;
import tech.blacksource.blacknectar.ebt.balance.State;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * @author SirWellington
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

        object.addProperty(Keys.STATE_ID, state.getAbbreviation().name());
        object.addProperty(Keys.STATE_NAME, state.getTitleCased());

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
                '}';
    }

    static class Keys
    {
        static final String STATE_ID = "id";
        static final String STATE_NAME = "name";
    }
}
