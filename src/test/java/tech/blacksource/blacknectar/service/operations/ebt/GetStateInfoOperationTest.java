package tech.blacksource.blacknectar.service.operations.ebt;

import java.util.List;
import java.util.Set;

import com.google.gson.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import sir.wellington.alchemy.collections.sets.Sets;
import spark.Request;
import spark.Response;
import tech.aroma.client.Aroma;
import tech.blacksource.blacknectar.ebt.balance.*;
import tech.blacksource.blacknectar.service.JSON;
import tech.blacksource.blacknectar.service.exceptions.*;
import tech.blacksource.blacknectar.service.operations.Parameters;
import tech.sirwellington.alchemy.generator.*;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.generator.NumberGenerators.integers;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;

@RunWith(AlchemyTestRunner.class)
public class GetStateInfoOperationTest
{

    private Aroma aroma;

    @Mock
    private StateWebsiteFactory websiteFactory;

    @Mock
    private StateWebsite stateWebsite;

    @GenerateEnum
    private State state;

    private Set<StateWebsite.Feature> features;

    private GetStateInfoOperation instance;

    @Mock
    private Request request;

    @Mock
    private Response response;

    @Before
    public void setUp() throws Exception
    {
        setupMocks();
        instance = new GetStateInfoOperation(aroma, websiteFactory);
    }

    private void setupMocks() throws Exception
    {
        aroma = Aroma.createNoOpInstance();

        when(websiteFactory.getSupportedStates()).thenReturn(Sets.createFrom(state));
        when(websiteFactory.getConnectionToState(state)).thenReturn(stateWebsite);


        AlchemyGenerator<StateWebsite.Feature> featureGenerator = EnumGenerators.enumValueOf(StateWebsite.Feature.class);

        int listSize = one(integers(1, 4));
        List<StateWebsite.Feature> featureList = CollectionGenerators.listOf(featureGenerator, listSize);
        this.features = Sets.copyOf(featureList);

        when(stateWebsite.getFeatures()).thenReturn(this.features);

        String stateParameter = state.getAbbreviation().toString().toLowerCase();
        when(request.params(Parameters.EBT.STATE)).thenReturn(stateParameter);
    }

    @DontRepeat
    @Test
    public void testConstructor() throws Exception
    {
        assertThrows(() -> new GetStateInfoOperation(aroma, null));
        assertThrows(() -> new GetStateInfoOperation(null, websiteFactory));
    }

    @Test
    public void handle() throws Exception
    {
        JsonElement result = instance.handle(request, response);
        checkResultIsExpected(result);
    }

    @DontRepeat
    @Test
    public void testWhenRequestIsNull() throws Exception
    {
        assertThrows(() -> instance.handle(null, response)).isInstanceOf(IllegalArgumentException.class);
    }

    @DontRepeat
    @Test
    public void testWhenResponseIsNull() throws Exception
    {
        assertThrows(() -> instance.handle(request, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @DontRepeat
    @Test
    public void testWhenStateParameterMissing() throws Exception
    {
        when(request.params(anyString())).thenReturn(null);

        assertThrows(() -> instance.handle(request, response))
                .isInstanceOf(BadArgumentException.class);
    }


    @Repeat(50)
    @Test
    public void testWhenStateParameterIsBad() throws Exception
    {
        String badState = one(StringGenerators.alphanumericString());

        when(request.params(Parameters.EBT.STATE)).thenReturn(badState);

        assertThrows(() -> instance.handle(request, response))
                .isInstanceOf(BadArgumentException.class);
    }

    @Repeat
    @Test
    public void testWhenFullStateNameUsedAsParameter() throws Exception
    {
        String stateParameter = state.toString().toLowerCase();

        when(request.params(Parameters.EBT.STATE)).thenReturn(stateParameter);

        JsonElement result = instance.handle(request, response);

        checkResultIsExpected(result);
    }

    @DontRepeat
    @Test
    public void testWhenStateIsNotSupported() throws Exception
    {
        when(websiteFactory.getSupportedStates()).thenReturn(Sets.emptySet());

        assertThrows(() -> instance.handle(request, response))
                .isInstanceOf(tech.blacksource.blacknectar.service.exceptions.UnsupportedStateException.class);
    }

    private void checkResultIsExpected(JsonElement result)
    {
        assertThat(result, notNullValue());
        assertThat(result instanceof JsonArray, is(true));

        JsonArray expected = features.stream()
                                     .map(StateWebsite.Feature::toString)
                                     .map(JsonPrimitive::new)
                                     .collect(JSON.collectArray());

        assertThat(result, is(expected));

    }
}