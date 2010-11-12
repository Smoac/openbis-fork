/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.datamover.filesystem.intf;

import java.io.File;

import ch.systemsx.cisd.common.filesystem.StoreItem;

/**
 * An {@link IFileStore} with additional capabilites.
 * 
 * @author Bernd Rinn
 */
public interface IExtendedFileStore extends IFileStore
{

    public boolean createNewFile(StoreItem item);

    /**
     * @return the target file of the move, or <code>null</code> if the operation fails.
     */

    public File tryMoveLocal(StoreItem sourceItem, File destinationDir, String newFilePrefix);

}