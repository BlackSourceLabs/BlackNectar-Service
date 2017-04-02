package tech.blacksource.blacknectar.service.operations.ebt;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.lists.Lists;
import sir.wellington.alchemy.collections.sets.Sets;
import spark.Request;
import spark.Response;
import tech.blacksource.blacknectar.ebt.balance.State;
import tech.blacksource.blacknectar.ebt.balance.StateWebsiteFactory;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.sirwellington.alchemy.generator.*;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

@RunWith(AlchemyTestRunner.class)
@Repeat(25)
public class GetStatesOperationTest
{

    @Mock
    private StateWebsiteFactory websiteFactory;

    private Set<State> states;

    @Mock
    private Request request;

    @Mock
    private Response response;

    private GetStatesOperation instance;

    @Before
    public void setUp()
    {
        AlchemyGenerator<State> stateGenerator = EnumGenerators.enumValueOf(State.class);
        List<State> statesList = CollectionGenerators.listOf(stateGenerator, 10);
        this.states = Sets.copyOf(statesList);

        setupMocks();

        instance = new GetStatesOperation(websiteFactory);
    }

    private void setupMocks()
    {
        when(websiteFactory.getSupportedStates()).thenReturn(states);
    }


    @DontRepeat
    @Test
    public void testConstructor()
    {
        assertThrows(() -> new GetStatesOperation(null));
    }

    @Test
    public void handle() throws Exception
    {
        JsonArray array = instance.handle(request, response);
        assertThat(array, Matchers.notNullValue());
        assertThat(array.size(), Matchers.greaterThan(0));

        List<JsonObject> expected = states.stream()
                .map(GetStatesOperation.StateJson::new)
                .map(GetStatesOperation.StateJson::asJson)
                .collect(Collectors.toList());

        List<JsonObject> result = Lists.create();

        for (int i = 0; i < array.size(); ++i)
        {
            JsonObject object = array.get(i).getAsJsonObject();
            result.add(object);
        }

        assertThat(Sets.copyOf(result), is(Sets.copyOf(expected)));

        verify(websiteFactory, never()).getConnectionToState(any());

    }

    @Test
    public void testHandleWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.handle(null, response)).isInstanceOf(BadArgumentException.class);
        assertThrows(() -> instance.handle(request, null)).isInstanceOf(BadArgumentException.class);
    }

}