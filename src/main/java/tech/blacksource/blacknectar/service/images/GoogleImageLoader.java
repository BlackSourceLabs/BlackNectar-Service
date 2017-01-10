
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
import tech.blacksource.blacknectar.service.algorithms.StoreMatchingAlgorithm;
import tech.blacksource.blacknectar.service.exceptions.OperationFailedException;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.google.places.GooglePlacesAPI;
import tech.redroma.google.places.data.Location;
import tech.redroma.google.places.data.Photo;
import tech.redroma.google.places.data.Place;
import tech.redroma.google.places.exceptions.GooglePlacesException;
import tech.redroma.google.places.requests.GetPhotoRequest;
import tech.redroma.google.places.requests.NearbySearchRequest;
import tech.sirwellington.alchemy.annotations.access.Internal;

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
    static final int DEFAULT_RADIUS = 1_000;

    private final Aroma aroma;
    private final GooglePlacesAPI google;
    private final StoreMatchingAlgorithm<Place> matchingAlgorithm;

    @Inject
    GoogleImageLoader(Aroma aroma, GooglePlacesAPI google, StoreMatchingAlgorithm<Place> matchingAlgorithm)
    {
        checkThat(aroma, google, matchingAlgorithm)
            .are(notNull());

        this.aroma = aroma;
        this.google = google;
        this.matchingAlgorithm = matchingAlgorithm;
    }

    @Override
    public URL getImageFor(Store store)
    {
        NearbySearchRequest request = createRequestToSearchFor(store);

        List<Place> places;

        try
        {
            places = google.simpleSearchNearbyPlaces(request);
        }
        catch (GooglePlacesException ex)
        {
            makeNoteThatGoogleSearchFailed(ex, request, store);
            throw new OperationFailedException("Could not make Google API Request", ex);
        }

        if (Lists.isEmpty(places))
        {
            makeNoteThatNoPlacesFoundFor(store, request);
            return null;
        }

        Place matchingPlace = tryToFindMatchingPlaceFor(store, places);

        if (Objects.isNull(matchingPlace))
        {
            makeNoteThatNoMatchesFoundFor(store, places);
            return null;
        }

        if (!matchingPlace.hasPhotos())
        {
            makeNoteThatPlaceHasNoPhotos(store, matchingPlace);
            return null;
        }

        Photo photo = Lists.oneOf(matchingPlace.photos);
        GetPhotoRequest photoRequest = createRequestToGetPhoto(photo);

        try
        {
            return google.getPhoto(photoRequest);
        }
        catch (GooglePlacesException ex)
        {
            makeNoteThatGooglePhotoCallFailed(photo, matchingPlace, ex);
            throw new OperationFailedException(ex);
        }
    }

    private NearbySearchRequest createRequestToSearchFor(Store store)
    {
        Location location = Location.of(store.getLocation().getLatitude(), store.getLocation().getLongitude());

        return NearbySearchRequest.newBuilder()
            .withKeyword(store.getName().toLowerCase())
            .withLocation(location)
            .withRadiusInMeters(DEFAULT_RADIUS)
            .build();

    }

    private GetPhotoRequest createRequestToGetPhoto(Photo photo)
    {
        return GetPhotoRequest.newBuilder()
            .withPhotoReference(photo.photoReference)
            .withMaxHeight(GetPhotoRequest.Builder.MAX_HEIGHT)
            .build();
    }

    private Place tryToFindMatchingPlaceFor(Store store, List<Place> places)
    {
        return places.stream()
            .filter(place -> matchingAlgorithm.matchesStore(place, store))
            .findFirst()
            .orElse(null);
    }

    private void makeNoteThatGoogleSearchFailed(GooglePlacesException ex, NearbySearchRequest request, Store store)
    {
        LOG.error("Failed to execute Google Place Search for store: [{}]", store, ex);

        aroma.begin().titled("Google API Failed")
            .text("Failed to execute Google Place Search. \n\nStore:\n{}\n\nRequest:\n{}\n\n{}", store, request, ex)
            .withUrgency(Urgency.HIGH)
            .send();
    }

    private void makeNoteThatNoMatchesFoundFor(Store store, List<Place> places)
    {
        LOG.warn("No matches found for store: [{}]", store);

        aroma.begin().titled("No Store Matches")
            .text("No Google Places found matching store:\n\n{}\n\nAmong results:\n{}", store, places)
            .withUrgency(Urgency.MEDIUM)
            .send();
    }

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

    private void makeNoteThatNoPlacesFoundFor(Store store, NearbySearchRequest request)
    {
        LOG.warn("No matches found for store: [{}]", store);

        aroma.begin().titled("No Places Found")
            .text("No Google Places found for Store:\n\n{}\n\nFor Request:\n{}", store, request)
            .withUrgency(Urgency.MEDIUM)
            .send();
    }

}
