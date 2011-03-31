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

package ch.systemsx.cisd.imagereaders.bioformats;

import java.awt.image.BufferedImage;
import java.util.Map;

import loci.common.IRandomAccess;
import loci.formats.IFormatReader;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.imagereaders.AbstractMetaDataAwareImageReader;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IReadParams;
import ch.systemsx.cisd.imagereaders.TiffReadParams;

/**
 * BioFormats {@link IImageReader} for TIFF files.
 * 
 * @author Kaloyan Enimanev
 */
class TiffBioformatsImageReader extends AbstractMetaDataAwareImageReader
{
    private final IFormatReader formatReader;

    TiffBioformatsImageReader(String libraryName, String readerName,
            IFormatReader formatReader)
    {
        super(libraryName, readerName);
        this.formatReader = formatReader;
    }

    @Override
    public Map<String, Object> readMetaData(IRandomAccessFile handle, IReadParams params)
            throws IOExceptionUnchecked
    {
        IRandomAccess input = new BioFormatsRandomAccessAdapter(handle);
        return BioFormatsImageUtils.readMetadata(formatReader, input);
    }

    public BufferedImage readImage(IRandomAccessFile handle, IReadParams params)
            throws IOExceptionUnchecked
    {
        int page = 0;
        Integer intensityRescalingChannel = null;

        TiffReadParams readParams = (TiffReadParams) params;
        if (readParams != null)
        {
            page = readParams.getPage();
            intensityRescalingChannel = readParams.getIntensityRescalingChannel();
        }

        IRandomAccess input = new BioFormatsRandomAccessAdapter(handle);
        if (intensityRescalingChannel != null)
        {
            return BioFormatsImageUtils.readImageWithIntensityRescaling(formatReader, input, page,
                    intensityRescalingChannel);
        } else
        {
            return BioFormatsImageUtils.readImage(formatReader, input, page);
        }
    }
}