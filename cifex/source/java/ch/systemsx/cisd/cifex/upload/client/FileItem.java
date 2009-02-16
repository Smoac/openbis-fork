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

package ch.systemsx.cisd.cifex.upload.client;

import java.io.File;

import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * Encapsulates a a file and its upload status.
 *
 * @author Franz-Josef Elmer
 */
final class FileItem 
{
    private final ITimeProvider timeProvider;
    private final File file;
    private final long length;
    
    private FileItemStatus status;
    private long uploadStartTime;
    private long numberOfBytesUploaded;
    
    FileItem(File file, ITimeProvider timeProvider)
    {
        this.file = file;
        this.timeProvider = timeProvider;
        length = file.length();
        status = FileItemStatus.NOT_STARTED;
    }
    
    File getFile()
    {
        return file;
    }
    
    long getLength()
    {
        return length;
    }

    FileItemStatus getStatus()
    {
        return status;
    }

    void setStatus(FileItemStatus status)
    {
        this.status = status;
    }

    long getNumberOfBytesUploaded()
    {
        return numberOfBytesUploaded;
    }

    void setNumberOfBytesUploaded(long numberOfBytesUploaded)
    {
        if (numberOfBytesUploaded <= 0)
        {
            uploadStartTime = timeProvider.getTimeInMilliseconds();
        }
        this.numberOfBytesUploaded = numberOfBytesUploaded;
    }
    
    long getEstimatedTimeOfArrival()
    {
        long duration = timeProvider.getTimeInMilliseconds() - uploadStartTime;
        return (long) (duration * (((double) length - numberOfBytesUploaded) / Math.max(
                numberOfBytesUploaded, 1)));
    }

    @Override
    public String toString()
    {
        return file.getName();
    }
}