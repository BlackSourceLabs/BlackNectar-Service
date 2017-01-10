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
import org.mockito.Mock;
import tech.aroma.client.Aroma;
import tech.redroma.google.places.data.Place;
import tech.redroma.yelp.YelpBusiness;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class ModuleAlgorithmsTest
{

    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;

    private ModuleAlgorithms instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new ModuleAlgorithms();
    }

    private void setupData() throws Exception
    {

    }

    private void setupMocks() throws Exception
    {

    }

    @Test
    public void testConfigure()
    {
    }

    @Test
    public void testProvideGooglePlacesMatchingAlgorithm()
    {
        StoreMatchingAlgorithm<Place> result = instance.provideGooglePlacesMatchingAlgorithm(aroma);
        assertThat(result, notNullValue());
    }
    
    @Test
    public void testProvideYelpBusinessesMatchingAlgoirhtm()
    {
        StoreMatchingAlgorithm<YelpBusiness> result = instance.provideYelpBusinessesMatchingAlgoirhtm(aroma);
        assertThat(result, notNullValue());
    }
    
}
