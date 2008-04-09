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
 * A wrapper for the multi-url command line client
 * <code>svnmucc</var> (formerly known as <code>mucc</code>).
 *
 * @author Bernd Rinn
 */
interface ISVNMucc
{
    /**
     * @return <code>true</code> if <code>svnmucc</code> is available (note that the client is
     *         still optional).
     */
    public boolean isMuccAvailable();

    /**
     * Call <code>svnmucc</code> with the given <var>commandLine</var>.
     * 
     * @param logMessage The log message to set.
     * @param args The command line arguments to provide to <code>svnmucc</code>. See the help
     *            output of for what is acceptable.
     */
    public void mucc(String logMessage, String... args) throws SVNException;

}
