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

 
package tech.blacksource.blacknectar.service.exceptions;


import com.google.common.base.Strings;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;
import tech.aroma.client.Aroma;
import tech.aroma.client.Urgency;
import tech.redroma.yelp.exceptions.YelpException;
import tech.sirwellington.alchemy.annotations.arguments.Required;
import tech.sirwellington.alchemy.arguments.FailedAssertionException;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;

/**
 * Handles Exceptions and reports them to {@link Aroma}.
 * 
 * @author SirWellington
 */
public final class BlackNectarExceptionHandler implements ExceptionHandler
{
    private final static Logger LOG = LoggerFactory.getLogger(BlackNectarExceptionHandler.class);
    private static final int BAD_ARGUMENT = 400;
    private static final int INTERNAL_ERROR = 500;
    
    private final Aroma aroma;

    @Inject
    public BlackNectarExceptionHandler(@Required Aroma aroma)
    {
        checkThat(aroma).is(notNull());
        this.aroma = aroma;
    }

    @Override
    public void handle(Exception ex, Request request, Response response)
    {
        if (ex == null)
        {
            LOG.warn("ExceptionMapper received null exception");
            return;
        }
        
        String ip = request.ip();
        ip = Strings.nullToEmpty(ip);
        
        if (ex instanceof BadArgumentException || ex instanceof FailedAssertionException)
        {
            LOG.error("Received BadArgumentException");
            
            aroma.begin().titled("Received Bad Argument")
                .text("From IP: [{}] | {}", ip, ex)
                .withUrgency(Urgency.MEDIUM)
                .send();
            
            response.status(BAD_ARGUMENT);
        }
        else if (ex instanceof BlackNectarAPIException)
        {
            LOG.error("Internal Operation failed", ex);
            
            aroma.begin().titled("Operation Failed")
                .text("From IP [{}] | {}", ip, ex)
                .withUrgency(Urgency.HIGH)
                .send();
            
            response.status(((BlackNectarAPIException) ex).getStatusCode());
        }
        else if (ex instanceof YelpException)
        {
            LOG.error("Failed to make Yelp API Call", ex);
            
            aroma.begin()
                .titled("Yelp API Call Failed")
                .text("Failed to make Yelp API Call from request: {}", request, ex)
                .withUrgency(Urgency.HIGH)
                .send();
            
            response.status(INTERNAL_ERROR);
        }
        else
        {
            LOG.error("Unexpected Exception", ex);
            
            aroma.begin().titled("Operation Failed")
                .text("Unexpected Exception occured From IP: [{}] | {}", ip, ex)
                .withUrgency(Urgency.HIGH)
                .send();
            
            response.status(INTERNAL_ERROR);
        }
        
        response.type("text/plain");
        response.body(ex.getMessage());
    }
}
