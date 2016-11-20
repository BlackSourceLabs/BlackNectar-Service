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
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.blackhole.blacknectar.service.stores.Location;
import tech.blackhole.blacknectar.service.stores.Store;
import tech.blackhole.blacknectar.service.stores.StoreRepository;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;

/**
 *
 * @author SirWellington
 */
@Internal
final class MemoryBlackNectarService implements BlackNectarService
{

    private final static Logger LOG = LoggerFactory.getLogger(MemoryBlackNectarService.class);

    private final List<Store> stores;
    private final DistanceFormula distanceFormula;

    MemoryBlackNectarService()
    {
        this.stores = loadDefaultStores();
        this.distanceFormula = DistanceFormula.HARVESINE;
    }

    MemoryBlackNectarService(List<Store> stores, DistanceFormula distanceFormula)
    {
        checkThat(stores, distanceFormula)
            .are(notNull());

        this.stores = stores;
        this.distanceFormula = distanceFormula;
    }

    @Override
    public List<Store> getAllStores(int limit)
    {
        checkThat(limit)
            .usingMessage("limit must be >= 0")
            .is(greaterThanOrEqualTo(0));

        if (limit == 0)
        {
            return stores;
        }

        return stores.stream()
            .limit(limit)
            .collect(toList());
    }

    private List<Store> loadDefaultStores()
    {
        List<Store> stores = StoreRepository.FILE.getAllStores();
        LOG.debug("Found {} stores in the File Repository", stores.size());

        return stores;
    }

    @Override
    public List<Store> searchForStoresByLocation(Location center, double radius, int limit)
    {
        return stores.parallelStream()
            .filter(nearby(center, radius))
            .collect(toList());
    }

    @Override
    public List<Store> searchForStoresByName(String searchTerm)
    {
        return stores.parallelStream()
            .filter(containsInName(searchTerm))
            .collect(toList());
    }

    @Override
    public List<Store> searchForStoresByName(String searchTerm, Location center, double radius, int limit)
    {
        return stores.parallelStream()
            .filter(containsInName(searchTerm))
            .filter(nearby(center, radius))
            .collect(toList());
    }

    private Predicate<Store> nearby(Location center, double radius)
    {
        return store ->
        {
            double distance = distanceFormula.distanceBetween(store.getLocation(), center);
            
            return distance <= radius;
        };
    }
    
    private Predicate<Store> containsInName(String term)
    {
        return store ->
        {
            String storeName = store.getName();
            
            if (isNullOrEmpty(storeName))
            {
                return false;
            }
            
            return storeName.contains(term);
        };
    }
}
