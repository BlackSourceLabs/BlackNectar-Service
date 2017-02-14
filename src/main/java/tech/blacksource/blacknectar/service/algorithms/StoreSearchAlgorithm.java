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
import tech.sirwellington.alchemy.annotations.arguments.Optional;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE;

/**
 *
 * @author SirWellington
 * @param <Result> The data type returned by the specific Search Engine.
 * @see StoreMatchingAlgorithm
 */
@StrategyPattern(role = INTERFACE)
public interface StoreSearchAlgorithm<Result>
{

    /**
     * Finds data pertaining to the Store.
     *
     * @param store The store to search for.
     * @return Information about the Store, or null if no match was found.
     */
    @Optional
    Result findMatchFor(@Required Store store);
}
