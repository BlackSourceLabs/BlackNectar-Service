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

import java.net.MalformedURLException;
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
import tech.blacksource.blacknectar.service.stores.Location;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.yelp.Coordinate;
import tech.redroma.yelp.YelpAPI;
import tech.redroma.yelp.YelpBusiness;
import tech.redroma.yelp.YelpSearchRequest;
import tech.redroma.yelp.exceptions.YelpException;

import static com.google.common.base.Strings.isNullOrEmpty;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
final class YelpImageLoader implements ImageLoader
{

    private final static Logger LOG = LoggerFactory.getLogger(YelpImageLoader.class);
    
    /**
     * The default limit to use when searching for Yelp Stores.
     */
    final static int DEFAULT_YELP_LIMIT = 15;

    private final Aroma aroma;
    private final StoreMatchingAlgorithm<YelpBusiness> matchingAlgorithm;
    private final YelpAPI yelp;

    @Inject
    YelpImageLoader(Aroma aroma, StoreMatchingAlgorithm<YelpBusiness> matchingAlgorithm, YelpAPI yelp)
    {
        checkThat(aroma, matchingAlgorithm, yelp)
            .are(notNull());

        this.aroma = aroma;
        this.matchingAlgorithm = matchingAlgorithm;
        this.yelp = yelp;
    }

    @Override
    public URL getImageFor(Store store)
    {
        checkThat(store).is(notNull());

        String url = tryToGetAPhotoURLFor(store);

        if (isNullOrEmpty(url))
        {
            return null;
        }
        else
        {
            try
            {
                return new URL(url);
            }
            catch (MalformedURLException ex)
            {
                LOG.error("Failed to parse URL: [{}]", url, ex);
                aroma.begin().titled("URL Parse Failed")
                    .text("Could not parse URL: [{}]", url, ex)
                    .withUrgency(Urgency.LOW)
                    .send();

                return null;
            }
        }

    }

    private String tryToGetAPhotoURLFor(Store store)
    {
        YelpSearchRequest request = buildRequestFor(store);

        List<YelpBusiness> results = null;

        try
        {
            results = yelp.searchForBusinesses(request);
        }
        catch (YelpException ex)
        {
            String message = "Failed to search yelp for business information";

            LOG.error(message, ex);
            aroma.begin().titled("Yelp Call Failed")
                .text("{}\n\nFor Request:\n{}", message, request, ex)
                .withUrgency(Urgency.HIGH)
                .send();
        }

        if (!Lists.isEmpty(results))
        {
            makeNoteOfYelpRequest(request, results, store);

            YelpBusiness yelpStore = tryToFindMatchingBusiness(results, store);

            if (Objects.nonNull(yelpStore) && Objects.nonNull(yelpStore.imageURL))
            {
                return yelpStore.imageURL;
            }
        }

        return null;
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

    private YelpBusiness tryToFindMatchingBusiness(List<YelpBusiness> results, Store store)
    {

        for (YelpBusiness business : results)
        {
            if (areSimilar(business, store))
            {
                makeNoteThatBusinessPickedForStore(business, store);

                return business;
            }
        }

        makeNotThatYelpMatchFailedFor(store);

        return null;
    }

    private boolean areSimilar(YelpBusiness business, Store store)
    {
        return matchingAlgorithm.matchesStore(business, store);
    }

    private void makeNoteOfYelpRequest(YelpSearchRequest request, List<YelpBusiness> results, Store store)
    {
        String message = "Yelp request {} \n\nResulted in {} results \n\nFor Store [{}]:\n\n{}";
        LOG.debug(message, request, results.size(), store);
        aroma.begin().titled("Yelp Request Complete")
            .text(message, request, results.size(), store, results)
            .withUrgency(Urgency.LOW)
            .send();
    }

    private void makeNotThatYelpMatchFailedFor(Store store)
    {
        String message = "Could not find a Yelp Store close to: \n\n{}";
        LOG.debug(message, store);
        aroma.begin().titled("Yelp Match Failed")
            .text(message, store)
            .withUrgency(Urgency.LOW)
            .send();
    }

    private void makeNoteThatBusinessPickedForStore(YelpBusiness business, Store store)
    {
        String message = "Picked Yelp Business [{}] for Store [{}]";

        LOG.debug(message, business, store);

        aroma.begin().titled("Store Picked")
            .text("Business: {}\n\n For Store: \n\n{}", business, store)
            .withUrgency(Urgency.LOW)
            .send();
    }

}
