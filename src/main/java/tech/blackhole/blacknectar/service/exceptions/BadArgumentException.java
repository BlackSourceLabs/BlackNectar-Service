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

 
package tech.blackhole.blacknectar.service.exceptions;

/**
 * Thrown when the service receives a bad argument.
 *
 * @author SirWellington
 */
public class BadArgumentException extends BlackNectarAPIException
{

    public BadArgumentException()
    {
    }

    public BadArgumentException(String message)
    {
        super(message);
    }

    public BadArgumentException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BadArgumentException(Throwable cause)
    {
        super(cause);
    }

    @Override
    public int getStatusCode()
    {
        return 400;
    }
    
}
