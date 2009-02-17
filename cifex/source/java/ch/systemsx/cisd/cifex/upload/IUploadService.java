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

/**
 * Service interface for file uploading.
 *
 * @author Franz-Josef Elmer
 */
public interface IUploadService
{
    /**
     * Cancels the specified upload session.
     */
    public void cancel(String uploadSessionID);

    /**
     * Defines the upload parameters for the specified upload session.
     * 
     * @param files Absolute file path of files to be uploaded.
     * @param recipients Whitespace of comma separated list of recipients (e-mail address or
     *        <code>id:<em>userID</em></code>).
     * @param comment Comment to be added in recipient notification.
     */
    public void defineUploadParameters(String uploadSessionID, String[] files,
            String recipients, String comment);
    
    /**
     * Returns the status of the specified upload session.
     */
    public UploadStatus getUploadStatus(String uploadSessionID);
    
    /**
     * Starts uploading of the specified upload session.
     */
    public void startUploading(String uploadSessionID);
    
    /**
     * Uploads a data block for the specified upload session.
     * 
     * @param block Block of data bytes.
     * @param blockSize Number of bytes of <code>block</code> which are to be taken.
     * @param lastBlock <code>true</code> if <code>block</code> is the last block of a file to be
     *      uploaded. 
     */
    public void uploadBlock(String uploadSessionID, byte[] block, int blockSize, boolean lastBlock);

    /**
     * Finishes the specified upload session.
     * 
     * @param successful Flag indicating whether the uploading was successful or not.
     */
    public void finish(String uploadSessionID, boolean successful);
    
    /**
     * Closes the specified upload session.
     */
    public void close(String uploadSessionID);
}
