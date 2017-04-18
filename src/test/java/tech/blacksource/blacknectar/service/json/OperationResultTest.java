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

import com.google.gson.JsonObject;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static tech.sirwellington.alchemy.test.junit.ThrowableAssertion.assertThrows;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat
public class OperationResultTest
{

    @GenerateString
    private String message;

    @GenerateBoolean
    private Boolean result;

    @GenerateInteger
    private Integer statusCode;

    private OperationResult instance;

    @Before
    public void setUp() throws Exception
    {
        instance = new OperationResult(message, result, statusCode);
    }

    @Test
    public void testAsJson() throws Exception
    {
        JsonObject result = instance.asJson();
        assertThat(result, Matchers.notNullValue());

        String messageResult = result.get(OperationResult.Keys.MESSAGE).getAsString();
        boolean successResult = result.get(OperationResult.Keys.SUCCESS).getAsBoolean();
        int statusCode = result.get(OperationResult.Keys.STATUS_CODE).getAsInt();

        assertThat(messageResult, not(isEmptyOrNullString()));

        assertThat(messageResult, is(this.message));
        assertThat(successResult, is(this.result));
        assertThat(statusCode, is(this.statusCode));
    }


    @Test
    public void testFromJson() throws Exception
    {
        JsonObject json = new JsonObject();

        json.addProperty(OperationResult.Keys.MESSAGE, message);
        json.addProperty(OperationResult.Keys.SUCCESS, result);
        json.addProperty(OperationResult.Keys.STATUS_CODE, statusCode);

        OperationResult result = OperationResult.fromJson(json);

        assertThat(result, is(instance));
    }

    @DontRepeat
    @Test
    public void testFromJsonWithBadArgs() throws Exception
    {
        assertThrows(() -> OperationResult.fromJson(null));
        assertThrows(() -> OperationResult.fromJson(new JsonObject()));
    }
}