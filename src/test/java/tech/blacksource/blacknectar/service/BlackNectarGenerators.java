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

 
package tech.blacksource.blacknectar.service;


import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.blacksource.blacknectar.service.images.Image;
import tech.blacksource.blacknectar.service.stores.Address;
import tech.blacksource.blacknectar.service.stores.Location;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.generator.AlchemyGenerator;
import tech.sirwellington.alchemy.generator.StringGenerators;

import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NetworkGenerators.httpUrls;
import static tech.sirwellington.alchemy.generator.NumberGenerators.doubles;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.generator.NumberGenerators.positiveIntegers;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphanumericString;
import static tech.sirwellington.alchemy.generator.StringGenerators.hexadecimalString;
import static tech.sirwellington.alchemy.generator.StringGenerators.numericString;
import static tech.sirwellington.alchemy.generator.StringGenerators.uuids;

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
        AlchemyGenerator<String> zipCodes = StringGenerators.numericString(5);
        AlchemyGenerator<String> localZipCodes = StringGenerators.numericString(4);
        
        return () ->
        {
            return Address.Builder.newBuilder()
                .withAddressLineOne(one(alphanumericString()))
                .withAddressLineTwo(one(alphanumericString()))
                .withZipCode(zipCodes.get())
                .withLocalZipCode(one(localZipCodes))
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
                .withStoreID(UUID.fromString(one(uuids)))
                .withAddress(addresses().get())
                .withLocation(locations().get())
                .withName(one(alphabeticString()))
                .withStoreCode(one(numericString(4)))
                .withMainImageURL(one(httpUrls()).toString())
                .build(); 
        };
    }

    public static AlchemyGenerator<Image> images()
    {
        return () ->
        {
            return Image.Builder.newInstance()
                .withStoreID(UUID.randomUUID())
                .withImageID(one(hexadecimalString(20)))
                .withSizeInBytes(one(integers(100, 1_000)))
                .withSource(one(alphabeticString()))
                .withURL(one(httpUrls()))
                .withWidthAndHeight(one(positiveIntegers()), one(positiveIntegers()))
                .withContentType(one(alphabeticString()))
                .withImageType(one(alphabeticString()))
                .build();
        };
    }
}
