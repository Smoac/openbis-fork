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

package ch.systemsx.cisd.datamover.utils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.highwatermark.HighwaterMarkWatcher.IFreeSpaceProvider;
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
    private final File sshExecutable;

    private final String host;

    public RemoteFreeSpaceProvider(final String host, final File sshExecutable)
    {
        this.host = host;
        this.sshExecutable = sshExecutable;
    }

    //
    // IFreeSpaceProvider
    //

    public final long freeSpaceKb(final File path) throws IOException
    {
        final List<String> command =
                Arrays.asList(sshExecutable.getPath(), host, "df -k " + path.getPath() + "");
        Logger rootLogger = Logger.getRootLogger();
        System.out.println(command);
        final ProcessResult processResult =
                ProcessExecutionHelper.run(command, 2000L, rootLogger, rootLogger);
        System.out.println(processResult.getProcessOutput());
        return 0L;
    }
}