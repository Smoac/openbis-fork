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

package ch.systemsx.cisd.cifex.rpc.client;

import java.io.File;

import ch.systemsx.cisd.common.utilities.ITimeProvider;

/**
 * Encapsulates a a file and its upload status.
 * 
 * @author Franz-Josef Elmer
 */
public final class FileItem
{
    private final File originalFile;
    
    private File uploadedFile;

    private final long length;

    private FileItemStatus status;

    private long numberOfBytesUploaded;

    private final TransmissionSpeedCalculator transmissionSpeedCalculator;

    public FileItem(File file, ITimeProvider timeProvider)
    {
        this.originalFile = file;
        this.uploadedFile = file;
        transmissionSpeedCalculator = new TransmissionSpeedCalculator(timeProvider);
        length = file.length();
        status = FileItemStatus.NOT_STARTED;
    }

    public File getFile()
    {
        return originalFile;
    }

    public File getUploadedFile()
    {
        return uploadedFile;
    }

    public void setUploadedFile(File uploadedFile)
    {
        this.uploadedFile = uploadedFile;
    }

    public long getLength()
    {
        return length;
    }

    public FileItemStatus getStatus()
    {
        return status;
    }

    public void setStatus(FileItemStatus status)
    {
        this.status = status;
    }

    public long getNumberOfBytesUploaded()
    {
        return numberOfBytesUploaded;
    }

    public void setNumberOfBytesUploaded(long numberOfBytesUploaded)
    {
        setStatus(FileItemStatus.UPLOADING);
        int transmittedSinceLastUpdate = (int) (numberOfBytesUploaded - this.numberOfBytesUploaded);
        transmissionSpeedCalculator.noteTransmittedBytesSinceLastUpdate(transmittedSinceLastUpdate);

        this.numberOfBytesUploaded = numberOfBytesUploaded;
    }

    /** return -1 if the estimated time is unknown */
    public long getEstimatedTimeOfArrival()
    {
        float remainingBytes = (length - numberOfBytesUploaded);
        float bytesPerMillisecond = transmissionSpeedCalculator.getEstimatedBytesPerMillisecond();
        if (bytesPerMillisecond < 0.001)
        {
            return -1;
        }
        return (long) (remainingBytes / bytesPerMillisecond);
    }

    @Override
    public String toString()
    {
        return originalFile.getName();
    }
}