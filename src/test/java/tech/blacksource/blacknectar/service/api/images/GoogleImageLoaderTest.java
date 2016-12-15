/*
 * Copyright 2016 BlackSourceLabs.
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

package tech.blacksource.blacknectar.service.api.images;

import java.net.URL;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.google.places.GooglePlacesAPI;
import tech.redroma.google.places.data.Location;
import tech.redroma.google.places.data.Photo;
import tech.redroma.google.places.data.Place;
import tech.redroma.google.places.requests.GetPhotoRequest;
import tech.redroma.google.places.requests.NearbySearchRequest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateList;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.GenerateURL;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Mockito.when;
import static tech.blacksource.blacknectar.service.api.images.GoogleImageLoader.DEFAULT_RADIUS;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class GoogleImageLoaderTest
{

    @GeneratePojo
    private Store store;

    @GenerateList(Place.class)
    private List<Place> places;
    
    private Place matchingPlace;
    private Photo photo;
    
    @GenerateURL(protocol = "https")
    private URL expectedURL;

    private NearbySearchRequest expectedRequest;
    
    @Mock
    private GooglePlacesAPI google;
    
    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;

    private GoogleImageLoader instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();
        
        instance = new GoogleImageLoader(aroma, google);
    }

    private void setupData() throws Exception
    {
        expectedRequest = createExpectedRequestFor(store);
        matchingPlace = Lists.oneOf(places);
        matchingPlace.name = store.getName();
        photo = Lists.oneOf(matchingPlace.photos);
        matchingPlace.photos = Lists.createFrom(photo);
    }

    private void setupMocks() throws Exception
    {
        GetPhotoRequest photoRequest = createExpectedPhotoRequestFor(photo);
        
        when(google.getPhoto(photoRequest)).thenReturn(expectedURL);
        
        when(google.simpleSearchNearbyPlaces(expectedRequest))
            .thenReturn(places);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new GoogleImageLoader(null, google))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new GoogleImageLoader(aroma, null))
            .isInstanceOf(IllegalArgumentException.class);
        
    }
    
    @Test
    public void testGetImageFor()
    {
        URL result = instance.getImageFor(store);
        assertThat(result, notNullValue());
        assertThat(result, is(expectedURL));
    }

    private NearbySearchRequest createExpectedRequestFor(Store store)
    {
        Location location = Location.of(store.getLocation().getLatitude(), store.getLocation().getLongitude());

        return NearbySearchRequest.newBuilder()
            .withKeyword(store.getName())
            .withLocation(location)
            .withRadiusInMeters(DEFAULT_RADIUS)
            .build();
    }

    private GetPhotoRequest createExpectedPhotoRequestFor(Photo photo)
    {
        return GetPhotoRequest.newBuilder()
            .withPhotoReference(photo.photoReference)
            .withMaxHeight(GetPhotoRequest.Builder.MAX_HEIGHT)
            .build();
    }

}
