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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.client.Aroma;
import tech.redroma.google.places.data.Place;
import tech.redroma.yelp.YelpBusiness;

/**
 *
 * @author SirWellington
 */
public final class ModuleAlgorithms extends AbstractModule
{

    private final static Logger LOG = LoggerFactory.getLogger(ModuleAlgorithms.class);

    @Override
    protected void configure()
    {
        bind(new TypeLiteral<StoreSearchAlgorithm<Place>>() {}).to(GooglePlacesStoreSearchAlgorithm.class);
    }

    @Provides
    StoreMatchingAlgorithm<Place> provideGooglePlacesMatchingAlgorithm(Aroma aroma)
    {
        return new GooglePlacesMatchingAlgorithm(aroma);
    }

    @Provides
    StoreMatchingAlgorithm<YelpBusiness> provideYelpBusinessesMatchingAlgoirhtm(Aroma aroma)
    {
        return new YelpMatchingAlgorithm(aroma);
    }
}
