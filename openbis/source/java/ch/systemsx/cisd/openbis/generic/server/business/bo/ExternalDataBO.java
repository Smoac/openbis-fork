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
import java.util.List;
import java.util.Set;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SourceType;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataSetUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DataPE.class)
public class ExternalDataBO extends AbstractExternalDataBusinessObject implements IExternalDataBO
{
    private ExternalDataPE externalData;

    protected final IEntityPropertiesConverter entityPropertiesConverter;

    public ExternalDataBO(IDAOFactory daoFactory, Session session)
    {
        this(daoFactory, session, new EntityPropertiesConverter(EntityKind.DATA_SET, daoFactory));
    }

    public ExternalDataBO(IDAOFactory daoFactory, Session session,
            IEntityPropertiesConverter entityPropertiesConverter)
    {
        super(daoFactory, session);
        this.entityPropertiesConverter = entityPropertiesConverter;
    }

    public ExternalDataPE getExternalData()
    {
        return externalData;
    }

    public void loadByCode(String dataSetCode)
    {
        externalData = getExternalDataDAO().tryToFindFullDataSetByCode(dataSetCode, true);
    }

    static final String PROPERTY_TYPES = "dataSetType.dataSetTypePropertyTypesInternal";

    public void loadDataByTechId(TechId datasetId)
    {
        String[] connections =
            { PROPERTY_TYPES };
        externalData = getExternalDataDAO().tryGetByTechId(datasetId, connections);
        if (externalData == null)
        {
            throw new UserFailureException(String.format("Data set with ID '%s' does not exist.",
                    datasetId));
        }
    }

    public void enrichWithParentsAndExperiment()
    {
        if (externalData != null)
        {
            enrichWithParentsAndExperiment(externalData);
        }
    }

    public void enrichWithChildren()
    {
        if (externalData != null)
        {
            enrichWithChildren(externalData);
        }
    }

    public final void enrichWithProperties()
    {
        if (externalData != null)
        {
            HibernateUtils.initialize(externalData.getProperties());
        }
    }

    public void define(NewExternalData data, SamplePE sample, SourceType sourceType)
    {
        assert sample != null : "Undefined sample.";
        assert data.getParentDataSetCodes() == null || data.getParentDataSetCodes().isEmpty();

        define(data, sourceType);

        externalData.setSample(sample);
        externalData.setupExperiment(sample.getExperiment());
    }

    public void define(NewExternalData data, ExperimentPE experiment, SourceType sourceType)
    {
        assert experiment != null : "Undefined experiment.";
        DataStorePE dataStore = define(data, sourceType);

        externalData.setupExperiment(experiment);
        final List<String> parentDataSetCodes = data.getParentDataSetCodes();
        if (parentDataSetCodes != null)
        {
            for (String parentCode : parentDataSetCodes)
            {
                final DataPE parent = getOrCreateParentData(parentCode, dataStore, experiment);
                externalData.addParent(parent);
            }
        }
    }

