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
 * Parses the given text in the constructor to extract the database instance, the group and the
 * project code.
 * 
 * <pre>
 * [[&lt;database-instance-code&gt;:]/&lt;group-code&gt;/]&lt;project-code&gt;
 * </pre>
 * 
 * @author Christian Ribeaud
 * @author Tomasz Pylak
 */
public final class ProjectIdentifierFactory extends AbstractIdentifierFactory
{

    public ProjectIdentifierFactory(final String textToParse)
    {
        super(textToParse);
    }

    public final ProjectIdentifier createIdentifier() throws UserFailureException
    {
        return parseProjectIdentifier(getTextToParse());
    }

    private static ProjectIdentifier parseProjectIdentifier(final String text)
    {
        final TokenLexer lexer = new TokenLexer(text);
        final ProjectIdentifier projectIdentifier = parseIdentifier(lexer);
        lexer.ensureNoTokensLeft();
        return projectIdentifier;
    }

    public static ProjectIdentifier parseIdentifier(final TokenLexer lexer)
    {
        final GroupIdentifier groupIdentifier = parseGroup(lexer);
        final String projectCode = assertValidCode(lexer.next());
        return create(groupIdentifier, projectCode);
    }

    private static GroupIdentifier parseGroup(final TokenLexer lexer)
    {
        final String firstToken = lexer.peek();
        if (tryAsDatabaseIdentifier(firstToken) == null && firstToken.length() > 0)
        {
            return GroupIdentifier.createHome();
        } else
        {
            return GroupIdentifierFactory.parseIdentifier(lexer);
        }
    }

    private static ProjectIdentifier create(final GroupIdentifier groupIdentifier,
            final String projectCode)
    {
        return new ProjectIdentifier(groupIdentifier.getDatabaseInstanceCode(), groupIdentifier
                .getSpaceCode(), projectCode);
    }

    public static String getSchema()
    {
        return "[" + GroupIdentifierFactory.getSchema()
                + DatabaseInstanceIdentifier.Constants.IDENTIFIER_SEPARATOR + "]"
                + "<project-code>";
    }
}
