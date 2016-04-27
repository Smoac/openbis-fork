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

package ch.systemsx.cisd.ant.task.subversion;

import java.io.File;

/**
 * A role for exporting single files from a subversion repository.
 * 
 * @author Bernd Rinn
 */
public interface ISVNFileExporter
{

    /**
     * Exports the HEAD of one file to the file system.
     * 
     * @param repositoryUrl The url in the subversion repository to check out.
     * @param targetDirectory The name of the directory to check out the file to to relative to some
     *            base directory.
     * @throws SVNException If there is a problem with performing the <code>export</code> command.
     */
    public void export(String repositoryUrl, File targetDirectory) throws SVNException;

}
