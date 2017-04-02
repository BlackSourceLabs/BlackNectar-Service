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

package tech.blacksource.blacknectar.service.data;

import java.util.List;
import java.util.UUID;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.TestingResources;
import tech.blacksource.blacknectar.service.exceptions.DoesNotExistException;
import tech.blacksource.blacknectar.service.images.Image;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.images;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class SQLImageRepositoryIT
{

    private Aroma aroma;
    private JdbcTemplate database;
    private SQLImageMapper mapper;

    private SQLImageRepository instance;

    private List<Image> images;
    private Store store;
    private StoreRepository storeRepository;

    private Image image;
    private UUID storeId;
    private String imageId;

    @Before
    public void setUp() throws Exception
    {
        setupResources();
        setupData();

        instance = new SQLImageRepository(aroma, database, mapper);

    }

    @After
    public void tearDown() throws Exception
    {
        instance.deleteImage(image);

        images.forEach(instance::deleteImage);
    }

    private void setupResources() throws Exception
    {
        aroma = TestingResources.getAroma();
        database = TestingResources.createDatabaseConnection();
        mapper = TestingResources.getImageMapper();
        storeRepository = TestingResources.getStoreRepository();
    }

    private void setupData() throws Exception
    {
        store = storeRepository.getAllStores(1).get(0);

        images = listOf(images());
        images = images.stream()
            .map(img -> Image.Builder.fromImage(img).withStoreID(store.getStoreId()).build())
            .collect(toList());

        image = Lists.oneOf(images);
        storeId = image.getStoreId();
        imageId = image.getImageId();
    }

    @Test
    public void testAddImage()
    {
        instance.addImage(image);
        assertTrue(instance.hasImages(storeId));
    }

    @Test
    public void testGetImage()
    {
        instance.addImage(image);
        Image result = instance.getImage(storeId, imageId);
        assertThat(result, is(image));
    }

    @Test
    public void testGetImageWhenNotExist() throws Exception
    {
        assertThrows(() -> instance.getImage(storeId, imageId)).isInstanceOf(DoesNotExistException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetImagesForStore()
    {
        images.forEach(instance::addImage);

        List<Image> result = instance.getImagesForStore(storeId);

        assertTrue(Sets.containTheSameElements(result, images));
    }

    @Test
    public void testGetImagesForStoreWhenNoImages() throws Exception
    {
        List<Image> results = instance.getImagesForStore(storeId);
        assertThat(results, notNullValue());
        assertThat(results, is(empty()));
    }

    @Test
    public void testHasImages()
    {
        assertFalse(instance.hasImages(storeId));
        instance.addImage(image);
        assertTrue(instance.hasImages(storeId));
    }

    @Test
    public void testDeleteImage()
    {
        instance.addImage(image);
        assertTrue(instance.hasImages(storeId));

        instance.deleteImage(image);
        assertFalse(instance.hasImages(storeId));
    }

    @Test
    public void testDeleteImageWhenNotExist() throws Exception
    {
        instance.deleteImage(image);
    }

}
