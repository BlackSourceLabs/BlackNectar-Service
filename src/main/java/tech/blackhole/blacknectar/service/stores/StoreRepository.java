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

package tech.blackhole.blacknectar.service.stores;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.aroma.client.Urgency;
import tech.blackhole.blacknectar.service.OperationFailedException;

import static java.util.stream.Collectors.toList;
import static tech.blackhole.blacknectar.service.Server.AROMA;

/**
 *
 * @author SirWellington
 */
public interface StoreRepository
{

    /**
     * Returns all of the {@linkplain Store Stores} saved in the Repository;
     *
     * @return
     */
    List<Store> getAllStores();
    
    static StoreRepository FILE = new FileRepository();


}
