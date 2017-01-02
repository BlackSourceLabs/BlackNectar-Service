/*
 * Copyright 2016 BlackSourceLabs.
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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class SQLQueriesTest 
{

    @Before
    public void setUp() throws Exception
    {
    }
    
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new SQLQueries())
            .isInstanceOf(IllegalAccessException.class);
    }
    
    @Test
    public void testQueries() throws Exception
    {
        assertThat(SQLQueries.CREATE_ADDRESS_TABLE, not(isEmptyOrNullString()));
        assertThat(SQLQueries.CREATE_STORES_TABLE, not(isEmptyOrNullString()));
        assertThat(SQLQueries.INSERT_STORE, not(isEmptyOrNullString()));
        assertThat(SQLQueries.QUERY_STORES_WITH_LOCATION, not(isEmptyOrNullString()));
        assertThat(SQLQueries.QUERY_STORES_WITH_NAME, not(isEmptyOrNullString()));
        assertThat(SQLQueries.QUERY_STORES_WITH_NAME_AND_LOCATION, not(isEmptyOrNullString()));
    }


}