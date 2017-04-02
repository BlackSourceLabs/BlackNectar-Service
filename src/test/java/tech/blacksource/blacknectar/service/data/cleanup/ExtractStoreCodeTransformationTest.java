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

package tech.blacksource.blacknectar.service.data.cleanup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import tech.blacksource.blacknectar.service.stores.Store;
import tech.sirwellington.alchemy.test.junit.runners.*;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static tech.blacksource.blacknectar.service.BlackNectarGenerators.stores;
import static tech.sirwellington.alchemy.generator.AlchemyGenerator.one;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.ALPHABETIC;
import static tech.sirwellington.alchemy.test.junit.runners.GenerateString.Type.NUMERIC;

/**
 *
 * @author SirWellington
 */
@Repeat(10)
@RunWith(AlchemyTestRunner.class)
public class ExtractStoreCodeTransformationTest
{

    @GenerateString(NUMERIC)
    private String storeCode;

    @GenerateString(ALPHABETIC)
    private String storeName;

    private String combinedName;

    private Store dirtyStore;
    private Store cleanStore;

    private ExtractStoreCodeTransformation instance;

    @Before
    public void setUp() throws Exception
    {

        setupData();
        setupMocks();

        instance = new ExtractStoreCodeTransformation();
    }

    private void setupData() throws Exception
    {
        combinedName = storeName + " " + storeCode;

        dirtyStore = one(stores());
        dirtyStore = Store.Builder.fromStore(dirtyStore)
            .withName(combinedName)
            .withoutStoreCode()
            .build();

        cleanStore = Store.Builder.fromStore(dirtyStore)
            .withName(storeName)
            .withStoreCode(storeCode)
            .build();
    }

    private void setupMocks() throws Exception
    {

    }

    @Test
    public void testApply() throws Exception
    {
        Store result = instance.apply(dirtyStore);
        assertThat(result, is(cleanStore));
    }

}
