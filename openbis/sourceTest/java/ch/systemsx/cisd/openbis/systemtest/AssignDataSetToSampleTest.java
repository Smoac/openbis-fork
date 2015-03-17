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

import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.DataSetNode;
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
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.systemtest.base.BaseTest;
import ch.systemsx.cisd.openbis.systemtest.base.auth.AuthorizationRule;
import ch.systemsx.cisd.openbis.systemtest.base.auth.GuardedDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.InstanceDomain;
import ch.systemsx.cisd.openbis.systemtest.base.auth.RolePermutator;
import ch.systemsx.cisd.openbis.systemtest.base.auth.SpaceDomain;

/**
 * @author anttil
 */
public class AssignDataSetToSampleTest extends BaseTest
{
    Sample sourceSample;

    Sample destinationSample;

    Experiment sourceExperiment;

    Experiment destinationExperiment;

    Space sourceSpace;

    Space destinationSpace;
    
    @Test
    public void dataSetReassignedFromExperimentToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "E2, samples: S2\n");

        reassignToExperiment(g.ds(1), g.e(2));

        assertEquals("E1, samples: S1\n"
                + "E2, samples: S2, data sets: DS1\n", renderGraph(g));
        repository.assertModified(g.e(1), g.e(2));
        repository.assertModified(g.ds(1));
        repository.assertUnmodified(g.s(1), g.s(2));
    }

    @Test
    public void dataSetReassignedFromExperimentToSampleWithExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1\nE2, samples: S2\n");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E2, samples: S2, data sets: DS1\nS2, data sets: DS1\n", renderGraph(g));
        repository.assertModified(g.e(1), g.e(2));
        repository.assertModified(g.s(2));
        repository.assertModified(g.ds(1));
    }
    
    @Test
    public void dataSetReassignedFromExperimentToSampleWithoutExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1[NEXP-TYPE]\n"
                + "S2\n");
        
        reassignToSample(g.ds(1), g.s(2));
        
        assertEquals("E1, samples: S1\nS2, data sets: DS1[NEXP-TYPE]\n", renderGraph(g));
        repository.assertModified(g.e(1));
        repository.assertUnmodified(g.s(1));
        repository.assertModified(g.s(2));
        repository.assertModified(g.ds(1));
    }
    
    @Test
    public void dataSetReassignedFromSampleWithExperimentToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "E2, samples: S2\nS1, data sets: DS1\n");

        reassignToExperiment(g.ds(1), g.e(2));

        assertEquals("E1, samples: S1\n"
                + "E2, samples: S2, data sets: DS1\n", renderGraph(g));
        repository.assertModified(g.e(1), g.e(2));
        repository.assertModified(g.s(1));
        repository.assertModified(g.ds(1));
        repository.assertUnmodified(g.s(2));
    }
    
    @Test
    public void dataSetReassignedFromSampleWithExperimentToSampleWithExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1\n"
                + "E2, samples: S2\nS1, data sets: DS1\n");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E1, samples: S1\n"
                + "E2, samples: S2, data sets: DS1\nS2, data sets: DS1\n", renderGraph(g));
        repository.assertModified(g.e(1), g.e(2));
        repository.assertModified(g.s(1), g.s(2));
        repository.assertModified(g.ds(1));
    }
    
    @Test
    public void dataSetReassignedFromSampleWithExperimentToSampleWithoutExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1, data sets: DS1[NEXP-TYPE]\n"
                + "S1, data sets: DS1[NEXP-TYPE]\nS2\n");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E1, samples: S1\n"
                + "S2, data sets: DS1[NEXP-TYPE]\n", renderGraph(g));
        repository.assertModified(g.e(1));
        repository.assertModified(g.s(1), g.s(2));
        repository.assertModified(g.ds(1));
    }

    @Test
    public void dataSetReassignedFromSampleWithoutExperimentToExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1\n"
                + "S1, data sets: DS1[NEXP-TYPE]\n");

        reassignToExperiment(g.ds(1), g.e(1));

        assertEquals("E1, data sets: DS1[NEXP-TYPE]\n", renderGraph(g));
        repository.assertModified(g.e(1));
        repository.assertModified(g.s(1));
        repository.assertModified(g.ds(1));
    }
    
    @Test
    public void dataSetReassignedFromSampleWithoutExperimentToSampleWithExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, samples: S1\n"
                + "S2, data sets: DS1[NEXP-TYPE]\n");

        reassignToSample(g.ds(1), g.s(1));

        assertEquals("E1, samples: S1, data sets: DS1[NEXP-TYPE]\n"
                + "S1, data sets: DS1[NEXP-TYPE]\n", renderGraph(g));
        repository.assertModified(g.e(1));
        repository.assertModified(g.s(1), g.s(2));
        repository.assertModified(g.ds(1));
    }
    
    @Test
    public void dataSetReassignedFromSampleWithoutExperimentToSampleWithoutExperiment()
    {
        EntityGraphGenerator g = parseAndCreateGraph("S1, data sets: DS1[NO-EXP-TYPE]\n"
                + "S2\n");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("S2, data sets: DS1[NO-EXP-TYPE]\n", renderGraph(g));
        repository.assertModified(g.s(1), g.s(2));
        repository.assertModified(g.ds(1));
    }
    
    @Test(expectedExceptions =
        { UserFailureException.class })
    public void dataSetCannotBeAssignedToSpaceSample() throws Exception
    {
        Sample sample = create(aSample().inSpace(destinationSpace));
        AbstractExternalData dataset = create(aDataSet().inSample(sourceSample));

        perform(anUpdateOf(dataset).toSample(sample));
    }

    @Test(expectedExceptions =
        { UserFailureException.class })
    public void dataSetCannotBeAssignedToSharedSample() throws Exception
    {
        Sample sample = create(aSample());
        AbstractExternalData dataset = create(aDataSet().inSample(sourceSample));

        perform(anUpdateOf(dataset).toSample(sample));
    }

    @Test
    public void childDataSetCanBeAssignedToAnotherSample() throws Exception
    {
        AbstractExternalData parent = create(aDataSet().inSample(sourceSample));
        AbstractExternalData child = create(aDataSet().inSample(sourceSample).withParent(parent));

        perform(anUpdateOf(child).toSample(destinationSample));

        assertThat(child, is(inSample(destinationSample)));
    }

    @Test
    public void sampleAssignmentOfParentDataSetIsNotChangedWhenChildDataSetIsAssignedToAnotherSample()
            throws Exception
    {
        AbstractExternalData parent = create(aDataSet().inSample(sourceSample));
        AbstractExternalData child = create(aDataSet().inSample(sourceSample).withParent(parent));

        perform(anUpdateOf(child).toSample(destinationSample));

        assertThat(parent, is(inSample(sourceSample)));
    }

    @Test
    public void parentDataSetCanBeAssignedToAnotherSample() throws Exception
    {
        AbstractExternalData parent = create(aDataSet().inSample(sourceSample));
        create(aDataSet().inSample(sourceSample).withParent(parent));

        perform(anUpdateOf(parent).toSample(destinationSample));

        assertThat(parent, is(inSample(destinationSample)));
    }

    @Test
    public void sampleAssignmentOfChildDataSetIsNotChangedWhenParentDatasetIsAssignedToAnotherSample()
            throws Exception
    {
        AbstractExternalData parent = create(aDataSet().inSample(sourceSample));
        AbstractExternalData child = create(aDataSet().inSample(sourceSample).withParent(parent));

        perform(anUpdateOf(parent).toSample(destinationSample));

        assertThat(child, is(inSample(sourceSample)));
    }

    @Test
    public void componentDataSetCanBeAssignedToAnotherSample() throws Exception
    {
        AbstractExternalData component = create(aDataSet().inSample(sourceSample));
        create(aDataSet().inSample(sourceSample).withComponent(component));

        perform(anUpdateOf(component).toSample(destinationSample));

        assertThat(component, is(inSample(destinationSample)));
    }

    @Test
    public void sampleAssignmentOfContainerDataSetIsNotChangedWhenComponentDataSetIsAssignedToAnotherSample()
            throws Exception
    {
        AbstractExternalData component = create(aDataSet().inSample(sourceSample));
        AbstractExternalData container = create(aDataSet().inSample(sourceSample).withComponent(component));

        perform(anUpdateOf(component).toSample(destinationSample));

        assertThat(container, is(inSample(sourceSample)));
    }

    @Test
    public void containerDataSetCanBeAssignedToAnotherSample() throws Exception
    {
        AbstractExternalData component = create(aDataSet().inSample(sourceSample));
        AbstractExternalData container = create(aDataSet().inSample(sourceSample).withComponent(component));

        perform(anUpdateOf(container).toSample(destinationSample));

        assertThat(container, is(inSample(destinationSample)));
    }

    @Test
    public void sampleAssignmentOfComponentDataSetIsNotChangedWhenContainerDataSetIsAssignedToAnotherSample()
            throws Exception
    {
        AbstractExternalData component = create(aDataSet().inSample(sourceSample));
        AbstractExternalData container = create(aDataSet().inSample(sourceSample).withComponent(component));

        perform(anUpdateOf(container).toSample(destinationSample));

        assertThat(component, is(inSample(sourceSample)));
    }

    @Test
    public void dataSetCanBeUnassignedFromSample() throws Exception
    {
        AbstractExternalData data = create(aDataSet().inSample(sourceSample));

        perform(anUpdateOf(data).removingSample());

        assertThat(data, hasNoSample());
        assertThat(data, is(inExperiment(sourceExperiment)));
    }

    private void reassignToExperiment(DataSetNode dataSetNode, ExperimentNode experimentNode)
    {
        AbstractExternalData dataSet = getDataSet(dataSetNode);
        Experiment experiment = repository.getExperiment(experimentNode);
        reassignToExperiment(dataSet.getCode(), experiment.getIdentifier());
    }

    private void reassignToSample(DataSetNode dataSetNode, SampleNode sampleNode)
    {
        AbstractExternalData dataSet = getDataSet(dataSetNode);
        Sample sample = repository.getSample(sampleNode);
        if (sample == null)
        {
            throw new IllegalArgumentException("Unknown sample " + sampleNode.getCode());
        }
        reassignToSample(dataSet.getCode(), sample.getPermId());
    }

    private AbstractExternalData getDataSet(DataSetNode dataSetNode)
    {
        AbstractExternalData dataSet = repository.getDataSet(dataSetNode);
        if (dataSet == null)
        {
            throw new IllegalArgumentException("Unknown data set " + dataSetNode.getCode());
        }
        return dataSet;
    }
    
    protected void reassignToExperiment(String dataSetCode, String experimentIdentifier)
    {
        AbstractExternalData dataSet = etlService.tryGetDataSet(systemSessionToken, dataSetCode);
        Experiment experiment = etlService.tryGetExperiment(systemSessionToken, 
                ExperimentIdentifierFactory.parse(experimentIdentifier));
        String user = create(aSession().withInstanceRole(RoleCode.ADMIN));
        perform(anUpdateOf(dataSet).toExperiment(experiment).as(user));
    }

    protected void reassignToSample(String dataSetCode, String samplePermId)
    {
        AbstractExternalData dataSet = etlService.tryGetDataSet(systemSessionToken, dataSetCode);
        SampleIdentifier sampleIdentifier = etlService.tryGetSampleIdentifier(systemSessionToken, samplePermId);
        Sample sample = etlService.tryGetSampleWithExperiment(systemSessionToken, sampleIdentifier);
        String user = create(aSession().withInstanceRole(RoleCode.ADMIN));
        perform(anUpdateOf(dataSet).toSample(sample).as(user));
    }
    
    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    @Test(dataProvider = "rolesAllowedToAssignDataSetToSample", groups = "authorization")
    public void assigningDataSetToSampleIsAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        AbstractExternalData dataset = create(aDataSet().inSample(sourceSample));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(dataset).toSample(destinationSample).as(user));
    }

    @Test(dataProvider = "rolesNotAllowedToAssignDataSetToSample", expectedExceptions =
        { AuthorizationFailureException.class }, groups = "authorization")
    public void assigningDataSetToSampleIsNotAllowedFor(RoleWithHierarchy sourceSpaceRole,
            RoleWithHierarchy destinationSpaceRole, RoleWithHierarchy instanceRole)
            throws Exception
    {
        AbstractExternalData dataset = create(aDataSet().inSample(sourceSample));
        String user =
                create(aSession().withSpaceRole(sourceSpaceRole, sourceSpace)
                        .withSpaceRole(destinationSpaceRole, destinationSpace)
                        .withInstanceRole(instanceRole)
                        .withSpaceRole(RoleWithHierarchy.SPACE_ADMIN, unrelatedAdmin)
                        .withSpaceRole(RoleWithHierarchy.SPACE_OBSERVER, unrelatedObserver));

        perform(anUpdateOf(dataset).toSample(destinationSample).as(user));
    }

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createFixture() throws Exception
    {
        sourceSpace = create(aSpace());
        Project sourceProject = create(aProject().inSpace(sourceSpace));
        sourceExperiment = create(anExperiment().inProject(sourceProject));
        sourceSample = create(aSample().inExperiment(sourceExperiment));

        destinationSpace = create(aSpace());
        Project destinationProject = create(aProject().inSpace(destinationSpace));
        destinationExperiment = create(anExperiment().inProject(destinationProject));
        destinationSample = create(aSample().inExperiment(destinationExperiment));

        unrelatedAdmin = create(aSpace());
        unrelatedObserver = create(aSpace());
        unrelatedNone = create(aSpace());
    }

    GuardedDomain source;

    GuardedDomain destination;

    GuardedDomain instance;

    AuthorizationRule assignDataSetToSampleRule;

    @BeforeClass(dependsOnMethods = "loginAsSystem")
    void createAuthorizationRules()
    {
        instance = new InstanceDomain();
        source = new SpaceDomain(instance);
        destination = new SpaceDomain(instance);

        assignDataSetToSampleRule =
                and(rule(source, RoleWithHierarchy.SPACE_POWER_USER),
                        rule(destination, RoleWithHierarchy.SPACE_POWER_USER));
    }

    @DataProvider
    Object[][] rolesAllowedToAssignDataSetToSample()
    {
        return RolePermutator.getAcceptedPermutations(assignDataSetToSampleRule, source,
                destination, instance);
    }

    @DataProvider
    Object[][] rolesNotAllowedToAssignDataSetToSample()
    {
        return RolePermutator.getAcceptedPermutations(not(assignDataSetToSampleRule), source,
                destination, instance);
    }
}
