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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;

/**
 * @author anttil
 */
@ContextConfiguration(locations =
    { "classpath:stub_relationship_service.xml" }, inheritLocations = true)
public class RelationshipServiceAuthorizationTest extends BaseTest
{

    private Space sourceSpace;

    private Space destinationSpace;

    private Project sourceProject;

    private Project destinationProject;

    private Experiment sourceExperiment;

    private Experiment destinationExperiment;

    private Sample sourceSample;

    private Sample destinationSample;

    private Sample sharedSample;

    private Sample childSample;

    private Sample parentSample;

    private DataSet dataSet;

    @BeforeClass
    public void createFixture()
    {
        sourceSpace = create(aSpace());
        sourceProject = create(aProject().inSpace(sourceSpace));
        sourceExperiment = create(anExperiment().inProject(sourceProject));
        sourceSample = create(aSample().inExperiment(sourceExperiment));
        dataSet = create(aDataSet().inSample(sourceSample));

        destinationSpace = create(aSpace());
        destinationProject = create(aProject().inSpace(destinationSpace));
        destinationExperiment = create(anExperiment().inProject(destinationProject));
        destinationSample = create(aSample().inExperiment(destinationExperiment));

        sharedSample = create(aSample());

        parentSample = create(aSample().inSpace(sourceSpace));
        childSample = create(aSample().inSpace(destinationSpace).withParent(parentSample));
    }

