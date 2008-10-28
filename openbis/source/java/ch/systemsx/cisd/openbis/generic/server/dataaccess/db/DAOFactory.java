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

import org.hibernate.SessionFactory;

import ch.systemsx.cisd.dbmigration.DatabaseConfigurationContext;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IExternalDataDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISamplePropertyDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;

/**
 * @author Franz-Josef Elmer
 */
public final class DAOFactory extends AuthorizationDAOFactory implements IDAOFactory
{
    private final ISampleDAO sampleDAO;

    private final ISampleTypeDAO sampleTypeDAO;

    private final ISamplePropertyDAO samplePropertyDAO;

    private final IExternalDataDAO externalDataDAO;

    public DAOFactory(final DatabaseConfigurationContext context,
            final SessionFactory sessionFactory)
    {
        super(context, sessionFactory);
        final DatabaseInstancePE databaseInstance = getHomeDatabaseInstance();
        sampleDAO = new SampleDAO(sessionFactory, databaseInstance);
        sampleTypeDAO = new SampleTypeDAO(sessionFactory, databaseInstance);
        samplePropertyDAO = new SamplePropertyDAO(sessionFactory, databaseInstance);
        externalDataDAO = new ExternalDataDAO(sessionFactory, databaseInstance);
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

    public final ISamplePropertyDAO getSamplePropertyDAO()
    {
        return samplePropertyDAO;
    }

    public final IExternalDataDAO getExternalDataDAO()
    {
        return externalDataDAO;
    }
}
