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

package tech.blacksource.blacknectar.service.algorithms;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.google.places.data.Place;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;


/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class GooglePlacesMatchingAlgorithmTest 
{

    @GenerateString
    private String storeName;
    
    private Store store;
    private Place place;
    
    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
    }


    private void setupData() throws Exception
    {
        
    }

    private void setupMocks() throws Exception
    {
        
    }

    @Test
    public void testMatchesStore()
    {
    }

}