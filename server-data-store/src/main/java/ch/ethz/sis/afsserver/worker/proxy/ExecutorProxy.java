/*
 * Copyright ETH 2022 - 2023 Zürich, Scientific IT Services
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
package ch.ethz.sis.afsserver.worker.proxy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import ch.ethz.sis.afs.exception.AFSExceptions;
import ch.ethz.sis.afsapi.dto.File;
import ch.ethz.sis.afsapi.dto.FreeSpace;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.afsserver.worker.AbstractProxy;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.shared.startup.Configuration;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;
import lombok.NonNull;

public class ExecutorProxy extends AbstractProxy
{

    private final IApplicationServerApi v3;

    private final String storageRoot;

    private final String storageUuid;

    public ExecutorProxy(final Configuration configuration)
    {
        super(null);

        storageRoot = configuration.getStringProperty(AtomicFileSystemServerParameter.storageRoot);
        storageUuid = configuration.getStringProperty(AtomicFileSystemServerParameter.storageUuid);

        if (storageUuid != null && !storageUuid.isBlank())
        {
            String openBISUrl = configuration.getStringProperty(AtomicFileSystemServerParameter.openBISUrl);

            if (openBISUrl == null || openBISUrl.isBlank())
            {
                throw new RuntimeException(
                        "Incorrect configuration. '" + AtomicFileSystemServerParameter.openBISUrl + "' property is mandatory when '"
                                + AtomicFileSystemServerParameter.storageUuid + "' is set.");
            }

            String openBISTimeout = configuration.getStringProperty(AtomicFileSystemServerParameter.openBISTimeout);

            if (openBISTimeout == null || openBISTimeout.isBlank())
            {
                throw new RuntimeException(
                        "Incorrect configuration. '" + AtomicFileSystemServerParameter.openBISTimeout + "' property is mandatory when '"
                                + AtomicFileSystemServerParameter.storageUuid + "' is set.");
            }

            v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, openBISUrl,
                    configuration.getIntegerProperty(AtomicFileSystemServerParameter.openBISTimeout));
        } else
        {
            v3 = null;
        }
    }

    //
    // Transaction Management
    //

    @Override
    public void begin(UUID transactionId) throws Exception
    {
        workerContext.setTransactionId(transactionId);
        workerContext.getConnection().begin(transactionId);
    }

    @Override
    public Boolean prepare() throws Exception
    {
        return workerContext.getConnection().prepare();
    }

    @Override
    public void commit() throws Exception
    {
        workerContext.getConnection().commit();
    }

    @Override
    public void rollback() throws Exception
    {
        workerContext.getConnection().rollback();
    }

    @Override
    public List<UUID> recover() throws Exception
    {
        return workerContext.getConnection().recover();
    }

    //
    // File System Operations
    //

    private String getOwnerPath(String owner)
    {
        if (storageUuid == null || storageUuid.isBlank())
        {
            // AFS does not reuse DSS store folder
            return joinPaths("", owner);
        } else
        {
            // AFS reuses DSS store folder
            IPermIdHolder foundOwner = ProxyUtil.findOwner(v3, workerContext.getSessionToken(), owner);

            if (foundOwner == null)
            {
                throw AFSExceptions.NotAPath.getInstance(owner);
            }

            String foundOwnerPath = null;

            if (foundOwner instanceof DataSet)
            {
                DataSet foundDataSet = (DataSet) foundOwner;
                foundOwnerPath = foundDataSet.getPhysicalData().getShareId() + "/" + foundDataSet.getPhysicalData().getLocation();
            } else
            {
                String[] shares = IOUtils.getShares(storageRoot);

                if (shares.length == 0)
                {
                    throw AFSExceptions.NotAPath.getInstance(owner);
                }

                String[] shards = IOUtils.getShards(owner);

                for (String share : shares)
                {
                    String potentialOwnerPath = share + "/" + storageUuid + "/" + String.join("/", shards) + "/" + foundOwner.getPermId().toString();
                    if (Files.exists(Paths.get(potentialOwnerPath)))
                    {
                        foundOwnerPath = potentialOwnerPath;
                        break;
                    }
                }

                if (foundOwnerPath == null)
                {
                    // if we don't find an existing owner folder at any share, then we will create it on the first share
                    foundOwnerPath = shares[0] + "/" + storageUuid + "/" + String.join("/", shards) + "/" + foundOwner.getPermId().toString();
                }
            }

            return joinPaths("", foundOwnerPath);
        }
    }

    private String getSourcePath(String owner, String source)
    {
        return joinPaths(getOwnerPath(owner), source);
    }

    private String joinPaths(String... paths)
    {
        return String.join("" + IOUtils.PATH_SEPARATOR, paths);
    }

    @Override
    public List<File> list(String owner, String source, Boolean recursively) throws Exception
    {
        String ownerPath = getOwnerPath(owner);
        String sourcePath = joinPaths(ownerPath, source);
        return workerContext.getConnection().list(sourcePath, recursively)
                .stream()
                .map(file -> convertToFile(owner, ownerPath, file))
                .collect(Collectors.toList());
    }

    private File convertToFile(String owner, String ownerPath, ch.ethz.sis.afs.api.dto.File file)
    {
        try
        {
            String ownerFullPath = new java.io.File(joinPaths(this.storageRoot, ownerPath)).getCanonicalPath();
            String fileFullPath;

            if (file.getPath().startsWith(this.storageRoot))
            {
                fileFullPath = new java.io.File(file.getPath()).getCanonicalPath();
            } else
            {
                fileFullPath = new java.io.File(joinPaths(this.storageRoot, file.getPath())).getCanonicalPath();
            }

            return new File(owner, fileFullPath.substring(ownerFullPath.length()), file.getName(), file.getDirectory(), file.getSize(),
                    file.getLastModifiedTime(), file.getCreationTime(), file.getLastAccessTime());
        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] read(String owner, String source, Long offset, Integer limit) throws Exception
    {
        return workerContext.getConnection().read(getSourcePath(owner, source), offset, limit);
    }

    @Override
    public Boolean write(String owner, String source, Long offset, byte[] data, byte[] md5Hash) throws Exception
    {
        return workerContext.getConnection().write(getSourcePath(owner, source), offset, data, md5Hash);
    }

    @Override
    public Boolean delete(String owner, String source) throws Exception
    {
        return workerContext.getConnection().delete(getSourcePath(owner, source));
    }

    @Override
    public Boolean copy(String sourceOwner, String source, String targetOwner, String target) throws Exception
    {
        return workerContext.getConnection().copy(getSourcePath(sourceOwner, source), getSourcePath(targetOwner, target));
    }

    @Override
    public Boolean move(String sourceOwner, String source, String targetOwner, String target) throws Exception
    {
        return workerContext.getConnection().move(getSourcePath(sourceOwner, source), getSourcePath(targetOwner, target));
    }

    @Override
    public @NonNull Boolean create(@NonNull final String owner, @NonNull final String source, @NonNull final Boolean directory)
            throws Exception
    {
        return workerContext.getConnection().create(getSourcePath(owner, source), directory);
    }

    @Override
    public @NonNull FreeSpace free(@NonNull final String owner, @NonNull final String source) throws Exception
    {
        final ch.ethz.sis.afs.api.dto.Space space = workerContext.getConnection().free(getSourcePath(owner, source));
        return new FreeSpace(space.getTotal(), space.getFree());
    }

}
