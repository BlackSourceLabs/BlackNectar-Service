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

import java.util.*;

import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.maps.Maps;
import tech.blacksource.blacknectar.service.exceptions.*;
import tech.sirwellington.alchemy.annotations.access.Internal;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
@Internal
final class MemoryImageRepository implements ImageRepository
{

    private final static Logger LOG = LoggerFactory.getLogger(MemoryImageRepository.class);

    /**
     * A Map of Store ID -> [Image]
     */
    private final Map<UUID, List<Image>> images = Maps.create();

    @Override
    public void addImage(Image image) throws BlackNectarAPIException
    {
        checkThat(image)
            .throwing(BadArgumentException.class)
            .is(notNull());

        UUID storeId = image.getStoreId();

        List<Image> imagesForStore = images.getOrDefault(storeId, Lists.create());
        imagesForStore.add(image);
        images.put(storeId, imagesForStore);
    }

    @Override
    public Image getImage(UUID storeId, String imageId) throws BlackNectarAPIException
    {
        checkThat(storeId)
            .usingMessage("missing storeId")
            .is(notNull());

        checkThat(imageId)
            .usingMessage("missing imageId")
            .is(nonEmptyString());

        List<Image> storeImages = images.getOrDefault(storeId, Lists.emptyList());

        return storeImages.stream()
            .filter(img -> Objects.equal(img.getImageId(), imageId))
            .findAny()
            .orElseThrow(() -> new DoesNotExistException());
    }

    @Override
    public List<Image> getImagesForStore(UUID storeId) throws BlackNectarAPIException
    {
        return images.getOrDefault(storeId, Lists.emptyList());
    }

    @Override
    public void deleteImage(UUID storeId, String imageId) throws BlackNectarAPIException
    {
        checkThat(storeId)
            .usingMessage("missing storeId")
            .is(notNull());

        checkThat(imageId)
            .usingMessage("imageId is missing")
            .is(nonEmptyString());

        List<Image> storeImages = images.getOrDefault(storeId, Lists.emptyList());

        if (Lists.notEmpty(storeImages))
        {
            storeImages.removeIf(img -> Objects.equal(img.getImageId(), imageId));
            
            images.put(storeId, storeImages);
        }

    }

}
