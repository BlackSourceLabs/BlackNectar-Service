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

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.exceptions.BlackNectarAPIException;
import tech.blacksource.blacknectar.service.exceptions.OperationFailedException;
import tech.blacksource.blacknectar.service.stores.Location;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.stream.Collectors.toList;
import static tech.blacksource.blacknectar.service.stores.Store.validStore;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 *
 * @author SirWellington
 */
@Internal
final class MemoryStoreRepository implements StoreRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(MemoryStoreRepository.class);

    private final List<Store> stores;
    private final GeoCalculator distanceFormula;

    @Inject 
    MemoryStoreRepository(List<Store> stores, GeoCalculator distanceFormula)
    {
        checkThat(stores, distanceFormula)
            .are(notNull());

        this.stores = stores;
        this.distanceFormula = distanceFormula;
    }

    @Override
    public void addStore(Store store) throws BadArgumentException
    {
        checkThat(store)
            .throwing(BadArgumentException.class)
            .is(notNull())
            .is(validStore());
        
        stores.add(store);
        LOG.debug("Successfully saved store: {}", store);
    }

    @Override
    public boolean containsStore(String storeId) throws BlackNectarAPIException
    {
        checkThat(storeId)
            .throwing(BadArgumentException.class)
            .is(validUUID());
        
        return stores
            .stream()
            .anyMatch(store -> Objects.equals(storeId, store.getStoreId()));
    }
    
    @Override
    public List<Store> getAllStores(int limit)
    {
        checkThat(limit)
            .usingMessage("limit must be >= 0")
            .is(greaterThanOrEqualTo(0));

        if (limit == 0)
        {
            return Lists.copy(stores);
        }

        return stores.stream()
            .limit(limit)
            .collect(toList());
    }

    @Override
    public List<Store> searchForStores(BlackNectarSearchRequest request) throws OperationFailedException
    {
        checkThat(request)
            .usingMessage("request missing")
            .is(notNull());

        Stream<Store> stream = stores.parallelStream();

        if (request.hasLimit())
        {
            stream = stream.limit(request.limit);
        }

        if (request.hasSearchTerm())
        {
            stream = stream.filter(containsInName(request.searchTerm));
        }
        
        if (request.hasCenter())
        {
            if (request.hasRadius())
            {
                stream = stream.filter(nearby(request.center, request.radiusInMeters));
            }
            else 
            {
                stream = stream.filter(nearby(request.center, DEFAULT_RADIUS));
            }
        }

        return stream.collect(toList());
    }

    @Override
    public void deleteStore(String storeId) throws BlackNectarAPIException
    {
        checkThat(storeId)
            .throwing(BadArgumentException.class)
            .is(nonEmptyString())
            .is(validUUID());
        
        this.stores.removeIf(s -> Objects.equals(s.getStoreId(), storeId));
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
