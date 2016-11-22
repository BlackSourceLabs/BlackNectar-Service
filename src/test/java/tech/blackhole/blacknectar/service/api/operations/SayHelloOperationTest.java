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

package tech.blackhole.blacknectar.service.api.operations;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import spark.Request;
import spark.Response;
import tech.aroma.client.Aroma;
import tech.blackhole.blacknectar.service.exceptions.BadArgumentException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class SayHelloOperationTest 
{
    
    @Mock
    private Aroma aroma;
    
    @Mock
    private Request request;
    
    @Mock
    private Response response;
    
    private SayHelloOperation instance;

    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        instance = new SayHelloOperation(aroma);
    }


    private void setupData() throws Exception
    {
        
    }

    private void setupMocks() throws Exception
    {
        aroma = Aroma.create();
    }
    
    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new SayHelloOperation(null));
    }

    @Test
    public void testHandle() throws Exception
    {
        String result = instance.handle(request, response);
        assertThat(result, notNullValue());
        
        assertThat(result, not(isEmptyOrNullString()));
    }
    
    @DontRepeat
    @Test
    public void testHandleWithBadArguments() throws Exception
    {
        assertThrows(() -> instance.handle(null, response)).isInstanceOf(BadArgumentException.class);
        assertThrows(() -> instance.handle(request, null)).isInstanceOf(BadArgumentException.class);
    }

}