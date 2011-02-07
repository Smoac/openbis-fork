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

package ch.systemsx.cisd.etlserver.registrator.api.v1.impl;

import java.io.File;

import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;

/**
 * Creates a directory and all necessary intermediate dirctories.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
class NewFileCommand extends AbstractTransactionalCommand
{
    private static final long serialVersionUID = 1L;

    private final String fileAbsolutePath;

    NewFileCommand(String directoryAbsoutePath)
    {
        this.fileAbsolutePath = directoryAbsoutePath;

    }

    public void execute()
    {
        File src = new File(fileAbsolutePath);

        IFileOperations fileOperations = FileOperations.getMonitoredInstanceForCurrentThread();
        fileOperations.createNewFile(src);
    }

    public void rollback()
    {
        File src = new File(fileAbsolutePath);

        if (false == src.exists())
        {
            // Nothing to rollback
            return;
        }

        IFileOperations fileOperations = FileOperations.getMonitoredInstanceForCurrentThread();
        fileOperations.delete(src);
    }
}
