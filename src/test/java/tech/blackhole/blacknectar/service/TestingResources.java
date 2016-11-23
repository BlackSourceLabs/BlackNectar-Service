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

 
package tech.blackhole.blacknectar.service;


import com.google.inject.Guice;
import com.google.inject.Injector;
import java.sql.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SirWellington
 */
public class TestingResources 
{
    private final static Logger LOG = LoggerFactory.getLogger(TestingResources.class);

    private static final Injector INJECTOR = Guice.createInjector(new ModuleServer());

    public static Injector createInjector()
    {
        return INJECTOR;
    }
    
    public static Connection createSQLConnection()
    {
        return INJECTOR.getInstance(Connection.class);
    }
    
}
