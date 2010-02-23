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

package ch.systemsx.cisd.openbis.generic.shared.dto.identifier;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Parses the given text in the constructor to extract the database instance and group. The expected
 * format is the following:
 * 
 * <pre>
 * [&lt;database-instance-code&gt;:]/&lt;group-code&gt;
 * </pre>
 * 
 * @author Christian Ribeaud
 * @author Tomasz Pylak
 */
public final class GroupIdentifierFactory extends AbstractIdentifierFactory
{
    public GroupIdentifierFactory(final String textToParse) throws UserFailureException
    {
        super(textToParse);
    }

    public final GroupIdentifier createIdentifier() throws UserFailureException
    {
        return parseGroupIdentifier(getTextToParse());
    }

    private static GroupIdentifier parseGroupIdentifier(final String text)
    {
        final TokenLexer lexer = new TokenLexer(text);
        final GroupIdentifier groupIdentifier = parseIdentifier(lexer);
        lexer.ensureNoTokensLeft();
        return groupIdentifier;
    }

    public static GroupIdentifier parseIdentifier(final TokenLexer lexer)
    {
        final String firstToken = lexer.peek();
        final String dbCodeOrNull = tryAsDatabaseIdentifier(firstToken);
        if (dbCodeOrNull == null)
        {
            if (firstToken.length() > 0)
            {
                throw createSlashMissingExcp(lexer.getOriginalText());
            }
        }
        lexer.next();

        String groupCodeOrNull = lexer.next();
        if (groupCodeOrNull.length() == 0)
        {
            groupCodeOrNull = null; // if someone used "//CP" shortcut to reference home group
        } else
        {
            assertValidCode(groupCodeOrNull);
        }
        return new GroupIdentifier(dbCodeOrNull, groupCodeOrNull);
    }

    public static String getSchema()
    {
        return "[" + getDatabaseInstanceIdentifierSchema()
                + DatabaseInstanceIdentifier.Constants.DATABASE_INSTANCE_SEPARATOR + "]"
                + DatabaseInstanceIdentifier.Constants.IDENTIFIER_SEPARATOR + "<space-code>";
    }
}
