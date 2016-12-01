/*
 * Copyright 2016 BlackWholeLabs.
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

 
package tech.blacksource.blacknectar.service.api.operations;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.sets.Sets;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Route;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blacksource.blacknectar.service.api.BlackNectarSearchRequest;
import tech.blacksource.blacknectar.service.api.BlackNectarService;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.exceptions.OperationFailedException;
import tech.blacksource.blacknectar.service.stores.Location;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;

<<<<<<< HEAD:src/main/java/tech/blackhole/blacknectar/service/api/operations/SearchStoresOperation.java
import static tech.blackhole.blacknectar.service.api.MediaTypes.APPLICATION_JSON;
=======
import static tech.blacksource.blacknectar.service.api.MediaTypes.APPLICATION_JSON;
import static tech.blacksource.blacknectar.service.stores.Location.validLatitude;
import static tech.blacksource.blacknectar.service.stores.Location.validLongitude;
>>>>>>> develop:src/main/java/tech/blacksource/blacknectar/service/api/operations/SearchStoresOperation.java
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.elementInCollection;
import static tech.sirwellington.alchemy.arguments.assertions.GeolocationAssertions.validLatitude;
import static tech.sirwellington.alchemy.arguments.assertions.GeolocationAssertions.validLongitude;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.decimalString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.integerString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.stringWithLengthGreaterThanOrEqualTo;

/**
 * This operation allows searching for Stores.
 * It takes the query parameters and constructs a {@link BlackNectarSearchRequest} object that it then
 * passes to the {@link BlackNectarService}.
 * 
 * @author SirWellington
 */
public class SearchStoresOperation implements Route
{
    private final static Logger LOG = LoggerFactory.getLogger(SearchStoresOperation.class);
    /** In the event that queries do not include a limit, this one is injected. */
    private final static int DEFAULT_LIMIT = 250;
    
    private final Aroma aroma;
    private final BlackNectarService service;

    @Inject
    public SearchStoresOperation(Aroma aroma, BlackNectarService service)
    {
        checkThat(aroma, service)
            .are(notNull());
        
        this.aroma = aroma;
        this.service = service;
    }

    @Override
    public JsonArray handle(Request request, Response response) throws Exception
    {
        checkThat(request, response)
            .usingMessage("request and response cannot be null")
            .throwing(BadArgumentException.class)
            .are(notNull());
        
        checkThat(request)
            .throwing(ex -> new BadArgumentException(ex))
            .is(validRequest());
        
        long begin = System.currentTimeMillis();
        
        LOG.info("Received GET request to search stores from IP [{}]", request.ip());

        aroma.begin().titled("Request Received")
            .text("To get stores from IP [{}] with query params: [{}]", request.ip(), request.queryString())
            .withUrgency(Urgency.LOW)
            .send();

        Supplier<JsonArray> supplier = () -> new JsonArray();
        BiConsumer<JsonArray, JsonObject> accumulator = (array, object) -> array.add(object);
        BiConsumer<JsonArray, JsonArray> combiner = (first, second) -> first.addAll(second);

        List<Store> stores = findStores(request);
        
        JsonArray json = stores.stream()
            .map(Store::asJSON)
            .collect(supplier, accumulator, combiner);
        
        {
            long delay = System.currentTimeMillis() - begin;
            String message = "Operation to search for stores with query parameters [{}] took {}ms and resulted in {} stores";
            LOG.debug(message, request.queryString(), delay, json.size());

            aroma.begin().titled("Request Complete")
                .text(message, request.queryString(), delay, json.size())
                .withUrgency(Urgency.LOW)
                .send();
        }

        response.status(200);
        response.type(APPLICATION_JSON);
        
        return json;
    }

    private List<Store> findStores(Request request)
    {
        
        BlackNectarSearchRequest searchRequest = createSearchRequestFrom(request);
        
        try
        {
            return service.searchForStores(searchRequest);
        }
        catch (Exception ex)
        {
            throw new OperationFailedException(ex);
        }
    }

    private BlackNectarSearchRequest createSearchRequestFrom(Request request)
    {
        BlackNectarSearchRequest searchRequest = new BlackNectarSearchRequest();

        QueryParamsMap queryParameters = request.queryMap();

        insertLocationIfPresentInto(searchRequest, queryParameters);
        insertRadiusIfPresentInto(searchRequest, queryParameters);
        insertSearchTermIfPresentInto(searchRequest, queryParameters);
        insertLimitIfPresentInto(searchRequest, queryParameters);

        return searchRequest;
    }

