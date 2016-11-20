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

import com.google.common.base.Strings;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.blackhole.blacknectar.service.stores.Location;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.concurrency.Mutable;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadUnsafe;
import tech.sirwellington.alchemy.annotations.objects.Pojo;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.positiveInteger;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * This is a mutable request object that encapsulates parameters for a BlackNectar API request.
 * All of the fields are publicly accessible and mutable, which makes this class Thread-Unsafe.
 * 
 * @author SirWellington
 */
@Pojo
@Mutable
@ThreadUnsafe
public class BlackNectarRequest
{

    private final static Logger LOG = LoggerFactory.getLogger(BlackNectarRequest.class);

    public Location center;
    public double radiusInMeters;
    public int limit;
    public String searchTerm;

    public BlackNectarRequest()
    {
        this.searchTerm = "";
        this.center = null;
        this.radiusInMeters = 0;
        this.limit = 0;
    }

    public BlackNectarRequest(String searchTerm, Location center, double radiusInMeters, int limit)
    {
        this.searchTerm = searchTerm;
        this.center = center;
        this.radiusInMeters = radiusInMeters;
        this.limit = limit;
    }
    
    public boolean hasRadius()
    {
        return radiusInMeters > 0;
    }
    
    public boolean hasCenter()
    {
        return center != null;
    }
    
    public boolean hasLimit()
    {
        return limit > 0;
    }
    
    public boolean hasSearchTerm()
    {
        return !Strings.isNullOrEmpty(searchTerm);
    }
    
    public BlackNectarRequest withSearchTerm(String searchTerm)
    {
        checkThat(searchTerm)
            .usingMessage("searchTerm cannot be empty")
            .is(nonEmptyString());
        
        this.searchTerm = searchTerm;
        return this;
    }
    
    public BlackNectarRequest withCenter(@Required Location center)
    {
        checkThat(center)
            .usingMessage("center cannot be null")
            .is(notNull());
        
        this.center = center;
        return this;
    }
    
    public BlackNectarRequest withLimit(int limit)
    {
        checkThat(limit)
            .is(positiveInteger());
        
        this.limit = limit;
        return this;
    }
    
    public BlackNectarRequest withRadius(double radius)
    {
        checkThat(radius)
            .is(greaterThanOrEqualTo(0.0));
        
        this.radiusInMeters = radius;
        return this;
    }
    

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.center);
        hash = 17 * hash + (int) (Double.doubleToLongBits(this.radiusInMeters) ^ (Double.doubleToLongBits(this.radiusInMeters) >>> 32));
        hash = 17 * hash + this.limit;
        hash = 17 * hash + Objects.hashCode(this.searchTerm);
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
        final BlackNectarRequest other = (BlackNectarRequest) obj;
        if (Double.doubleToLongBits(this.radiusInMeters) != Double.doubleToLongBits(other.radiusInMeters))
        {
            return false;
        }
        if (this.limit != other.limit)
        {
            return false;
        }
        if (!Objects.equals(this.searchTerm, other.searchTerm))
        {
            return false;
        }
        if (!Objects.equals(this.center, other.center))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "BlackNectarRequest{" + "center=" + center + ", radiusInMeters=" + radiusInMeters + ", limit=" + limit + ", searchTerm=" + searchTerm + '}';
    }

}
