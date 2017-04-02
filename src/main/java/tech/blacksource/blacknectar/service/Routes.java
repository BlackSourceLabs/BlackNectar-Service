package tech.blacksource.blacknectar.service;

import javax.inject.Inject;

import com.google.inject.ImplementedBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Service;
import tech.blacksource.blacknectar.service.operations.GetSampleStoreOperation;
import tech.blacksource.blacknectar.service.operations.SayHelloOperation;
import tech.blacksource.blacknectar.service.operations.ebt.GetStatesOperation;
import tech.blacksource.blacknectar.service.operations.stores.SearchStoresOperation;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.*;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * Created by Commander on 4/1/2017.
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
        private final SearchStoresOperation searchStoresOperation;

        @Inject
        Impl(SayHelloOperation sayHelloOperation, GetSampleStoreOperation getSampleStoreOperation, GetStatesOperation getStatesOperation, SearchStoresOperation searchStoresOperation)
        {
            checkThat(sayHelloOperation, getSampleStoreOperation, getStatesOperation, searchStoresOperation)
                    .are(notNull());

            this.sayHelloOperation = sayHelloOperation;
            this.getSampleStoreOperation = getSampleStoreOperation;
            this.getStatesOperation = getStatesOperation;
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
            });

        }
    }
}
