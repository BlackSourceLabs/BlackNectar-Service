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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static sir.wellington.alchemy.collections.lists.Lists.oneOf;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;


/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class SQLStoreMapperTest 
{
    private SQLStoreMapper instance;

    private List<Store> stores;
    private Store store;
    
    @Mock
    private ResultSet results;
    
    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        instance = SQLStoreMapper.INSTANCE;
    }


    private void setupData() throws Exception
    {
        stores = listOf(stores());
        store = oneOf(stores);
    }

    private void setupMocks() throws Exception
    {
        setupResultsWithStore(results, store);
    }

    @Test
    public void testMapToStore() throws Exception
    {
        Store result = instance.mapToStore(results);
        assertThat(result, is(store));
    }
    
    @Test
    public void testMapToStoreWithMultipleResults() throws Exception
    {
        for (Store store : stores)
        {
            setupResultsWithStore(results, store);
            Store result = instance.mapToStore(results);
            assertThat(result, is(store));
        }
    }

    @DontRepeat
    @Test
    public void testMapToStoreWithBadArguments() throws Exception
    {
        assertThrows(() -> instance.mapToStore(null)).isInstanceOf(IllegalArgumentException.class);
    }
    
    private void setupResultsWithStore(ResultSet results, Store store) throws SQLException
    {
        when(results.getString(SQLColumns.STORE_ID)).thenReturn(store.getStoreId());
        when(results.getString(SQLColumns.STORE_NAME)).thenReturn(store.getName());
        when(results.getString(SQLColumns.ADDRESS)).thenReturn(store.getAddress().getAddressLineOne());
        when(results.getString(SQLColumns.ADDRESS_LINE_TWO)).thenReturn(store.getAddress().getAddressLineTwo());
        when(results.getString(SQLColumns.CITY)).thenReturn(store.getAddress().getCity());
        when(results.getString(SQLColumns.STATE)).thenReturn(store.getAddress().getState());
        when(results.getString(SQLColumns.COUNTY)).thenReturn(store.getAddress().getCounty());
        when(results.getInt(SQLColumns.ZIP_CODE)).thenReturn(store.getAddress().getZip5());
        when(results.getInt(SQLColumns.LOCAL_ZIP_CODE)).thenReturn(store.getAddress().getZip4());
        when(results.getDouble(SQLColumns.LATITUDE)).thenReturn(store.getLocation().getLatitude());
        when(results.getDouble(SQLColumns.LONGITUDE)).thenReturn(store.getLocation().getLongitude());
    }

}