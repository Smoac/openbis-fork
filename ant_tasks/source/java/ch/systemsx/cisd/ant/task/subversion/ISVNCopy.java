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
 * A role that performs a <code>copy</code> command on a subversion repository.
 * <p>
 * This command works on a subversion repository.
 * 
 * @author Bernd Rinn
 */
interface ISVNCopy
{

    /**
     * Performs a <code>copy</code> command on a subversion repository.
     * 
     * @param sourcePathOrUrl The path of the file or directory in the subversion repository to copy from.
     * @param sourceRevision The revision of the <var>sourcePath</var> to copy to the <var>destinationPath</var>.
     * @param destinationPathOrUrl The path of the file or directory in the subversion repository to copy from.
     * @param logMessage The log message to set in the revision properties.
     * @throws SVNException If there is a problem with performing the <code>copy</code> command.
     */
    public void copy(String sourcePathOrUrl, String sourceRevision, String destinationPathOrUrl,
            String logMessage) throws SVNException;

}
