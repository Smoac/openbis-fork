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
import java.util.LinkedHashSet;
import java.util.Set;

import ch.systemsx.cisd.cifex.upload.IUploadService;
import ch.systemsx.cisd.cifex.upload.UploadState;
import ch.systemsx.cisd.cifex.upload.UploadStatus;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class Uploader
{
    private final Set<IUploadListener> listeners = new LinkedHashSet<IUploadListener>();
    private final IUploadService uploadService;
    private final String uploadSessionID;

    Uploader(IUploadService uploadService, String uploadSessionID)
    {
        this.uploadService = uploadService;
        this.uploadSessionID = uploadSessionID;
    }
    
    void addUploadListener(IUploadListener uploadListener)
    {
        listeners.add(uploadListener);
    }
    
    void upload()
    {
        UploadStatus status = getStatus();
        UploadState state = status.getUploadState();
        if (state == UploadState.INIT)
        {
            String fileName = status.getCurrentFile();
            File file = new File(fileName);
            long fileSize = file.length();
            int blockIndex = status.getBlockIndex();
            long numberOfBytes = blockIndex * UploadStatus.BLOCK_SIZE;
            int percentage = (int) ((numberOfBytes * 100) / Math.max(1, fileSize));
            for (IUploadListener listener : listeners)
            {
                listener.uploadingStarted(file);
                listener.uploadingProgress(percentage, numberOfBytes);
            }
        }
    }

    private UploadStatus getStatus()
    {
        return uploadService.getUploadStatus(uploadSessionID);
    }
}
