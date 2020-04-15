/*
 * Copyright 2016 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import ch.ethz.sis.openbis.generic.asapi.v3.IApplicationServerApi;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.common.search.SearchResult;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.ContentCopy;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSet;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.LinkedData;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.ContentCopyCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.DataSetCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.create.LinkedDataCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.delete.DataSetDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.fetchoptions.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IContentCopyId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.IDataSetId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.DataSetUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.update.LinkedDataUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.delete.MaterialDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.create.PersonCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.fetchoptions.PersonFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.person.id.PersonPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.delete.ProjectDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.project.id.ProjectPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.delete.SampleDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.fetchoptions.SampleFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.ISampleId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SamplePermId;
import ch.ethz.sis.openbis.generic.dssapi.v3.IDataStoreServerApi;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.dataset.create.FullDataSetCreation;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.DataSetFile;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.fetchoptions.DataSetFileFetchOptions;
import ch.ethz.sis.openbis.generic.dssapi.v3.dto.datasetfile.search.DataSetFileSearchCriteria;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.SkinnyEntityRetriever;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.SyncEntityKind;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.EntityGraph;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.IEntityRetriever;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.common.entitygraph.INode;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.ParallelizedExecutionPreferences;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.config.SyncConfig;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.datasourceconnector.DataSourceConnector;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.parallelizedExecutor.AttachmentSynchronizationSummary;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.parallelizedExecutor.AttachmentsSynchronizer;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.parallelizedExecutor.DataSetRegistrationTaskExecutor;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.parallelizedExecutor.DataSetSynchronizationSummary;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.DefaultNameTranslator;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.INameTranslator;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.translator.PrefixBasedNameTranslator;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.DSPropertyUtils;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.Monitor;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.ServiceUtils;
import ch.ethz.sis.openbis.generic.server.dss.plugins.sync.harvester.synchronizer.util.V3Facade;
import ch.systemsx.cisd.common.concurrent.ParallelizedExecutor;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.Log4jSimpleLogger;
import ch.systemsx.cisd.etlserver.registrator.api.v1.impl.ConversionUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IConfigProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IDataSetDirectoryProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IShareIdManager;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.SegmentedStoreUtils;
import ch.systemsx.cisd.openbis.generic.server.batch.BatchOperationExecutor;
import ch.systemsx.cisd.openbis.generic.server.batch.IBatchOperation;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.GenericEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Identifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterial;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewMaterialWithType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewProject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewSpace;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PhysicalDataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationDetails;
import ch.systemsx.cisd.openbis.generic.shared.dto.AtomicEntityOperationResult;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetBatchUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialUpdateDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.AtomicEntityOperationDetailsBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import net.lemnik.eodsql.QueryTool;

/**
 * @author Ganime Betul Akin
 */
public class EntitySynchronizer
{
    private final String dataStoreCode;

    private final File storeRoot;

    private final IEncapsulatedOpenBISService service;

    private final IApplicationServerApi v3Api;

    private final IDataStoreServerApi v3DssApi;

    private final Date lastSyncTimestamp;

    // the following indicates the actual value in the file, not the cutoff calculated according to full sync prefs
    // it is later read in data set deletions.
    private final Date lastIncSyncTimestamp;

    private final Set<String> dataSetsCodesToRetry;

    private final Set<String> attachmentHolderCodesToRetry;

    private final SyncConfig config;

    private final Logger operationLog;

    private final Set<String> blackListedDataSetCodes;

    public EntitySynchronizer(SynchronizationContext synContext)
    {
        service = synContext.getService();
        v3Api = synContext.getV3Api();
        v3DssApi = synContext.getV3DssApi();
        dataStoreCode = synContext.getDataStoreCode();
        storeRoot = synContext.getStoreRoot();
        lastSyncTimestamp = synContext.getLastSyncTimestamp();
        lastIncSyncTimestamp = synContext.getLastIncSyncTimestamp();
        dataSetsCodesToRetry = synContext.getDataSetsCodesToRetry();
        attachmentHolderCodesToRetry = synContext.getAttachmentHolderCodesToRetry();
        blackListedDataSetCodes = synContext.getBlackListedDataSetCodes();
        config = synContext.getConfig();
        operationLog = synContext.getOperationLog();
    }

    public Date synchronizeEntities() throws Exception
    {
        Document doc = getResourceList();
        ResourceListParserData data = parseResourceList(doc);

        processDeletions(data);
        if (config.isMasterDataUpdate())
        {
            registerMasterData(data.getMasterData());
        }
        MultiKeyMap<String, String> newEntities = registerEntities(data);
        List<String> notSyncedAttachmentsHolders = registerAttachments(data, newEntities);
        populateFileServiceRepository(data);
        registerDataSets(data, notSyncedAttachmentsHolders);

        if (config.keepOriginalTimestampsAndUsers())
        {
            updateTimestampsAndUsers(data);
        }
        if (config.keepOriginalFrozenFlags())
        {
            updateFrozenFlags(data);
        }

        return data.getResourceListTimestamp();
    }

    private void updateTimestampsAndUsers(ResourceListParserData data)
    {
        Monitor monitor = new Monitor("Update timestamps and users", operationLog);
        createMissingUsers(data, monitor);
        DataSource dataSource = ServiceProvider.getDataSourceProvider().getDataSource("openbis-db");
        IHarvesterQuery query = QueryTool.getQuery(dataSource, IHarvesterQuery.class);
        Map<String, Long> userTechIdsByUserId = getUserTechIds(query);
        updateMaterials(data.getMaterialsToProcess().values(), query, userTechIdsByUserId, monitor);
        updateSpaces(data.getRelevantSpacesToProcess(), query, userTechIdsByUserId, monitor);
        updateProjects(data.getProjectsToProcess().values(), query, userTechIdsByUserId, monitor);
        updateExperiments(data.getExperimentsToProcess().values(), query, userTechIdsByUserId, monitor);
        updateSamples(data.getSamplesToProcess().values(), query, userTechIdsByUserId, monitor);
        updateDataSets(data.getDataSetsToProcess().values(), query, userTechIdsByUserId, monitor);
    }

    private Map<String, Long> getUserTechIds(IHarvesterQuery query)
    {
        Map<String, Long> userTechIdsByUserId = new HashMap<>();
        List<PersonRecord> allUsers = query.listAllUsers();
        for (PersonRecord personRecord : allUsers)
        {
            userTechIdsByUserId.put(personRecord.userId, personRecord.id);
        }
        return userTechIdsByUserId;
    }

    private void createMissingUsers(ResourceListParserData data, Monitor monitor)
    {
        Set<String> users = new HashSet<>();
        addUsers(users, data.getMaterialsToProcess().values());
        addUsers(users, data.getProjectsToProcess().values());
        addUsers(users, data.getExperimentsToProcess().values());
        addUsers(users, data.getSamplesToProcess().values());
        addUsers(users, data.getDataSetsToProcess().values());
        Set<String> knownPersons = v3Api.getPersons(service.getSessionToken(),
                users.stream().map(PersonPermId::new).collect(Collectors.toList()),
                new PersonFetchOptions()).keySet().stream().map(p -> p.toString()).collect(Collectors.toSet());
        List<PersonCreation> personCreations = new ArrayList<>();
        for (String user : users)
        {
            if (knownPersons.contains(user) == false)
            {
                PersonCreation personCreation = new PersonCreation();
                personCreation.setUserId(user);
                personCreations.add(personCreation);
            }
        }
        monitor.log(personCreations.size() + " from " + users.size() + " users are new.");
        if (personCreations.isEmpty() == false)
        {
            v3Api.createPersons(service.getSessionToken(), personCreations);
        }
    }

    private void updateMaterials(Collection<IncomingMaterial> materials, IHarvesterQuery query,
            Map<String, Long> userTechIdsByUserId, Monitor monitor)
    {
        monitor.log("update " + materials.size() + " materials");
        List<MaterialTypeRecord> listAllMaterialTypes = query.listAllMaterialTypes();
        Map<String, Long> materialTypeIdsByCode = new HashMap<>();
        for (MaterialTypeRecord materialTypeRecord : listAllMaterialTypes)
        {
            materialTypeIdsByCode.put(materialTypeRecord.code, materialTypeRecord.id);
        }
        List<RegistrationDTO> registrations = new ArrayList<>();
        for (IncomingMaterial incomingMaterial : materials)
        {
            NewMaterialWithType material = incomingMaterial.getMaterial();
            Long typeId = materialTypeIdsByCode.get(material.getType());
            addRegistration(registrations, material.getCode(), typeId, incomingMaterial, userTechIdsByUserId);
        }
        query.updateMaterialRegistrations(registrations);
    }

    private void updateSpaces(Collection<IncomingSpace> spaces, IHarvesterQuery query,
            Map<String, Long> userTechIdsByUserId, Monitor monitor)
    {
        monitor.log("update " + spaces.size() + " spaces");
        List<RegistrationDTO> registrations = new ArrayList<>();
        for (IncomingSpace incomingSpace : spaces)
        {
            addRegistration(registrations, incomingSpace.getPermID(), incomingSpace, userTechIdsByUserId);
        }
        query.updateSpaceRegistrations(registrations);
    }

