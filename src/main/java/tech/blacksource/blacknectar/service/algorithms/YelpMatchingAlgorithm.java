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

import com.google.common.base.Strings;
import java.util.Objects;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.yelp.YelpBusiness;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * This class matches Stores with businesses on Yelp.
 * 
 * @author SirWellington
 */
@StrategyPattern(role = CONCRETE_BEHAVIOR)
final class YelpMatchingAlgorithm implements StoreMatchingAlgorithm<YelpBusiness>
{

    private final static Logger LOG = LoggerFactory.getLogger(YelpMatchingAlgorithm.class);
    private final Aroma aroma;

    @Inject
    YelpMatchingAlgorithm(Aroma aroma)
    {
        checkThat(aroma).is(notNull());

        this.aroma = aroma;
    }

    @Override
    public boolean matchesStore(YelpBusiness candidate, Store store)
    {
        checkThat(candidate, store)
            .are(notNull());

        return areSimilar(candidate, store);
    }

    private boolean areSimilar(YelpBusiness business, Store store)
    {
        if (haveTheSameName(business, store))
        {
            aroma.begin().titled("Yelp Match")
                .text("Businessess have the same name. \n\nYelp:\n{}\n\nBlackNectar:\n{}", business, store)
                .withUrgency(Urgency.LOW)
                .send();

            return true;
        }

        if (haveTheSameAddressLine(business, store))
        {
            aroma.begin().titled("Yelp Match")
                .text("Businessess have the same address. \n\nYelp:\n{}\n\nBlackNectar:\n{}", business, store)
                .withUrgency(Urgency.LOW)
                .send();

            return true;
        }

        if (haveDifferentCities(business, store) || haveDifferentZipCodes(business, store))
        {
            return false;
        }

        return false;
    }

    private boolean haveTheSameName(YelpBusiness business, Store store)
    {
        if (!Objects.isNull(business.name) && !Objects.isNull(store.getName()))
        {
            String yelpName = business.name.toLowerCase();
            String blackNectarName = store.getName().toLowerCase();

            if (yelpName.contains(blackNectarName) || blackNectarName.contains(yelpName))
            {
                return true;
            }
        }

        return false;
    }

    private boolean haveTheSameAddressLine(YelpBusiness business, Store store)
    {
        if (Objects.isNull(business.location) || Objects.isNull(store.getAddress()))
        {
            return false;
        }

        String yelpAddress = Strings.nullToEmpty(business.location.address1).toUpperCase();
        String blackNectarAddress = Strings.nullToEmpty(store.getAddress().getAddressLineOne()).toUpperCase();

        return Objects.equals(yelpAddress, blackNectarAddress);
    }

    private boolean haveDifferentCities(YelpBusiness business, Store store)
    {
        if (Objects.isNull(business.location) || Objects.isNull(store.getAddress()))
        {
            return true;
        }

        String yelpCity = Strings.nullToEmpty(business.location.city).toUpperCase();
        String blackNectarCity = Strings.nullToEmpty(store.getAddress().getCity()).toUpperCase();

        return Objects.equals(yelpCity, blackNectarCity);
    }

    private boolean haveDifferentZipCodes(YelpBusiness business, Store store)
    {
        if (Objects.isNull(business.location) || Objects.isNull(store.getAddress()))
        {
            return true;
        }

        String yelpZipCode = Strings.nullToEmpty(business.location.zipCode);
        String blackNectarZipCode = Strings.nullToEmpty(store.getAddress().getZipCode());

        return Objects.equals(yelpZipCode, blackNectarZipCode);
    }

}
