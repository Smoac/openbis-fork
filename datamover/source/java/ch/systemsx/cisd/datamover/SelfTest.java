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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathCopier;
import ch.systemsx.cisd.datamover.utils.FileStore;

/**
 * A class that can perform a self test of the data mover.
 * 
 * @author Bernd Rinn
 */
public class SelfTest
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION, SelfTest.class);

    static
    {
        LogInitializer.init();
    }

    private static void checkPathRecords(FileStore[] pathRecords, IPathCopier copier)
    {
        assert pathRecords != null;

        checkPathRecordsContainEachOther(pathRecords);
        for (FileStore pathRecord : pathRecords)
        {
            if (pathRecord.getPath() == null)
            {
                continue;
            }
            if (pathRecord.getHost() == null)
            {
                checkDirectoryOnLocalHost(pathRecord);
            } else
            {
                checkDirectoryOnRemoteHost(pathRecord, copier);
            }
        }
    }

    private static void checkPathRecordsContainEachOther(FileStore[] store) throws ConfigurationFailureException
    {
        for (int i = 1; i < store.length; ++i)
        {
            for (int j = 0; j < i; ++j)
            {
                if (StringUtils.equals(store[i].getHost(), store[j].getHost())
                        && containOneAnother(store[i].getCanonicalPath(), store[j].getCanonicalPath()))
                {
                    throw ConfigurationFailureException.fromTemplate("Directory '%s' and '%s' contain each other",
                            store[i].getCanonicalPath(), store[j].getCanonicalPath());
                }
            }
        }
    }

    private static void checkDirectoryOnRemoteHost(FileStore pathRecord, IPathCopier copier)
            throws ConfigurationFailureException
    {
        if (false == copier.exists(pathRecord.getPath(), pathRecord.getHost()))

        {
            throw ConfigurationFailureException.fromTemplate("Cannot access %s directory '%s' on host '%s'", pathRecord
                    .getKind(), pathRecord.getCanonicalPath(), pathRecord.getHost());
        }
    }

    private static void checkDirectoryOnLocalHost(FileStore pathRecord)
    {
        String errorMessage = FileUtilities.checkDirectoryFullyAccessible(pathRecord.getPath(), pathRecord.getKind());
        if (errorMessage != null)
        {
            throw new ConfigurationFailureException(errorMessage);
        }

    }

    private static boolean containOneAnother(String directory1, String directory2)
    {
        if (directory1 == null || directory2 == null)
        {
            return false;
        }
        if (directory1.length() < directory2.length())
        {
            return directory2.startsWith(directory1);
        } else
        {
            return directory1.startsWith(directory2);
        }
    }

    /**
     * Will perform all checks of the self-test. If the method returns without exception, the self-test can be
     * considered "past", otherwise the exception will have more information on what went wrong. This method performs
     * failure logging of {@link ConfigurationFailureException}s and {@link EnvironmentFailureException}s.
     */
    public static void check(IPathCopier copier, FileStore... stores)
    {
        try
        {
            if (false == copier.supportsExplicitHost())
            {
                for (FileStore store : stores)
                {
                    if (null != store.getHost())
                    {
                        throw ConfigurationFailureException
                                .fromTemplate(
                                        "Copier %s does not support explicit remote hosts, but %s store is on a remote host (%s)",
                                        copier.getClass().getSimpleName(), store.getKind(), store.getHost());
                    }
                }
            }
            copier.check();
            checkPathRecords(stores, copier);

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
}
