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

package ch.systemsx.cisd.ant.task.subversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.systemsx.cisd.ant.common.StringUtils;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.exception.UserFailureException;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.process.InputStreamReaderGobbler;

/**
 * Some utility methods helping with subversion paths.
 * 
 * @author Bernd Rinn
 */
class SVNUtilities
{
    static final String DEFAULT_GROUP = "cisd";

    static final String DEFAULT_REPOSITORY_ROOT = "svn+ssh://svncisd.ethz.ch/repos";

    static final String DEFAULT_VERSION = "trunk";

    static final String LIBRARIES = "libraries";

    static final String LIBRARIES_TRUNK = LIBRARIES + "/" + DEFAULT_VERSION;

    static final String HEAD_REVISION = "HEAD";

    /** A project all other projects depend on implicitly. */
    static final String BUILD_RESOURCES_PROJECT = "build_resources";

    static final String CISD_ANT_TASKS_FILE = "cisd-ant-tasks.jar";

    private static final String RELEASE_PATTERN_PREFIX = "(([0-9]+\\.)[0-9]+)\\.";

    /** A pattern that release branches must match. */
    static final Pattern releaseBranchPattern = Pattern.compile(RELEASE_PATTERN_PREFIX + "x");

    /** A pattern that release tags must match. */
    static final Pattern releaseTagPattern = Pattern.compile(RELEASE_PATTERN_PREFIX + "[0-9]+");

    /** A pattern that sprint branches must match. */
    static final Pattern sprintBranchPattern = Pattern.compile("(S[0-9]+)\\.x");

    /** A pattern that sprint tags must match. */
    static final Pattern sprintTagPattern = Pattern.compile("(S[0-9]+)\\.[0-9]+");

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

    /**
     * @return The top-level directory (first level of the hierarchy) of <var>path</var>.
     */
    static String getTopLevelDirectory(final String path)
    {
        return getNDirectoryLevels(path, 1);
    }

    /**
     * @return <var>n/var> directory levels of <var>path</var>, with the leading '/' removed.
     */
    private static String getNDirectoryLevels(final String path, final int n)
    {
        assert path != null && path.startsWith("/");

        int endIndex = 0;
        for (int i = 0; i < n && endIndex >= 0; ++i)
        {
            endIndex = path.indexOf("/", endIndex + 1);
        }
        if (endIndex > 0)
        {
            return path.substring(1, endIndex);
        } else if (endIndex == -1)
        {
            return path.substring(1);
        } else
        {
            return "/";
        }
    }

    /**
     * Returns the project name part of a path. If the project is in libraries, then the project
     * name will contain the next level of the directory, e.g. <code>libraries/activation</code>.
     */
    static String getProjectName(final String path)
    {
        String projectName = getTopLevelDirectory(path);
        if (LIBRARIES.equals(projectName))
        {
            projectName = getNDirectoryLevels(path, 2);
        }
        return projectName;
    }

    /**
     * @return The parent directory of the subversion repository <var>urlPath</var>, or
     *         <code>null</code>, if the <var>urlPath</var> does not have a parent directory.
     */
    static String getParent(final String urlPath)
    {
        assert urlPath != null;

        final String normalizedUrl = normalizeUrl(urlPath);
        final int topLevelEndIndex = normalizedUrl.lastIndexOf("/");
        if (topLevelEndIndex >= 0)
        {
            return normalizedUrl.substring(0, topLevelEndIndex);
        } else
        {
            return null;
        }
    }

    /**
     * @return The <var>url</var> with all trailing slashes removed and all multiple slashes
     *         replaced with single ones.
     */
    static String normalizeUrl(final String url)
    {
        assert url != null;
        String normalizedUrl = url.replaceAll("([^:/])/+", "$1/");
        normalizedUrl = normalizedUrl.replaceFirst("file:/+", "file:///");
        normalizedUrl = normalizedUrl.replaceFirst("http(s*):/+", "http$1://");
        normalizedUrl = normalizedUrl.replaceFirst("svn(\\+.+)*:/+", "svn$1://");

        if (normalizedUrl.endsWith("/"))
        {
            return normalizedUrl.substring(0, normalizedUrl.length() - 1);
        } else
        {
            return normalizedUrl;
        }
    }

    /**
     * Checks whether the <var>projectName</var> is valid.
     * 
     * @throws UserFailureException If <var>projectName</var> is invalid.
     */
    static void checkProjectName(final String projectName) throws UserFailureException
    {
        assert projectName != null;
        checkName(projectName, "Project", true);
    }

    /**
     * Checks whether the <var>groupName</var> is valid.
     * 
     * @throws UserFailureException If <var>groupName</var> is invalid.
     */
    static void checkGroupName(final String groupName) throws UserFailureException
    {
        assert groupName != null;
        checkName(groupName, "Group", false);
    }

    /**
     * Checks whether the <var>name</var> is valid.
     * 
     * @throws UserFailureException If <var>projectName</var> is invalid. <var>typeOfName</var> is
     *             used to create a meaningful error message.
     */
    private static void checkName(final String name, final String typeOfName,
            final boolean slashAllowed) throws UserFailureException
    {
        assert name != null;
        assert typeOfName != null;

        if (name.length() == 0)
        {
            throw new UserFailureException(typeOfName + " name is empty.");
        }
        if ((name.indexOf('/') >= 0 && slashAllowed == false) || name.indexOf('\\') >= 0)
        {
            throw new UserFailureException(typeOfName + " name '" + name
                    + "' contains illegal character.");
        }
    }

