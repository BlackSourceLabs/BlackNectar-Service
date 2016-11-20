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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import tech.blackhole.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.generator.NumberGenerators;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class ServerTest 
{

    private Server instance;
    @Mock
    private Request request;
    @Mock
    private Response response;
    
    private String ip;
    
    @Before
    public void setUp() throws Exception
    {
    
        instance = new Server();
        setupData();
        setupMocks();
    }


    private void setupData() throws Exception
    {
        int number = one(NumberGenerators.positiveIntegers());
        ip = String.valueOf(number);
    }

    private void setupMocks() throws Exception
    {
        when(request.ip()).thenReturn(ip);
        when(request.queryMap()).thenReturn(mock(QueryParamsMap.class));
    }

    @Test
    public void testSayHello()
    {
        instance.sayHello(request, response);
    }

    @Test
    public void testGetSampleStore()
    {
        JsonArray stores = instance.getSampleStore(request, response);
        assertThat(stores.size(), greaterThan(0));
        
        JsonElement store = stores.get(0);
        assertThat(store, is(Store.SAMPLE_STORE.asJSON()));
    }

    @Test
    public void testSearchStores()
    {
        JsonArray json = instance.searchStores(request, response);
        assertThat(json, notNullValue());
        assertThat(json.size(), greaterThanOrEqualTo(1_000));
    }

}