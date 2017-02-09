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

package tech.blacksource.blacknectar.service.data;

import java.util.List;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.blacksource.blacknectar.service.BlackNectarGenerators;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.stores.Location;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateDouble;
import tech.sirwellington.alchemy.test.junit.runners.GenerateInteger;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.locations;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class MemoryStoreRepositoryTest
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
    
    private MemoryStoreRepository instance;
    
    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();
        
        instance = new MemoryStoreRepository(stores, GeoCalculator.HARVESINE);
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
        
        request = new BlackNectarSearchRequest()
            .withCenter(location)
            .withRadius(10);
        
        List<Store> result = instance.searchForStores(request);
        assertThat(result, not(empty()));
        assertThat(result, contains(store));
    }

    @Test
    public void testContainsStoreWhenContains() throws Exception
    {
        Store store = Lists.oneOf(stores);
        assertTrue(instance.containsStore(store.getStoreId()));
    }
    
    @Test
    public void testContainsStoreWhenNotContains()
    {
        String randomId = one(uuids);
        assertFalse(instance.containsStore(randomId));
    }

    @DontRepeat
    @Test
    public void testContainsStoreWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.containsStore(null)).isInstanceOf(BadArgumentException.class);
        assertThrows(() -> instance.containsStore("")).isInstanceOf(BadArgumentException.class);
        assertThrows(() -> instance.containsStore(store.getName())).isInstanceOf(BadArgumentException.class);
    }

    @Test
    public void testAddStore() throws Exception
    {

        Store newStore = one(stores());
        
        List<Store> result = instance.getAllStores();
        assertThat(result, not(contains(newStore)));
        
        instance.addStore(newStore);
        result = instance.getAllStores();
        assertThat(result, hasItem(newStore));
    }
    
    @Test
    public void testSearchForStoresByName() throws Exception
    {
        request = new BlackNectarSearchRequest()
            .withSearchTerm(store.getName());
        
        List<Store> results = instance.searchForStores(request);
        assertThat(results, contains(store));
    }
    
    @Test
    public void testSearchForStoresByZipCode() throws Exception
    {
        request = new BlackNectarSearchRequest()
            .withZipCode(store.getAddress().getZipCode());
        
        List<Store> results = instance.searchForStores(request);
        assertThat(results, contains(store));
    }

    @Test
    public void testLimit() throws Exception
    {
        int totalStores = one(integers(10, 100));
        
        int limit = one(integers(1, totalStores /2));
        
        stores = listOf(stores(), totalStores);
        Location location = one(locations());
        
        //Give all the stores the same location
        stores = stores.stream()
            .map(s -> Store.Builder.fromStore(s).withLocation(location).build())
            .collect(toList());
                
        request = new BlackNectarSearchRequest()
            .withCenter(location)
            .withRadius(10)
            .withLimit(limit);
        
        instance = new MemoryStoreRepository(stores, GeoCalculator.HARVESINE);
        
        List<Store> results = instance.searchForStores(request);
        assertThat(results.size(), is(limit));
        
    }

    @Test
    public void testDeleteStore()
    {
        Store storeToDelete = Lists.oneOf(stores);
            
        List<Store> results = instance.getAllStores();
        assertThat(results, hasItem(storeToDelete));
        
        instance.deleteStore(storeToDelete);

        results = instance.getAllStores();
        assertThat(results, not(hasItem(storeToDelete)));

    }

    @DontRepeat
    @Test
    public void testDeleteStoreWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteStore((Store) null)).isInstanceOf(BadArgumentException.class);
        assertThrows(() -> instance.deleteStore((String) null)).isInstanceOf(BadArgumentException.class);
        assertThrows(() -> instance.deleteStore("")).isInstanceOf(BadArgumentException.class);
    }
    
    @DontRepeat
    @Test
    public void testDeleteStoreWhenStoreDoesNotExist() throws Exception
    {
        Store newStore = one(stores());
        instance.deleteStore(newStore);
    }
    
    @Test
    public void testUpdateStore()
    {
        String newName = one(alphabeticString());
        
        Store updatedStore = Store.Builder.fromStore(store)
            .withName(newName)
            .build();
        
        instance.updateStore(updatedStore);
        
        Store result = instance.getAllStores()
            .stream()
            .filter(s -> Objects.equals(s.getStoreId(), store.getStoreId()))
            .findFirst().get();
        
        assertThat(result, is(updatedStore));
             
    }

}
