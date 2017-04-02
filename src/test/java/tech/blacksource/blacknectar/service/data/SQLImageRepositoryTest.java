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

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.images.Image;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Mockito.*;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.images;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class SQLImageRepositoryTest
{

    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;

    @Mock
    private JdbcTemplate database;

    @Mock
    private SQLImageMapper imageMapper;

    @Mock
    private ResultSet resultSet;

    private SQLImageRepository instance;

    private Store store;
    private UUID storeId;

    private List<Image> images;
    private Image image;
    private String imageId;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new SQLImageRepository(aroma, database, imageMapper);
    }

    private void setupData() throws Exception
    {
        store = one(stores());
        storeId = UUID.fromString(store.getStoreId());

        images = listOf(images(), 10)
            .stream()
            .map(img -> Image.Builder.fromImage(img).withStoreID(storeId).build())
            .collect(toList());

        image = Lists.oneOf(images);
        imageId = image.getImageId();
    }

    private void setupMocks() throws Exception
    {
        when(imageMapper.mapRow(resultSet, 0)).thenReturn(image);
    }

    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new SQLImageRepository(null, database, imageMapper));
        assertThrows(() -> new SQLImageRepository(aroma, null, imageMapper));
        assertThrows(() -> new SQLImageRepository(aroma, database, null));
    }

    @Test
    public void testAddImage()
    {
        instance.addImage(image);

        String sql = SQLQueries.INSERT_STORE_IMAGE;
        verify(database).update(sql,
                                image.getStoreId(),
                                image.getImageId(),
                                image.getHeight(),
                                image.getWidth(),
                                image.getSizeInBytes(),
                                image.getContentType(),
                                image.getImageType(),
                                image.getSource(),
                                image.getUrl().toString());
    }

    @Test
    public void testGetImage()
    {
        String sql = SQLQueries.QUERY_IMAGE;
        when(database.queryForObject(sql, imageMapper, storeId, imageId))
            .thenReturn(image);

        Image result = instance.getImage(storeId, imageId);
        assertThat(result, is(image));
    }

    @Test
    public void testGetImagesForStore()
    {
        String query = SQLQueries.QUERY_IMAGES_FOR_STORE;

        when(database.query(query, imageMapper, storeId))
            .thenReturn(images);

        List<Image> results = instance.getImagesForStore(storeId);
        assertThat(results, is(images));
    }

    @Test
    public void testHasImagesWhenHasImage()
    {
        String sql = SQLQueries.COUNT_IMAGES_FOR_STORE;

        when(database.queryForObject(sql, Integer.class, storeId))
            .thenReturn(1);

        assertTrue(instance.hasImages(storeId));
    }

    @Test
    public void testHasImagesWhenDoesNotHaveImages() throws Exception
    {
        String sql = SQLQueries.COUNT_IMAGES_FOR_STORE;

        when(database.queryForObject(sql, Integer.class, storeId))
            .thenReturn(0);

        assertFalse(instance.hasImages(storeId));
    }

    @Test
    public void testDeleteImage()
    {
        instance.deleteImage(image);

        String sql = SQLQueries.DELETE_IMAGE;
        verify(database).update(sql, storeId, imageId);
    }

}
