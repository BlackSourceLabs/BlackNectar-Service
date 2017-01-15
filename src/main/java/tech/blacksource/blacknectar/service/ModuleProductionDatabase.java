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
import com.mchange.v2.c3p0.ComboPooledDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import javax.inject.Singleton;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;

/**
 *
 * @author SirWellington
 */
public final class ModuleProductionDatabase extends AbstractModule
{

    private final static Logger LOG = LoggerFactory.getLogger(ModuleProductionDatabase.class);

    @Override
    protected void configure()
    {
    }

    @Provides
    @Singleton
    DataSource provideSQLConnection(Aroma aroma) throws SQLException
    {
        int port = 5432;
        String host = "database.blacksource.tech";
        String database = "postgres";
        String user = Files.readFile("./secrets/postgres-user.txt").trim();
        String password = Files.readFile("./secrets/postgres-password.txt").trim();
        //Explicitly setting the schema seems to conflict with Postgis functions, so 
        //ignoring for now.
        String schema = "blacknectar";
        String applicationName = "BlackNectar";

        String url = String.format("jdbc:postgresql://%s:%d/%s?user=%s&password=%s&ApplicationName=%s",
                                   host,
                                   port,
                                   database,
                                   user,
                                   password,
                                   applicationName);

        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setJdbcUrl(url);

        //Configure with pooling settings
        dataSource.setMinPoolSize(3);
        dataSource.setMaxPoolSize(10);
        dataSource.setAcquireIncrement(3);
        dataSource.setTestConnectionOnCheckin(true);

        try
        {

            Connection connection = dataSource.getConnection();

            checkThat(connection)
                .throwing(SQLException.class)
                .is(connected());

            connection.close();
        }
        catch (SQLException ex)
        {
            String message = "Failed to create connection to PostgreSQL. Defaulting to SQLite.";
            LOG.error(message, ex);
            aroma.begin().titled("SQL Connection Failed")
                .text(message, ex)
                .withUrgency(Urgency.HIGH)
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
}
