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

package ch.systemsx.cisd.common.filesystem;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.utilities.ISelfTestable;
import ch.systemsx.cisd.common.utilities.ITerminable;

/**
 * Interface that represents a role that copies files and directories to another directory, usually
 * on another file system.
 * <p>
 * <i>Note: If the copier is terminated, the <var>destinationDirectory</var> is in an undefined
 * state afterwards.</i>
 * 
 * @author Bernd Rinn
 * @author Tomasz Pylak
 */
public interface IPathCopier extends ITerminable, ISelfTestable
{

    /**
     * Copies <var>sourcePath</var> to <var>destinationDir</var>.
     * 
     * @param sourcePath The source to copy. Can be a file or a directory. It needs to exist and be
     *            readable.
     * @param destinationDirectory The directory to use as a destination in the copy operation. It
     *            must be readable and writable. If <var>destinationDir/sourcePath</var> exists, it
     *            will be overwritten.
     * @return The status of the operation, {@link Status#OK} if everything went OK.
     */
    Status copy(File sourcePath, File destinationDirectory);

    /**
     * Copies <var>sourcePath</var> to <var>destinationDir</var> on <var>destinationHost</var>.
     * 
     * @param sourcePath The source to copy. Can be a file or a directory. It needs to exist and be
     *            readable.
     * @param destinationDirectory The directory to use as a destination in the copy operation. It
     *            must be readable and writable. If <var>destinationDir/sourcePath</var> exists, it
     *            will be overwritten.
     * @param destinationHostOrNull The host where the <var>destinationDirectory</var> resides or
     *            null if it is local.
     * @param rsyncModuleNameOrNull The name of the rsync module to use in the rsync protocol, or
     *            <code>null</code>, if the bulk transfer should be using an ssh tunnel.
     * @param rsyncPasswordFileOrNull The name of the password file to use if an rsync server is
     *            used. May be <code>null</code> or the name of a non-existent file, in which case
     *            no password is used when accessing the rsync server.
     * @return The status of the operation, {@link Status#OK} if everything went OK.
     */
    Status copyToRemote(File sourcePath, File destinationDirectory, String destinationHostOrNull,
            String rsyncModuleNameOrNull, String rsyncPasswordFileOrNull);

    /**
     * Copies <var>sourcePath</var> on <var>sourceHost</var> to <var>destinationDir</var>.
     * 
     * @param sourcePath The source to copy. Can be a file or a directory. It needs to exist and be
     *            readable.
     * @param sourceHost The host where the <var>sourcePath</var> resides
     * @param destinationDirectory The directory to use as a destination in the copy operation. It
     *            must be readable and writable. If <var>destinationDir/sourcePath</var> exists, it
     *            will be overwritten.
     * @param rsyncModuleNameOrNull The name of the rsync module to use in the rsync protocol, or
     *            <code>null</code>, if the bulk transfer should be using an ssh tunnel.
     * @param rsyncPasswordFileOrNull The name of the password file to use if an rsync server is
     *            used. May be <code>null</code> or the name of a non-existent file, in which case
     *            no password is used when accessing the rsync server.
     * @return The status of the operation, {@link Status#OK} if everything went OK.
     */
    Status copyFromRemote(File sourcePath, String sourceHost, File destinationDirectory,
            String rsyncModuleNameOrNull, String rsyncPasswordFileOrNull);

    /**
     * Try to connect to the <var>host</var> and see whether the connection is OK.
     * 
     * @return <code>true</code> if the connection was successful and <code>false</code> otherwise.
     */
    boolean checkRsyncConnection(String host, String rsyncModuleOrNull,
            String rsyncPassworFileOrNull);
}
