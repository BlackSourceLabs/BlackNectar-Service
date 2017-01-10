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

import com.google.common.base.Objects;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.sirwellington.alchemy.annotations.access.Internal;
import tech.sirwellington.alchemy.annotations.access.NonInstantiable;

/**
 *
 * @author SirWellington
 */
@Internal
@NonInstantiable
final class SQL
{

    private final static Logger LOG = LoggerFactory.getLogger(SQL.class);

    /**
     * Determines whether a {@link ResultSet} has a column present.
     * 
     * @param results
     * @param expectedColumn
     * @return
     * @throws SQLException 
     */
    static boolean hasColumn(ResultSet results, String expectedColumn) throws SQLException
    {
        ResultSetMetaData metadata = results.getMetaData();

        for (int i = 1; i <= metadata.getColumnCount(); ++i)
        {
            String columnName = metadata.getColumnLabel(i);

            if (Objects.equal(columnName, expectedColumn))
            {
                return true;
            }
        }

        return false;
    }
}
