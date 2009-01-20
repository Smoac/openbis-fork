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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IEntityTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentAttachmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExperimentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IHibernateSearchDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProcedureDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProcedureTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IProjectDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPropertyTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IVocabularyDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * {@link IDAOFactory} implementation working with {@link DatabaseConfigurationContext} and
 * {@link SessionFactory}.
 * 
 * @author Franz-Josef Elmer
 */
public final class DAOFactory extends AuthorizationDAOFactory implements IDAOFactory
{
    private final ISampleDAO sampleDAO;

    private final ISampleTypeDAO sampleTypeDAO;

    private final IExternalDataDAO externalDataDAO;

    private final IHibernateSearchDAO hibernateSearchDAO;

    private final IPropertyTypeDAO propertyTypeDAO;

    private final Map<EntityKind, IEntityTypeDAO> entityTypeDAOs =
            new HashMap<EntityKind, IEntityTypeDAO>();

    private final Map<EntityKind, IEntityPropertyTypeDAO> entityPropertyTypeDAOs =
            new HashMap<EntityKind, IEntityPropertyTypeDAO>();

    private final ExperimentDAO experimentDAO;

    private final IProjectDAO projectDAO;

    private final IVocabularyDAO vocabularyDAO;

    private IProcedureTypeDAO procedureTypeDAO;

    private IProcedureDAO procedureDAO;

    private IExperimentAttachmentDAO experimentAttachmentDAO;

    public DAOFactory(final DatabaseConfigurationContext context,
            final SessionFactory sessionFactory)
    {
        super(context, sessionFactory);
        final DatabaseInstancePE databaseInstance = getHomeDatabaseInstance();
        sampleDAO = new SampleDAO(sessionFactory, databaseInstance);
        sampleTypeDAO = new SampleTypeDAO(sessionFactory, databaseInstance);
        externalDataDAO = new ExternalDataDAO(sessionFactory, databaseInstance);
        hibernateSearchDAO = new HibernateSearchDAO(sessionFactory);
        propertyTypeDAO = new PropertyTypeDAO(sessionFactory, databaseInstance);
        experimentDAO = new ExperimentDAO(sessionFactory, databaseInstance);
        projectDAO = new ProjectDAO(sessionFactory, databaseInstance);
        vocabularyDAO = new VocabularyDAO(sessionFactory, databaseInstance);
        procedureTypeDAO = new ProcedureTypeDAO(sessionFactory, databaseInstance);
        procedureDAO = new ProcedureDAO(sessionFactory, databaseInstance);
        experimentAttachmentDAO = new ExperimentAttachmentDAO(sessionFactory, databaseInstance);
        final EntityKind[] entityKinds = EntityKind.values();
        for (final EntityKind entityKind : entityKinds)
        {
            final EntityTypeDAO dao =
                    new EntityTypeDAO(entityKind, sessionFactory, databaseInstance);
            entityTypeDAOs.put(entityKind, dao);
            entityPropertyTypeDAOs.put(entityKind, new EntityPropertyTypeDAO(entityKind,
                    sessionFactory, databaseInstance));
        }
    }

    //
    // IDAOFactory
    //

    public final ISampleDAO getSampleDAO()
    {
        return sampleDAO;
    }

    public final ISampleTypeDAO getSampleTypeDAO()
    {
        return sampleTypeDAO;
    }

    public final IExternalDataDAO getExternalDataDAO()
    {
        return externalDataDAO;
    }

    public final IHibernateSearchDAO getHibernateSearchDAO()
    {
        return hibernateSearchDAO;
    }

    public IEntityPropertyTypeDAO getEntityPropertyTypeDAO(final EntityKind entityKind)
    {
        return entityPropertyTypeDAOs.get(entityKind);
    }

    public IEntityTypeDAO getEntityTypeDAO(final EntityKind entityKind)
    {
        return entityTypeDAOs.get(entityKind);
    }

    public IPropertyTypeDAO getPropertyTypeDAO()
    {
        return propertyTypeDAO;
    }

    public IExperimentDAO getExperimentDAO()
    {
        return experimentDAO;
    }

    public final IProjectDAO getProjectDAO()
    {
        return projectDAO;
    }

    public final IVocabularyDAO getVocabularyDAO()
    {
        return vocabularyDAO;
    }

    public final IProcedureTypeDAO getProcedureTypeDAO()
    {
        return procedureTypeDAO;
    }

    public final IProcedureDAO getProcedureDAO()
    {
        return procedureDAO;
    }

    public final IExperimentAttachmentDAO getExperimentAttachmentDAO()
    {
        return experimentAttachmentDAO;
    }
}
