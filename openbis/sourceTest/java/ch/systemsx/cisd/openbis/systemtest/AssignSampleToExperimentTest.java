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

import static org.hamcrest.CoreMatchers.is;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityGraphGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.ExperimentNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.SampleNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy.RoleCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ProjectIdentifierFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.auth.AuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.auth.GuardedDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.InstanceDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.SpaceDomain;

/**
 * @author anttil
 */
public class AssignSampleToExperimentTest extends BaseTest
{
    Experiment sourceExperiment;

    Experiment destinationExperiment;

    Project sourceProject;

    Project destinationProject;

    Space sourceSpace;

    Space destinationSpace;
    
    @Test
    public void unassignSampleWithDataSetsFromExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1[NET]\n"
                + "S1, data sets: DS1[NET]\n");
        
        reassignSampleToExperiment(g.s(1), null);
        
        assertEquals("S1, data sets: DS1[NET]\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }
    
    @Test
    public void assignSampleWithoutExperimentWithDataSetsToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1\n"
                + "S1, data sets: DS1[NET]\n");
        
        reassignSampleToExperiment(g.s(1), g.e(1));
        
        assertEquals("E1, samples: S1, data sets: DS1[NET]\n"
                + "S1, data sets: DS1[NET]\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }
    
    @Test
    public void assignSampleWithExperimentWithDataSetsToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "E2, samples: S2, data sets: DS2\n"
                + "S1, data sets: DS1\n");
        
        reassignSampleToExperiment(g.s(1), g.e(2));
        
        assertEquals("E2, samples: S1 S2, data sets: DS1 DS2\n"
                + "S1, data sets: DS1\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }
    
    @Test
    public void assignScreeningPlateToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1 DS2\n"
                + "E2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n");
        
        reassignSampleToExperiment(g.s(1), g.e(2));
        
        assertEquals("E2, samples: S1, data sets: DS1 DS2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n", renderGraph(g));
        assertModified(g.e(1), g.e(2));
        assertModified(g.s(1));
        assertModified(g.ds(1), g.ds(2));
        assertUnmodified(g);
    }
    
    @Test
    public void assignSamplesWithoutExperimentWithDataSetsToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS2\n"
                + "S1, data sets: DS1[NET]\n");
        
        reassignSamplesToExperiment(g.e(1), g.s(1));
        
        assertEquals("E1, samples: S1, data sets: DS1[NET] DS2\n"
                + "S1, data sets: DS1[NET]\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }
    
    @Test
    public void removeSamplesWithDataSetsFromExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1 S2, data sets: DS1[NET] DS2\n"
                + "S1, data sets: DS1[NET]\n");
        
        reassignSamplesToExperiment(g.e(1));
        
        assertEquals("E1, data sets: DS2\n"
                + "S1, data sets: DS1[NET]\n", renderGraph(g));
        assertModified(g.e(1));
        assertModified(g.s(1), g.s(2));
        assertModified(g.ds(1));
        assertUnmodified(g);
    }
    
    @Test
    public void removeSamplesWithDataSetsFromExperimentFailsBecauseOneDataSetNeedsAnExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1[NET] DS2\n"
                + "S1, data sets: DS1[NET] DS2\n");
        
        try
        {
            reassignSamplesToExperiment(g.e(1));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Operation cannot be performed, because the sample " 
                    + entityGraphManager.getSample(g.s(1)).getIdentifier() 
                    + " has the following datasets which need an experiment: [" 
                    + entityGraphManager.getDataSet(g.ds(2)).getCode() + "]", ex.getMessage());
        }
    }
    
    private void reassignSamplesToExperiment(ExperimentNode experimentNode, SampleNode...sampleNodes)
    {
        String experimentIdentifier = entityGraphManager.getExperimentIdentifierOrNull(experimentNode);
        String user = create(aSession().withInstanceRole(RoleCode.ADMIN));
        reassignSamplesToExperiment(experimentIdentifier, getSamplePermIds(sampleNodes), user);
    }
    
    protected void reassignSamplesToExperiment(String experimentIdentifier, List<String> samplePermIds, 
            String userSessionToken)
    {
        Experiment experiment = etlService.tryGetExperiment(systemSessionToken, 
                ExperimentIdentifierFactory.parse(experimentIdentifier));
        perform(anUpdateOf(experiment).withSamples(loadSamples(samplePermIds)).as(userSessionToken));
        
    }
    
    /**
     * Reassigns specified sample to specified experiment for the specified user session token. 
     * If experiment is not specified unassignment is meant.
     * Sub class for testing API V3 should override this method.
     */
    protected void reassignSampleToExperiment(String samplePermId, String experimentIdentifierOrNull, 
            String userSessionToken)
    {
        SampleIdentifier sampleIdentifier = etlService.tryGetSampleIdentifier(systemSessionToken, samplePermId);
        Sample sample = etlService.tryGetSampleWithExperiment(systemSessionToken, sampleIdentifier);
        if (experimentIdentifierOrNull != null)
        {
            Experiment experiment = etlService.tryGetExperiment(systemSessionToken, 
                    ExperimentIdentifierFactory.parse(experimentIdentifierOrNull));
            perform(anUpdateOf(sample).toExperiment(experiment).as(userSessionToken));
        } else
        {
            perform(anUpdateOf(sample).removingExperiment().as(userSessionToken));
        }
    }
    
    /**
     * Registers a new experiment for the specified project with the specified existing samples. 
     */
    protected String registerExperimentWithSamples(String projectIdentifier, List<String> samplePermIds,
            String userSessionToken)
    {
        Sample[] samples = loadSamples(samplePermIds);
        Project project = commonServer.getProjectInfo(systemSessionToken, ProjectIdentifierFactory.parse(projectIdentifier));
        Experiment experiment = create(anExperiment().inProject(project).withSamples(samples).as(userSessionToken));
        return experiment.getIdentifier();
    }

    private Sample[] loadSamples(List<String> samplePermIds)
    {
        List<Sample> samples = new ArrayList<Sample>();
        for (String permId : samplePermIds)
        {
            SampleIdentifier sampleIdentifier = etlService.tryGetSampleIdentifier(systemSessionToken, permId);
            if (sampleIdentifier == null)
            {
                throw new IllegalArgumentException("Unknown sample with perm id: " + permId);
            }
            Sample sample = etlService.tryGetSampleWithExperiment(systemSessionToken, sampleIdentifier);
            if (sample == null)
            {
                throw new IllegalArgumentException("Unknown sample with identifier: " + sampleIdentifier);
            }
            samples.add(sample);
        }
        return samples.toArray(new Sample[0]);
    }


    @Test
    public void sampleWithExperimentCanBeAssignedToAnotherExperiment() throws Exception
    {
        Sample sample = create(aSample().inExperiment(sourceExperiment));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(sample, is(inExperiment(destinationExperiment)));
    }

    @Test
    public void sampleIsAssignedWithSpaceOfNewExperiment() throws Exception
    {
        Sample sample = create(aSample().inExperiment(sourceExperiment));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(sample, is(inSpace(destinationSpace)));
    }

    @Test
    public void dataSetsOfSampleAreAssociatedWithNewExperiment() throws Exception
    {
        Sample sample = create(aSample().inExperiment(sourceExperiment));
        AbstractExternalData dataSet = create(aDataSet().inSample(sample));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(dataSet, is(inExperiment(destinationExperiment)));
    }

    @Test
    public void spaceSampleCanBeAssignedToExperiment() throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(sample, is(inExperiment(destinationExperiment)));
        assertThat(sample, is(inSpace(destinationSpace)));
    }

    @Test
    public void sharedSampleCanBeAssignedToExperiment() throws Exception
    {
        Sample sample = create(aSample());

        perform(anUpdateOf(sample).toExperiment(destinationExperiment));

        assertThat(sample, is(inExperiment(destinationExperiment)));
        assertThat(sample, is(inSpace(destinationSpace)));
    }

    @Test
    public void childSampleCanBeAssignedToAnotherExperiment() throws Exception
    {
        Sample parent = create(aSample().inExperiment(sourceExperiment));
        Sample child = create(aSample().withParent(parent).inExperiment(sourceExperiment));

        perform(anUpdateOf(child).toExperiment(destinationExperiment));

        assertThat(child, is(inExperiment(destinationExperiment)));
        assertThat(child, is(inSpace(destinationSpace)));
    }

    @Test
    public void experimentAssignmentOfParentSampleIsNotChangedWhenChildSampleIsAssignedToAnotherExperiment()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(sourceExperiment));
        Sample child = create(aSample().withParent(parent).inExperiment(sourceExperiment));

        perform(anUpdateOf(child).toExperiment(destinationExperiment));

        assertThat(parent, is(inExperiment(sourceExperiment)));
        assertThat(parent, is(inSpace(sourceSpace)));
    }

    @Test
    public void parentSampleCanBeAssignedToAnotherExperiment() throws Exception
    {
        Sample parent = create(aSample().inExperiment(sourceExperiment));
        create(aSample().withParent(parent).inExperiment(sourceExperiment));

        perform(anUpdateOf(parent).toExperiment(destinationExperiment));

        assertThat(parent, is(inExperiment(destinationExperiment)));
        assertThat(parent, is(inSpace(destinationSpace)));
    }

    @Test
    public void experimentAssignmentOfChildSampleIsNotChangedWhenParentSampleIsAssignmedToAnotherExperiment()
            throws Exception
    {
        Sample parent = create(aSample().inExperiment(sourceExperiment));
        Sample child = create(aSample().withParent(parent).inExperiment(sourceExperiment));

        perform(anUpdateOf(parent).toExperiment(destinationExperiment));

        assertThat(child, is(inExperiment(sourceExperiment)));
        assertThat(child, is(inSpace(sourceSpace)));
    }

    @Test
    public void componentSampleCanBeAssignedToAnotherExperiment() throws Exception
    {
        Sample container = create(aSample().inExperiment(sourceExperiment));
        Sample component = create(aSample().inContainer(container).inExperiment(sourceExperiment));

        perform(anUpdateOf(component).toExperiment(destinationExperiment));

        assertThat(component, is(inExperiment(destinationExperiment)));
        assertThat(component, is(inSpace(destinationSpace)));
    }

    @Test
    public void experimentAssignmentOfContainerSampleIsNotChangedWhenComponentSampleIsAssignedToAnotherExperiment()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(sourceExperiment));
        Sample component = create(aSample().inContainer(container).inExperiment(sourceExperiment));

        perform(anUpdateOf(component).toExperiment(destinationExperiment));

        assertThat(container, is(inExperiment(sourceExperiment)));
        assertThat(container, is(inSpace(sourceSpace)));
    }

    @Test
    public void containerSampleCanBeAssignedToAnotherExperiment() throws Exception
    {
        Sample container = create(aSample().inExperiment(sourceExperiment));
        create(aSample().inContainer(container).inExperiment(sourceExperiment));

        perform(anUpdateOf(container).toExperiment(destinationExperiment));

        assertThat(container, is(inExperiment(destinationExperiment)));
        assertThat(container, is(inSpace(destinationSpace)));
    }

    @Test
    public void experimentAssignmentOfComponentSampleIsNotChangedWhenContainerSampleIsAssignedToAnotherExperiment()
            throws Exception
    {
        Sample container = create(aSample().inExperiment(sourceExperiment));
        Sample component = create(aSample().inContainer(container).inExperiment(sourceExperiment));

        perform(anUpdateOf(container).toExperiment(destinationExperiment));

        assertThat(component, is(inExperiment(sourceExperiment)));
        assertThat(component, is(inSpace(sourceSpace)));
    }

    @Test
    public void sampleWithoutExperimentCanBeAssignedToExperimentInSameSpaceThroughExperimentUpdate()
            throws Exception
    {
        Sample sample = create(aSample().inSpace(destinationSpace));

        perform(anUpdateOf(destinationExperiment).withSamples(sample));

        assertThat(sample, is(inExperiment(destinationExperiment)));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void sampleWithoutExperimentCanNotBeAssignedToExperimentInAnotherSpaceThroughExperimentUpdate()
            throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));

        perform(anUpdateOf(destinationExperiment).withSamples(sample));

        assertThat(sample, is(inExperiment(destinationExperiment)));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void sampleWithExperimentCanNotBeAssignedToAnotherExperimentThroughExperimentUpdate()
            throws Exception
    {
        Experiment destinationExperimentInSameSpace =
                create(anExperiment().inProject(sourceProject));
        Sample sample = create(aSample().inExperiment(sourceExperiment));

        perform(anUpdateOf(destinationExperimentInSameSpace).withSamples(sample));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void sharedSampleCanNotBeAssignedToExperimentThroughExperimentUpdate() throws Exception
    {
        Sample sample = create(aSample());

        perform(anUpdateOf(destinationExperiment).withSamples(sample));

        assertThat(sample, is(inExperiment(destinationExperiment)));
    }

    @Test
    public void registeringExperimentWithSampleInSameSpaceThatIsNotAssignedToAnyExperimentAssignsTheSampleToTheExperiment()
            throws Exception
    {
        Sample sample = create(aSample().inSpace(sourceSpace));

        Experiment experiment = create(anExperiment().inProject(sourceProject).withSamples(sample));

        assertThat(sample, is(inExperiment(experiment)));
    }
    
    @Test
    public void registerExperimentWithSampleWithDataSets()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, data sets: DS1[NET]\n");
        
        registerExperimentWithSamples(g.e(1), g.s(1));
        
        assertEquals("E1, samples: S1, data sets: DS1[NET]\n"
                + "S1, data sets: DS1[NET]\n", renderGraph(g));
        assertModified(g.s(1));
        assertModified(g.ds(1));
        assertUnmodified(g);
        
    }
    
    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    @Test(dataProvider = "rolesAllowedToAssignSampleToExperiment", groups = "authorization")
    public void assigningSampleToExperimentIsAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(sourceExperiment));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToExperiment", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningSampleToExperimentIsNotAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample().inExperiment(sourceExperiment));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment).as(user));
    }

    @Test(dataProvider = "rolesAllowedToAssignSharedSampleToExperiment", groups = "authorization")
    public void assigningSharedSampleToExperimentIsAllowedFor(
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample());
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSharedSampleToExperiment", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningSharedSampleToExperimentIsNotAllowedFor(
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample());
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(sample).toExperiment(destinationExperiment).as(user));
    }

    @Test(dataProvider = "rolesAllowedToAssignSampleToExperimentThroughExperimentUpdate", groups = "authorization")
    public void assigningSampleToExperimentThroughExperimentUpdateIsAllowedFor(
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample().inSpace(destinationSpace));
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(destinationExperiment).withSamples(sample).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignSampleToExperimentThroughExperimentUpdate", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningSampleToExperimentThroughExperimentUpdateIsNotAllowedFor(
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        Sample sample = create(aSample().inSpace(destinationSpace));
        String user =
                create(aSession().withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(destinationExperiment).withSamples(sample).as(user));
    }

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createFixture() throws Exception
    {
        sourceSpace = create(aSpace().withCode("sourceSpace"));
        destinationSpace = create(aSpace().withCode("destinationSpace"));

        sourceProject = create(aProject().inSpace(sourceSpace));
        destinationProject = create(aProject().inSpace(destinationSpace));

        sourceExperiment =
                create(anExperiment().inProject(sourceProject).withCode("sourceExperiment"));
        destinationExperiment =
                create(anExperiment().inProject(destinationProject).withCode(
                        "destinationExperiment"));

        unrelatedAdmin = create(aSpace());
        unrelatedObserver = create(aSpace());
        unrelatedNone = create(aSpace());
    }

    GuardedDomain source;

    GuardedDomain destination;

    GuardedDomain instance;

    AuthorizationRule assignSampleToExperimentRule;

    AuthorizationRule assignSampleToExperimentThroughExperimentUpdateRule;

    AuthorizationRule assignSharedSampleToExperimentRule;

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createAuthorizationRules()
    {
        instance = new InstanceDomain();
        source = new SpaceDomain(instance);
        destination = new SpaceDomain(instance);

        assignSampleToExperimentRule =
                or(and(rule(source, RoleWithHierarchy.SPACE_POWER_USER),
                        rule(destination, RoleWithHierarchy.SPACE_POWER_USER)),

                        and(rule(source, RoleWithHierarchy.SPACE_USER),
                                rule(destination, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)));

        assignSampleToExperimentThroughExperimentUpdateRule =
                or(rule(source, RoleWithHierarchy.SPACE_POWER_USER),

                        and(rule(source, RoleWithHierarchy.SPACE_USER),
                                rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER)));

        assignSharedSampleToExperimentRule =
                and(rule(destination, RoleWithHierarchy.SPACE_USER),
                        rule(instance, RoleWithHierarchy.INSTANCE_ETL_SERVER));
    }
