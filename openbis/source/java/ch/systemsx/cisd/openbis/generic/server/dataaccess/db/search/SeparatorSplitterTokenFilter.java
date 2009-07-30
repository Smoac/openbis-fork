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
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Used to split tokens further down after standard tokenizer. We need this, because "." which is
 * not followed be a space is not treated as a token separator by default.
 * 
 * @author Tomasz Pylak
 */
public class SeparatorSplitterTokenFilter extends TokenFilter
{
    static final char[] WORD_SEPARATORS = new char[]
        { '.', ',', '-', '_' };

    private static final String ALPHANUM_TOKEN_TYPE =
            StandardTokenizer.TOKEN_TYPES[StandardTokenizer.ALPHANUM];

    private static final String HOST_TOKEN_TYPE =
            StandardTokenizer.TOKEN_TYPES[StandardTokenizer.HOST];

    private static final String NUM_TOKEN_TYPE =
            StandardTokenizer.TOKEN_TYPES[StandardTokenizer.NUM];

    private List<Token> tokens = new LinkedList<Token>();

    protected SeparatorSplitterTokenFilter(TokenStream input)
    {
        super(input);
    }

    /**
     * Returns tokens from standard analysis, split additionally at specified separator characters.
     */
    @Override
    public final Token next(final Token reusableToken) throws IOException
    {
        if (tokens.size() > 0)
        {
            return extractFirstToken();
        }
        Token token = input.next(reusableToken);
        // avoid splitting special tokens like e-mails
        if (token == null || isSplittableToken(token) == false)
        {
            return token;
        }
        char[] termText = token.termBuffer();
        int endPos = token.termLength(); // exclusive
        int curPos = 0;
        do
        {
            int nextPos = getSeparatorIndex(termText, curPos, endPos);
            if (nextPos == endPos && tokens.size() == 0)
            {
                return token; // optimalisation, no split has occurred
            }
            addToken(token, curPos, nextPos);
            curPos = nextPos + 1;
        } while (curPos < endPos);
        return extractFirstToken();
    }

    private static boolean isSplittableToken(Token token)
    {
        String type = token.type();
        if (type.equals(ALPHANUM_TOKEN_TYPE) || type.equals(HOST_TOKEN_TYPE))
        {
            return true;
        }
        if (type.equals(NUM_TOKEN_TYPE))
        {
            // sometimes the original tokenizer lies to us and reports terms like 'version_3' to be
            // numbers. This is a heuristic to correct those lies.
            return Character.isLetter(token.term().charAt(0));
        }
        return false;
    }

    // returns the position of the first separator character. Starts browsing at curPos.
    private static int getSeparatorIndex(char[] termText, int startIndex, int endIndex)
    {
        for (int i = startIndex; i < endIndex; i++)
        {
            if (isSeparator(termText[i]))
            {
                return i;
            }
        }
        return endIndex;
    }

    private static boolean isSeparator(char ch)
    {
        for (int i = 0; i < WORD_SEPARATORS.length; i++)
        {
            if (WORD_SEPARATORS[i] == ch)
            {
                return true;
            }
        }
        return false;
    }

    private Token extractFirstToken()
    {
        assert tokens.size() > 0 : "no more tokens";
        Token t = tokens.get(0);
        tokens.remove(0);
        return t;
    }

    // startPos is inclusive position of the new token start
    // endPos is exclusive position of the new token end
    private void addToken(Token token, int startPos, int endPos)
    {
        if (startPos < endPos)
        {
            int startOffset = token.startOffset() + startPos;
            int endOffset = token.startOffset() + endPos;
            Token newToken =
                    new Token(token.termBuffer(), startPos, endPos - startPos, startOffset,
                            endOffset);
            tokens.add(newToken);
        }
    }
}
