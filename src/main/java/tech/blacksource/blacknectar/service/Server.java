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

 import javax.inject.Inject;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;
import spark.Service;
import tech.aroma.client.Aroma;
import tech.aroma.client.Priority;

import static com.google.common.base.Strings.isNullOrEmpty;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
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
    
    private final Aroma aroma;
    private final ExceptionHandler exceptionHandler;
    private final Routes routes;


    @Inject
    Server(Aroma aroma, ExceptionHandler exceptionHandler, Routes routes)
    {
        checkThat(aroma, exceptionHandler, routes)
                .are(notNull());

        this.aroma = aroma;
        this.exceptionHandler = exceptionHandler;
        this.routes = routes;
    }
    

    public static void main(String[] args)
    {
    
        Injector injector;
        Server server;

        Aroma aroma = null;

        try
        {
            injector = Guice.createInjector(new ModuleServer(), new ModuleDatabaseProduction());
            aroma = injector.getInstance(Aroma.class);
            server = injector.getInstance(Server.class);
        }
        catch (RuntimeException ex)
        {
            LOG.error("Server Launch Failed", ex);

            if (aroma != null)
            {
                aroma.begin().titled("Server Launch Failed")
                    .withBody("Could not create Guice Injector: {}", ex)
                    .withPriority(Priority.HIGH)
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

    private void setupPort(Service service, int port)
    {
        LOG.info("Starting server at {}", port);
        service.port(port);

        aroma.sendLowPriorityMessage("Service Launched", "At Port {}", port);
    }
    
    private void setupRoutes(Service service)
    {
        routes.setupRoutes(service);
    }
    
    private void setupSecurity(Service service)
    {
        String keystore = "./secrets/BlackSource.jks";
        String keystorePasswordFile = "./secrets/keystore-password.txt";
        String keystorePassword = Files.readFile(keystorePasswordFile);

        if (!isNullOrEmpty(keystorePassword))
        {
            service.secure(keystore, keystorePassword, null, null);

            aroma.sendLowPriorityMessage("SSL Enabled");
        }
        else 
        {
            aroma.begin().titled("SSL Disabled")
                .withBody("Could not load Keystore File [{}] and Password [{}]", keystore, keystorePasswordFile)
                .withPriority(Priority.MEDIUM)
                .send();
            
        }
    }
    
    private void setupExceptionHandler(Service service)
    {
        service.exception(Exception.class, exceptionHandler);
    }
}
