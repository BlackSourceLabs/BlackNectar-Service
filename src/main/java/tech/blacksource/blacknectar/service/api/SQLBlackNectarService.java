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

 
package tech.blacksource.blacknectar.service.api;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Predicate;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.exceptions.OperationFailedException;
import tech.blacksource.blacknectar.service.stores.Location;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.BooleanAssertions.falseStatement;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;

/**
 * Uses an SQL Connection to interact with Store Data.
 * 
 * @author SirWellington
 */
final class SQLBlackNectarService implements BlackNectarService
{
    private final static Logger LOG = LoggerFactory.getLogger(SQLBlackNectarService.class);
    
    private final Aroma aroma;
    private final Connection connection;
    private final GeoCalculator geoCalculator;
    private final SQLStoreMapper storeMapper;

    @Inject
    SQLBlackNectarService(@Required Aroma aroma, 
                          @Required Connection connection,
                          @Required GeoCalculator geoCalculator,
                          @Required SQLStoreMapper storeMapper) throws IllegalArgumentException, SQLException
    {
        checkThat(aroma, connection, geoCalculator, storeMapper)
            .are(notNull());

        this.aroma = aroma;

        boolean isClosed = connection.isClosed();
        checkThat(isClosed)
            .usingMessage("connection is closed")
            .is(falseStatement());

        this.connection = connection;
        this.geoCalculator = geoCalculator;
        this.storeMapper = storeMapper;
    }

    @Override
    public List<Store> getAllStores(int limit)
    {
        checkThat(limit)
            .usingMessage("limit must be >= 0")
            .throwing(BadArgumentException.class)
            .is(greaterThanOrEqualTo(0));

        PreparedStatement statement = createStatementToGetAllStores(limit);

        ResultSet results = tryToGetResults(statement, "Failed to get all stores with limit: " + limit);
        
        List<Store> stores = tryToGetStoresFrom(results);
        
        LOG.trace("SQL query to get all stores with limit {} turned up {} stores", limit, stores.size());
        aroma.begin().titled("SQL Query Complete")
            .text("Query to get all stores with limit {} turned up {} stores", limit, stores.size())
            .withUrgency(Urgency.LOW)
            .send();
        
        return stores;
    }

    @Override
    public List<Store> searchForStores(BlackNectarSearchRequest request) throws OperationFailedException
    {
        checkThat(request)
            .usingMessage("request missing")
            .throwing(BadArgumentException.class)
            .is(notNull());
        
        Statement statement = tryToCreateStatement();
        String query = createSearchQueryForRequest(request);
        
        ResultSet results = tryToExecute(statement, query, "Could not execute SQL to search: " + request);
        
        List<Store> stores = tryToGetStoresFrom(results);
        
        String message = "Found {} stores for Search Request {}";
        LOG.debug(message, stores.size(), request);
        aroma.begin().titled("SQL Complete")
            .text(message, stores.size(), request)
            .withUrgency(Urgency.LOW)
            .send();
        
        if (request.hasCenter())
        {
            return stores.stream()
                .filter(nearby(request.center, request.radiusInMeters))
                .collect(toList());
        }
        else 
        {
            return stores;
        }
    }

    void addStore(@Required Store store) throws OperationFailedException
    {
        checkThat(store)
            .throwing(BadArgumentException.class)
            .is(notNull());

        String insertStatement = createSQLToInsertStore();
        PreparedStatement statement = tryToPrepareStatement(insertStatement, "could not save Store to Database: " + store);

        try
        {
            prepareStatementForStore(statement, store);
        }
        catch (SQLException ex)
        {
            LOG.error("Failed to prepare statement to insert Store [{}]", store, ex);

            aroma.begin().titled("SQL Failed")
                .text("Could not prepare statement to insert Store: [{}]", store, ex)
                .withUrgency(Urgency.HIGH)
                .send();

            throw new OperationFailedException(ex);
        }

        try
        {
            int count = statement.executeUpdate();
            LOG.info("Successfully executed statement to insert Store. Received count {} for store [{}]", count, store);
        }
        catch (SQLException ex)
        {
            LOG.error("Failed to execute statement to insert Store: [{}]", store, ex);

            aroma.begin().titled("SQL Failed")
                .text("Could not execute SQL to insert Store [{}]", store, ex)
                .withUrgency(Urgency.HIGH)
                .send();

            throw new OperationFailedException(ex);
        }

    }

    private PreparedStatement createStatementToGetAllStores(int limit) throws OperationFailedException
    {
        String query = createSQLTOGetAllStores(limit);

        try
        {
            return connection.prepareStatement(query);
        }
        catch (SQLException ex)
        {
            LOG.error("Failed to create statement to get all stores with limit {}", limit);
            aroma.begin().titled("SQL Failed")
                .text("Could not create statement to get all stores: {}", ex)
                .send();
            throw new OperationFailedException(ex);
        }
    }

    private String createSQLTOGetAllStores(int limit)
    {
        if (limit <= 0)
        {
            return "SELECT * FROM Stores";
        }
        else
        {
            return "SELECT * FROM Stores " +
                   "LIMIT " + limit;
        }
    }

