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

package ch.systemsx.cisd.openbis.dss.etl.dto.api.v1.transformations;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.base.image.IStreamingImageTransformer;
import ch.systemsx.cisd.base.utilities.OSUtilities;
import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.IProcessIOHandler;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessIOStrategy;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.ImageID;
import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.ImageReaderFactory;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.ImageUtil;

/**
 * An {@link IStreamingImageTransformer} using the convert command line tool for transformations.
 * <p>
 * Warning: The serialized version of this class can be stored in the database for each image.
 * Moving this class to a different package would make all the saved transformations invalid.
 * 
 * @author Kaloyan Enimanev
 */
public class ConvertToolImageTransformer implements IStreamingImageTransformer
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

    private final byte[] buffer = new byte[ProcessExecutionHelper.RECOMMENDED_BUFFER_SIZE];

    private final List<String> convertCliArguments;

    ConvertToolImageTransformer(String arguments)
    {
        this.convertCliArguments = parseCommandArguments(arguments);
    }

    public BufferedImage transform(BufferedImage image)
    {
        try
        {
            byte[] input = ImageUtil.imageToPngFast(image);
            byte[] output = transform(input);
            return toBufferedImage(output);
        } catch (IOException ioex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ioex);
        }
    }

    public BufferedImage transform(InputStream input)
    {
        return toBufferedImage(transformToPNG(input));
    }

    public byte[] transformToPNG(InputStream input)
    {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transformToPNGStream(input, bos);
        return bos.toByteArray();
    }

    public void transformToPNGStream(InputStream input, OutputStream output)
    {
        try
        {
            transform(input, output);
        } catch (IOException ioex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ioex);
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
        return imageReader.readImage(output, ImageID.NULL, null);
    }

    private byte[] transform(final byte[] input) throws IOException
    {
        final ByteArrayInputStream bis = new ByteArrayInputStream(input);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transform(bis, bos);
        return bos.toByteArray();
    }

    private void transform(final InputStream input, final OutputStream output) throws IOException
    {

        final List<String> errorLines = new ArrayList<String>();
        ProcessIOStrategy customIOStrategy =
                createCustomProcessIOStrategy(input, output, errorLines);

        ProcessResult result =
                ProcessExecutionHelper.run(getCommandLine(), operationLog, machineLog,
                        ConcurrencyUtilities.NO_TIMEOUT, customIOStrategy, false);

        if (result.isOK() == false)
        {
            final String msg =
                    String.format(
                            "Error calling '%s'. Exit value: %d, I/O status: %s\nError output: %s",
                            getCommandLine().toString(), result.getExitValue(), result
                                    .getProcessIOResult().getStatus(), errorLines.toString());
            operationLog.warn(msg);
            throw new IOException(msg);
        }
    }

    private ProcessIOStrategy createCustomProcessIOStrategy(final InputStream input,
            final OutputStream output, final List<String> errorLines)
    {
        return ProcessIOStrategy.createCustom(new IProcessIOHandler()
            {

                public void handle(AtomicBoolean processRunning, OutputStream stdin,
                        InputStream stdout, InputStream stderr) throws IOException
                {
                    int n = 0;
                    final BufferedReader stdErrReader =
                            new BufferedReader(new InputStreamReader(stderr));
                    while (processRunning.get() && (-1 != (n = input.read(buffer))))
                    {
                        stdin.write(buffer, 0, n);
                        ProcessExecutionHelper.readBytesIfAvailable(stdout, output, buffer, -1,
                                false);
                        ProcessExecutionHelper.readTextIfAvailable(stdErrReader, errorLines, false);
                    }
                    stdin.flush();
                    stdin.close();

                    while (processRunning.get())
                    {
                        ProcessExecutionHelper.readBytesIfAvailable(stdout, output, buffer, -1,
                                false);
                        ProcessExecutionHelper.readTextIfAvailable(stdErrReader, errorLines, false);
                    }

                    ProcessExecutionHelper.readBytesIfAvailable(stdout, output, buffer, -1, false);
                    ProcessExecutionHelper.readTextIfAvailable(stdErrReader, errorLines, false);
                }
            });
    }

    private List<String> parseCommandArguments(String argsString)
    {
        String[] arguments = argsString.trim().split("\\s");
        return Arrays.asList(arguments);
    }

    private List<String> getCommandLine()
    {
        ArrayList<String> result = new ArrayList<String>();
        result.add(convertUtilityOrNull.getPath());
        result.addAll(convertCliArguments);
        // use standard input to read image
        result.add("-");
        // use standard output to produce result
        result.add("png:-");
        return result;
    }

}