    private DataStorePE define(NewExternalData data, SourceType sourceType)
    {
        assert data != null : "Undefined data.";
        final DataSetType dataSetType = data.getDataSetType();
        assert dataSetType != null : "Undefined data set type.";
        final FileFormatType fileFormatType = data.getFileFormatType();
        assert fileFormatType != null : "Undefined file format type.";
        final String location = data.getLocation();
        assert location != null : "Undefined location.";
        final LocatorType locatorType = data.getLocatorType();
        assert locatorType != null : "Undefined location type.";
        assert sourceType != null : "Undefined source type.";

        externalData = new ExternalDataPE();

        externalData.setDataProducerCode(data.getDataProducerCode());
        externalData.setProductionDate(data.getProductionDate());
        externalData.setCode(data.getCode());
        externalData.setDataSetType(getDataSetType(dataSetType));
        externalData.setFileFormatType(getFileFomatType(fileFormatType));
        externalData.setComplete(data.getComplete());
        externalData.setLocation(location);
        externalData.setStorageFormatVocabularyTerm(tryToFindStorageFormatTerm(data
                .getStorageFormat()));
        externalData.setLocatorType(getLocatorTypeDAO().tryToFindLocatorTypeByCode(
                locatorType.getCode()));
        DataStorePE dataStore = getDataStoreDAO().tryToFindDataStoreByCode(data.getDataStoreCode());
        externalData.setDataStore(dataStore);
        defineDataSetProperties(externalData, convertToDataSetProperties(data
                .getDataSetProperties()));
        externalData.setDerived(sourceType == SourceType.DERIVED);
        return dataStore;
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

        final IExternalDataDAO dataDAO = getExternalDataDAO();
        DataPE parent = dataDAO.tryToFindDataSetByCode(parentDataSetCode);
        if (parent == null)
        {
            parent = new DataPE();
            parent.setDataStore(dataStore);
            parent.setCode(parentDataSetCode);
            String code = DataSetTypeCode.UNKNOWN.getCode();
            parent.setDataSetType(getDataSetTypeDAO().tryToFindDataSetTypeByCode(code));
            parent.setupExperiment(experiment);
            parent.setPlaceholder(true);
            dataDAO.createDataSet(parent);
        }
        return parent;
    }

    public void save() throws UserFailureException
    {
        assert externalData != null : "Undefined external data.";
        IExternalDataDAO externalDataDAO = getExternalDataDAO();
        String dataCode = externalData.getCode();
        DataPE data = externalDataDAO.tryToFindDataSetByCode(dataCode);
        if (data == null)
        {
            externalDataDAO.createDataSet(externalData);
        } else
        {
            if (data.isPlaceholder() == false)
            {
                throw new UserFailureException("Already existing data set for code '" + dataCode
                        + "' can not be updated by data set " + externalData);
            }
            // NOTE: If new data set is created and there was no placeholder
            // cycles will not be created because only connections to parents are added
            // and we assume that there were no cycles before. On the other hand placeholders
            // have at least one child so cycles need to be checked when they are updated.
            validateRelationshipGraph(externalData.getParents());

            externalData.setPlaceholder(false);
            externalData.setId(HibernateUtils.getId(data));
            externalData.setRegistrationDate(new Date());
            externalData.setModificationDate(data.getModificationDate());

            externalDataDAO.updateDataSet(externalData);
        }
        entityPropertiesConverter.checkMandatoryProperties(externalData.getProperties(),
                externalData.getDataSetType());
    }

    public void update(DataSetUpdatesDTO updates)
    {
        loadDataByTechId(updates.getDatasetId());
        if (updates.getVersion().equals(externalData.getModificationDate()) == false)
        {
            throwModifiedEntityException("Data set");
        }
        final SampleIdentifier sampleIdentifierOrNull = updates.getSampleIdentifierOrNull();
        if (sampleIdentifierOrNull != null)
        {
        	// update sample and indirectly experiment
            updateSample(updates.getSampleIdentifierOrNull()); 
            // remove connections with parents 
            // (new colelction is needed bacause old one will be removed)
            removeParents(new ArrayList<DataPE>(externalData.getParents()));
        } else
        {
            updateExperiment(updates.getExperimentIdentifierOrNull());
            updateParents(updates.getModifiedParentDatasetCodesOrNull());
            // remove connection with sample
            externalData.setSample(null);
        }
        updateFileFormatType(updates.getFileFormatTypeCode());
		updateProperties(updates.getProperties());
        entityPropertiesConverter.checkMandatoryProperties(externalData.getProperties(),
                externalData.getDataSetType());
        validateAndSave();
    }

    private void validateAndSave()
    {
        getExternalDataDAO().validateAndSaveUpdatedEntity(externalData);
    }

    private void updateProperties(List<IEntityProperty> properties)
    {
        final Set<DataSetPropertyPE> existingProperties = externalData.getProperties();
        final EntityTypePE type = externalData.getDataSetType();
        final PersonPE registrator = findRegistrator();
        externalData.setProperties(entityPropertiesConverter.updateProperties(existingProperties,
                type, properties, registrator));
    }

