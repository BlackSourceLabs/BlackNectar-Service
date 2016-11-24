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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.blackhole.blacknectar.service.exceptions.BadArgumentException;
import tech.blackhole.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static tech.blackhole.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.negativeIntegers;
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
    public void testGetAllStoresWithBadLimit()
    {
        int limit = one(negativeIntegers());
        assertThrows(() -> instance.getAllStores(limit)).isInstanceOf(BadArgumentException.class);
    }

    @Test
    public void testSearchForStores()
    {
    }

    @Test
    public void testAddStore()
    {
    }

    @Test
    public void testPrepareStatementForStore() throws Exception
    {
    }

    @Test
    public void testGetStatementToCreateTable()
    {
    }

    private void setupResultsWithStore(ResultSet resultSet, Store store) throws SQLException
    {
        when(storeMapper.mapToStore(resultSet)).thenReturn(store);
    }

}