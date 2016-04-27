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
 * A role that performs a <code>checkout</code> command on a subversion repository.
 * <p>
 * This command works on a subversion repsitory.
 * 
 * @author Bernd Rinn
 */
interface ISVNCheckout
{

    /**
     * Checks out a project.
     * 
     * @param repositoryUrl The url in the subversion repository to check out.
     * @param projectName The name of the directory to check out to relative to some base directory.
     * @param revision The revision in the repository to check out.
     * @throws SVNException If there is a problem with performing the <code>checkout</code> command.
     */
    public void checkout(String repositoryUrl, String projectName, String revision) throws SVNException;

    /**
     * @return The directory where working copies are created by {@link #checkout(String,String,String)}.
     */
    public String getDirectoryToCheckout();

}