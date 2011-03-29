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

package ch.systemsx.cisd.imagereaders;

import java.io.File;
import java.util.Map;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.base.io.IRandomAccessFile;

/**
 * An {@link IImageReader} offering support for reading image metadata.
 * 
 * @author Kaloyan Enimanev
 */
public interface IMetaDataAwareImageReader extends IImageReader
{

    /**
     * Reads image metadata from {@link File}.
     * 
     * @param file the image file
     * @param params read parameters
     */
    public Map<String, Object> readMetaData(File file, IReadParams params)
            throws IOExceptionUnchecked;

    /**
     * Reads image metadata from byte array.
     * 
     * @param bytes the image file as a byte array
     * @param params read parameters
     */
    public Map<String, Object> readMetaData(byte[] bytes, IReadParams params);

    /**
     * Reads image metadata from handle.
     * 
     * @param handle the image file as {@link IRandomAccessFile}
     * @param params read parameters
     */
    public Map<String, Object> readMetaData(IRandomAccessFile handle, IReadParams params)
            throws IOExceptionUnchecked;

}
