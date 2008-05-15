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

package ch.systemsx.cisd.common.highwatermark;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher.IFreeSpaceProvider;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;

/**
 * An <code>IFreeSpaceProvider</code> implementation for computing the free space on a remote
 * computer.
 * 
 * @author Christian Ribeaud
 */
public final class RemoteFreeSpaceProvider implements IFreeSpaceProvider
{
    private static final String DF_COMMAND_TEMPLATE = "df -k %s";

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, RemoteFreeSpaceProvider.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RemoteFreeSpaceProvider.class);

    private static final long MILLIS_TO_WAIT_FOR_COMPLETION = 2 * DateUtils.MILLIS_PER_SECOND;

    private final File sshExecutable;

    private final String host;

    private final long millisToWaitForCompletion = MILLIS_TO_WAIT_FOR_COMPLETION;

    public RemoteFreeSpaceProvider(final String host, final File sshExecutable)
    {
        assert host != null : "Unspecified host";
        assert sshExecutable != null : "Unspecified ssh executable";
        this.host = host;
        this.sshExecutable = sshExecutable;
    }

    private final static long parseKbytes(final String freeSpaceInKb, final String dfCommand)
            throws IOException
    {
        try
        {
            final long kBytes = Long.parseLong(freeSpaceInKb);
            if (kBytes < 0)
            {
                throw new IOException(String.format(
                        "Command line '%s' did not find free space in response.", dfCommand));
            }
            return kBytes;
        } catch (final NumberFormatException ex)
        {
            throw new IOException(String.format(
                    "Command line '%s' did not return numeric data as expected.", dfCommand));
        }
    }

    //
    // IFreeSpaceProvider
    //

    public final long freeSpaceKb(final File file) throws IOException
    {
        assert file != null : "Unspecified remote file.";
        final String path = file.getPath();
        assert StringUtils.isNotEmpty(path) : "Empty path.";
        final String dfCommand = String.format(DF_COMMAND_TEMPLATE, path);
        final List<String> command = Arrays.asList(sshExecutable.getPath(), host, dfCommand);
        final ProcessResult processResult =
                ProcessExecutionHelper.run(command, millisToWaitForCompletion, operationLog,
                        machineLog);
        processResult.log();
        final List<String> processOutput = processResult.getProcessOutput();
        if (processOutput.size() >= 2)
        {
            final String output = processOutput.get(1);
            final String[] split = StringUtils.split(output, ' ');
            if (split.length >= 4)
            {
                // The column 'avail' (3th column) interests us.
                return parseKbytes(split[3], dfCommand);
            }
        }
        throw new IOException(String.format(
                "Command line '%s' did not return info as expected. Response was '%s'", dfCommand,
                processOutput));
    }
}