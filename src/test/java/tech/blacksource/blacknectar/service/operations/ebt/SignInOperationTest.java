package tech.blacksource.blacknectar.service.operations.ebt;/*
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

import java.util.List;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.sets.Sets;
import spark.Request;
import spark.Response;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.ebt.balance.*;
import tech.blacksource.blacknectar.service.BlackNectarGenerators;
import tech.blacksource.blacknectar.service.Responses;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.exceptions.UnsupportedStateException;
import tech.blacksource.blacknectar.service.json.EBTJsonSerializer;
import tech.blacksource.blacknectar.service.json.OperationResult;
import tech.blacksource.blacknectar.service.operations.Parameters;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat
public class SignInOperationTest
{

    private Aroma aroma;

    @Mock
    private Request request;

    @Mock
    private Response response;

    @Mock
    private EBTJsonSerializer jsonSerializer;

    @Mock
    private StateWebsiteFactory websiteFactory;

    @Mock
    private StateWebsite stateWebsite;

    @GenerateString
    private String requestBody;

    private List<FieldValue> fieldValues;

    @GenerateEnum
    private State state;

    private SignInOperation instance;

    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();

        instance = new SignInOperation(aroma, jsonSerializer, websiteFactory);
    }


    private void setupData()
    {
        fieldValues  = listOf(BlackNectarGenerators.fieldValues(), 5);
    }

    private void setupMocks()
    {
        aroma = Aroma.createNoOpInstance();

        when(request.params(Parameters.EBT.STATE))
                .thenReturn(state.getAbbreviation().toString());

        when(websiteFactory.getSupportedStates())
                .thenReturn(Sets.createFrom(state));

        when(websiteFactory.getConnectionToState(state))
                .thenReturn(stateWebsite);

        when(stateWebsite.isSignedIn()).thenReturn(true);

        when(request.body()).thenReturn(requestBody);

        when(jsonSerializer.deserializeFieldValues(requestBody))
                .thenReturn(fieldValues);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new SignInOperation(null, jsonSerializer, websiteFactory));
        assertThrows(() -> new SignInOperation(aroma, null, websiteFactory));
        assertThrows(() -> new SignInOperation(aroma, jsonSerializer, null));
    }

    @Test
    public void handle() throws Exception
    {
        JsonObject response = instance.handle(request, this.response);

        OperationResult result = OperationResult.fromJson(response);
        assertThat(result, notNullValue());
        assertThat(result.isSuccess(), is(true));
        assertThat(result.getStatusCode(), is(Responses.StatusCodes.OK));

        verify(websiteFactory).getConnectionToState(state);
        verify(websiteFactory).getSupportedStates();
        verify(jsonSerializer).deserializeFieldValues(requestBody);

        verify(stateWebsite).signIn(new Account(Sets.copyOf(fieldValues)));
    }

    @Test
    public void testWhenStateFailsToSignIn() throws Exception
    {
        when(stateWebsite.isSignedIn())
                .thenReturn(false);

        JsonObject jsonResponse = instance.handle(request, response);
        OperationResult response = OperationResult.fromJson(jsonResponse);

        assertThat(response, notNullValue());
        assertThat(response.isSuccess(), is(false));
    }

    @Test
    public void testWhenNoFieldValuesFound() throws Exception
    {
        when(jsonSerializer.deserializeFieldValues(requestBody))
                .thenReturn(Lists.emptyList());

        JsonObject jsonReponse = instance.handle(request, this.response);
        OperationResult response = OperationResult.fromJson(jsonReponse);
        assertThat(response, notNullValue());
        assertThat(response.isSuccess(), is(false));
        assertThat(response.getStatusCode(), is(Responses.StatusCodes.BAD_ARGUMENT));

    }

    @Test
    public void testWhenStateParameterInvalid() throws Exception
    {
        String state = one(alphabeticString());

        when(request.params(Parameters.EBT.STATE))
                .thenReturn(state);

        assertThrows(() -> instance.handle(request, response))
                .isInstanceOf(BadArgumentException.class);
    }

    @DontRepeat
    @Test
    public void testWhenStateParameterMissing() throws Exception
    {
        when(request.params(Parameters.EBT.STATE))
                .thenReturn("");

        assertThrows(() -> instance.handle(request, response))
                .isInstanceOf(BadArgumentException.class);
    }

    @DontRepeat
    @Test
    public void testHandleWhenStateNotSupported() throws Exception
    {
        when(websiteFactory.getSupportedStates())
                .thenReturn(Sets.emptySet());

        assertThrows(() -> instance.handle(request, response))
                .isInstanceOf(UnsupportedStateException.class);
    }

    @DontRepeat
    @Test
    public void testHandleWithNullArgs() throws Exception
    {
        assertThrows(() -> instance.handle(null, response)).isInstanceOf(BadArgumentException.class);
        assertThrows(() -> instance.handle(request, null)).isInstanceOf(BadArgumentException.class);
    }
}