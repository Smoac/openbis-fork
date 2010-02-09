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
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.compression.file.ICompressionMethod;
import ch.systemsx.cisd.common.compression.tiff.TiffCompressor;
import ch.systemsx.cisd.common.compression.tiff.TiffConvertCompressionMethod;
import ch.systemsx.cisd.common.compression.tiff.TiffCpCompressionMethod;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.PropertyUtils;

/**
 * {@link ITransformator} that compresses TIFF files.
 * 
 * @author Piotr Buczek
 */
public class TiffCompressorTransformator implements ITransformator
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, TiffCompressorTransformator.class);

    private static final Logger notificationLog =
            LogFactory.getLogger(LogCategory.NOTIFY, TiffCompressorTransformator.class);

    private static final String COMPRESSION_COMMAND_KEY = "compression-command";

    private static final String COMPRESSION_TYPE_KEY = "compression-type";

    private static final String THREADS_PER_PROCESSOR_KEY = "threads-per-processor";

    private static final CompressionCommand DEFAULT_COMPRESSION_COMMAND = CompressionCommand.TIFFCP;

    private static final int DEFAULT_THREADS_PER_PROCESSOR = 4;

    /**
     * Enumeration of supported methods of compression with names matching transformator property
     * value.
     */
    private enum CompressionCommand
    {
        TIFFCP, CONVERT
    }

    private final ICompressionMethod compressionMethod;

    private final int threadsPerProcessor;

    public TiffCompressorTransformator(Properties properties)
    {
        operationLog.info("TiffCompressorTransformator created with properties: \n" + properties);

        compressionMethod = extractCompressionMethod(properties);
        threadsPerProcessor = extractThreadsPerProcessor(properties);
    }

    private int extractThreadsPerProcessor(Properties properties)
    {
        int result =
                PropertyUtils.getInt(properties, THREADS_PER_PROCESSOR_KEY,
                        DEFAULT_THREADS_PER_PROCESSOR);
        if (result > 0)
        {
            return result;
        } else
        {
            throw ConfigurationFailureException
                    .fromTemplate(
                            "Number of threads per processor set for property '%s' is %s but needs to be greater than %s",
                            THREADS_PER_PROCESSOR_KEY, result, 0);
        }
    }

    private ICompressionMethod extractCompressionMethod(Properties properties)
    {
        final String compressionCommandName =
                PropertyUtils.getProperty(properties, COMPRESSION_COMMAND_KEY);
        CompressionCommand compressionCommand = DEFAULT_COMPRESSION_COMMAND;
        if (compressionCommandName != null)
        {
            try
            {
                compressionCommand = CompressionCommand.valueOf(compressionCommandName);
            } catch (IllegalArgumentException ex)
            {
                throw ConfigurationFailureException
                        .fromTemplate(
                                "Compression command '%s' set for property '%s' is not among supported commands: %s",
                                compressionCommandName, COMPRESSION_COMMAND_KEY, Arrays
                                        .toString(CompressionCommand.values()));
            }
        }
        final String compressionTypeOrNull =
                PropertyUtils.getProperty(properties, COMPRESSION_TYPE_KEY);
        switch (compressionCommand)
        {
            case CONVERT:
                return TiffConvertCompressionMethod.create(compressionTypeOrNull);
            case TIFFCP:
                return TiffCpCompressionMethod.create(compressionTypeOrNull);
        }
        return null; // not possible
    }

    //
    // ITransformator
    //

    public Status transform(File path)
    {
        // NOTE:
        // TiffCompressor performs compression in-place and doesn't change files that are already
        // compressed so no additional recovery mechanism needs to be implemented.

        String errorMsgOrNull;
        try
        {
            errorMsgOrNull =
                    TiffCompressor.compress(path.getAbsolutePath(), threadsPerProcessor,
                            compressionMethod);
            if (errorMsgOrNull != null)
            {
                notificationLog.error(errorMsgOrNull);
            }
        } catch (InterruptedException ex)
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
