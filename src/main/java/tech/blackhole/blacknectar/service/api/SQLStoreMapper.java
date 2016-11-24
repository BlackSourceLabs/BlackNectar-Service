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

import com.google.inject.ImplementedBy;
import java.sql.ResultSet;
import java.sql.SQLException;
import tech.blackhole.blacknectar.service.stores.Address;
import tech.blackhole.blacknectar.service.stores.Location;
import tech.blackhole.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static com.google.common.base.Strings.isNullOrEmpty;

/**
 * This interface is a {@link FunctionalInterface} responsible for extracting a 
 * {@link Store} from a JDBC {@linkplain ResultSet Row}.
 * 
 * @author SirWellington
 */
@FunctionalInterface
@ImplementedBy(SQLStoreMapper.Impl.class)
interface SQLStoreMapper
{
    /**
     * Takes a JDBC {@link ResultSet} and converts it into a {@link Store}.
     *  
     * @param results The SQL Row to read.
     * 
     * @return A {@link Store} representation of the data, or null if it could not be extracted proplerly.
     * 
     * @throws SQLException 
     */
    Store mapToStore(@Required ResultSet results) throws SQLException;

    static SQLStoreMapper INSTANCE = new Impl();
    
    static class Impl implements SQLStoreMapper
    {

        @Override
        public Store mapToStore(ResultSet results) throws SQLException
        {
            //Pull data from the ResultSet
            String name = results.getString(SQLKeys.STORE_NAME);
            Double latitude = results.getDouble(SQLKeys.LATITUDE);
            if (results.wasNull())
            {
                latitude = null;
            }

            Double longitude = results.getDouble(SQLKeys.LONGITUDE);
            if (results.wasNull())
            {
                longitude = null;
            }

            String address = results.getString(SQLKeys.ADDRESS);
            String addressTwo = results.getString(SQLKeys.ADDRESS_LINE_TWO);
            String city = results.getString(SQLKeys.CITY);
            String state = results.getString(SQLKeys.STATE);
            String county = results.getString(SQLKeys.COUNTY);
            Integer zipCode = results.getInt(SQLKeys.ZIP_CODE);
            Integer localZip = results.getInt(SQLKeys.LOCAL_ZIP_CODE);

            if (results.wasNull())
            {
                localZip = null;
            }

            //Use the data to start creating a Store object, piece by piece
            Address.Builder addressBuilder = Address.Builder.newBuilder()
                .withAddressLineOne(address)
                .withCity(city)
                .withState(state)
                .withZipCode(zipCode);

            if (!isNullOrEmpty(county))
            {
                addressBuilder.withCounty(county);
            }

            if (!isNullOrEmpty(addressTwo))
            {
                addressBuilder.withAddressLineTwo(addressTwo);
            }

            if (localZip != null && localZip > 0)
            {
                addressBuilder.withLocalZipCode(localZip);
            }

            Store.Builder storeBuilder = Store.Builder.newInstance()
                .withAddress(addressBuilder.build())
                .withName(name);

            if (latitude != null && longitude != null)
            {
                Location location = Location.with(latitude, longitude);
                storeBuilder.withLocation(location);
            }

            return storeBuilder.build();
        }

    }

}
