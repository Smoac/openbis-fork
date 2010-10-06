/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Gathers characters that need special treatment and methods useful during indexing.
 * 
 * @author Izabela Adamczyk
 */
public class CharacterHelper
{
    private static final char ESCAPE_CHARACTER = '\\';

    public final static Set<Character> SPECIAL_CHARACTERS = new HashSet<Character>(Arrays.asList(
    // Special code characters
            '.', ':', '-', '_',

            // Special word characters
            '\''));

    // (don't trim '-' or '_' because they may have special meaning in identifiers)
    /** those of special chars that should be trimmed */
    private final static Set<Character> TRIMMED_CHARACTERS = new HashSet<Character>(Arrays.asList(
            '.', ':', '\''));

    public static boolean isTokenCharacter(char c)
    {
        return Character.isLetterOrDigit(c) || SPECIAL_CHARACTERS.contains(c);
    }

    public static Collection<Character> getTokenSeparators()
    {
        Set<Character> separators = new HashSet<Character>();
        for (char ch = 32; ch < 256; ch++)
        {
            if (isTokenCharacter(ch) == false && ch != ESCAPE_CHARACTER)
            {
                separators.add(ch);
            }
        }
        return separators;
    }

    public static Set<Character> getTrimmedSpecialCharacters()
    {
        return TRIMMED_CHARACTERS;
    }

}