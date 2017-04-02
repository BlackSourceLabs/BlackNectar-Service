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
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.algorithms.StoreSearchAlgorithm;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.google.places.GooglePlacesAPI;
import tech.redroma.google.places.data.Photo;
import tech.redroma.google.places.data.Place;
import tech.redroma.google.places.requests.GetPhotoRequest;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Mockito.*;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NetworkGenerators.httpUrls;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@Repeat(25)
@RunWith(AlchemyTestRunner.class)
public class GoogleImageLoaderTest
{

    @Mock
    private GooglePlacesAPI google;

    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;

    @Mock
    private StoreSearchAlgorithm<Place> searchAlgorithm;

    private GoogleImageLoader instance;

    private Store store;

    @GeneratePojo
    private Place matchingPlace;
    
    @GenerateList(value = Photo.class, size = 15)
    private List<Photo> photos;

    private List<URL> urls;
    private Map<Photo, URL> urlPairings;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new GoogleImageLoader(aroma, google, searchAlgorithm);
    }

    private void setupData() throws Exception
    {
        store = one(stores());

        matchingPlace.photos = photos;

        urls = Lists.create();
        urlPairings = Maps.create();

        for (Photo photo : photos)
        {
            URL url = one(httpUrls());
            urls.add(url);
            urlPairings.put(photo, url);
        }

    }

    private void setupMocks() throws Exception
    {
        when(searchAlgorithm.findMatchFor(store)).thenReturn(matchingPlace);
        
        for (Photo photo : photos)
        {
            GetPhotoRequest photoRequest = createExpectedPhotoRequestFor(photo);

            URL expectedURL = urlPairings.get(photo);
            when(google.getPhoto(photoRequest)).thenReturn(expectedURL);
        }
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new GoogleImageLoader(null, google, searchAlgorithm))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new GoogleImageLoader(aroma, null, searchAlgorithm))
            .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new GoogleImageLoader(aroma, google, null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGetImageFor()
    {
        List<URL> result = instance.getImagesFor(store);

        assertThat(result, notNullValue());
        assertThat(result, is(urls));
    }

    @DontRepeat
    @Test
    public void testGetImageForWhenNoMatch() throws Exception
    {
        when(searchAlgorithm.findMatchFor(store)).thenReturn(null);

        List<URL> result = instance.getImagesFor(store);
        assertThat(result, notNullValue());
        assertThat(result, is(empty()));
    }
    
    @DontRepeat
    @Test
    public void testGetImageForWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.getImagesFor(null))
            .isInstanceOf(BadArgumentException.class);
    }

    private GetPhotoRequest createExpectedPhotoRequestFor(Photo photo)
    {
        return GetPhotoRequest.newBuilder()
            .withPhotoReference(photo.photoReference)
            .withMaxHeight(GetPhotoRequest.Builder.MAX_HEIGHT)
            .build();
    }

}
