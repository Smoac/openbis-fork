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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.systemsx.cisd.ant.common.StringUtils;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.ISimpleLogger;
import ch.systemsx.cisd.common.logging.LogLevel;
import ch.systemsx.cisd.common.utilities.OSUtilities;

/**
 * Some utility methods helping with subversion paths.
 * 
 * @author Bernd Rinn
 */
class SVNUtilities
{

    // @Private
    static final String DEFAULT_GROUP = "cisd";

    // @Private
    static final String DEFAULT_REPOSITORY_ROOT = "svn+ssh://source.systemsx.ch/repos";

    // @Private
    static final String DEFAULT_VERSION = "trunk";

    // @Private
    static final String HEAD_REVISION = "HEAD";

    /** A project all other projects depend on implicitely. */
    static final String BUILD_RESOURCES_PROJECT = "build_resources";

    /** The regular expression that a release tag has to match. */
    static final String RELEASE_TAG_PATTERN_STRING = "([0-9]+)\\.([0-9]+)\\.([0-9]+)";

    /** The regular expression that a release branch has to match. */
    static final String RELEASE_BRANCH_PATTERN_STRING = "([0-9]+)\\.([0-9]+)\\.x";

    /**
     * A class that holds the information about an operating system process when it is finished.
     */
    static final class ProcessInfo
    {
        private final String commandString;

        private final int exitValue;

        private final List<String> lines;

        ProcessInfo(String commandString, List<String> lines, int exitValue)
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

    private static final class StreamReaderGobbler
    {
        private final Semaphore waitForReadingFinishedSemaphore = new Semaphore(1);

        private final List<String> lines = new ArrayList<String>();

        StreamReaderGobbler(final InputStream stream) throws InterruptedException
        {
            waitForReadingFinishedSemaphore.acquire();
            final Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            final BufferedReader reader =
                                    new BufferedReader(new InputStreamReader(stream));
                            String line;
                            while ((line = reader.readLine()) != null)
                            {
                                lines.add(line);
                            }
                        } catch (IOException ex)
                        {
                            throw new EnvironmentFailureException("Couldn't gobble stream content",
                                    ex);
                        } finally
                        {
                            waitForReadingFinishedSemaphore.release();
                        }
                    }
                };
            t.start();
        }

        List<String> getLines() throws InterruptedException
        {
            waitForReadingFinishedSemaphore.acquire();
            try
            {
                return lines;
            } finally
            {
                waitForReadingFinishedSemaphore.release();
            }
        }

    }

    /**
     * @return The top-level directory (first level of the hierarchy) of <var>path</var>.
     */
    static String getTopLevelDirectory(String path)
    {
        assert path != null && path.startsWith("/");

        final int topLevelEndIndex = path.indexOf("/", 1);
        if (topLevelEndIndex >= 0)
        {
            return path.substring(1, topLevelEndIndex);
        } else
        {
            return path.substring(1);
        }
    }

    /**
     * @return The parent directory of the subversion repository <var>urlPath</var>, or
     *         <code>null</code>, if the <var>urlPath</var> does not have a parent directory.
     */
    static String getParent(String urlPath)
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
    static String normalizeUrl(String url)
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
    static void checkProjectName(String projectName) throws UserFailureException
    {
        assert projectName != null;
        checkName(projectName, "Project");
    }

    /**
     * Checks whether the <var>groupName</var> is valid.
     * 
     * @throws UserFailureException If <var>groupName</var> is invalid.
     */
    static void checkGroupName(String groupName) throws UserFailureException
    {
        assert groupName != null;
        checkName(groupName, "Group");
    }

    /**
     * Checks whether the <var>name</var> is valid.
     * 
     * @throws UserFailureException If <var>projectName</var> is invalid. <var>typeOfName</var> is
     *             used to create a meaningful error message.
     */
    private static void checkName(String name, String typeOfName) throws UserFailureException
    {
        assert name != null;
        assert typeOfName != null;

        if (name.length() == 0)
        {
            throw new UserFailureException(typeOfName + " name is empty.");
        }
        if (name.indexOf('/') >= 0 || name.indexOf('\\') >= 0)
        {
            throw new UserFailureException(typeOfName + " name '" + name
                    + "' contains illegal character.");
        }
    }

    static ProcessInfo subversionCommand(ISimpleLogger logger, final String command,
            final String... args)
    {
        return subversionCommand(logger, true, command, args);
    }

    static ProcessInfo subversionCommand(ISimpleLogger logger, final boolean redirectErrorStream,
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
        final String commandString = StringUtils.join(builder.command(), " ");
        logger.log(LogLevel.INFO, String.format("Executing '%s'", commandString));
        try
        {
            final Process process = builder.start();
            StreamReaderGobbler inputStreamGobbler =
                    new StreamReaderGobbler(process.getInputStream());
            StreamReaderGobbler errorStreamGobbler =
                    new StreamReaderGobbler(process.getErrorStream());
            final int exitValue = process.waitFor();
            List<String> lines = inputStreamGobbler.getLines();
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
        } catch (IOException ex)
        {
            throw SVNException.fromTemplate(ex, "Error while executing '%s'", commandString);
        } catch (InterruptedException ex)
        {
            throw SVNException.fromTemplate(ex, "Unexpectedly interrupted while executing '%s'",
                    commandString);
        }
    }

    static boolean isMuccAvailable()
    {
        return (null != OSUtilities.findExecutable("svnmucc"));
    }

    static ProcessInfo subversionMuccCommand(ISimpleLogger logger, String logMessage,
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
            StreamReaderGobbler inputStreamGobbler =
                    new StreamReaderGobbler(process.getInputStream());
            final int exitValue = process.waitFor();
            List<String> lines = inputStreamGobbler.getLines();
            if (0 != exitValue)
            {
                SVNUtilities.logSvnOutput(logger, inputStreamGobbler.getLines());
                throw SVNException.fromTemplate("Error while executing '%s' (exitValue=%d)",
                        commandString, exitValue);
            }
            return new ProcessInfo(commandString, lines, exitValue);
        } catch (IOException ex)
        {
            throw SVNException.fromTemplate(ex, "Error while executing '%s'", commandString);
        } catch (InterruptedException ex)
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
        for (String line : output)
        {
            logger.log(LogLevel.INFO, String.format("SVN > %s", line));
        }
    }

    static String getBranchForTag(String tagName)
    {
        final Matcher tagMatcher = Pattern.compile(RELEASE_TAG_PATTERN_STRING).matcher(tagName);
        boolean matches = tagMatcher.matches();
        assert matches;
        return String.format("%s.%s.x", tagMatcher.group(1), tagMatcher.group(2));
    }

    static String getFirstTagForBranch(String branchName)
    {
        final Matcher branchMatcher =
                Pattern.compile(RELEASE_BRANCH_PATTERN_STRING).matcher(branchName);
        boolean matches = branchMatcher.matches();
        assert matches;
        return String.format("%s.%s.0", branchMatcher.group(1), branchMatcher.group(2));
    }

}