    private String createSQLToInsertStore()
    {
        return "INSERT INTO Stores(store_name, latitude, longitude, address, address_line_two, city, state, county, zip_code, local_zip_code)\n" +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private PreparedStatement tryToPrepareStatement(String insertStatement, String message)
    {
        try
        {
            return connection.prepareStatement(insertStatement);
        }
        catch (SQLException ex)
        {
            LOG.error(message, ex);

            aroma.begin().titled("SQL Failed")
                .text(message)
                .withUrgency(Urgency.HIGH)
                .send();

            throw new OperationFailedException(ex);
        }
    }

    void prepareStatementForStore(PreparedStatement statement, Store store) throws SQLException
    {
        statement.setString(1, store.getName());
        statement.setDouble(2, store.getLocation().getLatitude());
        statement.setDouble(3, store.getLocation().getLongitude());
        statement.setString(4, store.getAddress().getAddressLineOne());
        statement.setString(5, store.getAddress().getAddressLineTwo());
        statement.setString(6, store.getAddress().getCity());
        statement.setString(7, store.getAddress().getState());
        statement.setString(8, store.getAddress().getCounty());
        statement.setString(9, "" + store.getAddress().getZip5());
        statement.setString(10, "" + store.getAddress().getZip4());
    }

    String getStatementToCreateTable()
    {
        return "CREATE TABLE IF NOT EXISTS Stores\n" +
               "(\n" +
               "    store_name text,\n" +
               "    latitude numeric,\n" +
               "    longitude numeric,\n" +
               "    address text,\n" +
               "    address_line_two text,\n" +
               "    city text,\n" +
               "    state text,\n" +
               "    county text,\n" +
               "    zip_code text,\n" +
               "    local_zip_code text,\n" +
               "\n" +
               "    PRIMARY KEY(store_name, latitude, longitude)\n" +
               ")";
    }

    private ResultSet tryToGetResults(PreparedStatement statement, String errorMessage)
    {
        try 
        {
            return statement.executeQuery();
        }
        catch(SQLException ex)
        {
            LOG.error("Failed to execute SQL statement: {}", errorMessage, ex);
            
            aroma.begin().titled("SQL Exception")
                .text("Failed to execute SQL: {}", errorMessage, ex)
                .withUrgency(Urgency.HIGH)
                .send();
            
            throw new OperationFailedException(errorMessage, ex);
        }
    }

    private List<Store> tryToGetStoresFrom(ResultSet results)
    {
        List<Store> stores = Lists.create();
        
        try
        {
            while (results.next())
            {
                Store store;
                try
                {
                    store = storeMapper.mapToStore(results);
                }
                catch (SQLException ex)
                {
                    String message = "Could not extract store from row: {}";
                    LOG.info(message, results, ex);
                    aroma.begin().titled("SQL Exception")
                        .text(message, results, ex)
                        .send();

                    continue;
                }

                if (store != null)
                {
                    stores.add(store);
                }
            }
        }
        catch (SQLException ex)
        {
            String message = "Failed to extract store from ResultSet: {}";
            LOG.error(message, results, ex);

            aroma.begin().titled("SQL Exception")
                .text(message, results, ex)
                .withUrgency(Urgency.HIGH)
                .send();
        }
        
        return stores;
    }


    private Statement tryToCreateStatement()
    {
        try 
        {
            return connection.createStatement();
        }
        catch(SQLException ex)
        {
            String message = "Could not prepare statement to SQL Database.";
            LOG.error(message);
            aroma.begin().titled("SQL Exception")
                .text(message)
                .withUrgency(Urgency.HIGH)
                .send();
            
            throw new OperationFailedException("Failed to open Database connection", ex);
        }
    }

    private ResultSet tryToExecute(Statement statement, String query, String errorMessage)
    {
        try 
        {
            return statement.executeQuery(query);
        }
        catch(SQLException ex)
        {
            String message = "Failed to execute SQL Statement: {}";
            LOG.error(message, errorMessage, ex);
            aroma.begin().titled("SQL Exception")
                .text(message, errorMessage, ex)
                .withUrgency(Urgency.HIGH)
                .send();
            
            throw new OperationFailedException(errorMessage, ex);
        }
    }
    
    
    private String createSearchQueryForRequest(BlackNectarSearchRequest request)
    {
        String query = "SELECT * " +
            "FROM Stores ";
        
        int clauses = 0;
        
        if (request.hasSearchTerm())
        {
            clauses += 1;
            query += format("WHERE %s LIKE \"%%%s%%\" ", SQLColumns.STORE_NAME, request.searchTerm);
        }
        
        if (request.hasCenter())
        {
            clauses += 1;
            
            if (clauses > 1)
            {
                query += " AND ";
            }
            else 
            {
                query += " WHERE ";
            }
            
           String locationClause = createLocationClauseFor(request);
           query += locationClause;
        }
        
        if (request.hasLimit())
        {
            query += " LIMIT " + request.limit;
        }

        return query;
    }

    private String createLocationClauseFor(BlackNectarSearchRequest request)
    {
        double topBearing = 0.0;
        double rightBearing = 90;
        double bottomBearing = 180;
        double leftBearing = 270;
        
        Location top = geoCalculator.calculateDestinationFrom(request.center, request.radiusInMeters, topBearing);
        Location bottom = geoCalculator.calculateDestinationFrom(request.center, request.radiusInMeters, bottomBearing);
        Location left = geoCalculator.calculateDestinationFrom(request.center, request.radiusInMeters, leftBearing);
        Location right = geoCalculator.calculateDestinationFrom(request.center, request.radiusInMeters, rightBearing);
        
        String clause = format("%s <= %f ", SQLColumns.LATITUDE, top.getLatitude()) +
                        format("AND %s >= %f ", SQLColumns.LATITUDE, bottom.getLatitude()) +
                        format("AND %s <= %f ", SQLColumns.LONGITUDE, right.getLongitude()) +
                        format("AND %s >= %f", SQLColumns.LONGITUDE, left.getLongitude());
        
        return clause;
    }

    private Predicate<? super Store> nearby(Location center, double radiusInMeters)
    {
        return store ->
        {
            double distance = geoCalculator.distanceBetween(store.getLocation(), center);
            return distance <= radiusInMeters;
        };
    }


}
