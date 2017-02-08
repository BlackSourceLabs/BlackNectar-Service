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

import com.google.inject.Guice;
import com.google.inject.Injector;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;
import spark.Service;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blacksource.blacknectar.service.operations.GetSampleStoreOperation;
import tech.blacksource.blacknectar.service.operations.SayHelloOperation;
import tech.blacksource.blacknectar.service.operations.SearchStoresOperation;

import static com.google.common.base.Strings.isNullOrEmpty;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

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
    
    public static Aroma AROMA;
    
    //INSTANCE VARIABLES
    private final Aroma aroma;
    private final SayHelloOperation sayHelloOperation;
    private final GetSampleStoreOperation getSampleStoreOperation;
    private final SearchStoresOperation searchStoresOperation;
    private final ExceptionHandler exceptionHandler;

    @Inject
    Server(Aroma aroma,
           SayHelloOperation sayHelloOperation,
           GetSampleStoreOperation getSampleStoreOperation,
           SearchStoresOperation searchStoresOperation,
           ExceptionHandler exceptionHandler)
    {
        checkThat(aroma, sayHelloOperation, getSampleStoreOperation, searchStoresOperation, exceptionHandler)
            .are(notNull());
        
        this.aroma = aroma;
        this.sayHelloOperation = sayHelloOperation;
        this.getSampleStoreOperation = getSampleStoreOperation;
        this.searchStoresOperation = searchStoresOperation;
        this.exceptionHandler = exceptionHandler;
    }
    

    public static void main(String[] args)
    {
    
        Injector injector;
        Server server;

        try
        {
            injector = Guice.createInjector(new ModuleServer(), new ModuleProductionDatabase());
            AROMA = injector.getInstance(Aroma.class);
            server = injector.getInstance(Server.class);
        }
        catch (RuntimeException ex)
        {
            LOG.error("Server Launch Failed", ex);

            if (AROMA != null)
            {
                AROMA.begin().titled("Server Launch Failed")
                    .text("Could not create Guice Injector: {}", ex)
                    .withUrgency(Urgency.HIGH)
                    .send();
            }

            throw ex;
        }

        server.setup();
    }
    
    void setup()
    {
        setupNonSecureServer();
        setupSecureServer();
    }
    
    void setupNonSecureServer()
    {
        Service http = Service.ignite();
        final int port = 9100;
        
        setupPort(http, port);
        setupExceptionHandler(http);
        setupRoutes(http);
    }
    
    void setupSecureServer()
    {
        Service https = Service.ignite();
        final int securePort = 9102;

        setupPort(https, securePort);
        setupSecurity(https);
        setupExceptionHandler(https);
        setupRoutes(https);
    }

    void setupPort(Service service, int port)
    {
        LOG.info("Starting server at {}", port);
        service.port(port);
        
        aroma.begin()
            .titled("Service Launched")
            .text("At port {}", port)
            .withUrgency(Urgency.LOW)
            .send();
    }
    
    void setupRoutes(Service service)
    {
        service.get("/stores", this.searchStoresOperation);
        service.get("/sample-store", this.getSampleStoreOperation);
        service.get("/", this.sayHelloOperation);
    }
    
    void setupSecurity(Service service)
    {
        String keystore = "./secrets/BlackSource.jks";
        String keystorePasswordFile = "./secrets/keystore-password.txt";
        String keystorePassword = Files.readFile(keystorePasswordFile);

        if (!isNullOrEmpty(keystorePassword))
        {
            service.secure(keystore, keystorePassword, null, null);
            
            AROMA.begin().titled("SSL Enabled")
                .withUrgency(Urgency.LOW)
                .send();
        }
        else 
        {
            AROMA.begin().titled("SSL Disabled")
                .text("Could not load Keystore File [{}] and Password [{}]", keystore, keystorePasswordFile)
                .withUrgency(Urgency.MEDIUM)
                .send();
            
        }
    }
    
    void setupExceptionHandler(Service service)
    {
        service.exception(Exception.class, exceptionHandler);
    }
}
