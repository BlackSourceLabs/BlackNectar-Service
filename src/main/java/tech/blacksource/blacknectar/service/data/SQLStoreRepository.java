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
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.exceptions.BlackNectarAPIException;
import tech.blacksource.blacknectar.service.exceptions.OperationFailedException;
import tech.blacksource.blacknectar.service.stores.Location;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
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
    private final GeoCalculator geoCalculator;
    private final SQLStoreMapper storeMapper;

    @Inject
    SQLStoreRepository(@Required Aroma aroma,
                       @Required JdbcTemplate database,
                       @Required GeoCalculator geoCalculator,
                       @Required SQLStoreMapper storeMapper) throws IllegalArgumentException, SQLException
    {
        checkThat(aroma, database, geoCalculator, storeMapper)
            .are(notNull());

        this.aroma = aroma;
        this.database = database;
        this.geoCalculator = geoCalculator;
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

        String sql = createSQLTOGetAllStores(limit);

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

        String query = createSearchQueryForRequest(request);

        List<Store> stores;

        try
        {
            stores = database.query(query, storeMapper);
        }
        catch (DataAccessException ex)
        {
            String message = "Failed to search for stores with request: {}";
            makeNoteOfSQLError(message, request, ex);
            throw new OperationFailedException(message, ex);
        }

        makeNoteThatStoresSearched(request, stores);

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

    private String createSearchQueryForRequest(BlackNectarSearchRequest request)
    {
        String query = "SELECT * " +
                       "FROM Stores ";

        int clauses = 0;

        if (request.hasSearchTerm())
        {
            clauses += 1;
            query += format("WHERE %s LIKE \'%%%s%%\' ", SQLColumns.STORE_NAME, request.searchTerm);
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

}
