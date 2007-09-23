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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.ISimpleLogger;

/**
 * Some useful utility methods for files and directories.
 * <p>
 * Note that these utilities are considered to be <i>internal</i> methods, that means they are not prepared to do the
 * error checking. If you hand in inappropriate values, e.g. <code>null</code>, all you will get are
 * {@link AssertionError}s or {@link NullPointerException}.
 * <p>
 * If you are tempted to add new functionality to this class, ensure that the new functionality does not yet exist in
 * <code>org.apache.common.io.FileUtils</code>, see <a
 * href="http://jakarta.apache.org/commons/io/api-release/org/apache/commons/io/FileUtils.html">javadoc</a>.
 * 
 * @author Bernd Rinn
 */
public final class FileUtilities
{
    private FileUtilities()
    {
        // Can not be instantiated.
    }

    /**
     * A file filter that accepts all entries.
     */
    public static final FileFilter ACCEPT_ALL_FILTER = new FileFilter()
    {
        public boolean accept(File pathname)
        {
            return true;
        }
    };


    
    /**
     * Loads a text file to a {@link String}.
     * 
     * @param file the file that should be loaded. This method asserts that given <code>File</code> is not
     *            <code>null</code>.
     * @return The content of the file. All newline characters are '\n' (Unix convention).
     * @throws CheckedExceptionTunnel for wrapping an {@link IOException}, e.g. if the file does not exist.
     */
    public static String loadToString(File file) throws CheckedExceptionTunnel
    {
        assert file != null;

        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader(file);
            return readString(new BufferedReader(fileReader));
        } catch (IOException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        } finally
        {
            IOUtils.closeQuietly(fileReader);
        }
    }

    /**
     * Loads a text file line by line to a {@link List} of {@link String}s.
     * 
     * @param file the file that should be loaded. This method asserts that given <code>File</code> is not
     *            <code>null</code>.
     * @return The content of the file line by line.
     * @throws CheckedExceptionTunnel for wrapping an {@link IOException}, e.g. if the file does not exist.
     */
    public static List<String> loadToStringList(File file) throws CheckedExceptionTunnel
    {
        assert file != null;

        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader(file);
            return readStringList(new BufferedReader(fileReader));
        } catch (IOException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        } finally
        {
            IOUtils.closeQuietly(fileReader);
        }
    }

    /**
     * Loads a resource to a string.
     * <p>
     * A non-existent resource will result in a return value of <code>null</code>.
     * 
     * @param clazz Class for which <code>getResourceAsStream()</code> will be invoked (must not be <code>null</code>).
     * @param resource Absolute path of the resource (will be the argument of <code>getResourceAsStream()</code>).
     * @return The content of the resource, or <code>null</code> if the specified resource does not exist.
     * @throws CheckedExceptionTunnel for wrapping an {@link IOException}
     */
    public static String loadToString(Class<?> clazz, String resource) throws CheckedExceptionTunnel
    {
        assert clazz != null;
        assert resource != null && resource.length() > 0;

        final BufferedReader reader = getBufferedReader(clazz, resource);
        try
        {
            return readString(reader);
        } catch (IOException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    /**
     * Loads a text file line by line to a {@link List} of {@link String}s.
     * <p>
     * A non-existent resource will result in a return value of <code>null</code>.
     * 
     * @param clazz Class for which <code>getResourceAsStream()</code> will be invoked.
     * @param resource Absolute path of the resource (will be the argument of <code>getResourceAsStream()</code>).
     * @return The content of the resource line by line.
     * @throws CheckedExceptionTunnel for wrapping an {@link IOException}, e.g. if the file does not exist.
     */
    public static List<String> loadToStringList(Class<?> clazz, String resource) throws CheckedExceptionTunnel
    {
        assert clazz != null;
        assert resource != null && resource.length() > 0;

        final BufferedReader reader = getBufferedReader(clazz, resource);
        try
        {
            return readStringList(reader);
        } catch (IOException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        } finally
        {
            IOUtils.closeQuietly(reader);
        }
    }

    private static BufferedReader getBufferedReader(Class<?> clazz, String resource)
    {
        final InputStream stream = clazz.getResourceAsStream(resource);
        if (stream == null)
        {
            return null;
        }
        return new BufferedReader(new InputStreamReader(stream));
    }

    private static String readString(BufferedReader reader) throws IOException
    {
        if (reader == null)
        {
            return null;
        }

        final StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
        {
            builder.append(line).append('\n');
        }
        return builder.toString();
    }

    private static List<String> readStringList(BufferedReader reader) throws IOException
    {
        if (reader == null)
        {
            return null;
        }

        final List<String> list = new ArrayList<String>();
        String line;
        while ((line = reader.readLine()) != null)
        {
            list.add(line);
        }
        return list;
    }

    /**
     * Checks whether a <var>path</var> of some <var>kind</var> is fully accessible to the program.
     * 
     * @param kindOfPath description of given <var>path</var>. Mainly used for error messages.
     * @return <code>null</code> if the <var>directory</var> is fully accessible and an error message describing the
     *         problem with the <var>directory</var> otherwise.
     */
    public static String checkPathFullyAccessible(File path, String kindOfPath)
    {
        assert path != null;
        assert kindOfPath != null;

        return checkPathFullyAccessible(path, kindOfPath, "path");
    }

    /**
     * Checks whether a <var>directory</var> of some <var>kind</var> is fully accessible to the program.
     * 
     * @return <code>null</code> if the <var>directory</var> is fully accessible and an error message describing the
     *         problem with the <var>directory</var> otherwise.
     */
    public static String checkDirectoryFullyAccessible(File directory, String kindOfDirectory)
    {
        assert directory != null;
        assert kindOfDirectory != null;

        final String msg = checkPathFullyAccessible(directory, kindOfDirectory, "directory");
        if (msg == null && directory.isDirectory() == false)
        {
            return String.format("Path '%s' is supposed to be a %s directory, but is a file.", directory.getPath(),
                    kindOfDirectory);
        }
        return msg;
    }

    private static String checkPathFullyAccessible(File path, String kindOfPath, String directoryOrFile)
    {
        assert path != null;
        assert kindOfPath != null;
        assert directoryOrFile != null;

        if (path.canRead() == false)
        {
            if (path.exists() == false)
            {
                return String.format("%s %s '%s' does not exist.", StringUtilities.capitalize(kindOfPath),
                        directoryOrFile, path.getPath());
            } else
            {
                return String.format("%s %s '%s' is not readable.", StringUtilities.capitalize(kindOfPath),
                        directoryOrFile, path.getPath());
            }
        }
        if (path.canWrite() == false)
        {
            return String.format("%s directory '%s' is not writable.", StringUtilities.capitalize(kindOfPath), path
                    .getPath());
        }
        return null;
    }

    /**
     * Deletes a directory recursively, that is deletes all files and directories within first and then the directory
     * itself.
     * <p>
     * Convenience method for {@link #deleteRecursively(File)} with <var>logger</var> set to <code>null</code>.
     * 
     * @param path Path of the file or directory to delete.
     * @return <code>true</code> if the path has been delete successfully, <code>false</code> otherwise.
     */
    public static boolean deleteRecursively(File path)
    {
        assert path != null;

        return deleteRecursively(path, null);
    }

    /**
     * Deletes a directory recursively, that is deletes all files and directories within first and then the directory
     * itself.
     * 
     * @param path Path of the file or directory to delete.
     * @param logger The logger that should be used to log deletion of path entries, or <code>null</code> if nothing
     *            should be logged.
     * @return <code>true</code> if the path has been delete successfully, <code>false</code> otherwise.
     */
    public static boolean deleteRecursively(File path, ISimpleLogger logger)
    {
        assert path != null;

        if (path.isDirectory())
        {
            for (File file : path.listFiles())
            {
                if (file.isDirectory())
                {
                    deleteRecursively(file, logger);
                } else
                {
                    if (logger != null)
                    {
                        logger.log("Deleting file '%s'", file.getPath());
                    }
                    file.delete();
                }
            }
        }
        if (logger != null)
        {
            logger.log("Deleting directory '%s'", path.getPath());
        }
        return path.delete();
    }

    /**
     * Deletes selected parts of a directory recursively, that is deletes all files and directories within the directory
     * that are accepted by the {@link FileFilter}. Any subdirectory that is accepted by the <var>filter</var> will be
     * completely deleted. This holds true also for the <var>path</var> itself.
     * 
     * @param path Path of the directory to delete the selected content from.
     * @param filter The {@link FileFilter} to use when deciding which paths to delete.
     * @param logger The logger that should be used to log deletion of path entries, or <code>null</code> if nothing
     *            should be logged.
     * @return <code>true</code> if the <var>path</var> itself has been deleted.
     */
    public static boolean deleteRecursively(File path, FileFilter filter, ISimpleLogger logger)
    {
        assert path != null;
        assert filter != null;

        if (filter.accept(path))
        {
            return FileUtilities.deleteRecursively(path, logger);
        } else
        {
            if (path.isDirectory())
            {
                for (File file : path.listFiles())
                {
                    deleteRecursively(file, filter, logger);
                }
            }
            return false;
        }
    }

    /**
     * @return The time when any file in (or below) <var>path</var> has last been changed in the file system.
     * @throws CheckedExceptionTunnel of an {@link IOException} if the <var>path</var> does not exist or is not
     *             readable.
     */
    public static long lastChanged(File path)
    {
        assert path != null;

        if (path.canRead() == false)
        {
            throw new CheckedExceptionTunnel(
                    new IOException(String.format("Path '%s' cannot be read.", path.getPath())));
        }

        long lastChanged = path.lastModified();
        if (path.isDirectory())
        {
            for (File subDirectory : getSubDirectories(path))
            {
                lastChanged = Math.max(lastChanged, lastChanged(subDirectory));
            }
        }
        return lastChanged;
    }

    private static File[] getSubDirectories(File superDirectory)
    {
        assert superDirectory.canRead() && superDirectory.isDirectory();

        return superDirectory.listFiles(new FileFilter()
            {

                public boolean accept(File pathname)
                {
                    return pathname.isDirectory();
                }

            });
    }

    /**
     * Removes given <var>prefix</var> from given <var>file</var> and returns a new <code>File</code>.
     * <p>
     * Returns given <var>file</var> if prefix is <i>empty</i> or could not be found in the file name.
     * </p>
     * 
     * @param file can not be <code>null</code>.
     * @param prefix prefix that should be removed from the file name. Can be <code>null</code>.
     */
    public final static File removePrefixFromFileName(File file, String prefix)
    {
        assert file != null;
        String name = file.getName();
        if (StringUtils.isEmpty(prefix))
        {
            return file;
        }
        if (name.indexOf(prefix) < 0)
        {
            return file;
        }
        return new File(file.getParent(), name.substring(prefix.length()));
    }

    /** A <i>Java</i> pattern matching one or more digits. */
    private final static Pattern ONE_OR_MORE_DIGITS = Pattern.compile("(\\d+)");

    public final static File createNextNumberedFile(File path, Pattern regex)
    {
        return createNextNumberedFile(path, regex, null);
    }

    /**
     * Creates the next numbered file if given <var>path</var> does already exist.
     * <p>
     * If the new suggested file already exists, then this method is called recursively.
     * </p>
     * 
     * @param defaultFileName the default name for the new file if the digit pattern could not be found in its name. If
     *            empty then "1" will be appended to its name.
     * @param regex pattern to find out the counter. If <code>null</code> then a default (<code>(\\d+)</code>)
     *            will be used. The given <var>regex</var> must contain <code>(\\d+)</code> or <code>([0-9]+)</code>.
     */
    public final static File createNextNumberedFile(File path, Pattern regex, String defaultFileName)
    {
        assert path != null;
        if (path.exists() == false)
        {
            return path;
        }
        final Pattern pattern;
        if (regex == null)
        {
            pattern = ONE_OR_MORE_DIGITS;
        } else
        {
            pattern = regex;
        }
        assert pattern.pattern().indexOf("(\\d+)") > -1 || pattern.pattern().indexOf("([0-9]+)") > -1;

        String pathName = path.getName();
        final Matcher matcher = pattern.matcher(pathName);
        boolean found = matcher.find();
        if (found == false)
        {
            final String fileName;
            if (StringUtils.isEmpty(defaultFileName) == false)
            {
                fileName = defaultFileName;
            } else
            {
                fileName = pathName + "1";
            }
            return createNextNumberedFile(new File(path.getParent(), fileName), pattern, defaultFileName);
        }
        StringBuilder builder = new StringBuilder();
        int nextStart = 0;
        while (found)
        {
            String group = matcher.group(1);
            final int newNumber = Integer.parseInt(group) + 1;
            builder.append(pathName.substring(nextStart, matcher.start(1))).append(newNumber);
            nextStart = matcher.end(1);
            found = matcher.find();
        }
        builder.append(pathName.substring(nextStart));
        File newFile = new File(path.getParent(), builder.toString());
        if (newFile.exists())
        {
            return createNextNumberedFile(newFile, pattern, defaultFileName);
        }
        return newFile;
    }

    /**
     * For given <var>root</var> and <var>file</var> extracts the relative <code>File</code>.
     * <p>
     * If given <var>file</var> does not contain given <var>root</var> path in its absolute path, then returns
     * <code>null</code> (as the relative file could not be determined).
     * </p>
     */
    public final static File getRelativeFile(File root, File file)
    {
        assert file != null;
        if (root == null)
        {
            return file;
        }
        String absolutePath = root.getAbsolutePath();
        final String strRoot = absolutePath + File.separator;
        final String absoluteFile = file.getAbsolutePath();
        if (absoluteFile.startsWith(strRoot))
        {
            return new File(absoluteFile.substring(strRoot.length()));
        } else
        {
            return null;
        }
    }

    /**
     * Lists all resources in a given directory which match the filter.
     * 
     * @param directory the directory to list
     * @param filter only files matching this filter will show up in the result
     * @param loggerOrNull logger, if <code>null</code> than no logging occurs
     * @return all files in <var>directory</var> that match the filter, or <code>null</code>, if <var>directory</var>
     *         does not exist or is not a directory.
     */
    public static File[] tryListFiles(File directory, FileFilter filter, ISimpleLogger loggerOrNull)
    {
        File[] paths = null;
        RuntimeException ex = null;
        try
        {
            paths = directory.listFiles(filter);
        } catch (RuntimeException e)
        {
            ex = e;
        }
        if (paths == null && loggerOrNull != null)
        {
            logFailureInDirectoryListing(ex, directory, loggerOrNull);
        }
        return paths;
    }

    private static void logFailureInDirectoryListing(RuntimeException exOrNull, File directory, ISimpleLogger logger)
    {
        if (exOrNull == null)
        {
            if (directory.isFile())
            {
                logger.log(String.format(
                        "Failed to get listing of directory '%s' (path is file instead of directory).", directory));
            } else
            {
                logger.log(String.format("Failed to get listing of directory '%s' (path not found).", directory));
            }
        } else
        {
            StringWriter exStackWriter = new StringWriter();
            exOrNull.printStackTrace(new PrintWriter(exStackWriter));
            logger.log(String.format("Failed to get listing of directory '%s'. Exception: %s", directory, exStackWriter
                    .toString()));
        }
    }

    /**
     * Copies the resource with the given name to a temporary file.
     * 
     * @param resource The name of the resource to copy.
     * @param prefix The prefix to use for the temporary name.
     * @param postfix The postfix to use for the temporary name.
     * @return The name of the temporary file.
     * @throws IllegalArgumentException If the resource cannot be found in the class path.
     * @throws CheckedExceptionTunnel If an {@link IOException} occurs.
     */
    public final static String copyResourceToTempFile(String resource, String prefix, String postfix)
    {
        final InputStream resourceStream = FileUtilities.class.getResourceAsStream(resource);
        if (resourceStream == null)
        {
            throw new IllegalArgumentException("Resource '" + resource + "' not found.");
        }
        try
        {
            final File tempFile = File.createTempFile(prefix, postfix);
            tempFile.deleteOnExit();
            OutputStream fileStream = new FileOutputStream(tempFile);
            try
            {
                IOUtils.copy(resourceStream, fileStream);
            } finally
            {
                IOUtils.closeQuietly(fileStream);
            }
            return tempFile.getAbsolutePath();
        } catch (IOException ex)
        {
            throw new CheckedExceptionTunnel(ex);
        } finally
        {
            IOUtils.closeQuietly(resourceStream);
        }
    }

    /**
     * Tries to copy the resource with the given name to a temporary file.
     * 
     * @param resource The name of the resource to copy.
     * @param prefix The prefix to use for the temporary name.
     * @param postfix The postfix to use for the temporary name.
     * @return The name of the temporary file, or <code>null</code>, if the resource could not be copied.
     */
    public final static String tryCopyResourceToTempFile(String resource, String prefix, String postfix)
    {
        try
        {
            return copyResourceToTempFile(resource, prefix, postfix);
        } catch (Exception ex)
        {
            return null;
        }
    }
}
