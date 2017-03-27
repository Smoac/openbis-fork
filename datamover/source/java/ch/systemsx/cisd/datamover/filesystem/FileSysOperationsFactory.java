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

package ch.systemsx.cisd.datamover.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.filesystem.FastRecursiveHardLinkMaker;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;
import ch.systemsx.cisd.common.filesystem.IPathCopier;
import ch.systemsx.cisd.common.filesystem.rsync.RsyncCopier;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathMover;
import ch.systemsx.cisd.datamover.filesystem.intf.IPathRemover;
import ch.systemsx.cisd.datamover.intf.IFileSysParameters;

/**
 * @author Tomasz Pylak
 */
public class FileSysOperationsFactory implements IFileSysOperationsFactory
{
    private static final String SSH_EXECUTABLE_NAME = "ssh";

    private static final String RSYNC_EXECUTABLE_NAME = "rsync";

    private static final String LN_EXECUTABLE_NAME = "ln";

    private final IFileSysParameters parameters;

    public FileSysOperationsFactory(final IFileSysParameters parameters)
    {
        assert parameters != null;

        this.parameters = parameters;
    }

    private final static File findExecutable(final String executablePath,
            final String executableName)
    {
        final File executableFile;
        if (StringUtils.isNotBlank(executablePath))
        {
            executableFile = new File(executablePath);
        } else
        {
            executableFile = OSUtilities.findExecutable(executableName);
        }
        if (executableFile != null && OSUtilities.executableExists(executableFile) == false)
        {
            throw ConfigurationFailureException.fromTemplate("Cannot find executable '%s'.",
                    executableFile.getAbsoluteFile());
        }
        return executableFile;
    }

    //
    // IFileSysOperationsFactory
    //

    @Override
    public final IPathRemover getRemover()
    {
        return new RetryingPathRemover(parameters.getMaximalNumberOfRetries(),
                parameters.getIntervalToWaitAfterFailure());
    }

    @Override
    public final IImmutableCopier getImmutableCopier()
    {
        final File rsyncExecutable = findRsyncExecutable();
        final File lnExecutable = findLnExecutable();
        return FastRecursiveHardLinkMaker.create(rsyncExecutable, lnExecutable, null);
    }

    @Override
    public final IPathCopier getCopier(final boolean requiresDeletionBeforeCreation)
    {
        final File rsyncExecutable = findRsyncExecutable();
        final File sshExecutableOrNull = tryFindSshExecutable();
        RsyncCopier rsyncCopier;

        if (parameters.getBasicRsyncParameters().length > 0)
        {
            // Concatenate the basic and extra command-line options into one array
            List<String> allCmdLineOptions = new ArrayList<String>(parameters.getBasicRsyncParameters().length
                    + parameters.getExtraRsyncParameters().length);
            Collections.addAll(allCmdLineOptions, parameters.getBasicRsyncParameters());
            Collections.addAll(allCmdLineOptions, parameters.getExtraRsyncParameters());
            rsyncCopier = new RsyncCopier(rsyncExecutable, sshExecutableOrNull,
                    allCmdLineOptions.toArray(new String[allCmdLineOptions.size()]));
        } else
        {
            rsyncCopier = new RsyncCopier(rsyncExecutable, sshExecutableOrNull, requiresDeletionBeforeCreation,
                    parameters.isRsyncOverwrite(), parameters.getExtraRsyncParameters());
        }
        return rsyncCopier;
    }

    @Override
    public final File tryFindSshExecutable()
    {
        return findExecutable(parameters.getSshExecutable(), SSH_EXECUTABLE_NAME);
    }

    private final File findRsyncExecutable()
    {
        final File rsyncExecutableOrNull =
                findExecutable(parameters.getRsyncExecutable(), RSYNC_EXECUTABLE_NAME);
        if (rsyncExecutableOrNull == null)
        {
            throw new ConfigurationFailureException("Unable to find an rsync executable.");
        }
        return rsyncExecutableOrNull;
    }

    private final File findLnExecutable()
    {
        final File lnExecutableOrNull =
                findExecutable(parameters.getLnExecutable(), LN_EXECUTABLE_NAME);
        if (lnExecutableOrNull == null)
        {
            throw new ConfigurationFailureException("Unable to find an ln executable.");
        }
        return lnExecutableOrNull;
    }

    @Override
    public final IPathMover getMover()
    {
        return new RetryingPathMover(parameters.getMaximalNumberOfRetries(),
                parameters.getIntervalToWaitAfterFailure());
    }

    @Override
    public String tryGetIncomingRsyncExecutable()
    {
        return parameters.getIncomingRsyncExecutable();
    }

    @Override
    public String tryGetOutgoingRsyncExecutable()
    {
        return parameters.getOutgoingRsyncExecutable();
    }
}
