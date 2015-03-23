/*
 * Copyright 2007 ETH Zuerich, CISD
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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.collection.CollectionUtils;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.IDataStoreServiceFactory;
import ch.systemsx.cisd.openbis.generic.server.business.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.server.business.bo.util.DataSetTypeWithoutExperimentChecker;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.EntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ICorePluginDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDeletionDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityHistoryDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityOperationsLogDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataManagementSystemDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGridCustomColumnDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGridCustomFilterDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ILocatorTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMetaprojectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPostRegistrationDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IQueryDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRelationshipTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IScriptDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISpaceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyTermDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.RelationshipUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ICodeSequenceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.IPermIdDAO;
import ch.systemsx.cisd.openbis.generic.server.util.SpaceIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.IDataStoreService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Identifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentHolderPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataStorePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityPropertiesHolder;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.IModifierAndModificationDateBean;
import ch.systemsx.cisd.openbis.generic.shared.dto.MaterialPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.managed_property.IManagedPropertyEvaluatorFactory;
import ch.systemsx.cisd.openbis.generic.shared.translator.AttachmentTranslator;

/**
 * An <code>abstract</code> <i>Business Object</i>.
 * 
 * @author Franz-Josef Elmer
 */
abstract class AbstractBusinessObject implements IDAOFactory
{
    private final IDAOFactory daoFactory;

    protected final Session session;

    protected final IEntityPropertiesConverter entityPropertiesConverter;

    protected final IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory;

    protected final DataSetTypeWithoutExperimentChecker dataSetTypeChecker;
    
    protected final IRelationshipService relationshipService;

    protected Map<String, List<AttachmentPE>> attachmentHolderPermIdToAttachmentsMap;

