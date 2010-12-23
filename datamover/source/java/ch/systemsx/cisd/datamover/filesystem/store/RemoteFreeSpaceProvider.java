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

package ch.systemsx.cisd.datamover.filesystem.store;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.filesystem.HostAwareFile;
import ch.systemsx.cisd.common.filesystem.IFreeSpaceProvider;
import ch.systemsx.cisd.common.filesystem.ssh.ISshCommandBuilder;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper.OutputReadingStrategy;

/**
 * An <code>IFreeSpaceProvider</code> implementation for computing the free space on a remote
 * computer.
 * 
 * @author Christian Ribeaud
 */
final class RemoteFreeSpaceProvider implements IFreeSpaceProvider
{
    private static final char SPACE = ' ';

    private static final String DF_COMMAND_TEMPLATE = "df -k %s";

    private static final Logger machineLog =
            LogFactory.getLogger(LogCategory.MACHINE, RemoteFreeSpaceProvider.class);

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RemoteFreeSpaceProvider.class);

    private final ISshCommandBuilder sshCommandBuilder;

    private final long millisToWaitForCompletion = Constants.MILLIS_TO_WAIT_BEFORE_TIMEOUT;

    public RemoteFreeSpaceProvider(final ISshCommandBuilder sshCommandBuilder)
    {
        assert sshCommandBuilder != null : "Unspecified ssh command builder";
        this.sshCommandBuilder = sshCommandBuilder;
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
                        "ICommand line '%s' did not find free space in response.", dfCommand));
            }
            return kBytes;
        } catch (final NumberFormatException ex)
        {
            throw new IOException(String.format(
                    "ICommand line '%s' did not return numeric data as expected.", dfCommand));
        }
    }

    //
    // IFreeSpaceProvider
    //

    public final long freeSpaceKb(final HostAwareFile file) throws IOException
    {
        assert file != null : "Unspecified remote file.";
        final String path = file.getFile().getPath();
        assert StringUtils.isNotEmpty(path) : "Empty path.";
        final String dfCommand = String.format(DF_COMMAND_TEMPLATE, path);
        final List<String> command =
                sshCommandBuilder.createSshCommand(dfCommand, file.tryGetHost());
        final ProcessResult processResult =
                ProcessExecutionHelper.run(command, operationLog, machineLog,
                        millisToWaitForCompletion, OutputReadingStrategy.ALWAYS, false);
        processResult.log();
        final List<String> processOutput = processResult.getOutput();
        final String commandLine = StringUtils.join(processResult.getCommandLine(), SPACE);
        String spaceOutputKb = tryParseFreeSpaceOutput(processOutput);
        if (spaceOutputKb == null)
        {
            throw new IOException(String.format(
                    "ICommand line '%s' did not return info as expected. Response was '%s'",
                    commandLine, processOutput));
        }
        return parseKbytes(spaceOutputKb, dfCommand);
    }

    // NOTE sometimes the line with results breaks if the value in the column is longer then the
    // header. So we cannot take the 3rd token from the second line, we have to count tokens in all
    // the lines which appear
    private static String tryParseFreeSpaceOutput(final List<String> outputLines)
    {
        int line = 1;
        int seenTokens = 0;
        while (line < outputLines.size())
        {
            final String output = outputLines.get(line);
            final String[] split = StringUtils.split(output, SPACE);
            if (seenTokens + split.length >= 4)
            {
                // The column 'avail' (3th column) interests us.
                return split[3 - seenTokens];
            }
            seenTokens += split.length;
            line++;
        }
        return null;
    }
}