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
import org.springframework.jdbc.core.JdbcTemplate;
import tech.aroma.client.Aroma;
import tech.redroma.google.places.GooglePlacesAPI;
import tech.redroma.yelp.YelpAPI;
import tech.sirwellington.alchemy.annotations.testing.IntegrationTest;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Answers.RETURNS_MOCKS;

/**
 *
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@IntegrationTest
public class ModuleServerTest
{

    @Mock(answer = RETURNS_MOCKS)
    private Aroma fakeAroma;

    @Mock
    private DataSource fakeDataSource;

    private ModuleServer instance;

    @Before
    public void setUp() throws Exception
    {
        instance = new ModuleServer();
    }

    @Test
    public void testProvideAromaClient()
    {
        Aroma aroma = instance.provideAromaClient();
        assertThat(aroma, notNullValue());
    }

    @Test
    public void testProvideJDBCTemplate()
    {
        JdbcTemplate jdbc = instance.provideJDBCTemplate(fakeDataSource);
        assertThat(jdbc, notNullValue());
    }

    @Test
    public void testProvideYelpAPI() throws Exception
    {
        YelpAPI yelp = instance.provideYelpAPI(fakeAroma);
        assertThat(yelp, notNullValue());
    }

    @Test
    public void testProvideGooglePlacesAPI() throws Exception
    {
        GooglePlacesAPI google = instance.provideGooglePlacesAPI(fakeAroma);
        assertThat(google, notNullValue());
    }

}
