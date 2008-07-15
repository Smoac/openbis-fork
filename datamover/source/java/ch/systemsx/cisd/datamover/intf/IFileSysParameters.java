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

package ch.systemsx.cisd.datamover.intf;

/**
 * @author Tomasz Pylak
 */
public interface IFileSysParameters
{
    /**
     * The path to the <code>ln</code> executable file for creating hard links.
     */
    String getHardLinkExecutable();

    /**
     * The name of the <code>rsync</code> executable to use for copy operations.
     */
    String getRsyncExecutable();

    /**
     * @return <code>true</code>, if rsync is called in such a way to files that already exist
     *         are overwritten rather than appended to.
     */

    boolean isRsyncOverwrite();

    /**
     * The name of the <code>ssh</code> executable to use for creating tunnels.
     */
    String getSshExecutable();

    /**
     * @return <code>true</code>, if <code>rsync</code> should be used for creating the
     *         additional hard link copies, <code>false</code> if <code>ln</code> should be
     *         called on every file individually (which is a lot slower).
     */
    boolean useRsyncForExtraCopies();
    
    /**
     * @return The time interval to wait after a retriable error has occurred before a new attempt
     *         is made.
     */
    public long getIntervalToWaitAfterFailure();

    /**
     * @return The maximal number of retries of a failed retriable operation before giving up on it.
     */
    public int getMaximalNumberOfRetries();

}
