/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.authentication.ISessionManager;
import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.IRelationshipService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;

/**
 * @author anttil
 */
// @Test(groups = "system test")
public class RelationshipServiceAuthorizationTest extends BaseTest
{

    @DataProvider(name = "rolesAllowedToAssignExperimentToProject")
    public static RoleWithHierarchy[][] rolesAllowedToAssignExperimentToProject()
    {
        return allRoleCombinationsStrongerOrEqualThanAnyOf(RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER);
    }

    @Test(dataProvider = "rolesAllowedToAssignExperimentToProject", enabled = false)
    public void assigningExperimentToProjectIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignExperimentToProject(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @DataProvider(name = "rolesNotAllowedToAssignExperimentToProject")
    public static RoleWithHierarchy[][] rolesNotAllowedToAssignExperimentToProject()
    {
        return allRoleCombinationsWeakerThanAllOf(RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignExperimentToProject", expectedExceptions =
        { AuthorizationFailureException.class }, enabled = false)
    public void assigningExperimentToProjectIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignExperimentToProject(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @DataProvider(name = "rolesAllowedToAssignProjectToSpace")
    public static RoleWithHierarchy[][] rolesAllowedToAssignProjectToSpace()
    {
        return allRoleCombinationsStrongerOrEqualThanAnyOf(RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER);
    }

    @Test(dataProvider = "rolesAllowedToAssignProjectToSpace", enabled = false)
    public void assigningProjectToSpaceIsAuthorizedFor(
            RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignProjectToSpace(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @DataProvider(name = "rolesNotAllowedToAssignProjectToSpace")
    public static RoleWithHierarchy[][] rolesNotAllowedToAssignProjectToSpace()
    {
        return allRoleCombinationsWeakerThanAllOf(RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignProjectToSpace", expectedExceptions =
        { AuthorizationFailureException.class }, enabled = false)
    public void assignProjectToSpaceIsNotAuthorizedFor(
            RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignProjectToSpace(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @DataProvider(name = "rolesAllowedToAssignSampleToExperiment")
    public static RoleWithHierarchy[][] rolesAllowedToAssignSampleToExperiment()
    {
        return allRoleCombinationsStrongerOrEqualThanAnyOf(RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER);
    }

    @Test(dataProvider = "rolesAllowedToAssignSampleToExperiment", enabled = false)
    public void assigningSampleToExperimentIsAuthorizedFor(
            RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignSampleToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @DataProvider(name = "rolesNotAllowedToAssignSampleToExperiment")
    public static RoleWithHierarchy[][] rolesNotAllowedToAssignSampleToExperiment()
    {
        return allRoleCombinationsWeakerThanAllOf(RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToExperiment", expectedExceptions =
        { AuthorizationFailureException.class }, enabled = false)
    public void assignSampleToExperimentIsNotAuthorizedFor(
            RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignSampleToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @DataProvider(name = "rolesAllowedToUnassignSampleFromExperiment")
    public static RoleWithHierarchy[][] rolesAllowedToUnassignSampleFromExperiment()
    {
        Set<RoleWithHierarchy[]> roles = new HashSet<RoleWithHierarchy[]>();
        for (RoleWithHierarchy[] roleList : allRoleCombinationsStrongerOrEqualThanAnyOf(
                RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_ADMIN))
        {
            roles.add(new RoleWithHierarchy[]
                { roleList[0], roleList[2] });
        }
        return roles.toArray(new RoleWithHierarchy[0][]);
    }

    @Test(dataProvider = "rolesAllowedToUnassignSampleFromExperiment", enabled = false)
    public void unassigningSampleFromExperimentIsAuthorizedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole)
    {
        unassignSampleFromExperiment(spaceRole, instanceRole);
    }

    private void assignExperimentToProject(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        Space sourceSpace = create(aSpace());
        Space destinationSpace = create(aSpace());
        Project sourceProject = create(aProject().inSpace(sourceSpace));
        Project destinationProject = create(aProject().inSpace(destinationSpace));
        Experiment experiment = create(anExperiment().inProject(sourceProject));

        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        relationshipService.assignExperimentToProject(sessionManager.getSession(session),
                id(experiment), id(destinationProject));
    }

    private void assignProjectToSpace(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        Space sourceSpace = create(aSpace());
        Space destinationSpace = create(aSpace());
        Project project = create(aProject().inSpace(sourceSpace));
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        relationshipService.assignProjectToSpace(sessionManager.getSession(session), id(project),
                id(destinationSpace));
    }

    private void assignSampleToExperiment(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        Space sourceSpace = create(aSpace());
        Space destinationSpace = create(aSpace());
        Project sourceProject = create(aProject().inSpace(sourceSpace));
        Project destinationProject = create(aProject().inSpace(destinationSpace));
        Experiment sourceExperiment = create(anExperiment().inProject(sourceProject));
        Experiment destinationExperiment = create(anExperiment().inProject(destinationProject));
        Sample sample = create(aSample().inExperiment(sourceExperiment));

        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        relationshipService.assignSampleToExperiment(sessionManager.getSession(session),
                id(sample),
                id(destinationExperiment));
    }

    private void unassignSampleFromExperiment(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole)
    {
        Space space = create(aSpace());
        Project project = create(aProject().inSpace(space));
        Experiment experiment = create(anExperiment().inProject(project));
        Sample sample = create(aSample().inExperiment(experiment));

        String session =
                create(aSession().withSpaceRole(spaceRole, space).withInstanceRole(instanceRole));

        relationshipService.unassignSampleFromExperiment(sessionManager.getSession(session),
                id(sample));
    }

    private IRelationshipService relationshipService;

    @Autowired
    public void setRelationshipService(final IRelationshipService relationshipService)
    {
        this.relationshipService = relationshipService;
    }

    ISessionManager<Session> sessionManager;

    @Autowired
    public void setSessionManager(final ISessionManager<Session> sessionManager)
    {
        this.sessionManager = sessionManager;
    }

    public static RoleWithHierarchy[][] allRoleCombinationsStrongerOrEqualThanAnyOf(
            RoleWithHierarchy... rolesAllowed)
    {
        return negate(allRoleCombinationsWeakerThanAllOf(rolesAllowed));
    }

    public static RoleWithHierarchy[][] negate(RoleWithHierarchy[][] original)
    {

        List<RoleWithHierarchy[]> negation = new ArrayList<RoleWithHierarchy[]>();

        main: for (RoleWithHierarchy[] element : allRoleCombinations)
        {
            for (RoleWithHierarchy[] o : original)
            {
                if (element.equals(o))
                {
                    continue main;
                }
            }
            negation.add(element);
        }

        return negation.toArray(new RoleWithHierarchy[0][]);
    }

    public static RoleWithHierarchy[][] allRoleCombinationsWeakerThanAllOf(
            RoleWithHierarchy... rolesAllowed)
    {
        List<RoleWithHierarchy[]> accepted = new ArrayList<RoleWithHierarchy[]>();
        for (RoleWithHierarchy[] roleSet : allRoleCombinations)
        {
            RoleWithHierarchy sourceRole = roleSet[0];
            RoleWithHierarchy destinationRole = roleSet[1];
            RoleWithHierarchy instanceRole = roleSet[2];

            if (belowAll(instanceRole, rolesAllowed)
                    && (belowAll(destinationRole, rolesAllowed) || belowAll(sourceRole,
                            rolesAllowed)))
            {
                accepted.add(roleSet);
            }
        }
        return accepted.toArray(new RoleWithHierarchy[0][]);
    }

    public static boolean belowAll(RoleWithHierarchy role, RoleWithHierarchy[] limits)
    {

        for (RoleWithHierarchy limit : limits)
        {
            if (role == null)
            {
                continue;
            }
            if (role.equals(limit) || limit.getRoles().contains(role))
            {
                return false;
            }
        }

        return true;
    }

    public static RoleWithHierarchy[] spaceRoles =
        { RoleWithHierarchy.SPACE_ADMIN, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_OBSERVER, RoleWithHierarchy.SPACE_POWER_USER,
                RoleWithHierarchy.SPACE_USER, null };

    public static RoleWithHierarchy[] instanceRoles =
        { RoleWithHierarchy.INSTANCE_ADMIN, RoleWithHierarchy.INSTANCE_ETL_SERVER,
                RoleWithHierarchy.INSTANCE_OBSERVER, null };

    public static RoleWithHierarchy[][] allRoleCombinations;

    {
        List<RoleWithHierarchy[]> combinations = new ArrayList<RoleWithHierarchy[]>();
        for (RoleWithHierarchy sourceRole : spaceRoles)
        {
            for (RoleWithHierarchy destinationRole : spaceRoles)
            {
                for (RoleWithHierarchy instanceRole : instanceRoles)
                {
                    if (sourceRole == null && destinationRole == null && instanceRole == null)
                    {
                        continue;
                    }
                    combinations.add(new RoleWithHierarchy[]
                        { sourceRole, destinationRole, instanceRole });
                }
            }
        }
        allRoleCombinations = combinations.toArray(new RoleWithHierarchy[0][]);
    }
}
