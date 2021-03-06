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
import tech.blacksource.blacknectar.service.stores.Address.Keys;
import tech.sirwellington.alchemy.arguments.AlchemyAssertion;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.addresses;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;

/**
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class AddressTest
{

    private Address instance;

    @Before
    public void setUp() throws Exception
    {
        setupData();
        setupMocks();
    }


    private void setupData() throws Exception
    {
        instance = one(addresses());
    }

    private void setupMocks() throws Exception
    {

    }

    @Test
    public void testValidAddress()
    {
        AlchemyAssertion<Address> assertion = Address.validAddress();
        assertThat(assertion, notNullValue());

        assertion.check(instance);
    }

    @Test
    public void testAsJSON()
    {
        JsonObject json = instance.asJSON();
        assertThat(json, notNullValue());

        String addressOne = json.get(Keys.ADDRESS_LINE_ONE).getAsString();
        assertThat(addressOne, is(instance.getAddressLineOne()));

        String addressTwo = json.get(Keys.ADDRESS_LINE_TWO).getAsString();
        assertThat(addressTwo, is(instance.getAddressLineTwo()));

        String city = json.get(Keys.CITY).getAsString();
        assertThat(city, is(instance.getCity()));

        String county = json.get(Keys.COUNTY).getAsString();
        assertThat(county, is(instance.getCounty()));

        String zipCode = json.get(Keys.ZIP).getAsString();
        assertThat(zipCode, is(instance.getZipCode()));

        String localZip = json.get(Keys.LOCAL_ZIP).getAsString();
        assertThat(localZip, is(instance.getLocalZipCode()));
    }

}