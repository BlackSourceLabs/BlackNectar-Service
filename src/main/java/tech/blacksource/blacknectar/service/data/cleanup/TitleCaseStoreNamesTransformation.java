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

import static com.google.common.base.Strings.isNullOrEmpty;
import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.Assertions.notNull;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 * This transformation fixes an issue where some store names come capitalized in ALL CAPS.
 * <p>
 * This algorithm uses a simple rule-based system.
 * 
 * <pre>
 * 1. In order to be considered as a candidate, store names must contains stretches with at least 3 upper-case characters [A-Z]
 * 2. Names are broken up into individual tokens.
 * 3. Keywords and abbreviations are ignored, like CVS, USA, LLC, IGA, etc.
 * </pre>
 * 
 * @author SirWellington
 */
final class TitleCaseStoreNamesTransformation implements StoreTransformation
{

    private final static Logger LOG = LoggerFactory.getLogger(TitleCaseStoreNamesTransformation.class);
    
    private static final int ABBREVIATION_LENGTH = 2;
    private static final String STORE_NAME_PATTERN = "[\\w\\-\\&\\' ]*[A-Z]{4,}.*";
    private static final String ALL_UPPER_CASE_PATTERN = "[A-Z\\']+";
    
    private static final List<String> KEYWORDS = Lists.createFrom("LLC", "CVS", "IGA", "USA");

    @Override
    public Store apply(Store store)
    {
        checkThat(store)
            .is(notNull());
        
        String storeName = store.getName();
        checkThat(storeName)
            .is(nonEmptyString());
        
        if (!matchesPattern(storeName))
        {
            return store;
        }
        
        List<String> tokens = tokenize(storeName);
        List<String> resultingTokens = Lists.create();
        
        for (String token : tokens)
        {
            if (shouldTitleCase(token))
            {
                String titleCased = titleCaseString(token);
                resultingTokens.add(titleCased);
            }
            else 
            {
                resultingTokens.add(token);
            }
        }
        
        String newName = String.join("", resultingTokens);
        
        return Store.Builder.fromStore(store)
            .withName(newName)
            .build();
        
    }

    private boolean matchesPattern(String storeName)
    {
        return storeName.matches(STORE_NAME_PATTERN);
    }

    private List<String> tokenize(String storeName)
    {
        StringTokenizer tokenizer = new StringTokenizer(storeName, " \t-&,", true);
        
        List<String> tokens = Lists.create();
        
        while(tokenizer.hasMoreTokens())
        {
            tokens.add(tokenizer.nextToken());
        }
        
        return tokens;
    }

    private boolean shouldTitleCase(String token)
    {
        return allUpperCase(token) &&
               notTooShort(token) &&
               notAKeyword(token);
    }

    private boolean notAKeyword(String token)
    {
        return KEYWORDS.stream()
            .noneMatch(keyword -> token.contains(keyword));
    }
    
    private boolean allUpperCase(String token)
    {
        return token.matches(ALL_UPPER_CASE_PATTERN);
    }

    private boolean notTooShort(String token)
    {
        return token.length() > ABBREVIATION_LENGTH;
    }

    private String titleCaseString(String token)
    {
        if (isNullOrEmpty(token))
        {
            return token;
        }
        
        String lowerCased = token.toLowerCase();
        
        String firstCharacter = "" + lowerCased.charAt(0);
        String uppercasedFirstCharacter = firstCharacter.toUpperCase();
        
        String tokenWithoutFirstCharacter = lowerCased.substring(1);
        
        return uppercasedFirstCharacter + tokenWithoutFirstCharacter;
    }


}
