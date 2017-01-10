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

import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.google.places.data.Place;
import tech.redroma.yelp.YelpBusiness;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE;

/**
 * Store Match Algoirthms are responsible for determining whether a businesses object matches the given EBT store.
 * This is used to join EBT Data with data from external sources, such as Google Places API, or the Yelp API.
 * <p>
 * Note: Uses the {@link StrategyPattern}.
 * 
 * @author SirWellington
 * @param <Candidate> The type of the Object to compare and match against. It could be a {@linkplain Place Google Place}, a
 *                    {@linkplain YelpBusiness Yelp Businesses}, etc.
 */
@StrategyPattern(role = INTERFACE)
public interface StoreMatchingAlgorithm<Candidate>
{

    /**
     * Returns true of the Candidate matches the Store.
     * 
     * @param candidate The candidate algorithm to test.
     * @param store The store to test against.
     * 
     * @return True if the candidate matches the store, false otherwise.
     */
    boolean matchesStore(@Required Candidate candidate, @Required Store store);

}
