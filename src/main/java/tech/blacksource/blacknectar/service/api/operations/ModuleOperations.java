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

 
package tech.blacksource.blacknectar.service.api.operations;


import com.google.inject.AbstractModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.blacksource.blacknectar.service.api.images.ModuleImageLoader;
import tech.blacksource.blacknectar.service.stores.ModuleStores;

/**
 *
 * @author SirWellington
 */
public final class ModuleOperations extends AbstractModule
{
    private final static Logger LOG = LoggerFactory.getLogger(ModuleOperations.class);

    @Override
    protected void configure()
    {
        install(new ModuleStores());
        install(new ModuleImageLoader());
        
        bind(GetSampleStoreOperation.class);
        bind(SayHelloOperation.class);
        bind(SearchStoresOperation.class);
    }

}
