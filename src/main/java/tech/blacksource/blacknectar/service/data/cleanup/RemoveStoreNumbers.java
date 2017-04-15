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

package tech.blacksource.blacknectar.service.data.cleanup;

import com.google.inject.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.blacksource.blacknectar.service.ModuleDatabaseProduction;
import tech.blacksource.blacknectar.service.ModuleServer;

/**
 * @author SirWellington
 */
public final class RemoveStoreNumbers
{

    private final static Logger LOG = LoggerFactory.getLogger(RemoveStoreNumbers.class);

    public static void main(String[] args) throws Exception
    {
        Injector injector = Guice.createInjector(new ModuleServer(),
                                                 new ModuleDatabaseProduction(),
                                                 new ModuleRemoveStoreNumbers());

        RunStoreTransformation instance = injector.getInstance(RunStoreTransformation.class);

        instance.call();
    }

    private static class ModuleRemoveStoreNumbers extends AbstractModule
    {

        @Override
        protected void configure()
        {
            bind(StoreTransformation.class).to(ExtractStoreCodeTransformation.class);
        }
    }

}
