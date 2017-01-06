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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.blacksource.blacknectar.service.exceptions.DoesNotExistException;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.images;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.generator.StringGenerators.strings;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class MemoryImageRepositoryTest
{

    private List<Image> images;
    private Image image;
    private Store store;

    private MemoryImageRepository instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new MemoryImageRepository();
    }

    private void setupData() throws Exception
    {
        images = listOf(images());
        store = one(stores());

        images = images
            .stream()
            .map(img -> Image.Builder.fromImage(img).withStoreID(store.getStoreId()).build())
            .collect(toList());

        image = Lists.oneOf(images);

    }

    private void setupMocks() throws Exception
    {

    }

    @Test
    public void testAddImage()
    {
        assertFalse(instance.hasImages(store.getStoreId()));

        instance.addImage(image);
        assertTrue(instance.hasImages(store.getStoreId()));
    }

    @Test
    public void testGetImageWhenExists() throws Exception
    {
        instance.addImage(image);

        Image result = instance.getImage(image.getStoreId(), image.getImageId());
        assertThat(result, notNullValue());
        assertThat(images, hasItem(result));
    }

    @Test
    public void testGetImageWhenNotExists()
    {
        String fakeId = one(strings());
        assertThrows(() -> instance.getImage(store.getStoreId(), fakeId))
            .isInstanceOf(DoesNotExistException.class);
    }

    @Test
    public void testGetImagesForStore()
    {
        images.forEach(instance::addImage);

        List<Image> result = instance.getImagesForStore(image.getStoreId());
        assertThat(result, is(images));
    }
    

    @Test
    public void testGetImagesForStoreWhenNoImages() throws Exception
    {
        List<Image> results = instance.getImagesForStore(image.getStoreId());
        assertThat(results, notNullValue());
        assertThat(results, is(empty()));
    }

    @Test
    public void testDeleteImage() throws Exception
    {
        instance.addImage(image);
        assertTrue(instance.hasImages(image.getStoreId()));

        instance.deleteImage(image);
        assertFalse(instance.hasImages(image.getStoreId()));
    }

    @Test
    public void testDeleteImageWhenImageDoesNotExist()
    {
        String fakeId = one(strings());
        instance.deleteImage(image.getStoreId(), fakeId);
    }

}
