/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.utilities;

import java.util.Random;

/**
 * Generator for human pronounceable passwords
 * 
 * @author Franz-Josef Elmer
 */
public class PasswordGenerator
{
    private enum State
    {
        DIGIT()
        {
            @Override
            State nextState(Random random)
            {
                return random.nextBoolean() ? CONSONANT : VOWEL;
            }

            @Override
            String getCharacters()
            {
                return "0123456789";
            }
        },

        CONSONANT()
        {
            @Override
            State nextState(Random random)
            {
                return random.nextBoolean() ? DIGIT : VOWEL;
            }

            @Override
            String getCharacters()
            {
                return "bdcfghjklmnpqrstvwxzBCDFGHJKLMNPQRSTVWXZ";
            }

        },

        VOWEL()
        {
            @Override
            State nextState(Random random)
            {
                return random.nextBoolean() ? CONSONANT : DIGIT;
            }

            @Override
            String getCharacters()
            {
                return "aeiouyAEIOUY";
            }
        };

        State nextState(Random random)
        {
            return this;
        }

        void appendCharacterTo(StringBuilder builder, Random random)
        {
            String characters = getCharacters();
            builder.append(characters.charAt(random.nextInt(characters.length())));
        }

        abstract String getCharacters();
    }

    private final Random random;

    /**
     * Creates an instance based on the default constructor of {@link Random}.
     */
    public PasswordGenerator()
    {
        this(new Random());
    }

    /**
     * Creates an instance which uses the specified random number generator.
     */
    public PasswordGenerator(Random random)
    {
        assert random != null : "Unspecified random number generator.";
        this.random = random;
    }

    /**
     * Creates a password of specified length.
     */
    public String generatePassword(int length)
    {
        State state = State.DIGIT;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++)
        {
            state = state.nextState(random);
            state.appendCharacterTo(builder, random);
        }
        return builder.toString();
    }
}
