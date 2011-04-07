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

package ch.systemsx.cisd.openbis.dss.etl.biozentrum;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.image.IImageTransformer;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessIOStrategy;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.ImageReaderFactory;

/**
 * An {@link IImageTransformer} using the convert command line tool for transformations.
 * 
 * @author Kaloyan Enimanev
 */
public class ConvertToolImageTransformer implements IImageTransformer
{

    private static final String PNG = "png";

    private static final File convertUtilityOrNull;
    static
    {
        convertUtilityOrNull = OSUtilities.findExecutable("convert");
        if (convertUtilityOrNull == null)
        {
            throw new ConfigurationFailureException(
                    "The 'convert' command line tool cannot be found on the system path. "
                            + "Requested image transformations cannot be completed.");
        }
    }

    private static final Logger machineLog = LogFactory.getLogger(LogCategory.MACHINE,
            ConvertToolImageTransformer.class);

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ConvertToolImageTransformer.class);

    private final List<String> convertCliArguments;

    ConvertToolImageTransformer(String arguments)
    {
        this.convertCliArguments = parseCommandArguments(arguments);
    }

    public BufferedImage transform(BufferedImage image)
    {
        File tmpFile = null;
        try
        {
            tmpFile = createTempImageFile(image);
            byte[] output = transform(tmpFile);
            return toBufferedImage(output);
        } catch (IOException ioex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ioex);
        } finally
        {
            if (tmpFile != null)
            {
                tmpFile.delete();
            }
        }
    }

    private BufferedImage toBufferedImage(byte[] output) throws ConfigurationFailureException
    {
        IImageReader imageReader =
                ImageReaderFactory.tryGetReader(ImageReaderConstants.IMAGEIO_LIBRARY, PNG);
        if (imageReader == null)
        {
            throw new ConfigurationFailureException("No ImageIO image readers available");
        }
        return imageReader.readImage(output, null);
    }

    private byte[] transform(File imageFile) throws IOException
    {
        String filePath = imageFile.getAbsolutePath();

        ProcessResult result =
                ProcessExecutionHelper.run(getCommandLine(filePath), operationLog, machineLog,
                        ConcurrencyUtilities.NO_TIMEOUT,
                        ProcessIOStrategy.BINARY_DISCARD_STDERR_IO_STRATEGY, false);

        if (result.isOK() == false)
        {
            throw new IOException(String.format(
                    "Error calling 'convert' for image '%s'. Exit value: %d, I/O status: %s",
                    filePath, result.getExitValue(), result.getProcessIOResult().getStatus()));
        } else
        {
            return result.getBinaryOutput();
        }

    }

    private List<String> parseCommandArguments(String argsString)
    {
        String[] arguments = argsString.trim().split("\\s");
        return Arrays.asList(arguments);
    }

    private List<String> getCommandLine(String filePath)
    {
        ArrayList<String> result = new ArrayList<String>();
        result.add(convertUtilityOrNull.getPath());
        result.addAll(convertCliArguments);
        result.add(filePath);
        result.add("png:-");
        return result;
    }

    private File createTempImageFile(BufferedImage image) throws IOException
    {
        File tmpFile = File.createTempFile(getClass().getSimpleName(), PNG);
        tmpFile.deleteOnExit();
        ImageIO.write(image, PNG, tmpFile);
        return tmpFile;
    }

}
