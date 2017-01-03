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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blacksource.blacknectar.service.data.SQLQueries;
import tech.blacksource.blacknectar.service.images.ImageLoader;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Positive;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;

import static java.util.Objects.isNull;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.CLIENT;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.CollectionAssertions.nonEmptyCollection;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.positiveInteger;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * Designed to load the Store Images database with images obtained from the corresponding {@link ImageLoader}.
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

    private Aroma aroma;
    private JdbcTemplate database;

    @Override
    public void accept(RunLoadImages.Arguments args)
    {
        checkThat(args);

        long startTime = System.currentTimeMillis();
        long sleepTime = args.timeUnit.toMillis(args.frequency);
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

            tryToSleepFor(sleepTime);
        }
        
        long endTime = System.currentTimeMillis();
        long runtimeMillis = endTime - startTime;
        long runtimeHours = TimeUnit.MILLISECONDS.toHours(runtimeMillis);
        
        makeNoteOfScriptCompletion(args, totalSuccesses, totalStoresProcessed, totalStores, runtimeHours);
    }

    private void tryToSleepFor(long sleepTime)
    {
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

        URL imageURL = imageLoader.getImageFor(store);

        if (Objects.isNull(imageURL))
        {
            LOG.info("No Image found for Store: {}", store);
            return;
        }

        byte[] imageData = Resources.toByteArray(imageURL);

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageData));
        int height = image.getHeight();
        int width = image.getWidth();
        int size = imageData.length;

        String contentType = getContentTypeFor(imageURL);
        String imageType = getImageTypeFrom(contentType);
        String source = args.source;

        saveImageInfoFor(store, imageURL, width, height, imageData, size, contentType, imageType, source);

        makeNoteThatImageSavedForStore(imageURL, store, args);
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

    private void makeNoteOfCompletion(Arguments args, Store store, int totalSuccesses, int totalStoresProcessed, int totalStores)
    {
        String source = args.source;
        String message = "Found image from {} for Store. {}/{} successful. {}/{} done. \n\nStore: {}";
        LOG.info(message, source, totalSuccesses, totalStoresProcessed, totalStoresProcessed, totalStores, store);
        aroma.begin().titled("Image Saved")
            .text(message, source, totalSuccesses, totalStoresProcessed, totalStoresProcessed, totalStores, store)
            .withUrgency(Urgency.LOW)
            .send();

    }

    private void saveImageInfoFor(Store store,
                                  URL imageURL,
                                  int width,
                                  int height,
                                  byte[] binary,
                                  int size,
                                  String contentType,
                                  String imageType,
                                  String source)
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

    private void makeNoteThatImageSavedForStore(URL imageURL, Store store, Arguments args)
    {
        String source = args.source;

        String message = "Successfully saved Image [{}: {}] for store: {}";
        LOG.debug(message, source, imageURL, store);
        aroma.begin().titled("Image Saved")
            .text(message, source, imageURL, store)
            .withUrgency(Urgency.LOW)
            .send();
    }

    private void makeNoteThatOperationToSaveImageFailed(Store store, Arguments args, Exception ex)
    {
        String source = args.source;
        String message = "Failed to find an image from {} for store: {}";

        LOG.error(message, source, store, ex);
        aroma.begin().titled("Image Load Failed")
            .text(message, source, store, ex)
            .withUrgency(Urgency.HIGH)
            .send();
    }

    private void makeNoteThatSleepInterrupted(InterruptedException ex)
    {
        String message = "Thread Sleep Interrupted";
        LOG.error(message, ex);
        aroma.begin().titled("Script Interrupted")
            .text(message, ex)
            .withUrgency(Urgency.MEDIUM)
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
            .text(message, totalStoresProcessed, totalStores, source, store)
            .withUrgency(Urgency.MEDIUM)
            .send();
    }

    private void makeNoteOfScriptCompletion(Arguments args, int totalSuccesses, int totalStoresProcessed, int totalStores, long runtimeHours)
    {
        String message = "Script[{}] completed in {} hours, with {}/{} successful and {}/{} processed";
        String source = args.source;
        LOG.info(message, source, runtimeHours, totalSuccesses, totalStoresProcessed, totalStoresProcessed, totalStores);
        
        aroma.begin().titled("Script Finished")
            .text(message, source, runtimeHours, totalSuccesses, totalStoresProcessed, totalStoresProcessed, totalStores)
            .withUrgency(Urgency.MEDIUM)
            .send();
    }

    static class Arguments
    {

        private final TimeUnit timeUnit;
        private final int frequency;
        private final String source;
        private final ImageLoader imageLoader;
        private final Queue<Store> stores;

        Arguments(TimeUnit timeUnit, int frequency, String source, ImageLoader imageLoader, Queue<Store> stores)
        {
            checkThat(timeUnit, source, imageLoader, stores)
                .are(notNull());

            checkThat((Collection<Store>) stores)
                .usingMessage("stores cannot be empty")
                .is(nonEmptyCollection());

            checkThat(frequency)
                .is(positiveInteger());

            this.timeUnit = timeUnit;
            this.frequency = frequency;
            this.source = source;
            this.imageLoader = imageLoader;
            this.stores = stores;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 29 * hash + Objects.hashCode(this.timeUnit);
            hash = 29 * hash + this.frequency;
            hash = 29 * hash + Objects.hashCode(this.source);
            hash = 29 * hash + Objects.hashCode(this.imageLoader);
            hash = 29 * hash + Objects.hashCode(this.stores);
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
            if (this.frequency != other.frequency)
            {
                return false;
            }
            if (!Objects.equals(this.source, other.source))
            {
                return false;
            }
            if (this.timeUnit != other.timeUnit)
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
            return "Arguments{" + "timeUnit=" + timeUnit + ", frequency=" + frequency + ", source=" + source + ", imageLoader=" + imageLoader + ", stores=" + stores + '}';
        }

        static class Builder
        {

            private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
            private int frequency = 500;
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

            Builder withTimeUnit(@Required TimeUnit timeUnit)
            {
                checkThat(timeUnit).is(notNull());

                this.timeUnit = timeUnit;
                return this;
            }

            Builder withFrequency(@Positive int frequency)
            {
                checkThat(frequency)
                    .is(positiveInteger());

                this.frequency = frequency;
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

                return new Arguments(timeUnit, frequency, source, imageLoader, stores);
            }

        }

    }

}
