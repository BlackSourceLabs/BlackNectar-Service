package tech.blacksource.blacknectar.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import spark.Service;
import tech.blacksource.blacknectar.service.operations.GetSampleStoreOperation;
import tech.blacksource.blacknectar.service.operations.SayHelloOperation;
import tech.blacksource.blacknectar.service.operations.ebt.GetStateInfoOperation;
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
    private GetStateInfoOperation getStateInfoOperation;

    @Mock
    private SearchStoresOperation searchStoresOperation;

    private Service service;

    private Routes instance;

    @Before
    public void setUp() throws Exception
    {
        service = Service.ignite();

        instance = new Routes.Impl(sayHelloOperation,
                                   getSampleStoreOperation,
                                   getStatesOperation,
                                   getStateInfoOperation,
                                   searchStoresOperation);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new Routes.Impl(null, getSampleStoreOperation, getStatesOperation, getStateInfoOperation, searchStoresOperation));
        assertThrows(() -> new Routes.Impl(sayHelloOperation, null, getStatesOperation, getStateInfoOperation, searchStoresOperation));
        assertThrows(() -> new Routes.Impl(sayHelloOperation, getSampleStoreOperation, null, getStateInfoOperation, searchStoresOperation));
        assertThrows(() -> new Routes.Impl(sayHelloOperation, getSampleStoreOperation, getStatesOperation, null, searchStoresOperation));
        assertThrows(() -> new Routes.Impl(sayHelloOperation, getSampleStoreOperation, getStatesOperation, getStateInfoOperation, null));
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