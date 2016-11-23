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

package tech.blackhole.blacknectar.service.api;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.blackhole.blacknectar.service.BlackNectarGenerators;
import tech.blackhole.blacknectar.service.stores.Location;
import tech.blackhole.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateDouble;
import tech.sirwellington.alchemy.test.junit.runners.GenerateInteger;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static tech.blackhole.blacknectar.service.BlackNectarGenerators.locations;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class MemoryBlackNectarServiceTest 
{
    @GeneratePojo
    private BlackNectarSearchRequest request;

    @GenerateString(ALPHABETIC)
    private String searchTerm;
    
    @GenerateInteger(GenerateInteger.Type.POSITIVE)
    private Integer limit;
    
    @GenerateDouble(GenerateDouble.Type.POSITIVE)
    private Double radius;
    
    private Location center;
    
    private List<Store> stores;
    private Store store;
    
    private MemoryBlackNectarService instance;
    
    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();
        
        instance = new MemoryBlackNectarService(stores, GeoCalculator.HARVESINE);
    }


    private void setupData() throws Exception
    {
        center = one(locations());
        stores = listOf(BlackNectarGenerators.stores());
        store = Lists.oneOf(stores);
        
        request = new BlackNectarSearchRequest()
            .withCenter(center)
            .withRadius(radius)
            .withLimit(limit)
            .withSearchTerm(searchTerm);
    }

    private void setupMocks() throws Exception
    {
        
    }

    @Test
    public void testGetAllStores()
    {
        List<Store> result = instance.getAllStores();
            
        assertThat(result, is(stores));
    }

    @Test
    public void testSearchForStoresByLocation()
    {
        Location location = store.getLocation();
        
        BlackNectarSearchRequest request = new BlackNectarSearchRequest()
            .withCenter(location);
        
        List<Store> result = instance.searchForStores(request);
        assertThat(result, not(empty()));
        assertThat(result, contains(store));
    }


}