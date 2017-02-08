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

import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.client.Aroma;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Answers.RETURNS_MOCKS;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class ModuleProductionDatabaseTest 
{

    @Mock(answer = RETURNS_MOCKS)
    private Aroma fakeAroma;
    
    private ModuleDatabaseProduction instance;
    
    @Before
    public void setUp() throws Exception
    {
        
        setupData();
        setupMocks();
        instance = new ModuleDatabaseProduction();
    }


    private void setupData() throws Exception
    {
        
    }

    private void setupMocks() throws Exception
    {
        
    }

    @Test
    public void testConfigure()
    {
        instance.configure();
    }
    
    @Test
    public void testProvideSQLConnection() throws Exception
    {
        DataSource connection = instance.provideSQLConnection(fakeAroma);
        assertThat(connection, notNullValue());
    }

}