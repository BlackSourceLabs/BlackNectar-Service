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

package tech.blacksource.blacknectar.service.exceptions;

/**
 * Thrown when referencing resources or materials that don't exist.
 *
 * @author SirWellington
 */
public class DoesNotExistException extends BlackNectarAPIException
{

    private static final long serialVersionUID = 1L;

    public DoesNotExistException()
    {
    }

    public DoesNotExistException(String message)
    {
        super(message);
    }

    public DoesNotExistException(Throwable cause)
    {
        super(cause);
    }

    public DoesNotExistException(String message, Throwable cause)
    {
        super(message, cause);
    }

}
