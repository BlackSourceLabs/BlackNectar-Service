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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.client.Aroma;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@Repeat(50)
@RunWith(AlchemyTestRunner.class)
public class SQLBlackNectarServiceTest 
{
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private Statement statement;
    @Mock
    private ResultSet resultSet;
    @Mock
    private GeoCalculator geoCalculator;
    
    private Aroma aroma ;
    
    private BlackNectarSearchRequest request;
    private SQLBlackNectarService instance;
    
    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        
        instance = new SQLBlackNectarService(aroma, connection, geoCalculator);
    }


    private void setupData() throws Exception
    {
        
    }

    private void setupMocks() throws Exception
    {
        aroma = Aroma.create();
        when(connection.isClosed()).thenReturn(false);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(connection.createStatement()).thenReturn(statement);
        
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        
    }
    
    @DontRepeat
    @Test
    public void testConstructorWithBadArguments()
    {
        assertThrows(() -> new SQLBlackNectarService(null, connection, geoCalculator))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new SQLBlackNectarService(aroma, null, geoCalculator))
            .isInstanceOf(IllegalArgumentException.class);
        
        assertThrows(() -> new SQLBlackNectarService(aroma, connection, null))
            .isInstanceOf(IllegalArgumentException.class);
    }
    
    @DontRepeat
    @Test
    public void testConstructorWithClosedConnection() throws Exception
    {
        when(connection.isClosed()).thenReturn(true);
        
        assertThrows(() -> new SQLBlackNectarService(aroma, connection, geoCalculator)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testGetAllStores()
    {
    }

    @Test
    public void testSearchForStores()
    {
    }

    @Test
    public void testAddStore()
    {
    }

    @Test
    public void testPrepareStatementForStore() throws Exception
    {
    }

    @Test
    public void testGetStatementToCreateTable()
    {
    }

}