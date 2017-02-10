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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.blacksource.blacknectar.service.stores.Location;
import tech.sirwellington.alchemy.generator.NumberGenerators;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.GeneratePojo;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.locations;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.NumberGenerators.negativeIntegers;
import static tech.sirwellington.alchemy.generator.NumberGenerators.positiveDoubles;
import static tech.sirwellington.alchemy.generator.NumberGenerators.positiveIntegers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class BlackNectarSearchRequestTest
{

    @GeneratePojo
    private BlackNectarSearchRequest generated;

    private BlackNectarSearchRequest instance;

    private Location center;
    private double radiusInMeters;
    private int limit;
    private String searchTerm;
    
    private String zipCode;

    @Before
    public void setUp() throws Exception
    {
        setupData();

        instance = new BlackNectarSearchRequest();
    }

    private void setupData() throws Exception
    {
        center = one(locations());
        limit = one(positiveIntegers());
        radiusInMeters = one(positiveDoubles());
        searchTerm = one(alphabeticString());
        
        int zipCodeInt = integers(10_000, 99_999).get();
        zipCode = String.valueOf(zipCodeInt);
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
    public void testHasZipCode() throws Exception
    {
        assertThat(instance.hasZipCode(), is(false));
        
        instance.zipCode = zipCode;
        assertThat(instance.hasZipCode(), is(true));
    }

    @Test
    public void testWithSearchTerm()
    {
        BlackNectarSearchRequest result = instance.withSearchTerm(searchTerm);
        assertThat(result, notNullValue());
        assertThat(result.searchTerm, is(searchTerm));
    }
    
    @DontRepeat
    @Test
    public void testWithSearchTermWithBadArg()
    {
        assertThrows(() -> instance.withSearchTerm(""));
    }

    @Test
    public void testWithCenter()
    {
        BlackNectarSearchRequest result = instance.withCenter(center);
        assertThat(result, notNullValue());
        assertThat(result.center, is(center));
    }

    @DontRepeat
    @Test
    public void testWithCenterWithBadArg()
    {
        assertThrows(() -> instance.withCenter(null));
    }

    @Test
    public void testWithLimit()
    {
        BlackNectarSearchRequest result = instance.withLimit(limit);
        assertThat(result, notNullValue());
        assertThat(result.limit, is(limit));
    }

    @Test
    public void testWithLimitWithBadArgs()
    {
        final int badLimit = one(negativeIntegers());
        assertThrows(() -> instance.withLimit(badLimit));
    }

    @Test
    public void testWithRadius()
    {
        BlackNectarSearchRequest result = instance.withRadius(radiusInMeters);
        assertThat(result, notNullValue());
        assertThat(result.radiusInMeters, is(radiusInMeters));
    }

    @Test
    public void testWithRadiusWithBadArgs()
    {
        double badRadius = one(NumberGenerators.doubles(-10000, -1));
        assertThrows(() -> instance.withRadius(badRadius));
    }
    
    @Test
    public void testWithZipCode() throws Exception
    {
        BlackNectarSearchRequest result = instance.withZipCode(zipCode);
        assertThat(result, notNullValue());
        assertThat(result.zipCode, is(zipCode));
    }
    
    @Test
    public void testHasZipCodeWithBadArgs() throws Exception
    {
        String badZipCode = one(alphabeticString());
        assertThrows(() -> instance.withZipCode(badZipCode)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> instance.withZipCode("")).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> instance.withZipCode(null)).isInstanceOf(IllegalArgumentException.class);
    }

}
