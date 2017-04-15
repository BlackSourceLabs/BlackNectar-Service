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

import java.sql.Connection;
import java.sql.SQLException;
import javax.inject.Singleton;
import javax.sql.DataSource;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.client.Aroma;
import tech.aroma.client.Priority;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;

import static tech.sirwellington.alchemy.arguments.Arguments.*;

/**
 * @author SirWellington
 */
public final class ModuleDatabaseTesting extends AbstractModule
{

    private final static Logger LOG = LoggerFactory.getLogger(ModuleDatabaseTesting.class);

    @Override
    protected void configure()
    {
    }

    @Provides
    @Singleton
    DataSource provideSQLConnection(Aroma aroma) throws SQLException
    {

        String url = createProductionTestConnection();

        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(url);

        try (Connection connection = dataSource.getConnection())
        {
            checkThat(connection)
                    .throwing(SQLException.class)
                    .is(connected());
        }
        catch (SQLException ex)
        {
            String message = "Failed to create connection to PostgreSQL.";
            LOG.error(message, ex);
            aroma.begin().titled("SQL Connection Failed")
                 .withBody(message, ex)
                 .withPriority(Priority.HIGH)
                 .send();

            throw ex;
        }

        return dataSource;
    }

    private AlchemyAssertion<Connection> connected()
    {
        return connection ->
        {
            try
            {
                if (connection.isClosed())
                {
                    throw new FailedAssertionException("Database is not connected");
                }

            }
            catch (SQLException ex)
            {
                throw new FailedAssertionException("Could not check for connection", ex);
            }
        };
    }

    private String createDevelopmentTestConnection()
    {
        int port = 5432;
        String database = "testing";
        String host = "dev.database.blacksource.tech";
        String user = Files.readFile("./secrets/postgres-user.txt").trim();
        String password = Files.readFile("./secrets/postgres-password.txt").trim();

        String applicationName = "BlackNectarService";

        String url = String.format("jdbc:postgresql://%s:%d/%s?user=%s&password=%s&ApplicationName=%s",
                                   host,
                                   port,
                                   database,
                                   user,
                                   password,
                                   applicationName);

        return url;
    }

    private String createProductionTestConnection()
    {
        int port = 5432;
        String database = "testing";
        String host = "prod.database.blacksource.tech";
        String user = Files.readFile("./secrets/postgres-user.txt").trim();
        String password = Files.readFile("./secrets/postgres-password.txt").trim();

        String applicationName = "BlackNectarService";

        String url = String.format("jdbc:postgresql://%s:%d/%s?user=%s&password=%s&ApplicationName=%s",
                                   host,
                                   port,
                                   database,
                                   user,
                                   password,
                                   applicationName);

        return url;
    }
}
