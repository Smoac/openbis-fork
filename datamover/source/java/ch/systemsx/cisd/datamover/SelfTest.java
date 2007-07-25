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

package ch.systemsx.cisd.datamover;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.common.utilities.ISelfTestable;

/**
 * A class that can perform a self test of the data mover.
 * 
 * @author Bernd Rinn
 */
public class SelfTest
{

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE, SelfTest.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, SelfTest.class);

    private static final String LOCAL_DATA_AND_TEMP_DIR_CONTAIN_EACHOTHER_TEMPLATE =
            "The local data directory '%s' and the local temporary directory '%s' contain each other.";

    private static final String LOCAL_AND_REMOTE_DATA_DIR_CONTAIN_EACHOTHER_TEMPLATE =
            "The local data directory '%s' and the remote data directory '%s' contain each other.";

    private static final String LOCAL_TEMP_AND_REMOTE_DATA_DIR_CONTAIN_EACHOTHER_TEMPLATE =
            "The local temporary directory '%s' and the remote data directory '%s' contain each other.";

    static
    {
        LogInitializer.init();
    }

    private static void checkDirectories(File localDataDirectory, File localTemporaryDirectory,
            File remoteDataDirectory, File manualInterventionDirectory, String remoteHost, IPathCopier copier)
            throws ConfigurationFailureException
    {
        assert localDataDirectory != null;
        assert localTemporaryDirectory != null;
        assert remoteDataDirectory != null;
        assert copier != null;

        if (null == remoteHost)
        {
            checkDirectoriesWithRemoteShare(localDataDirectory, localTemporaryDirectory, remoteDataDirectory);
        } else
        {
            checkDirectoriesWithRemoteHost(localDataDirectory, localTemporaryDirectory, remoteDataDirectory,
                    remoteHost, copier);
        }
        if (manualInterventionDirectory != null)
        {
            String errorMessage =
                    FileUtilities.checkDirectoryFullyAccessible(manualInterventionDirectory, "manual intervention");
            if (errorMessage != null)
            {
                throw new ConfigurationFailureException(errorMessage);
            }
        }
    }

    private static void checkDirectoriesWithRemoteHost(File localDataDirectory, File localTemporaryDirectory,
            File remoteDataDirectory, String remoteHost, IPathCopier copier) throws ConfigurationFailureException
    {
        checkLocalDirectories(localDataDirectory, localTemporaryDirectory);
        checkDirectoryOnRemoteHost(remoteDataDirectory, remoteHost, copier);
    }

    private static void checkLocalDirectories(File localDataDirectory, File localTemporaryDirectory)
            throws ConfigurationFailureException
    {
        final String localDataCanonicalPath = getCanonicalPath(localDataDirectory);
        final String localTempCanonicalPath = getCanonicalPath(localTemporaryDirectory);

        if (localDataCanonicalPath.startsWith(localTempCanonicalPath)
                || localTempCanonicalPath.startsWith(localDataCanonicalPath))
        {
            throw new ConfigurationFailureException(String.format(LOCAL_DATA_AND_TEMP_DIR_CONTAIN_EACHOTHER_TEMPLATE,
                    localDataCanonicalPath, localTempCanonicalPath));
        }

        String errorMessage = FileUtilities.checkDirectoryFullyAccessible(localDataDirectory, "local data");
        if (errorMessage != null)
        {
            throw new ConfigurationFailureException(errorMessage);
        }
        errorMessage = FileUtilities.checkDirectoryFullyAccessible(localTemporaryDirectory, "local temporary");
        if (errorMessage != null)
        {
            throw new ConfigurationFailureException(errorMessage);
        }
    }

    private static void checkDirectoryOnRemoteHost(File remoteDataDirectory, String remoteHost, IPathCopier copier)
            throws ConfigurationFailureException
    {
        if (false == copier.exists(remoteDataDirectory, remoteHost))
        {
            throw ConfigurationFailureException.fromTemplate("Cannot access directory '%s' on host '%s'",
                    remoteDataDirectory.getPath(), remoteHost);
        }
    }

    private static void checkDirectoriesWithRemoteShare(File localDataDirectory, File localTemporaryDirectory,
            File remoteDataDirectory) throws ConfigurationFailureException
    {
        final String localDataCanonicalPath = getCanonicalPath(localDataDirectory);
        final String localTempCanonicalPath = getCanonicalPath(localTemporaryDirectory);
        final String remoteDataCanonicalPath = getCanonicalPath(remoteDataDirectory);

        if (localDataCanonicalPath.startsWith(localTempCanonicalPath)
                || localTempCanonicalPath.startsWith(localDataCanonicalPath))
        {
            throw ConfigurationFailureException.fromTemplate(LOCAL_DATA_AND_TEMP_DIR_CONTAIN_EACHOTHER_TEMPLATE,
                    localDataCanonicalPath, localTempCanonicalPath);
        }
        if (localDataCanonicalPath.startsWith(remoteDataCanonicalPath)
                || remoteDataCanonicalPath.startsWith(localDataCanonicalPath))
        {
            throw ConfigurationFailureException.fromTemplate(LOCAL_AND_REMOTE_DATA_DIR_CONTAIN_EACHOTHER_TEMPLATE,
                    localDataCanonicalPath, remoteDataCanonicalPath);
        }
        if (localTempCanonicalPath.startsWith(remoteDataCanonicalPath)
                || remoteDataCanonicalPath.startsWith(localTempCanonicalPath))
        {
            throw ConfigurationFailureException.fromTemplate(LOCAL_TEMP_AND_REMOTE_DATA_DIR_CONTAIN_EACHOTHER_TEMPLATE,
                    localTempCanonicalPath, remoteDataCanonicalPath);
        }

        String errorMessage = FileUtilities.checkDirectoryFullyAccessible(localDataDirectory, "local data");
        if (errorMessage != null)
        {
            throw new ConfigurationFailureException(errorMessage);
        }
        errorMessage = FileUtilities.checkDirectoryFullyAccessible(localTemporaryDirectory, "local temporary");
        if (errorMessage != null)
        {
            throw new ConfigurationFailureException(errorMessage);
        }
        errorMessage = FileUtilities.checkDirectoryFullyAccessible(remoteDataDirectory, "remote");
        if (errorMessage != null)
        {
            throw new ConfigurationFailureException(errorMessage);
        }
    }

    /**
     * Will perform all checks of the self-test. If the method returns without exception, the self-test can be
     * considered "past", otherwise the exception will have more information on what went wrong. This method performs
     * failure logging of {@link ConfigurationFailureException}s and {@link EnvironmentFailureException}s.
     */
    public static void check(File localDataDirectory, File localTemporaryDirectory, File remoteDataDirectory,
            File manualInterventionDirectory, String remoteHost, IPathCopier copier, ISelfTestable... selfTestables)
            throws ConfigurationFailureException, EnvironmentFailureException
    {
        try
        {
            if (null != remoteHost && false == copier.supportsExplicitHost())
            {
                throw ConfigurationFailureException.fromTemplate(
                        "Copier %s does not support explicit remote hosts, but remote host given:%s", copier.getClass()
                                .getSimpleName(), remoteHost);
            }
            checkDirectories(localDataDirectory, localTemporaryDirectory, remoteDataDirectory,
                    manualInterventionDirectory, remoteHost, copier);
            copier.check();

            for (ISelfTestable selfTestable : selfTestables)
            {
                selfTestable.check();
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Self test successfully completed.");
            }
        } catch (HighLevelException e)
        {
            operationLog.error(String.format("Self test failed: [%s: %s]\n", e.getClass().getSimpleName(), e
                    .getMessage()));
            throw e;
        } catch (RuntimeException e)
        {
            operationLog.error(String.format("Self test failed: [%s: %s]\n", e.getClass().getSimpleName(), e
                    .getMessage()), e);
            throw e;
        }
    }

    private static String getCanonicalPath(File path)
    {
        try
        {
            return path.getCanonicalPath() + File.separator;
        } catch (IOException e)
        {
            throw EnvironmentFailureException.fromTemplate(e, "Cannot determine canonical form of path '%s'", path
                    .getPath());
        }
    }

    /**
     * @return <code>true</code> if the <var>copyProcess</var> on the file system where the <var>destinationDirectory</var>
     *         resides requires deleting an existing file before it can be overwritten.
     */
    public static boolean requiresDeletionBeforeCreation(IPathCopier copyProcess, File sourceDirectory,
            File destinationDirectory)
    {
        assert copyProcess != null;
        assert sourceDirectory != null;
        assert sourceDirectory.isDirectory();
        assert destinationDirectory != null;
        assert destinationDirectory.isDirectory();

        final File sourceFile = new File(sourceDirectory, ".requiresDeletionBeforeCreation");
        final File destinationFile = new File(destinationDirectory, ".requiresDeletionBeforeCreation");
        try
        {
            sourceFile.createNewFile();
            destinationFile.createNewFile();
            // If we have e.g. a Cellera NAS server, the next call will raise an IOException.
            final boolean OK = Status.OK.equals(copyProcess.copy(sourceFile, destinationDirectory));
            if (machineLog.isInfoEnabled())
            {
                if (OK)
                {
                    machineLog.info(String.format("Copier %s on directory '%s' works with overwriting existing files.",
                            copyProcess.getClass().getSimpleName(), destinationDirectory.getAbsolutePath()));
                } else
                {
                    machineLog.info(String.format(
                            "Copier %s on directory '%s' requires deletion before creation of existing files.",
                            copyProcess.getClass().getSimpleName(), destinationDirectory.getAbsolutePath()));
                }
            }
            return (OK == false);
        } catch (IOException e)
        {
            if (machineLog.isInfoEnabled())
            {
                machineLog.info(String.format(
                        "The file system on '%s' requires deletion before creation of existing files.",
                        destinationDirectory.getAbsolutePath()));
            }
            return true;
        } finally
        {
            // We don't check for success because there is nothing we can do if we fail.
            sourceFile.delete();
            destinationFile.delete();
        }
    }

    public static void main(String[] args)
    {
        if (args.length == 3)
        {
            final File localDataDirectory = new File(args[0]);
            final File localTemporaryDirectory = new File(args[1]);
            final File remoteDataDirectory = new File(args[2]);
            try
            {
                check(localDataDirectory, localTemporaryDirectory, remoteDataDirectory, null, null, new IPathCopier()
                    {
                        public Status copy(File sourcePath, File destinationDirectory)
                        {
                            return null;
                        }

                        public Status copy(File sourcePath, File destinationDirectory, String destinationHost)
                        {
                            return null;
                        }

                        public boolean exists(File destinationDirectory, String destinationHost)
                        {
                            return false;
                        }

                        public boolean supportsExplicitHost()
                        {
                            return false;
                        }

                        public boolean terminate()
                        {
                            return false;
                        }

                        public void check()
                        {
                        }
                    });
                System.err.println("Self test passed.");
            } catch (Exception e)
            {
                System.err.println("Self test failed:");
                e.printStackTrace();
                System.exit(1);
            }
        } else
        {
            System.err.println("Syntax: SelfTest <localDataDir> <localTempDir> <remoteDataDir>");
            System.exit(2);
        }
    }
}
