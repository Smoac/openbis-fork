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
 * A role that performs a <code>cat</code> query on a subversion repository.
 * <p>
 * This query works on a subversion repository.
 * 
 * @author Bernd Rinn
 */
interface ISVNCat
{

    /**
     * Performs a <code>cat</code> command on a file in subversion.
     * 
     * @param pathOrUl The path of the file in the subversion repository.
     * @return The content of the file specified by <var>path</code>.
     * @throws SVNException If there is a problem with performing the <code>cat</code> query.
     */
    public String cat(String pathOrUl) throws SVNException;

}