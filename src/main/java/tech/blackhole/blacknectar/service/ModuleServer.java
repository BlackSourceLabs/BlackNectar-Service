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

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;
import tech.aroma.client.Aroma;
import tech.blackhole.blacknectar.service.api.BlackNectarService;
import tech.blackhole.blacknectar.service.api.operations.ModuleOperations;
import tech.blackhole.blacknectar.service.exceptions.BlackNectarExceptionHandler;
import tech.sirwellington.alchemy.annotations.access.Internal;

/**
 *
 * @author SirWellington
 */
@Internal
class ModuleServer extends AbstractModule
{

    private final static Logger LOG = LoggerFactory.getLogger(ModuleServer.class);

    @Override
    protected void configure()
    {
        install(new ModuleOperations());

        bind(ExceptionHandler.class).to(BlackNectarExceptionHandler.class);
        bind(BlackNectarService.class).toInstance(BlackNectarService.newMemoryService());
        bind(Server.class);
    }

    @Provides
    @Singleton
    Aroma provideAromaClient()
    {
        return Aroma.create("ec07e6fe-7203-4f18-abf4-f33b48ec904d");
    }

    @Provides
    @Singleton
    Connection provideSQLConnection() throws SQLException
    {
        String file = "/Users/SirWellington/Documents/Code/BlackWholeLabs/BlackNectar/BlackNectar-Service/src/main/resources/Stores.db";
        return DriverManager.getConnection("jdbc:sqlite::resource:Stores.db");
    }

}
