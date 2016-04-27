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
 * A role that performs a <code>status</code> query on a subversion working copy.
 * <p>
 * This query works on a subversion working copy.
 * 
 * @author Bernd Rinn
 */
interface ISVNStatus
{

    /**
     * Performs a <code>status</code> query on a subversion repository.
     * 
     * @param path The path of the file or directory in the subversion repository to perform the
     *            query on.
     * @return The changed entries (as compared to the <code>BASE</code> revision of the
     *         repository) as returned by subversion.
     * @throws SVNException If there is a problem with performing the <code>status</code> query.
     */
    public List<SVNItemStatus> status(String path);

}
