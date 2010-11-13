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
     * The name of the <code>rsync</code> executable to use for copy operations.
     */
    String getRsyncExecutable();

    /**
     * The name of the <code>ln</code> executable to use for hard link operations.
     */
    String getLnExecutable();

    /**
     * @return The name of the <code>rsync</code> executable on the incoming host to use for copy
     *         operations.
     */
    public String getIncomingRsyncExecutable();

    /**
     * @return The name of the <code>rsync</code> executable on the outgoing host to use for copy
     *         operations.
     */
    public String getOutgoingRsyncExecutable();

    /**
     * @return <code>true</code>, if rsync is called in such a way to files that already exist
     *         are overwritten rather than appended to.
     */
    boolean isRsyncOverwrite();

    /**
     * @return Additional parameters to be added to the end of the <code>rsync</code> command line.
     */
    public String[] getExtraRsyncParameters();

    /**
     * The name of the <code>ssh</code> executable to use for creating tunnels.
     */
    String getSshExecutable();

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
