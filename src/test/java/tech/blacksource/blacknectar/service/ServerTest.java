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
import tech.blacksource.blacknectar.service.operations.GetSampleStoreOperation;
import tech.blacksource.blacknectar.service.operations.SayHelloOperation;
import tech.blacksource.blacknectar.service.operations.ebt.GetStatesOperation;
import tech.blacksource.blacknectar.service.operations.stores.SearchStoresOperation;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.mockito.Answers.RETURNS_MOCKS;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

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
    private SayHelloOperation sayHelloOperation;
    
    @Mock
    private GetSampleStoreOperation getSampleStoreOperation;

    @Mock
    private GetStatesOperation getStatesOperation;
    
    @Mock
    private SearchStoresOperation searchStoresOperation;

    @Mock
    private ExceptionHandler exceptionHandler;

    private Server instance;

    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();
        
        instance = new Server(aroma, sayHelloOperation, getSampleStoreOperation, getStatesOperation, searchStoresOperation, exceptionHandler);

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
        assertThrows(() -> new Server(null, sayHelloOperation, getSampleStoreOperation, getStatesOperation, searchStoresOperation, exceptionHandler));
        assertThrows(() -> new Server(aroma, null, getSampleStoreOperation, getStatesOperation, searchStoresOperation, exceptionHandler));
        assertThrows(() -> new Server(aroma, sayHelloOperation, null,getStatesOperation,  searchStoresOperation, exceptionHandler));
        assertThrows(() -> new Server(aroma, sayHelloOperation, getSampleStoreOperation, null, searchStoresOperation, exceptionHandler));
        assertThrows(() -> new Server(aroma, sayHelloOperation, getSampleStoreOperation, getStatesOperation, null, exceptionHandler));
        assertThrows(() -> new Server(aroma, sayHelloOperation, getSampleStoreOperation, getStatesOperation, searchStoresOperation, null));
    }
}
