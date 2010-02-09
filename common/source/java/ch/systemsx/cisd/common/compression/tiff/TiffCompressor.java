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

import java.util.Collection;

import ch.systemsx.cisd.common.compression.file.Compressor;
import ch.systemsx.cisd.common.compression.file.FailureRecord;
import ch.systemsx.cisd.common.compression.file.ICompressionMethod;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;

/**
 * The main class for tiff file compression.
 * 
 * @author Bernd Rinn
 */
public class TiffCompressor extends Compressor
{
    public static void main(String[] args)
    {
        if (args.length != 1)
        {
            System.err.println("Syntax: TiffCompressor <directory>");
            System.exit(1);
        }

        String errorMsgOrNull = tryCompress(args[0]);
        if (errorMsgOrNull != null)
        {
            System.err.print(errorMsgOrNull);
        }
    }

    private static String tryCompress(String path)
    {
        try
        {
            return compress(path, 1, TiffConvertCompressionMethod.create(null));
        } catch (InterruptedException ex)
        {
            return "Compression was interrupted:" + ex.getMessage();
        } catch (EnvironmentFailureException ex)
        {
            return ex.getMessage();
        }
    }

    /**
     * Compresses files in directory with given <var>path</var> with given tiff
     * <var>compressionMethod</var>.
     * 
     * @param threadsPerProcessor number of threads performing compression per processor (>0)
     * @return error message or null if no error occurred
     * @throws InterruptedException if compression was interrupted
     * @throws EnvironmentFailureException if there is a problem with specified path
     */
    public static String compress(String path, int threadsPerProcessor,
            ICompressionMethod compressionMethod) throws InterruptedException,
            EnvironmentFailureException
    {
        assert path != null;
        assert compressionMethod != null;
        assert threadsPerProcessor > 0;

        final StringBuilder errorMsgBuilder = new StringBuilder();
        final Collection<FailureRecord> failed;
        failed = start(path, compressionMethod, threadsPerProcessor);
        if (failed.size() > 0)
        {
            errorMsgBuilder.append("The following files could not bee successfully compressed:\n");
            for (FailureRecord r : failed)
            {
                errorMsgBuilder.append(String.format("%s (%s)\n", r.getFailedFile().getName(), r
                        .getFailureStatus().tryGetErrorMessage()));
            }
            return errorMsgBuilder.toString();
        }
        return null;
    }
}
