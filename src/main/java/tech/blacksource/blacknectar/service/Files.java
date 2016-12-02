/*
 * Copyright 2016 BlackSourceLabs.
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

 
package tech.blacksource.blacknectar.service;


import com.google.common.base.Charsets;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SirWellington
 */
class Files 
{

    private final static Logger LOG = LoggerFactory.getLogger(Files.class);

    static String readFile(String filename)
    {
        File file = new File(filename);
        try
        {
            return com.google.common.io.Files.toString(file, Charsets.UTF_8);
        }
        catch (IOException ex)
        {
            LOG.warn("Failed to read file: {}", ex);
            return "";
        }
    }
}
