package tech.blacksource.blacknectar.service.json;/*
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

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.blacksource.blacknectar.ebt.balance.State;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat
public class StateJsonTest
{
    @GenerateEnum
    private State state;

    private StateJson instance;

    @Before
    public void setUp() throws Exception
    {
        instance = new StateJson(state);
    }

    @Test
    public void asJson() throws Exception
    {
        JsonObject result = instance.asJson();
        assertThat(result, notNullValue());

        String stateId = result.get(StateJson.Keys.STATE_ID).getAsString();
        String stateName = result.get(StateJson.Keys.STATE_NAME).getAsString();

        assertThat(stateId, not(isEmptyOrNullString()));
        assertThat(stateName, not(isEmptyOrNullString()));

        assertThat(stateId, is(state.getAbbreviation().toString()));
        assertThat(stateName, is(state.getTitleCased()));
    }

}