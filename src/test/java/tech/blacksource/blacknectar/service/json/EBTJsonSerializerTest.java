package tech.blacksource.blacknectar.service.json;/*
 * 
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

import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import tech.aroma.client.Aroma;
import tech.sirwellington.alchemy.test.junit.runners.AlchemyTestRunner;

import static org.junit.Assert.assertNotNull;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
public class EBTJsonSerializerTest
{
    private Gson gson = new Gson();

    @Mock
    private Aroma aroma;

    @Test
    public void newInstance() throws Exception
    {
        EBTJsonSerializer result = EBTJsonSerializer.newInstance();
        assertNotNull(result);
    }

    @Test
    public void newInstance1() throws Exception
    {
        EBTJsonSerializer result = EBTJsonSerializer.newInstance(gson);
        assertNotNull(result);
    }

    @Test
    public void newInstance2() throws Exception
    {
        EBTJsonSerializer result = EBTJsonSerializer.newInstance(aroma);
        assertNotNull(result);
    }

    @Test
    public void newInstance3() throws Exception
    {
        EBTJsonSerializer result = EBTJsonSerializer.newInstance(aroma, gson);
        assertNotNull(result);
    }

    private EBTJsonSerializer instance;


    private void setupData()
    {

    }

    private void setupMocks()
    {

    }
}