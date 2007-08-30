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

package ch.systemsx.cisd.datamover.intf;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.Status;

/**
 * Interface that represents a role that deletes files and directories from a file system.
 * 
 * @author Bernd Rinn
 */
public interface IPathRemover
{

    /**
     * Removes the <var>path</var>, if necessary recursively.
     * 
     * @param path The file or directory to remove. It needs to be writable (including all entries in case of a
     *            directory).
     * @return The status of the operation, {@link Status#OK} if everything went fine. If the <var>path</var> didn't
     *         exist when the method is called, {@link Status#OK} will be returned as well.
     */
    public Status remove(File path);

}