    private void updateParents(String[] modifiedParentDatasetCodesOrNull)
    {
        if (modifiedParentDatasetCodesOrNull == null)
        {
            return; // parents were not changed
        } else
        {
            final Set<DataPE> currentParents = externalData.getParents();
            final Set<String> currentParentCodes = extractCodes(currentParents);
            final Set<String> newCodes = asSet(modifiedParentDatasetCodesOrNull);
            newCodes.removeAll(currentParentCodes);

            // quick check for direct cycle
            if (newCodes.contains(externalData.getCode()))
            {
                throw new UserFailureException("Data set '" + externalData.getCode()
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
        final TechId updatedDataSetId = TechId.create(externalData);
        final Set<TechId> visited = new HashSet<TechId>();
        Set<TechId> toVisit = new HashSet<TechId>();
        toVisit.add(TechId.create(parentToAdd));
        while (toVisit.isEmpty() == false)
        {
            if (toVisit.contains(updatedDataSetId))
            {
                throw UserFailureException.fromTemplate(
                        "Data Set '%s' is an ancestor of Data Set '%s' "
                                + "and cannot be at the same time set as its child.", externalData
                                .getCode(), parentToAdd.getCode());
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
        return getExternalDataDAO().findParentIds(dataSetIds);
    }

    private Collection<DataPE> filterDataSets(Collection<DataPE> dataSets,
            Collection<String> seekenCodes)
    {
        Collection<DataPE> result = new ArrayList<DataPE>();
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
            externalData.addParent(parent);
        }
    }

    private void removeParents(Collection<DataPE> parentsToRemove)
    {
        for (DataPE parent : parentsToRemove)
        {
            externalData.removeParent(parent);
        }
    }

    private List<DataPE> findDataSetsByCodes(Set<String> codes)
    {
        final IExternalDataDAO dao = getExternalDataDAO();
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
                    "Data Sets with following codes do not exist: '%s'.", CollectionUtils
                            .abbreviate(missingDataSetCodes, 10));
        } else
        {
            return dataSets;
        }
    }

    private static Set<String> asSet(String[] objects)
    {
        return new HashSet<String>(Arrays.asList(objects));
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
        SamplePE previousSampleOrNull = externalData.tryGetSample();
        if (newSample.equals(previousSampleOrNull))
        {
            return; // nothing to change
        }
        if (newSample.getGroup() == null)
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
        if (experiment.equals(externalData.getExperiment()) == false)
        {
            externalData.setupExperiment(experiment);
        }
        externalData.setSample(newSample);
    }

    private void updateExperiment(ExperimentIdentifier experimentIdentifierOrNull)
    {
        assert experimentIdentifierOrNull != null;
        ExperimentPE experiment = getExperimentByIdentifier(experimentIdentifierOrNull);
        externalData.setupExperiment(experiment);
    }

    private ExperimentPE getExperimentByIdentifier(final ExperimentIdentifier identifier)
    {
        assert identifier != null : "Experiment identifier unspecified.";
        final ProjectPE project =
                getProjectDAO().tryFindProject(identifier.getDatabaseInstanceCode(),
                        identifier.getGroupCode(), identifier.getProjectCode());
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

    private UserFailureException createWrongSampleException(SamplePE sample, String reason)
    {
        return UserFailureException.fromTemplate(
                "The dataset '%s' cannot be connected to the sample '%s'" + " because %s.",
                externalData.getCode(), sample.getIdentifier(), reason);
    }

    private final void defineDataSetProperties(final ExternalDataPE data,
            final IEntityProperty[] newProperties)
    {
        final String dataSetTypeCode = data.getDataSetType().getCode();
        final List<DataSetPropertyPE> properties =
                entityPropertiesConverter.convertProperties(newProperties, dataSetTypeCode,
                        findRegistrator());
        for (final DataSetPropertyPE property : properties)
        {
            data.addProperty(property);
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

}
