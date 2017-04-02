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

package tech.blacksource.blacknectar.service.algorithms;

import java.util.Objects;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.client.Aroma;
import tech.aroma.client.Priority;
import tech.blacksource.blacknectar.service.stores.Address;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.google.places.data.Place;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;
import tech.sirwellington.alchemy.arguments.Checks;

import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * This class matches {@linkplain Place Google Places} with {@linkplain Store EBT Stores}.
 *
 * @author SirWellington
 */
@StrategyPattern(role = CONCRETE_BEHAVIOR)
final class GooglePlacesMatchingAlgorithm implements StoreMatchingAlgorithm<Place>
{

    private final static Logger LOG = LoggerFactory.getLogger(GooglePlacesMatchingAlgorithm.class);

    private final Aroma aroma;

    @Inject
    GooglePlacesMatchingAlgorithm(Aroma aroma)
    {
        checkThat(aroma).is(notNull());

        this.aroma = aroma;
    }

    @Override
    public boolean matchesStore(Place candidate, Store store)
    {
        
        checkThat(candidate, store)
            .usingMessage("arguments cannot be null")
            .are(notNull());
        
        if (namesMatch(candidate.name, store.getName()))
        {
            makeNoteThatNamesMatch(candidate, store);
            return true;
        }

        if (addressesMatch(candidate, store))
        {
            makeNoteThatAddressesMatch(candidate, store);
            return true;
        }

        return false;

    }

    private boolean namesMatch(String first, String second)
    {
        if (Checks.anyAreNullOrEmpty(first, second))
        {
            return false;
        }

        first = first.toLowerCase();
        second = second.toLowerCase();

        if (Objects.equals(first, second))
        {
            return true;
        }
        
        return first.contains(second) || second.contains(first);
    }

    private boolean addressesMatch(Place place, Store store)
    {
        if (!place.hasFormattedAddress() && !place.hasVicinity())
        {
            return false;
        }

        Address address = store.getAddress();

        if (namesMatch(place.vicinity, address.getAddressLineOne()))
        {
            return true;
        }

        return namesMatch(place.formattedAddress, address.getAddressLineOne()) &&
               namesMatch(place.formattedAddress, address.getCity()) &&
               namesMatch(place.formattedAddress, address.getState());
    }

    private void makeNoteThatNamesMatch(Place place, Store store)
    {
        LOG.debug("Place matches store by name: {}", place);

        aroma.begin().titled("Matched By Name")
            .withBody("Google Place matches store by name.\n\nStore:\n{}\n\nPlace:\n{}", store, place)
            .withPriority(Priority.LOW)
            .send();
    }

    private void makeNoteThatAddressesMatch(Place place, Store store)
    {
        LOG.debug("Place matches store by address: {}", place);

        aroma.begin().titled("Matched By Address")
            .withBody("Google Place matches store by address.\n\nStore:\n{}\n\nPlace:\n{}", store, place)
            .withPriority(Priority.LOW)
            .send();
    }

}
