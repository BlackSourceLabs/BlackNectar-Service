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


package tech.blacksource.blacknectar.service.images;

import java.net.URL;
import java.util.List;

import tech.blacksource.blacknectar.service.exceptions.BlackNectarAPIException;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern;

import static tech.sirwellington.alchemy.annotations.designs.patterns.StrategyPattern.Role.INTERFACE;


/**
 * Image Loaders scour the Internet to find images relevant to a particular store. 
 * Each implementation has its own source and characteristics.
 * 
 * @author SirWellington
 */
@StrategyPattern(role = INTERFACE)
public interface ImageLoader 
{
    /**
     * Tries to find images pertaining to the store.
     * 
     * @param store The store to search for.
     * @return A URL pointing to a relevant image, null is one cannot be found.
     * 
     * @throws BlackNectarAPIException 
     */
    List<URL> getImagesFor(@Required Store store) throws BlackNectarAPIException;
}
