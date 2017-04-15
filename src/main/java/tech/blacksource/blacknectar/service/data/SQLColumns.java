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

package tech.blacksource.blacknectar.service.data;

import tech.sirwellington.alchemy.annotations.access.NonInstantiable;

/**
 * @author SirWellington
 */
@NonInstantiable
public final class SQLColumns
{

    SQLColumns() throws IllegalAccessException
    {
        throw new IllegalAccessException("cannot instantiate");
    }

    static final String STORE_ID = "store_id";
    static final String STORE_NAME = "store_name";
    static final String STORE_CODE = "store_code";
    static final String LATITUDE = "latitude";
    static final String LONGITUDE = "longitude";
    static final String ADDRESS_LINE_ONE = "address_line_one";
    static final String ADDRESS_LINE_TWO = "address_line_two";
    static final String CITY = "city";
    static final String STATE = "state";
    static final String COUNTY = "county";
    static final String ZIP_CODE = "zip_code";
    static final String LOCAL_ZIP_CODE = "local_zip_code";
    static final String IS_FARMERS_MARKET = "is_farmers_market";


    public static class Images
    {
        public static final String IMAGE_ID = "image_id";
        public static final String STORE_ID = "store_Id";
        public static final String IMAGE_BINARY = "image_binary";
        public static final String HEIGHT = "height";
        public static final String WIDTH = "width";
        public static final String SIZE_IN_BYTES = "size_in_bytes";
        public static final String CONTENT_TYPE = "content_type";
        public static final String IMAGE_TYPE = "image_type";
        public static final String SOURCE = "source";
        public static final String URL = "url";
    }
}
