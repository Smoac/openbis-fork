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

package ch.systemsx.cisd.datamover.filesystem.intf;

import java.io.File;

/**
 * A roles that allows moving paths within a file system, possibly adding a prefix.
 * 
 * @author Bernd Rinn
 */
public interface IPathMover
{

    /**
     * Moves source path (file or directory) to destination directory, putting <var>prefixTemplate</var>
     * in front of its name, where any occurrence of '%t' in the template is replaced with the
     * current time stamp.
     */
    public File tryMove(File sourcePath, File destinationDirectory, String prefixTemplate);

    /**
     * Moves source path (file or directory) to destination directory.
     */
    public File tryMove(File sourcePath, File destinationDir);

}
