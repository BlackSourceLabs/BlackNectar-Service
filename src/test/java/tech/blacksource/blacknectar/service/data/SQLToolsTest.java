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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.StringGenerators.alphabeticString;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateInteger.Type.RANGE;


/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class SQLToolsTest 
{
    
    @Mock
    private ResultSet results;
    
    @Mock
    private ResultSetMetaData metaData;
    
    private SQLTools instance;
    
    @GenerateInteger(value = RANGE, min = 1, max = 20)
    private int numberOfColumns;
    
    @GenerateString
    private String columnName;

    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        
        instance = new SQLTools.Impl();
    }


    private void setupData() throws Exception
    {
        
    }

    private void setupMocks() throws Exception
    {
        when(results.getMetaData()).thenReturn(metaData);
        when(metaData.getColumnCount()).thenReturn(numberOfColumns);
        
        when(metaData.getColumnLabel(anyInt())).thenReturn(one(alphabeticString()));
    }

    @Test
    public void testHasColumnWhenHasColumn() throws Exception
    {
        when(metaData.getColumnLabel(1)).thenReturn(columnName);
        
        boolean result = instance.hasColumn(results, columnName);
        assertTrue(result);
    }
    
    @Test
    public void testHasColumnWhenNotPresent() throws Exception
    {
        boolean result = instance.hasColumn(results, columnName);
        assertFalse(result);
    }
    
    @DontRepeat
    @Test
    public void testHasColumnWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.hasColumn(null, columnName)).isInstanceOf(IllegalArgumentException.class);
        assertThrows(() -> instance.hasColumn(results, "")).isInstanceOf(IllegalArgumentException.class);
    }

}