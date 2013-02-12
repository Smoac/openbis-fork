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

import org.springframework.dao.DataAccessException;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.IServiceConversationClientManagerLocal;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.RelationshipUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetCodeId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.DataSetTechIdId;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.dataset.IDataSetId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Code;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetKind;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataManagementSystemPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.FileFormatTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.LinkDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewContainerDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewLinkDataSet;
import ch.systemsx.cisd.openbis.generic.shared.dto.NewProperty;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.StorageFormat;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.VocabularyTermPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.types.DataSetTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.util.HibernateUtils;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DataPE.class)
public class DataBO extends AbstractDataSetBusinessObject implements IDataBO
{
    private DataPE data;

    private DataStorePE dataStore;

    public DataBO(IDAOFactory daoFactory, Session session,
            IRelationshipService relationshipService,
            IServiceConversationClientManagerLocal conversationClient,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        super(daoFactory, session, relationshipService, conversationClient,
                managedPropertyEvaluatorFactory);
    }

    public DataBO(IDAOFactory daoFactory, Session exampleSession,
            IEntityPropertiesConverter propertiesConverter,
            IRelationshipService relationshipService,
            IServiceConversationClientManagerLocal conversationClient,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory)
    {
        super(daoFactory, exampleSession, propertiesConverter, relationshipService,
                conversationClient, managedPropertyEvaluatorFactory);
    }

    @Override
    public DataPE tryFindByDataSetId(IDataSetId dataSetId)
    {
        if (dataSetId == null)
        {
            throw new IllegalArgumentException("Data set id cannot be null");
        }
        if (dataSetId instanceof DataSetCodeId)
        {
            DataSetCodeId codeId = (DataSetCodeId) dataSetId;
            return getDataDAO().tryToFindDataSetByCode(codeId.getCode());
        } else if (dataSetId instanceof DataSetTechIdId)
        {
            DataSetTechIdId techIdId = (DataSetTechIdId) dataSetId;
            return getDataDAO().tryGetByTechId(new TechId(techIdId.getTechId()));
        } else
        {
            throw new IllegalArgumentException("Unsupported data set id: " + dataSetId);
        }
    }

    @Override
    public DataPE tryGetData()
    {
        return data;
    }

    @Override
    public DataPE getData()
    {
        // TODO 2010-04-12, CR: This should throw an exception if the external data is null
        // -- will that cause problems with clients?
        return data;
    }

    @Override
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

