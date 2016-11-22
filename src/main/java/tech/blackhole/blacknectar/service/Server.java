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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blackhole.blacknectar.service.api.BlackNectarService;
import tech.blackhole.blacknectar.service.api.operations.GetSampleStoreOperation;
import tech.blackhole.blacknectar.service.api.operations.SayHelloOperation;
import tech.blackhole.blacknectar.service.api.operations.SearchStoresOperation;
import tech.blackhole.blacknectar.service.exceptions.BlackNectarAPIException;
import tech.blackhole.blacknectar.service.exceptions.BlackNectarExceptionHandler;

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
    
    //INSTANCE VARIABLES
    private final SayHelloOperation sayHelloOperation = new SayHelloOperation(AROMA);
    private final GetSampleStoreOperation getSampleStoreOperation = new GetSampleStoreOperation(AROMA);
    private final BlackNectarService service = BlackNectarService.newMemoryService();
    private final SearchStoresOperation searchStoresOperation = new SearchStoresOperation(AROMA, service);
    
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
        Spark.get("/stores", this.searchStoresOperation);
        Spark.get("/sample-store", this.getSampleStoreOperation);
        Spark.get("/", this.sayHelloOperation);
    }
    
    void setupExceptionHandler()
    {
        Spark.exception(BlackNectarAPIException.class, new BlackNectarExceptionHandler(AROMA));
    }
    
    
}
