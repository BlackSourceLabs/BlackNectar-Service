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

package tech.blacksource.blacknectar.service.stores;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.doubles;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
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
    
    @Before
    public void setUp() throws Exception
    {
        setupData();
    }

    private void setupData() throws Exception
    {
        location = new Location(latitude, longitude);
    }

    @Test
    public void testValidLocation()
    {
        AlchemyAssertion<Location> assertion = Location.validLocation();
        assertThat(assertion, notNullValue());
        
        assertion.check(location);
    }
    
    @Test
    public void testInvalidLocation()
    {
        double badLatitude = one(doubles(91, Double.MAX_VALUE));
        double badLongitude = one(doubles(-Double.MAX_VALUE, -91.0));
        
        assertThrows(() -> new Location(badLatitude, badLongitude)).isInstanceOf(IllegalArgumentException.class);
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

    @Test
    public void testWith()
    {
        Location result = Location.with(latitude, longitude);
        assertThat(result, notNullValue());
        assertThat(result, is(location));
    }


}