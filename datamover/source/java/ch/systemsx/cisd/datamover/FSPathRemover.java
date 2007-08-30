/*
 * Copyright 2007 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.utilities.FileUtilities;
import ch.systemsx.cisd.datamover.intf.IPathRemover;

/**
 * Removes a path (file or directory) from the file system, if necessary recursively.
 * 
 * @author Bernd Rinn
 */
public class FSPathRemover implements IPathRemover
{

    private final Status STATUS_FAILED_DELETION = new Status(StatusFlag.FATAL_ERROR, "Failed to remove path.");

    public Status remove(File path)
    {
        final boolean deletionOK = FileUtilities.deleteRecursively(path);

        return deletionOK ? Status.OK : STATUS_FAILED_DELETION;
    }

}
