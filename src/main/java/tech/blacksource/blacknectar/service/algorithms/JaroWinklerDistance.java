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

 
package tech.blacksource.blacknectar.service.algorithms;


import com.google.common.base.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static tech.sirwellington.alchemy.arguments.Arguments.checkThat;
import static tech.sirwellington.alchemy.arguments.assertions.StringAssertions.nonEmptyString;

/**
 *
 * @author SirWellington
 */
final class JaroWinklerDistance implements StringDistanceAlgorithm
{
    private final static Logger LOG = LoggerFactory.getLogger(JaroWinklerDistance.class);

    @Override
    public int distance(String first, String second) throws IllegalArgumentException
    {
        checkThat(first, second).are(nonEmptyString());
        
        int range = Math.max(first.length(), second.length());
        
        double res = -1;
        
        return 0;
    }

    private int getNumberOfMatches(String first, String second, int range)
    {
        String matches1 = "";
        String matches2 = "";
        
        int matches = 0;
        
        for (int i = 0; i < first.length(); ++i)
        {
            //Look Backwards
            int counter = 0;
            
            while (counter <= range && i >= 0 && counter <= i)
            {
                if (first.charAt(i) == second.charAt(i - counter))
                {
                    matches += 1;
                    
                    matches1 = matches1 + first.charAt(i);
                    matches2 = matches2 + second.charAt(i);
                }
                
                counter += 1;
            }
        }
        
        return matches;
    }
    
    private int getNumberOfMismatches(String firstMatches, String secondMatches, int range)
    {
        int transpositions = 0;
        
        return transpositions;
    }
    
    private String getCommonPrefix(String first, String second, int maxLength)
    {
        StringBuilder common = new StringBuilder();
        
        for (int i = 0; i < maxLength; ++i)
        {
            if (i >= first.length() || i >= second.length())
            {
                break;
            }
            
            if (Objects.equal(first.charAt(i), second.charAt(i)))
            {
                common.append(first.charAt(i));
            }
            else 
            {
                break;
            }
        }
        
        return common.toString();
    }
    
    private int getCommonPrefixLength(String first, String second, int maxLength)
    {
        return getCommonPrefix(first, second, maxLength).length();
    }
    
    private static class MatchResults
    {
        private String firstMatches;
        private String secondMatches;
    }
}