    private void updateProjects(Collection<IncomingProject> projects, IHarvesterQuery query,
            Map<String, Long> userTechIdsByUserId, Monitor monitor)
    {
        monitor.log("update " + projects.size() + " projects");
        List<RegistrationDTO> registrations = new ArrayList<>();
        for (IncomingProject incomingProject : projects)
        {
            addRegistration(registrations, incomingProject.getPermID(), incomingProject, userTechIdsByUserId);
        }
        query.updateProjectRegistrations(registrations);
    }

    private void updateExperiments(Collection<IncomingExperiment> experiments, IHarvesterQuery query,
            Map<String, Long> userTechIdsByUserId, Monitor monitor)
    {
        monitor.log("update " + experiments.size() + " experiments");
        List<RegistrationDTO> registrations = new ArrayList<>();
        for (IncomingExperiment incomingExperiment : experiments)
        {
            addRegistration(registrations, incomingExperiment.getPermID(), incomingExperiment, userTechIdsByUserId);
        }
        query.updateExperimentRegistrations(registrations);
    }

    private void updateSamples(Collection<IncomingSample> samples, IHarvesterQuery query,
            Map<String, Long> userTechIdsByUserId, Monitor monitor)
    {
        monitor.log("update " + samples.size() + " samples");
        BatchOperationExecutor.executeInBatches(new IBatchOperation<IncomingSample>()
            {
                @Override
                public List<IncomingSample> getAllEntities()
                {
                    return new ArrayList<>(samples);
                }

                @Override
                public void execute(List<IncomingSample> samples)
                {
                    List<RegistrationDTO> registrations = new ArrayList<>();
                    for (IncomingSample incomingSamples : samples)
                    {
                        addRegistration(registrations, incomingSamples.getPermID(), incomingSamples, userTechIdsByUserId);
                    }
                    query.updateSampleRegistrations(registrations);
                }

                @Override
                public String getEntityName()
                {
                    return "sample";
                }

                @Override
                public String getOperationName()
                {
                    return "update registration";
                }
            });
    }

    private void updateDataSets(Collection<IncomingDataSet> dataSets, IHarvesterQuery query,
            Map<String, Long> userTechIdsByUserId, Monitor monitor)
    {
        monitor.log("update " + dataSets.size() + " data sets");
        BatchOperationExecutor.executeInBatches(new IBatchOperation<IncomingDataSet>()
            {
                @Override
                public List<IncomingDataSet> getAllEntities()
                {
                    return new ArrayList<>(dataSets);
                }

                @Override
                public void execute(List<IncomingDataSet> dataSets)
                {
                    List<RegistrationDTO> registrations = new ArrayList<>();
                    for (IncomingDataSet incomingDataSet : dataSets)
                    {
                        addRegistration(registrations, incomingDataSet.getFullDataSet().getMetadataCreation().getCode(),
                                incomingDataSet, userTechIdsByUserId);
                    }
                    query.updateDataSetRegistrations(registrations);
                }

                @Override
                public String getEntityName()
                {
                    return "data set";
                }

                @Override
                public String getOperationName()
                {
                    return "update registration";
                }
            });
    }

    private void addRegistration(List<RegistrationDTO> registrations, String permID, AbstractTimestampsAndUserHolder entity,
            Map<String, Long> userTechIdsByUserId)
    {
        addRegistration(registrations, permID, null, entity, userTechIdsByUserId);
    }

    private void addRegistration(List<RegistrationDTO> registrations, String permID, Long typeId, AbstractTimestampsAndUserHolder entity,
            Map<String, Long> userTechIdsByUserId)
    {
        Long registratorId = userTechIdsByUserId.get(entity.getRegistrator());
        if (registratorId != null)
        {
            RegistrationDTO registration = new RegistrationDTO();
            registration.setPermId(permID);
            registration.setTypeId(typeId);
            registration.setModificationTimestamp(entity.getLastModificationDate());
            registration.setModifierId(userTechIdsByUserId.get(entity.getModifier()));
            registration.setRegistrationTimestamp(entity.getRegistrationTimestamp());
            registration.setRegistratorId(registratorId);
            registrations.add(registration);
        }
    }

    private void addUsers(Set<String> users, Collection<? extends AbstractTimestampsAndUserHolder> timestampsAndUserHolders)
    {
        for (AbstractTimestampsAndUserHolder holder : timestampsAndUserHolders)
        {
            users.add(holder.getRegistrator());
            String modifier = holder.getModifier();
            if (modifier != null)
            {
                users.add(modifier);
            }
        }
    }

    private void updateFrozenFlags(ResourceListParserData data)
    {
        Monitor monitor = new Monitor("Update frozen flags", operationLog);
        DataSource dataSource = ServiceProvider.getDataSourceProvider().getDataSource("openbis-db");
        IHarvesterQuery query = QueryTool.getQuery(dataSource, IHarvesterQuery.class);
        updateSpaceFrozenFlags(data.getRelevantSpacesToProcess(), query, monitor);
        updateProjectFrozenFlags(data.getProjectsToProcess().values(), query, monitor);
        updateExperimentFrozenFlags(data.getExperimentsToProcess().values(), query, monitor);
        updateSampleFrozenFlags(data.getSamplesToProcess().values(), query, monitor);
        updateDataSetFrozenFlags(data.getDataSetsToProcess().values(), query, monitor);
    }

    private void updateSpaceFrozenFlags(Collection<IncomingSpace> spaces, IHarvesterQuery query, Monitor monitor)
    {
        monitor.log("Update frozen flags of " + spaces.size() + " spaces.");
        query.updateSpaceFrozenFlags(spaces.stream().map(IncomingSpace::getFrozenFlags).collect(Collectors.toList()));
    }

    private void updateProjectFrozenFlags(Collection<IncomingProject> projects, IHarvesterQuery query, Monitor monitor)
    {
        monitor.log("Update frozen flags of " + projects.size() + " projects.");
        query.updateProjectFrozenFlags(projects.stream().map(IncomingProject::getFrozenFlags).collect(Collectors.toList()));
    }

    private void updateExperimentFrozenFlags(Collection<IncomingExperiment> experiments, IHarvesterQuery query, Monitor monitor)
    {
        monitor.log("Update frozen flags of " + experiments.size() + " experiments.");
        query.updateExperimentFrozenFlags(experiments.stream().map(IncomingExperiment::getFrozenFlags).collect(Collectors.toList()));
    }

    private void updateSampleFrozenFlags(Collection<IncomingSample> samples, IHarvesterQuery query, Monitor monitor)
    {
        monitor.log("Update frozen flags of " + samples.size() + " samples.");
        BatchOperationExecutor.executeInBatches(new IBatchOperation<IncomingSample>()
            {
                @Override
                public List<IncomingSample> getAllEntities()
                {
                    return new ArrayList<>(samples);
                }

                @Override
                public void execute(List<IncomingSample> samples)
                {
                    query.updateSampleFrozenFlags(samples.stream().map(IncomingSample::getFrozenFlags).collect(Collectors.toList()));
                }

                @Override
                public String getEntityName()
                {
                    return "sample";
                }

                @Override
                public String getOperationName()
                {
                    return "update frozen flags";
                }
            });
    }

    private void updateDataSetFrozenFlags(Collection<IncomingDataSet> dataSets, IHarvesterQuery query, Monitor monitor)
    {
        monitor.log("Update frozen flags of " + dataSets.size() + " data sets.");
        BatchOperationExecutor.executeInBatches(new IBatchOperation<IncomingDataSet>()
            {
                @Override
                public List<IncomingDataSet> getAllEntities()
                {
                    return new ArrayList<>(dataSets);
                }

                @Override
                public void execute(List<IncomingDataSet> dataSets)
                {
                    query.updateDataSetFrozenFlags(dataSets.stream().map(IncomingDataSet::getFrozenFlags).collect(Collectors.toList()));
                }

                @Override
                public String getEntityName()
                {
                    return "data set";
                }

                @Override
                public String getOperationName()
                {
                    return "update frozen flags";
                }
            });
    }

