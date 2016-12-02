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

package tech.blacksource.blacknectar.service.stores;

import com.google.gson.JsonObject;
import java.util.Objects;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.concurrency.Immutable;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;
import tech.sirwellington.alchemy.annotations.objects.Pojo;

import static com.google.common.base.Strings.isNullOrEmpty;
import static tech.blacksource.blacknectar.service.stores.Address.validAddress;
import static tech.blacksource.blacknectar.service.stores.Location.validLocation;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NetworkAssertions.validURL;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
@Pojo
@Immutable
@ThreadSafe
@BuilderPattern(role = PRODUCT)
public class Store implements JSONRepresentable
{

    private String name;
    private Location location;
    private Address address;
    private String mainImageURL;

    private JsonObject json;

    Store()
    {
    }

    public Store(String name, Location location, Address address, String mainImageURL)
    {
        checkThat(address).is(validAddress());
        checkThat(name).usingMessage("name is missing").is(nonEmptyString());
        checkThat(location).is(validLocation());
        
        if (!isNullOrEmpty(mainImageURL))
        {
            checkThat(mainImageURL).is(validURL());
        }

        this.name = name;
        this.location = location;
        this.address = address;
        this.mainImageURL = mainImageURL;
        this.json = createJSON();
    }
    
    public boolean hasMainImage()
    {
        return !isNullOrEmpty(mainImageURL);
    }

    public String getName()
    {
        return name;
    }

    public Location getLocation()
    {
        return location;
    }

    public Address getAddress()
    {
        return address;
    }

    @Override
    public JsonObject asJSON()
    {
        return createJSON();
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(this.name);
        hash = 83 * hash + Objects.hashCode(this.location);
        hash = 83 * hash + Objects.hashCode(this.address);
        hash = 83 * hash + Objects.hashCode(this.mainImageURL);
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
        final Store other = (Store) obj;
        if (!Objects.equals(this.name, other.name))
        {
            return false;
        }
        if (!Objects.equals(this.mainImageURL, other.mainImageURL))
        {
            return false;
        }
        if (!Objects.equals(this.location, other.location))
        {
            return false;
        }
        if (!Objects.equals(this.address, other.address))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Store{" + "name=" + name + ", location=" + location + ", address=" + address + ", mainImageURL=" + mainImageURL + '}';
    }

    private JsonObject createJSON()
    {
        JsonObject jsonObject = new JsonObject();
        
        jsonObject.addProperty(Keys.NAME, this.name);
        jsonObject.add(Keys.LOCATION, location.asJSON());
        jsonObject.add(Keys.ADDRESS, this.address.asJSON());
        
        if (hasMainImage())
        {
            jsonObject.addProperty(Keys.MAIN_IMAGE, this.mainImageURL);
        }

        return jsonObject;
    }

    /**
     * The Keys used when writing the object as JSON.
     */
    static class Keys
    {
        static final String NAME = "store_name";
        static final String LOCATION = "location";
        static final String ADDRESS = "address";
        static final String MAIN_IMAGE = "main_image_url";
    }
    
    public static final Store SAMPLE_STORE = createSampleStore();
    
    private static Store createSampleStore()
    {
        String name = "COOPERS FOODS";
        Location location = new Location(44.790585, -93.600769);
        Address address = new Address("710 N Walnut St",
                                      null,
                                      "Chaska",
                                      "MN",
                                      "CARVER",
                                      55318,
                                      2079);
        String sampleImage = "https://s3-media3.fl.yelpcdn.com/bphoto/hzF7KhWb1B6cdGJ1y9E05A/o.jpg";
        
        return new Store(name, location, address, sampleImage);
    }
    
    /**
     * Facilitates construction of {@link Store} objects.
     */
    @BuilderPattern(role = BUILDER)
    public final static class Builder
    {

        public static Builder newInstance()
        {
            return new Builder();
        }
        
        public static Builder fromStore(@Required Store store) throws IllegalArgumentException
        {
            checkThat(store).is(notNull());
            
            Builder builder = new Builder();
            builder.name = store.name;
            builder.location = store.location;
            builder.address = store.address;
            builder.mainImageURL = store.mainImageURL;
            
            return builder;
        }

        private String name;
        private Location location;
        private Address address;
        private String mainImageURL;

        Builder()
        {

        }
        
        public Builder withName(@NonEmpty String name) throws IllegalArgumentException
        {
            checkThat(name)
                .usingMessage("name cannot be empty")
                .is(nonEmptyString());

            this.name = name;
            return this;
        }

        public Builder withAddress(@Required Address address) throws IllegalArgumentException
        {
            checkThat(address)
                .is(validAddress());

            this.address = address;
            return this;
        }

        public Builder withLocation(@Required Location location) throws IllegalArgumentException
        {
            checkThat(location)
                .is(validLocation());

            this.location = location;
            return this;
        }
        
        public Builder withMainImageURL(@Required String imageURL) throws IllegalArgumentException
        {
            checkThat(imageURL)
                .is(nonEmptyString())
                .is(validURL());
            
            this.mainImageURL = imageURL;
            return this;
        }

        public Store build() throws IllegalStateException
        {
            checkThat(location)
                .usingMessage("Location is missing or invalid")
                .is(validLocation());

            checkThat(name)
                .usingMessage("Missing name")
                .is(nonEmptyString());

            checkThat(address)
                .usingMessage("Address is missing or invalid")
                .is(validAddress());
            
            return new Store(this.name, this.location, this.address, this.mainImageURL);
        }
    }
}
