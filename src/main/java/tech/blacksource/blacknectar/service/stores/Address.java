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
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Optional;
import tech.sirwellington.alchemy.annotations.concurrency.Immutable;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;
import tech.sirwellington.alchemy.annotations.objects.Pojo;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;

import static com.google.common.base.Strings.isNullOrEmpty;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.AddressAssertions.validZipCode;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThan;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.lessThan;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.lessThanOrEqualTo;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.positiveInteger;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * 
 * @author SirWellington
 */
@Pojo
@Immutable
@ThreadSafe
@BuilderPattern(role = PRODUCT)
public final class Address implements JSONRepresentable
{

    private String addressLineOne;
    private String addressLineTwo;
    private String city;
    private String state;
    private String county;
    private int zip5;
    private int zip4;
    private JsonObject json;
    
    static AlchemyAssertion<Address> validAddress()
    {
        return a ->
        {
            checkThat(a)
                .usingMessage("Address was null")
                .is(notNull());
            
            checkThat(a.addressLineOne)
                .usingMessage("Address Line 1 cannot be empty")
                .is(nonEmptyString());
            
            checkThat(a.city)
                .usingMessage("City is missing")
                .is(nonEmptyString());
            
            checkThat(a.state)
                .usingMessage("State is missing")
                .is(nonEmptyString());
            
            checkThat(a.county)
                .usingMessage("Country is missing")
                .is(nonEmptyString());
            
            checkThat(a.zip5)
                .usingMessage("Zip Code 5 is missing")
                .is(greaterThan(0))
                .usingMessage("Zip Code 5 must be < 100,000")
                .is(lessThan(100_000));
        };
    }

    Address()
    {
    }
    
    Address(String addressLineOne, 
            String addressLineTwo,
            String city,
            String state,
            String county,
            int zip5,
            int zip4)
    {
        this.addressLineOne = addressLineOne;
        this.addressLineTwo = addressLineTwo;
        this.city = city;
        this.state = state;
        this.county = county;
        this.zip5 = zip5;
        this.zip4 = zip4;
        
        this.json = createJSON();
    }

    public String getAddressLineOne()
    {
        return addressLineOne;
    }

    public String getAddressLineTwo()
    {
        return addressLineTwo;
    }

    public String getCity()
    {
        return city;
    }

    public String getState()
    {
        return state;
    }

    public String getCounty()
    {
        return county;
    }

    public int getZip5()
    {
        return zip5;
    }

    public int getZip4()
    {
        return zip4;
    }

    @Override
    public JsonObject asJSON()
    {
        return createJSON();
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.addressLineOne);
        hash = 41 * hash + Objects.hashCode(this.addressLineTwo);
        hash = 41 * hash + Objects.hashCode(this.city);
        hash = 41 * hash + Objects.hashCode(this.state);
        hash = 41 * hash + Objects.hashCode(this.county);
        hash = 41 * hash + this.zip5;
        hash = 41 * hash + this.zip4;
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
        final Address other = (Address) obj;
        if (this.zip5 != other.zip5)
        {
            return false;
        }
        if (this.zip4 != other.zip4)
        {
            return false;
        }
        if (!Objects.equals(this.addressLineOne, other.addressLineOne))
        {
            return false;
        }
        if (!Objects.equals(this.addressLineTwo, other.addressLineTwo))
        {
            return false;
        }
        if (!Objects.equals(this.city, other.city))
        {
            return false;
        }
        if (!Objects.equals(this.state, other.state))
        {
            return false;
        }
        if (!Objects.equals(this.county, other.county))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Address{" + "addressLineOne=" + addressLineOne + ", addressLineTwo=" + addressLineTwo + ", city=" + city + ", state=" + state + ", county=" + county + ", zip5=" + zip5 + ", zip4=" + zip4 + '}';
    }

