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

import java.util.List;
import java.util.UUID;

import com.google.inject.ImplementedBy;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.blacksource.blacknectar.service.exceptions.*;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 * Responsible for storing and retrieving images.
 *
 * @author SirWellington
 */
@ImplementedBy(MemoryImageRepository.class)
public interface ImageRepository
{

    /**
     * Adds an Image to the repository.
     *
     * @param image
     * @throws BlackNectarAPIException
     */
    void addImage(@Required Image image) throws BlackNectarAPIException;

    /**
     * Get an Image from the repository with the given Store and Image ID. This function does not return null, but instead
     *
     * @param storeId
     * @param imageId
     * @return
     * @throws BlackNectarAPIException
     */
    Image getImage(@Required UUID storeId, @NonEmpty String imageId) throws DoesNotExistException, BlackNectarAPIException;

    default Image getImage(@NonEmpty String storeId, @NonEmpty String imageId) throws DoesNotExistException, BlackNectarAPIException
    {
        checkThat(storeId)
            .throwing(BadArgumentException.class)
            .usingMessage("storeId is invalid")
            .is(validUUID());
        
        return this.getImage(UUID.fromString(storeId), imageId);
    }
    

    /**
     * Returns all of the images for a store.
     * <p>
     * If no images are found, and empty list is returned.
     *
     * @param storeId The {@linkplain Store#storeId ID of the Store} to search for.
     * @return
     * @throws BlackNectarAPIException
     */
    default List<Image> getImagesForStore(@NonEmpty String storeId) throws BlackNectarAPIException
    {
        checkThat(storeId)
            .throwing(BadArgumentException.class)
            .is(nonEmptyString())
            .is(validUUID());

        return getImagesForStore(UUID.fromString(storeId));
    }

    /**
     * Conveniences function for {@link #getImagesForStore(java.util.UUID) }.
     * 
     * @param store
     * @return
     * @throws BlackNectarAPIException 
     */
    default List<Image> getImagesForStore(@Required Store store) throws BlackNectarAPIException
    {
        checkThat(store)
            .throwing(BadArgumentException.class)
            .is(notNull());

        return this.getImagesForStore(store.getStoreId());
    }

    /**
     * Convenience function for {@link #getImagesForStore(java.lang.String) }.
     * 
     * @param storeId
     * @return
     * @throws BlackNectarAPIException 
     */
    List<Image> getImagesForStore(@NonEmpty UUID storeId) throws BlackNectarAPIException;

    /**
     * Checks whether the store has any pictures.
     * @param storeId
     * @return
     * @throws BlackNectarAPIException 
     */
    default boolean hasImages(@Required UUID storeId) throws BlackNectarAPIException
    {
        List<Image> images = getImagesForStore(storeId);

        return Lists.notEmpty(images);
    }

    default boolean hasImages(@NonEmpty String storeId) throws BlackNectarAPIException
    {
        checkThat(storeId)
            .throwing(BadArgumentException.class)
            .is(validUUID());

        return hasImages(UUID.fromString(storeId));
    }

    default void deleteImage(@Required Image image) throws BlackNectarAPIException
    {
        UUID storeId = image.getStoreId();
        String imageId = image.getImageId();
        
        deleteImage(storeId, imageId);
    }

    void deleteImage(@Required UUID storeId, @NonEmpty String imageId) throws BlackNectarAPIException;

}
