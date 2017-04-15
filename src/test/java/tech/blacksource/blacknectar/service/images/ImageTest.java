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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.blacksource.blacknectar.service.BlackNectarGenerators;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

/**
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class ImageTest
{

    private Image instance;
    private Image other;
    private Image copy;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();
    }

    private void setupData() throws Exception
    {
        instance = BlackNectarGenerators.images().get();
        other = BlackNectarGenerators.images().get();
        copy = Image.Builder.fromImage(instance).build();
    }

    private void setupMocks() throws Exception
    {

    }

    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new Image(null, null, 0, 0, 0, null, null, null, null));
    }

    @Test
    public void testHasContentType()
    {
        assertTrue(instance.hasContentType());
    }

    @Test
    public void testHasImageType()
    {
        assertTrue(instance.hasImageType());
    }

    @Test
    public void testHasSource()
    {
        assertTrue(instance.hasSource());
    }

    @Test
    public void testHasURL()
    {
        assertTrue(instance.hasURL());
    }

    @Test
    public void testGetStoreId()
    {
        assertThat(instance.getStoreId(), notNullValue());
    }

    @Test
    public void testGetImageId()
    {
        assertThat(instance.getImageId(), notNullValue());
    }

    @Test
    public void testGetImageType()
    {
        assertThat(instance.getImageType(), notNullValue());
    }

    @Test
    public void testGetSource()
    {
        assertThat(instance.getSource(), notNullValue());
    }

    @Test
    public void testGetUrl()
    {
        assertThat(instance.getUrl(), notNullValue());
    }

    @Test
    public void testHashCode()
    {
        assertThat(instance.hashCode(), is(instance.hashCode()));
        assertThat(copy.hashCode(), is(instance.hashCode()));
        assertThat(other.hashCode(), not(instance.hashCode()));
    }

    @Test
    public void testEquals()
    {
        assertThat(instance, is(instance));
        assertThat(copy, is(instance));
        assertThat(other, not(instance));
    }

    @Test
    public void testToString()
    {
        assertThat(instance.toString(), not(isEmptyOrNullString()));
    }

}
