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

package tech.blacksource.blacknectar.service.scripts;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.client.Aroma;
import tech.aroma.client.Priority;
import tech.blacksource.blacknectar.service.ModuleServer;
import tech.blacksource.blacknectar.service.data.StoreRepository;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.blacksource.blacknectar.service.stores.StoreDataSource;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
public final class RunLoadStores implements Callable<Void>
{

    private final static Logger LOG = LoggerFactory.getLogger(RunLoadStores.class);

    private final Aroma aroma;
    private final StoreRepository service;
    private final StoreDataSource storeRepository;

    @Inject
    RunLoadStores(Aroma aroma, StoreRepository service, StoreDataSource storeRepository)
    {
        checkThat(aroma, service, storeRepository)
            .are(notNull());

        this.aroma = aroma;
        this.service = service;
        this.storeRepository = storeRepository;
    }

    public static void main(String[] args) throws Exception
    {
        LOG.info("Running script to load all stores in to Database");

        Injector injector = Guice.createInjector(new ModuleServer());

        RunLoadStores instance = injector.getInstance(RunLoadStores.class);
        instance.call();
    }

    @Override
    public Void call() throws Exception
    {
        List<Store> stores = storeRepository.getAllStores();
        AtomicInteger counter = new AtomicInteger();
        
        stores.parallelStream().forEach((store) ->
        {
            try
            {
                service.addStore(store);
                counter.incrementAndGet();
            }
            catch (Exception ex)
            {
                LOG.error("Failed to save store: {}", store, ex);
                
                aroma.begin().titled("Script Failed")
                    .withBody("Could not save store: {}", store, ex)
                    .withPriority(Priority.MEDIUM)
                    .send();
            }
        });
        
        LOG.info("Successfully saved {} stores", counter.get());
        aroma.begin().titled("RunLoadStores Complete")
            .withBody("Finished loading {} stores", counter.get())
            .send();

        return null;
    }
}
