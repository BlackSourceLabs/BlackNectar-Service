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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * @author SirWellington
 */
@RunWith(AlchemyTestRunner.class)
@Repeat
public class OperationResultJsonTest
{
    @GenerateString
    private String message;

    @GenerateBoolean
    private Boolean result;

    @GenerateInteger
    private Integer statusCode;

    private OperationResultJson instance;

    @Before
    public void setUp() throws Exception
    {
        instance = new OperationResultJson(message, result, statusCode);
    }

    @Test
    public void testAsJson() throws Exception
    {
        JsonObject result = instance.asJson();
        assertThat(result, Matchers.notNullValue());

        String messageResult = result.get(OperationResultJson.Keys.MESSAGE).getAsString();
        boolean successResult = result.get(OperationResultJson.Keys.SUCCESS).getAsBoolean();
        int statusCode = result.get(OperationResultJson.Keys.STATUS_CODE).getAsInt();

        assertThat(messageResult, not(isEmptyOrNullString()));

        assertThat(messageResult, is(this.message));
        assertThat(successResult, is(this.result));
        assertThat(statusCode, is(this.statusCode));
    }

}