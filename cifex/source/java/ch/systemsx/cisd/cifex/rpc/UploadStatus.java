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

package ch.systemsx.cisd.cifex.rpc;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;

/**
 * Status of an uploading session.
 *
 * @author Franz-Josef Elmer
 */
public class UploadStatus implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final Set<String> uploadedFiles = new HashSet<String>();
    
    private String[] files;
    private int indexOfCurrentFile;
    private long filePointer;
    private UploadState uploadState = UploadState.INITIALIZED;
    
    public void reset()
    {
        files = null;
        filePointer = 0;
        indexOfCurrentFile = 0;
    }

    /**
     * Sets the absolute paths of all files to be uploaded.
     */
    public final void setFiles(String[] files)
    {
        this.files = files;
        if (files != null)
        {
            indexOfCurrentFile = findNextIndex(0);
        }
    }
    
    private int findNextIndex(int startIndex)
    {
        for (int i = startIndex; i < files.length; i++)
        {
            if (uploadedFiles.contains(files[i]) == false)
            {
                return i;
            }
        }
        return files.length;
    }
    
    /**
     * Goes to the next file in the list of files to be uploaded. Also changes the state and
     * resets the file pointer.
     */
    public void next()
    {
        uploadedFiles.add(getCurrentFile());
        indexOfCurrentFile = findNextIndex(indexOfCurrentFile);
        filePointer = 0;
        uploadState = indexOfCurrentFile < files.length ? UploadState.READY_FOR_NEXT_FILE : UploadState.FINISHED;
    }
    
    /**
     * Returns the absolute path of the current file to be uploaded.
     */
    public String getCurrentFile()
    {
        return files[indexOfCurrentFile];
    }
    
    /**
     * Returns only the name of the current file to be uploaded.
     */
    public String getNameOfCurrentFile()
    {
        return FilenameUtils.getName(getCurrentFile());
    }

    /**
     * Returns the current upload state.
     */
    public final UploadState getUploadState()
    {
        return uploadState;
    }

    /**
     * Sets the upload state.
     */
    public final void setUploadState(UploadState uploadState)
    {
        this.uploadState = uploadState;
    }

    /**
     * Returns the file pointer.
     */
    public final long getFilePointer()
    {
        return filePointer;
    }

    /**
     * Sets the file pointer.
     */
    public final void setFilePointer(long filePointer)
    {
        this.filePointer = filePointer;
    }

    /**
     * Renders this status in a human readable way.
     */
    @Override
    public String toString()
    {
        return "UploadStatus[" + uploadState + ",fileIndex=" + indexOfCurrentFile + ",filePointer="
                + filePointer + "]";
    }
    
}
