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

package tech.blacksource.blacknectar.service.data.cleanup;

import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.data.StoreRepository;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;
import tech.sirwellington.alchemy.test.mockito.MoreAnswers;

import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class RunStoreTransformationTest 
{
    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;
    
    @Mock
    private StoreTransformation transformation;
    
    @Mock
    private StoreRepository repository;
    
    private List<Store> stores;

    private RunStoreTransformation instance;
    
    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        instance = new RunStoreTransformation(aroma, repository, transformation);
    }


    private void setupData() throws Exception
    {
        stores = listOf(stores());
    }

    private void setupMocks() throws Exception
    {
        when(repository.getAllStores()).thenReturn(stores);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new RunStoreTransformation(null, repository, transformation)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> new RunStoreTransformation(aroma, null, transformation)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> new RunStoreTransformation(aroma, repository, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testWhenNoUpdatesNeedToHappen() throws Exception
    {
        when(transformation.apply(any(Store.class)))
            .then(MoreAnswers.returnFirst());
        
        instance.call();
            
        verify(repository, never()).updateStore(any());
    }
    
    @Test
    public void testStoresAreUpdated() throws Exception
    {
        Map<Store,Store> transformations = Maps.create();
        
        for (Store store: stores)
        {
            Store transformedStore = one(stores());
            
            when(transformation.apply(store)).thenReturn(transformedStore);
            transformations.put(store, transformedStore);
        }
        
        instance.call();
            
        for (Store store : stores)
        {
            Store transformedStore = transformations.get(store);
            
            verify(repository).updateStore(transformedStore);
        }
    }
}