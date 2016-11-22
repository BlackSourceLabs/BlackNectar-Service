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


import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.blackhole.blacknectar.service.exceptions.OperationFailedException;
import tech.blackhole.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.BooleanAssertions.falseStatement;
import static tech.sirwellington.alchemy.arguments.assertions.NumberAssertions.greaterThanOrEqualTo;

/**
 *
 * @author SirWellington
 */
final class SQLBlackNectarService implements BlackNectarService
{
    private final static Logger LOG = LoggerFactory.getLogger(SQLBlackNectarService.class);
    
    private final Connection connection;

    SQLBlackNectarService(@Required Connection connection) throws SQLException
    {
        checkThat(connection).is(notNull());
        
        boolean isClosed = connection.isClosed();
        checkThat(isClosed)
            .usingMessage("connection is closed")
            .throwing(SQLException.class)
            .is(falseStatement());
        
        this.connection = connection;
    }

    @Override
    public List<Store> getAllStores(int limit)
    {
        checkThat(limit)
            .usingMessage("limit must be >= 0")
            .is(greaterThanOrEqualTo(0));
        
        return Lists.emptyList();
    }

    @Override
    public List<Store> searchForStores(BlackNectarSearchRequest request) throws OperationFailedException
    {
        checkThat(request)
            .usingMessage("request missing")
            .is(notNull());
        
        return Lists.emptyList();
    }

}
