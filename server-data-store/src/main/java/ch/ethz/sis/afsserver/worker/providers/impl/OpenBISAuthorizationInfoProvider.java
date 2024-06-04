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
package ch.ethz.sis.afsserver.worker.providers.impl;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.afs.exception.AFSExceptions;
import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameterUtil;
import ch.ethz.sis.afsserver.worker.WorkerContext;
import ch.ethz.sis.afsserver.worker.providers.AuthorizationInfoProvider;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Right;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Rights;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.fetchoptions.RightsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.shared.io.FilePermission;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.shared.startup.Configuration;

public class OpenBISAuthorizationInfoProvider implements AuthorizationInfoProvider
{

    private String storageRoot;

    private String storageUuid;

    private String[] storageShares;

    private String storageIncomingShareId;

    private IApplicationServerApi applicationServerApi;

    @Override
    public void init(Configuration initParameter) throws Exception
    {
        storageRoot = AtomicFileSystemServerParameterUtil.getStorageRoot(initParameter);
        storageUuid = AtomicFileSystemServerParameterUtil.getStorageUuid(initParameter);
        storageShares = IOUtils.getShares(storageRoot);
        if (storageShares.length == 0)
        {
            throw AFSExceptions.NoSharesFound.getInstance();
        }
        storageIncomingShareId = AtomicFileSystemServerParameterUtil.getStorageIncomingShareId(initParameter);
        applicationServerApi = AtomicFileSystemServerParameterUtil.getApplicationServerApi(initParameter);
    }

    @Override
    public boolean doesSessionHaveRights(WorkerContext workerContext, String owner, Set<FilePermission> permissions)
    {
        String ownerShare = null;
        ObjectPermId ownerPermId = null;
        Set<FilePermission> ownerSupportedPermissions = null;

        Experiment foundExperiment = findExperiment(workerContext.getSessionToken(), owner);

        if (foundExperiment != null)
        {
            ownerPermId = foundExperiment.getPermId();

            if (foundExperiment.isFrozen())
            {
                ownerSupportedPermissions = Set.of(FilePermission.Read);
            } else
            {
                ownerSupportedPermissions = Set.of(FilePermission.Read, FilePermission.Write);
            }
        } else
        {
            Sample foundSample = findSample(workerContext.getSessionToken(), owner);

            if (foundSample != null)
            {
                ownerPermId = foundSample.getPermId();

                if (foundSample.isFrozen())
                {
                    ownerSupportedPermissions = Set.of(FilePermission.Read);
                } else
                {
                    ownerSupportedPermissions = Set.of(FilePermission.Read, FilePermission.Write);
                }
            } else
            {
                DataSet foundDataSet = findDataSet(workerContext.getSessionToken(), owner);

                if (foundDataSet != null)
                {
                    ownerPermId = foundDataSet.getPermId();
                    ownerShare = foundDataSet.getPhysicalData().getShareId();
                    ownerSupportedPermissions = Set.of(FilePermission.Read);
                }
            }
        }

        if (ownerPermId == null)
        {
            return false;
        }

        if (hasPermissions(workerContext, ownerPermId, ownerSupportedPermissions, permissions))
        {
            String ownerPath = findOwnerPath(ownerPermId, ownerShare);
            workerContext.getOwnerPathMap().put(owner, ownerPath);
            return true;
        } else
        {
            return false;
        }
    }

    private boolean hasPermissions(WorkerContext workerContext, ObjectPermId ownerPermId, Set<FilePermission> ownerSupportedPermissions,
            Set<FilePermission> requestedPermissions)
    {
        for (FilePermission requestPermission : requestedPermissions)
        {
            if (!ownerSupportedPermissions.contains(requestPermission))
            {
                return false;
            }
        }

        Set<FilePermission> foundPermissions = new HashSet<>();
        foundPermissions.add(FilePermission.Read);

        if (requestedPermissions.equals(foundPermissions))
        {
            return true;
        }

        Rights rights =
                applicationServerApi.getRights(workerContext.getSessionToken(), List.of(ownerPermId), new RightsFetchOptions()).get(ownerPermId);

        if (rights.getRights().contains(Right.UPDATE))
        {
            foundPermissions.add(FilePermission.Write);
        }

        for (FilePermission requestedPermission : requestedPermissions)
        {
            if (!foundPermissions.contains(requestedPermission))
            {
                return false;
            }
        }

        return true;
    }

    private Experiment findExperiment(String sessionToken, String experimentPermId)
    {
        Map<IExperimentId, Experiment> experiments =
                applicationServerApi.getExperiments(sessionToken, List.of(new ExperimentPermId(experimentPermId)), new ExperimentFetchOptions());

        if (!experiments.isEmpty())
        {
            return experiments.values().iterator().next();
        } else
        {
            return null;
        }
    }

    private Sample findSample(String sessionToken, String samplePermId)
    {
        Map<ISampleId, Sample> samples =
                applicationServerApi.getSamples(sessionToken, List.of(new SamplePermId(samplePermId)), new SampleFetchOptions());

        if (!samples.isEmpty())
        {
            return samples.values().iterator().next();
        } else
        {
            return null;
        }
    }

    private DataSet findDataSet(String sessionToken, String dataSetPermId)
    {
        IDataSetId dataSetId = new DataSetPermId(dataSetPermId);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        Map<IDataSetId, DataSet> dataSets = applicationServerApi.getDataSets(sessionToken, List.of(dataSetId), fo);

        if (!dataSets.isEmpty())
        {
            return dataSets.values().iterator().next();
        } else
        {
            return null;
        }
    }

    private String findOwnerPath(ObjectPermId ownerPermId, String ownerShare)
    {
        final String[] shards = IOUtils.getShards(ownerPermId.getPermId());

        if (ownerShare != null)
        {
            return createOwnerPath(ownerShare, storageUuid, shards, ownerPermId.getPermId());
        } else
        {
            for (String share : storageShares)
            {
                String potentialOwnerPath = createOwnerPath(share, storageUuid, shards, ownerPermId.getPermId());

                if (Files.exists(Paths.get(storageRoot, potentialOwnerPath)))
                {
                    return potentialOwnerPath;
                }
            }

            return createOwnerPath(storageIncomingShareId, storageUuid, shards, ownerPermId.getPermId());
        }
    }

    private String createOwnerPath(String shareId, String storageUuid, String[] shards, String ownerFolder)
    {
        List<String> elements = new LinkedList<>();
        elements.add(shareId);
        elements.add(storageUuid);
        elements.addAll(Arrays.asList(shards));
        elements.add(ownerFolder);
        return IOUtils.getPath("", elements.toArray(new String[] {}));
    }

}
