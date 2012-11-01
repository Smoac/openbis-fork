/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.transformation;

import java.io.File;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.base.exceptions.InterruptedExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.fileconverter.FileConverter;
import ch.systemsx.cisd.common.fileconverter.TiffCompressionStrategy;
import ch.systemsx.cisd.common.fileconverter.TiffCompressionStrategy.TiffCompressionMethod;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.properties.PropertyUtils;

/**
 * {@link ITransformator} that compresses TIFF files.
 * 
 * @author Piotr Buczek
 */
public class TiffCompressorTransformator implements ITransformator
{

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            TiffCompressorTransformator.class);

    private static final Logger notificationLog = LogFactory.getLogger(LogCategory.NOTIFY,
            TiffCompressorTransformator.class);

    private static final String COMPRESSION_COMMAND_KEY = "compression-command";

    private static final String COMPRESSION_TYPE_KEY = "compression-type";

    private static final String THREADS_PER_PROCESSOR_KEY = "threads-per-processor";

    private static final TiffCompressionMethod DEFAULT_COMPRESSION_METHOD =
            TiffCompressionMethod.LIBTIFF_TIFFCP;

    private static final double DEFAULT_THREADS_PER_CPU_CORE = 1.0;

    private final TiffCompressionStrategy compressionStrategy;

    private final double threadsPerCpuCore;

    public TiffCompressorTransformator(Properties properties)
    {
        operationLog.info("TiffCompressorTransformator created with properties: \n" + properties);

        compressionStrategy = extractCompressionMethod(properties);
        threadsPerCpuCore = extractThreadsPerProcessor(properties);
    }

    private double extractThreadsPerProcessor(Properties properties)
    {
        double result =
                PropertyUtils.getDouble(properties, THREADS_PER_PROCESSOR_KEY,
                        DEFAULT_THREADS_PER_CPU_CORE);
        if (result > 0)
        {
            return result;
        } else
        {
            throw ConfigurationFailureException.fromTemplate(
                    "Number of threads per processor set for property '%s' is %s "
                            + "but needs to be greater than %s", THREADS_PER_PROCESSOR_KEY, result,
                    0);
        }
    }

    private TiffCompressionStrategy extractCompressionMethod(Properties properties)
    {
        final String compressionCommandName =
                PropertyUtils.getProperty(properties, COMPRESSION_COMMAND_KEY).toUpperCase();
        TiffCompressionMethod compressionCommand = DEFAULT_COMPRESSION_METHOD;
        if (compressionCommandName != null)
        {
            if (compressionCommandName.equals("TIFFCP"))
            {
                compressionCommand = TiffCompressionMethod.LIBTIFF_TIFFCP;
            } else if (compressionCommandName.equals("CONVERT"))
            {
                compressionCommand = TiffCompressionMethod.IMAGEMAGICK_CONVERT;
            } else
            {
                throw ConfigurationFailureException.fromTemplate(
                        "Compression command '%s' set for property '%s' is not "
                                + "among supported commands: TIFFCP, CONVERT",
                        compressionCommandName, COMPRESSION_COMMAND_KEY);
            }
        }
        final String compressionTypeOrNull =
                PropertyUtils.getProperty(properties, COMPRESSION_TYPE_KEY);
        return new TiffCompressionStrategy(compressionCommand, compressionTypeOrNull);
    }

    //
    // ITransformator
    //

    @Override
    public Status transform(File path)
    {
        // NOTE:
        // TiffCompressor performs compression in-place and doesn't change files that are already
        // compressed so no additional recovery mechanism needs to be implemented.

        String errorMsgOrNull;
        try
        {
            errorMsgOrNull =
                    FileConverter.performConversion(path, compressionStrategy, threadsPerCpuCore,
                            Integer.MAX_VALUE);
            if (errorMsgOrNull != null)
            {
                notificationLog.error(errorMsgOrNull);
            }
        } catch (InterruptedExceptionUnchecked ex)
        {
            ex.printStackTrace();
            return Status.createError("Tiff compression was interrupted:" + ex.getMessage());
        } catch (EnvironmentFailureException ex)
        {
            return Status.createError(ex.getMessage());
        }

        // Even if compression of some files failed they can still be moved further.
        return Status.OK;
    }
}
