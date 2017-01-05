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
import java.sql.SQLException;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.blacksource.blacknectar.service.images.Image;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.images;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;

/**
 *
 * @author SirWellington
 */
@Repeat(25)
@RunWith(AlchemyTestRunner.class)
public class SQLImageMapperTest 
{

    @Mock
    private ResultSet results;
    
    private Image image;
    
    private SQLImageMapper instance;
    
    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        instance = new SQLImageMapper.Impl();
    }


    private void setupData() throws Exception
    {
        image = one(images());
    }

    private void setupMocks() throws Exception
    {
        setupResultsWithImage(results, image);
    }

    @Test
    public void testMapRow() throws Exception
    {
        Image result = instance.mapRow(results, 0);
        assertThat(result, notNullValue());
        assertThat(result, is(image));
    }
    
    @Test
    public void testWhenImageDataNotPresent() throws Exception
    {
        when(results.getBytes(SQLColumns.Images.IMAGE_BINARY)).thenReturn(null);
        
        Image expected = Image.Builder.fromImage(image).withoutImageData().build();
        Image result = instance.mapRow(results, 0);
        
        assertThat(result, is(expected));
    }
    
    @Test
    public void testWhenURLNotPresent() throws Exception
    {
        when(results.getString(SQLColumns.Images.URL)).thenReturn(null);
        
        Image expected = Image.Builder.fromImage(image).withoutURL().build();
        Image result = instance.mapRow(results, 0);
        
        assertThat(result, is(expected));
    }

    private void setupResultsWithImage(ResultSet results, Image image) throws SQLException
    {
        when(results.getObject(SQLColumns.Images.STORE_ID, UUID.class)).thenReturn(image.getStoreId());
        when(results.getString(SQLColumns.Images.IMAGE_ID)).thenReturn(image.getImageId());
        when(results.getString(SQLColumns.Images.CONTENT_TYPE)).thenReturn(image.getContentType());
        when(results.getString(SQLColumns.Images.IMAGE_TYPE)).thenReturn(image.getImageType());
        when(results.getString(SQLColumns.Images.SOURCE)).thenReturn(image.getSource());
        when(results.getString(SQLColumns.Images.URL)).thenReturn(image.getUrl().toString());
        
        when(results.getInt(SQLColumns.Images.HEIGHT)).thenReturn(image.getHeight());
        when(results.getInt(SQLColumns.Images.WIDTH)).thenReturn(image.getWidth());
        when(results.getInt(SQLColumns.Images.SIZE_IN_BYTES)).thenReturn(image.getSizeInBytes());
        
        when(results.getBytes(SQLColumns.Images.IMAGE_BINARY)).thenReturn(image.getImageData());
    }

}