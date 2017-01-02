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

package tech.blacksource.blacknectar.service.data;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.exceptions.BlackNectarAPIException;
import tech.blacksource.blacknectar.service.exceptions.OperationFailedException;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.blacksource.blacknectar.service.stores.Store.validStore;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;

/**
 * Uses an SQL Connection to interact with Store Data.
 *
 * @author SirWellington
 */
final class SQLStoreRepository implements StoreRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(SQLStoreRepository.class);

    private final Aroma aroma;
    private final JdbcTemplate database;
    private final SQLStoreMapper storeMapper;

    @Inject
    SQLStoreRepository(@Required Aroma aroma,
                       @Required JdbcTemplate database,
                       @Required SQLStoreMapper storeMapper) throws IllegalArgumentException, SQLException
    {
        checkThat(aroma, database, storeMapper)
            .are(notNull());

        this.aroma = aroma;
        this.database = database;
        this.storeMapper = storeMapper;
    }

    @Override
    public void addStore(@Required Store store) throws OperationFailedException
    {
        checkThat(store)
            .throwing(BadArgumentException.class)
            .is(notNull())
            .is(validStore());

        int inserted = 0;
        try
        {
            inserted = addStoreToDatabase(store, database);
        }
        catch (DataAccessException ex)
        {
            String message = "Failed to insert Store: {}";
            makeNoteOfSQLError(message, store, ex);

            throw new OperationFailedException("Could not insert store", ex);
        }

        aroma.begin().titled("SQL Store Inserted")
            .text("Inserted {} store: \n\n{}", inserted, store)
            .send();
        LOG.debug("Successfully inserted {} store", inserted);
    }

    @Override
    public List<Store> getAllStores(int limit) throws BlackNectarAPIException
    {
        checkThat(limit)
            .usingMessage("limit must be >= 0")
            .throwing(BadArgumentException.class)
            .is(greaterThanOrEqualTo(0));

        String sql = createSQLToGetAllStores(limit);

        List<Store> stores;

        try
        {
            stores = database.query(sql, storeMapper);
        }
        catch (DataAccessException ex)
        {
            String message = "Failed to query for all stores with limit {}";
            makeNoteOfSQLError(message, limit, ex);

            throw new OperationFailedException(message, ex);
        }

        LOG.trace("SQL query to get all stores with limit {} turned up {} stores", limit, stores.size());

        aroma.begin().titled("SQL Query Complete")
            .text("Query to get all stores with limit {} turned up {} stores", limit, stores.size())
            .withUrgency(Urgency.LOW)
            .send();

        return stores;
    }

    @Override
    public List<Store> searchForStores(BlackNectarSearchRequest request) throws BlackNectarAPIException
    {
        checkThat(request)
            .usingMessage("request missing")
            .throwing(BadArgumentException.class)
            .is(notNull());

        List<Store> stores;

        try
        {
            stores = findStoresBasedOfRequest(request);
        }
        catch (DataAccessException ex)
        {
            String message = "Failed to search for stores with request: {}";
            makeNoteOfSQLError(message, request, ex);
            throw new OperationFailedException(message, ex);
        }

        makeNoteThatStoresSearched(request, stores);

        return stores;
    }

    private int addStoreToDatabase(Store store, JdbcTemplate database) throws DataAccessException
    {
        String insertStatement = SQLQueries.INSERT_STORE;
        UUID storeId = UUID.fromString(store.getStoreId());

        double latitude = store.getLocation().getLatitude();
        double longitude = store.getLocation().getLongitude();

        return database.update(insertStatement,
                               storeId,
                               store.getName(),
                               latitude,
                               longitude,
                               //Remember that for ST_Point function, it is longitude(x), latitude(y).
                               longitude,
                               latitude,
                               store.getAddress().getAddressLineOne(),
                               store.getAddress().getAddressLineTwo(),
                               store.getAddress().getCity(),
                               store.getAddress().getState(),
                               store.getAddress().getCounty(),
                               store.getAddress().getZipCode(),
                               store.getAddress().getLocalZipCode());

    }

    private String createSQLToGetAllStores(int limit)
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

    private void makeNoteThatStoresSearched(BlackNectarSearchRequest request, List<Store> stores)
    {
        String message = "Found {} stores for Search Request {}";
        LOG.debug(message, stores.size(), request);
        aroma.begin().titled("SQL Complete")
            .text(message, stores.size(), request)
            .withUrgency(Urgency.LOW)
            .send();
    }

    private void makeNoteOfSQLError(String message, Object... args)
    {
        aroma.begin().titled("SQL Failed")
            .text(message, args)
            .withUrgency(Urgency.HIGH)
            .send();

        LOG.error(message, args);
    }

    private List<Store> findStoresBasedOfRequest(BlackNectarSearchRequest request)
    {
        
        String query = createSQLQueryFor(request);

        if (request.hasSearchTerm() && request.hasCenter())
        {
            double latitude = request.center.getLatitude();
            double longitude = request.center.getLongitude();

            return database.query(query, storeMapper,
                                  longitude,
                                  latitude,
                                  toSQLSearchTerm(request.searchTerm),
                                  longitude,
                                  latitude,
                                  request.radiusInMeters);
        }
        else if (request.hasCenter())
        {
            double latitude = request.center.getLatitude();
            double longitude = request.center.getLongitude();

            return database.query(query, storeMapper,
                                  longitude,
                                  latitude,
                                  longitude,
                                  latitude,
                                  request.radiusInMeters);
        }
        else if (request.hasSearchTerm())
        {
            return database.query(query, storeMapper, toSQLSearchTerm(request.searchTerm));
        }
        else 
        {
            return Lists.emptyList();
        }

    }

    private String createSQLQueryFor(BlackNectarSearchRequest request)
    {
        String query = "";
        
        if (request.hasSearchTerm() && request.hasCenter())
        {
            query = SQLQueries.QUERY_STORES_WITH_NAME_AND_LOCATION;
        }
        else if (request.hasCenter())
        {
            query = SQLQueries.QUERY_STORES_WITH_LOCATION;
        }
        else if (request.hasSearchTerm())
        {
            query = SQLQueries.QUERY_STORES_WITH_NAME;
        }
        
        if (request.hasLimit())
        {
            query += " LIMIT " + request.limit;
        }
        
        return query;
    }
    
    private String toSQLSearchTerm(String searchTerm)
    {
        return String.format("%%%s%%", searchTerm);
    }

}
