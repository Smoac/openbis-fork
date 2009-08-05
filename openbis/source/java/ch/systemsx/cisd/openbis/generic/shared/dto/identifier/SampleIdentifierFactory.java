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

import static ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING;

import org.apache.commons.lang.ArrayUtils;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.DatabaseInstanceIdentifier.Constants;

/**
 * Parses the given text in the constructor to extract the database instance, the group and the
 * sample code.
 * 
 * <pre>
 * [[&lt;database instance code&gt;:][/&lt;group code&gt;]/]&lt;sample code&gt;
 * </pre>
 * 
 * @author Tomasz Pylak
 */
public final class SampleIdentifierFactory extends AbstractIdentifierFactory
{
    public static final SampleIdentifier parse(final String textToParse)
            throws UserFailureException
    {
        return new SampleIdentifierFactory(textToParse).createIdentifier();
    }

    public static final SampleIdentifierPattern parsePattern(final String textToParse)
            throws UserFailureException
    {
        return new SampleIdentifierFactory(textToParse).createPattern();
    }

    public SampleIdentifierFactory(final String textToParse)
    {
        super(textToParse);
    }

    public final SampleIdentifier createIdentifier() throws UserFailureException
    {
        SampleIdentifierOrPattern ident = parse(getTextToParse(), false);
        return SampleIdentifier.createOwnedBy(ident.getOwner(), ident.getCode());
    }

    private SampleIdentifierPattern createPattern()
    {
        SampleIdentifierOrPattern ident = parse(getTextToParse(), true);
        return SampleIdentifierPattern.createOwnedBy(ident.getOwner(), ident.getCode());
    }

    private static class SampleIdentifierOrPattern
    {
        private final String code;

        private final SampleOwnerIdentifier owner;

        public SampleIdentifierOrPattern(String code, SampleOwnerIdentifier owner)
        {
            this.code = code;
            this.owner = owner;
        }

        public String getCode()
        {
            return code;
        }

        public SampleOwnerIdentifier getOwner()
        {
            return owner;
        }
    }

    private static SampleIdentifierOrPattern parse(String text, boolean isPattern)
    {
        String tokens[] = text.split(IDENTIFIER_SEPARARTOR_STRING);
        if (tokens.length == 0)
        {
            throw new UserFailureException(ILLEGAL_EMPTY_IDENTIFIER);
        }
        String sampleCode = tokens[tokens.length - 1];
        String[] ownerTokens = (String[]) ArrayUtils.subarray(tokens, 0, tokens.length - 1);
        SampleOwnerIdentifier owner = parseSampleOwner(ownerTokens, text);
        validateSampleCode(sampleCode, isPattern);
        return new SampleIdentifierOrPattern(sampleCode, owner);
    }

    private static void validateSampleCode(String sampleCode, boolean isPattern)
    {
        String[] sampleCodeTokens = sampleCode.split(CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING);
        if (sampleCodeTokens.length > 2)
        {
            throw UserFailureException.fromTemplate(
                    AbstractIdentifierFactory.ILLEGAL_CODE_CHARACTERS_TEMPLATE, sampleCode);
        }
        for (String token : sampleCodeTokens)
        {
            validateSampleCodeToken(token, isPattern);
        }
    }

    private static void validateSampleCodeToken(String token, boolean isPattern)
    {
        if (isPattern)
        {
            assertValidPatternCharacters(token);
        } else
        {
            assertValidCode(token);
        }
    }

    private static SampleOwnerIdentifier parseSampleOwner(String[] tokens, String originalText)
    {
        if (tokens.length == 0)
        {
            // case: originalText is e.g. "CP1"
            return new SampleOwnerIdentifier(GroupIdentifier.createHome());
        }
        String firstToken = tokens[0];
        if (firstToken.length() == 0)
        {
            // identifier starts with a slash - it refers to a home database
            if (tokens.length == 2 && tokens[1].length() == 0)
            {
                // case: shortcut to home group, originalText is e.g. "//CP1"
                return new SampleOwnerIdentifier(GroupIdentifier.createHome());
            }
            DatabaseInstanceIdentifier homeDb = DatabaseInstanceIdentifier.createHome();
            return continueParsingSampleOwner(tokens, originalText, homeDb);
        } else
        // identifier does not start with a slash
        {
            String dbCode = tryAsDatabaseIdentifier(firstToken);
            if (dbCode != null)
            {
                DatabaseInstanceIdentifier dbIdent = new DatabaseInstanceIdentifier(dbCode);
                return continueParsingSampleOwner(tokens, originalText, dbIdent);
            } else
            {
                throw createSlashMissingExcp(originalText);
            }
        }
    }

    // tries to parse owner group if there is any
    private static SampleOwnerIdentifier continueParsingSampleOwner(String[] tokens,
            String originalText, DatabaseInstanceIdentifier dbIdent)
    {
        if (tokens.length == 1)
        {
            // case: originalText is e.g. "db:/CP1" or "/CP1"
            return new SampleOwnerIdentifier(dbIdent);
        } else if (tokens.length == 2)
        {
            // case: originalText is e.g. "db:/group/CP1" or "/group/CP1"
            String groupCode = tokens[1];
            return new SampleOwnerIdentifier(new GroupIdentifier(dbIdent,
                    assertValidCode(groupCode)));
        } else
            throw createTooManyTokensExcp(originalText);
    }

    public static String getOwnerSchema()
    {
        return "[" + "[" + getDatabaseInstanceIdentifierSchema()
                + Constants.DATABASE_INSTANCE_SEPARATOR + "][" + Constants.IDENTIFIER_SEPARATOR
                + "<group-code>]" + Constants.IDENTIFIER_SEPARATOR + "]";
    }
}
