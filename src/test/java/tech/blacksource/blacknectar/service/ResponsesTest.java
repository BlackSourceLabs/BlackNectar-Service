package tech.blacksource.blacknectar.service;/*
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import spark.Response;
import tech.blacksource.blacknectar.service.json.OperationResultJson;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.mockito.Mockito.verify;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class ResponsesTest
{
    @Mock
    private Response response;

    @GenerateString
    private String message;

    private OperationResultJson expected;

    @Before
    public void setUp() throws Exception
    {
        setupData();
    }

    @Test
    public void badArgument() throws Exception
    {
    }


    private void setupData()
    {
        expected = new OperationResultJson(message, false, Responses.StatusCodes.BAD_ARGUMENT);
    }

    private void setupMocks()
    {

    }

    @DontRepeat
    @Test
    public void testCannoInstantiate() throws Exception
    {
        assertThrows(Responses.class::newInstance);
    }

    @Test
    public void testBadArgumentWithBadParameters() throws Exception
    {
        assertThrows(() -> Responses.badArgument("", null));
    }

    @Test
    public void testBadArgument() throws Exception
    {
        Responses.badArgument(message, response);

        verify(response).status(Responses.StatusCodes.BAD_ARGUMENT);
    }
}