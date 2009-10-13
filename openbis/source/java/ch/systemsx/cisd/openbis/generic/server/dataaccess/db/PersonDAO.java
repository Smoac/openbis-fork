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

package ch.systemsx.cisd.openbis.generic.server.dataaccess.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IPersonDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * Implementation of {@link IPersonDAO} for databases.
 * 
 * @author Franz-Josef Elmer
 */
public final class PersonDAO extends AbstractGenericEntityDAO<PersonPE> implements IPersonDAO
{
    private static final Class<PersonPE> ENTITY_CLASS = PersonPE.class;

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}. </p>
     */
    public static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, PersonDAO.class);

    PersonDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, ENTITY_CLASS);
    }

    //
    // IPersonDAO
    //

    public final void createPerson(final PersonPE person) throws DataAccessException
    {
        assert person != null : "Given person can not be null.";
        if (person.getDatabaseInstance() == null)
        {
            person.setDatabaseInstance(getDatabaseInstance());
        }
        person.setEmail(StringUtils.trim(person.getEmail()));
        validatePE(person);

        final HibernateTemplate template = getHibernateTemplate();
        template.save(person);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: person '%s'.", person));
        }
    }

    public final void updatePerson(final PersonPE person) throws DataAccessException
    {
        assert person != null : "Given person can not be null.";
        validatePE(person);

        final HibernateTemplate template = getHibernateTemplate();
        template.update(person);
        template.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("UPDATE: person '%s'.", person));
        }
    }

    public final PersonPE getPerson(final long id) throws DataAccessException
    {
        final PersonPE person = (PersonPE) getHibernateTemplate().load(ENTITY_CLASS, id);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("getPerson(" + id + "): '" + person + "'.");
        }
        return person;
    }

    public final PersonPE tryFindPersonByUserId(final String userId) throws DataAccessException
    {
        assert userId != null : "Unspecified user id";

        final List<PersonPE> persons =
                cast(getHibernateTemplate().find(
                        String.format("from %s p where p.userId = ? "
                                + "and p.databaseInstance = ?", TABLE_NAME),
                        toArray(userId, getDatabaseInstance())));
        final PersonPE person = tryFindEntity(persons, "persons", userId);
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(%s): '%s'.", MethodUtils.getCurrentMethod()
                    .getName(), userId, person));
        }
        return person;
    }

    public final List<PersonPE> listPersons() throws DataAccessException
    {
        final List<PersonPE> list =
                cast(getHibernateTemplate().find(
                        String.format("from %s p where p.databaseInstance = ?", TABLE_NAME),
                        toArray(getDatabaseInstance())));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d person(s) have been found.", MethodUtils
                    .getCurrentMethod().getName(), list.size()));
        }
        return list;
    }

    public final List<PersonPE> listByCodes(Collection<String> userIds) throws DataAccessException
    {
        if (userIds.size() == 0)
            return new ArrayList<PersonPE>();
        final Criteria criteria = getSession().createCriteria(PersonPE.class);
        criteria.add(Restrictions.in("userId", userIds));
        return cast(criteria.list());
    }
}
