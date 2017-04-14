package tech.blacksource.blacknectar.service.json;

import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import org.junit.Test;
import org.junit.runner.RunWith;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AlchemyTestRunner.class)
@Repeat
public class JSONTest
{

    @GenerateList(String.class)
    private List<String> strings;

    @GenerateList(Integer.class)
    private List<Integer> numbers;

    @Test
    public void testCollectArrayWithStrings() throws Exception
    {
        JsonArray expected = new JsonArray();
        strings.forEach(expected::add);

        JsonArray result = strings.stream()
                                  .map(JsonPrimitive::new)
                                  .collect(JSON.collectArray());

        assertThat(result, is(expected));
    }

    @Test
    public void testCollectArrayWithNumbers() throws Exception
    {
        JsonArray expected = new JsonArray();
        numbers.forEach(expected::add);

        JsonArray result = numbers.stream()
                                  .map(JsonPrimitive::new)
                                  .collect(JSON.collectArray());

        assertThat(result, is(expected));
    }

    @DontRepeat
    @Test
    public void testCollectArrayWithEmpty() throws Exception
    {
        JsonArray expected = new JsonArray();

        JsonArray result = Lists.<String>emptyList()
                                .stream()
                                .map(JsonPrimitive::new)
                                .collect(JSON.collectArray());

        assertThat(result, is(expected));
    }


}