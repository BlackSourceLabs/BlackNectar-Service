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
import java.util.List;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.client.Aroma;
import tech.blackhole.blacknectar.service.TestingResources;
import tech.blackhole.blacknectar.service.exceptions.OperationFailedException;
import tech.blackhole.blacknectar.service.stores.Store;
import tech.blackhole.blacknectar.service.stores.StoreRepository;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static tech.blackhole.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@RunWith(AlchemyTestRunner.class)
public class SQLBlackNectarServiceIT 
{
    private Aroma aroma;
    private Connection sql;
    
    private SQLBlackNectarService instance;
    
    private List<Store> stores;
    
    private List<Store> allStores;
    
    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        instance = new SQLBlackNectarService(aroma, sql);
    }


    private void setupData() throws Exception
    {
        stores = listOf(stores());
        allStores = StoreRepository.FILE.getAllStores();
    }

    private void setupMocks() throws Exception
    {
        aroma = TestingResources.getAroma();
        sql = TestingResources.createSQLConnection();
    }

    @Test
    public void testGetAllStores()
    {
    }

    @Test
    public void testSearchForStores()
    {
        
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
                continue;
            }
        }

    }

    @Test
    public void testPrepareStatementForStore() throws Exception
    {
    }

    @Test
    public void testGetStatementToCreateTable()
    {
    }

}