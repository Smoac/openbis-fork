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

package ch.systemsx.cisd.openbis.generic.server.authorization;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.StatusFlag;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.MethodUtils;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IAuthorizationDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.authorization.RoleWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.authorization.annotation.RolesAllowed;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.IAuthSession;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;

/**
 * A default <code>IAccessController</code> implementation.
 * 
 * @author Christian Ribeaud
 */
public final class DefaultAccessController implements IAccessController
{

    private static final Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, DefaultAccessController.class);

    @Private
    static final String MATCHING_ROLE_NOT_FOUND_TEMPLATE =
            "None of method roles '%s' could be found in roles of user '%s'.";

    @Private
    static final String USER_ROLE_ASSIGNMENTS_NOT_FOUND_TEMPLATE =
            "No role assignments could be found for user '%s'.";

    @Private
    static final String METHOD_ROLES_NOT_FOUND_TEMPLATE =
            "No roles have been found for method '%s'.";

    /**
     * Cache for the method roles as they are <code>static</code>.
     */
    private final Map<Method, Set<RoleWithHierarchy>> methodRolesCache = new HashMap<Method, Set<RoleWithHierarchy>>();

    public DefaultAccessController(final IAuthorizationDAOFactory daoFactory)
    {
        PredicateExecutor.setPredicateFactory(new PredicateFactory());
        PredicateExecutor.setDAOFactory(daoFactory);
    }

    public final static List<RoleWithIdentifier> getUserRoles(final PersonPE person)
    {
        Set<RoleAssignmentPE> roleAssignments = person.getAllPersonRoles();
        return extractRolesWithIdentifiers(roleAssignments);
    }

    private static List<RoleWithIdentifier> extractRolesWithIdentifiers(
            Set<RoleAssignmentPE> roleAssignments)
    {
        final List<RoleWithIdentifier> roles = new ArrayList<RoleWithIdentifier>();
        for (final RoleAssignmentPE roleAssignment : roleAssignments)
        {
            roles.add(RoleWithIdentifier.createRole(roleAssignment));
        }
        return roles;
    }

    private final static void logTimeTaken(final StopWatch stopWatch, final Method method)
    {
        stopWatch.stop();
        if (operationLog.isDebugEnabled())
        {
            operationLog.debug(String.format("Controlling access to method '%s' took %s",
                    MethodUtils.describeMethod(method), stopWatch));
        }
    }

    private Set<RoleWithHierarchy> getMethodRoles(final Method method)
    {
        synchronized (methodRolesCache)
        {
            Set<RoleWithHierarchy> roles = methodRolesCache.get(method);
            if (roles == null)
            {
                roles = new LinkedHashSet<RoleWithHierarchy>();
                final RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);
                if (rolesAllowed != null)
                {
                    for (RoleWithHierarchy role : rolesAllowed.value())
                    {
                        roles.addAll(role.getRoles());
                    }
                }
                methodRolesCache.put(method, roles);
            }
            return roles;
        }
    }

    public final Status isAuthorized(final IAuthSession session, final Method method,
            final Argument<?>[] arguments) throws UserFailureException
    {
        assert session != null : "Unspecified session";
        assert method != null : "Unspecified method";
        assert arguments != null : "Unspecified method arguments";
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try
        {
            final Set<RoleWithHierarchy> methodRoles = getMethodRoles(method);
            if (methodRoles.size() == 0)
            {
                // TODO 2008-08-07, Tomasz Pylak: why this is not a programming error? What a user
                // can do if a programmer does not put an authorization annotation for a method?
                final String msg =
                        String.format(METHOD_ROLES_NOT_FOUND_TEMPLATE, MethodUtils
                                .describeMethod(method));
                return Status.createError(msg);
            }
            PersonPE person = session.tryGetPerson();
            if (person == null || person.getAllPersonRoles().size() == 0)
            {
                final String msg =
                        String.format(USER_ROLE_ASSIGNMENTS_NOT_FOUND_TEMPLATE, session
                                .getUserName());
                return Status.createError(msg);
            }
            final List<RoleWithIdentifier> userRoles = getUserRoles(person);
            retainMatchingRoleWithIdentifiers(userRoles, methodRoles);

            if (userRoles.size() == 0)
            {
                final String msg =
                        String.format(MATCHING_ROLE_NOT_FOUND_TEMPLATE, methodRoles, session
                                .getUserName());
                return Status.createError(msg);
            }
            if (arguments.length > 0)
            {
                for (final Argument<?> argument : arguments)
                {
                    final Status status = PredicateExecutor.evaluate(person, userRoles, argument);
                    if (status.getFlag().equals(StatusFlag.OK) == false)
                    {
                        return status;
                    }
                }
            }
            return Status.OK;
        } finally
        {
            logTimeTaken(stopWatch, method);
        }
    }

    /**
     * Retains {@link RoleWithIdentifier}s with {@link RoleWithIdentifier#getRole()} included in the
     * set of {@link RoleWithHierarchy}s.
     */
    public static void retainMatchingRoleWithIdentifiers(final List<RoleWithIdentifier> userRoles,
            final Set<RoleWithHierarchy> methodRoles)
    {
        Iterator<RoleWithIdentifier> it = userRoles.iterator();
        while (it.hasNext())
        {
            RoleWithIdentifier roleWithIdentifier = it.next();
            if (methodRoles.contains(roleWithIdentifier.getRole()) == false)
            {
                it.remove();
            }
        }
    }
}
