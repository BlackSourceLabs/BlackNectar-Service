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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class TitleCaseStoreNamesTransformationTests
{

    @GenerateString(ALPHABETIC)
    private String name;

    private Store store;

    private TitleCaseStoreNamesTransformation instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();
        instance = new TitleCaseStoreNamesTransformation();
    }

    private void setupData() throws Exception
    {
        store = one(stores());
    }

    private void setupMocks() throws Exception
    {

    }

    @Test
    public void testWithSimpleName() throws Exception
    {
        String firstChar = "A";
        String expected = firstChar + name.toLowerCase();

        String argument = expected.toUpperCase();
        store = storeWithName(argument);

        Store result = instance.apply(store);
        assertThat(result.getName(), is(expected));

    }

    @Test
    public void testWithWalmart() throws Exception
    {
        String name = "Walmart SUPERCENTER";
        String expected = "Walmart Supercenter";

        assertNameMatches(name, expected);
    }
    
    @Test
    public void testWithSantoDomingoCoffee() throws Exception
    {
        String name = "SANTO DOMINGO BAKERY AND COFFEE";
        String expected = "Santo Domingo Bakery And Coffee";
        
        assertNameMatches(name, expected);
    }
    
    @Test
    public void testWithApostrophy() throws Exception
    {
        String name = "MANNING'S MARKETPLACE";
        String expected = "Manning's Marketplace";
        
        assertNameMatches(name, expected);
    }
    
    @Test
    public void testWithAmpersand() throws Exception
    {
        String name = "K & S FOOD MARKET, INC.";
        String expected = "K & S Food Market, INC.";
        
        assertNameMatches(name, expected);
    }
    
    @Test
    public void testWith7Eleven() throws Exception
    {
        String name = "7-ELEVEN";
        String expected = "7-Eleven";
        
        assertNameMatches(name, expected);
    }
    
    @Test
    public void testWithAmpersandAndApostrophe() throws Exception
    {
        String name = "KIRBIE'S FAMILY MEATS & CATERING";
        String expected = "Kirbie's Family Meats & Catering";
        
        assertNameMatches(name, expected);
    }
    
    @Test
    public void testWithCVS() throws Exception
    {
        String name = "CVS PHARMACY";
        String expected = "CVS Pharmacy";
        
        assertNameMatches(name, expected);
    }
    
    @Test
    public void testWithCVSAlreadyGood() throws Exception
    {
        String name = "CVS Pharmacy";
        String expected = "CVS Pharmacy";
        
        assertNameMatches(name, expected);
    }
    
    @Test
    public void testWithHyphens() throws Exception
    {
        String name = "SPEE-D-FOODS";
        String expected = "Spee-D-Foods";
        
        assertNameMatches(name, expected);
    }
    
    @Test
    public void testWithVons() throws Exception
    {
        String name = "VONS";
        String expected = "Vons";
        
        assertNameMatches(name, expected);
    }

    private void assertNameMatches(String name, String expected)
    {
        store = storeWithName(name);

        Store result = instance.apply(store);
        Store expectedStore = storeWithName(expected);
        assertThat(result, is(expectedStore));
    }

    private Store storeWithName(String name)
    {
        return Store.Builder.fromStore(store)
            .withName(name)
            .build();
    }
}
