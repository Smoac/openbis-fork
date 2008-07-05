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
        final Collection<FailureRecord> failed = start(args[0], new TiffZipCompressionMethod());
        if (failed.size() > 0)
        {
            System.err.println("The following files could not bee successfully compressed:");
        }
        for (FailureRecord r : failed)
        {
            System.err.printf("%s (%s)\n", r.getFailedFile().getName(), r.getFailureStatus()
                    .tryGetErrorMessage());
        }
    }

}
