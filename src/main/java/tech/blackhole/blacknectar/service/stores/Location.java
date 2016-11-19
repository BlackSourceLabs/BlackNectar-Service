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

import tech.sirwellington.alchemy.annotations.concurrency.Immutable;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.annotations.objects.Pojo;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.lessThanOrEqualTo;

/**
 * A {@link Location} represents a Global Geo-Coordinate.
 *
 * @author SirWellington
 */
@Pojo
@Immutable
@ThreadSafe
public final class Location
{
    
    private final double latitude;
    private final double longitude;
    
    public Location(double latitude, double longitude)
    {
        checkThat(latitude).is(validLatitude());
        checkThat(longitude).is(validLongitude());
        
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    static AlchemyAssertion<Location> validLocation()
    {
        return location ->
        {
            checkThat(location)
                .usingMessage("Location cannot be null")
                .is(notNull());
            
            checkThat(location.latitude).is(validLatitude());
            checkThat(location.longitude).is(validLongitude());
        };
    }
    
    static AlchemyAssertion<Double> validLatitude()
    {
        return lat ->
        {
            checkThat(lat)
                .usingMessage("Latitude must be between -90 and 90")
                .is(lessThanOrEqualTo(90.0))
                .is(greaterThanOrEqualTo(-90.0));
        };
        
    }
    
    static AlchemyAssertion<Double> validLongitude()
    {
        return lon ->
        {
            checkThat(lon)
                .usingMessage("Longitude must be between -180 and 180")
                .is(greaterThanOrEqualTo(-180.0))
                .is(lessThanOrEqualTo(180.0));
            };
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.latitude) ^ (Double.doubleToLongBits(this.latitude) >>> 32));
        hash = 89 * hash + (int) (Double.doubleToLongBits(this.longitude) ^ (Double.doubleToLongBits(this.longitude) >>> 32));
        return hash;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final Location other = (Location) obj;
        if (Double.doubleToLongBits(this.latitude) != Double.doubleToLongBits(other.latitude))
        {
            return false;
        }
        if (Double.doubleToLongBits(this.longitude) != Double.doubleToLongBits(other.longitude))
        {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString()
    {
        return "Location{" + "latitude=" + latitude + ", longitude=" + longitude + '}';
    }
}
