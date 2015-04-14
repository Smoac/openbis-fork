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

import java.io.IOException;
import java.io.Reader;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharTokenizer;

/**
 * Extends {@link Analyzer} splitting text on characters not allowed in codes or words.
 * 
 * @author Piotr Buczek
 */
public final class SearchAnalyzer extends Analyzer
{

    //
    // Helper classes
    //

    /**
     * A tokenizer that divides text at chars different than letters, digits and special chars defined in {@link CharacterHelper}.
     * <p>
     * Additionally it normalizes token text to lower case (with a performance gain compared to using LowerCaseFilter after tokenization).
     */
    private static class WordAndCodeTokenizer extends CharTokenizer
    {

        public WordAndCodeTokenizer(Reader input)
        {
            super(input);
        }

        @Override
        protected boolean isTokenChar(int c)
        {
            return CharacterHelper.isTokenCharacter((char) c);
        }

        @Override
        protected int normalize(int c)
        {
            return Character.toLowerCase((char) c);
        }
    }

    /**
     * Normalizes tokens extracted with {@link WordAndCodeTokenizer} trimming special chars.
     */
    private static final class TrimSpecialCharsFilter extends TokenFilter
    {
        private CharTermAttribute termAttr;

        public TrimSpecialCharsFilter(TokenStream input)
        {
            super(input);
            this.termAttr = addAttribute(CharTermAttribute.class);
        }

        @Override
        public final boolean incrementToken() throws IOException
        {
            while (input.incrementToken())
            {
                char[] buffer = this.termAttr.buffer();
                final int bufferLength = this.termAttr.length();

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
                    return true;
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
                    this.termAttr.setLength(trimmedLength);
                }

                return true;
            }
            return false;
        }
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName, Reader reader)
    {
        Tokenizer source = new WordAndCodeTokenizer(reader);
        TokenStream filter = new TrimSpecialCharsFilter(source);
        return new TokenStreamComponents(source, filter);
    }

}
