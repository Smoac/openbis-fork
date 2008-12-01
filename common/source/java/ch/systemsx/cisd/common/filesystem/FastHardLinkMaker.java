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

import ch.systemsx.cisd.common.TimingParameters;
import ch.systemsx.cisd.common.concurrent.MonitoringProxy;
import ch.systemsx.cisd.common.exceptions.WrappedIOException;
import ch.systemsx.cisd.common.os.Unix;

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
                    Unix.createHardLink(source.getAbsolutePath(), destination
                            .getAbsolutePath());
                    return true;
                } catch (WrappedIOException ex)
                {
                    return false;
                }
            }
        };

    /**
     * Returns <code>true</code>, if the native library could be initialized successfully and thus
     * this class is operational, or <code>false</code> otherwise.
     */
    public final static boolean isOperational()
    {
        return Unix.isOperational();
    }

    /**
     * Creates an {@link IFileImmutableCopier}.
     * 
     * @param timingParameters The timing parameters used to monitor and potentially retry the hard
     *            link creation.
     * @return The copier, if the native library could be initialized successfully, or
     *         <code>null</code> otherwise.
     */
    public final static IFileImmutableCopier tryCreate(final TimingParameters timingParameters)
    {
        if (Unix.isOperational() == false)
        {
            return null;
        }
        return new FastHardLinkMaker(timingParameters);

    }

    /**
     * Creates an {@link IFileImmutableCopier} with default timing pameters (uses
     * {@link TimingParameters#getDefaultParameters()}.
     * 
     * @return The copier, if the native library could be initialized successfully, or
     *         <code>null</code> otherwise.
     */
    public final static IFileImmutableCopier tryCreate()
    {
        if (Unix.isOperational() == false)
        {
            return null;
        }
        return new FastHardLinkMaker(TimingParameters.getDefaultParameters());

    }

    private final IFileImmutableCopier monitoringProxy;

    private FastHardLinkMaker(final TimingParameters timingParameters)
    {
        monitoringProxy =
                MonitoringProxy.create(IFileImmutableCopier.class, nativeCopier).timing(
                        timingParameters).get();
    }

    public boolean copyFileImmutably(final File file, final File destinationDirectory,
            final String nameOrNull)
    {
        return monitoringProxy.copyFileImmutably(file, destinationDirectory, nameOrNull);
    }

}
