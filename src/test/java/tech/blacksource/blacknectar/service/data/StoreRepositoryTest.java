/*
 * Copyright 2017 BlackSourceLabs.
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
import org.mockito.Mock;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.aroma.client.Aroma;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.Repeat;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 *
 * @author SirWellington
 */
@Repeat(25)
@RunWith(AlchemyTestRunner.class)
public class StoreRepositoryTest
{

    @Mock
    private Aroma aroma;
    
    @Mock
    private JdbcTemplate database;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();
    }

    private void setupData() throws Exception
    {

    }

    private void setupMocks() throws Exception
    {

    }

    @Test
    public void testNewMemoryService()
    {
        StoreRepository result = StoreRepository.newMemoryService();
        assertThat(result, notNullValue());
    }

    @Test
    public void testNewSQLServiceWithJdbcTemplate() throws Exception
    {
        StoreRepository result = StoreRepository.newSQLService(database);
        assertThat(result, notNullValue());
    }

    @Test
    public void testNewSQLServicWith3args() throws Exception
    {
        StoreRepository result = StoreRepository.newSQLService(aroma, database);
        assertThat(result, notNullValue());
    }

}