    private void registerDataSets(ResourceListParserData data, List<String> notSyncedAttachmentsHolders) throws IOException
    {
        Monitor monitor = new Monitor("Register data sets", operationLog);
        operationLog.info("Registering data sets...");
        registerLinkDataSets(data, monitor);

        // register physical data sets without any hierarchy
        // Note that container/component and parent/child relationships are established post-reg.
        // setParentDataSetsOnTheChildren(data);
        Map<String, IncomingDataSet> physicalDSMap =
                data.filterByDataSetKindAndLastModificationDate(DataSetKind.PHYSICAL, lastSyncTimestamp, dataSetsCodesToRetry,
                        blackListedDataSetCodes);

        if (config.isVerbose())
        {
            verboseLogPhysicalDataSetRegistrations(physicalDSMap);
        }

        List<String> notRegisteredDataSetCodes = new ArrayList<>();
        if (config.isDryRun() == false)
        {
            DataSetSynchronizationSummary dsRegistrationSummary = registerPhysicalDataSets(physicalDSMap);

            // backup the current not synched data set codes file, delete the original file
            saveFailedEntitiesFile(dsRegistrationSummary.notRegisteredDataSetCodes, notSyncedAttachmentsHolders);

            notRegisteredDataSetCodes = dsRegistrationSummary.notRegisteredDataSetCodes;
            operationLog.info("Data set synchronization summary:\n"
                    + dsRegistrationSummary.addedDsCount + " data set(s) were added.\n"
                    + dsRegistrationSummary.updatedDsCount + " data set(s) were updated.\n"
                    + (notRegisteredDataSetCodes.isEmpty() ? ""
                            : notRegisteredDataSetCodes.size()
                                    + " data set(s) FAILED to register.\n")
                    + blackListedDataSetCodes.size() + " data set(s)"
                    + " were skipped because they were BLACK-LISTED.");
        }

        // link physical data sets registered above to container data sets
        // and set parent/child relationships
        operationLog.info("\n");
        operationLog.info("Start establishing data set hierarchies...");
        List<String> skippedDataSets = new ArrayList<String>();
        skippedDataSets.addAll(notRegisteredDataSetCodes);
        skippedDataSets.addAll(blackListedDataSetCodes);
        Set<String> containerDataSets = data
                .filterByDataSetKindAndLastModificationDate(DataSetKind.CONTAINER, lastSyncTimestamp, dataSetsCodesToRetry, blackListedDataSetCodes)
                .keySet();
        establishDataSetRelationships(data.getDataSetsToProcess(), skippedDataSets, containerDataSets);
        monitor.log();
    }

    private void registerLinkDataSets(ResourceListParserData data, Monitor monitor)
    {
        Map<String, IncomingDataSet> linkDataSets =
                data.filterByDataSetKindAndLastModificationDate(DataSetKind.LINK, lastSyncTimestamp, dataSetsCodesToRetry, blackListedDataSetCodes);
        monitor.log("Register " + linkDataSets.size() + " link data sets");
        Collection<IncomingDataSet> values = linkDataSets.values();
        List<DataSetPermId> dataSetIds =
                values.stream().map(ds -> new DataSetPermId(ds.getFullDataSet().getMetadataCreation().getCode())).collect(Collectors.toList());
        System.err.println("new data sets:" + dataSetIds);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withLinkedData();
        Map<IDataSetId, DataSet> existingDataSets = v3Api.getDataSets(service.getSessionToken(), dataSetIds, fetchOptions);
        System.err.println("existing data sets:" + existingDataSets.keySet());
        List<DataSetUpdate> updates = new ArrayList<>();
        List<FullDataSetCreation> creations = new ArrayList<>();
        for (IncomingDataSet incomingDataSet : values)
        {
            FullDataSetCreation fullDataSet = incomingDataSet.getFullDataSet();
            DataSetCreation dataSet = fullDataSet.getMetadataCreation();
            DataSetPermId permId = new DataSetPermId(dataSet.getCode());
            if (existingDataSets.containsKey(permId))
            {
                updates.add(createLinkDataUpdate(existingDataSets, dataSet, permId));
            } else
            {
                creations.add(fullDataSet);
            }
        }
        if (updates.isEmpty() == false && config.isDryRun() == false)
        {
            v3Api.updateDataSets(service.getSessionToken(), updates);
        }
        if (creations.isEmpty() == false && config.isDryRun() == false)
        {
            v3DssApi.createDataSets(service.getSessionToken(), creations);
        }
    }

    private DataSetUpdate createLinkDataUpdate(Map<IDataSetId, DataSet> existingDataSets, DataSetCreation dataSet,
            DataSetPermId permId)
    {
        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(permId);
        update.setProperties(dataSet.getProperties());
        LinkedDataCreation linkedDataCreation = dataSet.getLinkedData();
        if (linkedDataCreation != null)
        {
            LinkedData linkedData = existingDataSets.get(permId).getLinkedData();
            Map<String, ContentCopy> existingContentCopies = getExistingContentCopies(linkedData);
            LinkedDataUpdate linkedDataUpdate = new LinkedDataUpdate();
            for (ContentCopyCreation cc : linkedDataCreation.getContentCopies())
            {
                String key = cc.getPath() + "," + cc.getGitCommitHash() + "," + cc.getGitRepositoryId();
                if (existingContentCopies.remove(key) == null)
                {
                    linkedDataUpdate.getContentCopies().add(cc);
                }
            }
            linkedDataUpdate.getContentCopies().remove(existingContentCopies.values().stream()
                    .map(ContentCopy::getId).collect(Collectors.toList()).toArray(new IContentCopyId[0]));
            update.setLinkedData(linkedDataUpdate);
        }
        return update;
    }

    private Map<String, ContentCopy> getExistingContentCopies(LinkedData linkedData)
    {
        HashMap<String, ContentCopy> result = new HashMap<>();
        for (ContentCopy cc : linkedData.getContentCopies())
        {
            result.put(cc.getPath() + "," + cc.getGitCommitHash() + "," + cc.getGitRepositoryId(), cc);
        }
        return result;
    }

    private List<String> registerAttachments(ResourceListParserData data, MultiKeyMap<String, String> newEntities)
    {
        Monitor monitor = new Monitor("Register attachments", operationLog);
        operationLog.info("Processing attachments...");
        List<IncomingEntity<?>> attachmentHoldersToProcess =
                data.filterAttachmentHoldersByLastModificationDate(lastSyncTimestamp, attachmentHolderCodesToRetry);
        monitor.log(attachmentHoldersToProcess.size() + " to process");
        if (config.isVerbose())
        {
            verboseLogProcessAttachments(attachmentHoldersToProcess, newEntities);
        }

        List<String> notSyncedAttachmentsHolders = new ArrayList<String>();
        if (config.isDryRun() == false)
        {
            AttachmentSynchronizationSummary syncSummary = processAttachments(attachmentHoldersToProcess, monitor);
            notSyncedAttachmentsHolders = syncSummary.notRegisteredAttachmentHolderCodes;
            operationLog.info("Attachment synchronization summary:\n" + syncSummary.addedCount + " attachment(s) were added.\n"
                    + syncSummary.updatedCount + " attachment(s) were updated.\n"
                    + syncSummary.deletedCount + " attachment(s) were deleted.\n"
                    + (notSyncedAttachmentsHolders.isEmpty() ? ""
                            : "synchronization of attachments for "
                                    + notSyncedAttachmentsHolders.size() + " entitities FAILED."));
        }
        monitor.log();
        return notSyncedAttachmentsHolders;
    }

    private void populateFileServiceRepository(ResourceListParserData data)
    {
        Monitor monitor = new Monitor("Populate file service repository", operationLog);
        operationLog.info("Processing files...");
        File fileRepo = new File(config.getFileServiceReporitoryPath());
        int count = 0;
        long totalSize = 0;
        for (Entry<String, byte[]> entry : data.getFileToProcess().entrySet())
        {
            String path = entry.getKey();
            byte[] fileContent = entry.getValue();
            File file = new File(fileRepo, path);
            file.getParentFile().mkdirs();
            FileUtilities.writeToFile(file, fileContent);
            count++;
            totalSize += fileContent.length;
        }
        monitor.log(count + " files (total size: " + FileUtilities.byteCountToDisplaySize(totalSize) + ") have been saved.");
    }

    private MultiKeyMap<String, String> registerEntities(ResourceListParserData data)
    {
        Monitor monitor = new Monitor("Register entities", operationLog);
        AtomicEntityOperationDetails entityOperationDetails = createEntityOperationDetails(data, monitor);

        MultiKeyMap<String, String> newEntities = new MultiKeyMap<String, String>();
        if (config.isDryRun() == false)
        {
            AtomicEntityOperationResult operationResult = service.performEntityOperations(entityOperationDetails);
            newEntities = getNewEntities(entityOperationDetails);
            operationLog.info("Entity operation result: " + operationResult);
        }
        monitor.log();
        return newEntities;
    }

    private AtomicEntityOperationDetails createEntityOperationDetails(ResourceListParserData data, Monitor monitor)
    {
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        builder.user(config.getHarvesterUser());

        processSpaces(data, builder);
        processProjects(data, builder);
        processExperiments(data, builder);
        processSamples(data, builder, monitor);
        processMaterials(data, builder);

        AtomicEntityOperationDetails entityOperationDetails = builder.getDetails();
        if (config.isVerbose() == true)
        {
            verboseLogEntityOperations(entityOperationDetails);
        }
        return entityOperationDetails;
    }

    private void processSpaces(ResourceListParserData data, AtomicEntityOperationDetailsBuilder builder)
    {
        List<IncomingSpace> list = data.getRelevantSpacesToProcess();
        for (IncomingSpace incomingSpace : list)
        {
            String spaceCode = incomingSpace.getPermID();
            Space space = service.tryGetSpace(new SpaceIdentifier(spaceCode));
            if (space == null)
            {
                builder.space(incomingSpace.getEntity());
            }
        }
    }

    private ResourceListParserData parseResourceList(Document doc) throws XPathExpressionException
    {
        Monitor monitor = new Monitor("Parsing resource list", operationLog);
        // Parse the resource list: This sends back all projects,
        // experiments, samples and data sets contained in the XML together with their last modification date to be used for filtering
        operationLog.info("Parsing the resource list xml document...");
        INameTranslator nameTranslator = new DefaultNameTranslator();
        if (config.isTranslateUsingDataSourceAlias())
        {
            nameTranslator = new PrefixBasedNameTranslator(config.getDataSourceAlias());
        }

        ResourceListParser parser = ResourceListParser.create(nameTranslator, dataStoreCode);
        ResourceListParserData data = parser.parseResourceListDocument(doc, monitor);
        monitor.log();
        return data;
    }

