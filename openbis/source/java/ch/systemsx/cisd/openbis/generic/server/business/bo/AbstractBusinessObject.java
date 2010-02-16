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

import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataSetTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDataStoreDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEventDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IFileFormatTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGridCustomColumnDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGridCustomFilterDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ILocatorTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IMaterialDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IQueryDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyTermDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.PersistencyResources;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.ICodeSequenceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.db.IPermIdDAO;
import ch.systemsx.cisd.openbis.generic.server.util.GroupIdentifierHelper;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.GroupIdentifier;
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

    AbstractBusinessObject(final IDAOFactory daoFactory, final Session session)
    {
        assert daoFactory != null : "Given DAO factory can not be null.";
        assert session != null : "Given session can not be null.";

        this.daoFactory = daoFactory;
        this.session = session;
    }

    public SessionFactory getSessionFactory()
    {
        return daoFactory.getSessionFactory();
    }

    protected final PersonPE findRegistrator()
    {
        final PersonPE registrator = session.tryGetPerson();
        assert registrator != null : "Missing person instance in session object.";
        return registrator;
    }

    protected void fillGroupIdentifier(final GroupIdentifier groupIdentifier)
    {
        if (org.apache.commons.lang.StringUtils.isBlank(groupIdentifier.getGroupCode()))
        {
            final GroupPE group =
                    GroupIdentifierHelper.tryGetGroup(groupIdentifier, findRegistrator(), this);
            checkNotNull(groupIdentifier, group);
            groupIdentifier.setDatabaseInstanceCode(group.getDatabaseInstance().getCode());
            groupIdentifier.setGroupCode(group.getCode());
        }
    }

    private static void checkNotNull(final GroupIdentifier groupIdentifier, final GroupPE group)
    {
        if (group == null)
        {
            throw new UserFailureException("Unknown group '" + groupIdentifier + "'.");
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
     * @return Generated code for given <var>entityKind</var>. The code has a prefix that depends on
     *         <var>entityKind</var> and a sufix witch is a unique number.
     */
    protected String createCode(EntityKind entityKind)
    {
        final long id = getCodeSequenceDAO().getNextCodeSequenceId();
        final String code = String.valueOf(entityKind.name().charAt(0)) + id;
        return code;
    }

    //
    // IDAOFactory
    //

    public final DatabaseInstancePE getHomeDatabaseInstance()
    {
        return daoFactory.getHomeDatabaseInstance();
    }

    public final IGroupDAO getGroupDAO()
    {
        return daoFactory.getGroupDAO();
    }

    public final IPersonDAO getPersonDAO()
    {
        return daoFactory.getPersonDAO();
    }

    public final IDatabaseInstanceDAO getDatabaseInstanceDAO()
    {
        return daoFactory.getDatabaseInstanceDAO();
    }

    public final IRoleAssignmentDAO getRoleAssignmentDAO()
    {
        return daoFactory.getRoleAssignmentDAO();
    }

    public final ISampleDAO getSampleDAO()
    {
        return daoFactory.getSampleDAO();
    }

    public final ISampleTypeDAO getSampleTypeDAO()
    {
        return daoFactory.getSampleTypeDAO();
    }

    public final IExternalDataDAO getExternalDataDAO()
    {
        return daoFactory.getExternalDataDAO();
    }

    public final IHibernateSearchDAO getHibernateSearchDAO()
    {
        return daoFactory.getHibernateSearchDAO();
    }

    public IPropertyTypeDAO getPropertyTypeDAO()
    {
        return daoFactory.getPropertyTypeDAO();
    }

    public IEntityTypeDAO getEntityTypeDAO(final EntityKind entityKind)
    {
        return daoFactory.getEntityTypeDAO(entityKind);
    }

    public IEntityPropertyTypeDAO getEntityPropertyTypeDAO(final EntityKind entityKind)
    {
        return daoFactory.getEntityPropertyTypeDAO(entityKind);
    }

    public IExperimentDAO getExperimentDAO()
    {
        return daoFactory.getExperimentDAO();
    }

    public IProjectDAO getProjectDAO()
    {
        return daoFactory.getProjectDAO();
    }

    public final IVocabularyDAO getVocabularyDAO()
    {
        return daoFactory.getVocabularyDAO();
    }

    public final IVocabularyTermDAO getVocabularyTermDAO()
    {
        return daoFactory.getVocabularyTermDAO();
    }

    public final IAttachmentDAO getAttachmentDAO()
    {
        return daoFactory.getAttachmentDAO();
    }

    public IDataSetTypeDAO getDataSetTypeDAO()
    {
        return daoFactory.getDataSetTypeDAO();
    }

    public IFileFormatTypeDAO getFileFormatTypeDAO()
    {
        return daoFactory.getFileFormatTypeDAO();
    }

    public ILocatorTypeDAO getLocatorTypeDAO()
    {
        return daoFactory.getLocatorTypeDAO();
    }

    public IMaterialDAO getMaterialDAO()
    {
        return daoFactory.getMaterialDAO();
    }

    public ICodeSequenceDAO getCodeSequenceDAO()
    {
        return daoFactory.getCodeSequenceDAO();
    }

    public IDataStoreDAO getDataStoreDAO()
    {
        return daoFactory.getDataStoreDAO();
    }

    public IPermIdDAO getPermIdDAO()
    {
        return daoFactory.getPermIdDAO();
    }

    public IEventDAO getEventDAO()
    {
        return daoFactory.getEventDAO();
    }

    public void disableSecondLevelCacheForSession()
    {
        daoFactory.disableSecondLevelCacheForSession();
    }

    public IAuthorizationGroupDAO getAuthorizationGroupDAO()
    {
        return daoFactory.getAuthorizationGroupDAO();
    }

    public PersistencyResources getPersistencyResources()
    {
        return daoFactory.getPersistencyResources();
    }

    public IGridCustomFilterDAO getGridCustomFilterDAO()
    {
        return daoFactory.getGridCustomFilterDAO();
    }

    public IGridCustomColumnDAO getGridCustomColumnDAO()
    {
        return daoFactory.getGridCustomColumnDAO();
    }

    public IQueryDAO getQueryDAO()
    {
        return daoFactory.getQueryDAO();
    }
}
