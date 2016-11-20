/*
 * Copyright 2016 BlackWholeLabs.
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

package tech.blackhole.blacknectar.service.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.blackhole.blacknectar.service.stores.Location;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static tech.blackhole.blacknectar.service.BlackNectarGenerators.locations;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.doubles;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class BlackNectarRequestTest
{

    @GeneratePojo
    private BlackNectarRequest generated;

    private BlackNectarRequest instance;

    private Location center;
    private double radiusInMeters;
    private int limit;
    private String searchTerm;

    @Before
    public void setUp() throws Exception
    {
        setupData();

        instance = new BlackNectarRequest();
    }

    private void setupData() throws Exception
    {
        center = one(locations());
        limit = one(integers(0, 1_000));
        radiusInMeters = one(doubles(0, 10_000));
        searchTerm = one(alphabeticString());

    }

    @Test
    public void testHasRadius()
    {
        assertThat(instance.hasRadius(), is(false));

        instance.radiusInMeters = radiusInMeters;

        if (radiusInMeters > 0)
        {
            assertThat(instance.hasRadius(), is(true));
        }
        else
        {
            assertThat(instance.hasRadius(), is(false));
        }
    }

    @Test
    public void testHasCenter()
    {
        assertThat(instance.hasCenter(), is(false));

        instance.center = center;
        assertThat(instance.hasCenter(), is(true));
    }

    @Test
    public void testHasLimit()
    {
        assertThat(instance.hasLimit(), is(false));

        instance.limit = limit;
        if (limit == 0)
        {
            assertThat(instance.hasLimit(), is(false));
        }
        else
        {
            assertThat(instance.hasLimit(), is(true));
        }
    }

    @Test
    public void testHasSearchTerm()
    {
        assertThat(instance.hasSearchTerm(), is(false));

        instance.searchTerm = searchTerm;
        assertThat(instance.hasSearchTerm(), is(true));
    }

    @Test
    public void testWithSearchTerm()
    {
    }

    @Test
    public void testWithCenter()
    {
    }

    @Test
    public void testWithLimit()
    {
    }

    @Test
    public void testWithRadius()
    {
    }

    @Test
    public void testHashCode()
    {
    }

    @Test
    public void testEquals()
    {
    }

    @Test
    public void testToString()
    {
    }

}
