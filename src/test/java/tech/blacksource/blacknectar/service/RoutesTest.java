package tech.blacksource.blacknectar.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import spark.Service;
import tech.blacksource.blacknectar.service.operations.GetSampleStoreOperation;
import tech.blacksource.blacknectar.service.operations.SayHelloOperation;
import tech.blacksource.blacknectar.service.operations.ebt.GetStatesOperation;
import tech.blacksource.blacknectar.service.operations.stores.SearchStoresOperation;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;
import tech.sirwellington.alchemy.test.junit.runners.DontRepeat;

import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

@RunWith(AlchemyTestRunner.class)
public class RoutesTest
{

    @Mock
    private SayHelloOperation sayHelloOperation;

    @Mock
    private GetSampleStoreOperation getSampleStoreOperation;

    @Mock
    private GetStatesOperation getStatesOperation;

    @Mock
    private SearchStoresOperation searchStoresOperation;

    private Service service;

    private Routes instance;

    @Before
    public void setUp() throws Exception
    {
        service = Service.ignite();

        instance = new Routes.Impl(sayHelloOperation, getSampleStoreOperation, getStatesOperation, searchStoresOperation);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new Routes.Impl(null, getSampleStoreOperation, getStatesOperation, searchStoresOperation))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new Routes.Impl(sayHelloOperation, null, getStatesOperation, searchStoresOperation))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new Routes.Impl(sayHelloOperation, getSampleStoreOperation, null, searchStoresOperation))
                .isInstanceOf(IllegalArgumentException.class);

        assertThrows(() -> new Routes.Impl(sayHelloOperation, getSampleStoreOperation, getStatesOperation, null))
                .isInstanceOf(IllegalArgumentException.class);

    }

    @Test
    public void testSetupRoutesWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.setupRoutes(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void setupRoutes() throws Exception
    {
        instance.setupRoutes(service);
    }

}