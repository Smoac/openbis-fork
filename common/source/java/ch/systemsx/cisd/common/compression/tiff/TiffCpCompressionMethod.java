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
import ch.systemsx.cisd.common.logging.LogInitializer;

/**
 * A compression method for TIFF files using the <code>tiffcp</code> utility with specified
 * compression type parameter (by default: <code>lzw:2</code>).
 * 
 * @author Piotr Buczek
 */
public class TiffCpCompressionMethod extends AbstractTiffCompressionMethod
{

    private final static String executableName = "tiffcp";

    private final static File executable = OSUtilities.findExecutable(executableName);

    private final static String DEFAULT_COMPRESSION_TYPE = "lzw:2";

    /**
     * @returns compression method using specified compression type
     * @param compressionTypeOrNull compression type to use (if <code>null</code> default
     *            compression type will be used)
     */
    public static TiffCpCompressionMethod create(String compressionTypeOrNull)
    {
        return compressionTypeOrNull == null ? new TiffCpCompressionMethod()
                : new TiffCpCompressionMethod(compressionTypeOrNull);
    }

    /** Constructs compression method using default compression type. */
    private TiffCpCompressionMethod()
    {
        this(DEFAULT_COMPRESSION_TYPE);
    }

    /** Constructs compression method using specified <var>compressonType</var>. */
    private TiffCpCompressionMethod(String compressionType)
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
                Arrays.asList(executable.getAbsolutePath(), "-c", getCompressionType(),
                        fileToCompress.getAbsolutePath(), inProgressFile.getAbsolutePath());
        return parameters;
    }

    public static void main(String[] args)
    {
        LogInitializer.init();
        final TiffCpCompressionMethod compressor = new TiffCpCompressionMethod();
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