    private Document getResourceList() throws Exception
    {
        Monitor monitor = new Monitor("Retrieving resoure list", operationLog);
        DataSourceConnector dataSourceConnector =
                new DataSourceConnector(config.getDataSourceURI(), config.getAuthenticationCredentials(), operationLog);
        operationLog.info("Retrieving the resource list...");
        Document doc = dataSourceConnector.getResourceListAsXMLDoc(config.getSpaceBlackList(), config.getSpaceWhiteList());
        monitor.log();
        return doc;
    }

    private MultiKeyMap<String, String> getNewEntities(AtomicEntityOperationDetails details)
    {
        MultiKeyMap<String, String> newEntities = new MultiKeyMap<String, String>();
        List<NewSample> sampleRegistrations = details.getSampleRegistrations();
        for (NewSample newSample : sampleRegistrations)
        {
            newEntities.put(SyncEntityKind.SAMPLE.toString(), newSample.getPermID(), newSample.getIdentifier());
        }
        List<NewExperiment> experimentRegistrations = details.getExperimentRegistrations();
        for (NewExperiment newExperiment : experimentRegistrations)
        {
            newEntities.put(SyncEntityKind.EXPERIMENT.toString(), newExperiment.getPermID(), newExperiment.getIdentifier());
        }
        List<NewProject> projectRegistrations = details.getProjectRegistrations();
        for (NewProject newProject : projectRegistrations)
        {
            newEntities.put(SyncEntityKind.PROJECT.toString(), newProject.getPermID(), newProject.getIdentifier());
        }
        return newEntities;
    }

    private void verboseLogPhysicalDataSetRegistrations(Map<String, IncomingDataSet> physicalDSMap)
    {
        if (physicalDSMap.isEmpty() == false)
        {
            operationLog.info("-------The following physical data sets will be processed-------");
            for (String dsCode : physicalDSMap.keySet())
            {
                operationLog.info(dsCode);
            }
        }
    }

    private void verboseLogProcessAttachments(List<IncomingEntity<?>> attachmentHoldersToProcess, MultiKeyMap<String, String> newEntities)
    {
        if (attachmentHoldersToProcess.isEmpty() == false)
        {
            operationLog.info("-------Attachments for the following entities will be processed-------");
            for (IncomingEntity<?> holder : attachmentHoldersToProcess)
            {
                // the following is done to not list holders in the log when they are just being created and have no attachments
                // updated ones will logged because the attachments might have been deleted.
                if (newEntities.containsKey(holder.getEntityKind().toString(), holder.getPermID()) == true)
                {
                    if (holder.hasAttachments() == false)
                    {
                        continue;
                    }
                }
                operationLog.info(holder.getIdentifier());
            }
        }
    }

    private void verboseLogDataSetUpdateOperation(List<DataSetBatchUpdatesDTO> dataSetUpdates)
    {
        if (dataSetUpdates.isEmpty() == false)
        {
            operationLog.info("-------The relationship hirearchies for the following pdata sets will be established-------");
            for (DataSetBatchUpdatesDTO dto : dataSetUpdates)
            {
                operationLog.info(dto.getDatasetCode());
            }
        }
    }

    private void verboseLogEntityOperations(AtomicEntityOperationDetails details)
    {
        List<NewSpace> spaceRegistrations = details.getSpaceRegistrations();
        if (spaceRegistrations.size() > 0)
        {
            operationLog.info("-------The following spaces will be created------- ");
            for (NewSpace newSpace : spaceRegistrations)
            {
                operationLog.info(newSpace.getCode());
            }
        }
        verboseLogEntityRegistrations(details.getProjectRegistrations());
        verboseLogEntityRegistrations(details.getExperimentRegistrations());
        verboseLogEntityRegistrations(details.getSampleRegistrations());
        verboseLogMaterialRegistration(details.getMaterialRegistrations());
        verboseLogSampleUpdates(details.getSampleUpdates());
        verboseLogExperimentUpdates(details.getExperimentUpdates());
        verboseLogProjectUpdates(details.getProjectUpdates());
        verboseLogMaterialUpdates(details.getMaterialUpdates());
    }

    private void verboseLogMaterialUpdates(List<MaterialUpdateDTO> materialUpdates)
    {
        if (materialUpdates.isEmpty() == false)
        {
            operationLog.info("-------" + materialUpdates.size() + " materials will be updated-------");
        }
    }

    private void verboseLogMaterialRegistration(Map<String, List<NewMaterial>> materials)
    {
        if (materials.isEmpty() == false)
        {
            operationLog.info("-------The following materials will be registered-------");
            for (String type : materials.keySet())
            {
                operationLog.info("-------Materials of type " + type + " -------");
                List<NewMaterial> list = materials.get(type);
                for (NewMaterial newMaterial : list)
                {
                    operationLog.info(newMaterial.getCode());
                }
            }
        }
    }

    private void verboseLogSampleUpdates(List<SampleUpdatesDTO> sampleUpdates)
    {
        if (sampleUpdates.isEmpty() == false)
        {
            operationLog.info("-------The following samples will be updated-------");
            for (SampleUpdatesDTO dto : sampleUpdates)
            {
                operationLog.info(dto.getSampleIdentifier());
            }
        }
    }

    private void verboseLogExperimentUpdates(List<ExperimentUpdatesDTO> experimentUpdates)
    {
        if (experimentUpdates.isEmpty() == false)
        {
            operationLog.info("-------The following experiments will be updated-------");
            for (ExperimentUpdatesDTO dto : experimentUpdates)
            {
                operationLog.info(dto.getProjectIdentifier());
            }
        }
    }

    private void verboseLogProjectUpdates(List<ProjectUpdatesDTO> projectUpdates)
    {
        if (projectUpdates.isEmpty() == false)
        {
            operationLog.info("-------The following projects will be updated-------");
            for (ProjectUpdatesDTO dto : projectUpdates)
            {
                operationLog.info(dto.getIdentifier());
            }
        }
    }

    private void verboseLogEntityRegistrations(List<? extends Identifier<?>> entityRegistrations)
    {
        if (entityRegistrations.isEmpty() == false)
        {
            Identifier<?> identifier = entityRegistrations.get(0);
            SyncEntityKind entityKind = null;
            if (identifier instanceof NewSample)
            {
                entityKind = SyncEntityKind.SAMPLE;
            } else if (identifier instanceof NewExperiment)
            {
                entityKind = SyncEntityKind.EXPERIMENT;
            } else if (identifier instanceof NewExperiment)
            {
                entityKind = SyncEntityKind.PROJECT;
            }
            operationLog.info("-------The following " + entityKind + "(s) will be created-------");
            for (Identifier<?> entity : entityRegistrations)
            {
                operationLog.info(entity.getIdentifier());
            }
        }
    }

    private AttachmentSynchronizationSummary processAttachments(List<IncomingEntity<?>> attachmentHoldersToProcess,
            Monitor monitor)
    {
        AttachmentSynchronizationSummary synchronizationSummary = new AttachmentSynchronizationSummary();

        ParallelizedExecutionPreferences preferences = config.getParallelizedExecutionPrefs();
        monitor.log("Services for accessing data source established");
        List<List<IncomingEntity<?>>> attachmentHoldersChunks = chunk(attachmentHoldersToProcess);
        IApplicationServerApi v3apiDataSource = ServiceUtils.createAsV3Api(config.getDataSourceOpenbisURL());
        String sessionTokenDataSource = v3apiDataSource.login(config.getUser(), config.getPassword());
        ParallelizedExecutor.process(attachmentHoldersChunks,
                new AttachmentsSynchronizer(v3Api, service.getSessionToken(), v3apiDataSource, sessionTokenDataSource,
                        lastSyncTimestamp, synchronizationSummary, monitor),
                preferences.getMachineLoad(), preferences.getMaxThreads(), "process attachments", preferences.getRetriesOnFail(),
                preferences.isStopOnFailure());

        return synchronizationSummary;
    }

    private List<List<IncomingEntity<?>>> chunk(List<IncomingEntity<?>> entities)
    {
        List<List<IncomingEntity<?>>> chunks = new ArrayList<>();
        List<IncomingEntity<?>> chunk = null;
        for (IncomingEntity<?> incomingEntity : entities)
        {
            if (chunk == null || chunk.size() >= 1000)
            {
                chunk = new ArrayList<>();
                chunks.add(chunk);
            }
            chunk.add(incomingEntity);
        }
        return chunks;
    }

    // private void cleanup()
    // {
    // operationLog.info("Cleaning up unused master data");
    // try
    // {
    // masterDataSyncronizer.cleanupUnusedMasterData();
    // } catch (Exception e)
    // {
    // operationLog.warn(e.getMessage());
    // }
    // }

