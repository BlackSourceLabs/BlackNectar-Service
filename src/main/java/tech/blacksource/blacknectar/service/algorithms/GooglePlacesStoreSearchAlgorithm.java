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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.aroma.client.Priority;
import tech.blacksource.blacknectar.service.exceptions.OperationFailedException;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.google.places.GooglePlacesAPI;
import tech.redroma.google.places.data.Location;
import tech.redroma.google.places.data.Place;
import tech.redroma.google.places.exceptions.GooglePlacesException;
import tech.redroma.google.places.requests.NearbySearchRequest;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import javax.inject.Inject;
import java.util.List;

import static tech.blacksource.blacknectar.service.stores.Store.validStore;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
@StrategyPattern(role = CONCRETE_BEHAVIOR)
class GooglePlacesStoreSearchAlgorithm implements StoreSearchAlgorithm<Place>
{
    
    private final static Logger LOG = LoggerFactory.getLogger(GooglePlacesStoreSearchAlgorithm.class);
    static final int DEFAULT_RADIUS = 4_000;
    
    private final Aroma aroma;
    private final GooglePlacesAPI google;
    private final StoreMatchingAlgorithm<Place> matchingAlgorithm;
    
    @Inject
    GooglePlacesStoreSearchAlgorithm(Aroma aroma, GooglePlacesAPI google, StoreMatchingAlgorithm<Place> matchingAlgorithm)
    {
        checkThat(aroma, google, matchingAlgorithm)
            .are(notNull());
        
        this.aroma = aroma;
        this.google = google;
        this.matchingAlgorithm = matchingAlgorithm;
    }
    
    @Override
    public Place findMatchFor(Store store)
    {
        checkStore(store);
        
        List<Place> places = tryToGetPlacesFor(store);
        if (Lists.isEmpty(places))
        {
            return null;
        }
        
        Place result = tryToFindMatchingPlaceFor(store, places);
        
        if (result == null)
        {
            makeNoteThatNoMatchesFoundFor(store, places);
        }
        else        
        {
            makeNoteThatPlaceMatched(store, result);
        }
        
        return result;
    }
    
    private List<Place> tryToGetPlacesFor(Store store)
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
        }
        
        return Lists.nullToEmpty(places);
    }
    
    private Place tryToFindMatchingPlaceFor(Store store, List<Place> places)
    {
        return places.stream()
            .filter(place -> matchingAlgorithm.matchesStore(place, store))
            .findFirst()
            .orElse(null);
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
    
    private void checkStore(Store store)
    {
        checkThat(store)
            .is(notNull())
            .is(validStore());
    }
    
    private void makeNoteThatGoogleSearchFailed(GooglePlacesException ex, NearbySearchRequest request, Store store)
    {
        LOG.error("Failed to execute Google Place Search for store: [{}]", store, ex);
        
        aroma.begin().titled("Google API Failed")
            .withBody("Failed to execute Google Place Search. \n\nStore:\n{}\n\nRequest:\n{}\n\n{}", store, request, ex)
            .withPriority(Priority.HIGH)
            .send();
    }
    
    private void makeNoteThatNoPlacesFoundFor(Store store, NearbySearchRequest request)
    {
        LOG.warn("No matches found for store: [{}]", store);
        
        aroma.begin().titled("No Places Found")
            .withBody("No Google Places found for Store:\n\n{}\n\nFor Request:\n{}", store, request)
            .withPriority(Priority.MEDIUM)
            .send();
    }
    
    private void makeNoteThatNoMatchesFoundFor(Store store, List<Place> places)
    {
        LOG.warn("No matches found for store: [{}]", store);
        
        aroma.begin().titled("No Store Matches")
            .withBody("No Google Places found matching store:\n\n{}\n\nAmong results:\n{}", store, places)
            .withPriority(Priority.MEDIUM)
            .send();
    }
    
    private void makeNoteThatPlaceMatched(Store store, Place place)
    {
        LOG.debug("Store {} matched with Place | {}", store, place);
        
        aroma.begin().titled("Google Places Match Found")
            .withBody("Store \n\n{}\n\nMatched with:\n\n{}", store, place)
            .withPriority(Priority.LOW)
            .send();
    }
}
