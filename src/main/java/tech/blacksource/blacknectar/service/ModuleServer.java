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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import spark.ExceptionHandler;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blacksource.blacknectar.service.algorithms.ModuleAlgorithms;
import tech.blacksource.blacknectar.service.data.ModuleBlackNectarService;
import tech.blacksource.blacknectar.service.exceptions.BlackNectarExceptionHandler;
import tech.blacksource.blacknectar.service.operations.ModuleOperations;
import tech.redroma.google.places.GooglePlacesAPI;
import tech.redroma.yelp.YelpAPI;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.http.AlchemyHttp;

/**
 *
 * @author SirWellington
 */
@Internal
public final class ModuleServer extends AbstractModule
{

    private final static Logger LOG = LoggerFactory.getLogger(ModuleServer.class);

    @Override
    protected void configure()
    {
        install(new ModuleOperations());
        install(new ModuleBlackNectarService());
        install(new ModuleAlgorithms());

        bind(AlchemyHttp.class).toInstance(AlchemyHttp.newDefaultInstance());
        bind(ExceptionHandler.class).to(BlackNectarExceptionHandler.class);
        bind(Server.class);
    }

    @Provides
    @Singleton
    Aroma provideAromaClient()
    {
        return Aroma.create("82c636ca-eb6d-48c6-bdd4-ff9dedae8ac4");
    }


    @Singleton
    @Provides
    JdbcTemplate provideJDBCTemplate(DataSource dataSource)
    {
        return new JdbcTemplate(dataSource, false);
    }
    
    @Provides
    YelpAPI provideYelpAPI(Aroma aroma) throws Exception
    {
        try
        {
            String cliendId = Files.readFile("./secrets/yelp-client.txt").trim();
            String secret = Files.readFile("./secrets/yelp-secret.txt").trim();

            return YelpAPI.Builder.newInstance()
                .withClientCredentials(cliendId, secret)
                .withEagerAuthentication()
                .build();
        }
        catch (RuntimeException ex)
        {
            aroma.begin().titled("Yelp Setup Failed")
                .text("Failed to setup the Yelp API Client", ex)
                .withUrgency(Urgency.HIGH)
                .send();

            return YelpAPI.NO_OP;
        }
    }

    @Provides
    @Singleton
    GooglePlacesAPI provideGooglePlacesAPI(Aroma aroma) throws Exception
    {
        try
        {
            String apiKey = Files.readFile("./secrets/google-places.txt").trim();

            return GooglePlacesAPI.create(apiKey);
        }
        catch (RuntimeException ex)
        {
            LOG.error("Failed to initialized Google Places API", ex);

            aroma.begin().titled("Initialization Failed")
                .text("Failed to initialized Google Places API", ex)
                .withUrgency(Urgency.HIGH)
                .send();

            return GooglePlacesAPI.NO_OP;
        }
    }

    /**
     * Creates a connection to the built-in SQLite database.
     * 
     * @return
     * @throws SQLException 
     */
    private Connection createSQLiteConnection() throws SQLException
    {
        return DriverManager.getConnection("jdbc:sqlite::resource:Stores.db");
    }
   
}
