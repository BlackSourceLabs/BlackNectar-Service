/*
 * Copyright 2016 BlackWholeLabs.
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

package tech.blacksource.blacknectar.service.exceptions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import spark.Request;
import spark.Response;
import tech.aroma.client.Aroma;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;


/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class BlackNectarExceptionHandlerTest 
{
    @Mock
    private Aroma aroma;
    @Mock
    private Aroma.Request request;
    
    private BlackNectarExceptionHandler instance;
    
    @Mock
    private Request httpRequest;
    @Mock
    private Response httpResponse;
    
    @Captor
    private ArgumentCaptor<String> messageCaptor;

    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();
        instance = new BlackNectarExceptionHandler(aroma);
    }


    private void setupData() throws Exception
    {
        
    }

    private void setupMocks() throws Exception
    {
        when(aroma.begin()).thenReturn(request);
        when(request.titled(anyString())).thenReturn(request);
        when(request.text(anyString(), anyVararg())).thenReturn(request);
        when(request.withUrgency(anyObject())).thenReturn(request);
    }

    @Test
    public void testHandleRuntime()
    {
        RuntimeException ex = new RuntimeException();
        instance.handle(ex, httpRequest, httpResponse);
        testWithException(ex);
    }
    
    @Test
    public void testHandleBadArgument()
    {
        String message = one(alphabeticString());
        BadArgumentException ex = new BadArgumentException(message);
        testWithException(ex);
    }
    
    @Test
    public void testHandleOperationFailed()
    {
        String message = one(alphabeticString());
        OperationFailedException ex = new OperationFailedException(message);
        testWithException(ex);
    }

    private void testWithException(Exception ex)
    {
        instance.handle(ex, httpRequest, httpResponse);
        
        verify(aroma, atLeastOnce()).begin();
        verify(request, atLeastOnce()).send();
        verify(request, atLeastOnce()).text(anyString(), anyObject(), eq(ex));
    }
}