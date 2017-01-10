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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
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
    private Store storeWithoutImage;

    @Mock
    private ResultSet results;

    @Mock
    private SQLTools sqlTools;

    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();

        instance = new SQLStoreMapper.Impl(sqlTools);
    }

    private void setupData() throws Exception
    {
        stores = listOf(stores());
        store = oneOf(stores);
        storeWithoutImage = Store.Builder.fromStore(store).withoutMainImageURL().build();
    }

    private void setupMocks() throws Exception
    {
        setupResultsWithStore(results, store);
        when(sqlTools.hasColumn(results, SQLColumns.Images.URL))
            .thenReturn(true);
    }
    
    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new SQLStoreMapper.Impl(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testMapToStore() throws Exception
    {
        Store result = instance.mapRow(results, 1);
        assertThat(result, is(store));
    }

    @Test
    public void testMapToStoreWithMultipleResults() throws Exception
    {
        for (Store store : stores)
        {
            setupResultsWithStore(results, store);
            Store result = instance.mapRow(results, 1);
            assertThat(result, is(store));
        }
    }

    @Test
    public void testMapToStoreWhenStoreHasNoImage() throws Exception
    {
        when(sqlTools.hasColumn(results, SQLColumns.Images.URL))
            .thenReturn(false);
        
        Store result = instance.mapRow(results, 1);
        assertThat(result, is(storeWithoutImage));
    }

    @DontRepeat
    @Test
    public void testMapToStoreWithBadArguments() throws Exception
    {
        assertThrows(() -> instance.mapRow(null, 1)).isInstanceOf(IllegalArgumentException.class);
    }

    private void setupResultsWithStore(ResultSet results, Store store) throws SQLException
    {
        when(results.getObject(SQLColumns.STORE_ID, UUID.class)).thenReturn(UUID.fromString(store.getStoreId()));
        when(results.getString(SQLColumns.STORE_NAME)).thenReturn(store.getName());
        when(results.getString(SQLColumns.ADDRESS_LINE_ONE)).thenReturn(store.getAddress().getAddressLineOne());
        when(results.getString(SQLColumns.ADDRESS_LINE_TWO)).thenReturn(store.getAddress().getAddressLineTwo());
        when(results.getString(SQLColumns.CITY)).thenReturn(store.getAddress().getCity());
        when(results.getString(SQLColumns.STATE)).thenReturn(store.getAddress().getState());
        when(results.getString(SQLColumns.COUNTY)).thenReturn(store.getAddress().getCounty());
        when(results.getString(SQLColumns.ZIP_CODE)).thenReturn(store.getAddress().getZipCode());
        when(results.getString(SQLColumns.LOCAL_ZIP_CODE)).thenReturn(store.getAddress().getLocalZipCode());
        when(results.getDouble(SQLColumns.LATITUDE)).thenReturn(store.getLocation().getLatitude());
        when(results.getDouble(SQLColumns.LONGITUDE)).thenReturn(store.getLocation().getLongitude());
        when(results.getString(SQLColumns.Images.URL)).thenReturn(store.getMainImageURL());
    }

    @Test
    public void testMapRow() throws Exception
    {
    }

    public class SQLStoreMapperImpl implements SQLStoreMapper
    {

        public Store mapRow(ResultSet results, int rowNum) throws SQLException
        {
            return null;
        }
    }

}