/*
    @DataProvider
    Object[][] rolesAllowedToAssignSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(assignSampleToExperimentRule, source,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(assignSampleToExperimentRule), source,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesAllowedToAssignSampleToExperimentThroughExperimentUpdate()
    {
        return RolePermutator.getAcceptedPermutations(
                assignSampleToExperimentThroughExperimentUpdateRule, source, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSampleToExperimentThroughExperimentUpdate()
    {
        return RolePermutator.getAcceptedPermutations(
                not(assignSampleToExperimentThroughExperimentUpdateRule), source, instance);
    }

    @DataProvider
    Object[][] rolesAllowedToAssignSharedSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(assignSharedSampleToExperimentRule,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignSharedSampleToExperiment()
    {
        return RolePermutator.getAcceptedPermutations(not(assignSharedSampleToExperimentRule),
                destination, instance);
    }
 */    
    
    private void reassignSampleToExperiment(SampleNode sampleNode, ExperimentNode experimentNodeOrNull)
    {
        String samplePermId = entityGraphManager.getSamplePermIdOrNull(sampleNode);
        String experimentIdentifierOrNull = entityGraphManager.getExperimentIdentifierOrNull(experimentNodeOrNull);
        String user = create(aSession().withInstanceRole(RoleCode.ADMIN));
        reassignSampleToExperiment(samplePermId, experimentIdentifierOrNull, user);
    }
    
    private void registerExperimentWithSamples(ExperimentNode experimentNode, SampleNode...sampleNodes)
    {
        List<String> samplePermIds = getSamplePermIds(sampleNodes);
        String user = create(aSession().withInstanceRole(RoleCode.ADMIN));
        String experimentIdentifier 
                = registerExperimentWithSamples(entityGraphManager.getIdentifierOfDefaultProject(), samplePermIds, user);
        addToRepository(experimentNode, etlService.tryGetExperiment(systemSessionToken, 
                ExperimentIdentifierFactory.parse(experimentIdentifier)));
    }

    private List<String> getSamplePermIds(SampleNode... sampleNodes)
    {
        List<String> samplePermIds = new ArrayList<String>();
        for (SampleNode sampleNode : sampleNodes)
        {
            samplePermIds.add(entityGraphManager.getSamplePermIdOrNull(sampleNode));
        }
        return samplePermIds;
    }
    
}
