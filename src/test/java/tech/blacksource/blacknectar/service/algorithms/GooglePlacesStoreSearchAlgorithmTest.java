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
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.google.places.GooglePlacesAPI;
import tech.redroma.google.places.data.Location;
import tech.redroma.google.places.data.Place;
import tech.redroma.google.places.requests.NearbySearchRequest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Mockito.when;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.blacksource.blacknectar.service.algorithms.GooglePlacesStoreSearchAlgorithm.DEFAULT_RADIUS;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(25)
@RunWith(AlchemyTestRunner.class)
public class GooglePlacesStoreSearchAlgorithmTest
{

    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;

    @Mock
    private GooglePlacesAPI google;

    @Mock
    private StoreMatchingAlgorithm<Place> matchingAlgorithm;

    private GooglePlacesStoreSearchAlgorithm instance;

    private NearbySearchRequest expectedRequest;
    private Store store;

    @GenerateList(Place.class)
    private List<Place> places;

    private Place matchingPlace;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new GooglePlacesStoreSearchAlgorithm(aroma, google, matchingAlgorithm);
    }

    private void setupData() throws Exception
    {
        store = one(stores());
        expectedRequest = createExpectedRequestFor(store);

        matchingPlace = Lists.oneOf(places);
    }

    private void setupMocks() throws Exception
    {
        when(google.simpleSearchNearbyPlaces(expectedRequest))
            .thenReturn(places);

        when(matchingAlgorithm.matchesStore(matchingPlace, store))
            .thenReturn(true);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new GooglePlacesStoreSearchAlgorithm(null, google, matchingAlgorithm));
        assertThrows(() -> new GooglePlacesStoreSearchAlgorithm(aroma, null, matchingAlgorithm));
        assertThrows(() -> new GooglePlacesStoreSearchAlgorithm(aroma, google, null));
    }

    @Test
    public void testFindMatchFor()
    {
        Place result = instance.findMatchFor(store);

        assertThat(result, is(matchingPlace));
    }

    @Test
    public void testFindMatchWhenNoStoresFound() throws Exception
    {
        when(google.simpleSearchNearbyPlaces(expectedRequest))
            .thenReturn(Lists.emptyList());

        Place result = instance.findMatchFor(store);
        assertThat(result, is(nullValue()));
    }

    @Test
    public void testFindMatchForWhenNoMatch() throws Exception
    {
        when(matchingAlgorithm.matchesStore(matchingPlace, store))
            .thenReturn(false);
        
        Place result = instance.findMatchFor(store);
        assertThat(result, is(nullValue()));
    }

    @DontRepeat
    @Test
    public void testFindMatchWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.findMatchFor(null)).isInstanceOf(IllegalArgumentException.class);
    }
    
    private NearbySearchRequest createExpectedRequestFor(Store store)
    {
        Location location = Location.of(store.getLocation().getLatitude(), store.getLocation().getLongitude());

        return NearbySearchRequest.newBuilder()
            .withKeyword(store.getName().toLowerCase())
            .withLocation(location)
            .withRadiusInMeters(DEFAULT_RADIUS)
            .build();
    }

}
