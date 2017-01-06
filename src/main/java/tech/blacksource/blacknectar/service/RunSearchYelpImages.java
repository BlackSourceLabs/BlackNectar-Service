/*
 * Copyright 2017 BlackSourceLabs.
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

package tech.blacksource.blacknectar.service;

import com.google.common.collect.Queues;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.data.StoreRepository;
import tech.blacksource.blacknectar.service.images.ImageLoader;
import tech.blacksource.blacknectar.service.images.Yelp;
import tech.blacksource.blacknectar.service.stores.Store;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
public final class RunSearchYelpImages implements Callable<Void>
{

    private final static Logger LOG = LoggerFactory.getLogger(RunSearchYelpImages.class);

    private final Aroma aroma;
    private final ImageLoader yelpImageLoader;
    private final String source = "Yelp";
    private final RunLoadImages runner;
    private final StoreRepository storeRepository;

    @Inject
    RunSearchYelpImages(Aroma aroma,
                        @Yelp ImageLoader yelpImageLoader,
                        RunLoadImages runner,
                        StoreRepository storeRepository)
    {
        checkThat(aroma, yelpImageLoader, runner, storeRepository)
            .are(notNull());
        
        this.aroma = aroma;
        this.yelpImageLoader = yelpImageLoader;
        this.runner = runner;
        this.storeRepository = storeRepository;
    }
    
    public static void main(String[] args) throws Exception
    {
        Injector injector = Guice.createInjector(new ModuleServer());
        
        RunSearchYelpImages instance = injector.getInstance(RunSearchYelpImages.class);
        instance.call();
    }

    @Override
    public Void call() throws Exception
    {
        LOG.debug("Beginning script.");

        List<Store> stores = storeRepository.getAllStores();
        Queue<Store> queue = Queues.newLinkedBlockingQueue(stores);

        RunLoadImages.Arguments args = RunLoadImages.Arguments.Builder.newInstance()
            .withSleepTime(400, TimeUnit.MILLISECONDS)
            .withSource(source)
            .withImageLoader(yelpImageLoader)
            .withStores(queue)
            .build();

        runner.accept(args);
        
        return null;
    }
}
