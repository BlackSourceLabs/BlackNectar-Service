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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.service.data.SQLImageMapper;
import tech.blacksource.blacknectar.service.data.StoreRepository;

/**
 *
 * @author SirWellington
 */
public class TestingResources 
{
    private final static Logger LOG = LoggerFactory.getLogger(TestingResources.class);

    private static final Injector INJECTOR = Guice.createInjector(new ModuleServer(), new ModuleTestingDatabase());

    public static Injector createInjector()
    {
        return INJECTOR;
    }
    
    public static JdbcTemplate createDatabaseConnection()
    {
        return INJECTOR.getInstance(JdbcTemplate.class);
    }

    public static Aroma getAroma()
    {
        return INJECTOR.getInstance(Aroma.class);
    }
    
    public static SQLImageMapper getImageMapper()
    {
        return INJECTOR.getInstance(SQLImageMapper.class);
    }

    public static StoreRepository getStoreRepository()
    {
        return INJECTOR.getInstance(StoreRepository.class);
    }
    
}
