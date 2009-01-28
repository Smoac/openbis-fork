/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.server.util;

import static org.apache.commons.io.FilenameUtils.EXTENSION_SEPARATOR_STR;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.io.FilenameUtils;

/**
 * Utilities for file names.
 *
 * @author Franz-Josef Elmer
 */
public class FilenameUtilities
{
    private static final int EXTENSION_SEPARATOR_LENGTH = EXTENSION_SEPARATOR_STR.length();
    
    private static final MimetypesFileTypeMap fileTypeToMIMETypeMap = new MimetypesFileTypeMap();
    
    static 
    {
        fileTypeToMIMETypeMap.addMimeTypes("application/pdf pdf PDF");
    }
    
    private FilenameUtilities()
    {
    }
    
    /**
     * Returns the MIME type of the specified file name.
     */
    public static String getMimeType(String filename)
    {
        return fileTypeToMIMETypeMap.getContentType(filename);
    }

    /**
     * Ensures that the specified filename is not longer than the specified maximum length.
     * 
     * @return Either the original filename or a filename which isn't longer than 
     *          <code>maximumLength</code>.
     */
    public static String ensureMaximumSize(String filename, int maximumLength)
    {
        if (filename.length() <= maximumLength)
        {
            return filename;
        }
        String extension = FilenameUtils.getExtension(filename);
        int extensionLength = extension.length();
        int maxLengthWithoutExtension = maximumLength - extensionLength - EXTENSION_SEPARATOR_LENGTH;
        if (maxLengthWithoutExtension < 1)
        {
            return filename.substring(0, maximumLength);
        }
        String filenameWithoutExtension = FilenameUtils.removeExtension(filename);
        return filenameWithoutExtension.substring(0, maxLengthWithoutExtension)
                + EXTENSION_SEPARATOR_STR + extension;
    }
}
