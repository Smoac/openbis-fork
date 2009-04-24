/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.util.HibernateTransformer;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * The only productive implementation of {@link IExternalDataTable}.
 * <p>
 * We are using an interface here to keep the system testable.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ExternalDataTable extends AbstractExternalDataBusinessObject implements
        IExternalDataTable
{
    @Private
    static final String UPLOAD_COMMENT_TEXT = "Uploaded zip file contains the following data sets:";

    @Private
    static final String NEW_LINE = "\n";

    @Private
    static final String AND_MORE_TEMPLATE = "and %d more.";

    @Private
    static final String DELETION_DESCRIPTION = "single deletion";

    @Private
    static String createUploadComment(List<ExternalDataPE> dataSets)
    {
        StringBuilder builder = new StringBuilder(UPLOAD_COMMENT_TEXT);
        for (int i = 0, n = dataSets.size(); i < n; i++)
        {
            builder.append(NEW_LINE);
            String code = dataSets.get(i).getCode();
            int length = builder.length() + code.length();
            if (i < n - 1)
            {
                length += NEW_LINE.length() + String.format(AND_MORE_TEMPLATE, n - i - 1).length();
            }
            if (length < BasicConstant.MAX_LENGTH_OF_CIFEX_COMMENT)
            {
                builder.append(code);
            } else
            {
                builder.append(String.format(AND_MORE_TEMPLATE, n - i));
                break;
            }
        }
        return builder.toString();
    }

    private final IDataStoreServiceFactory dssFactory;

    private List<ExternalDataPE> externalData;

    public ExternalDataTable(final IDAOFactory daoFactory, IDataStoreServiceFactory dssFactory,
            final Session session)
    {
        super(daoFactory, session);
        this.dssFactory = dssFactory;
    }

    //
    // IExternalDataTable
    //

    public final List<ExternalDataPE> getExternalData()
    {
        assert externalData != null : "External data not loaded.";
        return externalData;
    }

    public void setExternalData(List<ExternalDataPE> externalData)
    {
        this.externalData = externalData;
    }

    public void loadByDataSetCodes(List<String> dataSetCodes)
    {
        IExternalDataDAO externalDataDAO = getExternalDataDAO();
        externalData = new ArrayList<ExternalDataPE>();
        for (String dataSetCode : dataSetCodes)
        {
            ExternalDataPE dataSet = externalDataDAO.tryToFindFullDataSetByCode(dataSetCode);
            if (dataSet != null)
            {
                externalData.add(dataSet);
            }
        }
    }

    public final void loadBySampleIdentifier(final SampleIdentifier sampleIdentifier)
    {
        assert sampleIdentifier != null : "Unspecified sample identifier";
        externalData = new ArrayList<ExternalDataPE>();
        final SamplePE sample = getSampleByIdentifier(sampleIdentifier);
        externalData.addAll(getExternalDataDAO().listExternalData(sample, SourceType.MEASUREMENT));
        externalData.addAll(getExternalDataDAO().listExternalData(sample, SourceType.DERIVED));
        for (ExternalDataPE externalDataPE : externalData)
        {
            enrichWithParentsAndExperiment(externalDataPE);
            HibernateUtils.initialize(externalDataPE.getProperties());
        }
    }

    public void loadByExperimentIdentifier(ExperimentIdentifier identifier)
    {
        assert identifier != null : "Unspecified experiment identifier";

        ProjectPE project =
                getProjectDAO().tryFindProject(identifier.getDatabaseInstanceCode(),
                        identifier.getGroupCode(), identifier.getProjectCode());
        ExperimentPE experiment =
                getExperimentDAO().tryFindByCodeAndProject(project, identifier.getExperimentCode());
        externalData = new ArrayList<ExternalDataPE>();
        List<DataPE> dataSets = experiment.getDataSets();
        for (DataPE dataSet : dataSets)
        {
            if (dataSet.isDeleted() == false && dataSet instanceof ExternalDataPE)
            {
                ExternalDataPE externalDataPE = (ExternalDataPE) dataSet;
                HibernateUtils.initialize(dataSet.getParents());
                HibernateUtils.initialize(dataSet.getProperties());
                enrichWithParentsAndExperiment(externalDataPE);
                externalData.add(externalDataPE);
            }
        }
    }

    public void deleteLoadedDataSets(String reason)
    {
        Map<DataStorePE, List<ExternalDataPE>> map = groupDataSetsByDataStores();
        assertDataSetsAreKnown(map);
        IExternalDataDAO externalDataDAO = getExternalDataDAO();
        for (Map.Entry<DataStorePE, List<ExternalDataPE>> entry : map.entrySet())
        {
            DataStorePE dataStore = entry.getKey();
            List<ExternalDataPE> dataSets = entry.getValue();
            for (ExternalDataPE dataSet : dataSets)
            {
                externalDataDAO.markAsDeleted(dataSet, session.tryGetPerson(),
                        DELETION_DESCRIPTION, reason);
            }
            deleteDataSets(dataStore, getLocations(dataSets));
        }
    }

    public void uploadLoadedDataSetsToCIFEX(DataSetUploadContext uploadContext)
    {
        Map<DataStorePE, List<ExternalDataPE>> map = groupDataSetsByDataStores();
        assertDataSetsAreKnown(map);
        uploadContext.setUserEMail(session.getPrincipal().getEmail());
        if (StringUtils.isBlank(uploadContext.getComment()))
        {
            uploadContext.setComment(createUploadComment(externalData));
        }
        for (Map.Entry<DataStorePE, List<ExternalDataPE>> entry : map.entrySet())
        {
            DataStorePE dataStore = entry.getKey();
            List<ExternalDataPE> dataSets = entry.getValue();
            for (ExternalDataPE dataSet : dataSets)
            {
                HibernateUtils.initialize(dataSet.getParents());
                SamplePE sample = dataSet.getAssociatedSample();
                ExperimentPE experiment;
                if (sample != null)
                {
                    experiment = sample.getExperiment();
                } else
                {
                    experiment = dataSet.getExperiment();
                }
                HibernateUtils.initialize(experiment.getProject().getGroup());
            }
            uploadDataSetsToCIFEX(dataStore, dataSets, uploadContext);
        }
    }

    private void assertDataSetsAreKnown(Map<DataStorePE, List<ExternalDataPE>> map)
    {
        Set<String> knownLocations = new LinkedHashSet<String>();
        for (Map.Entry<DataStorePE, List<ExternalDataPE>> entry : map.entrySet())
        {
            DataStorePE dataStore = entry.getKey();
            List<String> locations = getLocations(entry.getValue());
            knownLocations.addAll(getKnownDataSets(dataStore, locations));
        }
        List<String> unknownDataSets = new ArrayList<String>();
        for (ExternalDataPE dataSet : externalData)
        {
            if (knownLocations.contains(dataSet.getLocation()) == false)
            {
                unknownDataSets.add(dataSet.getCode());
            }
        }
        if (unknownDataSets.isEmpty() == false)
        {
            throw new UserFailureException(
                    "The following data sets are unknown by any registered Data Store Server. "
                            + "May be the responsible Data Store Server is not running.\n"
                            + unknownDataSets);
        }
    }

    private Map<DataStorePE, List<ExternalDataPE>> groupDataSetsByDataStores()
    {
        Map<DataStorePE, List<ExternalDataPE>> map =
                new LinkedHashMap<DataStorePE, List<ExternalDataPE>>();
        for (ExternalDataPE dataSet : externalData)
        {
            DataStorePE dataStore = dataSet.getDataStore();
            List<ExternalDataPE> list = map.get(dataStore);
            if (list == null)
            {
                list = new ArrayList<ExternalDataPE>();
                map.put(dataStore, list);
            }
            list.add(dataSet);
        }
        return map;
    }

    private List<String> getLocations(List<ExternalDataPE> dataSets)
    {
        List<String> locations = new ArrayList<String>();
        for (ExternalDataPE dataSet : dataSets)
        {
            locations.add(dataSet.getLocation());
        }
        return locations;
    }

    private void uploadDataSetsToCIFEX(DataStorePE dataStore, List<ExternalDataPE> dataSets,
            DataSetUploadContext context)
    {
        IDataStoreService service = dssFactory.create(dataStore.getRemoteUrl());
        String sessionToken = dataStore.getSessionToken();
        List<ExternalDataPE> cleanDataSets =
                HibernateTransformer.HIBERNATE_BEAN_REPLICATOR.get().copy(dataSets);
        service.uploadDataSetsToCIFEX(sessionToken, cleanDataSets, context);
    }

    private void deleteDataSets(DataStorePE dataStore, List<String> locations)
    {
        IDataStoreService service = dssFactory.create(dataStore.getRemoteUrl());
        String sessionToken = dataStore.getSessionToken();
        service.deleteDataSets(sessionToken, locations);
    }

    private List<String> getKnownDataSets(DataStorePE dataStore, List<String> locations)
    {
        IDataStoreService service = dssFactory.create(dataStore.getRemoteUrl());
        String sessionToken = dataStore.getSessionToken();
        return service.getKnownDataSets(sessionToken, locations);
    }
}
