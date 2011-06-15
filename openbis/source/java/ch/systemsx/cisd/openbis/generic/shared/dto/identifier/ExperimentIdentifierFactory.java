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

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * Parses the given text in the constructor to extract the database instance, group, project and
 * experiment code. The expected format is the following:
 * 
 * <pre>
 * [[&lt;database-instance-code&gt;:]/&lt;group-code&gt;/]&lt;project-code&gt;/&lt;experiment-code&gt;
 * </pre>
 * 
 * @author Basil Neff
 * @author Tomasz Pylak
 */
public final class ExperimentIdentifierFactory extends AbstractIdentifierFactory
{
    public ExperimentIdentifierFactory(final String textToParse)
    {
        super(textToParse);
    }

    public final ExperimentIdentifier createIdentifier() throws UserFailureException
    {
        return parse(getTextToParse());
    }

    public static ExperimentIdentifier parse(final String text)
    {
        final TokenLexer lexer = new TokenLexer(text);
        final ProjectIdentifier parentIdentifier = ProjectIdentifierFactory.parseIdentifier(lexer);
        final String code = assertValidCode(lexer.next());
        lexer.ensureNoTokensLeft();
        return create(parentIdentifier, code);
    }

    public static List<ExperimentIdentifier> parse(final List<String> texts) {
        List<ExperimentIdentifier> identifiers = new ArrayList<ExperimentIdentifier>();
        for (String identifierString : texts)
        {
            identifiers.add(ExperimentIdentifierFactory.parse(identifierString));
        }
        return identifiers;
    }

    private static ExperimentIdentifier create(final ProjectIdentifier parent, final String code)
    {
        return new ExperimentIdentifier(parent, code);
    }

    public static String getSchema()
    {
        return ProjectIdentifierFactory.getSchema()
                + DatabaseInstanceIdentifier.Constants.IDENTIFIER_SEPARATOR + "<experiment-code>";
    }

}
