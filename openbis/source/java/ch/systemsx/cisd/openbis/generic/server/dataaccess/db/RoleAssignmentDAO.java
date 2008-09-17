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

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * <i>Data Access Object</i> implementation for {@link RoleAssignmentPE}.
 * 
 * @author Christian Ribeaud
 */
public final class RoleAssignmentDAO extends AbstractDAO implements IRoleAssignmentDAO
{
    public final static Class<RoleAssignmentPE> ENTITY_CLASS = RoleAssignmentPE.class;

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}.
     */
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RoleAssignmentDAO.class);

    RoleAssignmentDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance);
    }

    //
    // IRoleAssignmentDAO
    //

    public final List<RoleAssignmentPE> listRoleAssignments()
    {
        final List<RoleAssignmentPE> list = cast(getHibernateTemplate().loadAll(ENTITY_CLASS));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("listRoleAssignments(): " + list.size()
                    + " role assignment(s) have been found.");
        }
        return list;
    }

    public final List<RoleAssignmentPE> listRoleAssignmentsByPerson(final PersonPE person)
    {
        final Criterion granteeEq = Restrictions.eq("person", person);
        final DetachedCriteria criteria = DetachedCriteria.forClass(ENTITY_CLASS);
        criteria.add(granteeEq);
        final List<RoleAssignmentPE> list = cast(getHibernateTemplate().findByCriteria(criteria));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug("listRoleAssignmentsByPersonId(" + person + "): " + list.size()
                    + " role assignment(s) have been found.");
        }
        return list;
    }

    public final void createRoleAssignment(final RoleAssignmentPE roleAssignment)
    {
        assert roleAssignment != null : "Role assignment unspecified";
        validatePE(roleAssignment);

        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.save(roleAssignment);
        hibernateTemplate.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("ADD: role assignment '%s'.", roleAssignment));
        }
    }

    public final void deleteRoleAssignment(final RoleAssignmentPE roleAssignment)
    {
        getHibernateTemplate().delete(roleAssignment);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("DELETE: role assignment '%s'.", roleAssignment));
        }
    }

}
