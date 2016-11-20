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


import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Urgency;
import tech.blackhole.blacknectar.service.OperationFailedException;
import tech.blackhole.blacknectar.service.Server;
import tech.sirwellington.alchemy.annotations.access.Internal;

/**
 *
 * @author SirWellington
 */
@Internal
final class FileRepository implements StoreRepository 
{
    private final static Logger LOG = LoggerFactory.getLogger(FileRepository.class);
    private static final String FILENAME = "Stores.csv";

    private final int MAXIMUM_STORES = 10_000;
    
    @Override
    public List<Store> getAllStores()
    {
        String file = readCSVFile();
        List<String> lines = splitFileIntoLines(file);
        removeFirstLine(lines);
        
        if (lines.size() > MAXIMUM_STORES)
        {
            lines = lines.subList(0, MAXIMUM_STORES);
        }
        
        return lines.parallelStream()
            .map(this::toStore)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    String readCSVFile()
    {
        URL url;
        try
        {
            url = Resources.getResource(FILENAME);
        }
        catch (Exception ex)
        {
            LOG.error("Failed to load resource at {}", FILENAME, ex);
           
            Server.AROMA.begin()
                .titled("Operation Failed")
                .text("Could not load CSV file at {}", FILENAME, ex)
                .withUrgency(Urgency.HIGH)
                .send();
            
            return "";
        }
        try
        {
            return Resources.toString(url, Charsets.UTF_8);
        }
        catch (IOException ex)
        {
            LOG.error("Failed to load URL into a String: {}", url, ex);
           
            Server.AROMA.begin()
                .titled("Operation Failed")
                .text("Could not load URL into String: [{}]", url.toString(), ex)
                .withUrgency(Urgency.HIGH)
                .send();
            
            return "";
        }
    }

    List<String> splitFileIntoLines(String file)
    {
        String[] lines = file.split("\n");
        if (lines == null || lines.length == 0)
        {
            return Lists.emptyList();
        }
        
        return Lists.copy(Arrays.asList(lines));
    }

    Store toStore(String line)
    {
        String[] components = line.split(",");
        if (components == null || components.length == 0)
        {
            return null;
        }
        if (components.length != 10)
        {
            LOG.warn("Expected 10 values, but instead {}, in line {}", components.length, line);
            return null;
        }
        try
        {
            Store store = extractStoreFrom(components);
            return store;
        }
        catch (RuntimeException ex)
        {
            LOG.debug("Failed to extract Store from line: {}", line, ex);
            return null;
        }
    }

    Location extractLocationFrom(String latitudeString, String longitudeString)
    {
        try
        {
            return new Location(Double.valueOf(latitudeString), Double.valueOf(longitudeString));
        }
        catch (NumberFormatException ex)
        {
            LOG.error("Failed to convert Geo-Coordinate: [{},{}]", latitudeString, longitudeString, ex);
           
            Server.AROMA.begin()
                .titled("Conversion Failed")
                .text("Failed to convert to Geo-Point: [{}, {}]", latitudeString, longitudeString, ex)
                .withUrgency(Urgency.MEDIUM)
                .send();
            
            throw ex;
        }
    }

    Store extractStoreFrom(String[] components) throws OperationFailedException
    {
        String storeName = components[0].replaceAll("\"", "");
        String longitudeString = components[1];
        String latitudeString = components[2];
        String addressLineOne = components[3].replaceAll("\"", "");
        String addressLineTwo = components[4].replaceAll("\"", "");
        String city = components[5].replaceAll("\"", "");
        String state = components[6].replaceAll("\"", "");
        String zip5 = components[7].replaceAll("\"", "");
        String zip4 = components[8].replaceAll("\"", "");
        String county = components[9].replaceAll("\"", "").replaceAll("\r", "");
        
        Location location = extractLocationFrom(latitudeString, longitudeString);
        
        int zipCode = extractZipCode(zip5);
      
        Address.Builder addressBuilder = Address.Builder.newBuilder()
            .withAddressLineOne(addressLineOne)
            .withCity(city)
            .withState(state)
            .withZipCode(zipCode);

        if (!Strings.isNullOrEmpty(zip4))
        {
            int localZipCode = extractZipCode(zip4);
            addressBuilder = addressBuilder.withLocalZipCode(localZipCode);
        }

        if (!Strings.isNullOrEmpty(addressLineTwo))
        {
            addressBuilder = addressBuilder.withAddressLineTwo(addressLineTwo);
        }

        if (!Strings.isNullOrEmpty(county))
        {
            addressBuilder = addressBuilder.withCounty(county);
        }

        return Store.Builder.newInstance()
            .withAddress(addressBuilder.build())
            .withLocation(location)
            .withName(storeName)
            .build();
    }

    int extractZipCode(String zipCode)
    {
        try
        {
            return Integer.valueOf(zipCode);
        }
        catch (NumberFormatException ex)
        {
            LOG.error("Failed to parse Zip codes: {}", zipCode, ex);
           
            Server.AROMA.begin()
                .titled("Conversion Failed")
                .text("Could not parse Zip Code: {}", zipCode, ex)
                .withUrgency(Urgency.MEDIUM)
                .send();
            
            throw new OperationFailedException(ex);
        }
    }

    private void removeFirstLine(List<String> lines)
    {
        lines.remove(0);
    }

}
