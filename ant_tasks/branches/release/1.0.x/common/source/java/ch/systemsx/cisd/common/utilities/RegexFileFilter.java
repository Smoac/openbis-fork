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

package ch.systemsx.cisd.common.utilities;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * A {@link FileFilter} based on a set of path patterns. A path pattern consists of a {@link PathType} and a regular
 * expression matching (the entire region of) the name of the path entry as returned by {@link File#getName()}. If any
 * path pattern of the set matches a path entry, the filter will accept the path entry.
 * 
 * @author Bernd Rinn
 */
public class RegexFileFilter implements FileFilter
{

    /** A type that complements the regular expression when matching paths. */
    public enum PathType
    {
        /** Only files will match. */
        FILE,
        /** Only directories will match. */
        DIRECTORY,
        /** Both files and directories will match. */
        ALL
    }

    /** A class specifying a pattern for files, combining a regular expression for the file name and a file type. */
    private static class PathPattern
    {
        private final PathType type;

        private final Pattern pattern;

        PathPattern(PathType type, Pattern pattern)
        {
            assert type != null;
            assert pattern != null;

            this.type = type;
            this.pattern = pattern;
        }

        /**
         * @return <code>true</code> if the <var>file</var> matches the this path pattern.
         */
        boolean matches(File file)
        {
            return matchesType(file) && pattern.matcher(file.getName()).matches();
        }

        private boolean matchesType(File file)
        {
            if (PathType.ALL.equals(type))
            {
                return true;
            }
            if (PathType.FILE.equals(type) && file.isFile())
            {
                return true;
            }
            if (PathType.DIRECTORY.equals(type) && file.isDirectory())
            {
                return true;
            }
            return false;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (obj instanceof PathPattern == false)
            {
                return false;
            }
            final PathPattern that = (PathPattern) obj;
            return this.type.equals(that.type) && this.pattern.pattern().equals(that.pattern.pattern());
        }

        @Override
        public int hashCode()
        {
            return (17 * 59 + type.hashCode()) * 59 + pattern.pattern().hashCode();
        }

    }

    private final Set<PathPattern> pathPatternSet = new HashSet<PathPattern>();

    public RegexFileFilter()
    {
    }

    /**
     * A convenience constructor that adds one path pattern to the set of patterns.
     * 
     * @see #add(PathType, String)
     */
    public RegexFileFilter(PathType type, String regex)
    {
        add(type, regex);
    }

    /**
     * Adds a path pattern to this filter. Multiple path pattern can be added by calling this method several times. If
     * any path pattern matches a file, the file will be accepted by this filter.
     * 
     * @param type The type of path that a path entry has to be of in order to be accepted by this filter.
     * @param regexPattern The regular expression pattern that the name of the file (as returned by
     *            {@link File#getName()} has to match in order to be accepted by this filter.
     */
    public void add(PathType type, String regexPattern)
    {
        assert regexPattern != null;

        add(type, Pattern.compile(regexPattern));
    }

    /**
     * Adds a path pattern to this filter. Multiple path pattern can be added by calling this method several times. If
     * any path pattern matches a file, the file will be accepted by this filter.
     * 
     * @param type The type of path that a path entry has to be of in order to be accepted by this filter.
     * @param regex The regular expression that the name of the file (as returned by {@link File#getName()} has to match
     *            in order to be accepted by this filter.
     */
    public void add(PathType type, Pattern regex)
    {
        assert type != null;
        assert regex != null;

        pathPatternSet.add(new PathPattern(type, regex));
    }

    public boolean accept(File pathname)
    {
        for (PathPattern pattern : pathPatternSet)
        {
            if (pattern.matches(pathname))
            {
                return true;
            }
        }
        return false;
    }

}
