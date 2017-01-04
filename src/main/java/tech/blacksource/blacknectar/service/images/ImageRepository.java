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
import sir.wellington.alchemy.collections.lists.Lists;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.blacksource.blacknectar.service.exceptions.BlackNectarAPIException;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;

/**
 * Responsible for storing and retrieving images.
 *
 * @author SirWellington
 */
public interface ImageRepository
{

    void addImage(@Required Image image) throws BlackNectarAPIException;

    Image getImage(@NonEmpty String imageId) throws BlackNectarAPIException;

    default Image getImageWithoutData(@NonEmpty String imageId) throws BlackNectarAPIException
    {
        Image image = this.getImage(imageId);

        return Image.Builder.fromImage(image)
            .unsetImageData()
            .build();
    }

    default List<Image> getImagesForStore(@NonEmpty String storeId) throws BlackNectarAPIException
    {
        checkThat(storeId)
            .throwing(BadArgumentException.class)
            .is(nonEmptyString())
            .is(validUUID());

        return getImagesForStore(UUID.fromString(storeId));
    }

    default List<Image> getImagesForStore(@Required Store store) throws BlackNectarAPIException
    {
        checkThat(store)
            .throwing(BadArgumentException.class)
            .is(notNull());

        return this.getImagesForStore(store.getStoreId());
    }

    List<Image> getImagesForStore(@NonEmpty UUID storeId) throws BlackNectarAPIException;

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
        String imageId = image.getImageId();
        deleteImage(imageId);
    }

    void deleteImage(@Required String imageId) throws BlackNectarAPIException;

}
