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

package ch.systemsx.cisd.imagereaders.jai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import com.sun.media.jai.codec.ImageCodec;

import ch.systemsx.cisd.imagereaders.ImageReaderConstants;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IImageReaderLibrary;

/**
 * Implementation of {@link IImageReader} using JAI (Java Advanced Imaging) under the hood.
 * 
 * @author Kaloyan Enimanev
 */
public class JAIReaderLibrary implements IImageReaderLibrary
{

    private static final String TIFF_FORMAT = "TIFF";

    private static final String PNM_FORMAT = "PNM";

    /**
     * it is not evident from the JAI API how to determine whether a certain file extension is known
     * or not.
     */
    private static final Map<String /* unhandled extension */, String/* handled extension */> fixExtensions;

    static
    {
        fixExtensions = new HashMap<String, String>();
        fixExtensions.put("tif", TIFF_FORMAT);
        fixExtensions.put("jpg", "JPEG");
        fixExtensions.put("pbm", PNM_FORMAT);
        fixExtensions.put("pgm", PNM_FORMAT);
        fixExtensions.put("ppm", PNM_FORMAT);
    }

    public String getName()
    {
        return ImageReaderConstants.JAI_LIBRARY;
    }

    public List<String> getReaderNames()
    {
        List<ImageCodec> codecs = getCodecsList();
        ArrayList<String> result = new ArrayList<String>();
        for (ImageCodec codec : codecs)
        {
            result.add(codec.getFormatName());
        }
        return result;
    }

    public IImageReader tryGetReader(final String readerName)
    {
        assert readerName != null : "fileName cannot be null";

        ImageCodec codec = tryFindCodecForFormat(readerName);
        if (codec == null)
        {
            return null;
        }
        
        if (isTiffReader(readerName)) {
            return new TiffImageReader(getName(), readerName);
        } else {
            return new DefaultImageReader(getName(), readerName);
        }
    }

    public IImageReader tryGetReaderForFile(String fileName)
    {
        assert fileName != null : "fileName cannot be null";

        String fileExtension = FilenameUtils.getExtension(fileName);
        fileExtension = fixFileExtension(fileExtension);
        return tryGetReader(fileExtension);
    }


    private boolean isTiffReader(String readerName)
    {
        return TIFF_FORMAT.equalsIgnoreCase(readerName);
    }

    @SuppressWarnings("unchecked")
    private List<ImageCodec> getCodecsList()
    {
        Enumeration<ImageCodec> en = ImageCodec.getCodecs();
        List<ImageCodec> codecs =
                (en == null) ? Collections.<ImageCodec> emptyList() : Collections.list(en);
        return codecs;
    }

    private ImageCodec tryFindCodecForFormat(String formatName)
    {
        for (ImageCodec codec : getCodecsList())
        {
            if (formatName.equalsIgnoreCase(codec.getFormatName()))
            {
                return codec;
            }
        }
        return null;
    }

    private String fixFileExtension(String extension)
    {
        if (extension != null)
        {
            String fixedExtension = fixExtensions.get(extension.toLowerCase());
            if (fixedExtension != null)
            {
                return fixedExtension;
            }
        }
        return extension;
    }
}
