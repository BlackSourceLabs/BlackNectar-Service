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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.aroma.client.Priority;
import tech.blacksource.blacknectar.service.algorithms.StoreSearchAlgorithm;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.redroma.yelp.YelpAPI;
import tech.redroma.yelp.YelpBusiness;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Strings.isNullOrEmpty;
import static tech.blacksource.blacknectar.service.stores.Store.validStore;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
final class YelpImageLoader implements ImageLoader
{

    private final static Logger LOG = LoggerFactory.getLogger(YelpImageLoader.class);

    private final Aroma aroma;
    private final StoreSearchAlgorithm<YelpBusiness> searchAlgorithm;
    private final YelpAPI yelp;

    @Inject
    YelpImageLoader(Aroma aroma, StoreSearchAlgorithm<YelpBusiness> searchAlgorithm, YelpAPI yelp)
    {
        checkThat(aroma, searchAlgorithm, yelp)
            .are(notNull());

        this.aroma = aroma;
        this.searchAlgorithm = searchAlgorithm;
        this.yelp = yelp;
    }

    @Override
    public List<URL> getImagesFor(Store store)
    {
        checkThat(store)
            .is(notNull())
            .is(validStore());

        String url = tryToGetAPhotoURLFor(store);

        if (isNullOrEmpty(url))
        {
            return Lists.emptyList();
        }
        else
        {
            try
            {
                return Lists.createFrom(new URL(url));
            }
            catch (MalformedURLException ex)
            {
                makeNoteThatURLParseFailed(url, ex);

                return null;
            }
        }

    }

    private String tryToGetAPhotoURLFor(Store store)
    {
        YelpBusiness yelpStore = searchAlgorithm.findMatchFor(store);

        if (Objects.nonNull(yelpStore) && Objects.nonNull(yelpStore.imageURL))
        {
            return yelpStore.imageURL;
        }

        return null;
    }

    private void makeNoteThatURLParseFailed(String url, MalformedURLException ex)
    {
        LOG.error("Failed to parse URL: [{}]", url, ex);
        
        aroma.begin().titled("URL Parse Failed")
            .withBody("Could not parse URL: [{}]", url, ex)
            .withPriority(Priority.LOW)
            .send();
    }

}
