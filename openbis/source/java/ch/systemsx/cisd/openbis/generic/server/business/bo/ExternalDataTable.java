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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivizationStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.translator.ExternalDataTranslator;
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

    private static void assertDatasetsAreAvailable(List<ExternalDataPE> datasets)
    {
        List<String> notAvailableDatasets = new ArrayList<String>();
        for (ExternalDataPE dataSet : datasets)
        {
            if (dataSet.getStatus().isAvailable() == false)
            {
                notAvailableDatasets.add(dataSet.getCode());
            }
        }
        throwUnavailableOperationExceptionIfNecessary(notAvailableDatasets);
    }

    private static void throwUnavailableOperationExceptionIfNecessary(
            List<String> unavailableDatasets)
    {
        if (unavailableDatasets.isEmpty() == false)
        {
            throw UserFailureException.fromTemplate(
                    "Operation failed because following data sets are not available "
                            + "(they are archived or their status is pending): %s. "
                            + "Unarchive these data sets or filter them out using data set status "
                            + "before performing the operation once again.", CollectionUtils
                            .abbreviate(unavailableDatasets, 10));
        }
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

    public void loadByDataSetCodes(List<String> dataSetCodes, boolean lockForUpdate)
    {
        IExternalDataDAO externalDataDAO = getExternalDataDAO();
        externalData = new ArrayList<ExternalDataPE>();
        for (String dataSetCode : dataSetCodes)
        {
            ExternalDataPE dataSet =
                    externalDataDAO.tryToFindFullDataSetByCode(dataSetCode, true, lockForUpdate);
            if (dataSet != null)
            {
                externalData.add(dataSet);
            }
        }
    }

    public final void loadBySampleTechId(final TechId sampleId)
    {
        assert sampleId != null : "Unspecified sample id";
        final SamplePE sample = getSampleDAO().getByTechId(sampleId);
        externalData = new ArrayList<ExternalDataPE>();
        externalData.addAll(getExternalDataDAO().listExternalData(sample));
    }

    public void loadByExperimentTechId(final TechId experimentId)
    {
        assert experimentId != null : "Unspecified experiment id";

        ExperimentPE experiment = getExperimentDAO().getByTechId(experimentId);
        externalData = new ArrayList<ExternalDataPE>();
        externalData.addAll(getExternalDataDAO().listExternalData(experiment));
    }

    public void deleteLoadedDataSets(String reason)
    {
        assertDatasetsAreAvailable(externalData);
        Map<DataStorePE, List<ExternalDataPE>> map = groupDataSetsByDataStores();
        assertDataSetsAreKnown(map);
        for (Map.Entry<DataStorePE, List<ExternalDataPE>> entry : map.entrySet())
        {
            DataStorePE dataStore = entry.getKey();
            List<ExternalDataPE> dataSets = entry.getValue();
            // delete locally from DB
            for (ExternalDataPE dataSet : dataSets)
            {
                deleteDataSetLocally(dataSet, reason);
            }
            // delete remotely from Data Store
            deleteDataSets(dataStore, getLocations(dataSets));
        }
    }

    private void deleteDataSetLocally(ExternalDataPE dataSet, String reason)
            throws UserFailureException
    {
        try
        {
            getExternalDataDAO().delete(dataSet);
            getEventDAO().persist(createDeletionEvent(dataSet, session.tryGetPerson(), reason));
        } catch (final DataIntegrityViolationException ex)
        {
            // needed because we throw an exception in DAO instead of relying on DB FK integrity
            throwEntityInUseException(String.format("Data Set '%s'", dataSet.getCode()),
                    EntityKind.DATA_SET);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Data Set '%s'", dataSet.getCode()),
                    EntityKind.DATA_SET);
        }
    }

    public static EventPE createDeletionEvent(ExternalDataPE dataSet, PersonPE registrator,
            String reason)
    {
        EventPE event = new EventPE();
        event.setEventType(EventType.DELETION);
        event.setEntityType(EntityType.DATASET);
        event.setIdentifier(dataSet.getCode());
        event.setDescription(getDeletionDescription(dataSet));
        event.setReason(reason);
        event.setRegistrator(registrator);

        return event;
    }

    private static String getDeletionDescription(ExternalDataPE dataSet)
    {
        return dataSet.getIdentifier();
    }

    public String uploadLoadedDataSetsToCIFEX(DataSetUploadContext uploadContext)
    {
        assertDatasetsAreAvailable(externalData);
        Map<DataStorePE, List<ExternalDataPE>> map = groupDataSetsByDataStores();
        assertDataSetsAreKnown(map);
        uploadContext.setUserEMail(session.getPrincipal().getEmail());
        uploadContext.setSessionUserID(session.getUserName());
        if (StringUtils.isBlank(uploadContext.getComment()))
        {
            uploadContext.setComment(createUploadComment(externalData));
        }
        List<ExternalDataPE> dataSetsWithUnknownDSS = new ArrayList<ExternalDataPE>();
        for (Map.Entry<DataStorePE, List<ExternalDataPE>> entry : map.entrySet())
        {
            DataStorePE dataStore = entry.getKey();
            List<ExternalDataPE> dataSets = entry.getValue();
            for (ExternalDataPE dataSet : dataSets)
            {
                HibernateUtils.initialize(dataSet.getParents());
                HibernateUtils.initialize(dataSet.getProperties());
                SamplePE sampleOrNull = dataSet.tryGetSample();
                ExperimentPE experiment;
                if (sampleOrNull != null) // needed? dataSet should always have experiment
                {
                    experiment = sampleOrNull.getExperiment();
                } else
                {
                    experiment = dataSet.getExperiment();
                }
                HibernateUtils.initialize(experiment.getProject().getGroup());
                HibernateUtils.initialize(experiment.getProperties());
            }
            if (StringUtils.isBlank(dataStore.getRemoteUrl()))
            {
                dataSetsWithUnknownDSS.addAll(dataSets);
            } else
            {
                uploadDataSetsToCIFEX(dataStore, dataSets, uploadContext);
            }
        }
        StringBuilder builder = new StringBuilder();
        if (dataSetsWithUnknownDSS.isEmpty() == false)
        {
            builder
                    .append("The following data sets couldn't been uploaded because of unkown data store:");
            for (ExternalDataPE externalDataPE : dataSetsWithUnknownDSS)
            {
                builder.append(' ').append(externalDataPE.getCode());
            }
        }
        return builder.toString();
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
        List<ExternalData> cleanDataSets = ExternalDataTranslator.translate(dataSets, "?", "?");
        service.uploadDataSetsToCIFEX(sessionToken, cleanDataSets, context);
    }

    private void deleteDataSets(DataStorePE dataStore, List<String> locations)
    {
        IDataStoreService service = tryGetDataStoreService(dataStore);
        if (service == null)
        {
            // Nothing to delete on dummy data store
            return;
        }
        String sessionToken = dataStore.getSessionToken();
        service.deleteDataSets(sessionToken, locations);
    }

    // null if DSS URL has not been specified
    private IDataStoreService tryGetDataStoreService(DataStorePE dataStore)
    {
        String remoteURL = dataStore.getRemoteUrl();
        if (StringUtils.isBlank(remoteURL))
        {
            return null;
        }
        return dssFactory.create(remoteURL);
    }

    private List<String> getKnownDataSets(DataStorePE dataStore, List<String> locations)
    {
        String remoteURL = dataStore.getRemoteUrl();
        if (StringUtils.isBlank(remoteURL))
        {
            // Assuming dummy data store "knows" all locations
            return locations;
        }
        IDataStoreService service = dssFactory.create(remoteURL);
        String sessionToken = dataStore.getSessionToken();
        return service.getKnownDataSets(sessionToken, locations);
    }

    public void processDatasets(String datastoreServiceKey, String datastoreCode,
            List<String> datasetCodes)
    {
        DataStorePE dataStore = findDataStore(datastoreCode);
        IDataStoreService service = tryGetDataStoreService(dataStore);
        if (service == null)
        {
            throw createUnknownDataStoreServerException();
        }
        List<DatasetDescription> locations = loadAvailableDatasetDescriptions(datasetCodes);
        String sessionToken = dataStore.getSessionToken();
        service.processDatasets(sessionToken, datastoreServiceKey, locations,
                tryGetLoggedUserEmail());
    }

    private String tryGetLoggedUserEmail()
    {
        return session.tryGetPerson() == null ? null : session.tryGetPerson().getEmail();
    }

    private ConfigurationFailureException createUnknownDataStoreServerException()
    {
        return new ConfigurationFailureException(
                "Connection to Data Store Server has not been configured. "
                        + "Conntact your administrator.");
    }

    public TableModel createReportFromDatasets(String datastoreServiceKey, String datastoreCode,
            List<String> datasetCodes)
    {
        DataStorePE dataStore = findDataStore(datastoreCode);
        IDataStoreService service = tryGetDataStoreService(dataStore);
        if (service == null)
        {
            throw createUnknownDataStoreServerException();
        }
        List<DatasetDescription> locations = loadAvailableDatasetDescriptions(datasetCodes);
        String sessionToken = dataStore.getSessionToken();
        return service.createReportFromDatasets(sessionToken, datastoreServiceKey, locations);
    }

    private List<DatasetDescription> loadAvailableDatasetDescriptions(List<String> datasetCodes)
    {
        IExternalDataDAO externalDataDAO = getExternalDataDAO();
        List<DatasetDescription> result = new ArrayList<DatasetDescription>();
        List<String> notAvailableDatasets = new ArrayList<String>();
        for (String datasetCode : datasetCodes)
        {
            ExternalDataPE dataSet =
                    externalDataDAO.tryToFindFullDataSetByCode(datasetCode, false, false);
            if (dataSet != null)
            {
                if (dataSet.getStatus().isAvailable())
                {
                    result.add(createDatasetDescription(dataSet));
                } else
                {
                    notAvailableDatasets.add(datasetCode);
                }
            }
        }
        throwUnavailableOperationExceptionIfNecessary(notAvailableDatasets);
        return result;
    }

    private List<DatasetDescription> createDatasetDescriptions(List<ExternalDataPE> datasets)
    {
        List<DatasetDescription> result = new ArrayList<DatasetDescription>();
        for (ExternalDataPE dataset : datasets)
        {
            result.add(createDatasetDescription(dataset));
        }
        return result;
    }

    private DatasetDescription createDatasetDescription(ExternalDataPE dataSet)
    {
        assert dataSet != null;

        String datasetCode = dataSet.getCode();
        String location = dataSet.getLocation();
        SamplePE sample = dataSet.tryGetSample();
        String sampleCode = sample == null ? null : sample.getCode();
        ExperimentPE experiment = dataSet.getExperiment();
        ProjectPE project = experiment.getProject();
        String groupCode = project.getGroup().getCode();
        String projectCode = project.getCode();
        String experimentCode = experiment.getCode();
        String instanceCode = project.getGroup().getDatabaseInstance().getCode();

        return new DatasetDescription(datasetCode, location, sampleCode, groupCode, projectCode,
                experimentCode, dataSet.getDataSetType().getMainDataSetPattern(), dataSet
                        .getDataSetType().getMainDataSetPath(), instanceCode);
    }

    private DataStorePE findDataStore(String datastoreCode)
    {
        DataStorePE dataStore = getDataStoreDAO().tryToFindDataStoreByCode(datastoreCode);
        if (dataStore == null)
        {
            throw new IllegalStateException("Cannot find the data store " + datastoreCode);
        }
        return dataStore;
    }

    public void loadByDataStore(DataStorePE dataStore)
    {
        assert dataStore != null : "Unspecified data store";
        assert externalData == null : "Data already loaded";
        externalData = new ArrayList<ExternalDataPE>();
        externalData.addAll(getExternalDataDAO().listExternalData(dataStore));
    }

    //
    // Archivization
    //

    public void archiveDatasets()
    {
        Map<DataStorePE, List<ExternalDataPE>> datasetsByStore = groupDataSetsByDataStores();
        filterByStatusAndUpdate(datasetsByStore, DataSetArchivizationStatus.ACTIVE,
                DataSetArchivizationStatus.ARCHIVIZATION_IN_PROGRESS);
        performArchivization(datasetsByStore);
    }

    public void unarchiveDatasets()
    {
        Map<DataStorePE, List<ExternalDataPE>> datasetsByStore = groupDataSetsByDataStores();
        filterByStatusAndUpdate(datasetsByStore, DataSetArchivizationStatus.ARCHIVED,
                DataSetArchivizationStatus.ACTIVATION_IN_PROGRESS);
        performUnarchivization(datasetsByStore);
    }

    private void filterByStatusAndUpdate(Map<DataStorePE, List<ExternalDataPE>> datasetsByStore,
            DataSetArchivizationStatus oldStatus, DataSetArchivizationStatus newStatus)
    {
        IExternalDataDAO externalDataDAO = getExternalDataDAO();
        for (List<ExternalDataPE> dataSets : datasetsByStore.values())
        {
            Iterator<ExternalDataPE> iterator = dataSets.iterator();
            while (iterator.hasNext())
            {
                ExternalDataPE dataSet = iterator.next();
                if (dataSet.getStatus() != oldStatus)
                {
                    iterator.remove();
                } else
                {
                    dataSet.setStatus(newStatus);
                    externalDataDAO.validateAndSaveUpdatedEntity(dataSet);
                }
            }
        }
    }

    private interface IArchivizationAction
    {
        public void execute(String sessionToken, IDataStoreService service,
                List<DatasetDescription> descriptions, String userEmailOrNull);
    }

    private void performUnarchivization(Map<DataStorePE, List<ExternalDataPE>> datasetsByStore)
    {
        performArchivizationAction(datasetsByStore, new IArchivizationAction()
            {
                public void execute(String sessionToken, IDataStoreService service,
                        List<DatasetDescription> descriptions, String userEmailOrNull)
                {
                    service.unarchiveDatasets(sessionToken, descriptions, userEmailOrNull);
                }
            });

    }

    private void performArchivization(Map<DataStorePE, List<ExternalDataPE>> datasetsByStore)
    {
        performArchivizationAction(datasetsByStore, new IArchivizationAction()
            {
                public void execute(String sessionToken, IDataStoreService service,
                        List<DatasetDescription> descriptions, String userEmailOrNull)
                {
                    service.archiveDatasets(sessionToken, descriptions, userEmailOrNull);
                }
            });
    }

    private void performArchivizationAction(Map<DataStorePE, List<ExternalDataPE>> datasetsByStore,
            IArchivizationAction archivizationAction)
    {
        for (Entry<DataStorePE, List<ExternalDataPE>> entry : datasetsByStore.entrySet())
        {
            DataStorePE dataStore = entry.getKey();
            IDataStoreService service = tryGetDataStoreService(dataStore);
            if (service == null)
            {
                throw createUnknownDataStoreServerException();
            }
            List<ExternalDataPE> datasets = entry.getValue();
            List<DatasetDescription> descriptions = createDatasetDescriptions(datasets);
            String sessionToken = dataStore.getSessionToken();
            String userEmailOrNull = tryGetLoggedUserEmail();
            try
            {
                archivizationAction.execute(sessionToken, service, descriptions, userEmailOrNull);
            } catch (Exception e)
            {
                throw UserFailureException
                        .fromTemplate(
                                "Operation couldn't be performed for following datasets: %s. "
                                        + "Archiver may not be configured properly. Please contact your administrator.",
                                CollectionUtils.abbreviate(Code.extractCodes(datasets), 10));
            }
        }
    }
}
