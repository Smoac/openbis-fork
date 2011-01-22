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
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;

/**
 * A class that performs tiff file compression.
 * 
 * @author Bernd Rinn
 */
public class ImageMagickTiffCompressionConverter extends
        AbstractImageMagickConvertImageFileConverter implements IFileConversionStrategy
{
    private final static String DEFAULT_COMPRESSION_TYPE = "LZW";

    private final String compressionAlgorithmOrNull;

    public ImageMagickTiffCompressionConverter(String compressionAlgorithmOrNull)
    {
        super(LogFactory.getLogger(LogCategory.MACHINE, ImageMagickTiffCompressionConverter.class),
                LogFactory.getLogger(LogCategory.OPERATION,
                        ImageMagickTiffCompressionConverter.class));
        this.compressionAlgorithmOrNull = compressionAlgorithmOrNull;
    }

    @Override
    protected List<String> getCommandLine(File inFile, File outFile)
    {
        final String compression =
                StringUtils.isEmpty(compressionAlgorithmOrNull) ? DEFAULT_COMPRESSION_TYPE
                        : compressionAlgorithmOrNull;
        return Arrays.asList(inFile.getAbsolutePath(), "-compress", compression,
                outFile.getAbsolutePath());
    }

    //
    // IFileConversionStrategy
    //

    public File tryCheckConvert(File inFile)
    {
        final String ext = FilenameUtils.getExtension(inFile.getName()).toLowerCase();
        if ("tiff".equals(ext) || "tif".equals(ext))
        {
            return inFile;
        } else
        {
            return null;
        }
    }

    public boolean deleteOriginalFile()
    {
        return false;
    }

    public IFileConversionMethod getConverter()
    {
        return this;
    }

}
