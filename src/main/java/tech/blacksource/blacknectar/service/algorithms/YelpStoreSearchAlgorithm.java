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
import tech.blacksource.blacknectar.service.stores.Location;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.yelp.*;
import tech.redroma.yelp.exceptions.YelpException;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import javax.inject.Inject;
import java.util.List;

import static tech.blacksource.blacknectar.service.stores.Store.validStore;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * Looks through the Yelp API to find relevant information for EBT stores.
 *
 * @author SirWellington
 */
@StrategyPattern(role = CONCRETE_BEHAVIOR)
final class YelpStoreSearchAlgorithm implements StoreSearchAlgorithm<YelpBusiness>
{
    
    private final static Logger LOG = LoggerFactory.getLogger(YelpStoreSearchAlgorithm.class);

    /**
     * The default limit to use when searching for Yelp Stores.
     */
    final static int DEFAULT_YELP_LIMIT = 15;
    
    private final Aroma aroma;
    private final StoreMatchingAlgorithm<YelpBusiness> matchingAlgorithm;
    private final YelpAPI yelp;
    
    @Inject
    YelpStoreSearchAlgorithm(Aroma aroma, StoreMatchingAlgorithm<YelpBusiness> matchingAlgorithm, YelpAPI yelp)
    {
        checkThat(aroma, matchingAlgorithm, yelp)
            .are(notNull());
        
        this.aroma = aroma;
        this.matchingAlgorithm = matchingAlgorithm;
        this.yelp = yelp;
    }
    
    @Override
    public YelpBusiness findMatchFor(Store store)
    {
        checkThat(store)
            .is(validStore());
        
        YelpSearchRequest request = this.buildRequestFor(store);
        
        List<YelpBusiness> businesses = getBusinesses(request);
        
        if (Lists.isEmpty(businesses))
        {
            makeNoteThatNoBusinessesFoundFor(store);
            return null;
        }
        
        YelpBusiness business = tryToFindMatchingBusiness(businesses, store);
        
        if (business == null)
        {
            makeNoteThatYelpMatchFailedFor(store);
            return null;
        }
        else
        {
            makeNoteThatBusinessPickedForStore(business, store);
            return business;
        }
        
    }
    
    private List<YelpBusiness> getBusinesses(YelpSearchRequest request)
    {
        try
        {
            return yelp.searchForBusinesses(request);
        }
        catch (YelpException ex)
        {
            makeNoteThatYelpRequestFailed(request, ex);
            
            return Lists.emptyList();
        }
    }
    
    private YelpBusiness tryToFindMatchingBusiness(List<YelpBusiness> results, Store store)
    {
        for (YelpBusiness business : results)
        {
            if (areSimilar(business, store))
            {
                return business;
            }
        }
        
        return null;
    }
    
    private boolean areSimilar(YelpBusiness business, Store store)
    {
        return matchingAlgorithm.matchesStore(business, store);
    }
    
    private YelpSearchRequest buildRequestFor(Store store)
    {
        Coordinate coordinate = copyCoordinateFrom(store.getLocation());
        
        return YelpSearchRequest.newBuilder()
            .withCoordinate(coordinate)
            .withLimit(DEFAULT_YELP_LIMIT)
            .withSearchTerm(store.getName())
            .withSortBy(YelpSearchRequest.SortType.DISTANCE)
            .build();
    }
    
    private Coordinate copyCoordinateFrom(Location location)
    {
        return Coordinate.of(location.getLatitude(), location.getLongitude());
    }
    
    private void makeNoteThatYelpMatchFailedFor(Store store)
    {
        String message = "Could not find a Yelp Store close to: \n\n{}";
        LOG.debug(message, store);
        aroma.begin().titled("Yelp Match Failed")
            .withBody(message, store)
            .withPriority(Priority.LOW)
            .send();
    }
    
    private void makeNoteThatBusinessPickedForStore(YelpBusiness business, Store store)
    {
        String message = "Picked Yelp Business [{}] for Store [{}]";
        
        LOG.debug(message, business, store);
        
        aroma.begin().titled("Store Picked")
            .withBody("Business: {}\n\n For Store: \n\n{}", business, store)
            .withPriority(Priority.LOW)
            .send();
    }
    
    private void makeNoteThatNoBusinessesFoundFor(Store store)
    {
        LOG.debug("Found no Yelp Results for store: {}", store);
        
        aroma.begin().titled("No Yelp Matches")
            .withBody("Found no Yelp Results to match store: \n\n{}", store)
            .withPriority(Priority.LOW)
            .send();
    }
    
    private void makeNoteThatYelpRequestFailed(YelpSearchRequest request, YelpException ex)
    {
        String message = "Failed to search yelp for business information";
        
        LOG.error(message, ex);
        
        aroma.begin().titled("Yelp Call Failed")
            .withBody("{}\n\nFor Request:\n{}", message, request, ex)
            .withPriority(Priority.HIGH)
            .send();
    }
}
