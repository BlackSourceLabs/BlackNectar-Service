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
import org.mockito.Mock;
import spark.ExceptionHandler;
import tech.aroma.client.Aroma;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class ServerTest
{
    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;

    @Mock
    private ExceptionHandler exceptionHandler;

    @Mock
    private Routes routes;

    private Server instance;

    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();
        
        instance = new Server(aroma, exceptionHandler, routes);

    }

    private void setupData() throws Exception
    {
    }

    private void setupMocks() throws Exception
    {
    }

    @Test
    public void  testConstructor()
    {
        assertThrows(() -> new Server(null, exceptionHandler, routes)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> new Server(aroma, null, routes)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> new Server(aroma, exceptionHandler, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testSetupNonSecure() throws Exception
    {
        instance.setupNonSecureServer();

        verify(routes, atLeastOnce()).setupRoutes(any());
    }
}
