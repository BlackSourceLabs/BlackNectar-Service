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

package tech.blacksource.blacknectar.service.stores;

import com.google.inject.ImplementedBy;
import java.util.List;
import tech.aroma.client.Aroma;
import tech.sirwellington.alchemy.annotations.arguments.Required;

/**
 * A {@link StoreDataSource} represents a resource that can provide access to all EBT stores.
 * For a file-based Data Source that loads stores from a government-provided CSV file, see
 * {@link #newFileBasedDataSource(tech.aroma.client.Aroma) }.
 * 
 * @author SirWellington
 * @see #newFileBasedDataSource(tech.aroma.client.Aroma) 
 */
@ImplementedBy(FileStoreDataSource.class)
public interface StoreDataSource
{

    /**
     * Returns all of the {@linkplain Store Stores} saved in the Repository;
     *
     * @return
     */
    List<Store> getAllStores();
    
    /**
     * Creates a new file-based Data Source that reads all of
     * the stores out of a CSV file.
     * 
     * @param aroma
     * @return 
     */
    static StoreDataSource newFileBasedDataSource(@Required Aroma aroma)
    {
        return new FileStoreDataSource(aroma, IDGenerator.INSTANCE);
    }
}
