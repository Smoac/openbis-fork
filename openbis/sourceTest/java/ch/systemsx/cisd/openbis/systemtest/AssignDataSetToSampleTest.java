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

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ch.systemsx.cisd.common.exceptions.AuthorizationFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.DataSetNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityGraphGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.SampleNode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeWithRegistrationAndModificationDate;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.RoleWithHierarchy;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Space;
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
    
    // sourceExperiment -> destinationExperiment
    @Test
    public void dataSetWithoutSampleCanBeAssignedToAnExperiment() throws Exception
    {
        AbstractExternalData dataset = create(aDataSet().inExperiment(sourceExperiment));
        assertThat(dataset, is(inExperiment(sourceExperiment)));
        sourceExperimentChecker.takeModificationDate();
        destinationExperimentChecker.takeModificationDate();

        perform(anUpdateOf(dataset).toExperiment(destinationExperiment));

        sourceExperimentChecker.assertModificationDateChanged();
        destinationExperimentChecker.assertModificationDateChanged();
        assertThat(dataset, is(inExperiment(destinationExperiment)));
    }

    // sourceExperiment -> destinationSample
    @Test
    public void dataSetWithoutSampleCanBeAssignedToSample1() throws Exception
    {
        AbstractExternalData dataset = create(aDataSet().inExperiment(sourceExperiment));
        assertThat(dataset, is(inExperiment(sourceExperiment)));
        sourceExperimentChecker.takeModificationDate();
        destinationSampleChecker.takeModificationDate();
        destinationExperimentChecker.takeModificationDate();
        
        perform(anUpdateOf(dataset).toSample(destinationSample));
        
        sourceExperimentChecker.assertModificationDateChanged();
        destinationSampleChecker.assertModificationDateChanged();
        destinationExperimentChecker.assertModificationDateChanged();
        assertThat(dataset, is(inSample(destinationSample)));
        assertThat(dataset, is(inExperiment(destinationExperiment)));
    }
    
    @Test
    public void dataSetWithoutSampleCanBeAssignedToSample() throws Exception
    {
        EntityGraphGenerator g = parseAndCreateGraph("E1, data sets: DS1\nE2, samples: S2\n");

        reassignToSample(g.ds(1), g.s(2));

        assertEquals("E2, samples: S2, data sets: DS1\nS2, data sets: DS1\n", renderGraph(g));
    }

    private void reassignToSample(DataSetNode dataSetNode, SampleNode sampleNode)
    {
        AbstractExternalData dataSet = repository.getDataSet(dataSetNode);
        Sample sample = repository.getSample(sampleNode);
        reassignToSample(dataSet.getCode(), sample.getPermId());
    }

    protected void reassignToSample(String dataSetCode, String samplePermId)
    {
        SampleIdentifier sampleIdentifier = etlService.tryGetSampleIdentifier(systemSessionToken, samplePermId);
        Sample sample = etlService.tryGetSampleWithExperiment(systemSessionToken, sampleIdentifier);
        perform(anUpdateOf(etlService.tryGetDataSet(systemSessionToken, dataSetCode)).toSample(sample));
    }
    
    // sourceExperiment -> destinationSpaceSampleWithoutExperiment
    @Test
    public void dataSetWithoutSampleCanBeAssignedToSpaceSampleWithoutExperiment() throws Exception
    {
        AbstractExternalData dataset = create(aDataSet().withType("NEXP-TYPE").inExperiment(sourceExperiment));
        sourceExperimentChecker.takeModificationDate();
        destinationSpaceSampleWithoutExperimentChecker.takeModificationDate();
        
        perform(anUpdateOf(dataset).toSample(destinationSpaceSampleWithoutExperiment));
        
        sourceExperimentChecker.assertModificationDateChanged();
        destinationSpaceSampleWithoutExperimentChecker.assertModificationDateChanged();
        assertThat(dataset, is(inSample(destinationSpaceSampleWithoutExperiment)));
        assertThat(dataset, isNot(inExperiment(sourceExperiment)));
    }
    
    // sourceSample -> destinationExperiment
    @Test
    public void dataSetWithSampleAndExperimentCanBeAssignedToAnExperiment() throws Exception
    {
        AbstractExternalData dataset = create(aDataSet().inSample(sourceSample));
        assertThat(dataset, is(inSample(sourceSample)));
        assertThat(dataset, is(inExperiment(sourceExperiment)));
        sourceSampleChecker.takeModificationDate();
        sourceExperimentChecker.takeModificationDate();
        destinationExperimentChecker.takeModificationDate();
        
        perform(anUpdateOf(dataset).toExperiment(destinationExperiment));
        
        sourceSampleChecker.assertModificationDateChanged();
        sourceExperimentChecker.assertModificationDateChanged();
        destinationExperimentChecker.assertModificationDateChanged();
        assertThat(dataset, isNot(inSample(sourceSample)));
        assertThat(dataset, isNot(inExperiment(sourceExperiment)));
        assertThat(dataset, is(inExperiment(destinationExperiment)));
    }
    
    // sourceSample -> destinationSample
    @Test
    public void dataSetIsAssignedWithTheExperimentOfTheNewSample() throws Exception
    {
        AbstractExternalData dataset = create(aDataSet().inSample(sourceSample));
        assertThat(dataset, is(inSample(sourceSample)));
        assertThat(dataset, is(inExperiment(sourceExperiment)));
        sourceSampleChecker.takeModificationDate();
        sourceExperimentChecker.takeModificationDate();
        destinationSampleChecker.takeModificationDate();
        destinationExperimentChecker.takeModificationDate();
        
        perform(anUpdateOf(dataset).toSample(destinationSample));

        sourceSampleChecker.assertModificationDateChanged();
        sourceExperimentChecker.assertModificationDateChanged();
        destinationSampleChecker.assertModificationDateChanged();
        destinationExperimentChecker.assertModificationDateChanged();
        assertThat(dataset, isNot(inSample(sourceSample)));
        assertThat(dataset, isNot(inExperiment(sourceExperiment)));
        assertThat(dataset, is(inSample(destinationSample)));
        assertThat(dataset, is(inExperiment(destinationExperiment)));
    }
    
    // sourceSample -> destinationSpaceSampleWithoutExperiment
    @Test
    public void dataSetWithSampleAndExperimentCanBeAssignedToSpaceSampleWithoutExperiment() throws Exception
    {
        AbstractExternalData dataset = create(aDataSet().withType("NEXP-TYPE").inSample(sourceSample));
        assertThat(dataset, is(inSample(sourceSample)));
        assertThat(dataset, is(inExperiment(sourceExperiment)));
        sourceSampleChecker.takeModificationDate();
        sourceExperimentChecker.takeModificationDate();
        destinationSpaceSampleWithoutExperimentChecker.takeModificationDate();
        
        perform(anUpdateOf(dataset).toSample(destinationSpaceSampleWithoutExperiment));
        
        sourceSampleChecker.assertModificationDateChanged();
        sourceExperimentChecker.assertModificationDateChanged();
        destinationSpaceSampleWithoutExperimentChecker.assertModificationDateChanged();
        assertThat(dataset, isNot(inSample(sourceSample)));
        assertThat(dataset, isNot(inExperiment(sourceExperiment)));
        assertThat(dataset, is(inSample(destinationSpaceSampleWithoutExperiment)));
    }

    // sourceSpaceSampleWithoutExperiment -> destinationExperiment
    @Test
    public void dataSetWithSampleWithoutExperimentCanBeAssignedToExperiment() throws Exception
    {
        AbstractExternalData dataset = create(aDataSet().withType("NEXP-TYPE").inSample(sourceSpaceSampleWithoutExperiment));
        assertThat(dataset, is(inSample(sourceSpaceSampleWithoutExperiment)));
        sourceSpaceSampleWithoutExperimentChecker.takeModificationDate();
        destinationExperimentChecker.takeModificationDate();
        
        perform(anUpdateOf(dataset).toExperiment(destinationExperiment));
        
        sourceSpaceSampleWithoutExperimentChecker.assertModificationDateChanged();
        destinationExperimentChecker.assertModificationDateChanged();
        assertThat(dataset, isNot(inSample(sourceSpaceSampleWithoutExperiment)));
        assertThat(dataset, is(inExperiment(destinationExperiment)));
    }
    
    // sourceSpaceSampleWithoutExperiment -> destinationSample
    @Test
    public void dataSetWithSampleWithoutExperimentCanBeAssignedToSampleWithExperiment() throws Exception
    {
        AbstractExternalData dataset = create(aDataSet().withType("NEXP-TYPE").inSample(sourceSpaceSampleWithoutExperiment));
        assertThat(dataset, is(inSample(sourceSpaceSampleWithoutExperiment)));
        sourceSpaceSampleWithoutExperimentChecker.takeModificationDate();
        destinationExperimentChecker.takeModificationDate();
        destinationExperimentChecker.takeModificationDate();
        
        perform(anUpdateOf(dataset).toSample(destinationSample));
        
        sourceSpaceSampleWithoutExperimentChecker.assertModificationDateChanged();
        destinationSampleChecker.assertModificationDateChanged();
        destinationExperimentChecker.assertModificationDateChanged();
        assertThat(dataset, isNot(inSample(sourceSpaceSampleWithoutExperiment)));
        assertThat(dataset, is(inSample(destinationSample)));
        assertThat(dataset, is(inExperiment(destinationExperiment)));
    }
    
    // sourceSpaceSampleWithoutExperiment -> destinationSpaceSampleWithoutExperiment
    @Test
    public void dataSetWithSampleWithoutExperimentCanBeAssignedToSpaceSampleWithoutExperiment() throws Exception
    {
        AbstractExternalData dataset = create(aDataSet().withType("NEXP-TYPE").inSample(sourceSpaceSampleWithoutExperiment));
        assertThat(dataset, is(inSample(sourceSpaceSampleWithoutExperiment)));
        sourceSpaceSampleWithoutExperimentChecker.takeModificationDate();
        destinationSpaceSampleWithoutExperimentChecker.takeModificationDate();
        
        perform(anUpdateOf(dataset).toSample(destinationSpaceSampleWithoutExperiment));
        
        sourceSpaceSampleWithoutExperimentChecker.assertModificationDateChanged();
        destinationSpaceSampleWithoutExperimentChecker.assertModificationDateChanged();
        assertThat(dataset, isNot(inSample(sourceSpaceSampleWithoutExperiment)));
        assertThat(dataset, is(inSample(destinationSpaceSampleWithoutExperiment)));
        assertThat(dataset, isNot(inExperiment(sourceExperiment)));
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

    Space unrelatedAdmin;

    Space unrelatedObserver;

    Space unrelatedNone;

    private Sample destinationSpaceSampleWithoutExperiment;

    private Sample sourceSpaceSampleWithoutExperiment;

    private SampleModificationDateChecker sourceSpaceSampleWithoutExperimentChecker;

    private ExperimentModificationDateChecker sourceExperimentChecker;

    private ExperimentModificationDateChecker destinationExperimentChecker;

    private SampleModificationDateChecker destinationSpaceSampleWithoutExperimentChecker;

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
        sourceExperimentChecker = new ExperimentModificationDateChecker("source experiment", sourceExperiment);
        sourceSample = create(aSample().inExperiment(sourceExperiment));
        sourceSampleChecker = new SampleModificationDateChecker("source sample", sourceSample);
        sourceSpaceSampleWithoutExperiment = create(aSample().inSpace(sourceSpace));
        sourceSpaceSampleWithoutExperimentChecker = new SampleModificationDateChecker(
                "source sample wo experiment", sourceSpaceSampleWithoutExperiment);

        destinationSpace = create(aSpace());
        Project destinationProject = create(aProject().inSpace(destinationSpace));
        destinationExperiment = create(anExperiment().inProject(destinationProject));
        destinationExperimentChecker = new ExperimentModificationDateChecker(
                "destination experiment", destinationExperiment);
        destinationSample = create(aSample().inExperiment(destinationExperiment));
        destinationSampleChecker = new SampleModificationDateChecker("destination sample", destinationSample);
        destinationSpaceSampleWithoutExperiment = create(aSample().inSpace(destinationSpace));
        destinationSpaceSampleWithoutExperimentChecker = new SampleModificationDateChecker(
                "destination sample wo experiment", destinationSpaceSampleWithoutExperiment);

        unrelatedAdmin = create(aSpace());
        unrelatedObserver = create(aSpace());
        unrelatedNone = create(aSpace());
    }

    GuardedDomain source;

    GuardedDomain destination;

    GuardedDomain instance;

    AuthorizationRule assignDataSetToSampleRule;

    private SampleModificationDateChecker destinationSampleChecker;

    private SampleModificationDateChecker sourceSampleChecker;

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
    private abstract static class AbstractModificationDateChecker<T>
    {
        private final String name;
        protected final T entity;
        private long time;

        AbstractModificationDateChecker(String name, T entity)
        {
            this.name = name;
            this.entity = entity;
            
        }
        
        void takeModificationDate()
        {
            time = refresh().getModificationDate().getTime();
        }
        
        void assertModificationDateChanged()
        {
            long time2 = refresh().getModificationDate().getTime();
            if (time2 == time)
            {
                AssertJUnit.fail("Modification date of " + name + " didn't changed.");
            }
        }
        
        abstract CodeWithRegistrationAndModificationDate<?> refresh();
    }
    
    private class SampleModificationDateChecker extends AbstractModificationDateChecker<Sample>
    {
        SampleModificationDateChecker(String name, Sample entity)
        {
            super(name, entity);
        }

        @Override
        CodeWithRegistrationAndModificationDate<?> refresh()
        {
            return AssignDataSetToSampleTest.this.refresh(entity);
        }
        
    }
    
    private class ExperimentModificationDateChecker extends AbstractModificationDateChecker<Experiment>
    {
        ExperimentModificationDateChecker(String name, Experiment entity)
        {
            super(name, entity);
        }
        
        @Override
        CodeWithRegistrationAndModificationDate<?> refresh()
        {
            return AssignDataSetToSampleTest.this.refresh(entity);
        }
        
    }
    
}
