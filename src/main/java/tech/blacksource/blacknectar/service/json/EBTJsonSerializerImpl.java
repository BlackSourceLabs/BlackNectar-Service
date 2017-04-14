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
package tech.blacksource.blacknectar.service.json;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.blacksource.blacknectar.ebt.balance.State;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * @author SirWellington
 */
final class EBTJsonSerializerImpl implements EBTJsonSerializer
{
    private final static Logger LOG = LoggerFactory.getLogger(EBTJsonSerializerImpl.class);

    @Override
    public JsonObject serializeState(State state)
    {
        checkThat(state)
            .usingMessage("State cannot be null")
            .is(notNull());
        
        final StateJson stateJson = new StateJson(state);
        final JsonObject result = stateJson.asJson();
        
        LOG.debug("Serialized {} to {}", state, result);
        
        return result;
    }
}
