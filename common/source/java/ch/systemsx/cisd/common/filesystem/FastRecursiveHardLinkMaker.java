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

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.TimingParameters;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.rsync.RsyncBasedRecursiveHardLinkMaker;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A fast {@link IImmutableCopier} that uses a fallback option whenever one of the fast copiers for
 * either files or directories is not available.
 * 
 * @author Bernd Rinn
 */
public class FastRecursiveHardLinkMaker implements IImmutableCopier
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FastRecursiveHardLinkMaker.class);

    private static final String RSYNC_EXEC = "rsync";

    private static final String LN_EXEC = "ln";

    private final static int DEFAULT_MAX_ERRORS_TO_IGNORE = 3;

    private final IImmutableCopier fallbackCopierOrNull;

    private final IFileImmutableCopier fastFileCopierOrNull;

    private final IDirectoryImmutableCopier fastDirectoryCopierOrNull;

    public final static IImmutableCopier tryCreate()
    {
        return tryCreate(TimingParameters.getDefaultParameters());
    }

    public final static IImmutableCopier tryCreate(final TimingParameters timingParameters)
    {
        final File rsyncExecOrNull = OSUtilities.findExecutable(RSYNC_EXEC);
        if (rsyncExecOrNull == null)
        {
            return null;
        }
        final File lnExecOrNull = OSUtilities.findExecutable(LN_EXEC);
        if (lnExecOrNull == null)
        {
            return null;
        }
        return create(rsyncExecOrNull, lnExecOrNull, timingParameters, false);
    }

    public final static IImmutableCopier create(final File rsyncExecutable, final File lnExecutable)
    {
        return create(rsyncExecutable, lnExecutable, TimingParameters.getDefaultParameters(), false);
    }

    public final static IImmutableCopier create(final File rsyncExecutable,
            final File lnExecutable, final TimingParameters parameters)
    {
        return new FastRecursiveHardLinkMaker(rsyncExecutable, lnExecutable, parameters, false);
    }

    public final static IImmutableCopier create(final File rsyncExecutable,
            final File lnExecutable, final TimingParameters parameters, final boolean neverUseNative)
    {
        return new FastRecursiveHardLinkMaker(rsyncExecutable, lnExecutable, parameters,
                neverUseNative);
    }

    private FastRecursiveHardLinkMaker(final File rsyncExcutable, final File lnExecutable,
            final TimingParameters timingParameters, final boolean neverUseNative)
            throws ConfigurationFailureException
    {
        this.fastFileCopierOrNull =
                neverUseNative ? null : FastHardLinkMaker.tryCreate(timingParameters);
        this.fastDirectoryCopierOrNull =
                new RsyncBasedRecursiveHardLinkMaker(rsyncExcutable, timingParameters,
                        DEFAULT_MAX_ERRORS_TO_IGNORE);
        if (fastFileCopierOrNull == null)
        {
            this.fallbackCopierOrNull =
                    RecursiveHardLinkMaker.tryCreate(HardLinkMaker.create(lnExecutable,
                            timingParameters));
        } else
        {
            this.fallbackCopierOrNull = RecursiveHardLinkMaker.tryCreate(fastFileCopierOrNull);
        }
        if ((fastFileCopierOrNull == null && fallbackCopierOrNull == null)
                || (fastDirectoryCopierOrNull == null && fallbackCopierOrNull == null))
        {
            throw new ConfigurationFailureException("FastRecursiveHardLinkMaker not operational");
        }
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(timingParameters.toString());
            if (fastFileCopierOrNull != null)
            {
                operationLog.info("Using native library to create hard link copies of files.");
            } else
            {
                operationLog.info("Using 'ln' to create hard link copies of files.");
            }
            if (fastDirectoryCopierOrNull != null)
            {
                operationLog.info("Using 'rsync' to traverse directories when making recursive "
                        + "hard link copies.");
            } else
            {
                operationLog.info("Using Java to traverse directories when making recursive hard "
                        + "link copies");
            }
        }
    }

    public boolean copyImmutably(File source, File destinationDirectory, String nameOrNull)
    {
        if (source.isDirectory())
        {
            if (fastDirectoryCopierOrNull != null)
            {
                return fastDirectoryCopierOrNull.copyDirectoryImmutably(source,
                        destinationDirectory, nameOrNull);
            } else
            {
                return fallbackCopierOrNull.copyImmutably(source, destinationDirectory, nameOrNull);
            }
        } else
        {
            if (fastFileCopierOrNull != null)
            {
                return fastFileCopierOrNull.copyFileImmutably(source, destinationDirectory,
                        nameOrNull);
            } else
            {
                return fallbackCopierOrNull.copyImmutably(source, destinationDirectory, nameOrNull);
            }
        }
    }

}
