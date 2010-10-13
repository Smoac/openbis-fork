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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A little template engine. Usage example:
 * 
 * <pre>
 * Template template = new Template(&quot;Hello ${name}!&quot;);
 * template.bind(&quot;name&quot;, &quot;world&quot;);
 * String text = template.createText();
 * </pre>
 * 
 * The method {@link #bind(String, String)} throws an exception if the placeholder name is unknown.
 * The method {@link #attemptToBind(String, String)} returns <code>false</code> if the placeholder
 * name is unknown. The method {@link #createText()} throws an exception if not all placeholders
 * have been bound.
 * <p>
 * Since placeholder bindings change the state of an instance of this class there is method
 * {@link #createFreshCopy()} which creates a copy without reparsing the template. Usage example:
 * 
 * <pre>
 * static final Template TEMPLATE = new Template(&quot;Hello ${name}!&quot;);
 * 
 * void doSomething()
 * {
 *     Template template = TEMPLATE.createFreshCopy();
 *     template.bind(&quot;name&quot;, &quot;world&quot;);
 *     String text = template.createText();
 * }
 * </pre>
 * 
 * @author Franz-Josef Elmer
 */
public class Template
{
    private static final char PLACEHOLDER_ESCAPE_CHARACTER = '$';

    private static final char PLACEHOLDER_START_CHARACTER = '{';

    private static final char PLACEHOLDER_METADATA_SEPARATOR = ':';

    private static final char PLACEHOLDER_END_CHARACTER = '}';

    private static final String createPlaceholder(String variableName)
    {
        return PLACEHOLDER_ESCAPE_CHARACTER + (PLACEHOLDER_START_CHARACTER + variableName)
                + PLACEHOLDER_END_CHARACTER;
    }

    private static interface IToken
    {
        public void appendTo(StringBuilder builder);
    }

    private static final class PlainToken implements IToken
    {
        private final String plainText;

        PlainToken(String plainText)
        {
            assert plainText != null : "Unspecified plain text.";
            this.plainText = plainText;
        }

        public void appendTo(StringBuilder builder)
        {
            builder.append(plainText);
        }
    }

    private static final class VariableToken implements IToken
    {
        private final String variableName;

        private String value;

        VariableToken(String variablePlaceHolder)
        {
            assert variablePlaceHolder != null : "Unspecified variable place holder.";
            this.variableName = variablePlaceHolder;
        }

        public void appendTo(StringBuilder builder)
        {
            builder.append(isBound() ? value : createPlaceholder(variableName));
        }

        String getVariableName()
        {
            return variableName;
        }

        boolean isBound()
        {
            return value != null;
        }

        void bind(String v)
        {
            this.value = v;
        }
    }

    private static enum State
    {
        PLAIN()
        {
            @Override
            State next(char character, TokenBuilder tokenBuilder)
            {
                if (character == PLACEHOLDER_ESCAPE_CHARACTER)
                {
                    return STARTING_PLACEHOLDER;
                }
                tokenBuilder.addCharacter(character);
                return PLAIN;
            }
        },

        STARTING_PLACEHOLDER()
        {
            @Override
            State next(char character, TokenBuilder tokenBuilder)
            {
                switch (character)
                {
                    case PLACEHOLDER_ESCAPE_CHARACTER:
                        tokenBuilder.addCharacter(PLACEHOLDER_ESCAPE_CHARACTER);
                        return PLAIN;
                    case PLACEHOLDER_START_CHARACTER:
                        tokenBuilder.finishPlainToken();
                        return PLACEHOLDER;
                    default:
                        tokenBuilder.addCharacter(PLACEHOLDER_ESCAPE_CHARACTER);
                        tokenBuilder.addCharacter(character);
                        return PLAIN;
                }
            }
        },

        PLACEHOLDER()
        {
            @Override
            State next(char character, TokenBuilder tokenBuilder)
            {
                if (character == PLACEHOLDER_END_CHARACTER)
                {
                    tokenBuilder.finishPlaceholder();
                    return PLAIN;
                } else if (character == PLACEHOLDER_METADATA_SEPARATOR)
                {
                    return STARTING_PLACEHOLDER_METADATA;
                }
                tokenBuilder.addCharacter(character);
                return PLACEHOLDER;
            }
        },

        STARTING_PLACEHOLDER_METADATA()
        {
            @Override
            State next(char character, TokenBuilder tokenBuilder)
            {
                if (character == PLACEHOLDER_METADATA_SEPARATOR)
                {
                    return PLACEHOLDER_METADATA;
                } else
                {
                    tokenBuilder.addCharacter(PLACEHOLDER_METADATA_SEPARATOR);
                    tokenBuilder.addCharacter(character);
                    return PLACEHOLDER;
                }
            }
        },

        PLACEHOLDER_METADATA()
        {
            @Override
            State next(char character, TokenBuilder tokenBuilder)
            {
                if (character == PLACEHOLDER_END_CHARACTER)
                {
                    tokenBuilder.finishPlaceholder();
                    return PLAIN;
                }
                return PLACEHOLDER_METADATA;
            }
        };

        abstract State next(char character, TokenBuilder tokenBuilder);
    }

    private static final class TokenBuilder
    {
        private final Map<String, VariableToken> variableTokens;

        private final List<IToken> tokens;

        private final StringBuilder builder;

        TokenBuilder(Map<String, VariableToken> variableTokens, List<IToken> tokens)
        {
            this.variableTokens = variableTokens;
            this.tokens = tokens;
            builder = new StringBuilder();
        }

        public void addCharacter(char character)
        {
            builder.append(character);
        }

        public void finishPlainToken()
        {
            if (builder.length() > 0)
            {
                tokens.add(new PlainToken(builder.toString()));
                builder.setLength(0);
            }
        }

        public void finishPlaceholder()
        {
            String variableName = builder.toString();
            if (variableName.length() == 0)
            {
                throw new IllegalArgumentException("Nameless placeholder " + createPlaceholder("")
                        + " found.");
            }
            VariableToken token = variableTokens.get(variableName);
            if (token == null)
            {
                token = new VariableToken(variableName);
                variableTokens.put(variableName, token);
            }
            tokens.add(token);
            builder.setLength(0);
        }
    }

    private final Map<String, VariableToken> variableTokens;

    private final List<IToken> tokens;

    /**
     * Creates a new instance for the specified template.
     * 
     * @throws IllegalArgumentException if some error occurred during parsing.
     */
    public Template(String template)
    {
        this(new LinkedHashMap<String, VariableToken>(), new ArrayList<IToken>());
        assert template != null : "Unspecified template.";

        TokenBuilder tokenBuilder = new TokenBuilder(variableTokens, tokens);
        State state = State.PLAIN;
        final int n = template.length();
        for (int i = 0; i < n; i++)
        {
            state = state.next(template.charAt(i), tokenBuilder);
        }
        if (state != State.PLAIN)
        {
            throw new IllegalArgumentException("Incomplete placeholder detected at the end.");
        }
        tokenBuilder.finishPlainToken();
    }

    private Template(Map<String, VariableToken> variableTokens, List<IToken> tokens)
    {
        this.variableTokens = variableTokens;
        this.tokens = tokens;
    }

    /**
     * Creates a copy of this template with no variable bindings.
     */
    public Template createFreshCopy()
    {
        LinkedHashMap<String, VariableToken> map = new LinkedHashMap<String, VariableToken>();
        for (VariableToken variableToken : variableTokens.values())
        {
            String variableName = variableToken.getVariableName();
            map.put(variableName, new VariableToken(variableName));
        }
        ArrayList<IToken> list = new ArrayList<IToken>();
        for (IToken token : tokens)
        {
            if (token instanceof VariableToken)
            {
                list.add(map.get(((VariableToken) token).getVariableName()));
            } else
            {
                list.add(token);
            }
        }
        return new Template(map, list);
    }

    /**
     * Returns all placeholder names.
     */
    public Set<String> getPlaceholderNames()
    {
        return variableTokens.keySet();
    }

    /**
     * Binds the specified value to the specified placeholder name.
     * 
     * @throws IllegalArgumentException if placeholder is not known.
     */
    public void bind(String placeholderName, String value)
    {
        boolean successful = attemptToBind(placeholderName, value);
        if (successful == false)
        {
            throw new IllegalArgumentException("Unknown variable '" + placeholderName + "'.");
        }
    }

    /**
     * Attempts to bind the specified value to the specified placeholder name.
     * 
     * @return <code>true</code> if successful.
     */
    public boolean attemptToBind(String placeholderName, String value)
    {
        assert placeholderName != null : "Unspecified placeholder name.";
        assert value != null : "Unspecified value for '" + placeholderName + "'";

        VariableToken variableToken = variableTokens.get(placeholderName);
        if (variableToken == null)
        {
            return false;
        }
        variableToken.bind(value);
        return true;
    }

    /**
     * Creates the text by using all placeholder bindings.
     * 
     * @throws IllegalStateException if not all placeholders have been bound.
     */
    public String createText()
    {
        return createText(true);
    }

    /**
     * Creates the text by using placeholder bindings.
     * 
     * @param complete If <code>true</code> an {@link IllegalStateException} will be thrown if not
     *            all bindings are set.
     */
    public String createText(boolean complete)
    {
        if (complete)
        {
            assertAllVariablesAreBound();
        }
        StringBuilder builder = new StringBuilder();
        for (IToken token : tokens)
        {
            token.appendTo(builder);
        }
        return builder.toString();
    }

    private void assertAllVariablesAreBound()
    {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, VariableToken> entry : variableTokens.entrySet())
        {
            if (entry.getValue().isBound() == false)
            {
                builder.append(entry.getKey()).append(' ');
            }
        }
        if (builder.length() > 0)
        {
            throw new IllegalStateException("The following variables are not bound: " + builder);
        }
    }

    /**
     * Returns <code>true</code> if all variables are bound, <code>false</code> otherwise.
     */
    public boolean allVariablesAreBound()
    {
        for (VariableToken value : variableTokens.values())
        {
            if (value.isBound() == false)
            {
                return false;
            }
        }
        return true;
    }
}
