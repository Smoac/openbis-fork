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

import java.io.File;
import java.util.concurrent.Callable;

import ch.systemsx.cisd.common.concurrent.MonitoringProxy;
import ch.systemsx.cisd.common.filesystem.FileLinkUtilities;
import ch.systemsx.cisd.common.filesystem.FileLinkUtilities.FileLinkException;
import ch.systemsx.cisd.common.process.CallableExecutor;

/**
 * A {@link IFileImmutableCopier} that uses a native method to create hard links.
 * 
 * @author Bernd Rinn
 */
public class FastHardLinkMaker implements IFileImmutableCopier
{

    private final static IFileImmutableCopier nativeCopier = new IFileImmutableCopier()
        {
            public boolean copyFileImmutably(File source, File destinationDirectory,
                    String nameOrNull)
            {
                final File destination =
                        new File(destinationDirectory, (nameOrNull == null) ? source.getName()
                                : nameOrNull);
                try
                {
                    FileLinkUtilities.createHardLink(source.getAbsolutePath(), destination
                            .getAbsolutePath());
                    return true;
                } catch (FileLinkException ex)
                {
                    return false;
                }
            }
        };

    /**
     * Returns <code>true</code>, if the native library could be initialized successfully and
     * thus this clase is operational, or <code>false</code> otherwise.
     */
    public final static boolean isOperational()
    {
        return FileLinkUtilities.isOperational();
    }

    /**
     * Creates an {@link IFileImmutableCopier}.
     * 
     * @param millisToWaitForCompletion The time out in milli-seconds to wait for one link creation
     *            to finish.
     * @param maxRetryOnFailure Maximal number of retries in case of failure.
     * @param millisToSleepOnFailure Time in milli-seconds to sleep after a failure has occured and
     *            before trying it again.
     * @return The copier, if the native library could be initialized successfully, or
     *         <code>null</code> otherwise.
     */
    public final static IFileImmutableCopier tryCreate(final long millisToWaitForCompletion,
            final int maxRetryOnFailure, final long millisToSleepOnFailure)
    {
        if (FileLinkUtilities.isOperational() == false)
        {
            return null;
        }
        return new FastHardLinkMaker(millisToWaitForCompletion, maxRetryOnFailure,
                millisToSleepOnFailure);

    }

    private final CallableExecutor retryingExecutor;

    private final IFileImmutableCopier monitoringProxy;

    private FastHardLinkMaker(final long millisToWaitForCompletion, final int maxRetryOnFailure,
            final long millisToSleepOnFailure)
    {
        retryingExecutor = new CallableExecutor(maxRetryOnFailure, millisToSleepOnFailure);
        monitoringProxy =
                MonitoringProxy.create(IFileImmutableCopier.class, nativeCopier).timeoutMillis(
                        millisToWaitForCompletion).get();
    }

    public boolean copyFileImmutably(final File file, final File destinationDirectory,
            final String nameOrNull)
    {
        final Callable<Boolean> creatorCallable = new Callable<Boolean>()
            {
                public Boolean call() throws Exception
                {
                    return monitoringProxy
                            .copyFileImmutably(file, destinationDirectory, nameOrNull);
                }
            };
        return retryingExecutor.executeCallable(creatorCallable);
    }

}