    AbstractBusinessObject(final IDAOFactory daoFactory, final Session session,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker, 
            IRelationshipService relationshipService)
    {
        this(daoFactory, session, (IEntityPropertiesConverter) null,
                managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
    }

    AbstractBusinessObject(final IDAOFactory daoFactory, final Session session,
            EntityKind entityKindOrNull,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker, 
            IRelationshipService relationshipService)
    {
        this(daoFactory, session, entityKindOrNull == null ? null : new EntityPropertiesConverter(
                entityKindOrNull, daoFactory, managedPropertyEvaluatorFactory),
                managedPropertyEvaluatorFactory, dataSetTypeChecker, relationshipService);
    }

    AbstractBusinessObject(final IDAOFactory daoFactory, final Session session,
            IEntityPropertiesConverter converter,
            IManagedPropertyEvaluatorFactory managedPropertyEvaluatorFactory,
            DataSetTypeWithoutExperimentChecker dataSetTypeChecker, 
            IRelationshipService relationshipService)
    {
        this.dataSetTypeChecker = dataSetTypeChecker;
        this.relationshipService = relationshipService;
        assert daoFactory != null : "Given DAO factory can not be null.";
        assert session != null : "Given session can not be null.";

        this.daoFactory = daoFactory;
        this.session = session;
        entityPropertiesConverter = converter;
        this.managedPropertyEvaluatorFactory = managedPropertyEvaluatorFactory;
    }

    @Override
    public SessionFactory getSessionFactory()
    {
        return daoFactory.getSessionFactory();
    }

    protected final PersonPE findPerson()
    {
        final PersonPE actor = session.tryGetPerson();
        assert actor != null : "Missing person instance in session object.";
        return actor;
    }

    protected void fillSpaceIdentifiers(final List<? extends SpaceIdentifier> spaceIdentifiers)
    {
        if (spaceIdentifiers != null)
        {
            for (SpaceIdentifier spaceIdentifier : spaceIdentifiers)
            {
                fillSpaceIdentifier(spaceIdentifier);
            }
        }
    }

    protected void fillSpaceIdentifier(final SpaceIdentifier spaceIdentifier)
    {
        if (org.apache.commons.lang.StringUtils.isBlank(spaceIdentifier.getSpaceCode()))
        {
            final SpacePE space =
                    SpaceIdentifierHelper.tryGetSpace(spaceIdentifier, findPerson(), this);
            checkNotNull(spaceIdentifier, space);
            spaceIdentifier.setSpaceCode(space.getCode());
        }
    }

    private static void checkNotNull(final SpaceIdentifier spaceIdentifier, final SpacePE group)
    {
        if (group == null)
        {
            throw new UserFailureException("Unknown space '" + spaceIdentifier + "'.");
        }
    }

    protected final static void throwException(final DataAccessException exception,
            final String subject) throws UserFailureException
    {
        throwException(exception, subject, null);
    }

    protected final static void throwException(final DataAccessException exception,
            final String subject, final EntityKind entityKindOrNull) throws UserFailureException
    {
        DataAccessExceptionTranslator.throwException(exception, subject, entityKindOrNull);
    }

    protected final static void throwEntityInUseException(final String subject,
            final EntityKind entityKindOrNull) throws UserFailureException
    {
        DataAccessExceptionTranslator.throwForeignKeyViolationException(subject, entityKindOrNull);
    }

    private static final String ERR_MODIFIED_ENTITY =
            "%s has been modified in the meantime. Reopen tab to be able to continue with refreshed data.";

    protected final static void throwModifiedEntityException(String entityName)
    {
        throw UserFailureException.fromTemplate(ERR_MODIFIED_ENTITY, entityName);
    }

    protected static IDataStoreService tryGetDataStoreService(DataStorePE dataStore, 
            IDataStoreServiceFactory dataStoreServiceFactory)
    {
        String remoteURL = dataStore.getRemoteUrl();
        if (StringUtils.isBlank(remoteURL))
        {
            // null if DSS URL has not been specified
            return null;
        }
        return dataStoreServiceFactory.create(remoteURL);
    }

    /**
     * Returns the perm ID of specified identifier or creates a new one if it is <code>null</code>.
     */
    protected String getOrCreatePermID(Identifier<?> identifier)
    {
        String permID = identifier.getPermID();
        return permID == null ? getPermIdDAO().createPermId() : permID;
    }

    Set<String> extractPropertiesCodes(List<IEntityProperty> properties)
    {
        Set<String> propertiesCodes = new HashSet<String>();
        if (properties != null)
        {
            for (IEntityProperty property : properties)
            {
                propertiesCodes.add(property.getPropertyType().getCode());
            }
        }
        return propertiesCodes;
    }

    protected <T extends EntityPropertyPE> Set<T> convertProperties(final EntityTypePE type,
            final Set<T> existingProperties, List<IEntityProperty> properties, Set<String> propertiesToUpdate)
    {
        final PersonPE registrator = findPerson();
        return entityPropertiesConverter.updateProperties(existingProperties, type, properties,
                registrator, propertiesToUpdate);
    }

    protected void setMetaprojects(IEntityWithMetaprojects entity, String[] metaprojectsOrNull)
    {
        if (entity == null)
        {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        if (metaprojectsOrNull == null)
        {
            return;
        }

        PersonPE owner = getPersonDAO().tryFindPersonByUserId(session.getUserName());
        Set<MetaprojectPE> metaprojects = new HashSet<MetaprojectPE>();

        for (String metaprojectsOrNullItem : metaprojectsOrNull)
        {
            if (metaprojectsOrNullItem == null)
            {
                throw new IllegalArgumentException("Metaproject cannot be null");
            }

            MetaprojectPE metaproject =
                    getMetaprojectDAO().tryFindByOwnerAndName(session.getUserName(),
                            metaprojectsOrNullItem);

            if (metaproject == null)
            {
                metaproject = new MetaprojectPE();
                metaproject.setName(metaprojectsOrNullItem);
                metaproject.setOwner(owner);
                getMetaprojectDAO().createOrUpdateMetaproject(metaproject, owner);
            }
            metaprojects.add(metaproject);
        }

        Collection<MetaprojectPE> currentMetaprojects = null;

        if (entity.getId() != null)
        {
            currentMetaprojects = getMetaprojectDAO().listMetaprojectsForEntity(owner, entity);
        } else
        {
            currentMetaprojects = new HashSet<MetaprojectPE>();
        }

        Set<MetaprojectPE> metaprojectsToAdd = new HashSet<MetaprojectPE>();
        Set<MetaprojectPE> metaprojectsToRemove = new HashSet<MetaprojectPE>();

        for (MetaprojectPE metaproject : metaprojects)
        {
            if (currentMetaprojects.contains(metaproject) == false)
            {
                metaprojectsToAdd.add(metaproject);
            }
        }
        for (MetaprojectPE currentMetaproject : currentMetaprojects)
        {
            if (metaprojects.contains(currentMetaproject) == false)
            {
                metaprojectsToRemove.add(currentMetaproject);
            }
        }

        for (MetaprojectPE metaprojectToAdd : metaprojectsToAdd)
        {
            entity.addMetaproject(metaprojectToAdd);
        }
        for (MetaprojectPE metaprojectToRemove : metaprojectsToRemove)
        {
            entity.removeMetaproject(metaprojectToRemove);
        }
    }

    //
    // IDAOFactory
    //

    @Override
    public final ISpaceDAO getSpaceDAO()
    {
        return daoFactory.getSpaceDAO();
    }

    @Override
    public final IScriptDAO getScriptDAO()
    {
        return daoFactory.getScriptDAO();
    }

    @Override
    public final IPersonDAO getPersonDAO()
    {
        return daoFactory.getPersonDAO();
    }

    @Override
    public final IRoleAssignmentDAO getRoleAssignmentDAO()
    {
        return daoFactory.getRoleAssignmentDAO();
    }

    @Override
    public final ISampleDAO getSampleDAO()
    {
        return daoFactory.getSampleDAO();
    }

    @Override
    public final ISampleTypeDAO getSampleTypeDAO()
    {
        return daoFactory.getSampleTypeDAO();
    }

    @Override
    public final IDataDAO getDataDAO()
    {
        return daoFactory.getDataDAO();
    }

    @Override
    public final IHibernateSearchDAO getHibernateSearchDAO()
    {
        return daoFactory.getHibernateSearchDAO();
    }

    @Override
    public IPropertyTypeDAO getPropertyTypeDAO()
    {
        return daoFactory.getPropertyTypeDAO();
    }

    @Override
    public IEntityTypeDAO getEntityTypeDAO(final EntityKind entityKind)
    {
        return daoFactory.getEntityTypeDAO(entityKind);
    }

    @Override
    public IEntityPropertyTypeDAO getEntityPropertyTypeDAO(final EntityKind entityKind)
    {
        return daoFactory.getEntityPropertyTypeDAO(entityKind);
    }

    @Override
    public IEntityHistoryDAO getEntityPropertyHistoryDAO()
    {
        return daoFactory.getEntityPropertyHistoryDAO();
    }

    @Override
    public IExperimentDAO getExperimentDAO()
    {
        return daoFactory.getExperimentDAO();
    }

    @Override
    public IProjectDAO getProjectDAO()
    {
        return daoFactory.getProjectDAO();
    }

    @Override
    public final IVocabularyDAO getVocabularyDAO()
    {
        return daoFactory.getVocabularyDAO();
    }

    @Override
    public final IVocabularyTermDAO getVocabularyTermDAO()
    {
        return daoFactory.getVocabularyTermDAO();
    }

    @Override
    public final IAttachmentDAO getAttachmentDAO()
    {
        return daoFactory.getAttachmentDAO();
    }

    @Override
    public IDataSetTypeDAO getDataSetTypeDAO()
    {
        return daoFactory.getDataSetTypeDAO();
    }

    @Override
    public IFileFormatTypeDAO getFileFormatTypeDAO()
    {
        return daoFactory.getFileFormatTypeDAO();
    }

    @Override
    public ILocatorTypeDAO getLocatorTypeDAO()
    {
        return daoFactory.getLocatorTypeDAO();
    }

    @Override
    public IMaterialDAO getMaterialDAO()
    {
        return daoFactory.getMaterialDAO();
    }

    @Override
    public ICodeSequenceDAO getCodeSequenceDAO()
    {
        return daoFactory.getCodeSequenceDAO();
    }

    @Override
    public IDataStoreDAO getDataStoreDAO()
    {
        return daoFactory.getDataStoreDAO();
    }

    @Override
    public IPermIdDAO getPermIdDAO()
    {
        return daoFactory.getPermIdDAO();
    }

    @Override
    public IEventDAO getEventDAO()
    {
        return daoFactory.getEventDAO();
    }

    @Override
    public final IDeletionDAO getDeletionDAO()
    {
        return daoFactory.getDeletionDAO();
    }

    @Override
    public void setBatchUpdateMode(boolean batchMode)
    {
        daoFactory.setBatchUpdateMode(batchMode);
    }

    @Override
    public IAuthorizationGroupDAO getAuthorizationGroupDAO()
    {
        return daoFactory.getAuthorizationGroupDAO();
    }

    @Override
    public PersistencyResources getPersistencyResources()
    {
        return daoFactory.getPersistencyResources();
    }

    @Override
    public IGridCustomFilterDAO getGridCustomFilterDAO()
    {
        return daoFactory.getGridCustomFilterDAO();
    }

    @Override
    public IGridCustomColumnDAO getGridCustomColumnDAO()
    {
        return daoFactory.getGridCustomColumnDAO();
    }

    @Override
    public IQueryDAO getQueryDAO()
    {
        return daoFactory.getQueryDAO();
    }

    @Override
    public IRelationshipTypeDAO getRelationshipTypeDAO()
    {
        return daoFactory.getRelationshipTypeDAO();
    }

    @Override
    public ICorePluginDAO getCorePluginDAO()
    {
        return daoFactory.getCorePluginDAO();
    }

    @Override
    public IPostRegistrationDAO getPostRegistrationDAO()
    {
        return daoFactory.getPostRegistrationDAO();
    }

    @Override
    public IEntityOperationsLogDAO getEntityOperationsLogDAO()
    {
        return daoFactory.getEntityOperationsLogDAO();
    }

    @Override
    public IExternalDataManagementSystemDAO getExternalDataManagementSystemDAO()
    {
        return daoFactory.getExternalDataManagementSystemDAO();
    }

    @Override
    public IMetaprojectDAO getMetaprojectDAO()
    {
        return daoFactory.getMetaprojectDAO();
    }

    protected RelationshipTypePE getParentChildRelationshipType()
    {
        return RelationshipUtils.getParentChildRelationshipType(getRelationshipTypeDAO());
    }

    protected RelationshipTypePE getContainerComponentRelationshipType()
    {
        return RelationshipUtils.getContainerComponentRelationshipType(getRelationshipTypeDAO());
    }

    protected void updateProperties(EntityTypePE entityType, List<IEntityProperty> properties, Set<String> propertiesToUpdate,
            IEntityPropertiesHolder entityAsPropertiesHolder,
            IModifierAndModificationDateBean entityAsModifiableBean)
    {
        Set<? extends EntityPropertyPE> existingProperties =
                entityAsPropertiesHolder.getProperties();
        Map<String, Object> existingPropertyValuesByCode = new HashMap<String, Object>();
        for (EntityPropertyPE existingProperty : existingProperties)
        {
            String propertyCode =
                    existingProperty.getEntityTypePropertyType().getPropertyType().getCode();
            existingPropertyValuesByCode.put(propertyCode, getValue(existingProperty));
        }
        Set<? extends EntityPropertyPE> convertedProperties =
                convertProperties(entityType, existingProperties, properties, propertiesToUpdate);
        if (isEquals(existingPropertyValuesByCode, convertedProperties) == false)
        {
            entityAsPropertiesHolder.setProperties(convertedProperties);
            entityAsModifiableBean.setModifier(findPerson());
            entityAsModifiableBean.setModificationDate(new Date());
        }
    }

    private boolean isEquals(Map<String, Object> existingPropertyValuesByCode,
            Set<? extends EntityPropertyPE> properties)
    {
        for (EntityPropertyPE property : properties)
        {
            Object existingValue =
                    existingPropertyValuesByCode.remove(property.getEntityTypePropertyType()
                            .getPropertyType().getCode());
            if (existingValue == null || existingValue.equals(getValue(property)) == false)
            {
                return false;
            }
        }
        return existingPropertyValuesByCode.isEmpty();
    }

    private Object getValue(EntityPropertyPE property)
    {
        String value = property.getValue();
        if (value != null)
        {
            return value;
        }
        MaterialPE materialValue = property.getMaterialValue();
        if (materialValue != null)
        {
            return materialValue;
        }
        return property.getVocabularyTerm();
    }

    protected AttachmentPE prepareAttachment(IModifierAndModificationDateBean beanOrNull,
            NewAttachment attachment)
    {
        final AttachmentPE attachmentPE = AttachmentTranslator.translate(attachment);
        prepareAttachment(beanOrNull, attachmentPE);
        return attachmentPE;
    }

    protected void prepareAttachment(IModifierAndModificationDateBean beanOrNull,
            AttachmentPE attachment)
    {
        PersonPE user = findPerson();
        attachment.setRegistrator(user);
        escapeFileName(attachment);
        RelationshipUtils.updateModificationDateAndModifier(beanOrNull, user);
    }

    private void escapeFileName(final AttachmentPE attachment)
    {
        if (attachment != null)
        {
            attachment.setFileName(AttachmentHolderPE.escapeFileName(attachment.getFileName()));
        }
    }

    protected void addAttachments(IModifierAndModificationDateBean holder,
            Collection<NewAttachment> newAttachments, List<AttachmentPE> attachments)
    {
        if (newAttachments == null)
        {
            return;
        }
        for (NewAttachment attachment : newAttachments)
        {
            attachments.add(prepareAttachment(holder, attachment));
        }
    }

    protected void putAttachments(String attachmentHolderPermId, List<AttachmentPE> attachments)
    {
        if (attachments == null || attachments.isEmpty())
        {
            return;
        }

        List<AttachmentPE> currentAttachments = attachmentHolderPermIdToAttachmentsMap.get(attachmentHolderPermId);

        if (currentAttachments == null)
        {
            currentAttachments = new LinkedList<AttachmentPE>();
            attachmentHolderPermIdToAttachmentsMap.put(attachmentHolderPermId, currentAttachments);
        }

        currentAttachments.addAll(attachments);
    }

    protected void saveAttachment(final AttachmentHolderPE attachmentHolder,
            List<AttachmentPE> attachments)
    {
        if (attachments.isEmpty())
        {
            return;
        }
        AttachmentHolderPE actualAttachmentHolder = attachmentHolder;
        final IAttachmentDAO dao = getAttachmentDAO();
        for (final AttachmentPE attachment : attachments)
        {
            try
            {
                actualAttachmentHolder = dao.createAttachment(attachment, actualAttachmentHolder);
            } catch (final DataAccessException e)
            {
                final String fileName = attachment.getFileName();
                throwException(
                        e,
                        String.format("Filename '%s' for %s '%s'", fileName,
                                attachmentHolder.getHolderName(), attachmentHolder.getIdentifier()));
            }
        }
        attachments.clear();
    }

    protected void saveAttachments(List<? extends AttachmentHolderPE> holders,
            Map<String, List<AttachmentPE>> holderPermIdToAttachmentsMapOrNull)
    {
        if (holderPermIdToAttachmentsMapOrNull == null)
        {
            return;
        }

        for (AttachmentHolderPE holder : holders)
        {
            List<AttachmentPE> attachments = holderPermIdToAttachmentsMapOrNull.get(holder.getPermId());
            if (attachments != null)
            {
                saveAttachment(holder, attachments);
            }
        }
    }
    
    protected void checkSampleWithoutDatasets(SamplePE sample)
    {
        List<DataPE> dataSets = getDataDAO().listDataSets(sample);
        String sampleIdentifier = sample.getIdentifier();
        checkDataSetsDoNotNeedAnExperiment(sampleIdentifier, dataSets);
    }

    protected void checkDataSetsDoNotNeedAnExperiment(String sampleIdentifier, List<DataPE> dataSets)
    {
        List<String> dataSetsNeedingExperiment = new ArrayList<String>();
        for (DataPE dataSet : dataSets)
        {
            String dataSetTypeCode = dataSet.getDataSetType().getCode();
            if (dataSetTypeChecker.isDataSetTypeWithoutExperiment(dataSetTypeCode) == false)
            {
                dataSetsNeedingExperiment.add(dataSet.getCode());
            }
        }
        if (dataSetsNeedingExperiment.isEmpty() == false)
        {
            throw new UserFailureException("Operation cannot be performed, because the sample "
                    + sampleIdentifier + " has the following datasets which need an experiment: "
                    + CollectionUtils.abbreviate(dataSetsNeedingExperiment, 10));
        }
    }
    
    protected boolean hasDatasets2(IDataDAO dataDAO, SamplePE sample)
    {
        assert sample != null;

        return dataDAO.hasDataSet(sample);
    }

}
