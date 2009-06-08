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

package ch.systemsx.cisd.cifex.server.business;

import java.io.File;

import ch.systemsx.cisd.cifex.server.business.dto.FileDTO;
import ch.systemsx.cisd.cifex.server.business.dto.UserDTO;

/**
 * Interface of the manager for triggers.
 * 
 * @author Bernd Rinn
 */
public interface ITriggerManager
{
    /**
     * Returns <code>true</code>, if the given <var>user</var> is a trigger user.
     */
    public boolean isTriggerUser(UserDTO user);

    /**
     * Trigger on <var>file</var> with the given <var>fileDTO</var> for the given
     * <var>triggerUser</var>
     * 
     * @return <code>true</code>, if the file should be dismissed after running the trigger and
     *         <code>false</code> otherwise.
     */
    public boolean handle(UserDTO triggerUser, FileDTO fileDTO, File file, IFileManager fileManager);
}
