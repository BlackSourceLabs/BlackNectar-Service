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

package tech.blacksource.blacknectar.service;

import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blacksource.blacknectar.service.algorithms.StoreSearchAlgorithm;
import tech.blacksource.blacknectar.service.data.SQLQueries;
import tech.blacksource.blacknectar.service.data.StoreRepository;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.google.places.GooglePlacesAPI;
import tech.redroma.google.places.data.Photo;
import tech.redroma.google.places.data.Place;
import tech.redroma.google.places.data.PlaceDetails;
import tech.redroma.google.places.data.Types;

import static java.util.stream.Collectors.toList;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * This Script loads data from Google Places for EBT Stores.
 *
 * @author SirWellington
 */
public class RunLoadGoogleData implements Callable<Void>
{
    
    private final static Logger LOG = LoggerFactory.getLogger(RunLoadGoogleData.class);
    
    private final Aroma aroma;
    private final Gson gson;
    private final GooglePlacesAPI googlePlaces;
    private final StoreRepository storeRepository;
    private final StoreSearchAlgorithm<Place> storeSearch;
    private final JdbcTemplate database;
    
    @Inject
    RunLoadGoogleData(Aroma aroma,
                      Gson gson,
                      GooglePlacesAPI googlePlaces,
                      StoreRepository storeRepository,
                      StoreSearchAlgorithm<Place> storeSearch,
                      JdbcTemplate database)
    {
        checkThat(aroma, gson, googlePlaces, storeRepository, storeSearch, database)
            .are(notNull());
        
        this.aroma = aroma;
        this.gson = gson;
        this.googlePlaces = googlePlaces;
        this.storeRepository = storeRepository;
        this.storeSearch = storeSearch;
        this.database = database;
    }
    
    public static void main(String[] args) throws Exception
    {
        Injector injector = Guice.createInjector(new ModuleServer(), new ModuleDatabaseTesting());
        
        RunLoadGoogleData instance = injector.getInstance(RunLoadGoogleData.class);
        instance.call();
    }
    
    @Override
    public Void call() throws Exception
    {
        List<Store> stores = storeRepository.getAllStores();
        
        int totalStores = stores.size();
        int processed = -1;
        int succeeded = 0;
        int failed = 0;
        
        makeNoteThatScriptStartedWith(totalStores);
        
        for (Store store : stores)
        {
            ++processed;
            
            Place place = tryToFindMatchFor(store);
            
            if (place == null)
            {
                ++failed;
                makeNoteThatNoPlaceFound(store, failed, processed, totalStores);
                continue;
            }
            
            boolean success = tryToStorePlaceInformation(place, store);
            
            if (success)
            {
                ++succeeded;
                makeNoteOfSuccess(succeeded, processed, totalStores);
            }
            else
            {
                ++failed;
                makeNoteOfFailure(failed, processed, totalStores);
            }
            
        }
        
        makeNoteThatScriptCompleted(succeeded, failed, totalStores);
        
        return null;
    }
    
