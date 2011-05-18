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


import java.util.Arrays;
import java.util.List;

import loci.formats.IFormatReader;

import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IImageReaderLibrary;

/**
 * {@link IImageReaderLibrary} implementation for BioFormats.
 * 
 * @author Kaloyan Enimanev
 */
public class BioFormatsReaderLibrary implements IImageReaderLibrary
{

    private static final List<String> TIFF_FORMAT_SUBSTRINGS = Arrays.asList("tiff",
            "metamorph stk", "tagged image file format", "deltavision", "leica", "nikon", "zeiss");

    public String getName()
    {
        return ImageReaderConstants.BIOFORMATS_LIBRARY;
    }

    public List<String> getReaderNames()
    {
        return BioFormatsImageUtils.getReaderNames();
    }

    public IImageReader tryGetReader(String readerName)
    {
        final IFormatReader formatReaderOrNull =
                BioFormatsImageUtils.tryFindReaderByName(readerName);
        return tryAdaptFormatReader(formatReaderOrNull);
    }

    public IImageReader tryGetReaderForFile(String fileName)
    {
        IFormatReader formatReaderOrNull = BioFormatsImageUtils.tryFindReaderForFile(fileName);
        return tryAdaptFormatReader(formatReaderOrNull);
    }

    /**
     * Delegate the wrapping of non-null readers to an abstract method.
     */
    protected IImageReader tryAdaptFormatReader(final IFormatReader formatReaderOrNull)
    {
        return (formatReaderOrNull == null) ? null : adaptFormatReader(formatReaderOrNull);
    }

    protected IImageReader adaptFormatReader(final IFormatReader formatReader)
    {
        final String libraryName = getName();
        final String readerName = BioFormatsImageUtils.getReaderName(formatReader);

        if (isTiffReader(formatReader))
        {
            return new TiffBioformatsImageReader(libraryName, readerName, formatReader);
        } else
        {
            return new DefaultBioformatsImageReader(libraryName, readerName, formatReader);
        }
    }

    private boolean isTiffReader(IFormatReader reader)
    {
        String readerFormat = reader.getFormat().toLowerCase();
        for (String tiffSubstring : TIFF_FORMAT_SUBSTRINGS)
        {
            if (readerFormat.contains(tiffSubstring))
            {
                return true;
            }
        }
        return false;
    }
}
