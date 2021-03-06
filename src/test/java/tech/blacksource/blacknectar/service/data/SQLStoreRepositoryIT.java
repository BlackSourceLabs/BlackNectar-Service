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
import java.util.Optional;

import com.google.common.base.Objects;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.TestingResources;
import tech.blacksource.blacknectar.service.exceptions.BlackNectarAPIException;
import tech.blacksource.blacknectar.service.stores.Location;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.doubles;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 * @author SirWellington
 */
@IntegrationTest
@RunWith(AlchemyTestRunner.class)
public class SQLStoreRepositoryIT
{
    private static final Location NYC = Location.with(40.758659, -73.985217);
    private static final Location LA = Location.with(34.0420322, -118.2541062);
    private static final String ZIP_SANTA_MONICA = "90401";
    private static final String ZIP_BRONX = "10455";


    private Aroma aroma;
    private JdbcTemplate database;
    private SQLStoreMapper storeMapper;

    private SQLStoreRepository instance;

    private List<Store> stores;
    private Store store;
    private String storeId;

    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();

        instance = new SQLStoreRepository(aroma, database, storeMapper);
    }

    @After
    public void tearDown() throws Exception
    {
        instance.deleteStore(store);
        stores.parallelStream().forEach(instance::deleteStore);
    }

    private void setupData() throws Exception
    {
        stores = listOf(stores());
        store = Lists.oneOf(stores);
        storeId = store.getStoreId();
    }

    private void setupMocks() throws Exception
    {
        aroma = TestingResources.getAroma();
        database = TestingResources.createDatabaseConnection();
        storeMapper = SQLStoreMapper.INSTANCE;
    }

    @Test
    public void testAddStore() throws Exception
    {
        assertFalse(instance.containsStore(storeId));
        instance.addStore(store);
        assertTrue(instance.containsStore(storeId));
    }

    @Test
    public void testAddStoreWhenAlreadyExists() throws Exception
    {
        instance.addStore(store);
        assertTrue(instance.containsStore(storeId));

        assertThrows(() -> instance.addStore(store)).isInstanceOf(BlackNectarAPIException.class);
    }

    @Test
    public void testContainsStoreWhenNotContains() throws Exception
    {
        String randomId = one(uuids);
        assertFalse(instance.containsStore(randomId));
    }

    @Test
    public void testContainsStoreWhenContains() throws Exception
    {
        instance.addStore(store);

        assertTrue(instance.containsStore(store.getStoreId()));
    }

    @Test
    public void testGetAllStoresWithNoLimit()
    {
        stores.forEach(instance::addStore);

        List<Store> result = instance.getAllStores();
        assertThat(result, not(empty()));
    }

    @Test
    public void testGetAllStoresWithLimit()
    {
        stores.forEach(instance::addStore);
        int limit = one(integers(10, 1_000));

        List<Store> result = instance.getAllStores(limit);

        assertThat(result, not(empty()));
        assertThat(result.size(), lessThanOrEqualTo(limit));
    }

    @Test
    public void testSearchForStoresWithName()
    {
        String name = "Duane";

        BlackNectarSearchRequest request = new BlackNectarSearchRequest()
                .withSearchTerm(name);

        List<Store> results = instance.searchForStores(request);
        assertThat(results, not(empty()));

        results.forEach(s -> assertThat(s.getName(), not(isEmptyOrNullString())));

        for (Store result : results)
        {
            assertThat(result.getName(), anyOf(containsString(name), containsString(name.toUpperCase())));
        }
    }

    @Test
    public void testSearchForStoreWithCenter()
    {
        double radius = one(doubles(1_000, 10_000));

        BlackNectarSearchRequest request = new BlackNectarSearchRequest()
                .withCenter(LA)
                .withRadius(radius);

        List<Store> results = instance.searchForStores(request);
        assertThat(results, not(empty()));

    }

    @Test
    public void testSearchForStoresWithNameAndCenter()
    {
        double radius = one(doubles(5_000, 10_000));
        String searchTerm = "Duane";

        BlackNectarSearchRequest request = new BlackNectarSearchRequest()
                .withCenter(NYC)
                .withRadius(radius)
                .withSearchTerm(searchTerm);

        List<Store> results = instance.searchForStores(request);
        assertThat(results, not(empty()));

        results.forEach(s -> assertThat(s.getName(),
                                        anyOf(containsString(searchTerm),
                                              containsString(searchTerm.toUpperCase()))));
    }

    @Test
    public void testSearchForStoresWithZipCode() throws Exception
    {
        String zipCode = ZIP_SANTA_MONICA;

        BlackNectarSearchRequest request = new BlackNectarSearchRequest()
                .withZipCode(zipCode);

        List<Store> results = instance.searchForStores(request);
        assertThat(results, not(empty()));

        results.forEach(s -> assertThat(s.getAddress().getZipCode(), is(zipCode)));
    }

    @Test
    public void testSearchForStoresWithZipCodeAndName() throws Exception
    {
        String name = "Duane";
        String zip = ZIP_BRONX;

        BlackNectarSearchRequest request = new BlackNectarSearchRequest()
                .withZipCode(zip)
                .withSearchTerm(name);

        List<Store> results = instance.searchForStores(request);
        assertThat(results, notNullValue());

        results.forEach(s -> assertThat(s.getAddress().getZipCode(), is(zip)));
        results.forEach(s -> assertThat(s.getName(), containsString(name)));
    }

    @DontRepeat
    @Test
    public void testSearchForStoresResultsHaveImages() throws Exception
    {
        BlackNectarSearchRequest request = new BlackNectarSearchRequest()
                .withRadius(10_000)
                .withCenter(LA);

        List<Store> results = instance.searchForStores(request);

        long withImages = results.stream()
                                 .filter(Store::hasMainImage)
                                 .count();

        assertThat(withImages, greaterThan(0L));
    }

    @Test
    public void testDeleteStore() throws Exception
    {

        instance.addStore(store);
        assertTrue(instance.containsStore(storeId));

        instance.deleteStore(storeId);
        assertFalse(instance.containsStore(storeId));
    }

    @Test
    public void testUpdateStore() throws Exception
    {
        instance.addStore(store);
        assertTrue(instance.containsStore(storeId));

        String newName = one(alphabeticString());
        Store updatedStore = Store.Builder.fromStore(store).withName(newName).build();

        instance.updateStore(updatedStore);

        BlackNectarSearchRequest request = new BlackNectarSearchRequest()
                .withSearchTerm(newName)
                .withCenter(updatedStore.getLocation());

        List<Store> results = instance.searchForStores(request);
        boolean anyMatch = results.stream().anyMatch(s -> Objects.equal(s.getStoreId(), storeId));
        assertTrue(anyMatch);

        Optional<Store> match = results.stream()
                                       .filter(s -> Objects.equal(s.getStoreId(), storeId))
                                       .findFirst();

        assertTrue(match.isPresent());

        Store resultingStore = match.get();
        assertThat(resultingStore.getName(), is(newName));
        assertThat(resultingStore.getAddress(), is(store.getAddress()));
    }

    @Test
    public void testUpdateStoreWhenStoreDoesNotExist() throws Exception
    {
        instance.updateStore(store);

        assertTrue(instance.containsStore(storeId));
    }

}