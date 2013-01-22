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

package ch.systemsx.cisd.datamover.filesystem;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.filesystem.highwatermark.HostAwareFileWithHighwaterMark;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.datamover.filesystem.intf.AbstractFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileStore;
import ch.systemsx.cisd.datamover.filesystem.intf.IFileSysOperationsFactory;
import ch.systemsx.cisd.datamover.filesystem.store.FileStoreLocal;
import ch.systemsx.cisd.datamover.filesystem.store.FileStoreRemote;
import ch.systemsx.cisd.datamover.filesystem.store.FileStoreRemoteMounted;

/**
 * A {@link AbstractFileStore} factory.
 * 
 * @author Tomasz Pylak
 */
public final class FileStoreFactory
{
    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FileStoreFactory.class);

    private FileStoreFactory()
    {
        // This class can not be instantiated.
    }

    /**
     * use when file store is on a remote share mounted on local host
     */
    private static final IFileStore createRemoteHost(final HostAwareFileWithHighwaterMark path,
            final String kind, final IFileSysOperationsFactory factory,
            final boolean skipAccessibilityTest, final String findExecutableOrNull,
            final String lastchangedExecutableOrNull, final long remoteConnectionTimeoutMillis,
            final long remoteOperationTimeoutMillis)
    {
        return new FileStoreRemote(path, kind, factory, skipAccessibilityTest,
                findExecutableOrNull, lastchangedExecutableOrNull, remoteConnectionTimeoutMillis,
                remoteOperationTimeoutMillis);
    }

    /**
     * use when file store is on a local host.
     */
    public static final IFileStore createLocal(final HostAwareFileWithHighwaterMark path,
            final String kind, final IFileSysOperationsFactory factory,
            final boolean skipAccessibilityTest, final long remoteConnectionTimeoutMillis)
    {
        return new FileStoreLocal(path, kind, factory, skipAccessibilityTest,
                remoteConnectionTimeoutMillis);
    }

    /**
     * use when file store is on a local host.
     */
    public static final IFileStore createLocal(final String readyToMoveDir, final String string,
            final IFileSysOperationsFactory factory, final boolean skipAccessibilityTest,
            final long remoteConnectionTimeoutMillis)
    {
        return createLocal(new HostAwareFileWithHighwaterMark(readyToMoveDir), string, factory,
                skipAccessibilityTest, remoteConnectionTimeoutMillis);
    }

    /**
     * <i>For unit tests only!</i>
     * <p>
     * Use when file store is on a remote share mounted on local host
     */
    @Private
    public static final IFileStore createRemoteHost(final String path, final String host,
            final String rsyncModuleOrNull, final String kind,
            final IFileSysOperationsFactory factory, final long remoteConnectionTimeoutMillis,
            final long remoteOperationTimeoutMillis)
    {
        return createRemoteHost(new HostAwareFileWithHighwaterMark(host, path, rsyncModuleOrNull),
                kind, factory, false, null, null, remoteConnectionTimeoutMillis,
                remoteOperationTimeoutMillis);
    }

    /**
     * Returns the most convenient <code>IFileStore</code> implementation with given
     * <var>values</var>.
     * 
     * @param checkIntervalMillis only used if given <var>isRemote</var> is <code>true</code>.
     */
    public final static IFileStore createStore(final HostAwareFileWithHighwaterMark path,
            final String kind, final boolean isRemote, final IFileSysOperationsFactory factory,
            final boolean skipAccessibilityTest, final String findExecutableOrNull,
            final String lastchangedExecutableOrNull, final long checkIntervalMillis,
            final long remoteConnectionTimeoutMillis, final long remoteOperationTimeoutMillis)
    {
        if (path.tryGetHost() != null)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format(
                        "Create %s store for remote host %s, path %s, connection timeout: %d s, " +
                                "operation timeout: %d s.", kind, path.tryGetHost(),
                        path.getPath(), remoteConnectionTimeoutMillis / 1000,
                        remoteOperationTimeoutMillis / 1000));
            }
            return createRemoteHost(path, kind, factory, skipAccessibilityTest,
                    findExecutableOrNull, lastchangedExecutableOrNull,
                    remoteConnectionTimeoutMillis, remoteOperationTimeoutMillis);
        } else
        {
            if (isRemote)
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(
                            "Create %s store for mounted path %s, timeout: %d s.", kind, path
                                    .getLocalFile().toString(),
                            remoteOperationTimeoutMillis / 1000L));
                }
                return new FileStoreRemoteMounted(path, kind, factory, skipAccessibilityTest,
                        remoteConnectionTimeoutMillis, remoteOperationTimeoutMillis);
            } else
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format("Create %s store for local path %s.", kind,
                            path.getLocalFile().toString()));
                }
                return createLocal(path, kind, factory, skipAccessibilityTest,
                        remoteConnectionTimeoutMillis);
            }
        }
    }
}
