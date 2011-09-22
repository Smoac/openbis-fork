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

package ch.systemsx.cisd.ant.task.git;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Property;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.ant.common.StringUtils;
import ch.systemsx.cisd.ant.task.subversion.AntTaskSimpleLoggerAdapter;
import ch.systemsx.cisd.ant.task.subversion.SVNException;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.process.InputStreamReaderGobbler;

/**
 * A parallel version of the subversion GatherRevisionAndVersion Task
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class GatherRevisionAndVersionTask extends Property
{
    static final String TRUNK_VERSION = "SNAPSHOT";

    private String versionProperty;

    private String revisionProperty;

    private String cleanProperty;

    public void setFailOnDirty(final boolean failOnDirty)
    {
    }

    public void setFailOnInconsistency(final boolean failOnInconsistentRevisions)
    {
    }

    public void setRevision(final String revisionProperty)
    {
        this.revisionProperty = revisionProperty;
    }

    public void setVersion(final String versionProperty)
    {
        this.versionProperty = versionProperty;
    }

    public void setClean(final String cleanProperty)
    {
        this.cleanProperty = cleanProperty;
    }

    /**
     * A class that holds the information about an operating system process when it is finished.
     */
    static final class ProcessInfo
    {
        private final String commandString;

        private final int exitValue;

        private final List<String> lines;

        ProcessInfo(final String commandString, final List<String> lines, final int exitValue)
        {
            this.commandString = commandString;
            this.lines = lines;
            this.exitValue = exitValue;
        }

        /**
         * @return The command that has been performed in the process.
         */
        public String getCommandString()
        {
            return commandString;
        }

        public List<String> getLines()
        {
            return lines;
        }

        /**
         * @return The exit value of the process.
         */
        public int getExitValue()
        {
            return exitValue;
        }

    }

    @Override
    public void execute() throws BuildException
    {
        if (versionProperty == null && revisionProperty == null)
        {
            throw new BuildException("Neither version nor revision property is defined.");
        }

        ProcessInfo result = createGitCommand();
        List<String> lines = result.getLines();
        if (lines.size() < 1)
        {
            addRevisionProperty("0");
        } else
        {
            String line = lines.get(0);
            String[] tokens = line.split(" ");
            if (tokens.length < 1)
            {
                addRevisionProperty("0");
            } else
            {
                String revision = tokens[0].substring(1);
                addRevisionProperty(revision);
            }
        }
        addProperty(versionProperty, "SNAPSHOT");
        boolean isClean = true;
        addProperty(cleanProperty, isClean ? "clean" : "dirty");
    }

    private void addRevisionProperty(String revision)
    {
        if (revisionProperty != null)
        {
            addProperty(revisionProperty, revision);
        }
    }

    /**
     * <em>Can be overwritten in unit tests.</em>
     */
    @Private
    ProcessInfo createGitCommand()
    {
        return gitCommand(new AntTaskSimpleLoggerAdapter(this), false, getProject().getBaseDir(),
                "svn", "log", "--oneline", "--limit=1");
    }

    static ProcessInfo gitCommand(final ISimpleLogger logger, final boolean redirectErrorStream,
            final File workingDirectoryOrNull, final String command, final String... args)
    {
        final File svnExecutable = OSUtilities.findExecutable("git");
        if (svnExecutable == null)
        {
            throw new IllegalArgumentException("Could not find git executable.");
        }
        final List<String> fullCommand = new ArrayList<String>();
        fullCommand.add(svnExecutable.getAbsolutePath());
        fullCommand.add(command);
        fullCommand.addAll(Arrays.asList(args));
        final ProcessBuilder builder = new ProcessBuilder(fullCommand);
        builder.redirectErrorStream(redirectErrorStream);
        if (workingDirectoryOrNull != null)
        {
            builder.directory(workingDirectoryOrNull);
        }
        final String commandString = StringUtils.join(builder.command(), " ");
        logger.log(LogLevel.INFO, String.format("Executing '%s'", commandString));
        try
        {
            final Process process = builder.start();
            final InputStreamReaderGobbler inputStreamGobbler =
                    new InputStreamReaderGobbler(process.getInputStream());
            final InputStreamReaderGobbler errorStreamGobbler =
                    new InputStreamReaderGobbler(process.getErrorStream());
            final int exitValue = process.waitFor();
            final List<String> lines = inputStreamGobbler.getLines();
            if (0 != exitValue)
            {

                logGitOutput(logger, lines);
                if (false == redirectErrorStream)
                {
                    logGitOutput(logger, errorStreamGobbler.getLines());
                }
                throw SVNException.fromTemplate("Error while executing '%s' (exitValue=%d)",
                        commandString, exitValue);
            }
            return new ProcessInfo(commandString, lines, exitValue);
        } catch (final IOException ex)
        {
            throw SVNException.fromTemplate(ex, "Error while executing '%s'", commandString);
        } catch (InterruptedException ex)
        {
            throw SVNException.fromTemplate(ex, "Error while executing '%s'", commandString);
        }
    }

    private static void logGitOutput(final ISimpleLogger logger, final List<String> lines)
    {
        logger.log(LogLevel.INFO, String.format("git > %s", lines));
    }

    @Private
    String getParentDir()
    {
        return getProject().getBaseDir().getParentFile().getAbsolutePath();
    }

}