    @Test(dataProvider = "rolesAllowedToAssignExperimentToProject")
    public void assigningExperimentToProjectIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignExperimentToProject(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignExperimentToProject", expectedExceptions =
        { AuthorizationFailureException.class })
    public void assigningExperimentToProjectIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignExperimentToProject(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignProjectToSpace")
    public void assigningProjectToSpaceIsAuthorizedFor(
            RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignProjectToSpace(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignProjectToSpace", expectedExceptions =
        { AuthorizationFailureException.class })
    public void assignProjectToSpaceIsNotAuthorizedFor(
            RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignProjectToSpace(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignSampleToExperiment")
    public void assigningSampleToExperimentIsAuthorizedFor(
            RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignSampleToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToExperiment", expectedExceptions =
        { AuthorizationFailureException.class })
    public void assignSampleToExperimentIsNotAuthorizedFor(
            RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignSampleToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToUnassignSampleFromExperiment")
    public void unassigningSampleFromExperimentIsAuthorizedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole)
    {
        unassignSampleFromExperiment(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToUnassignSampleFromExperiment", expectedExceptions =
        { AuthorizationFailureException.class })
    public void unassigningSampleFromExperimentIsNotAuthorizedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole)
    {
        unassignSampleFromExperiment(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToUnshareSample")
    public void unsharingSampleIsAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole)
    {
        unshareSample(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToUnshareSample", expectedExceptions =
        { AuthorizationFailureException.class })
    public void unsharingSampleIsNotAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole)
    {
        unshareSample(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignSampleToSpace")
    public void assigningSampleToSpaceIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignSampleToSpace(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToSpace", expectedExceptions =
        { AuthorizationFailureException.class })
    public void assigningSampleToSpaceIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignSampleToSpace(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToShareSample")
    public void sharingSampleIsAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole)
    {
        shareSample(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToShareSample", expectedExceptions =
        { AuthorizationFailureException.class })
    public void sharingSampleIsNotAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole)
    {
        shareSample(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignDataSetToExperiment")
    public void assigningDataSetToExperimentIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignDataSetToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignDataSetToExperiment", expectedExceptions =
        { AuthorizationFailureException.class })
    public void assigningDataSetToExperimentIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignDataSetToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignDataSetToSample")
    public void assigningDataSetToSampleIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignDataSetToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignDataSetToExperiment", expectedExceptions =
        { AuthorizationFailureException.class })
    public void assigningDataSetToSampleIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        assignDataSetToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAddParentToSample")
    public void addingParentToSampleIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        addParentToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAddParentToSample", expectedExceptions =
        { AuthorizationFailureException.class })
    public void addingParentToSampleIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        addParentToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToRemoveParentFromSample")
    public void removingParentFromSampleIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        removeParentFromSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToRemoveParentFromSample", expectedExceptions =
        { AuthorizationFailureException.class })
    public void removingParentFromSampleIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        removeParentFromSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    private void assignExperimentToProject(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        relationshipService.assignExperimentToProject(sessionManager.getSession(session),
                id(sourceExperiment), id(destinationProject));
    }

    private void assignProjectToSpace(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        relationshipService.assignProjectToSpace(sessionManager.getSession(session),
                id(sourceProject),
                id(destinationSpace));
    }

    private void assignSampleToExperiment(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        relationshipService.assignSampleToExperiment(sessionManager.getSession(session),
                id(sourceSample),
                id(destinationExperiment));
    }

    private void unassignSampleFromExperiment(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole)
    {
        String session =
                create(aSession().withSpaceRole(spaceRole, sourceSpace).withInstanceRole(
                        instanceRole));

        relationshipService.unassignSampleFromExperiment(sessionManager.getSession(session),
                id(sourceSample));
    }

    private void unshareSample(RoleWithHierarchy spaceRole, RoleWithHierarchy instanceRole)
    {
        String session =
                create(aSession().withSpaceRole(spaceRole, destinationSpace).withInstanceRole(
                        instanceRole));
        relationshipService
                .unshareSample(sessionManager.getSession(session), id(sharedSample),
                        id(destinationSpace));
    }

    private void assignSampleToSpace(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {

        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        relationshipService.assignSampleToSpace(sessionManager.getSession(session),
                id(sourceSample),
                id(destinationSpace));
    }

    private void shareSample(RoleWithHierarchy spaceRole, RoleWithHierarchy instanceRole)
    {
        String session =
                create(aSession().withSpaceRole(spaceRole, sourceSpace).withInstanceRole(
                        instanceRole));
        relationshipService.shareSample(sessionManager.getSession(session), id(sourceSample));
    }

    private void assignDataSetToExperiment(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        relationshipService.assignDataSetToExperiment(sessionManager.getSession(session),
                dataSet.getCode(),
                id(destinationExperiment));
    }

    private void assignDataSetToSample(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {

        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        relationshipService.assignDataSetToSample(sessionManager.getSession(session),
                dataSet.getCode(),
                id(destinationSample));
    }

    private void addParentToSample(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        relationshipService.addParentToSample(sessionManager.getSession(session), id(sourceSample),
                id(destinationSample));

    }

    private void removeParentFromSample(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole)
    {
        String session =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace).withSpaceRole(
                        destinationSpaceRole, destinationSpace).withInstanceRole(instanceRole));

        relationshipService.removeParentFromSample(sessionManager.getSession(session),
                id(childSample),
                id(parentSample));
    }

    @DataProvider(name = "rolesAllowedToAssignExperimentToProject")
    public static RoleWithHierarchy[][] rolesAllowedToAssignExperimentToProject()
    {
        return toNestedArray(acceptedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesNotAllowedToAssignExperimentToProject")
    public static RoleWithHierarchy[][] rolesNotAllowedToAssignExperimentToProject()
    {
        return toNestedArray(rejectedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesAllowedToAssignProjectToSpace")
    public static RoleWithHierarchy[][] rolesAllowedToAssignProjectToSpace()
    {
        return toNestedArray(acceptedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesNotAllowedToAssignProjectToSpace")
    public static RoleWithHierarchy[][] rolesNotAllowedToAssignProjectToSpace()
    {
        return toNestedArray(rejectedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesAllowedToAssignSampleToExperiment")
    public static RoleWithHierarchy[][] rolesAllowedToAssignSampleToExperiment()
    {
        return toNestedArray(acceptedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesNotAllowedToAssignSampleToExperiment")
    public static RoleWithHierarchy[][] rolesNotAllowedToAssignSampleToExperiment()
    {
        return toNestedArray(rejectedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesAllowedToUnassignSampleFromExperiment")
    public static RoleWithHierarchy[][] rolesAllowedToUnassignSampleFromExperiment()
    {
        return toNestedArray(acceptedRoles(1, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesNotAllowedToUnassignSampleFromExperiment")
    public static RoleWithHierarchy[][] rolesNotAllowedToUnassignSampleFromExperiment()
    {
        return toNestedArray(rejectedRoles(1, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesAllowedToUnshareSample")
    public static RoleWithHierarchy[][] rolesAllowedToUnshareSample()
    {
        return toNestedArray(acceptedRoles(1, RoleWithHierarchy.INSTANCE_ETL_SERVER,
                RoleWithHierarchy.INSTANCE_ADMIN));
    }

    @DataProvider(name = "rolesNotAllowedToUnshareSample")
    public static RoleWithHierarchy[][] rolesNotAllowedToUnshareSample()
    {
        return toNestedArray(rejectedRoles(1, RoleWithHierarchy.INSTANCE_ETL_SERVER,
                RoleWithHierarchy.INSTANCE_ADMIN));
    }

    @DataProvider(name = "rolesAllowedToAssignSampleToSpace")
    public static RoleWithHierarchy[][] rolesAllowedToAssignSampleToSpace()
    {
        return toNestedArray(acceptedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesNotAllowedToAssignSampleToSpace")
    public static RoleWithHierarchy[][] rolesNotAllowedToAssignSampleToSpace()
    {
        return toNestedArray(rejectedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesAllowedToShareSample")
    public static RoleWithHierarchy[][] rolesAllowedToShareSample()
    {
        return toNestedArray(acceptedRoles(1, RoleWithHierarchy.INSTANCE_ETL_SERVER,
                RoleWithHierarchy.INSTANCE_ADMIN));
    }

    @DataProvider(name = "rolesNotAllowedToShareSample")
    public static RoleWithHierarchy[][] rolesNotAllowedToShareSample()
    {
        return toNestedArray(rejectedRoles(1, RoleWithHierarchy.INSTANCE_ETL_SERVER,
                RoleWithHierarchy.INSTANCE_ADMIN));
    }

    @DataProvider(name = "rolesAllowedToAssignDataSetToExperiment")
    public static RoleWithHierarchy[][] rolesAllowedToAssignDataSetToExperiment()
    {
        return toNestedArray(acceptedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesNotAllowedToAssignDataSetToExperiment")
    public static RoleWithHierarchy[][] rolesNotAllowedToAssignDataSetToExperiment()
    {
        return toNestedArray(rejectedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesAllowedToAssignDataSetToSample")
    public static RoleWithHierarchy[][] rolesAllowedToAssignDataSetToSample()
    {
        return toNestedArray(acceptedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesNotAllowedToAssignDataSetToSample")
    public static RoleWithHierarchy[][] rolesNotAllowedToAssignDataSetToSample()
    {
        return toNestedArray(rejectedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesAllowedToAddParentToSample")
    public static RoleWithHierarchy[][] rolesAllowedToAddParentToSample()
    {
        return toNestedArray(acceptedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesNotAllowedToAddParentToSample")
    public static RoleWithHierarchy[][] rolesNotAllowedToAddParentToSample()
    {
        return toNestedArray(rejectedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesAllowedRemoveAddParentFromSample")
    public static RoleWithHierarchy[][] rolesAllowedToRemoveParentFromSample()
    {
        return toNestedArray(acceptedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider(name = "rolesNotAllowedToRemoveParentFromSample")
    public static RoleWithHierarchy[][] rolesNotAllowedToRemoveParentFromSample()
    {
        return toNestedArray(rejectedRoles(2, RoleWithHierarchy.SPACE_ETL_SERVER,
                RoleWithHierarchy.SPACE_POWER_USER));
    }

    public static Collection<RoleWithHierarchy> allInstanceRoles = new HashSet<RoleWithHierarchy>();

    public static Collection<RoleWithHierarchy> allSpaceRoles = new HashSet<RoleWithHierarchy>();

    {
        allInstanceRoles.add(RoleWithHierarchy.INSTANCE_ADMIN);
        allInstanceRoles.add(RoleWithHierarchy.INSTANCE_ETL_SERVER);
        allInstanceRoles.add(RoleWithHierarchy.INSTANCE_OBSERVER);
        allInstanceRoles.add(null);

        allSpaceRoles.add(RoleWithHierarchy.SPACE_ADMIN);
        allSpaceRoles.add(RoleWithHierarchy.SPACE_ETL_SERVER);
        allSpaceRoles.add(RoleWithHierarchy.SPACE_POWER_USER);
        allSpaceRoles.add(RoleWithHierarchy.SPACE_USER);
        allSpaceRoles.add(RoleWithHierarchy.SPACE_OBSERVER);
        allSpaceRoles.add(null);
    }

    public static RoleWithHierarchy[][] toNestedArray(Collection<List<RoleWithHierarchy>> input)
    {
        RoleWithHierarchy[][] result = new RoleWithHierarchy[input.size()][];
        int index = 0;
        for (List<RoleWithHierarchy> roles : input)
        {
            result[index] = roles.toArray(new RoleWithHierarchy[0]);
            index++;
        }
        return result;
    }

    public static Collection<List<RoleWithHierarchy>> acceptedRoles(int numSpaceRoles,
            RoleWithHierarchy... limits)
    {
        Collection<RoleWithHierarchy> acceptedRoles = new HashSet<RoleWithHierarchy>();
        for (RoleWithHierarchy limit : limits)
        {
            acceptedRoles.addAll(limit.getRoles());
        }

        Collection<List<RoleWithHierarchy>> results = new HashSet<List<RoleWithHierarchy>>();
        for (RoleWithHierarchy instanceRole : allInstanceRoles)
        {
            for (List<RoleWithHierarchy> spaceRoles : getSpaceRoleCombinations(numSpaceRoles))
            {
                if (acceptedRoles.contains(instanceRole) || acceptedRoles.containsAll(spaceRoles))
                {
                    List<RoleWithHierarchy> result = new ArrayList<RoleWithHierarchy>();
                    for (RoleWithHierarchy spaceRole : spaceRoles)
                    {
                        result.add(spaceRole);
                    }
                    result.add(instanceRole);
                    results.add(result);
                }
            }
        }
        return results;
    }

    public static Collection<List<RoleWithHierarchy>> rejectedRoles(int numSpaceRoles,
            RoleWithHierarchy... limits)
    {
        Collection<List<RoleWithHierarchy>> allCombinations = getAllRoleCombinations(numSpaceRoles);
        Collection<List<RoleWithHierarchy>> acceptedCombinations =
                acceptedRoles(numSpaceRoles, limits);
        allCombinations.removeAll(acceptedCombinations);
        return allCombinations;
    }

    public static Collection<List<RoleWithHierarchy>> getSpaceRoleCombinations(int num)
    {
        Collection<List<RoleWithHierarchy>> result = new HashSet<List<RoleWithHierarchy>>();
        if (num == 0)
        {
            return result;
        }

        Collection<List<RoleWithHierarchy>> subLists = getSpaceRoleCombinations(num - 1);

        for (RoleWithHierarchy role : allSpaceRoles)
        {
            if (subLists.size() == 0)
            {
                List<RoleWithHierarchy> list = new ArrayList<RoleWithHierarchy>();
                list.add(role);
                result.add(list);
            } else
            {
                for (List<RoleWithHierarchy> subList : subLists)
                {
                    List<RoleWithHierarchy> list = new ArrayList<RoleWithHierarchy>();
                    list.add(role);
                    list.addAll(subList);
                    result.add(list);
                }
            }
        }
        return result;
    }

    public static Collection<List<RoleWithHierarchy>> getAllRoleCombinations(int numSpaceRoles)
    {
        Collection<List<RoleWithHierarchy>> result = new HashSet<List<RoleWithHierarchy>>();

        for (List<RoleWithHierarchy> spaceRoles : getSpaceRoleCombinations(numSpaceRoles))
        {
            for (RoleWithHierarchy instanceRole : allInstanceRoles)
            {
                List<RoleWithHierarchy> list = new ArrayList<RoleWithHierarchy>();
                list.addAll(spaceRoles);
                list.add(instanceRole);

                if (containsOnlyNulls(list))
                {
                    continue;
                }

                result.add(list);
            }
        }
        return result;
    }

    public static boolean containsOnlyNulls(Collection<?> collection)
    {
        for (Object o : collection)
        {
            if (o != null)
            {
                return false;
            }
        }
        return true;
    }
}
