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

package ch.systemsx.cisd.ant.task.subversion;

import java.util.List;

/**
 * A role that performs a <code>list</code> query on a subversion repository.
 * <p>
 * This query works on a subversion repository.
 * 
 * @author Bernd Rinn
 */
interface ISVNList
{

    /**
     * Performs a <code>list</code> on a file or directory in subversion.
     * 
     * @param pathOrUrl The path of the file or directory in the subversion repository.
     * @return The list of files and directories in the <var>path</code> (directories end with a slash).
     * @throws SVNException If there is a problem with performing the <code>list</code> query.
     */
    public List<String> list(String pathOrUrl) throws SVNException;

}
