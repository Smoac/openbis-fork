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

package ch.systemsx.cisd.cifex.upload;

import java.io.File;
import java.io.Serializable;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public class UploadStatus implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final String[] files;
    private int indexOfCurrentFile;
    private long filePointer;
    
    private UploadState uploadState = UploadState.INIT;

    public UploadStatus(String[] files)
    {
        this.files = files;
    }
    
    public void next()
    {
        indexOfCurrentFile++;
        filePointer = 0;
        uploadState = indexOfCurrentFile < files.length ? UploadState.INIT : UploadState.FINISHED;
    }
    
    public String getCurrentFile()
    {
        return files[indexOfCurrentFile];
    }
    
    public String getNameOfCurrentFile()
    {
        String fileName = getCurrentFile();
        int indexOfLastPathSeparator = fileName.lastIndexOf(File.separatorChar);
        if (indexOfLastPathSeparator > 0)
        {
            fileName = fileName.substring(indexOfLastPathSeparator + 1);
        }
        return fileName;
    }

    public final UploadState getUploadState()
    {
        return uploadState;
    }

    public final void setUploadState(UploadState uploadState)
    {
        this.uploadState = uploadState;
    }

    public final long getFilePointer()
    {
        return filePointer;
    }

    public final void setFilePointer(long filePointer)
    {
        this.filePointer = filePointer;
    }

    @Override
    public String toString()
    {
        return "UploadStatus[" + uploadState + ",fileIndex=" + indexOfCurrentFile + ",filePointer="
                + filePointer + "]";
    }
    
}
