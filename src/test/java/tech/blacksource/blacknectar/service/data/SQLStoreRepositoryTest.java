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

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.exceptions.BlackNectarAPIException;
import tech.blacksource.blacknectar.service.stores.*;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static java.lang.String.format;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.locations;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.*;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class SQLStoreRepositoryTest
{

    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;

    @Mock
    private JdbcTemplate database;

    @Mock
    private Statement statement;

    @Mock
    private SQLStoreMapper storeMapper;

    private List<Store> stores;
    private Store store;
    private UUID storeUuid;

    private BlackNectarSearchRequest request;
    private SQLStoreRepository instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new SQLStoreRepository(aroma, database, storeMapper);
    }

    private void setupData() throws Exception
    {
        stores = listOf(stores());
        store = Lists.oneOf(stores);
        storeUuid = UUID.fromString(store.getStoreId());

        Location center = one(locations());
        request = new BlackNectarSearchRequest()
            .withCenter(center)
            .withRadius(one(doubles(1000, 10_000)))
            .withSearchTerm(one(alphabeticString()));
    }

    private void setupMocks() throws Exception
    {
        setupSQLInsertForStore();
        
        when(database.queryForObject(eq(SQLQueries.CONTAINS_STORE), eq(Integer.class), any(UUID.class)))
            .thenReturn(0);
    }

    @DontRepeat
    @Test
    public void testConstructorWithBadArguments()
    {
        assertThrows(() -> new SQLStoreRepository(null, database, storeMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new SQLStoreRepository(aroma, null, storeMapper))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new SQLStoreRepository(aroma, database, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGetAllStoresWithNoLimit() throws SQLException
    {
        String expectedQuery = "SELECT * FROM Stores";

        when(database.query(anyString(), eq(storeMapper)))
            .thenReturn(stores);

        List<Store> results = instance.getAllStores(0);
        assertThat(results, is(stores));

        verify(database).query(expectedQuery, storeMapper);
    }

    @Test
    public void testGetAllStoresWithLimit() throws Exception
    {
        int limit = one(integers(10, 100));
        String expectedQuery = format("SELECT * FROM Stores LIMIT %d", limit);

        when(database.query(anyString(), eq(storeMapper)))
            .thenReturn(stores);

        List<Store> results = instance.getAllStores(limit);
        assertThat(results, not(empty()));
        assertThat(results, is(stores));

        verify(database).query(expectedQuery, storeMapper);

    }

    @Test
    public void testGetAllStoresWithBadLimit()
    {
        int limit = one(negativeIntegers());
        assertThrows(() -> instance.getAllStores(limit)).isInstanceOf(BadArgumentException.class);
    }

    @Test
    public void testGetAllStoresWhenFails() throws Exception
    {
        DataAccessException ex = mock(DataAccessException.class);

        when(database.query(anyString(), eq(storeMapper)))
            .thenThrow(ex);

        assertThrows(() -> instance.getAllStores())
            .isInstanceOf(BlackNectarAPIException.class);

    }

    @Test
    public void testSearchForStores()
    {
        when(database.query(anyString(), eq(storeMapper), Mockito.<Object>anyVararg()))
            .thenReturn(stores);

        List<Store> results = instance.searchForStores(request);

        assertThat(results, not(empty()));
        assertThat(results, is(stores));
    }
    
    @Test
    public void testSearchForStoresWithZipCode() throws Exception
    {
        int zipCodeInt = one(integers(10_000, 99_999));
        String zipCode = String.valueOf(zipCodeInt);
        
        request = new BlackNectarSearchRequest()
            .withSearchTerm(one(alphabeticString()))
            .withZipCode(zipCode);
        
        when(database.query(eq(SQLQueries.QUERY_STORES_WITH_NAME_AND_ZIPCODE), eq(storeMapper), Mockito.<Object>anyVararg()))
            .thenReturn(stores);
        
        List<Store> results = instance.searchForStores(request);
        
        assertThat(results, not(empty()));
        assertThat(results, is(stores));
    }

    @Test
    public void testAddStore() throws Exception
    {
        when(database.update(anyString(), Mockito.<Object>anyVararg()))
            .thenReturn(1);

        instance.addStore(store);

        assertStoreInsertedIntoDatabase(database, store);
    }

    private void setupSQLInsertForStore()
    {
        String insert = SQLQueries.INSERT_STORE;

        when(database.update(eq(insert), Mockito.<Object>anyVararg()))
            .thenReturn(1);
    }

    @Test
    public void testDeleteStore()
    {
        String expectedQuery = SQLQueries.DELETE_STORE;

        when(database.update(expectedQuery, storeUuid))
            .thenReturn(1);

        instance.deleteStore(store.getStoreId());

        verify(database).update(expectedQuery, storeUuid);
    }

    @DontRepeat
    @Test
    public void testDeleteStoreWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.deleteStore(""))
            .isInstanceOf(BadArgumentException.class);

        String badId = one(alphabeticString());
        assertThrows(() -> instance.deleteStore(badId))
            .isInstanceOf(BadArgumentException.class);

    }

    @Test
    public void testContainsStoreWhenContainsStore()
    {
        String sql = SQLQueries.CONTAINS_STORE;

        String storeId = store.getStoreId();
        
        when(database.queryForObject(sql, Integer.class, storeUuid))
            .thenReturn(1);

        assertTrue(instance.containsStore(storeId));
    }

    @Test
    public void testContainsStoreWhenNotContainsStore() throws Exception
    {
        String sql = SQLQueries.CONTAINS_STORE;

        String storeId = store.getStoreId();

        when(database.queryForObject(sql, Integer.class, storeUuid))
            .thenReturn(0);

        assertFalse(instance.containsStore(storeId));
    }

    @Test
    public void testUpdateStore() throws Exception
    {
        String newName = one(alphabeticString());

        store = Store.Builder.fromStore(store).withName(newName).build();

        instance.updateStore(store);
        assertStoreUpdatedIntoDatabase(database, store);
    }
    
    
    @Test
    public void testUpdateStoreWhenStoreDoesNotExist() throws Exception
    {
        Store newStore = one(stores());
        instance.updateStore(newStore);
        
        assertStoreInsertedIntoDatabase(database, newStore);
    }

    @DontRepeat
    @Test
    public void testUpdatStoreWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.updateStore(null)).isInstanceOf(BadArgumentException.class);
    }

    private void assertStoreInsertedIntoDatabase(JdbcTemplate database, Store store) throws Exception
    {
        String expectedStatement = SQLQueries.INSERT_STORE;
        UUID storeUuid = UUID.fromString(store.getStoreId());
        Address address = store.getAddress();
        double lat = store.getLocation().getLatitude();
        double lon = store.getLocation().getLongitude();

        verify(database).update(expectedStatement,
                                storeUuid,
                                store.getName(),
                                store.getStoreCode(),
                                lat,
                                lon,
                                lon,
                                lat,
                                address.getAddressLineOne(),
                                address.getAddressLineTwo(),
                                address.getCity(),
                                address.getState(),
                                address.getCounty(),
                                address.getZipCode(),
                                address.getLocalZipCode());
    }

    private void assertStoreUpdatedIntoDatabase(JdbcTemplate database, Store store) throws Exception
    {
        String expectedStatement = SQLQueries.UPDATE_STORE;
        UUID storeUuid = UUID.fromString(store.getStoreId());
        Address address = store.getAddress();
        double lat = store.getLocation().getLatitude();
        double lon = store.getLocation().getLongitude();

        verify(database).update(expectedStatement,
                                storeUuid,
                                store.getName(),
                                store.getStoreCode(),
                                lat,
                                lon,
                                lon,
                                lat,
                                address.getAddressLineOne(),
                                address.getAddressLineTwo(),
                                address.getCity(),
                                address.getState(),
                                address.getCounty(),
                                address.getZipCode(),
                                address.getLocalZipCode(),
                                storeUuid);
    }

}
