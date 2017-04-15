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

package tech.blacksource.blacknectar.service.operations.stores;

import java.util.*;

import com.google.gson.JsonArray;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import sir.wellington.alchemy.collections.sets.Sets;
import spark.*;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.data.*;
import tech.blacksource.blacknectar.service.exceptions.*;
import tech.blacksource.blacknectar.service.images.Image;
import tech.blacksource.blacknectar.service.images.ImageRepository;
import tech.blacksource.blacknectar.service.json.JsonCollectors;
import tech.blacksource.blacknectar.service.operations.stores.SearchStoresOperation.QueryKeys;
import tech.blacksource.blacknectar.service.stores.Location;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static sir.wellington.alchemy.collections.sets.Sets.toSet;
import static tech.blacksource.blacknectar.service.BlackNectarAssertions.MAX_QUERY_PARAMETER_ARGUMENT_LENGTH;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.images;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.blacksource.blacknectar.service.json.JsonCollectors.collectArray;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.GeolocationGenerators.latitudes;
import static tech.sirwellington.alchemy.generator.GeolocationGenerators.longitudes;
import static tech.sirwellington.alchemy.generator.NetworkGenerators.ip4Addresses;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateInteger.Type.RANGE;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHANUMERIC;

/**
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class SearchStoresOperationTest
{

    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;

    @Mock
    private StoreRepository storesRepository;

    @Mock
    private ImageRepository imageRepository;

    private List<Store> stores;

    private List<Store> storesWithoutImages;

    private Map<Store, Image> images;

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

    @GenerateInteger(value = RANGE, min = 10, max = 1_000)
    private Integer radius;

    @GenerateInteger
    private Integer limit;

    private QueryParamsMap queryParams;

    @GenerateString(length = MAX_QUERY_PARAMETER_ARGUMENT_LENGTH * 2)
    private String reallyLongString;

    private BlackNectarSearchRequest expectedSearchRequest;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new SearchStoresOperation(aroma, storesRepository);
    }

    private void setupData() throws Exception
    {
        stores = listOf(stores());
        storesWithoutImages = withoutImages(stores);

        ip = one(ip4Addresses());
        latitude = one(latitudes());
        longitude = one(longitudes());

        expectedSearchRequest = createExpectedRequest();
        images = Maps.create();
    }

    private void setupMocks() throws Exception
    {

        queryParams = createQueryParams();

        when(request.ip()).thenReturn(ip);
        when(request.queryString()).thenReturn(queryString);
        when(request.queryMap()).thenReturn(queryParams);
        when(request.queryParams()).thenReturn(Sets.copyOf(QueryKeys.KEYS));

        when(storesRepository.searchForStores(expectedSearchRequest)).thenReturn(stores);

        stores.stream().forEach((store) ->
        {
            Image image = images().get();
            images.put(store, image);

            when(imageRepository.getImagesForStore(store))
                .thenReturn(Lists.createFrom(image));
        });

    }

    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new SearchStoresOperation(null, storesRepository));
        assertThrows(() -> new SearchStoresOperation(aroma, null));
    }

    @Test
    public void testHandleWhenHasImage() throws Exception
    {
        JsonArray array = instance.handle(request, response);

        JsonArray expected = stores.stream()
                                   .map(Store::asJSON)
                                   .collect(JsonCollectors.collectArray());

        assertThat(array, is(expected));

        verify(response).type(MediaTypes.APPLICATION_JSON);
    }

    @Test
    public void testWhenHaveNoImage() throws Exception
    {
        when(storesRepository.searchForStores(expectedSearchRequest))
                .thenReturn(storesWithoutImages);

        JsonArray expectedResponse = storesWithoutImages.stream()
                                                        .map(Store::asJSON)
                                                        .collect(collectArray());

        JsonArray jsonResponse = instance.handle(request, response);

        assertThat(jsonResponse, is(expectedResponse));
        verify(response).type(MediaTypes.APPLICATION_JSON);

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

    @Ignore
    @DontRepeat
    @Test
    public void testWhenImageRepositoryFails() throws Exception
    {
        when(imageRepository.getImagesForStore(any(Store.class)))
                .thenThrow(new OperationFailedException());

        assertThrows(() -> instance.handle(request, response))
                .isInstanceOf(BlackNectarAPIException.class);
    }

    @Test
    public void testWithInsanelyLongQueryParameterKey() throws Exception
    {
        Set<String> queryParamKeys = request.queryParams();
        queryParamKeys.add(reallyLongString);
        when(request.queryParams()).thenReturn(queryParamKeys);

        assertThrows(() -> instance.handle(request, response)).isInstanceOf(BadArgumentException.class);
    }

    @Test
    public void testWithInsanelyLongQueryParameterValue() throws Exception
    {
        when(queryParams.value(QueryKeys.SEARCH_TERM)).thenReturn(reallyLongString);

        assertThrows(() -> instance.handle(request, response))
                .isInstanceOf(BadArgumentException.class);
    }

    private BlackNectarSearchRequest createExpectedRequest()
    {
        BlackNectarSearchRequest expectedRequest = new BlackNectarSearchRequest();

        expectedRequest.withCenter(Location.with(latitude, longitude))
                       .withLimit(limit)
                       .withRadius(radius)
                       .withSearchTerm(searchTerm);

        return expectedRequest;
    }

    private QueryParamsMap createQueryParams()
    {
        QueryParamsMap params = mock(QueryParamsMap.class);

        when(params.hasKey(QueryKeys.LATITUDE)).thenReturn(true);
        when(params.hasKey(QueryKeys.LONGITUDE)).thenReturn(true);
        when(params.hasKey(QueryKeys.RADIUS)).thenReturn(true);
        when(params.hasKey(QueryKeys.LIMIT)).thenReturn(true);
        when(params.hasKey(QueryKeys.SEARCH_TERM)).thenReturn(true);

        when(params.value(QueryKeys.LATITUDE)).thenReturn(String.valueOf(latitude));
        when(params.value(QueryKeys.LONGITUDE)).thenReturn(String.valueOf(longitude));
        when(params.value(QueryKeys.RADIUS)).thenReturn(radius.toString());
        when(params.value(QueryKeys.LIMIT)).thenReturn(limit.toString());
        when(params.value(QueryKeys.SEARCH_TERM)).thenReturn(searchTerm);

        when(params.value(QueryKeys.LATITUDE)).thenReturn(String.valueOf(latitude));
        when(params.value(QueryKeys.LONGITUDE)).thenReturn(String.valueOf(longitude));
        when(params.value(QueryKeys.RADIUS)).thenReturn(radius.toString());
        when(params.value(QueryKeys.LIMIT)).thenReturn(limit.toString());
        when(params.value(QueryKeys.SEARCH_TERM)).thenReturn(searchTerm);


        return params;
    }

    private List<Store> withoutImages(List<Store> stores)
    {
        return stores.stream()
                     .map(s -> Store.Builder.fromStore(s).withoutMainImageURL().build())
                     .collect(toList());
    }

}
