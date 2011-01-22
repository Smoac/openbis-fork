/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.common.fileconverter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper.OutputReadingStrategy;
import ch.systemsx.cisd.common.process.ProcessResult;

/**
 * An abstract file converter base class that uses the ImageMagick tool <code>convert</code>.
 * 
 * @author Bernd Rinn
 */
public abstract class AbstractImageMagickConvertImageFileConverter implements IFileConversionMethod
{

    protected final Logger machineLog;

    protected final Logger operationLog;

    private final static String executableName = "convert";

    private final static File executable = OSUtilities.findExecutable(executableName);

    protected AbstractImageMagickConvertImageFileConverter(Logger machineLog, Logger operationLog)
    {
        this.machineLog = machineLog;
        this.operationLog = operationLog;
    }

    /**
     * Returns the command line to <code>convert</code>.
     */
    protected abstract List<String> getCommandLine(File inFile, File outFile);

    private static String extractImageMagickVersion(String imageMagickVersionLine)
    {
        if (imageMagickVersionLine.startsWith("Version: ImageMagick") == false)
        {
            return null;
        } else
        {
            final String[] versionStringParts = imageMagickVersionLine.split("\\s+");
            if (versionStringParts.length < 3)
            {
                return null;
            }
            return versionStringParts[2];
        }
    }

    private String getImageMagickVersion(String convertExecutableToCheck)
    {
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(convertExecutableToCheck, "--version"),
                        operationLog, machineLog, Constants.MILLIS_TO_WAIT_BEFORE_TIMEOUT,
                        OutputReadingStrategy.ALWAYS, true);
        result.log();
        final String versionString = extractImageMagickVersion(result.getOutput().get(0));
        return versionString;
    }

    /**
     * Returns <code>false</code>.
     */
    public boolean isRemote()
    {
        return false;
    }

    /**
     * Checks for convert v6.2 or newer being installed and executable.
     */
    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        final String imageMagickVersionOrNull = getImageMagickVersion(executable.getAbsolutePath());
        if (imageMagickVersionOrNull == null)
        {
            throw new ConfigurationFailureException("Invalid convert utility");
        }
        String[] imageMagickVersionParts = imageMagickVersionOrNull.split("\\.");
        if (imageMagickVersionParts.length != 3)
        {
            throw new ConfigurationFailureException("Invalid convert utility");
        }
        final int imageMagickMajorVersion = Integer.parseInt(imageMagickVersionParts[0]);
        final int imageMagickMinorVersion = Integer.parseInt(imageMagickVersionParts[1]);
        if (imageMagickMajorVersion < 6 || imageMagickMinorVersion < 2)
        {
            throw ConfigurationFailureException.fromTemplate(
                    "Convert utility is too old (expected: v6.2 or newer, found: v%s)",
                    imageMagickVersionOrNull);
        }
        if (machineLog.isInfoEnabled())
        {
            machineLog.info(String.format("Using convert executable '%s', ImageMagick version %s",
                    executable, imageMagickVersionOrNull));
        }
    }

    public boolean convert(File inFile, File outFile)
    {
        final List<String> commandLine = new ArrayList<String>();
        commandLine.add(executable.getAbsolutePath());
        commandLine.addAll(getCommandLine(inFile, outFile));
        final boolean processOK = ProcessExecutionHelper.runAndLog(commandLine, operationLog, machineLog);
        final boolean exists = outFile.exists() && (outFile.length() > 0);
        if (exists == false)
        {
            operationLog.error("Outfile '" + outFile.getAbsolutePath() + "' does not exist after processing.");
        }
        return processOK && exists;
    }

}
