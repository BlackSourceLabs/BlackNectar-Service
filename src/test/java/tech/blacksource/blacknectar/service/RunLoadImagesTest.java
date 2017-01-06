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

package tech.blacksource.blacknectar.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.data.SQLQueries;
import tech.blacksource.blacknectar.service.images.Image;
import tech.blacksource.blacknectar.service.images.ImageLoader;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.http.AlchemyHttp;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GenerateString;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static java.util.stream.Collectors.toMap;
import static org.mockito.Answers.RETURNS_MOCKS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.images;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.CollectionGenerators.listOf;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class RunLoadImagesTest
{

    @GenerateString(ALPHABETIC)
    private String source;
    private List<Store> stores;
    private Map<Store, Image> images;
    private RunLoadImages.Arguments arguments;
    private UUID storeId;

    @Mock
    private AlchemyHttp http;
    
    @Mock(answer = RETURNS_MOCKS)
    private Aroma aroma;

    @Mock
    private JdbcTemplate database;

    @Mock
    private ImageLoader imageLoader;

    private RunLoadImages instance;

    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();

        instance = new RunLoadImages(http, aroma, database);
    }

    private void setupData() throws Exception
    {
        stores = listOf(stores());
        images = stores.stream()
            .collect(toMap(s -> s, s -> one(images())));

        arguments = RunLoadImages.Arguments.Builder.newInstance()
            .withSleepTime(0, TimeUnit.MINUTES)
            .withSource(source)
            .withStores(stores)
            .withImageLoader(imageLoader)
            .build();
    }

    private void setupMocks() throws Exception
    {
        images.forEach((store, image) -> when(imageLoader.getImageFor(store)).thenReturn(image.getUrl()));
    }
    
    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new RunLoadImages(null, aroma, database));
        assertThrows(() -> new RunLoadImages(http, null, database));
        assertThrows(() -> new RunLoadImages(http, aroma, null));
    }

    @Test
    public void testAccept()
    {
        instance.accept(arguments);
        
        String expectedSQL = SQLQueries.INSERT_STORE_IMAGE;
        
        for (Store store : stores)
        {
            verify(imageLoader).getImageFor(store);
            
            UUID storeId = UUID.fromString(store.getStoreId());
            Image image = images.get(store);
            
//            verify(database).update(eq(expectedSQL), Matchers.<Object>anyVararg());
        }
    }

}
