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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db.search;

import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharTokenizer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Extends {@link Analyzer} splitting text on characters not allowed in codes or words.
 * 
 * @author Piotr Buczek
 */
public class SearchAnalyzer extends Analyzer
{

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader)
    {
        return new TrimSpecialCharsFilter(new WordAndCodeTokenizer(reader));
    }

    //
    // Helper classes
    //

    /**
     * A tokenizer that divides text at chars different than letters, digits and special chars
     * defined in {@link CharacterHelper}.
     * <p>
     * Additionally it normalizes token text to lower case (with a performance gain compared to
     * using LowerCaseFilter after tokenization).
     */
    private static class WordAndCodeTokenizer extends CharTokenizer
    {

        public WordAndCodeTokenizer(Reader input)
        {
            super(input);
        }

        @Override
        protected boolean isTokenChar(char c)
        {
            return CharacterHelper.isTokenCharacter(c);
        }

        @Override
        protected char normalize(char c)
        {
            return Character.toLowerCase(c);
        }
    }

    /**
     * Normalizes tokens extracted with {@link WordAndCodeTokenizer} trimming special chars.
     */
    private static final class TrimSpecialCharsFilter extends TokenFilter
    {

        public TrimSpecialCharsFilter(TokenStream input)
        {
            super(input);
        }

        @Override
        public final Token next(final Token reusableToken) throws java.io.IOException
        {
            assert reusableToken != null;
            Token nextToken = input.next(reusableToken);

            if (nextToken == null)
                return null;

            char[] buffer = nextToken.termBuffer();
            final int bufferLength = nextToken.termLength();

            int startCounter = 0; // counts chars to trim from the beginning
            Set<Character> trimmedCharacters = CharacterHelper.getTrimmedSpecialCharacters();
            for (int i = 0; i < bufferLength; i++)
            {
                if (trimmedCharacters.contains(buffer[i]))
                {
                    startCounter++;
                } else
                {
                    break;
                }
            }

            // if all chars are special leave the token untouched
            if (startCounter == bufferLength)
            {
                return nextToken;
            }

            int endCounter = 0; // counts chars to trim from the end
            for (int i = bufferLength - 1; i > 0; i--)
            {
                if (trimmedCharacters.contains(buffer[i]))
                {
                    endCounter++;
                } else
                {
                    break;
                }
            }

            if (startCounter > 0)
            {
                // need to shift all characters (setting startPos to >0 breaks search equality)
                // see: StandardFilter
                for (int i = startCounter; i < bufferLength; i++)
                {
                    buffer[i - startCounter] = buffer[i];
                }
            }

            // change length
            if (startCounter + endCounter > 0)
            {
                int trimmedLength = bufferLength - (startCounter + endCounter);
                nextToken.setTermLength(trimmedLength);
            }

            return nextToken;
        }
    }

}
