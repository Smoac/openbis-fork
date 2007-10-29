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
 * Context of {@link IFormattedData}. Argument of all constructores of concrete implementations of
 * {@link IFormattedData}. 
 *
 * @author Franz-Josef Elmer
 */
class FormattedDataContext
{
    private final IDirectory dataDirectory;
    private final Format format;
    private final IFormatParameters formatParameters;

    FormattedDataContext(IDirectory dataDirectory, Format format, IFormatParameters formatParameters)
    {
        assert dataDirectory != null : "Unspecified data directory.";
        this.dataDirectory = dataDirectory;
        assert format != null : "Unspecified format.";
        this.format = format;
        assert formatParameters != null : "Unspecified format parameters.";
        this.formatParameters = formatParameters;
    }

    final IDirectory getDataDirectory()
    {
        return dataDirectory;
    }

    final Format getFormat()
    {
        return format;
    }

    final IFormatParameters getFormatParameters()
    {
        return formatParameters;
    }
}
