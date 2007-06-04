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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

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
    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, FileUtilities.class);

    private FileUtilities()
    {
        // Can not be instantiated.
    }

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
    public static String loadToString(Class clazz, String resource) throws CheckedExceptionTunnel
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
    public static List<String> loadToStringList(Class clazz, String resource) throws CheckedExceptionTunnel
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
        }
    }

    private static BufferedReader getBufferedReader(Class clazz, String resource)
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
     * Checks whether a <var>directory</var> of some <var>kind</var> is fully accessible to the program.
     * 
     * @return <code>null</code> if the <var>directory</var> is fully accessible and an error message describing the
     *         problem with the <var>directory</var> otherwise.
     */
    public static String checkDirectoryFullyAccessible(File directory, String kindOfDirectory)
    {
        assert directory != null;

        if (directory.canRead() == false)
        {
            if (directory.exists() == false)
            {
                return String.format("%s directory '%s' does not exist.", StringUtilities.capitalize(kindOfDirectory),
                        directory.getPath());
            } else
            {
                return String.format("%s directory '%s' is not readable.", StringUtilities.capitalize(kindOfDirectory),
                        directory.getPath());
            }
        }
        if (directory.canWrite() == false)
        {
            return String.format("%s directory '%s' is not writable.", StringUtilities.capitalize(kindOfDirectory),
                    directory.getPath());
        }
        if (directory.isDirectory() == false)
        {
            return String.format("Path '%s' is supposed to be a %s directory, but is a file.", directory.getPath(),
                    kindOfDirectory);
        }
        return null;
    }

    /**
     * Deletes a directory recursively, that is deletes all files and directories within first and then the directory
     * itself.
     * <p>
     * Convenience method for {@link #deleteRecursively(File, Level)} with <var>Level</var> set to {@link Level#DEBUG}.
     * 
     * @param path Path of the file or directory to delete.
     * @return <code>true</code> if the path has been delete successfully, <code>false</code> otherwise.
     */
    public static boolean deleteRecursively(File path)
    {
        return deleteRecursively(path, Level.DEBUG);
    }

    /**
     * Deletes a directory recursively, that is deletes all files and directories within first and then the directory
     * itself.
     * 
     * @param path Path of the file or directory to delete.
     * @param logLevel The logLevel that should be used to log deletion of path entries.
     * @return <code>true</code> if the path has been delete successfully, <code>false</code> otherwise.
     */
    public static boolean deleteRecursively(File path, Level logLevel)
    {
        assert path != null;

        if (path.isDirectory())
        {
            for (File file : path.listFiles())
            {
                if (file.isDirectory())
                {
                    deleteRecursively(file, logLevel);
                } else
                {
                    if (machineLog.isEnabledFor(logLevel))
                    {
                        machineLog.log(logLevel, String.format("Deleting file '%s'", file.getPath()));
                    }
                    file.delete();
                }
            }
        }
        if (machineLog.isEnabledFor(logLevel))
        {
            machineLog.log(logLevel, String.format("Deleting directory '%s'", path.getPath()));
        }
        return path.delete();
    }

    /**
     * Deletes selected parts of a directory recursively, that is deletes all files and directories within the directory
     * that are accepted by the {@link FileFilter}. Any subdirectory that is accepted by the <var>filter</var> will be
     * completely deleted. This holds true also for the <var>path</var> itself.
     * <p>
     * Convenience method for {@link #deleteRecursively(File, FileFilter, Level)} with <var>Level</var> set to
     * {@link Level#DEBUG}.
     * 
     * @param path Path of the directory to delete the selected content from.
     * @param filter The {@link FileFilter} to use when deciding which paths to delete.
     * @return <code>true</code> if the <var>path</var> itself has been deleted.
     */
    public static boolean deleteRecursively(File path, FileFilter filter)
    {
        return deleteRecursively(path, filter, Level.DEBUG);
    }

    /**
     * Deletes selected parts of a directory recursively, that is deletes all files and directories within the directory
     * that are accepted by the {@link FileFilter}. Any subdirectory that is accepted by the <var>filter</var> will be
     * completely deleted. This holds true also for the <var>path</var> itself.
     * 
     * @param path Path of the directory to delete the selected content from.
     * @param filter The {@link FileFilter} to use when deciding which paths to delete.
     * @param logLevel The logLevel that should be used to log deletion of path entries.
     * @return <code>true</code> if the <var>path</var> itself has been deleted.
     */
    public static boolean deleteRecursively(File path, FileFilter filter, Level logLevel)
    {
        assert path != null;
        assert filter != null;
        assert logLevel != null;

        if (filter.accept(path))
        {
            return FileUtilities.deleteRecursively(path, logLevel);
        } else
        {
            if (path.isDirectory())
            {
                for (File file : path.listFiles())
                {
                    deleteRecursively(file, filter, logLevel);
                }
            }
            return false;
        }
    }

    /**
     * Moves <var>path</var> to <var>destinationDir</var>.
     * 
     * @see File#renameTo(File)
     * @param path The file or directory that will be moved.
     * @param destinationDir Directory to move the <var>path</var> to.
     * @return <code>true</code> if the <var>path</var> has been moved successfully, <code>false</code> otherwise.
     */
    public static boolean movePath(File path, File destinationDir)
    {
        assert path != null;
        assert destinationDir != null;

        if (machineLog.isTraceEnabled())
        {
            machineLog.trace(String.format("Moving path '%s' to '%s'", path.getPath(), destinationDir.getPath())
                    .toString());
        }
        return path.renameTo(new File(destinationDir, path.getName()));
    }

    private static final String PATH_LAST_CHANGED_TEMPLATE = "Path '%s' has last been changed at %2$tF %2$tT";

    /**
     * @return The time when any file below <var>directory</var> has last been changed in the file system.
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
        if (machineLog.isTraceEnabled())
        {
            machineLog.trace(String.format(PATH_LAST_CHANGED_TEMPLATE, path, lastChanged));
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

}
