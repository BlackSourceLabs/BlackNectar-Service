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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.aroma.client.Aroma;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author SirWellington
 */
@IntegrationTest
@RunWith(AlchemyTestRunner.class)
public class ModuleServerIT
{

    private ModuleServer instance;

    private ModuleDatabaseTesting databaseModule;

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
        databaseModule = new ModuleDatabaseTesting();

    }

    @Test
    public void testBindings()
    {
        Injector injector = Guice.createInjector(databaseModule, instance);

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

}
