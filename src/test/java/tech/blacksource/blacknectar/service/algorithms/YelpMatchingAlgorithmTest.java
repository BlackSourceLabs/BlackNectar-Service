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
import tech.redroma.yelp.YelpBusiness;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Answers.RETURNS_MOCKS;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.BooleanGenerators.booleans;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(25)
@RunWith(AlchemyTestRunner.class)
public class YelpMatchingAlgorithmTest 
{
    
    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;
    
    private YelpMatchingAlgorithm instance;
    
    private Store store;
    
    @GeneratePojo
    private YelpBusiness business;

    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        
        instance = new YelpMatchingAlgorithm(aroma);
    }


    private void setupData() throws Exception
    {
        store = one(stores());
    }

    private void setupMocks() throws Exception
    {
        
    }
    
    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new YelpMatchingAlgorithm(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testMatchesStoreWhenNoMatch() throws Exception
    {
        boolean result = instance.matchesStore(business, store);
        assertFalse(result);
    }
    
    @Test
    public void testMatchesStoreWhenNamesMatch() throws Exception
    {
        addStoreNameToBusiness(store, business);
        
        boolean result = instance.matchesStore(business, store);
        assertTrue(result);
    }
    
    @Test
    public void testMatchesStoreWhenAddressMatch() throws Exception
    {
        copyStoreAddressToBusiness(store, business);
        
        boolean result = instance.matchesStore(business, store);
        assertTrue(result);
    }

    private void addStoreNameToBusiness(Store store, YelpBusiness business)
    {
        boolean useExactSameName = one(booleans());
        
        if (useExactSameName)
        {
            business.name = store.getName();
        }
        else 
        {
            business.name += store.getName();
        }
    }

    private void copyStoreAddressToBusiness(Store store, YelpBusiness business)
    {
        Address storeAddress = store.getAddress();
        business.location.address1 = storeAddress.getAddressLineOne();
        business.location.address2 = storeAddress.getAddressLineTwo();
        business.location.city = storeAddress.getCity();
        business.location.state = storeAddress.getState();
        business.location.zipCode = storeAddress.getZipCode();
    }

}