    static ProcessInfo subversionCommand(final ISimpleLogger logger, final String command,
            final String... args)
    {
        return subversionCommand(logger, true, command, args);
    }

    static ProcessInfo subversionCommand(final ISimpleLogger logger,
            final boolean redirectErrorStream, final String command, final String... args)
    {
        return subversionCommand(logger, redirectErrorStream, null, command, args);
    }

    static ProcessInfo subversionCommand(final ISimpleLogger logger,
            final boolean redirectErrorStream, final File workingDirectoryOrNull,
            final String command, final String... args)
    {
        final File svnExecutable = OSUtilities.findExecutable("svn");
        if (svnExecutable == null)
        {
            throw new SVNException("Cannot find executable 'svn'");
        }
        final List<String> fullCommand = new ArrayList<String>();
        fullCommand.add(svnExecutable.getAbsolutePath());
        fullCommand.add(command);
        fullCommand.add("--non-interactive");
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
                SVNUtilities.logSvnOutput(logger, inputStreamGobbler.getLines());
                if (false == redirectErrorStream)
                {
                    SVNUtilities.logSvnOutput(logger, errorStreamGobbler.getLines());
                }
                throw SVNException.fromTemplate("Error while executing '%s' (exitValue=%d)",
                        commandString, exitValue);
            }
            return new ProcessInfo(commandString, lines, exitValue);
        } catch (final IOException ex)
        {
            throw SVNException.fromTemplate(ex, "Error while executing '%s'", commandString);
        } catch (final InterruptedException ex)
        {
            throw SVNException.fromTemplate(ex, "Unexpectedly interrupted while executing '%s'",
                    commandString);
        }
    }

    static boolean isMuccAvailable()
    {
        return null != OSUtilities.findExecutable("svnmucc");
    }

    static ProcessInfo subversionMuccCommand(final ISimpleLogger logger, final String logMessage,
            final String... args)
    {
        final File svnExecutable = OSUtilities.findExecutable("svnmucc");
        if (svnExecutable == null)
        {
            throw new SVNException("Cannot find executable 'svnmucc'");
        }
        final List<String> fullCommand = new ArrayList<String>();
        fullCommand.add(svnExecutable.getAbsolutePath());
        fullCommand.add("--message");
        fullCommand.add(logMessage);
        fullCommand.addAll(Arrays.asList(args));
        final ProcessBuilder builder = new ProcessBuilder(fullCommand);
        builder.redirectErrorStream(true);
        final String commandString = StringUtils.join(builder.command(), " ");
        logger.log(LogLevel.INFO, String.format("Executing '%s'", commandString));
        try
        {
            final Process process = builder.start();
            final InputStreamReaderGobbler inputStreamGobbler =
                    new InputStreamReaderGobbler(process.getInputStream());
            final int exitValue = process.waitFor();
            final List<String> lines = inputStreamGobbler.getLines();
            if (0 != exitValue)
            {
                SVNUtilities.logSvnOutput(logger, inputStreamGobbler.getLines());
                throw SVNException.fromTemplate("Error while executing '%s' (exitValue=%d)",
                        commandString, exitValue);
            }
            return new ProcessInfo(commandString, lines, exitValue);
        } catch (final IOException ex)
        {
            throw SVNException.fromTemplate(ex, "Error while executing '%s'", commandString);
        } catch (final InterruptedException ex)
        {
            throw SVNException.fromTemplate(ex, "Unexpectedly interrupted while executing '%s'",
                    commandString);
        }
    }

    /**
     * Logs the <var>output</var> of an subversionprocess using the <var>logger</var>.
     */
    static void logSvnOutput(final ISimpleLogger logger, final List<String> output)
    {
        for (final String line : output)
        {
            logger.log(LogLevel.INFO, String.format("SVN > %s", line));
        }
    }

    static String getBranchForTagRelease(final String tagName)
    {
        final Matcher tagMatcher = releaseTagPattern.matcher(tagName);
        final boolean matches = tagMatcher.matches();
        assert matches;
        return String.format("%s.x", tagMatcher.group(1));
    }

    static String getBranchForTagSprint(final String tagName)
    {
        final Matcher tagMatcher = sprintTagPattern.matcher(tagName);
        final boolean matches = tagMatcher.matches();
        assert matches;
        return String.format("%s.x", tagMatcher.group(1));
    }

    static String getFirstTagForBranch(final SVNProjectVersionType type, final String branchName)
    {
        if (SVNProjectVersionType.RELEASE_BRANCH.equals(type))
        {
            return SVNUtilities.getFirstTagForReleaseBranch(branchName);
        } else if (SVNProjectVersionType.SPRINT_BRANCH.equals(type))
        {
            return SVNUtilities.getFirstTagForSprintBranch(branchName);
        } else
        {
            throw new IllegalArgumentException("Not a release or Sprint branch.");
        }
    }
    
    static String getFirstTagForReleaseBranch(final String branchName)
    {
        final Matcher branchMatcher = releaseBranchPattern.matcher(branchName);
        final boolean matches = branchMatcher.matches();
        assert matches;
        return String.format("%s.0", branchMatcher.group(1));
    }

    static String getFirstTagForSprintBranch(final String branchName)
    {
        final Matcher branchMatcher = sprintBranchPattern.matcher(branchName);
        final boolean matches = branchMatcher.matches();
        assert matches;
        return String.format("%s.0", branchMatcher.group(1));
    }

}