    @Override
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
        HibernateUtils.initialize(data.getDataSetType().getDataSetTypePropertyTypes());
    }

    @Override
    public void enrichWithParentsAndExperiment()
    {
        if (data != null)
        {
            enrichWithParentsAndExperiment(data);
        }
    }

    @Override
    public void enrichWithChildren()
    {
        if (data != null)
        {
            enrichWithChildren(data);
        }
    }

    @Override
    public final void enrichWithProperties()
    {
        if (data != null)
        {
            HibernateUtils.initialize(data.getProperties());
        }
    }

    @Override
    public void enrichWithContainedDataSets()
    {
        if (data != null && data.isContainer())
        {
            HibernateUtils.initialize(data.getContainedDataSets());
        }
    }

    @Override
    public void define(NewExternalData newData, SamplePE sample, SourceType sourceType)
    {
        assert sample != null : "Undefined sample.";

        boolean isContainer = newData instanceof NewContainerDataSet;
        boolean isLink = newData instanceof NewLinkDataSet;
        if (isContainer)
        {
            define((NewContainerDataSet) newData, sourceType);
        } else if (isLink)
        {
            define((NewLinkDataSet) newData, sourceType);
        } else
        {
            define(newData, sourceType);
        }

        final ExperimentPE experiment = sample.getExperiment();

        RelationshipUtils.setSampleForDataSet(data, sample, session);
        RelationshipUtils.setExperimentForDataSet(data, experiment, session);

        setParentDataSets(experiment, newData);
    }

    @Override
    public void define(NewExternalData newData, ExperimentPE experiment, SourceType sourceType)
    {
        assert experiment != null : "Undefined experiment.";

        boolean isContainer = newData instanceof NewContainerDataSet;
        boolean isLink = newData instanceof NewLinkDataSet;
        if (isContainer)
        {
            define((NewContainerDataSet) newData, sourceType);
        } else if (isLink)
        {
            define((NewLinkDataSet) newData, sourceType);
        } else
        {
            define(newData, sourceType);
        }

        RelationshipUtils.setExperimentForDataSet(data, experiment, session);
        setParentDataSets(experiment, newData);
    }

    private void setParentDataSets(ExperimentPE experiment, NewExternalData newData)
    {
        final List<String> parentDataSetCodes = newData.getParentDataSetCodes();
        final Set<DataPE> parentsToAdd = new HashSet<DataPE>();
        if (parentDataSetCodes != null)
        {
            for (String parentCode : parentDataSetCodes)
            {
                final DataPE parent = getOrCreateData(parentCode, experiment);
                parentsToAdd.add(parent);
            }
        }

        replaceParents(data, parentsToAdd, false);
    }

    @Override
    public void setContainedDataSets(ExperimentPE experiment, NewContainerDataSet newData)
    {
        SpacePE containerSpace = data.getSpace();
        // sanity check
        SpacePE newComponentsSpace = experiment.getProject().getSpace();
        if (containerSpace.equals(newComponentsSpace) == false)
        {
            throw UserFailureException.fromTemplate(
                    "Contained data sets need to be in the same space ('%s') as the container.",
                    containerSpace.getCode());
        } else
        {
            if (experiment.equals(data.getExperiment()))
            {
                final List<String> containedDataSetCodes = newData.getContainedDataSetCodes();
                if (containedDataSetCodes != null)
                {
                    PersonPE modifier = findPerson();
                    for (String containedCode : containedDataSetCodes)
                    {
                        final DataPE contained = getOrCreateData(containedCode, experiment);
                        data.addComponent(contained, modifier);
                        checkSameSpace(data, contained);
                    }
                }
            }
        }
    }

    private void define(NewExternalData newData, SourceType sourceType)
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
        externalData.setDataSetType(getDataSetType(dataSetType, DataSetKind.PHYSICAL));
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
        PersonPE registrator = tryToGetRegistrator(newData);
        externalData.setRegistrator(registrator);
        RelationshipUtils.updateModificationDateAndModifier(externalData, registrator);
        dataStore = getDataStoreDAO().tryToFindDataStoreByCode(newData.getDataStoreCode());
        externalData.setDataStore(dataStore);
        defineDataSetProperties(externalData,
                convertToDataSetProperties(newData.getDataSetProperties()));
        externalData.setDerived(sourceType == SourceType.DERIVED);

        data = externalData;
    }

    private void define(NewContainerDataSet newData, SourceType sourceType)
    {
        assert newData != null : "Undefined data.";
        final DataSetType dataSetType = newData.getDataSetType();
        assert dataSetType != null : "Undefined data set type.";
        assert sourceType != null : "Undefined source type.";

        final DataPE dataPE = new DataPE();

        dataPE.setDataProducerCode(newData.getDataProducerCode());
        dataPE.setProductionDate(newData.getProductionDate());
        dataPE.setCode(newData.getCode());
        dataPE.setDataSetType(getDataSetType(dataSetType, DataSetKind.CONTAINER));
        PersonPE registrator = tryToGetRegistrator(newData);
        dataPE.setRegistrator(registrator);
        RelationshipUtils.updateModificationDateAndModifier(dataPE, registrator);
        dataStore = getDataStoreDAO().tryToFindDataStoreByCode(newData.getDataStoreCode());
        dataPE.setDataStore(dataStore);
        defineDataSetProperties(dataPE, convertToDataSetProperties(newData.getDataSetProperties()));
        dataPE.setDerived(sourceType == SourceType.DERIVED);

        data = dataPE;
    }

    private void define(NewLinkDataSet newData, SourceType sourceType)
    {
        assert newData != null : "Undefined data.";
        final DataSetType dataSetType = newData.getDataSetType();
        assert dataSetType != null : "Undefined data set type.";
        assert sourceType != null : "Undefined source type.";

        final LinkDataPE dataPE = new LinkDataPE();

        dataPE.setExternalCode(newData.getExternalCode());

        String code = newData.getExternalDataManagementSystemCode();
        ExternalDataManagementSystemPE externalDMS =
                getExternalDataManagementSystemDAO().tryToFindExternalDataManagementSystemByCode(
                        code);
        dataPE.setExternalDataManagementSystem(externalDMS);

        dataPE.setDataProducerCode(newData.getDataProducerCode());
        dataPE.setProductionDate(newData.getProductionDate());
        dataPE.setCode(newData.getCode());
        dataPE.setDataSetType(getDataSetType(dataSetType, DataSetKind.LINK));
        dataPE.setRegistrator(tryToGetRegistrator(newData));
        dataStore = getDataStoreDAO().tryToFindDataStoreByCode(newData.getDataStoreCode());
        dataPE.setDataStore(dataStore);
        defineDataSetProperties(dataPE, convertToDataSetProperties(newData.getDataSetProperties()));
        dataPE.setDerived(sourceType == SourceType.DERIVED);

        data = dataPE;
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

    private final DataSetTypePE getDataSetType(final DataSetType dataSetType,
            DataSetKind expectedDataSetKind)
    {
        final String dataSetTypeCode = dataSetType.getCode();
        final DataSetTypePE dataSetTypeOrNull =
                getDataSetTypeDAO().tryToFindDataSetTypeByCode(dataSetTypeCode);
        if (dataSetTypeOrNull == null)
        {
            throw UserFailureException.fromTemplate("There is no data set type with code '%s'",
                    dataSetTypeCode);
        }
        String dataSetKind = dataSetTypeOrNull.getDataSetKind();
        if (dataSetKind.equals(expectedDataSetKind.toString()) == false)
        {
            String dataSetKinAsString = dataSetKind.toString();
            throw new UserFailureException("Data set type " + dataSetTypeCode + " is not a "
                    + expectedDataSetKind + " data set type but "
                    + (dataSetKinAsString.startsWith("E") ? "an " : "a ") + dataSetKinAsString
                    + " data set type.");
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

    private final DataPE getOrCreateData(final String dataSetCode, ExperimentPE experiment)
    {
        assert dataSetCode != null : "Unspecified parent data set code.";

        final IDataDAO dataDAO = getDataDAO();
        DataPE result = dataDAO.tryToFindDataSetByCode(dataSetCode);
        if (result == null)
        {
            result = new DataPE();
            result.setDataStore(dataStore);
            result.setCode(dataSetCode);
            String code = DataSetTypeCode.UNKNOWN.getCode();
            result.setDataSetType(getDataSetTypeDAO().tryToFindDataSetTypeByCode(code));
            RelationshipUtils.setExperimentForDataSet(result, experiment, session);
            result.setPlaceholder(true);
            dataDAO.createDataSet(result, findPerson());
        }
        return result;
    }

    @Override
    public void save() throws UserFailureException
    {
        assert data != null : "Undefined external data.";

        IDataDAO dataDAO = getDataDAO();
        String dataCode = data.getCode();
        DataPE placeholder = dataDAO.tryToFindDataSetByCode(dataCode);
        if (placeholder == null)
        {
            dataDAO.createDataSet(data, findPerson());
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
            validateParentsRelationshipGraph(data, data.getParents());

            if (data.isContainer())
            {
                Collection<String> componentCodes = Code.extractCodes(data.getContainedDataSets());
                validateContainerRelationshipGraph(componentCodes);
            }

            data.setPlaceholder(false);
            data.setId(HibernateUtils.getId(placeholder));
            data.setRegistrationDate(new Date());
            RelationshipUtils.updateModificationDateAndModifier(data, findPerson());

            dataDAO.updateDataSet(data, findPerson());
        }
        entityPropertiesConverter.checkMandatoryProperties(data.getProperties(),
                data.getDataSetType());
    }

    @Override
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
        final PersonPE registrator = findPerson();
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

    @Override
    public void update(DataSetUpdatesDTO updates)
    {
        loadDataByTechId(updates.getDatasetId());
        if (updates.getVersion() != data.getVersion())
        {
            throwModifiedEntityException("Data set");
        }
        final SampleIdentifier sampleIdentifierOrNull = updates.getSampleIdentifierOrNull();
        if (sampleIdentifierOrNull != null)
        {
            // update sample and indirectly experiment
            updateSample(data, updates.getSampleIdentifierOrNull());
        } else
        {
            data.setSample(null);
            updateExperiment(data, updates.getExperimentIdentifierOrNull());
        }

        setParents(data, asListOrNull(updates.getModifiedParentDatasetCodesOrNull()));
        setMetaprojects(data, updates.getMetaprojectsOrNull());
        updateContainer(updates.getModifiedContainerDatasetCodeOrNull());
        updateComponents(updates.getModifiedContainedDatasetCodesOrNull());
        updateFileFormatType(data, updates.getFileFormatTypeCode());
        updateProperties(data.getEntityType(), updates.getProperties(), data, data);

        if (data.getContainer() != null)
        {
            // space could be changed by change of experiment
            checkSameSpace(data.getContainer(), data);
        }
        if (data.getContainedDataSets() != null)
        {
            // even if components were not changed
            checkSameSpace(data, data.getContainedDataSets());
        }

        entityPropertiesConverter.checkMandatoryProperties(data.getProperties(),
                data.getDataSetType());
        validateAndSave();
    }

    private List<String> asListOrNull(String[] arrayOrNull)
    {
        if (arrayOrNull == null)
        {
            return null;
        } else
        {
            return Arrays.asList(arrayOrNull);
        }
    }

    private void validateAndSave()
    {
        try
        {
            getDataDAO().validateAndSaveUpdatedEntity(data);
        } catch (final DataAccessException ex)
        {
            throwException(ex, String.format("Data Set '%s'", data.getCode()));
        }
    }

    private void updateContainer(String modifiedContainerDatasetCodeOrNull)
    {
        if (modifiedContainerDatasetCodeOrNull == null)
        {
            return; // container data set was not changed
        } else
        {
            updateContainer(data, modifiedContainerDatasetCodeOrNull);
        }
    }

    private void updateComponents(String[] modifiedContainedDatasetCodesOrNull)
    {
        if (modifiedContainedDatasetCodesOrNull == null)
        {
            return; // contained data sets were not changed
        } else if (modifiedContainedDatasetCodesOrNull.length > 0 && false == data.isContainer())
        {
            throw new UserFailureException("Data set '" + data.getCode()
                    + " is not a container data set, and cannot contain other data sets.");
        } else
        {
            final List<DataPE> currentComponents =
                    new ArrayList<DataPE>(data.getContainedDataSets());
            removeComponents(currentComponents);

            final Set<String> currentCodes = extractCodes(currentComponents);
            final Set<String> newCodes = asSet(asListOrNull(modifiedContainedDatasetCodesOrNull));

            // quick check for direct cycle
            final Set<String> brandNewCodes = new HashSet<String>(newCodes);
            brandNewCodes.removeAll(currentCodes);

            validateContainerRelationshipGraph(brandNewCodes);
            final Set<DataPE> newComponents = findDataSetsByCodes(newCodes);
            addComponents(newComponents);
        }
    }

    private void validateContainerRelationshipGraph(Collection<String> componentCodes)
    {
        if (componentCodes.isEmpty())
        {
            return;
        } else if (componentCodes.contains(data.getCode()))
        {
            throw new UserFailureException(
                    "Data set '"
                            + data.getCode()
                            + "' cannot contain itself as a component neither directly nor via subordinate components.");
        }

        final Set<DataPE> components = findDataSetsByCodes(componentCodes);
        for (DataPE componentDataSet : components)
        {
            if (componentDataSet.isContainer())
            {
                List<DataPE> containedDataSets = componentDataSet.getContainedDataSets();
                validateContainerRelationshipGraph(Code.extractCodes(containedDataSets));
            }
        }

    }

    private void addComponents(Collection<DataPE> componentsToAdd)
    {
        for (DataPE component : componentsToAdd)
        {
            relationshipService.assignDataSetToContainer(session, component, data);
        }
    }

    private void removeComponents(Collection<DataPE> componentsToRemove)
    {
        for (DataPE component : componentsToRemove)
        {
            relationshipService.removeDataSetFromContainer(session, component);
        }
    }

    private final void defineDataSetProperties(final DataPE dataSet,
            final IEntityProperty[] newProperties)
    {
        final String dataSetTypeCode = dataSet.getDataSetType().getCode();
        final List<DataSetPropertyPE> properties =
                entityPropertiesConverter.convertProperties(newProperties, dataSetTypeCode,
                        findPerson());
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

    @Override
    public void updateStatuses(List<String> dataSetCodes, DataSetArchivingStatus newStatus,
            boolean newPresentInArchive)
    {
        getDataDAO().updateDataSetStatuses(dataSetCodes, newStatus, newPresentInArchive);
    }

    @Override
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

    @Override
    public void updateManagedProperty(IManagedProperty managedProperty)
    {
        final Set<DataSetPropertyPE> existingProperties = data.getProperties();
        final DataSetTypePE type = data.getDataSetType();
        final PersonPE registrator = findPerson();
        data.setProperties(entityPropertiesConverter.updateManagedProperty(existingProperties,
                type, managedProperty, registrator));
    }

    @Override
    public void setStorageConfirmed()
    {
        ExternalDataPE externalData = data.tryAsExternalData();
        if (null != externalData)
        {
            externalData.setStorageConfirmation(true);
        }
    }

    @Override
    public boolean isStorageConfirmed()
    {
        ExternalDataPE externalData = data.tryAsExternalData();
        if (null != externalData)
        {
            return externalData.isStorageConfirmation();
        }
        return true;
    }
}
