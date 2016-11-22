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

package tech.blackhole.blacknectar.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import spark.ExceptionHandler;
import tech.aroma.client.Aroma;
import tech.blackhole.blacknectar.service.api.operations.GetSampleStoreOperation;
import tech.blackhole.blacknectar.service.api.operations.SayHelloOperation;
import tech.blackhole.blacknectar.service.api.operations.SearchStoresOperation;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class ServerTest
{
    
    private Aroma aroma;
    
    @Mock
    private SayHelloOperation sayHelloOperation;
    
    @Mock
    private GetSampleStoreOperation getSampleStoreOperation;
    
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
        
        instance = new Server(aroma, sayHelloOperation, getSampleStoreOperation, searchStoresOperation, exceptionHandler);

    }

    private void setupData() throws Exception
    {
    }

    private void setupMocks() throws Exception
    {
        aroma = Aroma.create();
    }

    @Test
    public void  testConstructor()
    {
        assertThrows(() -> new Server(null, sayHelloOperation, getSampleStoreOperation, searchStoresOperation, exceptionHandler));
        assertThrows(() -> new Server(aroma, null, getSampleStoreOperation, searchStoresOperation, exceptionHandler));
        assertThrows(() -> new Server(aroma, sayHelloOperation, null, searchStoresOperation, exceptionHandler));
        assertThrows(() -> new Server(aroma, sayHelloOperation, getSampleStoreOperation, null, exceptionHandler));
        assertThrows(() -> new Server(aroma, sayHelloOperation, getSampleStoreOperation, searchStoresOperation, null));
    }
}
