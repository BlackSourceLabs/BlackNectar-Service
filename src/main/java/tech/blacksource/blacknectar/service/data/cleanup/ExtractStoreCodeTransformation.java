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


import java.util.List;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sir.wellington.alchemy.collections.lists.Lists;
import tech.blacksource.blacknectar.service.stores.Store;

/**
 * @author SirWellington
 */
public final class ExtractStoreCodeTransformation implements StoreTransformation
{
    //^"[\w'\-\./ ]+ [\w\-\#]{0,2}[\d]+[A-Za-z]?"
    private final static Logger LOG = LoggerFactory.getLogger(ExtractStoreCodeTransformation.class);
    private static final String STORE_CODE_PATTERN = "[\\w\\-\\#]{0,2}[\\d]+[A-Za-z]?";

    @Override
    public Store apply(Store store)
    {
        if (store.hasStoreCode())
        {
            return store;
        }

        String storeName = store.getName();
        List<String> tokens = tokenize(storeName);
        String lastToken = Lists.last(tokens);

        if (isStoreCode(lastToken))
        {
            List<String> storeNameTokens = Lists.copy(tokens);
            String storeCode = Lists.removeLast(storeNameTokens);
            String cleanedName = String.join(" ", storeNameTokens);

            return Store.Builder.fromStore(store)
                                .withName(cleanedName)
                                .withStoreCode(storeCode)
                                .build();
        }
        else
        {
            return store;
        }
    }

    private List<String> tokenize(String string)
    {
        StringTokenizer tokenizer = new StringTokenizer(string, " ");
        List<String> tokens = Lists.create();

        while (tokenizer.hasMoreTokens())
        {
            tokens.add(tokenizer.nextToken());
        }

        return tokens;
    }

    private boolean isStoreCode(String lastToken)
    {
        return lastToken.matches(STORE_CODE_PATTERN);
    }

}
