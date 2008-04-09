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

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;

/**
 * Class which adds a prefix to a path. Which prefix is added depends on whether the path starts
 * with '/' (absolute path) or not (relative path).
 * 
 * @author Franz-Josef Elmer
 */
public final class PathPrefixPrepender
{
    private final String prefixForAbsolutePaths;

    private final String prefixForRelativePaths;

    /**
     * Creates an instances for the specified prefixes. <code>null</code> arguments are handled as
     * empty strings.
     */
    public PathPrefixPrepender(final String prefixForAbsolutePathsOrNull,
            final String prefixForRelativePathsOrNull) throws ConfigurationFailureException
    {
        this.prefixForAbsolutePaths = StringUtils.defaultString(prefixForAbsolutePathsOrNull);
        assertValid(this.prefixForAbsolutePaths, "absolute");
        this.prefixForRelativePaths = preparePrefix(prefixForRelativePathsOrNull);
        assertValid(this.prefixForRelativePaths, "relative");
    }

    private final static String preparePrefix(final String pathPrefix)
    {
        if (StringUtils.isEmpty(pathPrefix))
        {
            return "";
        }
        if (pathPrefix.endsWith("/"))
        {
            return pathPrefix;
        }
        return pathPrefix + "/";
    }

    private void assertValid(final String prefix, final String type)
            throws ConfigurationFailureException
    {
        if (prefix.length() != 0)
        {
            final File file = new File(prefix);
            if (file.exists() == false)
            {
                throw ConfigurationFailureException.fromTemplate(
                        "Invalid prefix for %s paths: given file '%s' does not exist.", type, file
                                .getAbsolutePath());
            }
        }
    }

    /**
     * Returns the specified path with the appropriated prefix.
     */
    public String addPrefixTo(final String path)
    {
        assert path != null : "Undefined path.";
        return (FilenameUtils.getPrefixLength(path) > 0 ? prefixForAbsolutePaths
                : prefixForRelativePaths)
                + path;
    }
}
