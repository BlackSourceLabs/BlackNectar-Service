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
import tech.blackhole.blacknectar.service.stores.Location;
import tech.blackhole.blacknectar.service.stores.Store;

import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.base.Strings.nullToEmpty;

/**
 *
 * @author SirWellington
 */
public final class Server
{
    
    private final static Logger LOG = LoggerFactory.getLogger(Server.class);
    public final static Aroma AROMA = Aroma.create("ec07e6fe-7203-4f18-abf4-f33b48ec904d");
    
    final static String APPLICATION_JSON = "application/json";
    
    private final BlackNectarService service = new MemoryBlackNectarService();
    
    public static void main(String[] args)
    {
        final int port = 9100;
        
        Server server = new Server();
        server.serveAtPort(port);
        server.setupRoutes();
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
        Spark.get("/stores", this::getStores);
        Spark.get("/sample-store", this::getSampleStore);
        Spark.get("/", this::sayHello);
    }
    
    String sayHello(Request request, Response response)
    {
        LOG.info("Received GET request from IP [{}]", request.ip());
        
        AROMA.begin().titled("Request Received")
            .text("From IP [{}]", request.ip())
            .withUrgency(Urgency.LOW)
            .send();
        
        response.status(200);
        //U+1F573
        return "🌑";
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

    JsonArray getStores(Request request, Response response)
    {
        LOG.info("Received GET request to GET all stores from IP [{}]", request.ip());

        AROMA.begin().titled("Request Received")
            .text("To get stores from IP [{}] with query params: [{}]", request.ip(), request.queryMap().toMap())
            .withUrgency(Urgency.LOW)
            .send();

        response.status(200);
        response.type(APPLICATION_JSON);

        Supplier<JsonArray> supplier = () -> new JsonArray();
        BiConsumer<JsonArray, JsonObject> accumulator = (array, object) -> array.add(object);
        BiConsumer<JsonArray, JsonArray> combiner = (first, second) -> first.addAll(second);

        List<Store> stores = findStores(request);
        
        return stores.stream()
            .map(Store::asJSON)
            .collect(supplier, accumulator, combiner);
    }

    private List<Store> findStores(Request request)
    {
        QueryParamsMap queryParams = request.queryMap();
        
        Location center = null;
        
        if (hasLocationParameters(queryParams))
        {
            double latitude = queryParams.get(QueryKeys.LATITUDE).doubleValue();
            double longitude = queryParams.get(QueryKeys.LONGITUDE).doubleValue();
            center = new Location(latitude, longitude);
        }
        
        int limit = 0;
        if (hasLimitParameter(queryParams))
        {
            limit = queryParams.get(QueryKeys.LIMIT).integerValue();
            if (limit <0 )
            {
                LOG.warn("Unexpected limit received: {}", limit);
                
                AROMA.begin()
                    .titled("Bad Argument")
                    .text("Unexpected limit received: {}", limit)
                    .send();
                
                limit = 0;
            }
        }
        
        String searchTerm = "";
        if (hasSearchTermParameter(queryParams))
        {
            searchTerm = queryParams.value(QueryKeys.SEARCH_TERM);
            searchTerm = nullToEmpty(searchTerm);
        }
        
        double radius = -1.0;
        if (hasRadiusParameter(queryParams))
        {
            radius = queryParams.get(QueryKeys.RADIUS).doubleValue();
        }
        
        //Search Term, Center, Radius, Limit
        if (hasSearchTerm(searchTerm) && 
            hasLocationParameters(queryParams) &&
            hasLimit(limit) &&
            hasRadius(radius))
        {
            return service.searchForStoresByName(searchTerm, center, radius, limit);
        }
        
        //Search Term, Center, Radius
        if (hasSearchTerm(searchTerm) &&
            hasLocationParameters(queryParams) &&
            hasRadius(radius))
        {
            return service.searchForStoresByName(searchTerm, center, radius);
        }
        
        //Center, Radius, Limit
        if (hasLocationParameters(queryParams) && 
            hasRadius(radius) &&
            hasLimit(limit))
        {
            return service.searchForStoresByLocation(center, radius, limit);
        }
        
        //Center, Radius
        if (hasLocationParameters(queryParams) && hasRadius(radius))
        {
            return service.searchForStoresByLocation(center, radius);
        }
        
        //Search Term
        if (hasSearchTerm(searchTerm))
        {
            return service.searchForStoresByName(searchTerm);
        }
        
        //Limit
        if (hasLimit(limit))
        {
            return service.getAllStores(limit);
        }
        
        return service.getAllStores();
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

    private boolean hasLimit(int limit)
    {
        return limit > 0;
    }

    private boolean hasSearchTerm(String searchTerm)
    {
        return !isNullOrEmpty(searchTerm);
    }

    private boolean hasRadius(double radius)
    {
        return radius > 0.0;
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
