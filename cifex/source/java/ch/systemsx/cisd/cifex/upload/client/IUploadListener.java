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

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
public interface IUploadListener
{
    public void uploadingStarted(File file, long fileSize);
    
    public void uploadingProgress(int percentage, long numberOfBytes);
    
    public void fileUploaded();
    
    public void uploadingFinished(boolean successful);
    
    public void exceptionOccured(Throwable throwable);

    public void reset();
}
