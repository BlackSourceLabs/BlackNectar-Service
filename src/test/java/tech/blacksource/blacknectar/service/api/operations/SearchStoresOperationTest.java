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

package tech.blacksource.blacknectar.service.api.operations;

import com.google.gson.JsonArray;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.api.BlackNectarSearchRequest;
import tech.blacksource.blacknectar.service.api.BlackNectarService;
import tech.blacksource.blacknectar.service.api.operations.SearchStoresOperation.QueryKeys;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.stores.Location;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateInteger;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sir.wellington.alchemy.collections.sets.Sets.toSet;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.GeolocationGenerators.latitudes;
import static tech.sirwellington.alchemy.generator.GeolocationGenerators.longitudes;
import static tech.sirwellington.alchemy.generator.NetworkGenerators.ip4Addresses;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateInteger.Type.POSITIVE;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHANUMERIC;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class SearchStoresOperationTest 
{
    private Aroma aroma;
    
    @Mock
    private BlackNectarService service;
    
    @Mock
    private Request request;
    
    @Mock
    private Response response;
    
    @GenerateString(ALPHANUMERIC)
    private String queryString;
    
    private String ip;
    
    private SearchStoresOperation instance;
    
    private double latitude;
    private double longitude;
   
    @GenerateString
    private String searchTerm;
   
    @GenerateInteger(POSITIVE)
    private Integer radius;
   
    @GenerateInteger
    private Integer limit;
    
    private QueryParamsMap queryParams;
    
    private List<Store> stores;
    
    private BlackNectarSearchRequest expectedSearchRequest;

    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        
        instance = new SearchStoresOperation(aroma, service);
    }


    private void setupData() throws Exception
    {
        ip = one(ip4Addresses());
        latitude = one(latitudes());
        longitude = one(longitudes());
        
        stores = listOf(stores());
        expectedSearchRequest = createExpectedRequest();
    }

    private void setupMocks() throws Exception
    {
        aroma = Aroma.create();

        queryParams = createQueryParams();
        
        when(request.ip()).thenReturn(ip);
        when(request.queryString()).thenReturn(queryString);
        when(request.queryMap()).thenReturn(queryParams);

        
        when(service.searchForStores(expectedSearchRequest))
            .thenReturn(stores);
    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new SearchStoresOperation(null, service));
        assertThrows(() -> new SearchStoresOperation(aroma, null));
    }
    
    @Test
    public void testHandle() throws Exception
    {
        JsonArray array = instance.handle(request, response);
        
        JsonArray expected = new JsonArray();
        stores.stream()
            .map(Store::asJSON)
            .forEach(store -> expected.add(store));

        assertThat(array, is(expected));
            
    }

    @DontRepeat
    @Test
    public void testHandleWithBadArguments() throws Exception
    {
        assertThrows(() -> instance.handle(request, null)).isInstanceOf(BadArgumentException.class);
        assertThrows(() -> instance.handle(null, response)).isInstanceOf(BadArgumentException.class);
    }
    
    @DontRepeat
    @Test
    public void testHandleWithUnrecognizedQueryKeys() throws Exception
    {
        Set<String> params = toSet(listOf(alphabeticString()));
        when(request.queryParams()).thenReturn(params);
        
        assertThrows(() -> instance.handle(request, response))
            .isInstanceOf(BadArgumentException.class);
    }

    private BlackNectarSearchRequest createExpectedRequest()
    {
        BlackNectarSearchRequest request = new BlackNectarSearchRequest();
        
        request.withCenter(Location.with(latitude, longitude))
            .withLimit(limit)
            .withRadius(radius)
            .withSearchTerm(searchTerm);
        
        return request;
    }

    private QueryParamsMap createQueryParams()
    {
        QueryParamsMap params = mock(QueryParamsMap.class);
        
        when(params.hasKey(anyString())).thenReturn(true);
        
        when(params.value(QueryKeys.LATITUDE)).thenReturn(String.valueOf(latitude));
        when(params.value(QueryKeys.LONGITUDE)).thenReturn(String.valueOf(longitude));
        when(params.value(QueryKeys.RADIUS)).thenReturn(radius.toString());
        when(params.value(QueryKeys.LIMIT)).thenReturn(limit.toString());
        when(params.value(QueryKeys.SEARCH_TERM)).thenReturn(searchTerm);
        
        return params;
    }
    
}