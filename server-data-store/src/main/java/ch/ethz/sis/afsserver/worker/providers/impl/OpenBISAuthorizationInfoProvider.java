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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.sis.afsserver.startup.AtomicFileSystemServerParameter;
import ch.ethz.sis.afsserver.worker.WorkerContext;
import ch.ethz.sis.afsserver.worker.providers.AuthorizationInfoProvider;
import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.id.ObjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.interfaces.IPermIdHolder;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.Experiment;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.fetchoptions.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.IExperimentId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Right;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.Rights;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.rights.fetchoptions.RightsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.shared.io.FilePermission;
import ch.ethz.sis.shared.io.IOUtils;
import ch.ethz.sis.shared.startup.Configuration;
import ch.systemsx.cisd.common.spring.HttpInvokerUtils;

public class OpenBISAuthorizationInfoProvider implements AuthorizationInfoProvider
{

    private IApplicationServerApi v3 = null;

    @Override
    public void init(Configuration initParameter) throws Exception
    {
        String openBISUrl = initParameter.getStringProperty(AtomicFileSystemServerParameter.openBISUrl);
        int openBISTimeout = initParameter.getIntegerProperty(AtomicFileSystemServerParameter.openBISTimeout);
        v3 = HttpInvokerUtils.createServiceStub(IApplicationServerApi.class, openBISUrl, openBISTimeout);

        String storageUuid = initParameter.getStringProperty(AtomicFileSystemServerParameter.storageUuid);
        if (storageUuid == null || storageUuid.isBlank())
        {
            throw new RuntimeException("Configuration parameter '" + AtomicFileSystemServerParameter.storageUuid + "' cannot be null or empty.");
        }
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
            ownerSupportedPermissions = Set.of(FilePermission.Read, FilePermission.Write);
        } else
        {
            Sample foundSample = findSample(workerContext.getSessionToken(), owner);

            if (foundSample != null)
            {
                ownerPermId = foundSample.getPermId();
                ownerSupportedPermissions = Set.of(FilePermission.Read, FilePermission.Write);
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
            workerContext.setOwnerShareId(ownerShare);
            workerContext.setOwnerShards(IOUtils.getShards(ownerPermId.getPermId()));
            workerContext.setOwnerFolder(ownerPermId.getPermId());
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

        Rights rights = v3.getRights(workerContext.getSessionToken(), List.of(ownerPermId), new RightsFetchOptions()).get(ownerPermId);

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

    public IPermIdHolder findOwner(String sessionToken, String owner)
    {
        Experiment foundExperiment = findExperiment(sessionToken, owner);

        if (foundExperiment != null)
        {
            return foundExperiment;
        }

        Sample foundSample = findSample(sessionToken, owner);

        if (foundSample != null)
        {
            return foundSample;
        }

        return findDataSet(sessionToken, owner);
    }

    public Experiment findExperiment(String sessionToken, String experimentPermIdOrIdentifier)
    {
        IExperimentId experimentId;

        if (experimentPermIdOrIdentifier.contains("/"))
        { // Is Identifier
            experimentId = new ExperimentIdentifier(experimentPermIdOrIdentifier);
        } else
        { // Is permId
            experimentId = new ExperimentPermId(experimentPermIdOrIdentifier);
        }

        Map<IExperimentId, Experiment> experiments = v3.getExperiments(sessionToken, List.of(experimentId), new ExperimentFetchOptions());

        if (!experiments.isEmpty())
        {
            return experiments.values().iterator().next();
        } else
        {
            return null;
        }
    }

    public Sample findSample(String sessionToken, String samplePermIdOrIdentifier)
    {
        ISampleId sampleId;

        if (samplePermIdOrIdentifier.contains("/"))
        { // Is Identifier
            sampleId = new SampleIdentifier(samplePermIdOrIdentifier);
        } else
        { // Is permId
            sampleId = new SamplePermId(samplePermIdOrIdentifier);
        }

        Map<ISampleId, Sample> samples = v3.getSamples(sessionToken, List.of(sampleId), new SampleFetchOptions());

        if (!samples.isEmpty())
        {
            return samples.values().iterator().next();
        } else
        {
            return null;
        }
    }

    public DataSet findDataSet(String sessionToken, String dataSetPermId)
    {
        IDataSetId dataSetId = new DataSetPermId(dataSetPermId);

        DataSetFetchOptions fo = new DataSetFetchOptions();
        fo.withPhysicalData();

        Map<IDataSetId, DataSet> dataSets = v3.getDataSets(sessionToken, List.of(dataSetId), fo);

        if (!dataSets.isEmpty())
        {
            return dataSets.values().iterator().next();
        } else
        {
            return null;
        }
    }

}