    private Place tryToFindMatchFor(Store store)
    {
        try
        {
            return storeSearch.findMatchFor(store);
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    private boolean tryToStorePlaceInformation(Place place, Store store)
    {
        try
        {
            storePlaceInformation(place, store);
            return true;
        }
        catch (Exception ex)
        {
            makeNoteOfError(ex, place, store);
            return false;
        }
    }
    
    private void storePlaceInformation(Place place, Store store) throws Exception
    {
        PlaceDetails placeDetails = getPlaceDetailsFor(place);
        
        String statement = SQLQueries.INSERT_GOOGLE_DATA;
        
        UUID storeId = UUID.fromString(store.getStoreId());
        String placeId = place.placeId;
        
        database.update(statement,
                        storeId,
                        placeId,
                        place.name,
                        place.rating,
                        place.vicinity,
                        toJsonString(placeDetails.getAddressComponents()),
                        place.formattedAddress,
                        placeDetails.getFormattedPhoneNumber(),
                        placeDetails.getInternationalPhoneNumber(),
                        place.permanentlyClosed,
                        place.geometry.location.latitude,
                        place.geometry.location.longitude,
                        place.iconURL,
                        placeDetails.getUrl(),
                        placeDetails.getWebsite(),
                        placeDetails.getUtcOffset(),
                        toJsonString(placeDetails.getReviews()),
                        toStringArray(place.types),
                        null);
        
        savePhotosFor(placeDetails);
        
    }
    
    private void savePhotosFor(PlaceDetails placeDetails)
    {
        List<Photo> photos = placeDetails.getPhotos();
        if (Lists.isEmpty(photos))
        {
            return;
        }
        
        photos.forEach(p -> this.savePhoto(p, placeDetails));
    }
    
    private void savePhoto(Photo photo, PlaceDetails place)
    {
        String statement = SQLQueries.INSERT_GOOGLE_PHOTO;
        String attributions = String.join(",", Lists.nullToEmpty(photo.htmlAttributions));
        
        database.update(statement,
                        photo.photoReference,
                        place.getPlaceId(),
                        photo.height,
                        photo.width,
                        attributions,
                        null);
    }
    
    private PlaceDetails getPlaceDetailsFor(Place place)
    {
        return googlePlaces.simpleGetPlaceDetails(place);
    }
    
    private String toStringArray(List<Types.ReturnedPlaceType> types)
    {
        List<String> list = types.stream()
            .filter(Objects::nonNull)
            .map(t -> t.toString())
            .collect(toList());
        
        return String.join(",", list);
    }
    
    private String toJsonString(Object object)
    {
        if (Objects.isNull(object))
        {
            return null;
        }
        
        return gson.toJson(object);
    }
    
    private void makeNoteThatScriptStartedWith(int totalStores)
    {
        String message = "Script Started with {} Total stores for processing.";
        LOG.debug(message, totalStores);

        aroma.begin().titled("Script Began")
            .text(message, totalStores)
            .withUrgency(Urgency.LOW)
            .send();
    }

    private void makeNoteThatNoPlaceFound(Store store, int failed, int processed, int totalStores)
    {
        String message = "[{} failed, {} processed, {} remaining, {} total] - No matching place found for store: {}";
        int remaining = totalStores - processed;
        
        LOG.info(message, failed, processed, remaining, totalStores, store);
        
        aroma.begin().titled("Store Skipped")
            .text(message, failed, processed, remaining, totalStores, store)
            .withUrgency(Urgency.MEDIUM)
            .send();
    }
    
    private void makeNoteThatScriptCompleted(int completed, int failed, int totalStores)
    {
        String message = "[{} completed, {} failed, {} total stores] - Completed processing all stores.";
        
        LOG.info(message, completed, failed, totalStores);
        
        aroma.begin().titled("Script Complete")
            .text(message, completed, failed, totalStores)
            .withUrgency(Urgency.HIGH)
            .send();
    }
    
    private void makeNoteOfSuccess(int completed, int processed, int totalStores)
    {
        int remaining = totalStores - processed;
        String message = "[{} succeeded, {} processed, {} remaining, {} total] - Successfully stored Place Information.";
        
        LOG.info(message, completed, processed, remaining, totalStores);
        aroma.begin().titled("Google Place Saved")
            .text(message, completed, processed, remaining, totalStores)
            .withUrgency(Urgency.LOW)
            .send();
    }
    
    private void makeNoteOfFailure(int failed, int processed, int totalStores)
    {
        String message = "[{} failed, {} processed, {} remaining, {} total] - Failed to store Place Information.";
        int remaining = totalStores - processed;
        
        LOG.warn(message, failed, processed, remaining, totalStores);
        aroma.begin().titled("Database Insert Failed")
            .text(message, failed, processed, remaining, totalStores)
            .withUrgency(Urgency.MEDIUM)
            .send();
    }
    
    private void makeNoteOfError(Exception ex, Place place, Store store)
    {
        String message = "Failed to process store: {}";
        
        LOG.error(message, store, ex);
        
        aroma.begin().titled("Script Error")
            .text(message, store, ex)
            .withUrgency(Urgency.HIGH)
            .send();
    }

}
