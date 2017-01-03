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
import com.google.gson.annotations.SerializedName;
import java.util.Objects;
import java.util.UUID;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.annotations.concurrency.Immutable;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;
import tech.sirwellington.alchemy.annotations.objects.Pojo;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;

import static com.google.common.base.Strings.isNullOrEmpty;
import static tech.blacksource.blacknectar.service.stores.Address.validAddress;
import static tech.blacksource.blacknectar.service.stores.Location.validLocation;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NetworkAssertions.validURL;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.validUUID;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;

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

    @SerializedName(Keys.STORE_ID)
    private final String storeId;

    @SerializedName(Keys.NAME)
    private final String name;

    @SerializedName(Keys.LOCATION)
    private final Location location;

    @SerializedName(Keys.ADDRESS)
    private final Address address;

    @SerializedName(Keys.MAIN_IMAGE)
    private final String mainImageURL;

    private final JsonObject json;


    public Store(String storeId, String name, Location location, Address address, String mainImageURL)
    {
        checkThat(storeId).usingMessage("storeId must be a valid UUID").is(validUUID());
        checkThat(address).is(validAddress());
        checkThat(name).usingMessage("name is missing").is(nonEmptyString());
        checkThat(location).is(validLocation());

        if (!isNullOrEmpty(mainImageURL))
        {
            checkThat(mainImageURL).is(validURL());
        }

        this.storeId = storeId;
        this.name = name;
        this.location = location;
        this.address = address;
        this.mainImageURL = mainImageURL;
        this.json = createJSON();
    }

    /**
     * @return An assertion that checks whether a store is valid or not.
     */
    public static AlchemyAssertion<Store> validStore()
    {
        return store ->
        {
            checkThat(store)
                .usingMessage("store cannot be null")
                .is(notNull());

            checkThat(store.storeId)
                .usingMessage("storeId must be a valid UUID")
                .is(validUUID());

            checkThat(store.address)
                .usingMessage("store is missing address")
                .is(notNull())
                .is(validAddress());

            checkThat(store.location)
                .usingMessage("store is missing location")
                .is(notNull())
                .is(validLocation());

            if (!isNullOrEmpty(store.mainImageURL))
            {
                checkThat(store.mainImageURL)
                    .usingMessage("store image must be a valid URL")
                    .is(validURL());
            }
        };
    }

    public boolean hasMainImage()
    {
        return !isNullOrEmpty(mainImageURL);
    }

    public String getStoreId()
    {
        return storeId;
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

    public String getMainImageURL()
    {
        return mainImageURL;
    }

    @Override
    public JsonObject asJSON()
    {
        return json;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.storeId);
        hash = 53 * hash + Objects.hashCode(this.name);
        hash = 53 * hash + Objects.hashCode(this.location);
        hash = 53 * hash + Objects.hashCode(this.address);
        hash = 53 * hash + Objects.hashCode(this.mainImageURL);
        hash = 53 * hash + Objects.hashCode(this.json);
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
        if (!Objects.equals(this.storeId, other.storeId))
        {
            return false;
        }
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
        if (!Objects.equals(this.json, other.json))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Store{" + "storeId=" + storeId + ", name=" + name + ", location=" + location + ", address=" + address + ", mainImageURL=" + mainImageURL + ", json=" + json + '}';
    }

    private JsonObject createJSON()
    {
        JsonObject jsonObject = new JsonObject();
        
        jsonObject.addProperty(Keys.STORE_ID, this.storeId);
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

        static final String STORE_ID = "store_id";
        static final String NAME = "store_name";
        static final String LOCATION = "location";
        static final String ADDRESS = "address";
        static final String MAIN_IMAGE = "main_image_url";
    }

    public static final Store SAMPLE_STORE = createSampleStore();

    private static Store createSampleStore()
    {
        String storeId = one(uuids());
        String name = "COOPERS FOODS";
        Location location = new Location(44.790585, -93.600769);
        Address address = new Address("710 N Walnut St",
                                      null,
                                      "Chaska",
                                      "MN",
                                      "CARVER",
                                      "55318",
                                      "2079");
        
        String sampleImage = "https://s3-media3.fl.yelpcdn.com/bphoto/hzF7KhWb1B6cdGJ1y9E05A/o.jpg";

        return new Store(storeId, name, location, address, sampleImage);
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
            builder.storeId = store.storeId;
            builder.name = store.name;
            builder.location = store.location;
            builder.address = store.address;
            builder.mainImageURL = store.mainImageURL;

            return builder;
        }

        private String storeId;
        private String name;
        private Location location;
        private Address address;
        private String mainImageURL;

        Builder()
        {

        }
        
        /**
         * Sets the Store ID.
         * @param storeId Must be a valid {@link UUID} String.
         * @return
         * @throws IllegalArgumentException 
         */
        public Builder withStoreID(@NonEmpty UUID storeId) throws IllegalArgumentException
        {
            checkThat(storeId)
                .is(notNull());
            
            this.storeId = storeId.toString();
            return this;
        }

        /**
         * Sets the name of the store.
         * @param name
         * @return
         * @throws IllegalArgumentException 
         */
        public Builder withName(@NonEmpty String name) throws IllegalArgumentException
        {
            checkThat(name)
                .usingMessage("name cannot be empty")
                .is(nonEmptyString());

            this.name = name;
            return this;
        }

        /**
         * Sets the address of the store. This parameter is required.
         * 
         * @param address
         * @return
         * @throws IllegalArgumentException 
         */
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
            
            checkThat(storeId)
                .usingMessage("storeId is required")
                .is(validUUID());
            
            checkThat(location)
                .usingMessage("Location is missing or invalid")
                .is(validLocation());

            checkThat(name)
                .usingMessage("Missing name")
                .is(nonEmptyString());

            checkThat(address)
                .usingMessage("Address is missing or invalid")
                .is(validAddress());

            return new Store(this.storeId, this.name, this.location, this.address, this.mainImageURL);
        }
    }

}
