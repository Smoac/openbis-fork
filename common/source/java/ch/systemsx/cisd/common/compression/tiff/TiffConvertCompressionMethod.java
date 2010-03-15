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

package ch.systemsx.cisd.common.compression.tiff;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.logging.LogInitializer;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper.OutputReadingStrategy;

/**
 * A compression method for TIFF files using the ImageMagick <code>convert</code> utility with
 * specified compression type parameter (by default: <code>LZW</code>).
 * 
 * @author Bernd Rinn
 */
public class TiffConvertCompressionMethod extends AbstractTiffCompressionMethod
{

    private final static String executableName = "convert";

    private final static File executable = OSUtilities.findExecutable(executableName);

    private final static String DEFAULT_COMPRESSION_TYPE = "LZW";

    private static String getImageMagickVersion(String convertExecutableToCheck)
    {
        final ProcessResult result =
                ProcessExecutionHelper.run(Arrays.asList(convertExecutableToCheck, "--version"),
                        operationLog, machineLog, Constants.MILLIS_TO_WAIT_BEFORE_TIMEOUT,
                        OutputReadingStrategy.ALWAYS, true);
        result.log();
        final String versionString = extractImageMagickVersion(result.getOutput().get(0));
        return versionString;
    }

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

    /**
     * @returns compression method using specified compression type
     * @param compressionTypeOrNull compression type to use (if <code>null</code> default
     *            compression type will be used)
     */
    public static TiffConvertCompressionMethod create(String compressionTypeOrNull)
    {
        return compressionTypeOrNull == null ? new TiffConvertCompressionMethod()
                : new TiffConvertCompressionMethod(compressionTypeOrNull);
    }

    /** Constructs compression method using default compression type. */
    private TiffConvertCompressionMethod()
    {
        this(DEFAULT_COMPRESSION_TYPE);
    }

    /** Constructs compression method using specified <var>compressonType</var>. */
    private TiffConvertCompressionMethod(String compressionType)
    {
        super(compressionType);
    }

    @Override
    protected List<String> createCommandLine(File fileToCompress, File inProgressFile)
    {
        assert executable != null;
        assert fileToCompress != null;
        assert fileToCompress.isFile();
        assert inProgressFile != null;
        assert inProgressFile.exists() == false;

        final List<String> parameters =
                Arrays.asList(executable.getAbsolutePath(), fileToCompress.getAbsolutePath(),
                        "-compress", getCompressionType(), inProgressFile.getAbsolutePath());
        return parameters;
    }

    @Override
    public void check() throws EnvironmentFailureException, ConfigurationFailureException
    {
        super.check();
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

    public static void main(String[] args)
    {
        LogInitializer.init();
        final TiffConvertCompressionMethod compressor = new TiffConvertCompressionMethod();
        compressor.check();
    }

    @Override
    protected File getExecutable()
    {
        return executable;
    }

    @Override
    protected String getExecutableName()
    {
        return executableName;
    }

}
