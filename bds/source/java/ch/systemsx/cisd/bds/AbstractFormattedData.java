/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.bds;

import ch.systemsx.cisd.bds.storage.IDirectory;

/**
 * Common code of implementations of {@link IFormattedData}.
 * 
 * @author Franz-Josef Elmer
 */
public abstract class AbstractFormattedData implements IFormattedData
{
    protected final IDirectory dataDirectory;

    protected final Format format;

    private final IFormatParameters formatParameters;

    protected AbstractFormattedData(final FormattedDataContext context)
    {
        assert context != null : "Unspecified context.";
        dataDirectory = context.getDataDirectory();
        format = context.getFormat();
        formatParameters = context.getFormatParameters();
        assertValidFormatAndFormatParameters();
    }

    /**
     * Asserts valid format and format parameters. Will be called at the end of the constructor. The format of the
     * {@link FormattedDataContext} will be available by the protected attribute {@link #format}. The format parameters
     * will be available by {@link #getFormatParameters()}.
     * 
     * @throws DataStructureException if format of format parameters are invalid.
     */
    protected void assertValidFormatAndFormatParameters()
    {
        final String formatCode = getFormat().getCode();
        if (format.getCode().equals(formatCode) == false)
        {
            throw new DataStructureException(String.format("Format codes do not match: '%s' versus '%s'.", format
                    .getCode(), formatCode));
        }
        final Version formatVersion = getFormat().getVersion();
        if (format.getVersion().isBackwardsCompatibleWith(formatVersion) == false)
        {
            throw new DataStructureException(String.format(
                    "Version '%s' is not backwards compatible with version '%s'.", format.getVersion(), formatVersion));
        }
    }

    //
    // IFormattedData
    //

    public final IFormatParameters getFormatParameters()
    {
        return formatParameters;
    }

}
