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

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.blacksource.blacknectar.ebt.balance.State;
import tech.blacksource.blacknectar.ebt.balance.StateWebsiteFactory;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 * @author SirWellington
 */
@Repeat
@RunWith(AlchemyTestRunner.class)
public class BlackNectarAssertionsTest
{
    @Mock
    private StateWebsiteFactory websiteFactory;

    @GenerateString
    private String normalString;

    @GenerateString(length = BlackNectarAssertions.MAX_QUERY_PARAMETER_ARGUMENT_LENGTH * 2)
    private String longString;

    @GenerateEnum
    private State state;

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
        when(websiteFactory.getSupportedStates())
                .thenReturn(Sets.copyOf(Arrays.asList(State.values())));

    }


    @DontRepeat
    @Test
    public void testCannotInstantiate() throws Exception
    {
        assertThrows(BlackNectarAssertions.class::newInstance);
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

    @Test
    public void testSupportedState() throws Exception
    {
        AlchemyAssertion<State> assertion = BlackNectarAssertions.supportedState(websiteFactory);
        assertThat(assertion, notNullValue());
        assertion.check(state);
    }

    @DontRepeat
    @Test
    public void testSupportedStateWithUnsupportedState() throws Exception
    {
        when(websiteFactory.getSupportedStates()).thenReturn(Sets.emptySet());

        AlchemyAssertion<State> assertion = BlackNectarAssertions.supportedState(websiteFactory);
        assertThat(assertion, notNullValue());

        assertThrows(() -> assertion.check(state)).isInstanceOf(FailedAssertionException.class);
    }

    @DontRepeat
    @Test
    public void testSupportedStateWithBadArgs() throws Exception
    {
        assertThrows(() -> BlackNectarAssertions.supportedState(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}