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

package tech.blacksource.blacknectar.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class BlackNectarAssertionsTest 
{
    
    @GenerateString
    private String normalString;
    
    @GenerateString(length = BlackNectarAssertions.MAX_QUERY_PARAMETER_ARGUMENT_LENGTH * 2)
    private String longString;

    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
    }


    private void setupData() throws Exception
    {
        
    }

    private void setupMocks() throws Exception
    {
        
    }
    
    @DontRepeat
    @Test
    public void testCannotInstantiate() throws Exception
    {
        assertThrows(() -> BlackNectarAssertions.class.newInstance());
    }
    
    
    @Test
    public void testArgumentWithSaneLength() throws Exception
    {
        AlchemyAssertion<String> assertion = BlackNectarAssertions.argumentWithSaneLength();
        assertThat(assertion, notNullValue());
        
        assertion.check(normalString);
        
        assertThrows(() -> assertion.check(longString)).isInstanceOf(FailedAssertionException.class);
        
    }
    
    @DontRepeat
    @Test
    public void testArgumentWithSaneLengthWithEmptyArgs() throws Exception
    {
        AlchemyAssertion<String> assertion = BlackNectarAssertions.argumentWithSaneLength();
        //These checks should not cause an exception.
        assertion.check("");
        assertion.check(null);
    }
    

}