    private void establishDataSetRelationships(Map<String, IncomingDataSet> dataSetsToProcess,
            List<String> skippedDataSets, Set<String> containerDataSets)
    {
        // set parent and container data set codes before everything else
        // container and physical data sets can both be parents/children of each other
        AtomicEntityOperationDetailsBuilder builder = new AtomicEntityOperationDetailsBuilder();
        Map<String, NewExternalData> datasetsToUpdate = new HashMap<String, NewExternalData>();
        Map<String, Set<String>> dsToParents = new HashMap<String, Set<String>>();
        Map<String, Set<String>> dsToContained = new HashMap<String, Set<String>>();
        for (IncomingDataSet dsWithConn : dataSetsToProcess.values())
        {
            NewExternalData dataSet = dsWithConn.getDataSet();
            for (Connection conn : dsWithConn.getConnections())
            {
                IncomingDataSet dataSet2 = dataSetsToProcess.get(conn.getToPermId());
                if (dataSet2 != null)
                {
                    if (conn.getType().equals("Child"))
                    {
                        if (skippedDataSets.contains(dataSet.getCode()) == false)
                        {
                            NewExternalData childDataSet = dataSet2.getDataSet();
                            List<String> parentDataSetCodes = childDataSet.getParentDataSetCodes();
                            parentDataSetCodes.add(dataSet.getCode());
                            dsToParents.put(childDataSet.getCode(), new HashSet<String>(parentDataSetCodes));
                        }
                    } else if (conn.getType().equals("Component"))
                    {
                        NewExternalData componentDataSet = dataSet2.getDataSet();
                        if (skippedDataSets.contains(componentDataSet.getCode()) == false)
                        {
                            NewContainerDataSet containerDataSet = (NewContainerDataSet) dataSet;
                            List<String> containedDataSetCodes = containerDataSet.getContainedDataSetCodes();
                            containedDataSetCodes.add(componentDataSet.getCode());
                            dsToContained.put(dataSet.getCode(), new HashSet<String>(containedDataSetCodes));
                        }
                    }
                }
            }
        }
        // go through all the data sets, decide what needs to be updated
        for (IncomingDataSet dsWithConn : dataSetsToProcess.values())
        {
            NewExternalData dataSet = dsWithConn.getDataSet();

            if (dsWithConn.getLastModificationDate().after(lastSyncTimestamp)
                    || dataSetsCodesToRetry.contains(dataSet.getCode()) == true
                    || isParentModified(dsToParents, dataSet))
            {
                if (blackListedDataSetCodes.contains(dataSet.getCode()) == false)
                {
                    if (containerDataSets.contains(dataSet.getCode()) && service.tryGetDataSet(dataSet.getCode()) == null)
                    {
                        builder.dataSet(dataSet);
                    } else
                    {
                        datasetsToUpdate.put(dataSet.getCode(), dataSet);
                    }
                }
            }
        }

        // go thru to-be-updated DS list and establish/break relations
        for (NewExternalData dataSet : datasetsToUpdate.values())
        {
            // if the DS could not have been registered for some reason,
            // skip this.
            AbstractExternalData dsInHarvester = service.tryGetDataSet(dataSet.getCode());
            if (dsInHarvester == null)
            {
                continue;
            }
            DataSetBatchUpdatesDTO dsBatchUpdatesDTO = createDataSetBatchUpdateDTO(dataSet, dsInHarvester);

            // mark the properties to be updated otherwise properties that were reset (set to empty values) will not be carried over
            Set<String> resetPropertyList =
                    getResetPropertyList(DSPropertyUtils.convertToEntityProperty(dataSet.getDataSetProperties()), dsInHarvester.getProperties());
            Set<String> propertiesToUpdate = dsBatchUpdatesDTO.getDetails().getPropertiesToUpdate();
            propertiesToUpdate.addAll(resetPropertyList);
            dsBatchUpdatesDTO.getDetails().setPropertiesToUpdate(propertiesToUpdate);
            if (dataSet instanceof NewContainerDataSet)
            {
                NewContainerDataSet containerDS = (NewContainerDataSet) dataSet;
                if (dsToContained.containsKey(containerDS.getCode()))
                {
                    dsBatchUpdatesDTO.setModifiedContainedDatasetCodesOrNull(
                            dsToContained.get(dataSet.getCode()).toArray(new String[containerDS.getContainedDataSetCodes().size()]));
                } else
                {
                    dsBatchUpdatesDTO.setModifiedContainedDatasetCodesOrNull(new String[0]);
                }
                dsBatchUpdatesDTO.getDetails().setContainerUpdateRequested(true);
            }
            if (dsToParents.containsKey(dataSet.getCode()))
            {
                dsBatchUpdatesDTO.setModifiedParentDatasetCodesOrNull(dsToParents.get(dataSet.getCode()).toArray(
                        new String[dataSet.getParentDataSetCodes().size()]));
                // TODO should this always be true or should we flag the ones that require parent update. Same for container
            } else
            {
                dsBatchUpdatesDTO.setModifiedParentDatasetCodesOrNull(new String[0]);
            }
            dsBatchUpdatesDTO.getDetails().setParentsUpdateRequested(true);
            SampleIdentifier sampleIdentifier = dataSet.getSampleIdentifierOrNull();
            if (sampleIdentifier != null)
            {
                dsBatchUpdatesDTO.setSampleIdentifierOrNull(sampleIdentifier);
            } else
            {
                dsBatchUpdatesDTO.setSampleIdentifierOrNull(null);
            }
            dsBatchUpdatesDTO.getDetails().setSampleUpdateRequested(true);

            ExperimentIdentifier expIdentifier = dataSet.getExperimentIdentifierOrNull();
            if (expIdentifier != null)
            {
                dsBatchUpdatesDTO.setExperimentIdentifierOrNull(expIdentifier);
            } else
            {
                dsBatchUpdatesDTO.setExperimentIdentifierOrNull(null);
            }
            dsBatchUpdatesDTO.getDetails().setExperimentUpdateRequested(true);
            builder.dataSetUpdate(dsBatchUpdatesDTO);
        }

        if (config.isVerbose())
        {
            verboseLogDataSetUpdateOperation(builder.getDetails().getDataSetUpdates());
        }
        if (config.isDryRun())
        {
            return;
        }
        AtomicEntityOperationResult operationResult = service.performEntityOperations(builder.getDetails());
        operationLog.info("entity operation result: " + operationResult);
    }

    private boolean isParentModified(Map<String, Set<String>> dsToParents, NewExternalData dataSet)
    {
        Set<String> parents = dsToParents.get(dataSet.getCode());
        if (parents == null)
        {
            return false;
        }
        for (String parentDSCode : parents)
        {
            if (dataSetsCodesToRetry.contains(parentDSCode))
            {
                return true;
            }
        }
        return false;
    }

    private DataSetSynchronizationSummary registerPhysicalDataSets(Map<String, IncomingDataSet> physicalDSMap) throws IOException
    {
        List<DataSetCreation> dsList = new ArrayList<IncomingDataSet>(physicalDSMap.values()).stream()
                .map(IncomingDataSet::getFullDataSet).map(FullDataSetCreation::getMetadataCreation)
                .collect(Collectors.toList());
        DataSetSynchronizationSummary dataSetSynchronizationSummary = new DataSetSynchronizationSummary();

        // This parallelization is possible because each DS is registered without dependencies
        // and the dependencies are established later on in the sync process.
        ParallelizedExecutionPreferences preferences = config.getParallelizedExecutionPrefs();
        String sessionToken = service.getSessionToken();
        DataSetProcessingContext context = new DataSetProcessingContext(null, null, null, null, null, null, sessionToken);

        ParallelizedExecutor.process(dsList, new DataSetRegistrationTaskExecutor(dataSetSynchronizationSummary, operationLog, storeRoot, context,
                config),
                preferences.getMachineLoad(), preferences.getMaxThreads(), "register data sets", preferences.getRetriesOnFail(),
                preferences.isStopOnFailure());

        return dataSetSynchronizationSummary;
    }

    private void saveFailedEntitiesFile(List<String> notRegisteredDataSetCodes, List<String> notSyncedAttachmentsHolders) throws IOException
    {
        File notSyncedEntitiesFile = new File(config.getNotSyncedEntitiesFileName());
        if (notSyncedEntitiesFile.exists())
        {
            backupAndResetNotSyncedDataSetsFile(notSyncedEntitiesFile);
        }

        // first write the data set codes to be retried next time we sync
        for (String dsCode : notRegisteredDataSetCodes)
        {
            FileUtilities.appendToFile(notSyncedEntitiesFile, SyncEntityKind.DATA_SET + "-" + dsCode, true);
        }
        // append the ids of holder entities for the failed attachment synchronizations
        for (String holderCode : notSyncedAttachmentsHolders)
        {
            FileUtilities.appendToFile(notSyncedEntitiesFile, holderCode, true);
        }
        // append the blacklisted codes to the end of the file
        for (String dsCode : blackListedDataSetCodes)
        {
            FileUtilities.appendToFile(notSyncedEntitiesFile, ("#" + SyncEntityKind.DATA_SET + "-" + dsCode), true);
        }
    }

