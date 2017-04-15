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

import javax.inject.Inject;

import com.google.inject.ImplementedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;
import tech.blacksource.blacknectar.service.operations.*;
import tech.blacksource.blacknectar.service.operations.ebt.GetStateInfoOperation;
import tech.blacksource.blacknectar.service.operations.ebt.GetStatesOperation;
import tech.blacksource.blacknectar.service.operations.stores.SearchStoresOperation;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * @author SirWellington
 */
@FunctionalInterface
@ImplementedBy(Routes.Impl.class)
interface Routes
{
    void setupRoutes(@Required Service service);

    class Impl implements Routes
    {
        private static final Logger LOG = LoggerFactory.getLogger(Impl.class);

        private final SayHelloOperation sayHelloOperation;
        private final GetSampleStoreOperation getSampleStoreOperation;
        private final GetStatesOperation getStatesOperation;
        private final GetStateInfoOperation getStateInfoOperation;
        private final SearchStoresOperation searchStoresOperation;

        @Inject
        Impl(SayHelloOperation sayHelloOperation,
             GetSampleStoreOperation getSampleStoreOperation,
             GetStatesOperation getStatesOperation,
             GetStateInfoOperation getStateInfoOperation,
             SearchStoresOperation searchStoresOperation)
        {
            checkThat(sayHelloOperation,
                      getSampleStoreOperation,
                      getStatesOperation,
                      getStateInfoOperation,
                      searchStoresOperation)
                    .are(notNull());

            this.sayHelloOperation = sayHelloOperation;
            this.getSampleStoreOperation = getSampleStoreOperation;
            this.getStatesOperation = getStatesOperation;
            this.getStateInfoOperation = getStateInfoOperation;
            this.searchStoresOperation = searchStoresOperation;
        }

        @Override
        public void setupRoutes(Service service)
        {
            checkThat(service).is(notNull());


            service.get("/stores", this.searchStoresOperation);
            service.get("/sample-store", this.getSampleStoreOperation);
            service.get("/", this.sayHelloOperation);

            service.path("/ebt", () ->
            {
                service.get("", this.getStatesOperation);

                service.get("/" + Parameters.EBT.STATE, this.getStateInfoOperation);
            });

        }
    }
}