    private void insertLocationIfPresentInto(BlackNectarSearchRequest request, QueryParamsMap queryParameters)
    {
        if (!hasLocationParameters(queryParameters))
        {
            return;
        }

        String latitudeString = queryParameters.value(QueryKeys.LATITUDE);
        String longitudeString = queryParameters.value(QueryKeys.LONGITUDE);

        checkThat(latitudeString, longitudeString)
            .usingMessage("latitude and longitude must be numerical")
            .throwing(BadArgumentException.class)
            .are(decimalString());

        double latitude = Double.valueOf(latitudeString);
        double longitude = Double.valueOf(longitudeString);

        checkThat(latitude)
            .throwing(BadArgumentException.class)
            .is(validLatitude());

        checkThat(longitude)
            .throwing(BadArgumentException.class)
            .is(validLongitude());

        request.withCenter(new Location(latitude, longitude));
    }

    private void insertRadiusIfPresentInto(BlackNectarSearchRequest request, QueryParamsMap queryParameters)
    {
        if (!hasRadiusParameter(queryParameters))
        {
            request.withRadius(BlackNectarService.DEFAULT_RADIUS);
            return;
        }
        
        String radiusString = queryParameters.value(QueryKeys.RADIUS);

        checkThat(radiusString)
            .throwing(BadArgumentException.class)
            .usingMessage("radius parameter must be a decimal value")
            .is(decimalString());

        double radius = Double.valueOf(radiusString);

        checkThat(radius)
            .throwing(BadArgumentException.class)
            .usingMessage("radius must be > 0")
            .is(greaterThanOrEqualTo(0.0));

        request.withRadius(radius);
    }

    private void insertLimitIfPresentInto(BlackNectarSearchRequest request, QueryParamsMap queryParameters)
    {
        if (!hasLimitParameter(queryParameters))
        {
            request.withLimit(DEFAULT_LIMIT);
            return;
        }

        String limitString = queryParameters.value(QueryKeys.LIMIT);

        checkThat(limitString)
            .throwing(BadArgumentException.class)
            .usingMessage("limit must be a number")
            .is(integerString());

        int limit = Integer.valueOf(limitString);

        checkThat(limit)
            .throwing(BadArgumentException.class)
            .usingMessage("limit must be > 0")
            .is(greaterThanOrEqualTo(0));

        request.withLimit(limit);

    }

    private void insertSearchTermIfPresentInto(BlackNectarSearchRequest request, QueryParamsMap queryParameters)
    {
        if (!hasSearchTermParameter(queryParameters))
        {
            return;
        }

        String searchTerm = queryParameters.value(QueryKeys.SEARCH_TERM);
        checkThat(searchTerm)
            .throwing(BadArgumentException.class)
            .usingMessage("search term cannot be empty")
            .is(nonEmptyString())
            .usingMessage("search term must have at least 2 characters")
            .is(stringWithLengthGreaterThanOrEqualTo(2));

        request.withSearchTerm(searchTerm);
    }

    private boolean hasLocationParameters(QueryParamsMap queryParams)
    {
        return queryParams.hasKey(QueryKeys.LATITUDE) &&
               queryParams.hasKey(QueryKeys.LONGITUDE);
    }

    private boolean hasLimitParameter(QueryParamsMap queryParams)
    {
        return queryParams.hasKey(QueryKeys.LIMIT);
    }

    private boolean hasSearchTermParameter(QueryParamsMap queryParams)
    {
        return queryParams.hasKey(QueryKeys.SEARCH_TERM);
    }

    private boolean hasRadiusParameter(QueryParamsMap queryParams)
    {
        return queryParams.hasKey(QueryKeys.RADIUS);
    }

    private AlchemyAssertion<Request> validRequest()
    {
        return request ->
        {
            Set<String> queryParams = request.queryParams();
            
            for (String key : queryParams)
            {
                checkThat(key)
                    .usingMessage("Unexpected empty query parameter")
                    .is(nonEmptyString())
                    .usingMessage("Unrecognized Query Parameter: " + key)
                    .is(elementInCollection(QueryKeys.KEYS));
            }
        };
    }

    static class QueryKeys
    {

        static final String LATITUDE = "latitude";
        static final String LONGITUDE = "longitude";
        static final String LIMIT = "limit";
        static final String RADIUS = "radius";
        static final String SEARCH_TERM = "searchTerm";
        
        static Set<String> KEYS = Sets.createFrom(LATITUDE, LONGITUDE, LIMIT, RADIUS, SEARCH_TERM);
    }
}