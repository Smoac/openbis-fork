/*
 * Copyright 2009 ETH Zuerich, CISD
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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.api.IManagedProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DataPE.class)
public class DataBO extends AbstractDataSetBusinessObject implements IDataBO
{
    private DataPE data;

    public DataBO(IDAOFactory daoFactory, Session session)
    {
        super(daoFactory, session);
    }

    public DataBO(IDAOFactory daoFactory, Session exampleSession,
            IEntityPropertiesConverter propertiesConverter)
    {
        super(daoFactory, exampleSession, propertiesConverter);
    }

    public DataPE tryGetData()
    {
        return data;
    }

    public DataPE getData()
    {
        // TODO 2010-04-12, CR: This should throw an exception if the external data is null
        // -- will that cause problems with clients?
        return data;
    }

    public void loadByCode(String dataSetCode)
    {
        loadByCode(dataSetCode, true, false);
    }

    private void loadByCode(String dataSetCode, boolean withPropertyTypes, boolean lockForUpdate)
    {
        data =
                getDataDAO().tryToFindFullDataSetByCode(dataSetCode, withPropertyTypes,
                        lockForUpdate);
    }

    static final String DATA_SET_TYPE = "dataSetType";

    static final String PROPERTY_TYPES = "dataSetType.dataSetTypePropertyTypesInternal";

    public void loadDataByTechId(TechId datasetId)
    {
        String[] connections =
            { PROPERTY_TYPES, DATA_SET_TYPE };
        data = getDataDAO().tryGetByTechId(datasetId, connections);
        if (data == null)
        {
            throw new UserFailureException(String.format("Data set with ID '%s' does not exist.",
                    datasetId));
        }
    }

    public void enrichWithParentsAndExperiment()
    {
        if (data != null)
        {
            enrichWithParentsAndExperiment(data);
        }
    }

    public void enrichWithChildren()
    {
        if (data != null)
        {
            enrichWithChildren(data);
        }
    }

    public final void enrichWithProperties()
    {
        if (data != null)
        {
            HibernateUtils.initialize(data.getProperties());
        }
    }

    public void enrichWithContainedDataSets()
    {
        if (data != null && data.isContainer())
        {
            HibernateUtils.initialize(data.getContainedDataSets());
        }
    }

    public void define(NewExternalData newData, SamplePE sample, SourceType sourceType)
    {
        assert sample != null : "Undefined sample.";

        final DataStorePE dataStore = define(newData, sourceType);
        final ExperimentPE experiment = sample.getExperiment();

        data.setSample(sample);
        data.setExperiment(experiment);

        setParentDataSets(dataStore, experiment, newData);
    }

    public void define(NewExternalData newData, ExperimentPE experiment, SourceType sourceType)
    {
        assert experiment != null : "Undefined experiment.";

        final DataStorePE dataStore = define(newData, sourceType);

        data.setExperiment(experiment);
        setParentDataSets(dataStore, experiment, newData);
    }

    private void setParentDataSets(DataStorePE dataStore, ExperimentPE experiment,
            NewExternalData newData)
    {
        final List<String> parentDataSetCodes = newData.getParentDataSetCodes();
        if (parentDataSetCodes != null)
        {
            for (String parentCode : parentDataSetCodes)
            {
                final DataPE parent = getOrCreateParentData(parentCode, dataStore, experiment);
                data.addParent(parent);
            }
        }
    }

    private DataStorePE define(NewExternalData newData, SourceType sourceType)
    {
        assert newData != null : "Undefined data.";
        final DataSetType dataSetType = newData.getDataSetType();
        assert dataSetType != null : "Undefined data set type.";
        final FileFormatType fileFormatType = newData.getFileFormatType();
        assert fileFormatType != null : "Undefined file format type.";
        final String location = newData.getLocation();
        assert location != null : "Undefined location.";
        final LocatorType locatorType = newData.getLocatorType();
        assert locatorType != null : "Undefined location type.";
        assert sourceType != null : "Undefined source type.";

        final ExternalDataPE externalData = new ExternalDataPE();

        externalData.setDataProducerCode(newData.getDataProducerCode());
        externalData.setProductionDate(newData.getProductionDate());
        externalData.setCode(newData.getCode());
        externalData.setDataSetType(getDataSetType(dataSetType));
        externalData.setFileFormatType(getFileFomatType(fileFormatType));
        externalData.setComplete(newData.getComplete());
        externalData.setShareId(newData.getShareId());
        externalData.setLocation(location);
        externalData.setSize(newData.getSize());
        externalData.setSpeedHint(newData.getSpeedHint());
        externalData.setStorageFormatVocabularyTerm(tryToFindStorageFormatTerm(newData
                .getStorageFormat()));
        externalData.setLocatorType(getLocatorTypeDAO().tryToFindLocatorTypeByCode(
                locatorType.getCode()));
        externalData.setRegistrator(tryToGetRegistrator(newData));
        DataStorePE dataStore =
                getDataStoreDAO().tryToFindDataStoreByCode(newData.getDataStoreCode());
        externalData.setDataStore(dataStore);
        defineDataSetProperties(externalData,
                convertToDataSetProperties(newData.getDataSetProperties()));
        externalData.setDerived(sourceType == SourceType.DERIVED);

        data = externalData;

        return dataStore;
    }

    private PersonPE tryToGetRegistrator(NewExternalData newData)
    {
        String userId = newData.getUserId();
        if (userId != null)
        {
            return getPersonDAO().tryFindPersonByUserId(userId);
        }
        String userEMail = newData.getUserEMail();
        if (userEMail == null)
        {
            return null;
        }
        List<PersonPE> persons = getPersonDAO().listPersons();
        for (PersonPE person : persons)
        {
            if (userEMail.equalsIgnoreCase(person.getEmail()))
            {
                return person;
            }
        }
        return null;
    }

    private VocabularyTermPE tryToFindStorageFormatTerm(StorageFormat storageFormat)
    {
        IVocabularyDAO vocabularyDAO = getVocabularyDAO();
        VocabularyPE vocabulary =
                vocabularyDAO.tryFindVocabularyByCode(StorageFormat.VOCABULARY_CODE);
        Set<VocabularyTermPE> terms = vocabulary.getTerms();
        for (VocabularyTermPE term : terms)
        {
            if (storageFormat.getCode().equals(term.getCode()))
            {
                return term;
            }
        }
        return null;
    }

    private final DataSetTypePE getDataSetType(final DataSetType dataSetType)
    {
        final String dataSetTypeCode = dataSetType.getCode();
        final DataSetTypePE dataSetTypeOrNull =
                getDataSetTypeDAO().tryToFindDataSetTypeByCode(dataSetTypeCode);
        if (dataSetTypeOrNull == null)
        {
            throw UserFailureException.fromTemplate("There is no data set type with code '%s'",
                    dataSetTypeCode);
        }
        return dataSetTypeOrNull;
    }

    private final FileFormatTypePE getFileFomatType(final FileFormatType fileFormatType)
    {
        final String fileFormatTypeCode = fileFormatType.getCode();
        final FileFormatTypePE fileFormatTypeOrNull =
                getFileFormatTypeDAO().tryToFindFileFormatTypeByCode(fileFormatTypeCode);
        if (fileFormatTypeOrNull == null)
        {
            throw UserFailureException.fromTemplate("There is no file format type with code '%s'",
                    fileFormatTypeCode);
        }
        return fileFormatTypeOrNull;
    }

    private final DataPE getOrCreateParentData(final String parentDataSetCode,
            DataStorePE dataStore, ExperimentPE experiment)
    {
        assert parentDataSetCode != null : "Unspecified parent data set code.";

        final IDataDAO dataDAO = getDataDAO();
        DataPE parent = dataDAO.tryToFindDataSetByCode(parentDataSetCode);
        if (parent == null)
        {
            parent = new DataPE();
            parent.setDataStore(dataStore);
            parent.setCode(parentDataSetCode);
            String code = DataSetTypeCode.UNKNOWN.getCode();
            parent.setDataSetType(getDataSetTypeDAO().tryToFindDataSetTypeByCode(code));
            parent.setExperiment(experiment);
            parent.setPlaceholder(true);
            dataDAO.createDataSet(parent);
        }
        return parent;
    }

    public void save() throws UserFailureException
    {
        assert data != null : "Undefined external data.";
        IDataDAO dataDAO = getDataDAO();
        String dataCode = data.getCode();
        DataPE placeholder = dataDAO.tryToFindDataSetByCode(dataCode);
        if (placeholder == null)
        {
            dataDAO.createDataSet(data);
        } else
        {
            if (placeholder.isPlaceholder() == false)
            {
                throw new UserFailureException("Already existing data set for code '" + dataCode
                        + "' can not be updated by data set " + placeholder);
            }
            // NOTE: If new data set is created and there was no placeholder
            // cycles will not be created because only connections to parents are added
            // and we assume that there were no cycles before. On the other hand placeholders
            // have at least one child so cycles need to be checked when they are updated.
            validateRelationshipGraph(data.getParents());

            data.setPlaceholder(false);
            data.setId(HibernateUtils.getId(placeholder));
            data.setRegistrationDate(new Date());
            data.setModificationDate(placeholder.getModificationDate());

            dataDAO.updateDataSet(data);
        }
        entityPropertiesConverter.checkMandatoryProperties(data.getProperties(),
                data.getDataSetType());
    }

    public void addPropertiesToDataSet(String dataSetCode, List<NewProperty> properties)
    {
        loadByCode(dataSetCode);
        updatePropertiesPreservingExisting(properties);
        entityPropertiesConverter.checkMandatoryProperties(data.getProperties(),
                data.getDataSetType());
        validateAndSave();
    }

    private void updatePropertiesPreservingExisting(List<NewProperty> properties)
    {
        final Set<DataSetPropertyPE> existingProperties = data.getProperties();
        Set<String> propertyUpdatesCodes =
                extractPropertyCodesToUpdate(properties, existingProperties);
        List<NewProperty> propertyUpdates =
                extractNewPropertiesToUpdate(properties, propertyUpdatesCodes);
        final DataSetTypePE type = data.getDataSetType();
        final PersonPE registrator = findRegistrator();
        data.setProperties(entityPropertiesConverter.updateProperties(existingProperties, type,
                Arrays.asList(convertToDataSetProperties(propertyUpdates)), registrator,
                propertyUpdatesCodes));
    }

    private List<NewProperty> extractNewPropertiesToUpdate(List<NewProperty> properties,
            Set<String> propertiesToUpdate)
    {
        List<NewProperty> newPropertiesToUpdate = new ArrayList<NewProperty>();
        for (NewProperty np : properties)
        {
            if (propertiesToUpdate.contains(np.getPropertyCode()))
            {
                newPropertiesToUpdate.add(np);
            }
        }
        return newPropertiesToUpdate;
    }

    private Set<String> extractPropertyCodesToUpdate(List<NewProperty> properties,
            final Set<DataSetPropertyPE> existingProperties)
    {
        Set<String> propertiesToUpdate = new HashSet<String>();
        for (NewProperty np : properties)
        {
            propertiesToUpdate.add(np.getPropertyCode());
        }
        for (DataSetPropertyPE ep : existingProperties)
        {
            propertiesToUpdate.remove(ep.getEntityTypePropertyType().getPropertyType().getCode());
        }
        return propertiesToUpdate;
    }

    public void update(DataSetUpdatesDTO updates)
    {
        loadDataByTechId(updates.getDatasetId());
        if (updates.getVersion().equals(data.getModificationDate()) == false)
        {
            System.err.println("modification date error: " + updates.getVersion() + " != "
                    + data.getModificationDate());
            // throwModifiedEntityException("Data set"); FIXME
        }
        final SampleIdentifier sampleIdentifierOrNull = updates.getSampleIdentifierOrNull();
        if (sampleIdentifierOrNull != null)
        {
            // update sample and indirectly experiment
            updateSample(updates.getSampleIdentifierOrNull());
        } else
        {
            updateExperiment(updates.getExperimentIdentifierOrNull());
            // remove connection with sample
            data.setSample(null);
        }
        updateParents(updates.getModifiedParentDatasetCodesOrNull());
        updateComponents(updates.getModifiedContainedDatasetCodesOrNull());
        updateFileFormatType(updates.getFileFormatTypeCode());
        updateProperties(data, updates.getProperties());
        entityPropertiesConverter.checkMandatoryProperties(data.getProperties(),
                data.getDataSetType());
        validateAndSave();
    }

    private void validateAndSave()
    {
        getDataDAO().validateAndSaveUpdatedEntity(data);
    }

    private void updateParents(String[] modifiedParentDatasetCodesOrNull)
    {
        if (modifiedParentDatasetCodesOrNull == null)
        {
            return; // parents were not changed
        } else
        {
            final Set<DataPE> currentParents = data.getParents();
            final Set<String> currentParentCodes = extractCodes(currentParents);
            final Set<String> newCodes = asSet(modifiedParentDatasetCodesOrNull);
            newCodes.removeAll(currentParentCodes);

            // quick check for direct cycle
            if (newCodes.contains(data.getCode()))
            {
                throw new UserFailureException("Data set '" + data.getCode()
                        + "' can not be its own parent.");
            }

            final List<DataPE> parentsToAdd = findDataSetsByCodes(newCodes);
            validateRelationshipGraph(parentsToAdd);
            addParents(parentsToAdd);

            final Set<String> removedCodes = currentParentCodes;
            removedCodes.removeAll(asSet(modifiedParentDatasetCodesOrNull));
            removeParents(filterDataSets(currentParents, removedCodes));
        }
    }

    private void updateComponents(String[] modifiedContainedDatasetCodesOrNull)
    {
        if (modifiedContainedDatasetCodesOrNull == null)
        {
            return; // contained data sets were not changed
        } else
        {
            final List<DataPE> currentComponents =
                    new ArrayList<DataPE>(data.getContainedDataSets());
            removeComponents(currentComponents);

            final Set<String> currentCodes = extractCodes(currentComponents);
            final Set<String> newCodes = asSet(modifiedContainedDatasetCodesOrNull);

            // quick check for direct cycle
            final Set<String> brandNewCodes = new HashSet<String>(newCodes);
            brandNewCodes.removeAll(currentCodes);
            if (brandNewCodes.contains(data.getCode()))
            {
                throw new UserFailureException("Data set '" + data.getCode()
                        + "' can not be its own component.");
            }
            // TODO 2011-05-16, Piotr Buczek: validation of container relationship graph
            // validateContainerRelationshipGraph(componentsToAdd);

            final List<DataPE> newComponents = findDataSetsByCodes(newCodes);
            addComponents(newComponents);
        }
    }

    /**
     * Throws {@link UserFailureException} if adding specified parents to this data set will create
     * a cycle in data set relationships.
     */
    private void validateRelationshipGraph(Collection<DataPE> parentsToAdd)
    {
        // DFS from new parents that are to be added to this business object going in direction
        // of parent relationship until:
        // - all related ancestors are visited == graph has no cycles
        // - we get to this business object == cycle is found
        // NOTE: The assumption is that there were no cycles in the graph of relationship before.
        // This algorithm will not find cycles that don't include this business object,
        // although such cycles shouldn't cause it to loop forever.

        // Algorithm operates only on data set ids to make it perform better
        // - there is no need to join DB tables.
        // To be able to inform user about the exact data set that cannot be connected as a parent
        // we need start seeking cycles starting from each parent to be added separately. Otherwise
        // we would need to get invoke more queries to DB (not going layer by layer of graph depth
        // per query) or use BFS instead (which would also be slower in a general case).
        for (DataPE parentToAdd : parentsToAdd)
        {
            validateRelationshipGraph(parentToAdd);
        }
    }

    private void validateRelationshipGraph(DataPE parentToAdd)
    {
        final TechId updatedDataSetId = TechId.create(data);
        final Set<TechId> visited = new HashSet<TechId>();
        Set<TechId> toVisit = new HashSet<TechId>();
        toVisit.add(TechId.create(parentToAdd));
        while (toVisit.isEmpty() == false)
        {
            if (toVisit.contains(updatedDataSetId))
            {
                throw UserFailureException.fromTemplate(
                        "Data Set '%s' is an ancestor of Data Set '%s' "
                                + "and cannot be at the same time set as its child.",
                        data.getCode(), parentToAdd.getCode());
            } else
            {
                final Set<TechId> nextToVisit = findParentIds(toVisit);
                visited.addAll(toVisit);
                nextToVisit.removeAll(visited);
                toVisit = nextToVisit;
            }
        }
    }

    private Set<TechId> findParentIds(Set<TechId> dataSetIds)
    {
        return getDataDAO().findParentIds(dataSetIds);
    }

    private List<DataPE> filterDataSets(Collection<DataPE> dataSets, Collection<String> seekenCodes)
    {
        List<DataPE> result = new ArrayList<DataPE>();
        for (DataPE dataSet : dataSets)
        {
            if (seekenCodes.contains(dataSet.getCode()))
            {
                result.add(dataSet);
            }
        }
        return result;
    }

    private void addParents(Collection<DataPE> parentsToAdd)
    {
        for (DataPE parent : parentsToAdd)
        {
            data.addParent(parent);
        }
    }

    private void removeParents(Collection<DataPE> parentsToRemove)
    {
        for (DataPE parent : parentsToRemove)
        {
            data.removeParent(parent);
        }
    }

    private void addComponents(Collection<DataPE> componentsToAdd)
    {
        for (DataPE component : componentsToAdd)
        {
            data.addComponent(component);
        }
    }

    private void removeComponents(Collection<DataPE> componentsToRemove)
    {
        for (DataPE component : componentsToRemove)
        {
            data.removeComponent(component);
        }
    }

    private List<DataPE> findDataSetsByCodes(Set<String> codes)
    {
        final IDataDAO dao = getDataDAO();
        final List<DataPE> dataSets = new ArrayList<DataPE>();
        final List<String> missingDataSetCodes = new ArrayList<String>();
        for (String code : codes)
        {
            DataPE dataSetOrNull = dao.tryToFindDataSetByCode(code);
            if (dataSetOrNull == null)
            {
                missingDataSetCodes.add(code);
            } else
            {
                dataSets.add(dataSetOrNull);
            }
        }
        if (missingDataSetCodes.size() > 0)
        {
            throw UserFailureException.fromTemplate(
                    "Data Sets with following codes do not exist: '%s'.",
                    CollectionUtils.abbreviate(missingDataSetCodes, 10));
        } else
        {
            return dataSets;
        }
    }

    private static Set<String> asSet(String[] objects)
    {
        return new LinkedHashSet<String>(Arrays.asList(objects)); // keep the ordering
    }

    private static Set<String> extractCodes(Collection<DataPE> parents)
    {
        Set<String> codes = new HashSet<String>(parents.size());
        for (DataPE parent : parents)
        {
            codes.add(parent.getCode());
        }
        return codes;
    }

    //

    private void updateSample(SampleIdentifier sampleIdentifierOrNull)
    {
        assert sampleIdentifierOrNull != null;
        SamplePE newSample = getSampleByIdentifier(sampleIdentifierOrNull);
        SamplePE previousSampleOrNull = data.tryGetSample();
        if (newSample.equals(previousSampleOrNull))
        {
            return; // nothing to change
        }
        if (newSample.getSpace() == null)
        {
            throw createWrongSampleException(newSample, "the new sample is shared");
        }
        ExperimentPE experiment = newSample.getExperiment();
        if (experiment == null)
        {
            throw createWrongSampleException(newSample,
                    "the new sample is not connected to any experiment");
        }
        // move dataset to the experiment if needed
        if (experiment.equals(data.getExperiment()) == false)
        {
            data.setExperiment(experiment);
        }
        data.setSample(newSample);
    }

    private void updateExperiment(ExperimentIdentifier experimentIdentifierOrNull)
    {
        assert experimentIdentifierOrNull != null;
        ExperimentPE experiment = getExperimentByIdentifier(experimentIdentifierOrNull);
        data.setExperiment(experiment);
    }

    private ExperimentPE getExperimentByIdentifier(final ExperimentIdentifier identifier)
    {
        assert identifier != null : "Experiment identifier unspecified.";
        final ProjectPE project =
                getProjectDAO().tryFindProject(identifier.getDatabaseInstanceCode(),
                        identifier.getSpaceCode(), identifier.getProjectCode());
        if (project == null)
        {
            throw new UserFailureException("Unkown experiment because of unkown project: "
                    + identifier);
        }
        final ExperimentPE exp =
                getExperimentDAO().tryFindByCodeAndProject(project, identifier.getExperimentCode());
        return exp;
    }

    private void updateFileFormatType(String fileFormatTypeCode)
    {
        if (data.isExternalData())
        {
            ExternalDataPE externalData = data.tryAsExternalData();
            FileFormatTypePE fileFormatTypeOrNull =
                    getFileFormatTypeDAO().tryToFindFileFormatTypeByCode(fileFormatTypeCode);
            if (fileFormatTypeOrNull == null)
            {
                throw new UserFailureException(String.format("File type '%s' does not exist.",
                        fileFormatTypeCode));
            } else
            {
                externalData.setFileFormatType(fileFormatTypeOrNull);
            }
        }
    }

    private UserFailureException createWrongSampleException(SamplePE sample, String reason)
    {
        return UserFailureException.fromTemplate(
                "The dataset '%s' cannot be connected to the sample '%s'" + " because %s.",
                data.getCode(), sample.getIdentifier(), reason);
    }

    private final void defineDataSetProperties(final DataPE dataSet,
            final IEntityProperty[] newProperties)
    {
        final String dataSetTypeCode = dataSet.getDataSetType().getCode();
        final List<DataSetPropertyPE> properties =
                entityPropertiesConverter.convertProperties(newProperties, dataSetTypeCode,
                        findRegistrator());
        for (final DataSetPropertyPE property : properties)
        {
            dataSet.addProperty(property);
        }
    }

    private static IEntityProperty[] convertToDataSetProperties(List<NewProperty> list)
    {
        IEntityProperty[] result = new IEntityProperty[list.size()];
        for (int i = 0; i < list.size(); i++)
        {
            result[i] = convertProperty(list.get(i));
        }
        return result;
    }

    private static IEntityProperty convertProperty(NewProperty newProperty)
    {
        IEntityProperty result = new EntityProperty();
        result.setValue(newProperty.getValue());
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(newProperty.getPropertyCode());
        result.setPropertyType(propertyType);
        return result;
    }

    public void updateStatuses(List<String> dataSetCodes, DataSetArchivingStatus newStatus,
            boolean newPresentInArchive)
    {
        getDataDAO().updateDataSetStatuses(dataSetCodes, newStatus, newPresentInArchive);
    }

    public boolean compareAndSetDataSetStatus(DataSetArchivingStatus oldStatus,
            DataSetArchivingStatus newStatus, boolean newPresentInArchive)
    {
        if (data == null || data.isExternalData() == false)
        {
            return false;
        }
        final ExternalDataPE externalData = data.tryAsExternalData();
        if (externalData.getStatus() != oldStatus)
        {
            return false;
        }
        updateStatuses(Arrays.asList(data.getCode()), newStatus, newPresentInArchive);
        return true;
    }

    public void updateManagedProperty(IManagedProperty managedProperty)
    {
        final Set<DataSetPropertyPE> existingProperties = data.getProperties();
        final DataSetTypePE type = data.getDataSetType();
        final PersonPE registrator = findRegistrator();
        data.setProperties(entityPropertiesConverter.updateManagedProperty(existingProperties,
                type, managedProperty, registrator));
    }

}
