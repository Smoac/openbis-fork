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
 * @author Tomasz Pylak on Sep 7, 2007
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
     * @return <code>true</code>, if rsync is called in such a way to files that already exist are overwritten rather
     *         than appended to.
     */

    boolean isRsyncOverwrite();

    /**
     * The name of the <code>ssh</code> executable to use for creating tunnels.
     */
    String getSshExecutable();
}
