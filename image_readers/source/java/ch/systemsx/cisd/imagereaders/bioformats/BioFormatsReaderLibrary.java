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
import java.io.File;
import java.util.List;

import loci.formats.IFormatReader;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;
import ch.systemsx.cisd.imagereaders.IImageReader;
import ch.systemsx.cisd.imagereaders.IImageReaderLibrary;

/**
 * 
 *
 * @author Bernd Rinn
 */
public class BioFormatsReaderLibrary implements IImageReaderLibrary
{

    @Override
    public String getName()
    {
        return "bioformats";
    }

    @Override
    public List<String> getReaderNames()
    {
        return BioFormatsImageUtils.getReaderNames();
    }

    @Override
    public IImageReader tryGetReader(String readerName)
    {
        final IFormatReader formatReaderOrNull = BioFormatsImageUtils.tryFindReaderByName(readerName);
        if (formatReaderOrNull == null)
        {
            return null;
        } else
        {
            return new IImageReader()
                {
                    
                    @Override
                    public BufferedImage readImage(String filename, byte[] bytes, int page)
                    {
                        // TODO Auto-generated method stub
                        return null;
                    }
                    
                    @Override
                    public BufferedImage readImage(String filename, IRandomAccessFile handle, int page)
                            throws IOExceptionUnchecked
                    {
                        // TODO Auto-generated method stub
                        return null;
                    }
                    
                    @Override
                    public BufferedImage readImage(File file, int page) throws IOExceptionUnchecked
                    {
                        // TODO Auto-generated method stub
                        return null;
                    }
                    
                    @Override
                    public String getName()
                    {
                        return formatReaderOrNull.getClass().getSimpleName();
                    }
                    
                    @Override
                    public String getLibraryName()
                    {
                        return BioFormatsReaderLibrary.this.getName();
                    }
                };
        }
    }

    @Override
    public IImageReader tryGetReaderForFile(String fileName)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
