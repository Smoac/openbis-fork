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

/**
 * Class which adds a prefix to a path. Which prefix is added depends on whether the path starts with '/'
 * (absolute path) or not (relative path).
 *
 * @author Franz-Josef Elmer
 */
public class PathPrefixPrepender
{
    private final String prefixForAbsolutePaths;
    private final String prefixForRelativePaths;

    /**
     * Creates an instances for the specified prefixes. <code>null</code> arguments are handled as empty strings.
     */
    public PathPrefixPrepender(String prefixForAbsolutePathsOrNull, String prefixForRelativePathsOrNull)
    {
        this.prefixForAbsolutePaths = prefixForAbsolutePathsOrNull == null ? "" : prefixForAbsolutePathsOrNull;
        this.prefixForRelativePaths = prefixForRelativePathsOrNull == null ? "" : prefixForRelativePathsOrNull;
    }

    /**
     * Returns the specified path with the appropriated prefix.
     */
    public String addPrefixTo(String path)
    {
        assert path != null : "Undefined path.";
        return (path.startsWith("/") ? prefixForAbsolutePaths : prefixForRelativePaths) + path;
    }
}
