/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.filesystem.FileUtilities;

/**
 * Utility class defining methods related to auto resolving data sets.
 * 
 * @author Izabela Adamczyk
 */
public class AutoResolveUtils
{

    /**
     * Returns the files from root/path directory with canonical path matching given pattern.
     * <p>
     * Recursive search is stopped after:
     * <li>at least 2 files have been found
     * <li>all directories have been checked
     * </p>
     * 
     * @param root - the search will start from this directory
     * @param path - narrows the search to this directory; relative to the root
     * @param pattern - regular expression defining the files that will be accepted; if no pattern
     *            is specified - the result will be empty
     * @return empty list - if the pattern has not been specified or no files matched; 1 file - if
     *         there exactly one file matched; 2 or more files (not necessarily all) - if more than
     *         one file matched
     */
    public static List<File> findSomeMatchingFiles(File root, String path, final String pattern)
    {
        if (StringUtils.isBlank(pattern))
        {
            return new ArrayList<File>();
        } else
        {
            List<File> result = new ArrayList<File>();
            findFiles(createStartingPoint(root, path), createCanonicalPathMatchingFilter(pattern),
                    result);
            return result;
        }
    }

    public static File tryGetTheOnlyMatchingFileOrDir(File root, String pattern)
    {
        if (root.isDirectory())
        {
            if (continueAutoResolving(pattern, root))
            {
                return tryGetTheOnlyMatchingFileOrDir(root.listFiles()[0], pattern);
            } else
            {
                return root;
            }
        } else if (root.isFile() && acceptFile(pattern, root))
        {
            return root;
        } else
        {
            return null;
        }
    }

    /**
     * Returns {@link FileFilter} accepting files with canonical path matching the pattern.
     * 
     * @param pattern - pattern to be matched; no files will be accepted if the pattern is empty
     */
    private static FileFilter createCanonicalPathMatchingFilter(final String pattern)
    {
        FileFilter filter = new FileFilter()
            {
                public boolean accept(File pathname)
                {
                    return acceptFile(pattern, pathname);
                }

            };
        return filter;
    }

    /**
     * Accepts regular files matching pattern and nothing if pattern is empty.
     */
    static private boolean acceptFile(final String pattern, File file)
    {
        if (StringUtils.isBlank(pattern) || file.isFile() == false)
        {
            return false;
        }
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(FileUtilities.getCanonicalPath(file));
        return m.find();
    }

    /**
     * For given directory and main data set pattern decides if the auto resolving should stop or
     * continue.
     * <p>
     * The resolving should continue if file has only one child and
     * <li>it is a directory; or
     * <li>it matches given pattern
     * </p>
     */
    public static boolean continueAutoResolving(String mainDataSetPattern, File file)
    {
        assert file.isDirectory();
        return file.listFiles().length == 1
                && (file.listFiles()[0].isDirectory() || acceptFile(mainDataSetPattern, file
                        .listFiles()[0]));
    }

    /**
     * Recursively browses startingPoint looking for files accepted by the filter. Stops if more
     * than one file has been already found.
     */
    private static void findFiles(File startingPoint, FileFilter filter, List<File> result)
    {
        if (result.size() > 1)
        {
            return;
        } else
        {
            for (File f : startingPoint.listFiles(filter))
            {
                result.add(f);
            }
            for (File d : startingPoint.listFiles())
            {
                if (d.isDirectory())
                {
                    findFiles(d, filter, result);
                }
            }
        }
    }

    /**
     * Returns the directory defined by root and given relative path. If path is not defined or the
     * result file does not exist or is not a directory, root is returned.
     */
    private static File createStartingPoint(File root, String path)
    {
        File startingPoint = root;
        if (StringUtils.isBlank(path) == false)
        {
            File tmp = new File(root, path);
            if (tmp.exists() && tmp.isDirectory())
            {
                startingPoint = tmp;
            }
        }
        return startingPoint;
    }

}
