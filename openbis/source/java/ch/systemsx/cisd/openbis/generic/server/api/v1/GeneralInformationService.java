/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.server.api.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.spring.IInvocationLoggerContext;
import ch.systemsx.cisd.openbis.generic.server.AbstractServer;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDatabaseInstanceDAO;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.IGeneralInformationService;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Role;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.SpaceWithProjectsAndRoleAssignments;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.dto.AuthorizationGroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatabaseInstancePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.GroupPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RoleAssignmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.dto.SessionContextDTO;

/**
 * @author Franz-Josef Elmer
 */
@Component(ResourceNames.GENERAL_INFORMATION_SERVICE_SERVER)
public class GeneralInformationService extends AbstractServer<IGeneralInformationService> implements
        IGeneralInformationService
{
    // Default constructor needed by Spring
    public GeneralInformationService()
    {
    }

    GeneralInformationService(ISessionManager<Session> sessionManager, IDAOFactory daoFactory)
    {
        super(sessionManager, daoFactory);
    }

    public IGeneralInformationService createLogger(IInvocationLoggerContext context)
    {
        return new GeneralInformationServiceLogger(sessionManager, context);
    }

    public String tryToAuthenticateForAllServices(String userID, String userPassword)
    {
        SessionContextDTO session = tryToAuthenticate(userID, userPassword);
        return session == null ? null : session.getSessionToken();
    }

    public Map<String, Set<Role>> listNamedRoleSets(String sessionToken)
    {
        checkSession(sessionToken);

        Map<String, Set<Role>> namedRoleSets = new LinkedHashMap<String, Set<Role>>();
        RoleWithHierarchy[] values = RoleWithHierarchy.values();
        for (RoleWithHierarchy roleSet : values)
        {
            Set<RoleWithHierarchy> roles = roleSet.getRoles();
            Set<Role> translatedRoles = new HashSet<Role>();
            for (RoleWithHierarchy role : roles)
            {
                translatedRoles.add(Translator.translate(role));
            }
            namedRoleSets.put(roleSet.name(), translatedRoles);
        }
        return namedRoleSets;
    }

    public List<SpaceWithProjectsAndRoleAssignments> listSpacesWithProjectsAndRoleAssignments(
            String sessionToken, String databaseInstanceCodeOrNull)
    {
        checkSession(sessionToken);

        Map<String, List<RoleAssignmentPE>> roleAssignmentsPerSpace = getRoleAssignmentsPerSpace();
        List<RoleAssignmentPE> instanceRoleAssignments = roleAssignmentsPerSpace.get(null);
        List<GroupPE> spaces = listSpaces(databaseInstanceCodeOrNull);
        List<SpaceWithProjectsAndRoleAssignments> result =
                new ArrayList<SpaceWithProjectsAndRoleAssignments>();
        for (GroupPE space : spaces)
        {
            SpaceWithProjectsAndRoleAssignments fullSpace =
                    new SpaceWithProjectsAndRoleAssignments(space.getCode());
            addProjectsTo(fullSpace, space);
            addRoles(fullSpace, instanceRoleAssignments);
            List<RoleAssignmentPE> list = roleAssignmentsPerSpace.get(space.getCode());
            if (list != null)
            {
                addRoles(fullSpace, list);
            }
            result.add(fullSpace);
        }
        return result;
    }

    public int getMajorVersion()
    {
        return 1;
    }

    public int getMinorVersion()
    {
        return 0;
    }

    private Map<String, List<RoleAssignmentPE>> getRoleAssignmentsPerSpace()
    {
        List<RoleAssignmentPE> roleAssignments =
                getDAOFactory().getRoleAssignmentDAO().listRoleAssignments();
        Map<String, List<RoleAssignmentPE>> roleAssignmentsPerSpace =
                new HashMap<String, List<RoleAssignmentPE>>();
        for (RoleAssignmentPE roleAssignment : roleAssignments)
        {
            GroupPE space = roleAssignment.getGroup();
            String spaceCode = space == null ? null : space.getCode();
            List<RoleAssignmentPE> list = roleAssignmentsPerSpace.get(spaceCode);
            if (list == null)
            {
                list = new ArrayList<RoleAssignmentPE>();
                roleAssignmentsPerSpace.put(spaceCode, list);
            }
            list.add(roleAssignment);
        }
        return roleAssignmentsPerSpace;
    }

    private List<GroupPE> listSpaces(String databaseInstanceCodeOrNull)
    {
        IDAOFactory daoFactory = getDAOFactory();
        DatabaseInstancePE databaseInstance = daoFactory.getHomeDatabaseInstance();
        if (databaseInstanceCodeOrNull != null)
        {
            IDatabaseInstanceDAO databaseInstanceDAO = daoFactory.getDatabaseInstanceDAO();
            databaseInstance =
                    databaseInstanceDAO.tryFindDatabaseInstanceByCode(databaseInstanceCodeOrNull);
        }
        return daoFactory.getGroupDAO().listGroups(databaseInstance);
    }

    private void addProjectsTo(SpaceWithProjectsAndRoleAssignments fullSpace, GroupPE space)
    {
        List<ProjectPE> projects = getDAOFactory().getProjectDAO().listProjects(space);
        for (ProjectPE project : projects)
        {
            fullSpace.add(new Project(fullSpace.getCode(), project.getCode()));
        }
    }

    private void addRoles(SpaceWithProjectsAndRoleAssignments fullSpace, List<RoleAssignmentPE> list)
    {
        for (RoleAssignmentPE roleAssignment : list)
        {
            Role role =
                    Translator.translate(roleAssignment.getRole(),
                            roleAssignment.getGroup() != null);
            Set<PersonPE> persons;
            AuthorizationGroupPE authorizationGroup = roleAssignment.getAuthorizationGroup();
            if (authorizationGroup != null)
            {
                persons = authorizationGroup.getPersons();
            } else
            {
                persons = Collections.singleton(roleAssignment.getPerson());
            }
            for (PersonPE person : persons)
            {
                fullSpace.add(person.getUserId(), role);
            }
        }
    }
}
