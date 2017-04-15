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

package tech.blacksource.blacknectar.service.images;

import java.net.URL;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.algorithms.StoreSearchAlgorithm;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.yelp.YelpAPI;
import tech.redroma.yelp.YelpBusiness;
import tech.sirwellington.alchemy.generator.NetworkGenerators;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Mockito.*;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 * @author SirWellington
 */
@Repeat(25)
@RunWith(AlchemyTestRunner.class)
public class YelpImageLoaderTest
{

    @GeneratePojo
    private YelpBusiness matchingBusiness;

    private Store store;

    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;

    @Mock
    private StoreSearchAlgorithm<YelpBusiness> searchAlgorithm;

    @Mock
    private YelpAPI yelpAPI;

    private YelpImageLoader instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new YelpImageLoader(aroma, searchAlgorithm, yelpAPI);
    }

    private void setupData() throws Exception
    {
        store = one(stores());

        setupYelpData();
    }

    private void setupYelpData()
    {
        matchingBusiness.imageURL = NetworkGenerators.httpUrls().get().toString();
    }

    private void setupMocks() throws Exception
    {
        when(searchAlgorithm.findMatchFor(store)).thenReturn(matchingBusiness);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new YelpImageLoader(null, searchAlgorithm, yelpAPI))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new YelpImageLoader(aroma, null, yelpAPI))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new YelpImageLoader(aroma, searchAlgorithm, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGetImageFor() throws Exception
    {
        List<URL> result = instance.getImagesFor(store);

        assertThat(result, notNullValue());

        URL expected = new URL(matchingBusiness.imageURL);
        assertThat(result, hasItem(expected));
    }

    @DontRepeat
    @Test
    public void testGetImageForWhenNoMatchesFound() throws Exception
    {
        when(searchAlgorithm.findMatchFor(store)).thenReturn(null);

        List<URL> result = instance.getImagesFor(store);
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }

    @DontRepeat
    @Test
    public void testGetImageWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getImagesFor(null)).isInstanceOf(IllegalArgumentException.class);
    }

}
