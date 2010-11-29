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
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.jdbc.support.JdbcAccessor;
import org.springframework.orm.hibernate3.HibernateTemplate;

import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IRoleAssignmentDAO;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Grantee.GranteeType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * <i>Data Access Object</i> implementation for {@link RoleAssignmentPE}.
 * 
 * @author Christian Ribeaud
 */
public final class RoleAssignmentDAO extends AbstractGenericEntityDAO<RoleAssignmentPE> implements
        IRoleAssignmentDAO
{
    private static final String AUTHORIZATION_GROUP_INTERNAL_CODE =
            "authorizationGroupInternal.code";

    private static final String PERSON_INTERNAL_USER_ID = "personInternal.userId";

    public static final Class<RoleAssignmentPE> ENTITY_CLASS = RoleAssignmentPE.class;

    private static final String TABLE_NAME = ENTITY_CLASS.getSimpleName();

    /**
     * This logger does not output any SQL statement. If you want to do so, you had better set an
     * appropriate debugging level for class {@link JdbcAccessor}.
     */
    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, RoleAssignmentDAO.class);

    RoleAssignmentDAO(final SessionFactory sessionFactory, final DatabaseInstancePE databaseInstance)
    {
        super(sessionFactory, databaseInstance, ENTITY_CLASS);
    }

    //
    // IRoleAssignmentDAO
    //

    public final List<RoleAssignmentPE> listRoleAssignments()
    {
        // returns roles connected directly or indirectly (through space) to current db instance
        final List<RoleAssignmentPE> list =
                cast(getHibernateTemplate().find(
                        String.format("select r from %s r left join r.databaseInstance ri"
                                + " left join r.space g left join g.databaseInstance gi"
                                + " where ri = ? or (ri is null and gi = ?)", TABLE_NAME),
                        toArray(getDatabaseInstance(), getDatabaseInstance())));
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("%s(): %d role assignment(s) have been found.",
                    MethodUtils.getCurrentMethod().getName(), list.size()));
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
            operationLog.debug(String.format("%s(%s): %d role assignment(s) have been found.",
                    MethodUtils.getCurrentMethod().getName(), person, list.size()));
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

        // Remove the role assignment from the grantee before delete it from the hibernate
        // session. Or you will get an InvalidDataAccessApiUsageException caused by an
        // ObjectDeletedException.
        PersonPE person = roleAssignment.getPerson();
        if (person != null)
        {
            person.removeRoleAssigment(roleAssignment);
        } else
        {
            roleAssignment.getAuthorizationGroup().removeRoleAssigment(roleAssignment);
        }
        final HibernateTemplate hibernateTemplate = getHibernateTemplate();
        hibernateTemplate.delete(roleAssignment);
        hibernateTemplate.flush();
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("DELETE: role assignment '%s'.", roleAssignment));
        }
    }

    private final static String getGranteeHqlParameter(Grantee.GranteeType type)
    {
        if (type.equals(GranteeType.AUTHORIZATION_GROUP))
        {
            return AUTHORIZATION_GROUP_INTERNAL_CODE;
        } else
        {
            return PERSON_INTERNAL_USER_ID;
        }
    }

    public final RoleAssignmentPE tryFindSpaceRoleAssignment(final RoleCode role,
            final String space, final Grantee grantee)
    {
        assert role != null : "Unspecified role.";
        assert grantee != null : "Unspecified grantee.";

        final List<RoleAssignmentPE> roles =
                cast(getHibernateTemplate().find(
                        String.format("from %s r where r."
                                + getGranteeHqlParameter(grantee.getType())
                                + " = ? and space.code = ? " + "and r.role = ?", TABLE_NAME),
                        toArray(grantee.getCode(), space, role)));
        final RoleAssignmentPE roleAssignment =
                tryFindEntity(roles, "role_assignments", role, space, grantee);
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("FIND: space role assignment '%s'.", roleAssignment));
        }
        return roleAssignment;

    }

    public final RoleAssignmentPE tryFindInstanceRoleAssignment(final RoleCode role,
            final Grantee grantee)
    {
        assert role != null : "Unspecified role.";
        assert grantee != null : "Unspecified grantee.";

        final List<RoleAssignmentPE> roles =
                cast(getHibernateTemplate().find(
                        String.format("from %s r where r."
                                + getGranteeHqlParameter(grantee.getType()) + " = ? "
                                + "and r.role = ? and r.databaseInstance = ?", TABLE_NAME),
                        toArray(grantee.getCode(), role, getDatabaseInstance())));
        final RoleAssignmentPE roleAssignment =
                tryFindEntity(roles, "role_assignments", role, grantee);
        if (operationLog.isInfoEnabled())
        {
            operationLog
                    .info(String.format("FIND: instance role assignment '%s'.", roleAssignment));
        }
        return roleAssignment;

    }

    public List<RoleAssignmentPE> listRoleAssignmentsByAuthorizationGroup(
            AuthorizationGroupPE authGroup)
    {
        final Criteria criteria = getSession().createCriteria(RoleAssignmentPE.class);
        criteria.add(Restrictions.eq("authorizationGroupInternal", authGroup));
        List<RoleAssignmentPE> result = cast(criteria.list());
        if (operationLog.isInfoEnabled())
        {
            operationLog.info(String.format("LIST: role assignments for authorization group '%s'.",
                    authGroup));
        }
        return result;
    }

}
