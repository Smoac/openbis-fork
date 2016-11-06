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

package ch.systemsx.cisd.common.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.CharUtils;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.base.unix.Unix;
import ch.systemsx.cisd.common.concurrent.IActivityObserver;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;

/**
 * Some useful utility methods for files and directories.
 * <p>
 * Note that these utilities are considered to be <i>internal</i> methods, that means they are not prepared to do the error checking. If you hand in
 * inappropriate values, e.g. <code>null</code>, all you will get are {@link AssertionError}s or {@link NullPointerException}.
 * <p>
 * If you are tempted to add new functionality to this class, ensure that the new functionality does not yet exist in
 * <code>org.apache.common.io.FileUtils</code>, see <a
 * href="https://commons.apache.org/proper/commons-io/apidocs/org/apache/commons/io/FileUtils.html" >javadoc</a>.
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
     * Loads a text file to a {@link String}.
     * 
     * @param file the file that should be loaded. This method asserts that given <code>File</code> is not <code>null</code>.
     * @return The content of the file. All newline characters are '\n' (Unix convention). Never returns <code>null</code>.
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}, e.g. if the file does not exist.
     */
    public static String loadToString(final File file) throws IOExceptionUnchecked
    {
        assert file != null;

        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader(file);
            return readString(new BufferedReader(fileReader));
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(fileReader);
        }
    }

    private static String readString(final BufferedReader reader) throws IOException
    {
        assert reader != null : "Unspecified BufferedReader.";
        final StringBuilder builder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null)
        {
            builder.append(line).append(CharUtils.LF);
        }
        return builder.toString();
    }

    /**
     * Writes the specified string to the specified file.
     * 
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}.
     */
    public static void writeToFile(final File file, final String str) throws IOExceptionUnchecked
    {
        assert file != null : "Unspecified file.";
        assert str != null : "Unspecified string.";

        FileWriter fileWriter = null;
        try
        {
            fileWriter = new FileWriter(file);
            fileWriter.write(str);
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(fileWriter);
        }
    }

    /**
     * Loads a text file to a {@link String}. Doesn't append new line at the end.
     * 
     * @param file the file that should be loaded. This method asserts that given <code>File</code> is not <code>null</code>.
     * @return The content of the file. All newline characters are '\n' (Unix convention). Never returns <code>null</code>.
     * @throws IOExceptionUnchecked for wrapping an {@link IOException}, e.g. if the file does not exist.
     */
    public static String loadExactToString(final File file) throws IOExceptionUnchecked
    {
        assert file != null;

        FileReader fileReader = null;
        try
        {
            fileReader = new FileReader(file);
            return readExactString(new BufferedReader(fileReader));
        } catch (final IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        } finally
        {
            IOUtils.closeQuietly(fileReader);
        }
    }

    private static String readExactString(final BufferedReader reader) throws IOException
    {
        assert reader != null : "Unspecified BufferedReader.";
        final StringBuilder builder = new StringBuilder();
        int numRead = 0;
        while ((numRead = reader.read()) != -1)
        {
            builder.append(String.copyValueOf(Character.toChars(numRead)));
        }
        reader.close();
        return builder.toString();
    }

    /**
     * Deletes a directory recursively, that is deletes all files and directories within first and then the directory itself.
     * <p>
     * Convenience method for {@link #deleteRecursively(File)} with <var>logger</var> set to <code>null</code>.
     * 
     * @param path Path of the file or directory to delete.
     * @return <code>true</code> if the path has been deleted successfully, <code>false</code> otherwise.
     * @throws InterruptedExceptionUnchecked If the current thread has been interrupted.
     */
    public static boolean deleteRecursively(final File path) throws InterruptedExceptionUnchecked
    {
        assert path != null;

        return deleteRecursively(path, (ISimpleLogger) null, null);
    }

    /**
     * Deletes a directory recursively, that is deletes all files and directories within first and then the directory itself.
     * 
     * @param path Path of the file or directory to delete.
     * @param loggerOrNull The logger that should be used to log deletion of path entries, or <code>null</code> if nothing should be logged.
     * @return <code>true</code> if the path has been deleted successfully, <code>false</code> otherwise.
     * @throws InterruptedExceptionUnchecked If the current thread has been interrupted.
     */
    public static boolean deleteRecursively(final File path, final ISimpleLogger loggerOrNull)
            throws InterruptedExceptionUnchecked
    {
        return deleteRecursively(path, loggerOrNull, null);
    }

    /**
     * Deletes a directory recursively, that is deletes all files and directories within first and then the directory itself.
     * 
     * @param path Path of the file or directory to delete.
     * @param loggerOrNull The logger that should be used to log deletion of path entries, or <code>null</code> if nothing should be logged.
     * @param observerOrNull If not <code>null</code>, will be updated on progress in the deletion. This can be used to find out whether a
     *            (potentially) long-running recursive deletion call is alive-and-kicking or hangs (e.g. due to a remote directory becoming
     *            unresponsive).
     * @return <code>true</code> if the path has been deleted successfully, <code>false</code> otherwise.
     * @throws InterruptedExceptionUnchecked If the current thread has been interrupted.
     */
    public static boolean deleteRecursively(final File path, final ISimpleLogger loggerOrNull,
            final IActivityObserver observerOrNull) throws InterruptedExceptionUnchecked
    {
        assert path != null;

        if (path.isDirectory())
        {
            if (isSymbolicLink(path))
            {
                return deleteSymbolicLink(path, loggerOrNull);
            }
            ensureWritable(path);
            for (final File file : path.listFiles())
            {
                if (Thread.interrupted())
                {
                    throw new InterruptedExceptionUnchecked();
                }
                if (observerOrNull != null)
                {
                    observerOrNull.update();
                }
                if (file.isDirectory())
                {
                    deleteRecursively(file, loggerOrNull, observerOrNull);
                } else
                {
                    if (loggerOrNull != null)
                    {
                        loggerOrNull.log(LogLevel.INFO,
                                String.format("Deleting file '%s'", file.getPath()));
                    }
                    delete(file);
                }
            }
        }
        if (loggerOrNull != null)
        {
            loggerOrNull.log(LogLevel.INFO,
                    String.format("Deleting directory '%s'", path.getPath()));
        }
        return delete(path);
    }

    /** @return true if file is a symbolic link */
    public static boolean isSymbolicLink(File path)
    {
        if (Unix.isOperational())
        {
            return Unix.isSymbolicLink(path.getAbsolutePath());
        } else
        {
            // it is not Linux, Solaris or Mac. So it's probably Windows which does not support
            // symbolic links
            return false;
        }
    }

    private static boolean deleteSymbolicLink(File path, ISimpleLogger loggerOrNull)
    {
        if (loggerOrNull != null)
        {
            loggerOrNull.log(LogLevel.INFO,
                    String.format("Deleting symbolic link to a directory '%s'", path.getPath()));
        }
        return delete(path);
    }

    private static boolean ensureWritable(File path)
    {
        if (path.canWrite() == false && Unix.isOperational())
        {
            try
            {
                Unix.setAccessMode(path.getPath(), (short) 0777);
            } catch (IOExceptionUnchecked ex)
            {
                if (ex.getCause() != null
                        && ex.getCause().getMessage().contains("No such file or directory"))
                {
                    return false;
                }
            }

        }
        return path.canWrite();
    }

    /**
     * Deletes the <var>file</var>, setting it to read-write mode if necessary.
     */
    public static boolean delete(File file)
    {
        final boolean OK = file.delete();
        if (OK)
        {
            return true;
        }
        if (file.exists() && Unix.isOperational())
        {
            Unix.setAccessMode(file.getPath(), (short) 0777);
            return file.delete();
        }
        return false;
    }

    /**
     * Deletes selected parts of a directory recursively, that is deletes all files and directories within the directory that are accepted by the
     * {@link FileFilter}. Any subdirectory that is accepted by the <var>filter</var> will be completely deleted. This holds true also for the
     * <var>path</var> itself.
     * 
     * @param path Path of the directory to delete the selected content from.
     * @param filter The {@link FileFilter} to use when deciding which paths to delete.
     * @param logger The logger that should be used to log deletion of path entries, or <code>null</code> if nothing should be logged.
     * @return <code>true</code> if the <var>path</var> itself has been deleted.
     * @throws InterruptedExceptionUnchecked If the current thread has been interrupted.
     */
    public static boolean deleteRecursively(final File path, final FileFilter filter,
            final ISimpleLogger logger) throws InterruptedExceptionUnchecked
    {
        assert path != null;

        return deleteRecursively(path, filter, logger, null);
    }

    /**
     * Deletes selected parts of a directory recursively, that is deletes all files and directories within the directory that are accepted by the
     * {@link FileFilter}. Any subdirectory that is accepted by the <var>filter</var> will be completely deleted. This holds true also for the
     * <var>path</var> itself.
     * 
     * @param path Path of the directory to delete the selected content from.
     * @param filter The {@link FileFilter} to use when deciding which paths to delete.
     * @param logger The logger that should be used to log deletion of path entries, or <code>null</code> if nothing should be logged.
     * @param observerOrNull If not <code>null</code>, will be updated on progress in the deletion. This can be used to find out whether a
     *            (potentially) long-running recursive deletion call is alive-and-kicking or hangs (e.g. due to a remote directory becoming
     *            unresponsive).
     * @return <code>true</code> if the <var>path</var> itself has been deleted.
     * @throws InterruptedExceptionUnchecked If the current thread has been interrupted.
     */
    public static boolean deleteRecursively(final File path, final FileFilter filter,
            final ISimpleLogger logger, final IActivityObserver observerOrNull)
            throws InterruptedExceptionUnchecked
    {
        assert path != null;
        assert filter != null;

        if (filter.accept(path))
        {
            return deleteRecursively(path, logger, observerOrNull);
        } else
        {
            if (path.isDirectory())
            {
                for (final File file : path.listFiles())
                {
                    if (Thread.interrupted())
                    {
                        throw new InterruptedExceptionUnchecked();
                    }
                    if (observerOrNull != null)
                    {
                        observerOrNull.update();
                    }
                    deleteRecursively(file, filter, logger, observerOrNull);
                }
            }
            return false;
        }
    }

}
