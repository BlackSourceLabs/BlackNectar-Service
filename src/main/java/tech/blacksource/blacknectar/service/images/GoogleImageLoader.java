
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
import java.util.Objects;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blacksource.blacknectar.service.algorithms.StoreSearchAlgorithm;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.google.places.GooglePlacesAPI;
import tech.redroma.google.places.data.Photo;
import tech.redroma.google.places.data.Place;
import tech.redroma.google.places.exceptions.GooglePlacesException;
import tech.redroma.google.places.requests.GetPhotoRequest;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static java.util.stream.Collectors.toList;
import static tech.blacksource.blacknectar.service.stores.Store.validStore;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@Internal
final class GoogleImageLoader implements ImageLoader
{

    private final static Logger LOG = LoggerFactory.getLogger(GoogleImageLoader.class);

    private final Aroma aroma;
    private final GooglePlacesAPI google;
    private final StoreSearchAlgorithm<Place> googlePlaces;

    @Inject
    GoogleImageLoader(Aroma aroma, GooglePlacesAPI google, StoreSearchAlgorithm<Place> googlePlaces)
    {
        checkThat(aroma, google, googlePlaces)
            .are(notNull());

        this.aroma = aroma;
        this.google = google;
        this.googlePlaces = googlePlaces;
    }

    @Override
    public List<URL> getImagesFor(Store store)
    {
        checkThat(store)
            .throwing(BadArgumentException.class)
            .is(validStore());
        
        Place matchingPlace = googlePlaces.findMatchFor(store);

        if (Objects.isNull(matchingPlace))
        {
            return Lists.emptyList();
        }

        if (!matchingPlace.hasPhotos())
        {
            makeNoteThatPlaceHasNoPhotos(store, matchingPlace);
            return Lists.emptyList();
        }

        List<URL> urls = matchingPlace.photos.stream()
            .map(photo -> this.loadPhoto(photo, matchingPlace))
            .filter(Objects::nonNull)
            .collect(toList());

        return urls;
    }

    private GetPhotoRequest createRequestToGetPhoto(Photo photo)
    {
        return GetPhotoRequest.newBuilder()
            .withPhotoReference(photo.photoReference)
            .withMaxHeight(GetPhotoRequest.Builder.MAX_HEIGHT)
            .build();
    }

    private URL loadPhoto(Photo photo, Place place)
    {
        GetPhotoRequest photoRequest = createRequestToGetPhoto(photo);

        try
        {
            return google.getPhoto(photoRequest);
        }
        catch (GooglePlacesException ex)
        {
            makeNoteThatGooglePhotoCallFailed(photo, place, ex);
            return null;
        }
    }

    //================================================================
    // Notes of Completion
    //================================================================
    private void makeNoteThatPlaceHasNoPhotos(Store store, Place place)
    {
        LOG.info("Matching store has no photos: {}", place);

        aroma.begin().titled("No Image Found")
            .text("Google Place has no image.\n\nStore:\n{}\n\nPlace:\n{}", store, place)
            .withUrgency(Urgency.LOW);
    }

    private void makeNoteThatGooglePhotoCallFailed(Photo photo, Place place, GooglePlacesException ex)
    {
        LOG.error("API call to Google Places failed", ex);

        aroma.begin().titled("Image Load Failed")
            .text("API Call to Google failed.\nPhoto: {}\n\nPlace:\n{}\n\n{}", photo, place, ex)
            .withUrgency(Urgency.HIGH);
    }

}
