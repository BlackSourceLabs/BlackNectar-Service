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

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blackhole.blacknectar.service.stores.Store;
import tech.blackhole.blacknectar.service.stores.StoreRepository;

/**
 *
 * @author SirWellington
 */
public final class Server
{
    
    private final static Logger LOG = LoggerFactory.getLogger(Server.class);
    public final static Aroma AROMA = Aroma.create("ec07e6fe-7203-4f18-abf4-f33b48ec904d");
    
    final static String APPLICATION_JSON = "application/json";
    
    private final StoreRepository repository = StoreRepository.FILE;
//    private final List<Store> stores = repository.getAllStores();
    
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
//        Spark.get("/stores", this::getStores);
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
    
    JsonObject getSampleStore(Request request, Response response)
    {
        LOG.info("Received GET request to GET a Sample Store from IP [{}]", request.ip());

        AROMA.begin().titled("Request Received")
            .text("From IP [{}]", request.ip())
            .withUrgency(Urgency.LOW)
            .send();

        response.status(200);
        response.type(APPLICATION_JSON);

        try
        {
            Store store = Store.SAMPLE_STORE;

            return store.asJSON();
        }
        catch (Exception ex)
        {
            AROMA.begin().titled("Request Failed")
                .text("Could not load Store, {}", ex)
                .withUrgency(Urgency.HIGH)
                .send();
            return new JsonObject();
        }
    }

//    JsonArray getStores(Request request, Response response)
//    {
//        LOG.info("Received GET request to GET all stores from IP [{}]", request.ip());
//
//        AROMA.begin().titled("Request Received")
//            .text("From IP [{}]", request.ip())
//            .withUrgency(Urgency.LOW)
//            .send();
//
//        response.status(200);
//        response.type(APPLICATION_JSON);
//        
//        Supplier<JsonArray> supplier = () -> new JsonArray();
//        BiConsumer<JsonArray, JsonObject> accumulator = (array, object) -> array.add(object);
//        BiConsumer<JsonArray, JsonArray> combiner = (first, second) -> first.addAll(second);
//        
//        return stores.stream()
//            .map(Store::asJSON)
//            .collect(supplier, accumulator, combiner);
//    }
}
