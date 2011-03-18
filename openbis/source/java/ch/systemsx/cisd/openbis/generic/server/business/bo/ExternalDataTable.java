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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.shared.Constants;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LinkModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUploadContext;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePropertyTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventPE.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.dto.EventType;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
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

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            ExternalDataTable.class);

    @Private
    static final String UPLOAD_COMMENT_TEXT = "Uploaded zip file contains the following data sets:";

    @Private
    static final String NEW_LINE = "\n";

    @Private
    static final String AND_MORE_TEMPLATE = "and %d more.";

    private boolean dataChanged;

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
                            + "before performing the operation once again.",
                    CollectionUtils.abbreviate(unavailableDatasets, 10));
        }
    }

    private static void assertDatasetsAreDeletable(List<ExternalDataPE> datasets)
    {
        List<String> notDeletableDatasets = new ArrayList<String>();
        for (ExternalDataPE dataSet : datasets)
        {
            if (dataSet.getStatus().isDeletable() == false)
            {
                notDeletableDatasets.add(dataSet.getCode());
            }
        }
        if (notDeletableDatasets.isEmpty() == false)
        {
            throw UserFailureException.fromTemplate(
                    "Deletion failed because the following data sets are required "
                            + "by a background process (their status is pending): %s. ",
                    CollectionUtils.abbreviate(notDeletableDatasets, 10));
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

    public void loadByDataSetCodes(List<String> dataSetCodes, boolean withProperties,
            boolean lockForUpdate)
    {
        IExternalDataDAO externalDataDAO = getExternalDataDAO();

        externalData = new ArrayList<ExternalDataPE>();
        externalData.addAll(externalDataDAO.tryToFindFullDataSetsByCodes(dataSetCodes,
                withProperties, lockForUpdate));
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
        assertDatasetsAreDeletable(externalData);

        Map<DataStorePE, List<ExternalDataPE>> allToBeDeleted = groupDataSetsByDataStores();
        Map<DataStorePE, List<ExternalDataPE>> availableDatasets =
                filterAvailableDatasets(allToBeDeleted);

        assertDataSetsAreKnown(availableDatasets);
        for (Map.Entry<DataStorePE, List<ExternalDataPE>> entry : allToBeDeleted.entrySet())
        {
            DataStorePE dataStore = entry.getKey();
            List<ExternalDataPE> allDataSets = entry.getValue();
            // delete locally from DB
            for (ExternalDataPE dataSet : allDataSets)
            {
                deleteDataSetLocally(dataSet, reason);
            }
            // delete remotely from Data Store (only executed for available datasets)
            List<ExternalDataPE> available = availableDatasets.get(dataStore);
            deleteDataSets(dataStore, createDatasetDescriptions(available));
        }
    }

    private Map<DataStorePE, List<ExternalDataPE>> filterAvailableDatasets(
            Map<DataStorePE, List<ExternalDataPE>> map)
    {
        Map<DataStorePE, List<ExternalDataPE>> result =
                new HashMap<DataStorePE, List<ExternalDataPE>>();
        for (Map.Entry<DataStorePE, List<ExternalDataPE>> entry : map.entrySet())
        {
            ArrayList<ExternalDataPE> available = new ArrayList<ExternalDataPE>();
            for (ExternalDataPE data : entry.getValue())
            {
                if (data.getStatus().isAvailable())
                {
                    available.add(data);
                }
            }
            result.put(entry.getKey(), available);
        }
        return result;
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
        return dataSet.getLocation();
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
                HibernateUtils.initialize(experiment.getProject().getSpace());
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
            builder.append("The following data sets couldn't been uploaded because of unkown data store:");
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
            List<ExternalDataPE> dataSets = entry.getValue();
            knownLocations.addAll(getKnownDataSets(dataStore, createDatasetDescriptions(dataSets)));
        }
        List<String> unknownDataSets = new ArrayList<String>();
        for (ExternalDataPE dataSet : externalData)
        {
            if (dataSet.getStatus() == DataSetArchivingStatus.ARCHIVED)
            {
                // archived datasets are currently not available in the data store
                // but can be deleted
            } else if (knownLocations.contains(dataSet.getLocation()) == false)
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

    private void uploadDataSetsToCIFEX(DataStorePE dataStore, List<ExternalDataPE> dataSets,
            DataSetUploadContext context)
    {
        IDataStoreService service = dssFactory.create(dataStore.getRemoteUrl());
        String sessionToken = dataStore.getSessionToken();
        List<ExternalData> cleanDataSets = ExternalDataTranslator.translate(dataSets, "?", "?");
        service.uploadDataSetsToCIFEX(sessionToken, cleanDataSets, context);
    }

    private void deleteDataSets(DataStorePE dataStore, List<DatasetDescription> list)
    {
        IDataStoreService service = tryGetDataStoreService(dataStore);
        if (service == null)
        {
            // Nothing to delete on dummy data store
            return;
        }
        String sessionToken = dataStore.getSessionToken();
        service.deleteDataSets(sessionToken, list);
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

    private List<String> getKnownDataSets(DataStorePE dataStore, List<DatasetDescription> dataSets)
    {
        String remoteURL = dataStore.getRemoteUrl();
        if (StringUtils.isBlank(remoteURL))
        {
            // Assuming dummy data store "knows" all locations
            List<String> locations = new ArrayList<String>();
            for (DatasetDescription dataSet : dataSets)
            {
                locations.add(dataSet.getDataSetLocation());
            }
            return locations;
        }
        IDataStoreService service = dssFactory.create(remoteURL);
        String sessionToken = dataStore.getSessionToken();
        return service.getKnownDataSets(sessionToken, dataSets);
    }

    public void processDatasets(String datastoreServiceKey, String datastoreCode,
            List<String> datasetCodes, Map<String, String> parameterBindings)
    {
        DataStorePE dataStore = findDataStore(datastoreCode);
        IDataStoreService service = tryGetDataStoreService(dataStore);
        if (service == null)
        {
            throw createUnknownDataStoreServerException();
        }
        List<DatasetDescription> locations = loadAvailableDatasetDescriptions(datasetCodes);
        String sessionToken = dataStore.getSessionToken();
        parameterBindings.put(Constants.USER_PARAMETER, session.tryGetPerson().getUserId());
        service.processDatasets(sessionToken, datastoreServiceKey, locations, parameterBindings,
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

    private List<DatasetDescription> loadAvailableDatasetDescriptions(List<String> dataSetCodes)
    {
        IExternalDataDAO externalDataDAO = getExternalDataDAO();
        List<DatasetDescription> result = new ArrayList<DatasetDescription>();
        List<String> notAvailableDatasets = new ArrayList<String>();
        List<ExternalDataPE> dataSets =
                externalDataDAO.tryToFindFullDataSetsByCodes(dataSetCodes, false, false);
        for (ExternalDataPE dataSet : dataSets)
        {
            if (dataSet.getStatus().isAvailable())
            {
                result.add(createDatasetDescription(dataSet));
            } else
            {
                notAvailableDatasets.add(dataSet.getCode());
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

        DatasetDescription description = new DatasetDescription();
        description.setDatasetCode(dataSet.getCode());
        description.setDataSetLocation(dataSet.getLocation());
        description.setDataSetSize(dataSet.getSize());
        SamplePE sample = dataSet.tryGetSample();
        if (sample != null)
        {
            description.setSampleCode(sample.getCode());
            description.setSampleIdentifier(sample.getIdentifier());
            description.setSampleTypeCode(sample.getSampleType().getCode());
        }
        ExperimentPE experiment = dataSet.getExperiment();
        description.setExperimentIdentifier(experiment.getIdentifier());
        description.setExperimentTypeCode(experiment.getExperimentType().getCode());
        description.setExperimentCode(experiment.getCode());
        ProjectPE project = experiment.getProject();
        description.setProjectCode(project.getCode());
        SpacePE group = project.getSpace();
        description.setSpaceCode(group.getCode());
        description.setDatabaseInstanceCode(group.getDatabaseInstance().getCode());
        DataSetTypePE dataSetType = dataSet.getDataSetType();
        description.setMainDataSetPath(dataSetType.getMainDataSetPath());
        description.setMainDataSetPattern(dataSetType.getMainDataSetPattern());
        description.setDatasetTypeCode(dataSetType.getCode());

        return description;
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
    // Archiving
    //

    public int archiveDatasets(boolean removeFromDataStore)
    {
        Map<DataStorePE, List<ExternalDataPE>> datasetsByStore = groupDataSetsByDataStores();
        Map<DataStoreWithService, List<ExternalDataPE>> datasetsWithService =
                enrichWithService(datasetsByStore);

        DataSetArchivingStatus pendingStatus =
                (removeFromDataStore) ? DataSetArchivingStatus.ARCHIVE_PENDING
                        : DataSetArchivingStatus.BACKUP_PENDING;
        int result =
                filterByStatusAndUpdate(datasetsByStore, DataSetArchivingStatus.AVAILABLE,
                        pendingStatus);
        performArchiving(datasetsWithService, removeFromDataStore);
        return result;
    }

    public int unarchiveDatasets()
    {
        Map<DataStorePE, List<ExternalDataPE>> datasetsByStore = groupDataSetsByDataStores();
        Map<DataStoreWithService, List<ExternalDataPE>> datasetsWithService =
                enrichWithService(datasetsByStore);
        int result =
                filterByStatusAndUpdate(datasetsByStore, DataSetArchivingStatus.ARCHIVED,
                        DataSetArchivingStatus.UNARCHIVE_PENDING);
        performUnarchiving(datasetsWithService);
        return result;
    }

    public int lockDatasets()
    {
        Map<DataStorePE, List<ExternalDataPE>> datasetsByStore = groupDataSetsByDataStores();
        return filterByStatusAndUpdate(datasetsByStore, DataSetArchivingStatus.AVAILABLE,
                DataSetArchivingStatus.LOCKED);
    }

    public int unlockDatasets()
    {
        Map<DataStorePE, List<ExternalDataPE>> datasetsByStore = groupDataSetsByDataStores();
        return filterByStatusAndUpdate(datasetsByStore, DataSetArchivingStatus.LOCKED,
                DataSetArchivingStatus.AVAILABLE);
    }

    private int filterByStatusAndUpdate(Map<DataStorePE, List<ExternalDataPE>> datasetsByStore,
            DataSetArchivingStatus oldStatus, DataSetArchivingStatus newStatus)
    {
        List<String> codesToUpdate = new ArrayList<String>();
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
                    codesToUpdate.add(dataSet.getCode());
                }
            }
        }
        // WORKAROUND In order not to load data set properties at the end of transaction
        // for Hibernate Search indexing, we don't make change to PE's loaded by Hibernate,
        // but perform a 'bulk update' operation. Such an operation is quicker and Hibernate
        // Search doesn't spot the change. The drawback is that the loaded objects are
        // not updated with a new status.
        externalDataDAO.updateDataSetStatuses(codesToUpdate, newStatus);
        return codesToUpdate.size();
    }

    private interface IArchivingAction
    {
        public void execute(String sessionToken, IDataStoreService service,
                List<DatasetDescription> descriptions, String userEmailOrNull);

        public DataSetArchivingStatus getStatusToRestoreOnFailure();
    }

    private void performUnarchiving(Map<DataStoreWithService, List<ExternalDataPE>> datasetsByStore)
    {
        performArchivingAction(datasetsByStore, new IArchivingAction()
            {
                public void execute(String sessionToken, IDataStoreService service,
                        List<DatasetDescription> descriptions, String userEmailOrNull)
                {
                    service.unarchiveDatasets(sessionToken, descriptions, userEmailOrNull);
                }

                public DataSetArchivingStatus getStatusToRestoreOnFailure()
                {
                    return DataSetArchivingStatus.ARCHIVED;
                }

            });

    }

    private void performArchiving(Map<DataStoreWithService, List<ExternalDataPE>> datasetsByStore,
            final boolean removeFromDataStore)
    {
        performArchivingAction(datasetsByStore, new IArchivingAction()
            {
                public void execute(String sessionToken, IDataStoreService service,
                        List<DatasetDescription> descriptions, String userEmailOrNull)
                {
                    service.archiveDatasets(sessionToken, descriptions, userEmailOrNull,
                            removeFromDataStore);
                }

                public DataSetArchivingStatus getStatusToRestoreOnFailure()
                {
                    return DataSetArchivingStatus.AVAILABLE;
                }
            });
    }

    private static class DataStoreWithService
    {
        DataStorePE dataStore;

        IDataStoreService service;
    }

    private Map<DataStoreWithService, List<ExternalDataPE>> enrichWithService(
            Map<DataStorePE, List<ExternalDataPE>> datasetsByStore)
    {

        Map<DataStoreWithService, List<ExternalDataPE>> result =
                new HashMap<DataStoreWithService, List<ExternalDataPE>>();
        for (Entry<DataStorePE, List<ExternalDataPE>> entry : datasetsByStore.entrySet())
        {
            DataStorePE dataStore = entry.getKey();
            IDataStoreService service = tryGetDataStoreService(dataStore);
            if (service == null)
            {
                throw createUnknownDataStoreServerException();
            }
            List<ExternalDataPE> datasets = entry.getValue();
            DataStoreWithService dataStoreWithService = new DataStoreWithService();
            dataStoreWithService.dataStore = dataStore;
            dataStoreWithService.service = service;

            result.put(dataStoreWithService, datasets);
        }
        return result;
    }

    private void performArchivingAction(
            Map<DataStoreWithService, List<ExternalDataPE>> datasetsByStore,
            IArchivingAction archivingAction)
    {
        Iterator<Entry<DataStoreWithService, List<ExternalDataPE>>> iterator =
                datasetsByStore.entrySet().iterator();

        while (iterator.hasNext())
        {
            Entry<DataStoreWithService, List<ExternalDataPE>> entry = iterator.next();
            DataStorePE dataStore = entry.getKey().dataStore;
            IDataStoreService service = entry.getKey().service;
            List<ExternalDataPE> datasets = entry.getValue();
            if (datasets.isEmpty())
            {
                continue;
            }
            List<DatasetDescription> descriptions = createDatasetDescriptions(datasets);
            String sessionToken = dataStore.getSessionToken();
            String userEmailOrNull = tryGetLoggedUserEmail();
            try
            {
                archivingAction.execute(sessionToken, service, descriptions, userEmailOrNull);
            } catch (Exception e)
            {
                clearPendingStatuses(datasets, iterator,
                        archivingAction.getStatusToRestoreOnFailure());
                throw UserFailureException
                        .fromTemplate(
                                "Operation couldn't be performed for following datasets: %s. "
                                        + "Archiver may not be configured properly. Please contact your administrator.",
                                CollectionUtils.abbreviate(Code.extractCodes(datasets), 10));
            }
        }
    }

    /**
     * clear the pending status of data sets when the remote invocation to DSS has failed.
     */
    private void clearPendingStatuses(List<ExternalDataPE> datasets,
            Iterator<Entry<DataStoreWithService, List<ExternalDataPE>>> iterator,
            DataSetArchivingStatus statusToRestoreOnFailure)
    {
        ArrayList<ExternalDataPE> datasetsWithPendingStatus =
                new ArrayList<ExternalDataPE>(datasets);
        while (iterator.hasNext())
        {
            datasetsWithPendingStatus.addAll(iterator.next().getValue());
        }
        List<String> codes = Code.extractCodes(datasetsWithPendingStatus);
        getExternalDataDAO().updateDataSetStatuses(codes, statusToRestoreOnFailure);
    }

    public void save()
    {
        assert dataChanged == true : "Data not changed";
        assert externalData != null : "Undefined external data.";
        try
        {
            checkMandatoryProperties();
            IExternalDataDAO externalDataDAO = getExternalDataDAO();
            externalDataDAO.updateDataSets(externalData);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("One of data sets"));
        }
        dataChanged = false;
        operationLog.info("State of external data saved.");
    }

    private void checkMandatoryProperties()
    {
        final Map<EntityTypePE, List<EntityTypePropertyTypePE>> cache =
                new HashMap<EntityTypePE, List<EntityTypePropertyTypePE>>();
        for (ExternalDataPE s : externalData)
        {
            entityPropertiesConverter.checkMandatoryProperties(s.getProperties(),
                    s.getDataSetType(), cache);
        }
    }

    public void update(List<NewDataSet> updates)
    {
        assert updates != null : "Unspecified updates.";
        setBatchUpdateMode(true);

        loadByDataSetCodes(Code.extractCodes(updates), true, true);

        final Map<String, ExternalDataPE> externalDataByCode =
                new HashMap<String, ExternalDataPE>();
        for (ExternalDataPE dataSet : externalData)
        {
            externalDataByCode.put(dataSet.getIdentifier(), dataSet);
        }
        for (NewDataSet dataSetUpdates : updates)
        {
            final ExternalDataPE dataSet = externalDataByCode.get(dataSetUpdates.getCode());
            prepareBatchUpdate(dataSet, dataSetUpdates);
        }
        setBatchUpdateMode(false);
        dataChanged = true;
        operationLog.info("External data updated");
    }

    private ExternalDataPE prepareBatchUpdate(ExternalDataPE dataSet, NewDataSet dataSetUpdates)
    {
        if (dataSet == null)
        {
            throw new UserFailureException(String.format("Data set with code '%s' does not exist.",
                    dataSetUpdates.getCode()));
        }
        updateBatchProperties(dataSet, Arrays.asList(dataSetUpdates.getProperties()),
                dataSetUpdates.getPropertiesToUpdate());
        return dataSet;
    }

    public LinkModel retrieveLinkFromDataSet(String key, String datastoreCode, String dataSetCode)
    {
        DataStorePE dataStore = findDataStore(datastoreCode);
        IDataStoreService service = tryGetDataStoreService(dataStore);
        if (service == null)
        {
            throw createUnknownDataStoreServerException();
        }
        List<DatasetDescription> locations =
                loadAvailableDatasetDescriptions(Collections.singletonList(dataSetCode));
        if (locations.size() < 1)
        {
            throw new UserFailureException(String.format("Data set with code '%s' does not exist.",
                    dataSetCode));
        }

        DatasetDescription dataSet = locations.get(0);
        String sessionToken = dataStore.getSessionToken();
        return service.retrieveLinkFromDataSet(sessionToken, key, dataSet);
    }
}
