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

import java.io.File;

import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.highwatermark.HostAwareFileWithHighwaterMark;
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
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FileStoreFactory.class);

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
            final String lastchangedExecutableOrNull)
    {
        return new FileStoreRemote(path, kind, factory, skipAccessibilityTest,
                findExecutableOrNull, lastchangedExecutableOrNull);
    }

    /**
     * use when file store is on a local host.
     */
    public static final IFileStore createLocal(final HostAwareFileWithHighwaterMark path,
            final String kind, final IFileSysOperationsFactory factory,
            final boolean skipAccessibilityTest)
    {
        return new FileStoreLocal(path, kind, factory, skipAccessibilityTest);
    }

    /**
     * use when file store is on a local host.
     */
    public static final IFileStore createLocal(final File readyToMoveDir, final String string,
            final IFileSysOperationsFactory factory, final boolean skipAccessibilityTest)
    {
        return createLocal(new HostAwareFileWithHighwaterMark(readyToMoveDir), string, factory,
                skipAccessibilityTest);
    }

    /**
     * <i>For unit tests only!</i>
     * <p>
     * Use when file store is on a remote share mounted on local host
     */
    @Private
    public static final IFileStore createRemoteHost(final File path, final String host,
            final String rsyncModuleOrNull, final String kind,
            final IFileSysOperationsFactory factory)
    {
        return createRemoteHost(new HostAwareFileWithHighwaterMark(host, path, rsyncModuleOrNull),
                kind, factory, false, null, null);
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
            final String lastchangedExecutableOrNull, final long checkIntervalMillis)
    {
        if (path.tryGetHost() != null)
        {
            if (operationLog.isDebugEnabled())
            {
                operationLog.debug(String.format(
                        "Create %s store for remote host %s, path %s, timeout: %f s.", kind, path
                                .tryGetHost(), path.getFile().toString(),
                        FileStoreRemote.LONG_SSH_TIMEOUT_MILLIS / 1000.0));
            }
            return createRemoteHost(path, kind, factory, skipAccessibilityTest,
                    findExecutableOrNull, lastchangedExecutableOrNull);
        } else
        {
            final long timoutMillis = checkIntervalMillis * 3;
            if (isRemote)
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(
                            "Create %s store for mounted path %s, timeout: %f s.", kind, path
                                    .getFile().toString(), timoutMillis / 1000.0));
                }
                return new FileStoreRemoteMounted(path, kind, factory, skipAccessibilityTest,
                        timoutMillis);
            } else
            {
                if (operationLog.isDebugEnabled())
                {
                    operationLog.debug(String.format(
                            "Create %s store for local path %s.", kind, path
                                    .getFile().toString()));
                }
                return createLocal(path, kind, factory, skipAccessibilityTest);
            }
        }
    }
}
