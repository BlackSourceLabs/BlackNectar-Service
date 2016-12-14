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


package tech.blacksource.blacknectar.service.api.images;

import java.net.URL;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.annotations.arguments.Required;


/**
 *
 * @author SirWellington
 */
public interface ImageLoader 
{
    URL getImageFor(@Required Store store);
}
