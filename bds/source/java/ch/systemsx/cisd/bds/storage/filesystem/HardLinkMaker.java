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

package ch.systemsx.cisd.bds.storage.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.utilities.OSUtilities;
import ch.systemsx.cisd.common.utilities.ProcessExecutionHelper;

/**
 * An <code>ILinkMaker</code> implementation which is able to make hard links using the operating system.
 * <p>
 * Note that the current implementation only works for files.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class HardLinkMaker implements ILinkMaker
{
    static final String ALREADY_EXISTS_FORMAT = "Given file '%s' already exists.";

    private static final String HARD_LINK_EXEC = "ln";

    private static HardLinkMaker instance;

    private final String linkExecPath;

    private final BufferedAppender loggerRecorder;

    private HardLinkMaker(final String linkExecPath)
    {
        this.linkExecPath = linkExecPath;
        loggerRecorder = new BufferedAppender("%m", Level.ERROR);
    }

    private final List<String> createLnCmdLine(final File srcFile, final File destFile)
    {
        List<String> tokens = new ArrayList<String>();
        tokens.add(linkExecPath);
        tokens.add(srcFile.getAbsolutePath());
        // The destination file does not yet exist. Is going to be created and is the link.
        tokens.add(destFile.getAbsolutePath());
        return tokens;
    }

    /**
     * Returns the unique instance of this class.
     */
    static synchronized final HardLinkMaker getInstance() throws EnvironmentFailureException
    {
        if (instance == null)
        {
            final File lnExec = OSUtilities.findExecutable(HARD_LINK_EXEC);
            if (lnExec == null)
            {
                throw EnvironmentFailureException.fromTemplate(
                        "No hard link executable '%s' could be found in path '%s'.", HARD_LINK_EXEC, OSUtilities
                                .getSafeOSPath());
            }
            instance = new HardLinkMaker(lnExec.getAbsolutePath());
        }
        return instance;
    }

    //
    // ILinkMaker
    //

    public final File tryCreateLink(final java.io.File file, final java.io.File destDir, final String nameOrNull)
            throws EnvironmentFailureException
    {
        assert file != null && file.isFile() : "Given file can not be null and must be a file.";
        assert destDir != null && destDir.isDirectory() : "Given destination directory can not be null and must be a directory.";
        final String destName;
        if (nameOrNull == null)
        {
            destName = file.getName();
        } else
        {
            destName = nameOrNull;
        }
        final File destFile = new File(destDir, destName);
        if (destFile.exists())
        {
            throw new IllegalArgumentException(String.format(ALREADY_EXISTS_FORMAT, destFile.getAbsolutePath()));
        }
        final List<String> cmd = createLnCmdLine(file, destFile);
        final Logger rootLogger = Logger.getRootLogger();
        loggerRecorder.resetLogContent();
        final boolean ok = ProcessExecutionHelper.runAndLog(cmd, rootLogger, rootLogger);
        if (ok == false)
        {
            String message = loggerRecorder.getLogContent();
            if (message.length() == 0)
            {
                return null;
            }
            throw new EnvironmentFailureException(message);
        }
        return destFile;
    }
}
