/*
 * Copyright 2016 BlackSourceLabs.
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

package tech.blacksource.blacknectar.service.stores;

import java.util.UUID;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.CONCRETE_BEHAVIOR;
import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * This interface generates unique IDs on each call, useful for storing data in databases as the primary key.
 *
 * @author SirWellington
 */
@StrategyPattern(role = INTERFACE)
public interface IDGenerator
{

    /**
     * Generates a unique key that is guaranteed to be different on each call.
     *
     * @return
     * @see #generateKeyAsString()
     */
    UUID generateKey();

    /**
     * Generates a unique {@link UUID} as a String.
     *
     * @return
     * @see #generateKey()
     */
    default String generateKeyAsString()
    {
        UUID key = this.generateKey();
        checkThat(key).is(notNull());
        return key.toString();
    }

    /**
     * Singleton instance that generates unique UUIDs.
     */
    IDGenerator INSTANCE = new Impl();

    /**
     * Generates IDs based on {@link UUID#randomUUID() }.
     */
    @StrategyPattern(role = CONCRETE_BEHAVIOR)
    class Impl implements IDGenerator
    {

        @Override
        public UUID generateKey()
        {
            return UUID.randomUUID();
        }

    }

}
