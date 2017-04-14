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

import java.util.*;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.sets.Sets;
import spark.Request;
import spark.Response;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.ebt.balance.State;
import tech.blacksource.blacknectar.ebt.balance.StateWebsiteFactory;
import tech.blacksource.blacknectar.service.data.MediaTypes;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.json.EBTJsonSerializer;
import tech.sirwellington.alchemy.generator.*;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.jsonObjects;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

@RunWith(AlchemyTestRunner.class)
@Repeat(25)
public class GetStatesOperationTest
{

    private Aroma aroma;
    
    @Mock
    private EBTJsonSerializer jsonSerializer;
    
    @Mock
    private StateWebsiteFactory websiteFactory;

    private Set<State> states;
    
    private Map<State, JsonObject> jsonMap;
    
    private JsonArray expected;

    @Mock
    private Request request;

    @Mock
    private Response response;

    private GetStatesOperation instance;

    @Before
    public void setUp()
    {
        setupData();
        setupMocks();

        instance = new GetStatesOperation(aroma, jsonSerializer, websiteFactory);
    }

    private void setupData()
    {
        AlchemyGenerator<State> stateGenerator = EnumGenerators.enumValueOf(State.class);
        List<State> statesList = CollectionGenerators.listOf(stateGenerator, 10);
        this.states = Sets.copyOf(statesList);
        
        this.jsonMap = Maps.newLinkedHashMap();
        
        this.states.forEach(s -> jsonMap.put(s, one(jsonObjects())));
    }

    private void setupMocks()
    {
        aroma = Aroma.createNoOpInstance();

        when(websiteFactory.getSupportedStates()).thenReturn(states);
        
        jsonMap.forEach((state, json) -> when(jsonSerializer.serializeState(state)).thenReturn(json));
    }


    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new GetStatesOperation(null, jsonSerializer, websiteFactory));
        assertThrows(() -> new GetStatesOperation(aroma, null, websiteFactory));
        assertThrows(() -> new GetStatesOperation(aroma, jsonSerializer, null));
    }

    @Test
    public void handle() throws Exception
    {
        JsonArray result = instance.handle(request, response);
        assertThat(result, notNullValue());
        assertThat(result.size(), greaterThan(0));
        assertThat(result.size(), is(states.size()));
        
        jsonMap.values().forEach(json -> assertTrue(result.contains(json)));

        verify(websiteFactory, never()).getConnectionToState(any());
        verify(response).type(MediaTypes.APPLICATION_JSON);
    }
    
    @Test
    public void testHandleWhenNoStatesSupported() throws Exception
    {
        when(websiteFactory.getSupportedStates()).thenReturn(Sets.emptySet());
        
        JsonArray result = instance.handle(request, response);
        assertThat(result, notNullValue());
        assertThat(result.size(), is(0));
    }

    @Test
    public void testHandleWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.handle(null, response)).isInstanceOf(BadArgumentException.class);
        assertThrows(() -> instance.handle(request, null)).isInstanceOf(BadArgumentException.class);
    }

}