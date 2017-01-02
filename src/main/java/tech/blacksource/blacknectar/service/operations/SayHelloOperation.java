/*
 * Copyright 2016 BlackWholeLabs.
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

 
package tech.blacksource.blacknectar.service.operations;


import com.google.common.base.Strings;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Route;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.blacksource.blacknectar.service.exceptions.BadArgumentException;
import tech.sirwellington.alchemy.annotations.arguments.Required;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 *
 * @author SirWellington
 */
public class SayHelloOperation implements Route
{
    private final static Logger LOG = LoggerFactory.getLogger(SayHelloOperation.class);

    private final Aroma aroma;

    @Inject
    public SayHelloOperation(@Required Aroma aroma)
    {
        checkThat(aroma).is(notNull());
        
        this.aroma = aroma;
    }
    
    @Override
    public String handle(Request request, Response response) throws Exception
    {
        checkThat(request, response)
            .usingMessage("Request and response cannot be null")
            .throwing(BadArgumentException.class)
            .is(notNull());
        
        LOG.info("Received GET request from IP [{}]", request.ip());
        
        aroma.begin().titled("Request Received")
            .text("From IP [{}]", request.ip())
            .withUrgency(Urgency.LOW)
            .send();
        
        response.status(200);
        //U+1F573
        return Strings.repeat("🌑", 1000);
    }

}
