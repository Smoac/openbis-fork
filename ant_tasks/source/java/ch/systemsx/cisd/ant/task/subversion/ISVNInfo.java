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

/**
 * A role that performs an <code>info</code> query on a subversion working copy.
 * <p>
 * This query works on a subversion working copy.
 * 
 * @author Bernd Rinn
 */
interface ISVNInfo
{

    /**
     * Performs an <code>info</code> query on a subversion repository.
     * 
     * @param pathOrUrl The path of the file or directory in the subversion repository to perform
     *            the query on.
     * @return The info record as returned by subversion.
     * @throws SVNException If there is a problem with performing the <code>info</code> query.
     */
    public SVNInfoRecord info(String pathOrUrl);

}
