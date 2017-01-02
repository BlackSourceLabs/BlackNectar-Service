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
import com.google.common.io.Resources;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blacksource.blacknectar.service.data.SQLQueries;
import tech.blacksource.blacknectar.service.data.StoreRepository;
import tech.blacksource.blacknectar.service.images.Google;
import tech.blacksource.blacknectar.service.images.ImageLoader;
import tech.blacksource.blacknectar.service.stores.Store;

/**
 *
 * @author SirWellington
 */
public final class RunSearchGoogleImages implements Callable<Void>
{

    private final static Logger LOG = LoggerFactory.getLogger(RunSearchGoogleImages.class);

    private final Aroma aroma;
    private final JdbcTemplate database;
    private final StoreRepository storeRepository;
    private final ImageLoader imageLoader;

    private final AtomicInteger successful = new AtomicInteger();
    private final String source = "Google";

    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    @Inject
    RunSearchGoogleImages(Aroma aroma, JdbcTemplate database, StoreRepository storeRepository, @Google ImageLoader imageLoader)
    {
        this.aroma = aroma;
        this.database = database;
        this.storeRepository = storeRepository;
        this.imageLoader = imageLoader;
    }

    public static void main(String[] args) throws Exception
    {
        Injector injector = Guice.createInjector(new ModuleServer());

        RunSearchGoogleImages instance = injector.getInstance(RunSearchGoogleImages.class);
        instance.call();
    }

    @Override
    public Void call() throws Exception
    {
        LOG.debug("Beginning script.");

        List<Store> stores = storeRepository.getAllStores();

        Queue<Store> queue = Queues.newLinkedBlockingQueue(stores);

        loadNextStore(queue);

        return null;
    }

    private void loadNextStore(Queue<Store> queue)
    {
        Store store = queue.poll();

        if (store == null)
        {
            makeNoteOfCompletion();
            return;
        }

        tryToSaveImageFor(store);

        executor.schedule(() -> this.loadNextStore(queue), 500, TimeUnit.MILLISECONDS);
    }

    private void tryToSaveImageFor(Store store)
    {
        try
        {
            saveImageFor(store);
        }
        catch (Exception ex)
        {
            makeNoteThatOperationToSaveImageFailed(store, ex);
        }
    }

    private void saveImageFor(Store store) throws IOException
    {

        URL imageURL = imageLoader.getImageFor(store);
        if (Objects.isNull(imageURL))
        {
            LOG.info("No Image found for Store: {}", store);
            return;
        }

        BufferedImage image = ImageIO.read(imageURL);

        int height = image.getHeight();
        int width = image.getWidth();
        byte[] binary = Resources.toByteArray(imageURL);
        int size = binary.length;
        String contentType = getContentTypeFor(imageURL);
        String imageType = getImageTypeFrom(contentType);

        saveImageInfoFor(store, imageURL, width, height, binary, size, contentType, imageType);

        makeNoteThatImageSavedForStore(imageURL, store);
    }

    private String getImageTypeFrom(String contentType) throws IOException
    {
        switch (contentType)
        {
            case "image/png":
            case "IMAGE/PNG":
                return "PNG";
            case "image/jpg":
            case "image/jpeg":
            case "IMAGE/JPEG":
            case "IMAGE/JPG":
                return "JPG";
            case "image/bmp":
            case "IMAGE/BMP":
                return "BMP";
        }

        return null;
    }

    private void makeNoteOfCompletion()
    {
        String message = "Successfully found yelp images for {} stores";
        LOG.info(message, successful.get());
        aroma.begin().titled("Script Finished")
            .text(message, successful.get())
            .withUrgency(Urgency.MEDIUM)
            .send();
    }

    private void saveImageInfoFor(Store store,
                                  URL imageURL,
                                  int width,
                                  int height,
                                  byte[] binary,
                                  int size,
                                  String contentType,
                                  String imageType)
    {

        String statementToInsertImage = SQLQueries.INSERT_IMAGE;

        String imageId = UUID.randomUUID().toString();

        database.update(statementToInsertImage,
                        imageId,
                        binary,
                        height,
                        width,
                        size,
                        contentType,
                        imageType,
                        source,
                        imageURL.toString());

        String statementToInsertStoreImage = SQLQueries.INSERT_STORE_IMAGE;

        UUID storeId = UUID.fromString(store.getStoreId());

        database.update(statementToInsertStoreImage,
                        storeId,
                        imageId,
                        "Cover");
    }

    private String getContentTypeFor(URL imageURL) throws IOException
    {
        URLConnection connection = imageURL.openConnection();
        String contentType = connection.getContentType();

        return contentType;
    }

    private void makeNoteThatImageSavedForStore(URL imageURL, Store store)
    {
        int count = successful.incrementAndGet();

        String message = "{} - Successfully saved Image [{}: {}] for store: {}";
        LOG.debug(message, count, source, imageURL, store);
        aroma.begin().titled("Image Saved")
            .text(message, count, source, imageURL, store)
            .withUrgency(Urgency.LOW)
            .send();
    }

    private void makeNoteThatOperationToSaveImageFailed(Store store, Exception ex)
    {
        String message = "Failed to find and save an image for store: {}";
        LOG.error(message, store, ex);
        aroma.begin().titled("Image Load Failed")
            .text(message, store, ex)
            .withUrgency(Urgency.HIGH)
            .send();
    }

}