    @Internal
    private JsonObject createJSON()
    {
        JsonObject jsonObject = new JsonObject();
        
        jsonObject.addProperty(Keys.ADDRESS_LINE_ONE, addressLineOne);
        
        if (!isNullOrEmpty(addressLineTwo))
        {
            jsonObject.addProperty(Keys.ADDRESS_LINE_TWO, addressLineTwo);
        }
        
        jsonObject.addProperty(Keys.CITY, city);
        jsonObject.addProperty(Keys.STATE, state);
        jsonObject.addProperty(Keys.COUNTY, county);
        jsonObject.addProperty(Keys.ZIP, zipToString(zip5));
        jsonObject.addProperty(Keys.LOCAL_ZIP, localZipToString(zip4));
        
        return jsonObject;
    }
    
    private String zipToString(int zip)
    {
        return String.format("%05d", zip);
    }
    
    private String localZipToString(int localZip)
    {
        return String.format("%04d", localZip);
    }

    @BuilderPattern(role = BUILDER)
    public final static class Builder
    {

        private String addressLineOne;
        @Optional
        private String addressLineTwo;
        private String city;
        private String state;
        private String county;
        private int zipCode;
        @Optional
        private int localZip;

        public static Builder newBuilder()
        {
            return new Builder();
        }

        Builder()
        {
        }

        public Builder withAddressLineOne(@NonEmpty String addressLine) throws IllegalArgumentException
        {
            checkThat(addressLine)
                .usingMessage("Address Line 1 cannot be empty")
                .is(nonEmptyString());

            this.addressLineOne = addressLine;
            return this;
        }

        @Optional
        public Builder withAddressLineTwo(@NonEmpty String addressLine) throws IllegalArgumentException
        {
            checkThat(addressLine)
                .usingMessage("Address Line 2 should not be empty")
                .is(nonEmptyString());

            this.addressLineTwo = addressLine;
            return this;
        }

        public Builder withCity(@NonEmpty String city) throws IllegalArgumentException
        {
            checkThat(city)
                .usingMessage("City cannot be empty")
                .are(nonEmptyString());

            this.city = city;
            return this;
        }

        public Builder withState(@NonEmpty String state) throws IllegalArgumentException
        {
            checkThat(state)
                .usingMessage("State cannot be empty")
                .are(nonEmptyString());

            this.state = state;
            return this;
        }

        @Optional
        public Builder withCounty(@NonEmpty String county) throws IllegalArgumentException
        {
            checkThat(county)
                .usingMessage("County cannot be empty")
                .are(nonEmptyString());

            this.county = county;
            return this;
        }
        
        public Builder withZipCode(int zipCode) throws IllegalArgumentException
        {
            checkThat(zipCode).is(validZipCode());
            
            this.zipCode = zipCode;
            return this;
        }
        
        @Optional
        public Builder withLocalZipCode(int localZipCode) throws IllegalArgumentException
        {
            checkThat(localZipCode)
                .usingMessage("Local Zip Code cannot be negative")
                .is(positiveInteger())
                .usingMessage("Local Zip Code cannot exceed 4 digits")
                .is(lessThanOrEqualTo(9_999));
            
            this.localZip = localZipCode;
            return this;
        }

        public Address build() throws IllegalStateException
        {
            checkThat(addressLineOne, city, state, county)
                .throwing(IllegalStateException.class)
                .usingMessage("Required information missing")
                .are(notNull())
                .are(nonEmptyString());

            checkThat(zipCode)
                .throwing(IllegalStateException.class)
                .is(validZipCode());

            Address address = new Address(this.addressLineOne,
                               this.addressLineTwo,
                               this.city,
                               this.state,
                               this.county,
                               this.zipCode,
                               this.localZip);

            checkThat(address).is(validAddress());

            return address;
        }

    }

    /**
     * Keys used when serializing an {@link Address} to JSON.
     */
    static class Keys
    {

        final static String ADDRESS_LINE_ONE = "address_line_1";
        final static String ADDRESS_LINE_TWO = "address_line_2";
        final static String CITY = "city";
        final static String STATE = "state";
        final static String COUNTY = "county";
        final static String ZIP = "zip_code";
        final static String LOCAL_ZIP = "local_zip_code";
    }
}
