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

 
package tech.blacksource.blacknectar.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.blacksource.blacknectar.service.stores.Address;
import tech.blacksource.blacknectar.service.stores.Location;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;

import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.doubles;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphanumericString;

/**
 *
 * @author SirWellington
 */
public class BlackNectarGenerators 
{
    private final static Logger LOG = LoggerFactory.getLogger(BlackNectarGenerators.class);

    public static AlchemyGenerator<Location> locations()
    {
        AlchemyGenerator<Double> latitudes = doubles(-90, 90);
        AlchemyGenerator<Double> longitudes = doubles(-180, 180);
        
        return () ->
        {
            return new Location(latitudes.get(), longitudes.get());
        };
    }
    
    public static AlchemyGenerator<Address> addresses()
    {
        AlchemyGenerator<Integer> zipCodes = integers(1, 99_999);
        
        return () ->
        {
            return Address.Builder.newBuilder()
                .withZipCode(zipCodes.get())
                .withAddressLineOne(one(alphanumericString()))
                .withCity(one(alphabeticString(5)))
                .withState(one(alphabeticString(2)).toUpperCase())
                .withCounty(one(alphabeticString()))
                .build();
        };
    }
    
    public static AlchemyGenerator<Store> stores()
    {
        return () ->
        {
            return Store.Builder.newInstance()
                .withAddress(addresses().get())
                .withLocation(locations().get())
                .withName(one(alphabeticString()))
                .build(); 
        };
    }
}
