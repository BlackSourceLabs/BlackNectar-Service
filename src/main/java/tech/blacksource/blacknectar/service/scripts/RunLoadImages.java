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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.inject.Inject;

import com.google.common.collect.Queues;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.aroma.client.Priority;
import tech.blacksource.blacknectar.service.data.SQLQueries;
import tech.blacksource.blacknectar.service.images.ImageLoader;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.*;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;
import tech.sirwellington.alchemy.http.AlchemyHttp;

import static java.util.Objects.isNull;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.CLIENT;
import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.nonEmptyCollection;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.nonEmptyList;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * Designed to find images for a store and load them into "Store Images" Database.
 * <p>
 * To use this class, create {@linkplain Arguments Arguments} using
 * {@linkplain Arguments.Builder#newInstance() an argument builder}.
 *
 * @author SirWellington
 */
@BuilderPattern(role = CLIENT)
class RunLoadImages implements Consumer<RunLoadImages.Arguments>
{

    private final static Logger LOG = LoggerFactory.getLogger(RunLoadImages.class);

    private final AlchemyHttp http;
    private final Aroma aroma;
    private final JdbcTemplate database;

    @Inject
    RunLoadImages(AlchemyHttp http, Aroma aroma, JdbcTemplate database)
    {
        checkThat(http, aroma, database)
            .are(notNull());

        this.http = http;
        this.aroma = aroma;
        this.database = database;
    }

    @Override
    public void accept(RunLoadImages.Arguments args)
    {
        checkThat(args).is(notNull());

        long startTime = System.currentTimeMillis();
        long sleepTimeMillis = args.sleepTimeMillis;

        int totalStores = args.stores.size();
        int totalStoresProcessed = 0;
        int totalSuccesses = 0;

        while (Lists.notEmpty(args.stores))
        {
            Store nextStore = args.stores.poll();

            if (isNull(nextStore))
            {
                break;
            }

            boolean success = tryToProcessStore(nextStore, args);

            totalStoresProcessed += 1;

            if (success)
            {
                totalSuccesses += 1;
                makeNoteOfCompletion(args, nextStore, totalSuccesses, totalStoresProcessed, totalStores);
            }
            else
            {
                makeNoteThatOperationToProcessStoreFailed(nextStore, args, totalSuccesses, totalStoresProcessed, totalStores);
            }

            tryToSleepFor(sleepTimeMillis);
        }

        long endTime = System.currentTimeMillis();
        long runtimeMillis = endTime - startTime;
        long runtimeHours = TimeUnit.MILLISECONDS.toHours(runtimeMillis);

        makeNoteOfScriptCompletion(args, totalSuccesses, totalStoresProcessed, totalStores, runtimeHours);
    }

    private void tryToSleepFor(long sleepTime)
    {
        if (sleepTime <= 0)
        {
            return;
        }

        try
        {
            Thread.sleep(sleepTime);
        }
        catch (InterruptedException ex)
        {
            makeNoteThatSleepInterrupted(ex);
        }
    }

    private boolean tryToProcessStore(Store store, Arguments args)
    {
        try
        {
            processStore(store, args);

            return true;
        }
        catch (IOException | RuntimeException ex)
        {
            makeNoteThatOperationToSaveImageFailed(store, args, ex);
            return false;
        }
    }

    private void processStore(Store store, Arguments args) throws IOException
    {
        ImageLoader imageLoader = args.imageLoader;

        List<URL> images = imageLoader.getImagesFor(store);

        checkThat(images)
            .usingMessage("No Images found for Store: " + store)
            .is(notNull())
            .is(nonEmptyList());

        images.stream().forEach(url -> this.tryToStoreImage(store, args, url));
    }

    private void tryToStoreImage(Store store, Arguments args, URL imageUrl)
    {
        try
        {
            storeImage(store, args, imageUrl);
        }
        catch (IOException ex)
        {
            makeNoteThatOperationToSaveImageFailed(store, args, ex);
        }
    }

    private void storeImage(Store store, Arguments args, URL imageUrl) throws IOException
    {
        byte[] imageData = http.go().download(imageUrl);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
        int height = image.getHeight();
        int width = image.getWidth();
        int size = imageData.length;

        String contentType = getContentTypeFor(imageUrl);
        String imageType = getImageTypeFrom(contentType);
        String source = args.source;

        saveImageInfoFor(store, imageUrl, width, height, size, contentType, imageType, source);

        makeNoteThatImageSavedForStore(imageUrl, store, args);
    }

    private String getContentTypeFor(URL imageUrl) throws IOException
    {
        URLConnection connection = imageUrl.openConnection();
        String contentType = connection.getContentType();
        
        if (connection instanceof HttpURLConnection)
        {
            ((HttpURLConnection) connection).disconnect();
        }

        return contentType;
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

    private void saveImageInfoFor(Store store,
                                  URL imageUrl,
                                  int width,
                                  int height,
                                  int size,
                                  String contentType,
                                  String imageType,
                                  String source)
    {

        String statementToInsertImage = SQLQueries.INSERT_STORE_IMAGE;

        UUID storeId = UUID.fromString(store.getStoreId());
        String imageLink = imageUrl.toString();
        String imageId = imageLink;

        database.update(statementToInsertImage,
                        storeId,
                        imageId,
                        height,
                        width,
                        size,
                        contentType,
                        imageType,
                        source,
                        imageLink);
    }

    //================================================================
    // Notes of Completion
    //================================================================
    private void makeNoteOfCompletion(Arguments args, Store store, int totalSuccesses, int totalStoresProcessed, int totalStores)
    {
        String source = args.source;
        String message = "Found image from {} for Store. {}/{} successful. {}/{} done. \n\nStore: {}";

        LOG.info(message, source, totalSuccesses, totalStoresProcessed, totalStoresProcessed, totalStores, store);

        aroma.begin().titled("Image Saved")
            .withBody(message, source, totalSuccesses, totalStoresProcessed, totalStoresProcessed, totalStores, store)
            .withPriority(Priority.LOW)
            .send();

    }

    private void makeNoteThatImageSavedForStore(URL imageUrl, Store store, Arguments args)
    {
        String source = args.source;

        String message = "Successfully saved Image [{}: {}] for store: {}";

        LOG.debug(message, source, imageUrl, store);

        aroma.begin().titled("Image Saved")
            .withBody(message, source, imageUrl, store)
            .withPriority(Priority.LOW)
            .send();
    }

    private void makeNoteThatOperationToSaveImageFailed(Store store, Arguments args, Exception ex)
    {
        String source = args.source;
        String message = "Failed to find an image from {} for store: {}";

        LOG.error(message, source, store, ex);

        aroma.begin().titled("Image Load Failed")
            .withBody(message, source, store, ex)
            .withPriority(Priority.HIGH)
            .send();
    }

    private void makeNoteThatSleepInterrupted(InterruptedException ex)
    {
        String message = "Thread Sleep Interrupted";

        LOG.error(message, ex);

        aroma.begin().titled("Script Interrupted")
            .withBody(message, ex)
            .withPriority(Priority.MEDIUM)
            .send();
    }

    private void makeNoteThatOperationToProcessStoreFailed(Store store,
                                                           Arguments args,
                                                           int totalSuccesses,
                                                           int totalStoresProcessed,
                                                           int totalStores)
    {
        String source = args.source;
        String message = "{}/{} - Failed to process image from {} for store: {}";

        LOG.error(message, totalStoresProcessed, totalStores, source, store);

        aroma.begin().titled("Store Image Load Failed")
            .withBody(message, totalStoresProcessed, totalStores, source, store)
            .withPriority(Priority.MEDIUM)
            .send();
    }

    private void makeNoteOfScriptCompletion(Arguments args, int totalSuccesses, int totalStoresProcessed, int totalStores,
                                            long runtimeHours)
    {
        String message = "Script[{}] completed in {} hours, with {}/{} successful and {}/{} processed";
        String source = args.source;

        LOG.info(message, source, runtimeHours, totalSuccesses, totalStoresProcessed, totalStoresProcessed, totalStores);

        aroma.begin().titled("Script Finished")
            .withBody(message, source, runtimeHours, totalSuccesses, totalStoresProcessed, totalStoresProcessed, totalStores)
            .withPriority(Priority.MEDIUM)
            .send();
    }

    //================================================================
    // Arguments Class
    //================================================================
    static class Arguments
    {

        private final long sleepTimeMillis;
        private final String source;
        private final ImageLoader imageLoader;
        private final Queue<Store> stores;

        Arguments(long sleepTimeMillis, String source, ImageLoader imageLoader, Queue<Store> stores)
        {
            checkThat(source, imageLoader, stores)
                .are(notNull());

            checkThat((Collection<Store>) stores)
                .usingMessage("stores cannot be empty")
                .is(nonEmptyCollection());

            checkThat(sleepTimeMillis).is(greaterThanOrEqualTo(0L));

            this.sleepTimeMillis = sleepTimeMillis;
            this.source = source;
            this.imageLoader = imageLoader;
            this.stores = stores;
        }

        @Override
        public int hashCode()
        {
            int hash = 5;
            hash = 83 * hash + (int) (this.sleepTimeMillis ^ (this.sleepTimeMillis >>> 32));
            hash = 83 * hash + Objects.hashCode(this.source);
            hash = 83 * hash + Objects.hashCode(this.imageLoader);
            hash = 83 * hash + Objects.hashCode(this.stores);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Arguments other = (Arguments) obj;
            if (this.sleepTimeMillis != other.sleepTimeMillis)
            {
                return false;
            }
            if (!Objects.equals(this.source, other.source))
            {
                return false;
            }
            if (!Objects.equals(this.imageLoader, other.imageLoader))
            {
                return false;
            }
            if (!Objects.equals(this.stores, other.stores))
            {
                return false;
            }
            return true;
        }

        @Override
        public String toString()
        {
            return "Arguments{" + "sleepTimeMillis=" + sleepTimeMillis + ", source=" + source + ", imageLoader=" + imageLoader + ", stores=" + stores + '}';
        }

        //================================================================
        // Arguments Builder
        //================================================================
        static class Builder
        {

            private long sleepTimeMillis = 500;
            private String source;
            private ImageLoader imageLoader;
            private Queue<Store> stores = Queues.newArrayDeque();

            static Builder newInstance()
            {
                return new Builder();
            }

            Builder()
            {

            }

            Builder withSleepTime(@Positive int sleepTime, @Required TimeUnit timeUnit)
            {
                checkThat(sleepTime).is(greaterThanOrEqualTo(0));

                checkThat(timeUnit).is(notNull());

                this.sleepTimeMillis = timeUnit.toMillis(sleepTime);
                return this;
            }

            Builder withSource(@NonEmpty String source)
            {
                checkThat(source).is(nonEmptyString());

                this.source = source;
                return this;
            }

            Builder withImageLoader(@Required ImageLoader imageLoader)
            {
                checkThat(imageLoader).is(notNull());

                this.imageLoader = imageLoader;
                return this;
            }

            Builder withStores(@NonEmpty Queue<Store> stores)
            {
                checkThat((Collection<Store>) stores)
                    .is(nonEmptyCollection());

                this.stores = Queues.newArrayDeque(stores);
                return this;
            }

            Builder withStores(@NonEmpty List<Store> stores)
            {
                checkThat(stores).is(nonEmptyList());

                this.stores = Queues.newArrayDeque(stores);
                return this;
            }

            Arguments build() throws IllegalStateException
            {
                checkThat(source)
                    .usingMessage("source is missing")
                    .is(nonEmptyString());

                checkThat(imageLoader)
                    .usingMessage("imageLoader is missing")
                    .is(notNull());

                checkThat((Collection<Store>) stores)
                    .is(nonEmptyCollection());

                return new Arguments(sleepTimeMillis, source, imageLoader, stores);
            }

        }

    }

}
