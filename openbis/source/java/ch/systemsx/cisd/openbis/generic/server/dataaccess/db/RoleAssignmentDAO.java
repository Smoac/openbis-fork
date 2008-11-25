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
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleCode;

/**
 * <i>Data Access Object</i> implementation for {@link RoleAssignmentPE}.
 * 
 * @author Christian Ribeaud
 */
public final class RoleAssignmentDAO extends AbstractDAO implements IRoleAssignmentDAO
{
    public final static Class<RoleAssignmentPE> ENTITY_CLASS = RoleAssignmentPE.class;

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

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
        assert person != null : "Unspecified person.";

        final DetachedCriteria criteria = DetachedCriteria.forClass(ENTITY_CLASS);
        criteria.add(Restrictions.eq("personInternal", person));
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
        assert roleAssignment != null : "Role assignment unspecified";

        // Remove the role assignment from the person grantee before delete it from the hibernate
        // session. Or you will get an InvalidDataAccessApiUsageException caused by an
        // ObjectDeletedException.
        roleAssignment.getPerson().removeRoleAssigment(roleAssignment);
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.delete(roleAssignment);
        hibernateTemplate.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("DELETE: role assignment '%s'.", roleAssignment));
        }
    }

    public final RoleAssignmentPE tryFindGroupRoleAssignment(final RoleCode role,
            final String group, final String person)
    {
        final List<RoleAssignmentPE> roles =
                cast(getHibernateTemplate().find(
                        String.format(
                                "from %s r where r.personInternal.userId = ? and group.code = ? "
                                        + "and r.role = ?", TABLE_NAME),
                        toArray(person, group, role)));
        final RoleAssignmentPE roleAssignment =
                tryFindEntity(roles, "role_assignments", role, group, person);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("FIND: group role assignment '%s'.", roleAssignment));
        }
        return roleAssignment;

    }

    public final RoleAssignmentPE tryFindInstanceRoleAssignment(final RoleCode role,
            final String person)
    {
        assert role != null : "Unspecified role.";
        assert person != null : "Unspecified person.";

        final List<RoleAssignmentPE> roles =
                cast(getHibernateTemplate().find(
                        String.format("from %s r where r.personInternal.userId = ? "
                                + "and r.role = ? and r.databaseInstance = ?", TABLE_NAME),
                        toArray(person, role, getDatabaseInstance())));
        final RoleAssignmentPE roleAssignment =
                tryFindEntity(roles, "role_assignments", role, person);
        if (operationLog.isInfoEnabled())
        {
            operationLog
                    .info(String.format("FIND: instance role assignment '%s'.", roleAssignment));
        }
        return roleAssignment;

    }

}
