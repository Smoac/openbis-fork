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

import org.springframework.dao.DataAccessException;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IGroupDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleDAO;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.ISampleTypeDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;

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

    protected final Long findRegistratorID()
    {
        return findRegistrator().getId();
    }

    protected final PersonPE findRegistrator()
    {
        PersonPE registrator = session.tryGetPerson();
        assert registrator != null : "Missing person instance in session object.";
        return registrator;
    }

    protected final static void throwException(final DataAccessException exception,
            final String subject) throws UserFailureException
    {
        DataAccessExceptionTranslator.throwException(exception, subject);
    }

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

    public final IDatabaseInstanceDAO getDatabaseInstancesDAO()
    {
        return daoFactory.getDatabaseInstancesDAO();
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
}