    private void backupAndResetNotSyncedDataSetsFile(File notSyncedDataSetsFile) throws IOException
    {
        File backupLastSyncTimeStampFile = new File(config.getNotSyncedEntitiesFileName() + ".bk");
        FileUtils.copyFile(notSyncedDataSetsFile, backupLastSyncTimeStampFile);
        FileUtils.writeStringToFile(notSyncedDataSetsFile, "", Charset.defaultCharset());
    }

    private void registerMasterData(MasterData masterData)
    {
        Monitor monitor = new Monitor("Register master data", operationLog);
        operationLog.info("Registering master data...");
        MasterDataSynchronizer masterDataSyncronizer = new MasterDataSynchronizer(config, operationLog);
        masterDataSyncronizer.synchronizeMasterData(masterData, monitor);
        monitor.log();
    }

    private void processDeletions(ResourceListParserData data) throws Exception
    {
        Monitor monitor = new Monitor("Delete entities", operationLog);
        String sessionToken = service.getSessionToken();
        IEntityRetriever entityRetriever =
                SkinnyEntityRetriever.createWithSessionToken(v3Api, sessionToken);

        Set<String> incomingProjectPermIds = data.getProjectsToProcess().keySet();
        Set<String> incomingExperimentPermIds = data.getExperimentsToProcess().keySet();
        Set<String> incomingSamplePermIds = data.getSamplesToProcess().keySet();
        Set<String> incomingDataSetCodes = data.getDataSetsToProcess().keySet();
        MultiKeyMap<String, IncomingMaterial> incomingMaterials = data.getMaterialsToProcess();

        // find projects, experiments, samples and data sets to be deleted
        Map<ProjectPermId, String> projectsToDelete = new HashMap<ProjectPermId, String>();
        Map<ExperimentPermId, String> experimentsToDelete = new HashMap<ExperimentPermId, String>();
        Map<SamplePermId, String> samplesToDelete = new HashMap<SamplePermId, String>();
        // for data sets and materials permId and identifier(code) are the same but still we keep a map
        Map<DataSetPermId, String> dataSetsToDelete = new HashMap<DataSetPermId, String>();
        Map<MaterialPermId, String> materialsToDelete = new HashMap<MaterialPermId, String>();

        Set<PhysicalDataSet> physicalDataSetsDelete = new HashSet<PhysicalDataSet>();
        // first find out the entities to be deleted
        for (String harvesterSpaceId : data.getHarvesterSpaceList())
        {
            EntityGraph<INode> harvesterEntityGraph = entityRetriever.getEntityGraph(harvesterSpaceId);
            List<INode> entities = harvesterEntityGraph.getNodes();
            for (INode entity : entities)
            {
                String permId = entity.getPermId();
                String identifier = entity.getIdentifier().getEntityIdentifier();
                String typeCodeOrNull = entity.getTypeCodeOrNull();
                if (entity.getEntityKind().equals(SyncEntityKind.PROJECT))
                {
                    if (incomingProjectPermIds.contains(permId) == false)
                    {
                        ProjectPermId projectPermId = new ProjectPermId(permId);
                        projectsToDelete.put(projectPermId, identifier);
                    }
                } else if (entity.getEntityKind().equals(SyncEntityKind.EXPERIMENT))
                {
                    ExperimentPermId experimentPermId = new ExperimentPermId(permId);
                    if (incomingExperimentPermIds.contains(permId) == false)
                    {
                        experimentsToDelete.put(experimentPermId, identifier);
                    } else
                    {
                        NewExperiment exp = data.getExperimentsToProcess().get(permId).getExperiment();
                        if (typeCodeOrNull.equals(exp.getExperimentTypeCode()) == false)
                        {
                            experimentsToDelete.put(experimentPermId, identifier);
                        }
                    }
                } else if (entity.getEntityKind().equals(SyncEntityKind.SAMPLE))
                {
                    SamplePermId samplePermId = new SamplePermId(permId);
                    if (incomingSamplePermIds.contains(permId) == false)
                    {
                        samplesToDelete.put(samplePermId, identifier);
                    } else
                    {
                        NewSample smp = data.getSamplesToProcess().get(permId).getSample();
                        if (typeCodeOrNull.equals(smp.getSampleType().getCode()) == false)
                        {
                            samplesToDelete.put(samplePermId, identifier);
                        }
                    }
                } else if (entity.getEntityKind().equals(SyncEntityKind.DATA_SET))
                {
                    DataSetPermId dataSetPermId = new DataSetPermId(permId);
                    if (incomingDataSetCodes.contains(permId) == false)
                    {
                        dataSetsToDelete.put(dataSetPermId, dataSetPermId.getPermId());
                    } else
                    {
                        boolean sameDS = true;
                        IncomingDataSet dsWithConns = data.getDataSetsToProcess().get(permId);
                        NewExternalData ds = dsWithConns.getDataSet();
                        if (typeCodeOrNull.equals(ds.getDataSetType().getCode()) == false)
                        {
                            sameDS = false;
                        } else
                        {
                            if (dsWithConns.getKind() == DataSetKind.PHYSICAL && dsWithConns.getLastModificationDate().after(lastIncSyncTimestamp))
                            {
                                PhysicalDataSet physicalDS = service.tryGetDataSet(permId).tryGetAsDataSet();
                                sameDS = deepCompareDataSets(permId);
                                if (sameDS == false)
                                {
                                    physicalDataSetsDelete.add(physicalDS);
                                }
                            }
                        }
                        if (sameDS == false)
                        {
                            dataSetsToDelete.put(dataSetPermId, dataSetPermId.getPermId());
                        }
                    }
                }
            }
        }

        List<ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material> materials = entityRetriever.fetchMaterials();

        for (ch.ethz.sis.openbis.generic.asapi.v3.dto.material.Material material : materials)
        {
            if (incomingMaterials.containsKey(material.getCode(), material.getType().getCode()) == false)
            {
                MaterialPermId materialPermId = new MaterialPermId(material.getCode(), material.getType().getCode());
                materialsToDelete.put(materialPermId, materialPermId.getCode());
            }
        }

        operationLog.info("-------Processing deletions-------");
        if (config.isVerbose() == true)
        {
            if (dataSetsToDelete.isEmpty() == false
                    || samplesToDelete.isEmpty() == false
                    || experimentsToDelete.isEmpty() == false
                    || projectsToDelete.isEmpty() == false
                    || materialsToDelete.isEmpty() == false)
            {
                operationLog.info("!!!!!!!!!!!!!The following will be PERMAMENTLY removed from openbis!!!!!!!!!!!!!");
            }
            verboseLogDeletions(dataSetsToDelete.values(), "data sets");
            verboseLogDeletions(samplesToDelete.values(), "samples");
            verboseLogDeletions(experimentsToDelete.values(), "experiments");
            verboseLogDeletions(projectsToDelete.values(), "projects");
            verboseLogDeletions(materialsToDelete.values(), "materials");
        }

        if (config.isDryRun() == true)
        {
            monitor.log();
            return;
        }

        // delete data sets
        DataSetDeletionOptions dsDeletionOpts = new DataSetDeletionOptions();
        String reasonDetail = " from data source : " + config.getDataSourceAlias();
        dsDeletionOpts.setReason("sync data set deletions" + reasonDetail);

        IDeletionId dsDeletionId =
                v3Api.deleteDataSets(sessionToken, new ArrayList<DataSetPermId>(dataSetsToDelete.keySet()), dsDeletionOpts);

        // delete samples
        SampleDeletionOptions sampleDeletionOptions = new SampleDeletionOptions();
        sampleDeletionOptions.setReason("sync sample deletions" + reasonDetail);
        IDeletionId smpDeletionId = v3Api.deleteSamples(sessionToken, new ArrayList<SamplePermId>(samplesToDelete.keySet()), sampleDeletionOptions);

        // delete experiments
        ExperimentDeletionOptions expDeletionOpts = new ExperimentDeletionOptions();
        expDeletionOpts.setReason("sync experiment deletions" + reasonDetail);
        IDeletionId expDeletionId =
                v3Api.deleteExperiments(sessionToken, new ArrayList<ExperimentPermId>(experimentsToDelete.keySet()), expDeletionOpts);

        // confirm deletions: Deletions need be confirm in the right order because of dependencies(foreign key constraints)
        v3Api.confirmDeletions(sessionToken, Collections.singletonList(dsDeletionId));
        v3Api.confirmDeletions(sessionToken, Collections.singletonList(smpDeletionId));
        v3Api.confirmDeletions(sessionToken, Collections.singletonList(expDeletionId));

        // delete projects
        ProjectDeletionOptions prjDeletionOpts = new ProjectDeletionOptions();
        prjDeletionOpts.setReason("Sync projects" + reasonDetail);
        v3Api.deleteProjects(sessionToken, new ArrayList<ProjectPermId>(projectsToDelete.keySet()), prjDeletionOpts);

        // delete materials
        MaterialDeletionOptions matDeletionOptions = new MaterialDeletionOptions();
        matDeletionOptions.setReason("sync materials" + reasonDetail);

        try
        {
            v3Api.deleteMaterials(sessionToken, new ArrayList<MaterialPermId>(materialsToDelete.keySet()), matDeletionOptions);
        } catch (Exception e)
        {
            operationLog.warn("One or more materials could not be deleted due to: " + e.getMessage());
        }

        // The following summary is not accurate if an error occurs in material deletions
        StringBuffer summary = new StringBuffer();
        if (projectsToDelete.size() > 0)
        {
            summary.append(projectsToDelete.size() + " projects,");
        }
        if (materialsToDelete.size() > 0)
        {
            summary.append(materialsToDelete.size() + " materials,");
        }
        if (expDeletionId != null)
        {
            summary.append(experimentsToDelete.size() + " experiments,");
        }
        if (smpDeletionId != null)
        {
            summary.append(samplesToDelete.size() + " samples,");
        }
        if (dsDeletionId != null)
        {
            summary.append(dataSetsToDelete.size() + " data sets");
        }
        if (summary.length() > 0)
        {
            operationLog.info(summary.substring(0, summary.length() - 1) + " have been deleted:");
        } else
        {
            operationLog.info("Nothing has been deleted:");
        }
        for (PhysicalDataSet physicalDS : physicalDataSetsDelete)
        {
            operationLog.info("Is going to delete the location: " + physicalDS.getLocation());
            File datasetDir =
                    getDirectoryProvider().getDataSetDirectory(physicalDS);
            SegmentedStoreUtils.deleteDataSetInstantly(physicalDS.getCode(), datasetDir, new Log4jSimpleLogger(operationLog));
        }
        monitor.log();
    }

