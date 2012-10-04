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

import ch.systemsx.cisd.common.reflection.AbstractHashable;

/**
 * A data transfer object for file preregistration information.
 *
 * @author Bernd Rinn
 */
public final class FilePreregistrationDTO extends AbstractHashable implements Serializable
{
    
    private static final long serialVersionUID = 1L;

    private final String filePathOnClient;
    
    private final long fileSize;

    public FilePreregistrationDTO(String fileName, long fileSize)
    {
        this.filePathOnClient = fileName;
        this.fileSize = fileSize;
    }

    public final String getFilePathOnClient()
    {
        return filePathOnClient;
    }

    public final long getFileSize()
    {
        return fileSize;
    }

}
