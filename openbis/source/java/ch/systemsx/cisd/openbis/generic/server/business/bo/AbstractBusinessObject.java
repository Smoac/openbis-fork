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

import java.sql.Connection;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.EntityPropertiesConverter;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ICorePluginDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
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
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ICodeSequenceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.IPermIdDAO;
import ch.systemsx.cisd.openbis.generic.server.util.SpaceIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Identifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityPropertyPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.EntityTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.IEntityWithMetaprojects;
import ch.systemsx.cisd.openbis.generic.shared.dto.MetaprojectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

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

    AbstractBusinessObject(final IDAOFactory daoFactory, final Session session)
    {
        this(daoFactory, session, (IEntityPropertiesConverter) null);
    }

    AbstractBusinessObject(final IDAOFactory daoFactory, final Session session,
            EntityKind entityKindOrNull)
    {
        this(daoFactory, session, entityKindOrNull == null ? null : new EntityPropertiesConverter(
                entityKindOrNull, daoFactory));
    }

    AbstractBusinessObject(final IDAOFactory daoFactory, final Session session,
            IEntityPropertiesConverter converter)
    {
        assert daoFactory != null : "Given DAO factory can not be null.";
        assert session != null : "Given session can not be null.";

        this.daoFactory = daoFactory;
        this.session = session;
        entityPropertiesConverter = converter;
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

    protected void fillSpaceIdentifier(final SpaceIdentifier spaceIdentifier)
    {
        if (org.apache.commons.lang.StringUtils.isBlank(spaceIdentifier.getSpaceCode()))
        {
            final SpacePE space =
                    SpaceIdentifierHelper.tryGetSpace(spaceIdentifier, findPerson(), this);
            checkNotNull(spaceIdentifier, space);
            spaceIdentifier.setDatabaseInstanceCode(space.getDatabaseInstance().getCode());
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

    /**
     * Returns the perm ID of specified identifier or creates a new one if it is <code>null</code>.
     */
    protected String getOrCreatePermID(Identifier<?> identifier)
    {
        String permID = identifier.getPermID();
        return permID == null ? getPermIdDAO().createPermId() : permID;
    }

    protected <T extends EntityPropertyPE> Set<T> convertProperties(final EntityTypePE type,
            final Set<T> existingProperties, List<IEntityProperty> properties)
    {
        final PersonPE registrator = findPerson();
        Set<String> propertiesToUpdate = new HashSet<String>();
        if (properties != null)
        {
            for (IEntityProperty property : properties)
            {
                propertiesToUpdate.add(property.getPropertyType().getCode());
            }
        }
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
    public final DatabaseInstancePE getHomeDatabaseInstance()
    {
        return daoFactory.getHomeDatabaseInstance();
    }

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
    public final IDatabaseInstanceDAO getDatabaseInstanceDAO()
    {
        return daoFactory.getDatabaseInstanceDAO();
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
    public Connection getConnection()
    {
        return daoFactory.getConnection();
    }

    @Override
    public IMetaprojectDAO getMetaprojectDAO()
    {
        return daoFactory.getMetaprojectDAO();
    }
}
