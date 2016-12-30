/*
 * Copyright 2016 BlackSourceLabs.
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

package tech.blacksource.blacknectar.service.stores;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.sets.Sets;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateInteger;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateInteger.Type.RANGE;

/**
 *
 * @author SirWellington
 */
@Repeat(25)
@RunWith(AlchemyTestRunner.class)
public class IDGeneratorTest
{

    private IDGenerator instance;

    @GenerateInteger(value = RANGE, min = 100, max = 1000)
    private Integer iterations;

    @Before
    public void setUp() throws Exception
    {

    }

    @Test
    public void testGenerateKey()
    {
    }

    @Test
    public void testUuids()
    {
        instance = IDGenerator.uuids();
        assertThat(instance, notNullValue());

        assertAllValuesAreUnique(instance);
    }

    @Test
    public void testSerialInteger()
    {

        instance = IDGenerator.serialInteger();
        assertThat(instance, notNullValue());

        assertAllValuesAreUnique(instance);
    }

    private void assertAllValuesAreUnique(IDGenerator generator)
    {
        Set<String> ids = Sets.create();

        for (int i = 0; i < iterations; ++i)
        {
            ids.add(generator.generateKey());
        }

        assertThat(ids.size(), is(iterations));

    }

}