    private void verboseLogDeletions(Collection<String> identifiers, String entityKind)
    {
        if (identifiers.isEmpty())
        {
            return;
        }
        operationLog.info(identifiers.size() + " " + entityKind + " with the following identifiers:");
        for (String identifier : identifiers)
        {
            operationLog.info(identifier);
        }
    }

    private void processExperiments(ResourceListParserData data,
            AtomicEntityOperationDetailsBuilder builder)
    {
        // process experiments
        Map<String, IncomingExperiment> experimentsToProcess = data.getExperimentsToProcess();
        for (IncomingExperiment exp : experimentsToProcess.values())
        {
            NewExperiment incomingExp = exp.getExperiment();
            if (exp.getLastModificationDate().after(lastSyncTimestamp))
            {
                Experiment experiment = null;
                try
                {
                    experiment = service.tryGetExperimentByPermId(incomingExp.getPermID());
                } catch (Exception e)
                {
                    // doing nothing because when the experiment with the perm id not found
                    // an exception will be thrown. Seems to be the same with entity kinds
                }

                if (experiment == null)
                {
                    // ADD EXPERIMENT
                    builder.experiment(incomingExp);
                } else
                {
                    // UPDATE EXPERIMENT
                    ExperimentUpdatesDTO expUpdate = createExperimentUpdateDTOs(incomingExp, experiment);
                    builder.experimentUpdate(expUpdate);
                }
            }
            // handleExperimentConnections(data, exp, incomingExp);
        }
    }

    private ExperimentUpdatesDTO createExperimentUpdateDTOs(NewExperiment incomingExp, Experiment experiment)
    {
        ExperimentUpdatesDTO expUpdate = new ExperimentUpdatesDTO();
        expUpdate.setProjectIdentifier(ExperimentIdentifierFactory.parse(incomingExp.getIdentifier()));
        expUpdate.setVersion(experiment.getVersion());

        List<IEntityProperty> newPropList =
                prepareUpdatedPropertyList(incomingExp.getProperties(), experiment.getProperties());

        expUpdate.setProperties(newPropList);
        expUpdate.setExperimentId(TechId.create(experiment));
        // attachments are synched later separately but we need to have a non-null value for attachment list.
        expUpdate.setAttachments(Collections.<NewAttachment> emptyList());
        return expUpdate;
    }

    private void processMaterials(ResourceListParserData data, AtomicEntityOperationDetailsBuilder builder)
    {
        // process materials
        MultiKeyMap<String, IncomingMaterial> materialsToProcess = data.getMaterialsToProcess();
        for (IncomingMaterial newMaterialWithType : materialsToProcess.values())
        {
            NewMaterialWithType incomingMaterial = newMaterialWithType.getMaterial();
            if (newMaterialWithType.getLastModificationDate().after(lastSyncTimestamp))
            {
                Material material = service.tryGetMaterial(new MaterialIdentifier(incomingMaterial.getCode(), incomingMaterial.getType()));
                if (material == null)
                {
                    builder.material(incomingMaterial);
                } else
                {
                    List<IEntityProperty> newPropList =
                            prepareUpdatedPropertyList(incomingMaterial.getProperties(), material.getProperties());
                    MaterialUpdateDTO update =
                            new MaterialUpdateDTO(TechId.create(material), newPropList,
                                    material.getModificationDate());
                    builder.materialUpdate(update);
                }
            }
        }
    }

    private void processProjects(ResourceListParserData data, AtomicEntityOperationDetailsBuilder builder)
    {
        Map<String, IncomingProject> projectsToProcess = data.getProjectsToProcess();
        for (IncomingProject prj : projectsToProcess.values())
        {
            NewProject incomingProject = prj.getProject();
            if (prj.getLastModificationDate().after(lastSyncTimestamp))
            {
                Project project = null;
                try
                {
                    project = service.tryGetProjectByPermId(incomingProject.getPermID());
                } catch (Exception e)
                {
                    // TODO doing nothing because when the project with the perm is not found
                    // an exception will be thrown. See bug report SSDM-4108
                }

                if (project == null)
                {
                    // ADD PROJECT
                    builder.project(incomingProject);
                } else
                {
                    // UPDATE PROJECT
                    builder.projectUpdate(createProjectUpdateDTO(incomingProject, project));
                }
            }
            // handleProjectConnections(data, prj);
        }
    }

    private ProjectUpdatesDTO createProjectUpdateDTO(NewProject incomingProject, Project project)
    {
        ProjectUpdatesDTO prjUpdate = new ProjectUpdatesDTO();
        prjUpdate.setVersion(project.getVersion());
        prjUpdate.setTechId(TechId.create(project));
        prjUpdate.setDescription(incomingProject.getDescription());
        // attachments are synched later separately but we need to have a non-null value for attachment list.
        prjUpdate.setAttachments(Collections.<NewAttachment> emptyList());
        ProjectIdentifier projectIdentifier = ProjectIdentifierFactory.parse(incomingProject.getIdentifier());
        prjUpdate.setIdentifier(projectIdentifier.asProjectIdentifierString());
        prjUpdate.setSpaceCode(projectIdentifier.getSpaceCode());
        return prjUpdate;
    }

    private void processSamples(ResourceListParserData data, AtomicEntityOperationDetailsBuilder builder, Monitor monitor)
    {
        // process samples
        Map<String, IncomingSample> samplesToProcess = data.getSamplesToProcess();
        Map<String, ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample> knownSamples = getKnownSamples(samplesToProcess.keySet());
        Map<SampleIdentifier, NewSample> samplesToUpdate = new HashMap<SampleIdentifier, NewSample>();
        Set<String> sampleWithUpdatedParents = new HashSet<String>();
        int count = 0;
        int n = samplesToProcess.size();
        for (IncomingSample sample : samplesToProcess.values())
        {
            if (++count % 10000 == 0)
            {
                monitor.log(String.format("%7d/%d sample: %s", count, n, sample.getIdentifier()));
            }
            NewSample incomingSample = sample.getSample();
            if (sample.getLastModificationDate().after(lastSyncTimestamp))
            {
                SampleIdentifier sampleIdentifier = SampleIdentifierFactory.parse(incomingSample);
                ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample knownSample = null;
                knownSample = knownSamples.get(incomingSample.getPermID());
                if (knownSample == null)
                {
                    // ADD SAMPLE
                    builder.sample(incomingSample);
                } else
                {
                    // defer creation of sample update objects until all samples have been gone through;
                    samplesToUpdate.put(sampleIdentifier, incomingSample);
                    ;
                    for (ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample child : knownSample.getChildren())
                    {
                        String childSampleIdentifier = child.getIdentifier().getIdentifier();// edgeNodePair.getNode().getIdentifier();
                        IncomingSample childSampleWithConns = findChildInSamplesToProcess(childSampleIdentifier, samplesToProcess);
                        if (childSampleWithConns == null)
                        {
                            // TODO Handle sample delete
                        } else
                        {
                            // the childSample will appear in the incoming samples list anyway
                            // but we want to make sure its parent modification is handled
                            NewSample childSample = childSampleWithConns.getSample();
                            sampleWithUpdatedParents.add(childSample.getIdentifier());
                        }
                    }
                }
            }
            for (Connection conn : sample.getConnections())
            {
                if (conn.getType().equals("Component"))
                {
                    NewSample containedSample = getRelated(samplesToProcess, conn).getSample();
                    containedSample.setContainerIdentifier(incomingSample.getIdentifier());
                } else if (conn.getType().equals("Child"))
                {
                    NewSample childSample = getRelated(samplesToProcess, conn).getSample();
                    String[] parents = childSample.getParentsOrNull();
                    List<String> parentIds = null;
                    if (parents == null)
                    {
                        parentIds = new ArrayList<String>();
                    } else
                    {
                        parentIds = new ArrayList<String>(Arrays.asList(parents));
                    }
                    parentIds.add(incomingSample.getIdentifier());
                    childSample.setParentsOrNull(parentIds.toArray(new String[parentIds.size()]));
                }
                // TODO how about Connection Type
                // else if (conn.getType().equals("Connection")) // TODO not sure if this guarantees that we have a dataset in the toPermId
                // {
                // NewExternalData externalData = dataSetsToCreate.get(conn.getToPermId()).getDataSet();
                // externalData.setSampleIdentifierOrNull(new SampleIdentifier(newSmp.getIdentifier()));
                // }
            }
        }

        // create sample update dtos for the samples that need to be updated
        for (SampleIdentifier sampleIdentifier : samplesToUpdate.keySet())
        {
            NewSample incomingSmp = samplesToUpdate.get(sampleIdentifier);
            Sample sample = service.tryGetSampleByPermId(incomingSmp.getPermID());

            TechId sampleId = TechId.create(sample);
            ExperimentIdentifier experimentIdentifier = getExperimentIdentifier(incomingSmp);
            ProjectIdentifier projectIdentifier = getProjectIdentifier(incomingSmp);
            String[] modifiedParentIds = incomingSmp.getParentsOrNull();
            if (modifiedParentIds == null)
            {
                if (sampleWithUpdatedParents.contains(incomingSmp.getIdentifier()))
                {
                    modifiedParentIds = new String[0];
                }
            }
            String containerIdentifier = getContainerIdentifier(incomingSmp);

            List<IEntityProperty> newPropList = prepareUpdatedPropertyList(incomingSmp.getProperties(), sample.getProperties());

            // attachments are synched later separately but we need to have a non-null value for attachment list.
            SampleUpdatesDTO updates =
                    new SampleUpdatesDTO(sampleId, newPropList, experimentIdentifier,
                            projectIdentifier, Collections.<NewAttachment> emptyList(),
                            sample.getVersion(), sampleIdentifier, containerIdentifier,
                            modifiedParentIds);
            builder.sampleUpdate(updates);
        }
    }

