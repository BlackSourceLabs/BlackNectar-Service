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

package tech.blackhole.blacknectar.service.stores;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.GenerateDouble;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.doubles;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateDouble.Type.RANGE;

/**
 *
 * @author SirWellington
 */
@Repeat(100)
@RunWith(AlchemyTestRunner.class)
public class LocationTest 
{
    @GenerateDouble(value = RANGE, min = -90.0, max = 90.0)
    private Double latitude;
    
    @GenerateDouble(value = RANGE, min = -180.0, max = 180.0)
    private Double longitude;
    
    private Location location;
    private Location badLocation;
    
    @Before
    public void setUp() throws Exception
    {
        setupData();
    }

    private void setupData() throws Exception
    {
        location = new Location(latitude, longitude);
        
        double badLatitude = one(doubles(91, Double.MAX_VALUE));
        double badLongitude = one(doubles(-Double.MAX_VALUE, -91.0));
        badLocation = new Location(badLatitude, badLongitude);
    }

    @Test
    public void testValidLocation()
    {
        AlchemyAssertion<Location> assertion = Location.validLocation();
        assertThat(assertion, notNullValue());
        
        assertion.check(location);
    }
    
    @Test
    public void testValidLocationWithInvalid()
    {
        AlchemyAssertion<Location> assertion = Location.validLocation();
        assertThrows(() -> assertion.check(badLocation)).isInstanceOf(FailedAssertionException.class);
    }

    @Test
    public void testValidLatitude()
    {
        AlchemyAssertion<Double> assertion = Location.validLatitude();
        assertion.check(latitude);
    }
    @Test
    public void testValidLatitudeWithInvalid()
    {
        AlchemyAssertion<Double> assertion = Location.validLatitude();
        assertThrows(() -> assertion.check(badLocation.getLatitude()));
    }

    @Test
    public void testValidLongitude()
    {
        AlchemyAssertion<Double> assertion = Location.validLongitude();
        assertion.check(longitude);
    }

    @Test
    public void testValidLongitudeWithInvalid()
    {
        AlchemyAssertion<Double> assertion = Location.validLongitude();
        assertThrows(() -> assertion.check(badLocation.getLongitude()));
    }

    @Test
    public void testAsJSON()
    {
        JsonObject json = location.asJSON();
        assertThat(json, notNullValue());
        
        double lat = json.get(Location.Keys.LATITUDE).getAsDouble();
        double lon = json.get(Location.Keys.LONGITUDE).getAsDouble();
        
        assertThat(lat, is(location.getLatitude()));
        assertThat(lon, is(location.getLongitude()));
    }

}