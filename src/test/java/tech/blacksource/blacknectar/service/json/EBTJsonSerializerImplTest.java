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

package tech.blacksource.blacknectar.service.json;

import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.blacksource.blacknectar.ebt.balance.State;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.*;


/**
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class EBTJsonSerializerImplTest
{

    @GenerateEnum
    private State state;

    private EBTJsonSerializerImpl instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new EBTJsonSerializerImpl();
    }


    private void setupData() throws Exception
    {
    }

    private void setupMocks() throws Exception
    {

    }

    @Test
    public void testSerializeState()
    {
        JsonObject expected = new StateJson(state).asJson();
        JsonObject result = instance.serializeState(state);

        assertThat(result, is(expected));
    }

    @DontRepeat
    @Test
    public void testSerializeStateWithBadArgs() throws Exception
    {
        assertThrows(() -> instance.serializeState(null)).isInstanceOf(IllegalArgumentException.class);
    }

}