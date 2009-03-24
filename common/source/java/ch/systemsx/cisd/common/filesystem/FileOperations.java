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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.io.FileCopyUtils;
import org.apache.commons.io.FileSystemUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.TimingParameters;
import ch.systemsx.cisd.common.concurrent.IActivityObserver;
import ch.systemsx.cisd.common.concurrent.MonitoringProxy;
import ch.systemsx.cisd.common.concurrent.RecordingActivityObserverSensor;
import ch.systemsx.cisd.common.exceptions.UnknownLastChangedException;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * Monitoring for (potentially hanging) file operations. Supposed to be used on remote file systems.
 * 
 * @author Bernd Rinn
 */
public class FileOperations implements IFileOperations
{

    @Private
    final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, IFileOperations.class);

    private final static IFileOperations instance = new FileOperations(null, null);

    private final static ThreadLocal<IFileOperations> monitoredInstanceForThread =
            new ThreadLocal<IFileOperations>();

    /**
     * Returns the straight-forward implementation of {@link IFileOperations}.
     */
    public static IFileOperations getInstance()
    {
        return instance;
    }

    /**
     * Returns the monitored implementation of {@link IFileRemover} for the current thread with the
     * default {@link TimingParameters} (i.e. with {@link TimingParameters#getDefaultParameters()}).
     * <p>
     * Note that you should only ever cache the return value of this method if you can ensure that
     * the cached value will only be used in the same thread as the one where this method has been
     * called. The monitored instance is <i>not</i> thread-safe with respect to the recursive list,
     * remove and copy methods.
     */
    public static IFileOperations getMonitoredInstanceForCurrentThread()
    {
        IFileOperations monitoredInstance = monitoredInstanceForThread.get();
        if (monitoredInstance == null)
        {
            monitoredInstance =
                    internalCreateMonitored(TimingParameters.getDefaultParameters(),
                            new RecordingActivityObserverSensor());
            monitoredInstanceForThread.set(monitoredInstance);
        }

        return monitoredInstance;
    }

    /**
     * Creates a monitored implementation of {@link IFileRemover} with the given
     * <var>observerSensor</var>.
     */
    public static IFileOperations createMonitoredInstance(
            RecordingActivityObserverSensor observerSensor)
    {
        return internalCreateMonitored(TimingParameters.getDefaultParameters(), observerSensor);
    }

    /**
     * Creates a monitored implementation of {@link IFileRemover} with the given
     * <var>parameters</var>.
     */
    public static IFileOperations createMonitoredInstance(TimingParameters parameters)
    {
        // One of the rare cases where '==' is exactly what we mean. Note that TimingParameters are
        // <i>not</i> immutable.
        if (parameters == TimingParameters.getDefaultParameters())
        {
            return getMonitoredInstanceForCurrentThread();
        } else
        {
            return internalCreateMonitored(parameters, new RecordingActivityObserverSensor());
        }
    }

    private static IFileOperations internalCreateMonitored(TimingParameters parameters,
            RecordingActivityObserverSensor observerSensor)
    {
        return MonitoringProxy.create(IFileOperations.class,
                new FileOperations(parameters, observerSensor)).timing(parameters).sensor(
                observerSensor).errorLog(new Log4jSimpleLogger(operationLog)).name(
                "remote file operations").get();
    }

    private final TimingParameters timingParametersOrNull;

    private final IActivityObserver observerOrNull;

    @Private
    FileOperations(TimingParameters timingParametersOrNull, IActivityObserver observerOrNull)
    {
        this.timingParametersOrNull = timingParametersOrNull;
        this.observerOrNull = observerOrNull;
    }

    //
    // IFileOperations
    //

    // java.io.File

    public boolean exists(File file)
    {
        return file.exists();
    }

    public boolean delete(File file)
    {
        return file.delete();
    }

    public boolean rename(File source, File destination)
    {
        return source.renameTo(destination);
    }

    public boolean canRead(File file)
    {
        return file.canRead();
    }

    public boolean canWrite(File file)
    {
        return file.canWrite();
    }

    public boolean createNewFile(File file) throws IOExceptionUnchecked
    {
        try
        {
            return file.createNewFile();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public File createTempFile(String prefix, String suffix, File directory)
            throws IOExceptionUnchecked
    {
        try
        {
            return File.createTempFile(prefix, suffix, directory);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public File createTempFile(String prefix, String suffix) throws IOExceptionUnchecked
    {
        try
        {
            return File.createTempFile(prefix, suffix);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public File getCanonicalFile(File file) throws IOExceptionUnchecked
    {
        try
        {
            return file.getCanonicalFile();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public String getCanonicalPath(File file) throws IOExceptionUnchecked
    {
        try
        {
            return file.getCanonicalPath();
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public boolean isDirectory(File file)
    {
        return file.isDirectory();
    }

    public boolean isFile(File file)
    {
        return file.isFile();
    }

    public boolean isHidden(File file)
    {
        return file.isHidden();
    }

    public long lastModified(File file)
    {
        return file.lastModified();
    }

    public long length(File file)
    {
        return file.length();
    }

    public String[] list(File file)
    {
        return file.list();
    }

    public String[] list(File file, FilenameFilter filter)
    {
        return file.list(filter);
    }

    public File[] listFiles(File file)
    {
        return file.listFiles();
    }

    public File[] listFiles(File file, FilenameFilter filter)
    {
        return file.listFiles(filter);
    }

    public File[] listFiles(File file, FileFilter filter)
    {
        return file.listFiles(filter);
    }

    public boolean mkdir(File file)
    {
        return file.mkdir();
    }

    public boolean mkdirs(File file)
    {
        return file.mkdirs();
    }

    public boolean setLastModified(File file, long time)
    {
        return file.setLastModified(time);
    }

    public boolean setReadOnly(File file)
    {
        return file.setReadOnly();
    }

    // Advanced

    public List<File> listDirectories(File directory, boolean recursive)
    {
        return FileUtilities.listDirectories(directory, recursive, observerOrNull);
    }

    public List<File> listFiles(File directory, String[] extensionsOrNull, boolean recursive)
    {
        return FileUtilities.listFiles(directory, extensionsOrNull, recursive, observerOrNull);
    }

    public List<File> listFilesAndDirectories(File directory, boolean recursive)
    {
        return FileUtilities.listFilesAndDirectories(directory, recursive, observerOrNull);
    }

    public long lastChanged(File path) throws UnknownLastChangedException
    {
        return FileUtilities.lastChanged(path, false, 0L, observerOrNull);
    }

    public long lastChanged(File path, boolean subDirectoriesOnly, long stopWhenFindYounger)
            throws UnknownLastChangedException
    {
        return FileUtilities.lastChanged(path, subDirectoriesOnly, stopWhenFindYounger,
                observerOrNull);
    }

    public long lastChangedRelative(File path, boolean subDirectoriesOnly,
            long stopWhenFindYoungerRelative) throws UnknownLastChangedException
    {
        return FileUtilities.lastChangedRelative(path, subDirectoriesOnly,
                stopWhenFindYoungerRelative, observerOrNull);
    }

    public String checkPathFullyAccessible(File path, String kindOfPath)
    {
        return FileUtilities.checkPathFullyAccessible(path, kindOfPath);
    }

    public String checkPathReadAccessible(File path, String kindOfPath)
    {
        return FileUtilities.checkPathReadAccessible(path, kindOfPath);
    }

    public String checkDirectoryFullyAccessible(File directory, String kindOfDirectory)
    {
        return FileUtilities.checkDirectoryFullyAccessible(directory, kindOfDirectory);
    }

    public String checkDirectoryReadAccessible(File directory, String kindOfDirectory)
    {
        return FileUtilities.checkDirectoryReadAccessible(directory, kindOfDirectory);
    }

    public String checkFileFullyAccessible(File file, String kindOfFile)
    {
        return FileUtilities.checkFileFullyAccessible(file, kindOfFile);
    }

    public String checkFileReadAccessible(File file, String kindOfFile)
    {
        return FileUtilities.checkFileReadAccessible(file, kindOfFile);
    }

    public void touch(File file) throws IOExceptionUnchecked
    {
        try
        {
            FileUtils.touch(file);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void deleteRecursively(File fileToRemove) throws IOExceptionUnchecked
    {
        final boolean deleteOK =
                FileUtilities.deleteRecursively(fileToRemove, null, observerOrNull);
        if (deleteOK == false)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(new IOException("Recursive deletion of '"
                    + fileToRemove + "' failed."));
        }
    }

    public boolean removeRecursively(File fileToRemove)
    {
        return FileUtilities.deleteRecursively(fileToRemove, null, observerOrNull);
    }

    public boolean removeRecursivelyQueueing(File fileToRemove)
    {
        return QueueingPathRemoverService.removeRecursively(fileToRemove);
    }

    public void move(File source, File destination) throws IOExceptionUnchecked
    {
        if (destination.isDirectory())
        {
            moveToDirectory(source, destination);
        } else
        {
            final boolean renameOK = rename(source, destination);
            if (renameOK == false)
            {
                throw CheckedExceptionTunnel.wrapIfNecessary(new IOException("Moving '"
                        + source.getAbsolutePath() + "' into directory '"
                        + destination.getAbsolutePath() + "' failed."));
            }
        }
    }

    public void moveToDirectory(File source, File destinationDirectory) throws IOExceptionUnchecked
    {
        final File target = new File(destinationDirectory, source.getName());
        final boolean moveOK = source.renameTo(target);
        if (moveOK == false)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(new IOException("Moving '"
                    + source.getAbsolutePath() + "' into directory '"
                    + destinationDirectory.getAbsolutePath() + "' failed."));
        }
    }

    public void copyDirectory(File srcDir, File destDir)
    {
        try
        {
            FileCopyUtils.copyDirectory(srcDir, destDir, observerOrNull);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void copyDirectoryToDirectory(File srcDir, File destDir) throws IOExceptionUnchecked
    {
        try
        {
            FileCopyUtils.copyDirectoryToDirectory(srcDir, destDir, observerOrNull);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void copyFile(File srcFile, File destFile) throws IOExceptionUnchecked
    {
        try
        {
            FileCopyUtils.copyFile(srcFile, destFile);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void copyFileToDirectory(File srcFile, File destDir) throws IOExceptionUnchecked
    {
        try
        {
            FileCopyUtils.copyFileToDirectory(srcFile, destDir);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void copy(File source, File destination) throws IOExceptionUnchecked
    {
        try
        {
            FileCopyUtils.copy(source, destination);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public void copyToDirectory(File source, File destDir) throws IOExceptionUnchecked
    {
        try
        {
            FileCopyUtils.copyToDirectory(source, destDir);
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    public byte[] getContentAsByteArray(File file) throws IOExceptionUnchecked
    {
        java.io.InputStream inputStream = null;
        try
        {
            inputStream = new FileInputStream(file);
            return IOUtils.toByteArray(inputStream);
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        } finally
        {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public String getContentAsString(File file) throws IOExceptionUnchecked
    {
        return FileUtilities.loadToString(file);
    }

    public String getExactContentAsString(File file) throws IOExceptionUnchecked
    {
        return FileUtilities.loadExactToString(file);
    }

    public List<String> getContentAsStringList(File file) throws IOExceptionUnchecked
    {
        return FileUtilities.loadToStringList(file);
    }

    public InputStream getInputStream(File file) throws IOExceptionUnchecked
    {
        return new IInputStreamAdapter(getIInputStream(file));
    }

    public IInputStream getIInputStream(File file) throws IOExceptionUnchecked
    {
        try
        {
            final IInputStream is = internalGetIInputStream(file);
            if (timingParametersOrNull != null)
            {
                return MonitoringProxy.create(IInputStream.class, is)
                        .timing(timingParametersOrNull).name(
                                "input stream <" + file.getAbsolutePath() + ">").get();
            } else
            {
                return is;
            }
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    @Private
    IInputStream internalGetIInputStream(File file) throws FileNotFoundException
    {
        return new InputStreamAdapter(new FileInputStream(file));
    }

    public OutputStream getOutputStream(File file) throws IOExceptionUnchecked
    {
        return new IOutputStreamAdapter(getIOutputStream(file));
    }

    public IOutputStream getIOutputStream(File file) throws IOExceptionUnchecked
    {
        try
        {
            final IOutputStream os = internalGetIOutputStream(file);
            if (timingParametersOrNull != null)
            {
                return MonitoringProxy.create(IOutputStream.class, os).timing(
                        timingParametersOrNull).name(
                        "output stream <" + file.getAbsolutePath() + ">").get();
            } else
            {
                return os;
            }
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

    @Private
    IOutputStream internalGetIOutputStream(File file) throws FileNotFoundException
    {
        return new OutputStreamAdapter(new FileOutputStream(file));
    }

    public void writeToFile(File file, String content) throws IOExceptionUnchecked
    {
        FileUtilities.writeToFile(file, content);
    }

    public long freeSpaceKb(String path) throws IOExceptionUnchecked
    {
        try
        {
            return FileSystemUtils.freeSpaceKb(path);
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }
    }

}
