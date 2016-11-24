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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.client.Aroma;
import tech.blackhole.blacknectar.service.TestingResources;
import tech.blackhole.blacknectar.service.exceptions.OperationFailedException;
import tech.blackhole.blacknectar.service.stores.Location;
import tech.blackhole.blacknectar.service.stores.Store;
import tech.blackhole.blacknectar.service.stores.StoreRepository;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static tech.blackhole.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.NumberGenerators.doubles;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@RunWith(AlchemyTestRunner.class)
public class SQLBlackNectarServiceIT 
{
    private static final Location NYC = Location.with(40.758659, -73.985217);

    private Aroma aroma;
    private Connection connection;
    private GeoCalculator geoCalculator;
    private SQLStoreMapper storeMapper;
    
    private SQLBlackNectarService instance;
    
    private List<Store> stores;
    
    private List<Store> allStores;

    
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
        allStores = StoreRepository.FILE.getAllStores();
    }

    private void setupMocks() throws Exception
    {
        aroma = TestingResources.getAroma();
        connection = TestingResources.createSQLConnection();
        geoCalculator = GeoCalculator.HARVESINE;
        storeMapper = SQLStoreMapper.INSTANCE;
    }

    @Test
    public void testGetAllStoresWithNoLimit()
    {
        List<Store> result = instance.getAllStores();
        assertThat(result, not(empty()));
    }
    
    @Test
    public void testGetAllStoresWithLimit()
    {
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
        
        results.forEach(store -> assertThat(store.getName(), not(isEmptyOrNullString())));
        
        for (Store store : results)
        {
            assertThat(store.getName(), anyOf(containsString(name), containsString(name.toUpperCase())));
        }
    }
    
    @Test
    public void testSearchForStoreWithCenter()
    {
        double radius = one(doubles(3_000, 30_000));
        
        BlackNectarSearchRequest request = new BlackNectarSearchRequest()
            .withCenter(NYC)
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
        
        results.forEach(store -> assertThat(store.getName(), 
                                              anyOf(containsString(searchTerm),
                                                    containsString(searchTerm.toUpperCase()))));
    }
    
    @Ignore
    @Test
    public void testAddAllStores()
    {
        for (Store store : allStores)
        {
            try
            {
                instance.addStore(store);
            }
            catch (OperationFailedException ex)
            {
            }
        }

    }

    @Test
    public void testGetStatementToCreateTable() throws SQLException
    {
        String sql = instance.getStatementToCreateTable();
        Statement statement = connection.createStatement();
        statement.execute(sql);
    }

}