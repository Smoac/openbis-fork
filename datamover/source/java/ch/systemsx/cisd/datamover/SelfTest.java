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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.HighLevelException;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.utilities.ISelfTestable;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;

/**
 * A class that can perform a self test of the data mover.
 * 
 * @author Bernd Rinn
 */
public class SelfTest
{
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, SelfTest.class);

    static
    {
        LogInitializer.init();
    }

    private static void checkPathRecords(final IFileStore[] fileStores)
    {
        assert fileStores != null : "Unspecified file stores";
        for (final ISelfTestable fileStore : fileStores)
        {
            fileStore.check();
        }
        checkPathRecordsContainEachOther(fileStores);
    }

    private static void checkPathRecordsContainEachOther(final IFileStore[] store)
            throws ConfigurationFailureException
    {
        for (int i = 1; i < store.length; ++i)
        {
            for (int j = 0; j < i; ++j)
            {
                if (store[i].isParentDirectory(store[j]) || store[j].isParentDirectory(store[i]))
                {
                    throw ConfigurationFailureException.fromTemplate(
                            "Directory '%s' and '%s' contain each other", store[i], store[j]);
                }
            }
        }
    }

    /**
     * Will perform all checks of the self-test. If the method returns without exception, the
     * self-test can be considered "past", otherwise the exception will have more information on
     * what went wrong. This method performs failure logging of
     * {@link ConfigurationFailureException}s and {@link EnvironmentFailureException}s.
     */
    public static void check(final IPathCopier copier, final IFileStore... stores)
    {
        try
        {
            copier.check();
            checkPathRecords(stores);

            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Self test successfully completed.");
            }
        } catch (final HighLevelException e)
        {
            operationLog.error(String.format("Self test failed: [%s: %s]\n", e.getClass()
                    .getSimpleName(), e.getMessage()));
            throw e;
        } catch (final RuntimeException e)
        {
            operationLog.error(String.format("Self test failed: [%s: %s]\n", e.getClass()
                    .getSimpleName(), e.getMessage()), e);
            throw e;
        }
    }
}
