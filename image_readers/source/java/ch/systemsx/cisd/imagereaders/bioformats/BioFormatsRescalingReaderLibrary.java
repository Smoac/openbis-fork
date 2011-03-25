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

import loci.common.IRandomAccess;
import loci.formats.IFormatReader;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.imagereaders.AbstractImageReader;
import ch.systemsx.cisd.imagereaders.Constants;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IImageReaderLibrary;

/**
 * {@link IImageReaderLibrary} for bioformat readers with intensity rescaling.
 * 
 * @author Kaloyan Enimanev
 */
public class BioFormatsRescalingReaderLibrary extends AbstractBioFormatsReaderLibrary
{

    @Override
    public String getName()
    {
        return Constants.BIOFORMATS_RESCALING_LIBRARY;
    }

    @Override
    protected IImageReader adaptFormatReader(final IFormatReader formatReader)
    {
        final String libraryName = getName();
        final String readerName = BioFormatsImageUtils.getReaderName(formatReader);

        return new AbstractImageReader(libraryName, readerName)
            {
                @Override
                public BufferedImage readImage(IRandomAccessFile handle, int page)
                        throws IOExceptionUnchecked
                {
                    IRandomAccess input = new BioFormatsRandomAccessAdapter(handle);
                    // TODO KE: Is 0 a sensible default for channel ?
                    return BioFormatsImageUtils.readImageWithIntensityRescaling(formatReader,
                            input, page, 0);
                }
            };
    }
}
