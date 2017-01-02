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

package tech.blacksource.blacknectar.service.data;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.blacksource.blacknectar.service.stores.Location;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.doubles;
import static tech.sirwellington.alchemy.generator.NumberGenerators.negativeIntegers;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class GeoCalculatorTest
{
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private GeoCalculator formula;
    
    private Location first;
    private Location second;
    
    @Before
    public void setUp() throws Exception
    {
        formula = new GeoCalculator.HarvesineCalculator();

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
        formula = new GeoCalculator.HarvesineCalculator();

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
        formula = new GeoCalculator.HarvesineCalculator();
        assertThrows(() -> formula.distanceBetween(null, null));
    }

    @DontRepeat
    @Test
    public void testBearing()
    {
        LOG.info("First: {}, Second: {}", first, second);
        
        Location start = Location.with(-20.179275814170015, 127.56747344883186);
        Location end = Location.with(-45.62360109416997, 37.51602010912128);
        double bearing = formula.calculateBearingFromTo(start, end);
        double expected = 226.1;
        
        assertEquals(expected, bearing, 0.1);
    }
    
    @DontRepeat
    @Test
    public void testCalculateBearingWithBadArguments()
    {
        assertThrows(() -> formula.calculateBearingFromTo(null, second)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> formula.calculateBearingFromTo(first, null)).isInstanceOf(IllegalArgumentException.class);
    }
    
    @Test
    public void testCalculateDestination()
    {

        double distanceInMeters = formula.distanceBetween(first, second);
        double distanceInKM = distanceInMeters / 1000;
        double bearing = formula.calculateBearingFromTo(first, second);
        
        Location expected = second;
        
        Location destination = formula.calculateDestinationFrom(first, distanceInMeters, bearing);
        assertEquals(expected.getLatitude(), destination.getLatitude(), 1.0);
        assertEquals(expected.getLongitude(), destination.getLongitude(), 1.0);
    }
    
    @DontRepeat
    @Test
    public void testCalculateDestinationWithBadArgs()
    {
        AlchemyGenerator<Integer> negatives = negativeIntegers();
        AlchemyGenerator<Double> badBearings = doubles(360, 1000);
        
        assertThrows(() -> formula.calculateDestinationFrom(null, 0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> formula.calculateDestinationFrom(first, one(negatives), 0)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> formula.calculateDestinationFrom(first, 0, 0)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> formula.calculateDestinationFrom(first, 10, one(badBearings))).isInstanceOf(IllegalArgumentException.class);
    }
}
