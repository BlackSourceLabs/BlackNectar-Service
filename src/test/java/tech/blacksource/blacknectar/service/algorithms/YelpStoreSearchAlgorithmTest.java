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

package tech.blacksource.blacknectar.service.algorithms;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.stores.Location;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.yelp.Address;
import tech.redroma.yelp.Coordinate;
import tech.redroma.yelp.YelpAPI;
import tech.redroma.yelp.YelpBusiness;
import tech.redroma.yelp.YelpSearchRequest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Mockito.when;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.blacksource.blacknectar.service.algorithms.YelpStoreSearchAlgorithm.DEFAULT_YELP_LIMIT;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class YelpStoreSearchAlgorithmTest
{
    
    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;
    
    @Mock
    private StoreMatchingAlgorithm<YelpBusiness> matchingAlgorithm;
    
    @Mock
    private YelpAPI yelp;
    
    private YelpStoreSearchAlgorithm instance;
    
    @GenerateList(YelpBusiness.class)
    private List<YelpBusiness> yelpBusinesses;
    
    private YelpBusiness matchingBusiness;
    
    private Store store;
    
    private YelpSearchRequest expectedRequest;
    
    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        
        instance = new YelpStoreSearchAlgorithm(aroma, matchingAlgorithm, yelp);
    }
    
    private void setupData() throws Exception
    {
        store = one(stores());
        matchingBusiness = Lists.oneOf(yelpBusinesses);
        expectedRequest = createExpectedYelpRequestFor(store);
    }
    
    private void setupMocks() throws Exception
    {
        when(yelp.searchForBusinesses(expectedRequest)).thenReturn(yelpBusinesses);
        
        when(matchingAlgorithm.matchesStore(matchingBusiness, store))
            .thenReturn(true);
    }
    
    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new YelpStoreSearchAlgorithm(null, matchingAlgorithm, yelp));
        assertThrows(() -> new YelpStoreSearchAlgorithm(aroma, null, yelp));
        assertThrows(() -> new YelpStoreSearchAlgorithm(aroma, matchingAlgorithm, null));
        
    }
    
    @Test
    public void testFindMatchFor()
    {
        YelpBusiness result = instance.findMatchFor(store);
        
        assertThat(result, is(matchingBusiness));
    }
    
    @Test
    public void testFindMatchForWhenNoBusinessResults() throws Exception
    {
        when(yelp.searchForBusinesses(expectedRequest))
            .thenReturn(Lists.emptyList());
        
        YelpBusiness result = instance.findMatchFor(store);
        assertThat(result, is(nullValue()));
    }
    
    @Test
    public void testFindMatchForWhenNoMatchesFound() throws Exception
    {
        when(matchingAlgorithm.matchesStore(matchingBusiness, store))
            .thenReturn(false);
        
        YelpBusiness result = instance.findMatchFor(store);
        assertThat(result, is(nullValue()));
    }
    
    @Test
    public void testWhenMultipleBusinessesMatch() throws Exception
    {
        YelpBusiness secondBusinesses = Lists.oneOf(yelpBusinesses);

        when(matchingAlgorithm.matchesStore(secondBusinesses, store))
            .thenReturn(true);

        YelpBusiness result = instance.findMatchFor(store);

        assertThat(result, notNullValue());
        assertThat(result, either(is(matchingBusiness)).or(is(secondBusinesses)));
    }
    
    @DontRepeat
    @Test
    public void testFindMatchForWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.findMatchFor(null)).isInstanceOf(IllegalArgumentException.class);
    }
    
    private YelpSearchRequest createExpectedYelpRequestFor(Store store)
    {
        Address yelpAddress = copyAddressFrom(store.getAddress());
        Coordinate coordinate = copyCoordinateFrom(store.getLocation());
        
        return YelpSearchRequest.newBuilder()
            .withCoordinate(coordinate)
            .withLimit(DEFAULT_YELP_LIMIT)
            .withSearchTerm(store.getName())
            .withSortBy(YelpSearchRequest.SortType.DISTANCE)
            .build();
    }
    
    private Address copyAddressFrom(tech.blacksource.blacknectar.service.stores.Address address)
    {
        Address yelpAddress = new Address();
        
        yelpAddress.address1 = address.getAddressLineOne();
        yelpAddress.address2 = address.getAddressLineTwo();
        yelpAddress.city = address.getCity();
        yelpAddress.state = address.getState();
        yelpAddress.zipCode = String.valueOf(address.getZipCode());
        
        return yelpAddress;
    }
    
    private Coordinate copyCoordinateFrom(Location location)
    {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        
        return Coordinate.of(lat, lon);
    }
    
}
