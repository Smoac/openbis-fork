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

import static ch.systemsx.cisd.openbis.systemtest.base.auth.RuleBuilder.and;
import static ch.systemsx.cisd.openbis.systemtest.base.auth.RuleBuilder.not;
import static ch.systemsx.cisd.openbis.systemtest.base.auth.RuleBuilder.or;
import static ch.systemsx.cisd.openbis.systemtest.base.auth.RuleBuilder.rule;

import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ProjectPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SpacePE;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.auth.AuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.auth.GuardedDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.InstanceDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.RolePermutator;
import ch.systemsx.cisd.openbis.systemtest.base.auth.SpaceDomain;

/**
 * @author anttil
 */
@ContextConfiguration(locations =
    { "classpath:stub_relationship_service.xml" }, inheritLocations = true)
public class RelationshipServiceAuthorizationTest extends BaseTest
{
    Space sourceSpace;

    Space destinationSpace;

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    Project sourceProject;

    Project destinationProject;

    Experiment sourceExperiment;

    Experiment destinationExperiment;

    Sample sourceSample;

    Sample destinationSample;

    Sample sharedSample;

    Sample childSample;

    Sample parentSample;

    ExternalData dataSet;

    @BeforeClass
    public void createFixture() throws Exception
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

        unrelatedAdmin = create(aSpace());
        unrelatedObserver = create(aSpace());
        unrelatedNone = create(aSpace());
    }

    @Test(dataProvider = "rolesAllowedToAssignExperimentToProject", groups = "authorization")
    public void assigningExperimentToProjectIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        assignExperimentToProject(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignExperimentToProject", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningExperimentToProjectIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        assignExperimentToProject(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignProjectToSpace", groups = "authorization")
    public void assigningProjectToSpaceIsAuthorizedFor(
            RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        assignProjectToSpace(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignProjectToSpace", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assignProjectToSpaceIsNotAuthorizedFor(
            RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        assignProjectToSpace(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignSampleToExperiment", groups = "authorization")
    public void assigningSampleToExperimentIsAuthorizedFor(
            RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        assignSampleToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToExperiment", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assignSampleToExperimentIsNotAuthorizedFor(
            RoleWithHierarchy sourceSpaceRole, RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        assignSampleToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToUnassignSampleFromExperiment", groups = "authorization")
    public void unassigningSampleFromExperimentIsAuthorizedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        unassignSampleFromExperiment(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToUnassignSampleFromExperiment", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void unassigningSampleFromExperimentIsNotAuthorizedFor(
            RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        unassignSampleFromExperiment(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToUnshareSample", groups = "authorization")
    public void unsharingSampleIsAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        unshareSample(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToUnshareSample", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void unsharingSampleIsNotAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        unshareSample(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignSampleToSpace", groups = "authorization")
    public void assigningSampleToSpaceIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        assignSampleToSpace(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToSpace", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningSampleToSpaceIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        assignSampleToSpace(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToShareSample", groups = "authorization")
    public void sharingSampleIsAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        shareSample(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToShareSample", expectedExceptions =
        { AuthorizationFailureException.class })
    public void sharingSampleIsNotAuthorizedFor(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        shareSample(spaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignDataSetToExperiment", groups = "authorization")
    public void assigningDataSetToExperimentIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        assignDataSetToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignDataSetToExperiment", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningDataSetToExperimentIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        assignDataSetToExperiment(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAssignDataSetToSample", groups = "authorization")
    public void assigningDataSetToSampleIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        assignDataSetToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAssignDataSetToExperiment", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningDataSetToSampleIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        assignDataSetToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToAddParentToSample", groups = "authorization")
    public void addingParentToSampleIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        addParentToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToAddParentToSample", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void addingParentToSampleIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        addParentToSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesAllowedToRemoveParentFromSample", groups = "authorization")
    public void removingParentFromSampleIsAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        removeParentFromSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    @Test(dataProvider = "rolesNotAllowedToRemoveParentFromSample", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void removingParentFromSampleIsNotAuthorizedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        removeParentFromSample(sourceSpaceRole, destinationSpaceRole, instanceRole);
    }

    void assignExperimentToProject(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String session =
                create(aSession()
                        .withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.assignExperimentToProject(sessionManager.getSession(session),
                pe(sourceExperiment), pe(destinationProject));
    }

    void assignProjectToSpace(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String session =
                create(aSession()
                        .withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.assignProjectToSpace(sessionManager.getSession(session),
                pe(sourceProject), pe(destinationSpace));
    }

    void assignSampleToExperiment(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String session =
                create(aSession()
                        .withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.assignSampleToExperiment(sessionManager.getSession(session),
                pe(sourceSample), pe(destinationExperiment));
    }

    void unassignSampleFromExperiment(RoleWithHierarchy spaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String session =
                create(aSession()
                        .withSpaceRole(spaceRole, sourceSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.unassignSampleFromExperiment(sessionManager.getSession(session),
                pe(sourceSample));
    }

    void unshareSample(RoleWithHierarchy spaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession()
                        .withSpaceRole(spaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.unshareSample(sessionManager.getSession(session), pe(sharedSample),
                pe(destinationSpace));
    }

    void assignSampleToSpace(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {

        String session =
                create(aSession()
                        .withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.assignSampleToSpace(sessionManager.getSession(session),
                pe(sourceSample), pe(destinationSpace));
    }

    void shareSample(RoleWithHierarchy spaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        String session =
                create(aSession()
                        .withSpaceRole(spaceRole, sourceSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.shareSample(sessionManager.getSession(session), pe(sourceSample));
    }

    void assignDataSetToExperiment(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String session =
                create(aSession()
                        .withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.assignDataSetToExperiment(sessionManager.getSession(session),
                pe(dataSet), pe(destinationExperiment));
    }

    void assignDataSetToSample(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {

        String session =
                create(aSession()
                        .withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.assignDataSetToSample(sessionManager.getSession(session),
                pe(dataSet), pe(destinationSample));
    }

    void addParentToSample(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String session =
                create(aSession()
                        .withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.addParentToSample(sessionManager.getSession(session), pe(sourceSample),
                pe(destinationSample));

    }

    void removeParentFromSample(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole,
            RoleWithHierarchy instanceRole) throws Exception
    {
        String session =
                create(aSession()
                        .withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        relationshipService.removeParentFromSample(sessionManager.getSession(session),
                pe(childSample), pe(parentSample));
    }

    GuardedDomain instance = new InstanceDomain("instance");

    GuardedDomain source = new SpaceDomain("source", instance);

    GuardedDomain destination = new SpaceDomain("destination", instance);

    AuthorizationRule spaceAdminOrSpaceEtlServer =
            and(
                    or(
                            rule(source, RoleWithHierarchy.SPACE_POWER_USER),
                            rule(source, RoleWithHierarchy.SPACE_ETL_SERVER)
                    ),
                    or(
                            rule(destination, RoleWithHierarchy.SPACE_POWER_USER),
                            rule(destination, RoleWithHierarchy.SPACE_ETL_SERVER)
                    )
            );

    AuthorizationRule spaceAdminOrSpaceEtlServerSingle =
            or(
                    rule(source, RoleWithHierarchy.SPACE_POWER_USER),
                    rule(source, RoleWithHierarchy.SPACE_ETL_SERVER)
            );

    AuthorizationRule instanceEtlServer = rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER);

    @DataProvider(name = "rolesAllowedToAssignExperimentToProject")
    RoleWithHierarchy[][] rolesAllowedToAssignExperimentToProject()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer,
                source, destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAssignExperimentToProject")
    RoleWithHierarchy[][] rolesNotAllowedToAssignExperimentToProject()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer),
                source, destination, instance);
    }

    @DataProvider(name = "rolesAllowedToAssignProjectToSpace")
    RoleWithHierarchy[][] rolesAllowedToAssignProjectToSpace()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer,
                source, destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAssignProjectToSpace")
    RoleWithHierarchy[][] rolesNotAllowedToAssignProjectToSpace()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer),
                source, destination, instance);
    }

    @DataProvider(name = "rolesAllowedToAssignSampleToExperiment")
    RoleWithHierarchy[][] rolesAllowedToAssignSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer,
                source, destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAssignSampleToExperiment")
    RoleWithHierarchy[][] rolesNotAllowedToAssignSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer),
                source, destination, instance);
    }

    @DataProvider(name = "rolesAllowedToUnassignSampleFromExperiment")
    RoleWithHierarchy[][] rolesAllowedToUnassignSampleFromExperiment()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServerSingle,
                source, instance);
    }

    @DataProvider(name = "rolesNotAllowedToUnassignSampleFromExperiment")
    RoleWithHierarchy[][] rolesNotAllowedToUnassignSampleFromExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServerSingle),
                source, instance);
    }

    @DataProvider(name = "rolesAllowedToUnshareSample")
    RoleWithHierarchy[][] rolesAllowedToUnshareSample()
    {
        return RolePermutator.getAcceptedPermutations(instanceEtlServer,
                destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToUnshareSample")
    RoleWithHierarchy[][] rolesNotAllowedToUnshareSample()
    {
        return RolePermutator.getAcceptedPermutations(not(instanceEtlServer),
                destination, instance);
    }

    @DataProvider(name = "rolesAllowedToAssignSampleToSpace")
    RoleWithHierarchy[][] rolesAllowedToAssignSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer,
                source, destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAssignSampleToSpace")
    RoleWithHierarchy[][] rolesNotAllowedToAssignSampleToSpace()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer),
                source, destination, instance);
    }

    @DataProvider(name = "rolesAllowedToShareSample")
    RoleWithHierarchy[][] rolesAllowedToShareSample()
    {
        return RolePermutator.getAcceptedPermutations(instanceEtlServer, source, instance);
    }

    @DataProvider(name = "rolesNotAllowedToShareSample")
    RoleWithHierarchy[][] rolesNotAllowedToShareSample()
    {
        return RolePermutator.getAcceptedPermutations(not(instanceEtlServer), source, instance);
    }

    @DataProvider(name = "rolesAllowedToAssignDataSetToExperiment")
    RoleWithHierarchy[][] rolesAllowedToAssignDataSetToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer,
                source, destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAssignDataSetToExperiment")
    RoleWithHierarchy[][] rolesNotAllowedToAssignDataSetToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer),
                source, destination, instance);
    }

    @DataProvider(name = "rolesAllowedToAssignDataSetToSample")
    RoleWithHierarchy[][] rolesAllowedToAssignDataSetToSample()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer,
                source, destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAssignDataSetToSample")
    RoleWithHierarchy[][] rolesNotAllowedToAssignDataSetToSample()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer),
                source, destination, instance);
    }

    @DataProvider(name = "rolesAllowedToAddParentToSample")
    RoleWithHierarchy[][] rolesAllowedToAddParentToSample()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer,
                source, destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToAddParentToSample")
    RoleWithHierarchy[][] rolesNotAllowedToAddParentToSample()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer),
                source, destination, instance);
    }

    @DataProvider(name = "rolesAllowedRemoveAddParentFromSample")
    RoleWithHierarchy[][] rolesAllowedToRemoveParentFromSample()
    {
        return RolePermutator.getAcceptedPermutations(spaceAdminOrSpaceEtlServer,
                source, destination, instance);
    }

    @DataProvider(name = "rolesNotAllowedToRemoveParentFromSample")
    RoleWithHierarchy[][] rolesNotAllowedToRemoveParentFromSample()
    {
        return RolePermutator.getAcceptedPermutations(not(spaceAdminOrSpaceEtlServer),
                source, destination, instance);
    }

    ExperimentPE pe(Experiment experiment)
    {
        return daoFactory.getExperimentDAO().tryGetByPermID(experiment.getPermId());
    }

    ProjectPE pe(Project project)
    {
        return daoFactory.getProjectDAO().tryFindProject(
                project.getSpace().getInstance().getCode(), project.getSpace().getCode(),
                project.getCode());
    }

    SpacePE pe(Space space)
    {
        return daoFactory.getSpaceDAO().tryFindSpaceByCodeAndDatabaseInstance(
                space.getCode(),
                daoFactory.getDatabaseInstanceDAO().tryFindDatabaseInstanceByCode(
                        space.getInstance().getCode()));
    }

    SamplePE pe(Sample sample)
    {
        return daoFactory.getSampleDAO().tryToFindByPermID(sample.getPermId());
    }

    DataPE pe(ExternalData data)
    {
        return daoFactory.getDataDAO().tryToFindDataSetByCode(data.getCode());
    }
}
