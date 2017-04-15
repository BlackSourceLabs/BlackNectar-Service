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

import tech.sirwellington.alchemy.annotations.arguments.NonEmpty;


/**
 * String Distance algorithms compute the distance between two strings, with the overall goals of matching Strings that are "sort of"
 * the same. This is otherwise known as Fuzzy Matching.
 *
 * @author SirWellington
 * @see <a href="https://asecuritysite.com/forensics/simstring">https://asecuritysite.com/forensics/simstring</a>
 */
public interface StringDistanceAlgorithm
{
    /**
     * Calculates the distance from one string to another.
     *
     * @param first
     * @param second
     * @return The number of edits/transpositions that need to be made to make the strings equal.
     * @throws IllegalArgumentException
     */
    int distance(@NonEmpty String first, @NonEmpty String second) throws IllegalArgumentException;

}
