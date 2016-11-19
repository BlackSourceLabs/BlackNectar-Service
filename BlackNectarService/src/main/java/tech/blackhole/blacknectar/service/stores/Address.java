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

import java.util.Objects;
import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;
import tech.sirwellington.alchemy.annotations.arguments.Optional;
import tech.sirwellington.alchemy.annotations.concurrency.Immutable;
import tech.sirwellington.alchemy.annotations.concurrency.ThreadSafe;
import tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern;
import tech.sirwellington.alchemy.annotations.objects.Pojo;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;

import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.BUILDER;
import static tech.sirwellington.alchemy.annotations.designs.patterns.BuilderPattern.Role.PRODUCT;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThan;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.lessThan;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * 
 * @author SirWellington
 */
@Pojo
@Immutable
@ThreadSafe
@BuilderPattern(role = PRODUCT)
final class Address
{

    private final String addressLineOne;
    private final String addressLineTwo;
    private final String city;
    private final String state;
    private final String country;
    private final int zip5;
    private final int zip4;
    
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
            
            checkThat(a.country)
                .usingMessage("Country is missing")
                .is(nonEmptyString());
            
            checkThat(a.zip5)
                .usingMessage("Zip Code 5 is missing")
                .is(greaterThan(0))
                .usingMessage("Zip Code 5 must be < 100,000")
                .is(lessThan(100_000));
        };
    }

    Address(String addressLineOne, String addressLineTwo, String city, String state, String country, int zip5, int zip4)
    {
        this.addressLineOne = addressLineOne;
        this.addressLineTwo = addressLineTwo;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zip5 = zip5;
        this.zip4 = zip4;
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

    public String getCountry()
    {
        return country;
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
    public int hashCode()
    {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.addressLineOne);
        hash = 41 * hash + Objects.hashCode(this.addressLineTwo);
        hash = 41 * hash + Objects.hashCode(this.city);
        hash = 41 * hash + Objects.hashCode(this.state);
        hash = 41 * hash + Objects.hashCode(this.country);
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
        if (!Objects.equals(this.country, other.country))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Address{" + "addressLineOne=" + addressLineOne + ", addressLineTwo=" + addressLineTwo + ", city=" + city + ", state=" + state + ", country=" + country + ", zip5=" + zip5 + ", zip4=" + zip4 + '}';
    }

    @BuilderPattern(role = BUILDER)
    static class Builder
    {

        private String addressLineOne;
        @Optional
        private String addressLineTwo;
        private String city;
        private String state;
        private String county;
        private int zipCode5;
        @Optional
        private int zipCode4;

        public static Builder newBuilder()
        {
            return new Builder();
        }

        Builder()
        {
        }

        Builder withAddressLineOne(@NonEmpty String addressLine) throws IllegalArgumentException
        {
            checkThat(addressLine)
                .usingMessage("Address Line 1 cannot be empty")
                .is(nonEmptyString());

            this.addressLineOne = addressLine;
            return this;
        }

        Builder withAddressLineTwo(@NonEmpty String addressLine) throws IllegalArgumentException
        {
            checkThat(addressLine)
                .usingMessage("Address Line 2 should not be empty")
                .is(nonEmptyString());

            this.addressLineTwo = addressLine;
            return this;
        }

        Builder withCity(@NonEmpty String city) throws IllegalArgumentException
        {
            checkThat(city)
                .usingMessage("City cannot be empty")
                .are(nonEmptyString());

            this.city = city;
            return this;
        }

        Builder withState(@NonEmpty String state) throws IllegalArgumentException
        {
            checkThat(state)
                .usingMessage("State cannot be empty")
                .are(nonEmptyString());

            this.state = state;
            return this;
        }

        Builder withCounty(@NonEmpty String county) throws IllegalArgumentException
        {
            checkThat(county)
                .usingMessage("County cannot be empty")
                .are(nonEmptyString());

            this.county = county;
            return this;
        }

        Address build() throws IllegalStateException
        {
            checkThat(addressLineOne, city, state, county)
                .throwing(IllegalStateException.class)
                .usingMessage("Required information missing")
                .are(notNull())
                .are(nonEmptyString());

            checkThat(zipCode5)
                .throwing(IllegalStateException.class)
                .usingMessage("Zip Code must be > 0")
                .are(greaterThan(0))
                .usingMessage("Zip5 must be less than 100,000")
                .is(lessThan(100_000));

            Address address = new Address(this.addressLineOne,
                               this.addressLineTwo,
                               this.city,
                               this.state,
                               this.county,
                               this.zipCode5,
                               this.zipCode4);

            checkThat(address).is(validAddress());
            
            return address;
        }
    }

}
