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

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.client.Aroma;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Answers.RETURNS_MOCKS;

/**
 *
 * @author SirWellington
 */
@IntegrationTest
@RunWith(AlchemyTestRunner.class)
public class ModuleServerIT
{

    @Mock(answer = RETURNS_MOCKS)
    private Aroma fakeAroma;
    
    private ModuleServer instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();
        instance = new ModuleServer();
    }

    private void setupData() throws Exception
    {

    }

    private void setupMocks() throws Exception
    {

    }

    @Test
    public void testBindings()
    {
        Injector injector = Guice.createInjector(instance);

        Server server = injector.getInstance(Server.class);
        assertThat(server, notNullValue());
    }

    @Test
    public void testProvideAromaClient()
    {
        Aroma aroma = instance.provideAromaClient();
        assertThat(aroma, notNullValue());
    }

    @Test
    public void testConfigure()
    {
    }

    @Test
    public void testProvideSQLConnection() throws Exception
    {
        Aroma aroma = instance.provideAromaClient();
        DataSource connection = instance.provideSQLConnection(aroma);
        assertThat(connection, notNullValue());
    }
    
    @Test
    public void testSQLConnectionRefreshesOnClosure() throws Exception
    {
        DataSource dataSource = instance.provideSQLConnection(fakeAroma);
        
        Connection connection = dataSource.getConnection();
        connection.close();
        assertTrue(connection.isClosed());
        
        connection = dataSource.getConnection();
        assertFalse(connection.isClosed());
        
    }

}
