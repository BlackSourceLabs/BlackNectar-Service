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

package tech.blackhole.blacknectar.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Spark;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blackhole.blacknectar.service.api.BlackNectarSearchRequest;
import tech.blackhole.blacknectar.service.api.BlackNectarService;
import tech.blackhole.blacknectar.service.api.operations.SayHelloOperation;
import tech.blackhole.blacknectar.service.exceptions.BadArgumentException;
import tech.blackhole.blacknectar.service.exceptions.BlackNectarAPIException;
import tech.blackhole.blacknectar.service.exceptions.BlackNectarExceptionHandler;
import tech.blackhole.blacknectar.service.stores.Location;
import tech.blackhole.blacknectar.service.stores.Store;

import static tech.blackhole.blacknectar.service.stores.Location.validLatitude;
import static tech.blackhole.blacknectar.service.stores.Location.validLongitude;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.decimalString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.integerString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.stringWithLengthGreaterThanOrEqualTo;

/**
 *
 * @author SirWellington
 */
/*
 * Ideally, this Server class is only responsible for setting up a routes,
 * with each API call getting its own Dedicated RequestHandler class, for focus.
 */
public final class Server
{
    //STATIC VARIABLES
    private final static Logger LOG = LoggerFactory.getLogger(Server.class);
    public final static Aroma AROMA = Aroma.create("ec07e6fe-7203-4f18-abf4-f33b48ec904d");
    final static String APPLICATION_JSON = "application/json";
    private final BlackNectarService service = BlackNectarService.newMemoryService();
    
    //INSTANCE VARIABLES
    private final SayHelloOperation sayHelloOperation = new SayHelloOperation(AROMA);
    
    public static void main(String[] args)
    {
        final int port = 9100;
        
        Server server = new Server();
        server.serveAtPort(port);
        server.setupRoutes();
        server.setupExceptionHandler();
    }
    
    void serveAtPort(int port)
    {
        LOG.info("Starting server at {}");
        Spark.port(port);
        
        AROMA.begin()
            .titled("Service Launched")
            .withUrgency(Urgency.LOW)
            .send();
    }
    
    void setupRoutes()
    {
        Spark.get("/stores", this::searchStores);
        Spark.get("/sample-store", this::getSampleStore);
        Spark.get("/", this.sayHelloOperation);
    }
    
    void setupExceptionHandler()
    {
        Spark.exception(BlackNectarAPIException.class, new BlackNectarExceptionHandler(AROMA));
    }
    
    JsonArray getSampleStore(Request request, Response response)
    {
        LOG.info("Received GET request to GET a Sample Store from IP [{}]", request.ip());

        AROMA.begin().titled("Request Received")
            .text("Request to get sample store from IP [{}]", request.ip())
            .withUrgency(Urgency.LOW)
            .send();

        response.status(200);
        response.type(APPLICATION_JSON);

        try
        {
            Store store = Store.SAMPLE_STORE;
            
            JsonArray json = new JsonArray();
            json.add(store.asJSON());
            return json;
        }
        catch (Exception ex)
        {
            AROMA.begin().titled("Request Failed")
                .text("Could not load Store, {}", ex)
                .withUrgency(Urgency.HIGH)
                .send();
            
            return new JsonArray();
        }
    }

    JsonArray searchStores(Request request, Response response)
    {
        LOG.info("Received GET request to search stores from IP [{}]", request.ip());

        AROMA.begin().titled("Request Received")
            .text("To get stores from IP [{}] with query params: [{}]", request.ip(), request.queryString())
            .withUrgency(Urgency.LOW)
            .send();

        response.status(200);
        response.type(APPLICATION_JSON);

        Supplier<JsonArray> supplier = () -> new JsonArray();
        BiConsumer<JsonArray, JsonObject> accumulator = (array, object) -> array.add(object);
        BiConsumer<JsonArray, JsonArray> combiner = (first, second) -> first.addAll(second);

        List<Store> stores = findStores(request);
        
        LOG.debug("Found {} stores to match query parameters: {}", stores.size(), request.queryString());
        
        AROMA.begin().titled("Request Complete")
            .text("Found {} stores to match query parameters {}", stores.size(), request.queryString())
            .withUrgency(Urgency.LOW)
            .send();
        
        return stores.stream()
            .map(Store::asJSON)
            .collect(supplier, accumulator, combiner);
    }

    private List<Store> findStores(Request request)
    {
    
        BlackNectarSearchRequest searchRequest = createSearchRequestFrom(request);
        
        return service.searchForStores(searchRequest);
    }

    private BlackNectarSearchRequest createSearchRequestFrom(Request request)
    {
        BlackNectarSearchRequest searchRequest = new BlackNectarSearchRequest();
        
        QueryParamsMap queryParameters = request.queryMap();
        
        insertLocationIfPresentInto(searchRequest, queryParameters);
        insertRadiusIfPresentInto(searchRequest, queryParameters);
        insertLimitIfPresentInto(searchRequest, queryParameters);
        insertSearchTermIfPresentInto(searchRequest, queryParameters);
        
        return searchRequest;
    }

    private void insertLocationIfPresentInto(BlackNectarSearchRequest request, QueryParamsMap queryParameters)
    {
        if (!hasLocationParameters(queryParameters))
        {
            return;
        }
        
        String latitudeString = queryParameters.get(QueryKeys.LATITUDE).value();
        String longitudeString = queryParameters.get(QueryKeys.LONGITUDE).value();

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


    static class QueryKeys
    {

        static final String LATITUDE = "lat";
        static final String LONGITUDE = "lon";
        static final String LIMIT = "limit";
        static final String RADIUS = "radius";
        static final String SEARCH_TERM = "searchTerm";
    }
    
}
