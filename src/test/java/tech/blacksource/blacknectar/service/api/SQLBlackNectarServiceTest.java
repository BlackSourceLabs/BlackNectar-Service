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

package tech.blacksource.blacknectar.service.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.stubbing.OngoingStubbing;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.stores.Location;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.locations;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.doubles;
import static tech.sirwellington.alchemy.generator.NumberGenerators.negativeIntegers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class SQLBlackNectarServiceTest 
{
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
   
    @Mock
    private Statement statement;
    
    @Mock
    private ResultSet resultSet;
    
    @Mock
    private GeoCalculator geoCalculator;
    
    @Mock
    private SQLStoreMapper storeMapper;
    
    private List<Store> stores;
    private Store store;
    
    private Aroma aroma ;
    
    private BlackNectarSearchRequest request;
    private SQLBlackNectarService instance;
    
    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        
        instance = new SQLBlackNectarService(aroma, connection, geoCalculator, storeMapper);
    }


    private void setupData() throws Exception
    {
        stores = listOf(stores());
        store = Lists.oneOf(stores);

        Location center = one(locations());
        request = new BlackNectarSearchRequest()
            .withCenter(center)
            .withRadius(one(doubles(1000, 10_000)))
            .withSearchTerm(one(alphabeticString()));
    }

    private void setupMocks() throws Exception
    {
        aroma = Aroma.create();
        when(connection.isClosed()).thenReturn(false);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.createStatement()).thenReturn(statement);
        
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        
        setupResultsWithStore(resultSet, store);
        
        when(geoCalculator.calculateDestinationFrom(eq(request.center), eq(request.radiusInMeters), anyDouble()))
            .thenReturn(one(locations()))
            .thenReturn(one(locations()))
            .thenReturn(one(locations()))
            .thenReturn(one(locations()));
        
    }
    
    @DontRepeat
    @Test
    public void testConstructorWithBadArguments()
    {
        assertThrows(() -> new SQLBlackNectarService(null, connection, geoCalculator, storeMapper))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new SQLBlackNectarService(aroma, null, geoCalculator, storeMapper))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new SQLBlackNectarService(aroma, connection, null, storeMapper))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new SQLBlackNectarService(aroma, connection, geoCalculator, null))
            .isInstanceOf(IllegalArgumentException.class);
        
    }
    
    @DontRepeat
    @Test
    public void testConstructorWithClosedConnection() throws Exception
    {
        when(connection.isClosed()).thenReturn(true);
        
        assertThrows(() -> new SQLBlackNectarService(aroma, connection, geoCalculator, storeMapper))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGetAllStores()
    {
        List<Store> results = instance.getAllStores(0);
        assertThat(results, not(empty()));
        
        Store first = results.get(0);
        assertThat(first, is(store));
    }

    @Test
    public void testGetAllStoresWithAllStores() throws SQLException
    {
        OngoingStubbing<Boolean> resultStubbing = when(resultSet.next());
        for (Store store : stores)
        {
            resultStubbing = resultStubbing.thenReturn(true);
        }
        resultStubbing.thenReturn(false);

        OngoingStubbing<Store> mapperStubbing = when(storeMapper.mapToStore(resultSet));
        for (Store store : stores)
        {
            resultStubbing = resultStubbing.thenReturn(true);
            mapperStubbing = mapperStubbing.thenReturn(store);
        }
        mapperStubbing.thenReturn(null);
        
        List<Store> results = instance.getAllStores(0);
        assertThat(results, is(stores));
    }

    @Test
    public void testGetAllStoresWithBadLimit()
    {
        int limit = one(negativeIntegers());
        assertThrows(() -> instance.getAllStores(limit)).isInstanceOf(BadArgumentException.class);
    }

    @Test
    public void testSearchForStores()
    {
            
        List<Store> results = instance.searchForStores(request);
        
        assertThat(results, not(empty()));
        Store first = results.get(0);
        assertThat(first, is(store));
    }

    @Test
    public void testAddStore() throws Exception
    {
        when(connection.prepareStatement(anyString()))
            .thenReturn(preparedStatement);
        
        instance.addStore(store);
        
        verify(preparedStatement).executeUpdate();
        
        assertThatStatementWasPreparedAgainstStore(preparedStatement, store);
    }

    @Test
    public void testPrepareStatementForStore() throws Exception
    {
        instance.prepareStatementToInsertStore(preparedStatement, store);
        
        assertThatStatementWasPreparedAgainstStore(preparedStatement, store);
    }

    @Test
    public void testGetStatementToCreateTable()
    {
        String sqlStatement = instance.getStatementToCreateTable();
        assertThat(sqlStatement.isEmpty(), is(false));
    }

    private void setupResultsWithStore(ResultSet resultSet, Store store) throws SQLException
    {
        when(storeMapper.mapToStore(resultSet)).thenReturn(store);
    }

    private void assertThatStatementWasPreparedAgainstStore(PreparedStatement preparedStatement, Store store) throws Exception
    {
        UUID storeUuid = UUID.fromString(store.getStoreId());
        
        verify(preparedStatement).setObject(1, storeUuid);
        verify(preparedStatement).setString(2, store.getName());
        verify(preparedStatement).setDouble(3, store.getLocation().getLatitude());
        verify(preparedStatement).setDouble(4, store.getLocation().getLongitude());
        verify(preparedStatement).setDouble(5, store.getLocation().getLongitude());
        verify(preparedStatement).setDouble(6, store.getLocation().getLatitude());
        verify(preparedStatement).setString(7, store.getAddress().getAddressLineOne());
        verify(preparedStatement).setString(8, store.getAddress().getAddressLineTwo());
        verify(preparedStatement).setString(9, store.getAddress().getCity());
        verify(preparedStatement).setString(10, store.getAddress().getState());
        verify(preparedStatement).setString(11, store.getAddress().getCounty());
        verify(preparedStatement).setString(12, store.getAddress().getZipCode());
        verify(preparedStatement).setString(13, store.getAddress().getLocalZipCode());
    }

}