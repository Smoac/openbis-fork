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

import java.util.List;

import loci.formats.IFormatReader;

import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IImageReaderLibrary;

/**
 * {@link IImageReaderLibrary} for BioFormats readers with intensity rescaling.
 * 
 * @author Kaloyan Enimanev
 */
abstract class AbstractBioFormatsReaderLibrary implements IImageReaderLibrary
{

    /**
     * Adapt a BioFormats {@link IFormatReader} to {@link IImageReader}.
     * 
     * @param formatReader this parameter is guaranteed to be non-null.
     */
    protected abstract IImageReader adaptFormatReader(final IFormatReader formatReader);


    @Override
    public List<String> getReaderNames()
    {
        return BioFormatsImageUtils.getReaderNames();
    }

    @Override
    public IImageReader tryGetReader(String readerName)
    {
        final IFormatReader formatReaderOrNull = BioFormatsImageUtils.tryFindReaderByName(readerName);
        return tryAdaptFormatReader(formatReaderOrNull);
    }

    @Override
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

}
