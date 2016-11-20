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
import tech.blackhole.blacknectar.service.stores.Location;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.NumberGenerators.doubles;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class DistanceFormulaTest
{

    private DistanceFormula formula;
    
    private Location first;
    private Location second;
    
    @Before
    public void setUp() throws Exception
    {
        formula = new DistanceFormula.HarvesineDistance();

        setupData();
    }

    private void setupData() throws Exception
    {
        AlchemyGenerator<Double> latitudes = doubles(-90, 90.0);
        AlchemyGenerator<Double> longitudes = doubles(-180, 180);
        
        first = new Location(latitudes.get(), longitudes.get());
        second = new Location(latitudes.get(), longitudes.get());
    }

    @Test
    public void testHarvesine()
    {
        double distance = formula.distanceBetween(first, second);
        assertThat(distance, greaterThan(0.0));
    }

    @DontRepeat
    @Test
    public void testHarvesineAccuracy()
    {
        formula = new DistanceFormula.HarvesineDistance();

        first = new Location(36.12, -86.67);
        second = new Location(33.94, -118.40);

        double expected = 2887.2599506071124 * 1_000;
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
