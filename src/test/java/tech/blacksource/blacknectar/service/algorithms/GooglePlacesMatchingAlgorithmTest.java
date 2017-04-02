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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.stores.Address;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.google.places.data.Place;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Answers.RETURNS_MOCKS;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.BooleanGenerators.booleans;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class GooglePlacesMatchingAlgorithmTest
{

    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;

    @GenerateString
    private String storeName;

    private Store store;

    @GeneratePojo
    private Place randomPlace;

    @GeneratePojo
    private Place place;

    private GooglePlacesMatchingAlgorithm instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new GooglePlacesMatchingAlgorithm(aroma);
    }

    private void setupData() throws Exception
    {
        store = one(stores());
    }

    private void setupMocks() throws Exception
    {

    }

    @Test
    public void testMatchesStoreWhenNameMatch() throws Exception
    {
        updatePlaceToMatchStoreName(place, store);

        boolean result = instance.matchesStore(place, store);
        assertTrue(result);
    }

    @Test
    public void testMatchesStoreWhenAddressMatch() throws Exception
    {
        updatePlaceToMatchStoreAddress(place, store);

        boolean result = instance.matchesStore(place, store);
        assertTrue(result);
    }

    @Test
    public void testMatchesStoreWhenNoMatch()
    {
        boolean result = instance.matchesStore(randomPlace, store);
        assertFalse(result);
    }

    @Test
    public void testmatchesStoreWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.matchesStore(null, store)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> instance.matchesStore(place, null)).isInstanceOf(IllegalArgumentException.class);
    }

    private void updatePlaceToMatchStoreName(Place place, Store store)
    {
        boolean shouldEqual = one(booleans());

        if (shouldEqual)
        {
            place.name = store.getName();
        }
        else
        {
            place.name += store.getName();
        }
    }

    private void updatePlaceToMatchStoreAddress(Place place, Store store)
    {
        Address storeAddress = store.getAddress();
        boolean useVicinity = one(booleans());

        if (useVicinity)
        {
            place.vicinity = storeAddress.getAddressLineOne();
        }

        else
        {
            place.formattedAddress = storeAddress.getAddressLineOne() + " " +
                                     storeAddress.getAddressLineTwo() + " " + 
                                     storeAddress.getCity() + " " + 
                                     storeAddress.getState();
        }

    }

}