    private IncomingSample getRelated(Map<String, IncomingSample> samplesToProcess, Connection conn)
    {
        String permId = conn.getToPermId();
        IncomingSample sample = samplesToProcess.get(permId);
        if (sample != null)
        {
            return sample;
        }
        throw new IllegalArgumentException("sample " + permId + " hasn't been provided by the data source.");
    }

    private Map<String, ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample> getKnownSamples(Collection<String> samplePermIds)
    {
        String sessionToken = service.getSessionToken();
        List<SamplePermId> sampleIds = samplePermIds.stream().map(SamplePermId::new).collect(Collectors.toList());
        SampleFetchOptions fetchOptions = new SampleFetchOptions();
        fetchOptions.withChildren();
        Map<ISampleId, ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample> samples = v3Api.getSamples(sessionToken, sampleIds, fetchOptions);
        HashMap<String, ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample> result = new HashMap<>();
        for (ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.Sample sample : samples.values())
        {
            result.put(sample.getPermId().getPermId(), sample);
        }
        return result;
    }

    /**
     * Pads out the incoming property lists with remaining properties set to "" This way any properties that were re-set (value removed) in the data
     * source will be carried over to the harvester
     */
    private List<IEntityProperty> prepareUpdatedPropertyList(IEntityProperty[] iEntityProperties, List<IEntityProperty> existingProperties)
    {
        ArrayList<IEntityProperty> incomingProperties = new ArrayList<IEntityProperty>(Arrays.asList(iEntityProperties));
        Set<String> existingPropertyNames = extractPropertyNames(existingProperties);
        Set<String> newPropertyNames = extractPropertyNames(incomingProperties);
        existingPropertyNames.removeAll(newPropertyNames);

        for (String propName : existingPropertyNames)
        {
            GenericEntityProperty property = new GenericEntityProperty();
            PropertyType propertyType = new PropertyType();
            propertyType.setCode(propName);
            property.setPropertyType(propertyType);
            property.setValue("");
            incomingProperties.add(property);
        }
        return incomingProperties;
    }

    private Set<String> getResetPropertyList(IEntityProperty[] iEntityProperties, List<IEntityProperty> existingProperties)
    {
        ArrayList<IEntityProperty> incomingProperties = new ArrayList<IEntityProperty>(Arrays.asList(iEntityProperties));
        Set<String> existingPropertyNames = extractPropertyNames(existingProperties);
        Set<String> newPropertyNames = extractPropertyNames(incomingProperties);
        existingPropertyNames.removeAll(newPropertyNames);
        return existingPropertyNames;
    }

    private Set<String> extractPropertyNames(List<IEntityProperty> existingProperties)
    {
        Set<String> existingPropertyNames = new HashSet<String>();
        for (IEntityProperty prop : existingProperties)
        {
            existingPropertyNames.add(prop.getPropertyType().getCode());
        }
        return existingPropertyNames;
    }

    private String getContainerIdentifier(NewSample newSmp)
    {
        String containerIdentifier = newSmp.getContainerIdentifier();
        return containerIdentifier == null ? null : containerIdentifier;
    }

    private ExperimentIdentifier getExperimentIdentifier(NewSample newSmp)
    {
        String expIdentifier = newSmp.getExperimentIdentifier();
        if (expIdentifier == null)
        {
            return null;
        }
        return ExperimentIdentifierFactory.parse(expIdentifier);
    }

    private ProjectIdentifier getProjectIdentifier(NewSample sample)
    {
        String projectIdentifier = sample.getProjectIdentifier();
        if (projectIdentifier == null)
        {
            return null;
        }
        return ProjectIdentifierFactory.parse(projectIdentifier);
    }

    private IncomingSample findChildInSamplesToProcess(String childSampleIdentifier, Map<String, IncomingSample> samplesToProcess)
    {
        for (IncomingSample sample : samplesToProcess.values())
        {
            if (sample.getSample().getIdentifier().equals(childSampleIdentifier))
            {
                return sample;
            }
        }
        return null;
    }

    private boolean deepCompareDataSets(String dataSetCode)
            throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        // get the file nodes in the incoming DS by querying the data source openbis
        V3Facade v3FacadeToDataSource = new V3Facade(config);

        DataSetFileSearchCriteria criteria = new DataSetFileSearchCriteria();
        criteria.withDataSet().withCode().thatEquals(dataSetCode);
        SearchResult<DataSetFile> result = v3FacadeToDataSource.searchFiles(criteria, new DataSetFileFetchOptions());

        // get the file nodes in the harvester openbis
        IDataStoreServerApi dssharvester = (IDataStoreServerApi) ServiceProvider.getDssServiceV3().getService();
        SearchResult<DataSetFile> resultHarvester =
                dssharvester.searchFiles(ServiceProvider.getOpenBISService().getSessionToken(), criteria, new DataSetFileFetchOptions());
        if (result.getTotalCount() != resultHarvester.getTotalCount())
        {
            return false;
        }
        List<DataSetFile> dsNodes = result.getObjects();
        List<DataSetFile> harvesterNodes = resultHarvester.getObjects();
        sortFileNodes(dsNodes);
        sortFileNodes(harvesterNodes);
        return calculateHash(dsNodes).equals(calculateHash(harvesterNodes));
    }

    private void sortFileNodes(List<DataSetFile> nodes)
    {
        Collections.sort(nodes, new Comparator<DataSetFile>()
            {

                @Override
                public int compare(DataSetFile dsFile1, DataSetFile dsFile2)
                {
                    return dsFile1.getPath().compareTo(dsFile2.getPath());
                }
            });
    }

    private String calculateHash(List<DataSetFile> nodes) throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        StringBuffer sb = new StringBuffer();
        for (DataSetFile dataSetFile : nodes)
        {
            sb.append(dataSetFile.getPath());
            sb.append(dataSetFile.getChecksumCRC32());
            sb.append(dataSetFile.getFileLength());
        }
        byte[] digest = MessageDigest.getInstance("MD5").digest(new String(sb).getBytes("UTF-8"));
        return new String(Hex.encodeHex(digest));
    }

    private DataSetBatchUpdatesDTO createDataSetBatchUpdateDTO(NewExternalData dataSet, AbstractExternalData dsInHarvester)
    {
        ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetUpdatable updateUpdatable =
                new ch.systemsx.cisd.etlserver.registrator.api.v1.impl.DataSetUpdatable(dsInHarvester, service);
        DataSetBatchUpdatesDTO dsBatchUpdatesDTO = ConversionUtils.convertToDataSetBatchUpdatesDTO(updateUpdatable);
        dsBatchUpdatesDTO.setDatasetId(TechId.create(dsInHarvester));

        List<IEntityProperty> updatedProperties =
                prepareUpdatedPropertyList(DSPropertyUtils.convertToEntityProperty(dataSet.getDataSetProperties()), dsInHarvester.getProperties());

        dsBatchUpdatesDTO.setProperties(updatedProperties);
        return dsBatchUpdatesDTO;
    }

    private IDataSetDirectoryProvider getDirectoryProvider()
    {
        return new DataSetDirectoryProvider(getConfigProvider().getStoreRoot(), getShareIdManager());
    }

    private IConfigProvider getConfigProvider()
    {
        return ServiceProvider.getConfigProvider();
    }

    private IShareIdManager getShareIdManager()
    {
        return ServiceProvider.getShareIdManager();
    }

}
