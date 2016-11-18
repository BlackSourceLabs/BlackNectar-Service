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

package tech.blackhole.blacknectar.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class DistanceFormulaTest
{

    private DistanceFormula formula;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();
    }

    private void setupData() throws Exception
    {

    }

    private void setupMocks() throws Exception
    {

    }

    @DontRepeat
    @Test
    public void testHarvesine()
    {
        formula = new DistanceFormula.HarvesineDistance();

        Location first = new Location(36.12, -86.67);
        Location second = new Location(33.94, -118.40);

        double expected = 2887.2599506071124;
        double result = formula.distanceBetween(first, second);

        Assert.assertEquals(expected, result, 0.2);
    }

    @DontRepeat
    @Test
    public void testHarvesineWithBadArguments()
    {
        formula = new DistanceFormula.HarvesineDistance();
        assertThrows(() -> formula.distanceBetween(null, null));
    }

}
