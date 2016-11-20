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

import java.util.List;
import tech.blackhole.blacknectar.service.stores.Location;
import tech.blackhole.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;


/**
 * The BlackNectarService serves as the Backbone for the REST API.
 * 
 * It allows for querying EBT stores by name and location.
 * 
 * @author SirWellington
 */
public interface BlackNectarService 
{
    /**
     * The default radius, in meters, used in queries where a radius is not provided.
     * <p>
     * 5 Kilometers.
     */
    public double DEFAULT_RADIUS = 5_000;

    /**
     * Get all of the EBT stores in the country.
     * 
     * @return  All of the Stores.
     */
    default List<Store> getAllStores()
    {
        return getAllStores(0);
    }
    
    /**
     * Get all of the EBT stores, with a specified limit.
     * 
     * @param limit A limit on the query, so that no more than {@code limit} stores are returned. Must be >= 0. A value of 0 means
     *              no limit.
     *
     * @return 
     */
    List<Store> getAllStores(int limit);
    
    /**
     * Search for stores around the specified location.
     * 
     * @param center Search for stores within this center, with a radius of {@link BlackNectarService#DEFAULT_RADIUS}.
     * 
     * @return 
     */
    default List<Store> searchForStoresByLocation(@Required Location center)
    {
        return searchForStoresByLocation(center, DEFAULT_RADIUS, 0);
    }
    
    /**
     * Search for all of the stores around the specified location
     * 
     * @param center Only searches for stores close to this location.
     * @param radius Radius, in meters, of all
     * 
     * @return 
     */
    default List<Store> searchForStoresByLocation(@Required Location center, double radius)
    {
        return searchForStoresByLocation(center, radius, 0);
    }
    
    
    /**
     * Search for all of the stores around the specified location
     * 
     * @param center Only searches for stores close to this location.
     * @param radius Radius, in meters, of all
     * @param limit A limit on the query, so that no more than {@code limit} stores are returned. Must be >= 0. A value of 0 means
     *              no limit.     * 
     * @return 
     */
    List<Store> searchForStoresByLocation(@Required Location center, double radius, int limit);
    
    /**
     * Search for all stores that match {@code searchTerm} in the Store Name.
     * 
     * @param searchTerm The query term to use when searching for stores.
     * 
     * @return 
     */
    List<Store> searchForStoresByName(@NonEmpty String searchTerm);
    
    /**
     * Search for all of the stores that match {@code searchTerm} and are close to {@code center}.
     * 
     * @param searchTerm Searches for stores with names matching this search term.
     * @param center Only searches for stores close to this location.
     * 
     * @return 
     */
    default List<Store> searchForStoresByName(@NonEmpty String searchTerm, @Required Location center)
    {
        return this.searchForStoresByName(searchTerm, center, DEFAULT_RADIUS);
    }
    
    /**
     * Search for all of the stores that match {@code searchTerm} and are close to {@code center} within a radius of {@code radius}.
     * 
     * @param searchTerm Searches for stores with names matching this search term.
     * @param center Only searches for stores close to this location.
     * @param radius Radius, in meters, of the geo-query.
     * 
     * @return 
     */
    default List<Store> searchForStoresByName(@NonEmpty String searchTerm, @Required Location center, double radius)
    {
        return this.searchForStoresByName(searchTerm, center, radius, 0);
    }
    
    
    /**
     * Search for all of the stores that match {@code searchTerm} and are close to {@code center} within a radius of {@code radius}.
     * 
     * @param searchTerm Searches for stores with names matching this search term.
     * @param center Only searches for stores close to this location.
     * @param radius Radius, in meters, of the geo-query.
     * @param limit A limit on the query, so that no more than {@code limit} stores are returned. Must be >= 0. A value of 0 means
     *              no limit.
     * 
     * @return 
     */
    List<Store> searchForStoresByName(@NonEmpty String searchTerm, @Required Location center, double radius, int limit);
    
}
