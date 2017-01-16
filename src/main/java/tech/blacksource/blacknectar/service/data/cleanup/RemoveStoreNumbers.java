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

package tech.blacksource.blacknectar.service.data.cleanup;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blacksource.blacknectar.service.ModuleProductionDatabase;
import tech.blacksource.blacknectar.service.ModuleServer;
import tech.blacksource.blacknectar.service.data.StoreRepository;
import tech.blacksource.blacknectar.service.exceptions.BlackNectarAPIException;
import tech.blacksource.blacknectar.service.stores.Store;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
public class RemoveStoreNumbers implements Callable<Void>
{

    private final static Logger LOG = LoggerFactory.getLogger(RemoveStoreNumbers.class);

    private final Aroma aroma;
    private final StoreRepository storeRepository;
    private final ExtractStoreCodeTransformation transformation;

    @Inject
    RemoveStoreNumbers(Aroma aroma, StoreRepository storeRepository, ExtractStoreCodeTransformation transformation)
    {
        checkThat(aroma, storeRepository, transformation)
            .are(notNull());

        this.aroma = aroma;
        this.storeRepository = storeRepository;
        this.transformation = transformation;
    }

    public static void main(String[] args) throws Exception
    {
        Injector injector = Guice.createInjector(new ModuleServer(), new ModuleProductionDatabase());

        RemoveStoreNumbers instance = injector.getInstance(RemoveStoreNumbers.class);

        instance.call();
    }

    @Override
    public Void call() throws Exception
    {
        long start = System.currentTimeMillis();

        try
        {
            execute();
        }
        catch (Exception ex)
        {
            makeNoteThatProcessFailed(ex);
            throw ex;
        }
        long end = System.currentTimeMillis();

        LOG.info("Script completed in {}ms", end - start);
        aroma.begin().titled("Stores Updated")
            .text("Successfully cleaned store numbers in {}ms", end - start)
            .withUrgency(Urgency.MEDIUM)
            .send();

        return null;

    }

    private void execute() throws Exception
    {
        List<Store> stores = storeRepository.getAllStores();

        stores.parallelStream()
            .filter(this::updateNeeded)
            .forEach(store ->
            {
                Store updatedStore = transformation.apply(store);
                makeNoteThatUpdatingStore(store, updatedStore);
                tryToUpdateStore(updatedStore);
            });

    }

    private boolean updateNeeded(Store store)
    {
        try
        {
            Store transformed = transformation.apply(store);

            return areDifferent(transformed, store);
        }
        catch (RuntimeException ex)
        {
            makeNoteThatFailedToTransform(store, ex);
            return false;
        }
    }

    private boolean areDifferent(Store oldStore, Store updatedStore)
    {
        return !Objects.equals(oldStore, updatedStore);
    }

    private void makeNoteThatProcessFailed(Exception ex)
    {
        String message = "Failed to run scrip to remove and strip store data";
        LOG.error(message, ex);

        aroma.begin().titled("Data Cleanup Failed")
            .text(message, ex)
            .withUrgency(Urgency.HIGH)
            .send();
    }

    private void makeNoteThatUpdatingStore(Store store, Store updatedStore)
    {
        String message = "Updating Store [{}] with [{]]";
        LOG.debug(message, store, updatedStore);

        aroma.begin().titled("Updating Store")
            .text("Old Store:\n{}\n\nNew Store:\n{}", store, updatedStore)
            .withUrgency(Urgency.LOW)
            .send();
    }

    private void tryToUpdateStore(Store updatedStore)
    {
        try
        {
            storeRepository.updateStore(updatedStore);
        }
        catch (BlackNectarAPIException ex)
        {
            makeNoteThatUpdateFailed(updatedStore, ex);
        }
    }

    private void makeNoteThatUpdateFailed(Store updatedStore, BlackNectarAPIException ex)
    {
        String message = "Failed to update store: [{}]";
        LOG.error(message, updatedStore, ex);

        aroma.begin().titled("Store Update Failed")
            .text("Failed to update store:\n{}\n\n{}", updatedStore, ex)
            .withUrgency(Urgency.HIGH)
            .send();

    }

    private void makeNoteThatFailedToTransform(Store store, RuntimeException ex)
    {
        String message = "Failed to try to transform store: {}";
        LOG.error(message, store, ex);
        aroma.begin().titled("Script Operation Failed")
            .text(message, store, ex)
            .withUrgency(Urgency.HIGH)
            .send();
    }

}
