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


package tech.blacksource.blacknectar.service.data;

import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.exceptions.*;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;


/**
 * The StoreRepository serves as the Backbone for the REST API.
 * 
 * It allows for querying EBT stores by name and location.
 * 
 * @author SirWellington
 */
public interface StoreRepository 
{
    /**
     * The default radius, in meters, used in queries where a radius is not provided.
     * <p>
     * A little more than 8 miles.
     */
    public double DEFAULT_RADIUS_METERS = 13_000;
    
    /**
     * Adds a Store to the repository.
     * 
     * @param store The store to add.
     * @throws BadArgumentException If the argument is null or invalid.
     */
    public void addStore(@Required Store store) throws BlackNectarAPIException;
    
    /**
     * Checks whether the repository contains a Store with the specified Store ID.
     * 
     * @param storeId The ID of the store to check. Must be a valid UUID.
     * @return
     * @throws BlackNectarAPIException 
     */
    public boolean containsStore(@NonEmpty String storeId) throws BlackNectarAPIException;

    /**
     * Get all of the EBT stores in the country.
     * 
     * @return  All of the Stores.
     */
    default List<Store> getAllStores() throws BlackNectarAPIException
    {
        return getAllStores(0);
    }
    
    /**
     * Get all of the EBT stores, with a specified limit.
     * 
     * @param limit A limit on the query, so that no more than {@code limit} stores are returned. Must be {@code >= 0}. A value of 0 means
     *              no limit.
     *
     * @return 
     */
    List<Store> getAllStores(int limit) throws BlackNectarAPIException;
    
    /**
     * Searches for stores that match the given criteria.
     * 
     * @param request
     * @return
     * 
     * @throws OperationFailedException 
     */
    List<Store> searchForStores(@Required BlackNectarSearchRequest request) throws BlackNectarAPIException;
    
    /**
     * Unlike {@link #addStore(tech.blacksource.blacknectar.service.stores.Store) }, this operation is for
     * updating an existing {@link Store} with new information.
     * 
     * @param store Cannot be empty.
     * @throws BlackNectarAPIException 
     */
    void updateStore(@Required Store store) throws BlackNectarAPIException;
    
    /**
     * Deletes a Store from the repository. This is a convenience method for {@link #deleteStore(java.lang.String) }.
     *
     * @param store
     * @throws BlackNectarAPIException
     */
    default void deleteStore(@Required Store store) throws BlackNectarAPIException
    {
        checkThat(store)
            .throwing(BadArgumentException.class)
            .is(notNull());

        String storeId = store.getStoreId();

        checkThat(storeId)
            .throwing(BadArgumentException.class)
            .usingMessage("missing storeId")
            .is(nonEmptyString())
            .usingMessage("invalid storeId")
            .is(validUUID());

        this.deleteStore(storeId);
    }

    /**
     * Permanently deletes a Store from the repository.
     *
     * @param storeId The ID of the Store to delete.
     * @throws BlackNectarAPIException
     */
    void deleteStore(@NonEmpty String storeId) throws BlackNectarAPIException;

    /**
     * Creates a new in-memory service that performs all operations in-memory.
     * <p>
     * Note that to start, the memory service included is empty.
     * 
     * @return 
     */
    static StoreRepository newMemoryService()
    {
        List<Store> stores = Lists.create();
        GeoCalculator formula = GeoCalculator.HARVESINE;

        return new MemoryStoreRepository(stores, formula);
    }

    
    /**
     * Creates a new SQL-backed Service that performs all operations against 
     * a JDBC connection.
     * 
     * @param database The {@linkplain JdbcTemplate JDBC connection} , must be open.
     * 
     * @return
     * 
     * @throws SQLException 
     */
    static StoreRepository newSQLService(@Required JdbcTemplate database) throws SQLException
    {
        return newSQLService(Aroma.createNoOpInstance(), database);
    }
    
    /**
     * Creates a new SQL-backed Service that performs all operations against 
     * a JDBC connection.
     * 
     * @param aroma
     * @param database The {@linkplain JdbcTemplate JDBC connection} , must be open.
     * 
     * @return
     *
     * @throws SQLException
     */
    static StoreRepository newSQLService(@Required Aroma aroma,
                                         @Required JdbcTemplate database) throws SQLException
    {
        return new SQLStoreRepository(aroma, database, SQLStoreMapper.INSTANCE);
    }
}
