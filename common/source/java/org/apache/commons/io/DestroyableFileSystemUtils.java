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

package org.apache.commons.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.process.ProcessWatchdog;

/**
 * A <code>FileSystemUtils</code> extension which puts a watch dog to the process created.
 * 
 * @author Christian Ribeaud
 */
@Deprecated
public final class DestroyableFileSystemUtils extends FileSystemUtils
{

    private final static DestroyableFileSystemUtils INSTANCE = new DestroyableFileSystemUtils();

    private final static int OS;

    static
    {
        int os = -1;
        try
        {
            final Field osField = FileSystemUtils.class.getDeclaredField("OS");
            osField.setAccessible(true);
            os = osField.getInt(FileSystemUtils.class);
        } catch (final Exception ex)
        {
        }
        OS = os;
    }

    final ProcessWatchdog watchdog = new ProcessWatchdog(Constants.MILLIS_TO_WAIT_BEFORE_TIMEOUT);

    private DestroyableFileSystemUtils()
    {
        // Can not be instantiated.
    }

    /**
     * Waits for a given process.
     * 
     * @param process the process one wants to wait for.
     */
    protected final void waitFor(final Process process, final String[] cmdAttribs)
            throws IOException
    {
        try
        {
            process.waitFor();
            watchdog.stop();
        } catch (final InterruptedException e)
        {
            process.destroy();
            throw new IOException("Command line threw an InterruptedException '" + e.getMessage()
                    + "' for command " + Arrays.asList(cmdAttribs));
        }
    }

    /**
     * Returns the free space on a drive or volume in kilobytes by invoking the command line.
     */
    public static long freeSpaceKb(final String path) throws IOException
    {
        return INSTANCE.freeSpaceOS(path, OS, true);
    }

    //
    // FileSystemUtils
    //

    @Override
    final List<String> performCommand(final String[] cmdAttribs, final int max) throws IOException
    {
        final List<String> lines = new ArrayList<String>(20);
        Process proc = null;
        InputStream in = null;
        OutputStream out = null;
        InputStream err = null;
        BufferedReader inr = null;
        try
        {
            proc = openProcess(cmdAttribs);
            watchdog.start(proc);
            in = proc.getInputStream();
            out = proc.getOutputStream();
            err = proc.getErrorStream();
            inr = new BufferedReader(new InputStreamReader(in));
            String line = inr.readLine();
            while (line != null && lines.size() < max)
            {
                line = line.toLowerCase().trim();
                lines.add(line);
                line = inr.readLine();
            }
            waitFor(proc, cmdAttribs);
            if (proc.exitValue() != 0)
            {
                // os command problem, throw exception
                throw new IOException("Command line returned OS error code '" + proc.exitValue()
                        + "' for command " + Arrays.asList(cmdAttribs));
            }
            if (lines.size() == 0)
            {
                // unknown problem, throw exception
                throw new IOException("Command line did not return any info " + "for command "
                        + Arrays.asList(cmdAttribs));
            }
            return lines;
        } finally
        {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(err);
            IOUtils.closeQuietly(inr);
            if (proc != null)
            {
                proc.destroy();
            }
        }
    }
}
