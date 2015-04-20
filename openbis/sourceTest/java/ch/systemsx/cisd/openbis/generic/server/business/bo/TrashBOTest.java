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

package ch.systemsx.cisd.openbis.generic.server.business.bo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.internal.NamedSequence;
import org.jmock.lib.action.ReturnValueAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.DataSetNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityGraphGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.EntityNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.ExperimentNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.SampleNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.entitygraph.Utils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ScriptPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.properties.EntityKind;

/**
 * Test cases for {@link TrashBO}.
 * 
 * @author Piotr Buczek
 */
@Friend(toClasses =
{ ScriptBO.class, ScriptBO.IScriptFactory.class, ScriptPE.class })
public final class TrashBOTest extends AbstractBOTest
{

    private static String EXAMPLE_REASON = "example reason";

    private static Long COMPONENT_CONTAINER_RELATIONSHIP_ID = 1001L;
    private static Long CHILDREN_PARENT_RELATIONSHIP_ID = 1002L;

    private static TechId EXAMPLE_ID = new TechId(1L);

    private static List<TechId> EXAMPLE_ID_LIST = TechId.createList(1, 2, 3);

    private ITrashBO trashBO;

    private ICommonBusinessObjectFactory boFactory;

    private IDataSetTable dataSetTable;

    private int dataSetTableSequenceId;

    @Override
    @BeforeMethod
    public void beforeMethod()
    {
        super.beforeMethod();
        boFactory = context.mock(ICommonBusinessObjectFactory.class);
        dataSetTable = context.mock(IDataSetTable.class);
        trashBO =
                new TrashBO(daoFactory, boFactory, ManagerTestTool.EXAMPLE_SESSION,
                        managedPropertyEvaluatorFactory, null, null);
        context.checking(new Expectations()
            {
                {
                    allowing(boFactory).createDataSetTable(ManagerTestTool.EXAMPLE_SESSION);
                    will(returnValue(dataSetTable));

                    allowing(relationshipTypeDAO).tryFindRelationshipTypeByCode(BasicConstant.CONTAINER_COMPONENT_INTERNAL_RELATIONSHIP);
                    RelationshipTypePE type = new RelationshipTypePE();
                    type.setId(COMPONENT_CONTAINER_RELATIONSHIP_ID);
                    will(returnValue(type));

                    allowing(relationshipTypeDAO).tryFindRelationshipTypeByCode(BasicConstant.PARENT_CHILD_INTERNAL_RELATIONSHIP);
                    type = new RelationshipTypePE();
                    type.setId(CHILDREN_PARENT_RELATIONSHIP_ID);
                    will(returnValue(type));
                    
                    allowing(boFactory).createSampleLister(ManagerTestTool.EXAMPLE_SESSION);
                    will(returnValue(sampleLister));
                    
                    allowing(boFactory).createDatasetLister(ManagerTestTool.EXAMPLE_SESSION);
                    will(returnValue(datasetLister));
                }
            });
    }

    private DeletionPE createDeletion()
    {
        return createDeletion(EXAMPLE_REASON);
    }

    private DeletionPE createDeletion(final String reason)
    {
        final RecordingMatcher<DeletionPE> deletionMatcher = new RecordingMatcher<DeletionPE>();
        context.checking(new Expectations()
            {
                {
                    one(deletionDAO).create(with(deletionMatcher));
                }
            });
        trashBO.createDeletion(reason);
        return deletionMatcher.recordedObject();
    }

    @Test
    public final void testCreateDeletion()
    {
        final String reason = EXAMPLE_REASON;
        DeletionPE deletionPE = createDeletion(reason);
        assertEquals(reason, deletionPE.getReason());
        assertEquals(ManagerTestTool.EXAMPLE_SESSION.tryGetPerson(), deletionPE.getRegistrator());

        context.assertIsSatisfied();
    }

    @Test
    public final void testRevertDeletion()
    {
        final TechId deletionId = EXAMPLE_ID;
        context.checking(new Expectations()
            {
                {
                    DeletionPE dummyDeletion = new DeletionPE();

                    one(deletionDAO).getByTechId(deletionId);
                    will(returnValue(dummyDeletion));

                    one(deletionDAO).revert(dummyDeletion,
                            ManagerTestTool.EXAMPLE_SESSION.tryGetPerson());
                }
            });
        trashBO.revertDeletion(deletionId);

        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = AssertionError.class)
    public final void testTrashSamplesFailsWithNoDeletion()
    {
        trashBO.trashSamples(EXAMPLE_ID_LIST);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = AssertionError.class)
    public final void testTrashExperimentsFailsWithNoDeletion()
    {
        trashBO.trashExperiments(EXAMPLE_ID_LIST);
        context.assertIsSatisfied();
    }

    @Test(expectedExceptions = AssertionError.class)
    public final void testTrashDataSetsFailsWithNoDeletion()
    {
        trashBO.trashDataSets(EXAMPLE_ID_LIST);
        context.assertIsSatisfied();
    }

    @Test
    public final void testTrashExperimentsAlreadyTrashed()
    {
        final DeletionPE deletion = createDeletion();
        final List<TechId> experimentIds = EXAMPLE_ID_LIST;
        context.checking(new Expectations()
            {
                {
                    one(deletionDAO).trash(EntityKind.EXPERIMENT, experimentIds, deletion, true);
                    will(returnValue(0));
                }
            });
        trashBO.trashExperiments(experimentIds);
        context.assertIsSatisfied();
    }

    @Test
    public final void testTrashExperimentsWithOneLevelOfDependencies()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("E1, samples: S50, data sets: DS60 DS61\n"
                + "E2, samples: S51\n"
                + "E3\n"
                );
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashExperiments(deletion, true, g.e(1), g.e(2), g.e(3));
        prepareTrashSamples(deletion, false, g.s(50), g.s(51));
        prepareTrashDataSets(deletion, false, g.ds(60), g.ds(61));
        
        trashBO.trashExperiments(asIds(g.e(1), g.e(2), g.e(3)));

        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashExperimentsWithDeepDependencies()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("E1, samples: S50, data sets: DS60 DS61\n"
                + "E2, samples: S51\n"
                + "E3\n"
                + "S50, components: S52, data sets: DS70 DS71\n"
                + "S51, components: S53\n"
                );
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashExperiments(deletion, true, g.e(1), g.e(2), g.e(3));
        prepareTrashSamples(deletion, false, g.s(50), g.s(51));
        prepareTrashSamples(deletion, false, g.s(52), g.s(53));
        prepareTrashDataSets(deletion, false, g.ds(60), g.ds(61), g.ds(70), g.ds(71));

        trashBO.trashExperiments(asIds(g.e(1), g.e(2), g.e(3)));

        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashExperimentWithSamplesAndDataSetsAndNoExternalLinks()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("E1, samples: S1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n");
        ExperimentNode e1 = g.e(1);
        SampleNode s1 = g.s(1);
        ExperimentNode e2 = g.e(2);
        DataSetNode ds1 = g.ds(1);
        DataSetNode ds2 = g.ds(2);
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashExperiments(deletion, true, e1, e2);
        prepareTrashSamples(deletion, false, s1);
        prepareTrashDataSets(deletion, false, ds1, ds2);

        trashBO.trashExperiments(asIds(e1, e2));

        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashExperimentWithARelatedDataSetComponentWhichBelongsToAnExternalExperiment()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("E1, samples: S1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n");
        ExperimentNode e1 = g.e(1);
        SampleNode s1 = g.s(1);
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashExperiments(deletion, true, e1);
        prepareTrashSamples(deletion, false, s1);
        prepareTrashDataSets(deletion, false, g.ds(1));
        
        trashBO.trashExperiments(asIds(e1));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashExperimentWithARelatedDataSetInAnExternalContainer()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("E1, samples: S1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "S1, data sets: DS1\n"
                + "DS1, components: DS2\n");
        ExperimentNode e2 = g.e(2);
        DataSetNode ds2 = g.ds(2);
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashExperiments(deletion, true, e2);
        
        failTrashExperiment(e2, ds2, g.ds(1), g.s(1));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testTrashPublishedExperiment()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("E1, data sets: DS1 DS2 DS3 DS4\n"
                + "DS1, components: DS3\n"
                + "DS2, children: DS4\n"
                + "E2, data sets: DS5 DS6\n"
                + "DS5, components: DS1\n"
                + "DS6, components: DS2\n");
        ExperimentNode e2 = g.e(2); // published experiment
        DataSetNode ds5 = g.ds(5);
        DataSetNode ds6 = g.ds(6);
        prepareEntityGraph(g);
        DeletionPE deletion = createDeletion();
        prepareTrashExperiments(deletion, true, e2);
        prepareTrashDataSets(deletion, false, ds5 , ds6);
        
        trashBO.trashExperiments(asIds(e2));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashPublishedExperimentWithOrginalExperimentWithSamples()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("E1, samples: S1\n"
                + "S1, data sets: DS1 DS2\n"
                + "DS1, components: DS2\n"
                + "E2, data sets: DS3\n"
                + "DS3, components: DS1");
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashExperiments(deletion, true, g.e(2));
        prepareTrashDataSets(deletion, false, g.ds(3));
        
        trashBO.trashExperiments(asIds(g.e(2)));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashOrginalExperimentWithSample()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("E1, samples: S1\n"
                + "S1, data sets: DS1 DS2\n"
                + "DS1, components: DS2\n"
                + "E2, data sets: DS3\n"
                + "DS3, components: DS1");
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashExperiments(deletion, true, g.e(1));
        prepareTrashSamples(deletion, false, g.s(1));
        
        failTrashExperiment(g.e(1), g.ds(1), g.ds(3), g.e(2));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashExperimentsWithContainerDataSetWithPhysicalDataSetFromAnotherExperiment()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("E1, data sets: DS1\n"
                + "E2, data sets: DS2\n"
                + "DS1, components: DS2\n"
                + "DS2, containers: DS1");
        prepareEntityGraph(g);
        DeletionPE deletion = createDeletion();
        prepareTrashExperiments(deletion, true, g.e(1));
        prepareTrashDataSets(deletion, false, g.ds(1));
        
        trashBO.trashExperiments(asIds(g.e(1)));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashSamplesAlreadyTrashed()
    {
        final EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("S1\nS2\n");
        prepareEntityGraph(g);

        final DeletionPE deletion = createDeletion();
        context.checking(new Expectations()
            {
                {
                    one(deletionDAO).trash(EntityKind.SAMPLE, asIds(g.s(1), g.s(2)), deletion, true);
                    will(returnValue(0));
                }
            });
        trashBO.trashSamples(asIds(g.s(1), g.s(2)));
        context.assertIsSatisfied();
    }

    @Test
    public final void testTrashSamplesWithOneLevelOfDependencies()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("S1, components: S20\n"
                + "S3, data sets: DS60\n"
                + "S20, data sets: DS61\n");
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashSamples(deletion, true, g.s(1), g.s(3));
        prepareTrashSamples(deletion, false, g.s(20));
        prepareTrashDataSets(deletion, false, g.ds(60), g.ds(61));
        
        trashBO.trashSamples(asIds(g.s(1), g.s(3)));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashSampleWithTwoLevelOfDependencies()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("S1, components: S2, data sets: DS1\n"
                + "S2, components: S3, data sets: DS2\n"
                + "S3, data sets: DS3\n");
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashSamples(deletion, true, g.s(1));
        prepareTrashSamples(deletion, false, g.s(2));
        prepareTrashDataSets(deletion, false, g.ds(1), g.ds(2));
        
        trashBO.trashSamples(asIds(g.s(1)));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashSampleWithAComponentWithAContainerDataSetWithAComponentDataSetOfFirstSample()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("S1, components: S2, data sets: DS1\n"
                + "S2, data sets: DS2\n"
                + "DS2, components: DS1\n");
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashSamples(deletion, true, g.s(1));
        prepareTrashSamples(deletion, false, g.s(2));
        prepareTrashDataSets(deletion, false, g.ds(1), g.ds(2));
        
        trashBO.trashSamples(asIds(g.s(1)));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashSampleWithAComponentWithAComponentDataSetOfAContainerDataSetOfFirstSample()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("S1, components: S2, data sets: DS1\n"
                + "S2, data sets: DS2\n"
                + "DS1, components: DS2\n");
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashSamples(deletion, true, g.s(1));
        prepareTrashSamples(deletion, false, g.s(2));
        prepareTrashDataSets(deletion, false, g.ds(1), g.ds(2));
        
        trashBO.trashSamples(asIds(g.s(1)));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashSampleWithAnExperimentSampleWithADataSet()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("E1, samples: S2\n"
                + "S1, components: S2\n"
                + "S2, data sets: DS1\n");
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashSamples(deletion, true, g.s(1));
        prepareTrashSamples(deletion, false, g.s(2));
        
        failTrashSample(g.s(1), g.s(2), g.e(1));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashSampleWithComponentWithDataSetWithComponentOfAnotherSample()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("S1, components: S2\n"
                + "S2, data sets: DS2\n"
                + "S3, data sets: DS1\n"
                + "DS1, components: DS2\n");
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashSamples(deletion, true, g.s(1));
        prepareTrashSamples(deletion, false, g.s(2));

        failTrashSample(g.s(1), g.ds(2), g.ds(1), g.s(3));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashSampleWithComponentWithDataSetWithComponentOfAnotherExperiment()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("E1, data sets: DS1\n"
                + "S1, components: S2\n"
                + "S2, data sets: DS2\n"
                + "DS1, components: DS2\n");
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashSamples(deletion, true, g.s(1));
        prepareTrashSamples(deletion, false, g.s(2));
        
        failTrashSample(g.s(1), g.ds(2), g.ds(1), g.e(1));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashDataSets()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("S1, data sets: DS1 DS2 DS3 DS4 DS5 DS6\n"
                + "DS1, components: DS5\n"
                + "DS2, components: DS6\n");
        prepareEntityGraph(g);
        DeletionPE deletion = createDeletion();
        prepareTrashDataSets(deletion, true, g.ds(1), g.ds(2), g.ds(3));
        prepareTrashDataSets(deletion, false, g.ds(5), g.ds(6));
        
        trashBO.trashDataSets(asIds(g.ds(1), g.ds(2), g.ds(3)));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testTrashDataSetsWithDataSetInAContainer()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("S1, data sets: DS1 DS2 DS3\n"
                + "DS1, components: DS2\n"
                + "DS2, components: DS3\n");
        prepareEntityGraph(g);
        DeletionPE deletion = createDeletion();
        prepareTrashDataSets(deletion, true, g.ds(2));
        prepareTrashDataSets(deletion, false, g.ds(3));
        
        trashBO.trashDataSets(asIds(g.ds(2)));
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testTrashDataSetsWithDataSetComponentIndirectlyDependentOnOutsideContainer()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("S1, data sets: DS1 DS2 DS3 DS4 DS5 DS6\n"
                + "DS1, components: DS3 DS4 DS5\n"
                + "DS2, components: DS4\n"
                + "DS4, components: DS5 DS6\n");
        prepareEntityGraph(g);
        DeletionPE deletion = createDeletion();
        prepareTrashDataSets(deletion, true, g.ds(1));
        prepareTrashDataSets(deletion, false, g.ds(3));

        trashBO.trashDataSets(asIds(g.ds(1)));

        context.assertIsSatisfied();
    }
    
    @Test
    public void testTrashSampleWithDataSetWithDataSetComponentIndirectlyDependentOnOutsideContainer()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("S1, data sets: DS1 DS3 DS4 DS5 DS6\n"
                + "S2, data sets: DS2\n"
                + "DS1, components: DS3 DS4 DS5\n"
                + "DS2, components: DS4\n"
                + "DS4, components: DS5 DS6\n");
        prepareEntityGraph(g);
        DeletionPE deletion = createDeletion();
        prepareTrashSamples(deletion, true, g.s(1));

        failTrashSample(g.s(1), g.ds(4), g.ds(2), g.s(2));
        
        context.assertIsSatisfied();
    }

    @Test
    public final void testTrashUnavailableDataSets()
    {
        EntityGraphGenerator g = new EntityGraphGenerator();
        g.parse("S1, data sets: DS1\n");
        prepareEntityGraph(g);
        g.ds(1).nonDeletable();
        createDeletion();

        try
        {
            trashBO.trashDataSets(asIds(g.ds(1)));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals(
                    "Deletion not possible because the following data sets are not deletable:\n"
                            + " Status: ARCHIVE_PENDING, data sets: [DS1]", ex.getMessage());
        }

        context.assertIsSatisfied();
    }
    
    private void prepareTrashExperiments(DeletionPE deletion, boolean isOriginalDeletion, 
            ExperimentNode... experiments)
    {
        prepareTrash(deletion, EntityKind.EXPERIMENT, isOriginalDeletion, asIds(experiments));
    }

    private void prepareTrashSamples(DeletionPE deletion, boolean isOriginalDeletion, 
            SampleNode... samples)
    {
        prepareTrash(deletion, EntityKind.SAMPLE, isOriginalDeletion, asIds(samples));
    }
    
    private void prepareTrashDataSets(DeletionPE deletion, boolean isOriginalDeletion, 
            DataSetNode... dataSets)
    {
        prepareTrash(deletion, EntityKind.DATA_SET, isOriginalDeletion, asIds(dataSets));
    }
    
    private void prepareTrash(final DeletionPE deletion, final EntityKind entityKind, 
            final boolean isOriginalDeletion, final List<TechId> entityIds)
    {
        context.checking(new Expectations()
            {
                {
                    one(deletionDAO).trash(entityKind, entityIds, deletion, isOriginalDeletion);
                    will(returnValue(entityIds.size()));
                }
            });
    }
    
    private void failTrashExperiment(ExperimentNode experimentNode, DataSetNode originalDataSet,
            DataSetNode relatedDataSet, EntityNode outsiderNode)
    {
        try
        {
            trashBO.trashExperiments(asIds(experimentNode));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertExceptionMessage(originalDataSet, relatedDataSet, outsiderNode, ex);
        }
    }
    
    private void failTrashSample(SampleNode sampleNode, SampleNode relatedSample, EntityNode outsiderNode)
    {
        try
        {
            trashBO.trashSamples(asIds(sampleNode));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            String outsiderType = outsiderNode instanceof ExperimentNode ? "experiment" : "sample";
            assertEquals("The sample " + relatedSample.getCode() + " belongs to " + outsiderType + " " 
                    + outsiderNode.getCode() + " is outside the deletion set.", ex.getMessage());
        }
    }

    private void failTrashSample(SampleNode sampleNode, DataSetNode originalDataSet,
            DataSetNode relatedDataSet, EntityNode outsiderNode)
    {
        try
        {
            trashBO.trashSamples(asIds(sampleNode));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertExceptionMessage(originalDataSet, relatedDataSet, outsiderNode, ex);
        }
    }
    
    private void assertExceptionMessage(DataSetNode originalDataSet, DataSetNode relatedDataSet, EntityNode outsiderNode, UserFailureException ex)
    {
        String outsiderType = outsiderNode instanceof ExperimentNode ? "experiment" : "sample";
        assertEquals("The data set " + originalDataSet.getCode() + " is a component of the data set " 
                + relatedDataSet.getCode() + " which belongs to " + outsiderType + " " 
                + outsiderNode.getCode() + " outside the deletion set.", ex.getMessage());
    }

    private void prepareEntityGraph(EntityGraphGenerator g)
    {
        g.assertConsistency();
        prepareListSampleIdsByExperimentIds(g);
        prepareListDataSetIdsByExperimentIds(g);
        prepareListDataSetIdsBySampleIds(g);
        prepareGetByTechId(g);
        prepareFindChildrenOrComponentIds(g);
        prepareListSamples(g);
        prepareListByDataSetIds(g);
        prepareListDataSetContainerIds(g);
        prepareListDataSetComponentIds(g);
        prepareGetDataSetsAndNonDeletableDataSets(g);
        prepareListSampleIdsByContainerIds(g);
    }

    private void prepareListSampleIdsByExperimentIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<List<TechId>> handler = new AbstractMockHandler<List<TechId>>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    List<TechId> ids = g.getSampleIdsByExperimentIds(argument);
                    print("listSampleIdsByExperimentIds(" + argument + ") = " + ids);
                    return ids;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(sampleDAO).listSampleIdsByExperimentIds(with(handler));
                    will(handler);
                }
            });
    }

    private void prepareListSampleIdsByContainerIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<Collection<TechId>> handler = new AbstractMockHandler<Collection<TechId>>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    List<TechId> ids = g.getSampleIdsByContainerIds(argument);
                    print("listSampleIdsByContainerIds(" + argument + ") = " + ids);
                    return ids;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(sampleDAO).listSampleIdsByContainerIds(with(handler));
                    will(handler);
                }
            });
    }
    
    private void prepareListDataSetIdsByExperimentIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<List<TechId>> handler = new AbstractMockHandler<List<TechId>>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    List<TechId> ids = g.getDataSetIdsByExperimentIds(argument);
                    print("listDataSetIdsByExperimentIds(" + argument + ") = " + ids);
                    return ids;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(dataDAO).listDataSetIdsByExperimentIds(with(handler));
                    will(handler);
                }
            });
    }
    
    private void prepareListDataSetIdsBySampleIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<Collection<TechId>> handler = new AbstractMockHandler<Collection<TechId>>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    List<TechId> ids = g.getDataSetIdsBySampleIds(argument);
                    print("listDataSetIdsBySampleIds(" + argument + ") = " + ids);
                    return ids;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(dataDAO).listDataSetIdsBySampleIds(with(handler));
                    will(handler);
                }
            });
    }
    
    private void prepareGetByTechId(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<TechId> handler = new AbstractMockHandler<TechId>()
                {
            @Override
            public Object invoke(Invocation invocation) throws Throwable
            {
                DataSetNode dataSetNode = g.getDataSets().get(argument.getId());
                ExternalDataPE dataSet = Utils.createData(dataSetNode);
                print("getByTechId(" + argument + ") = " + dataSet);
                return dataSet;
            }
                };
                context.checking(new Expectations()
                {
                    {
                        allowing(dataDAO).getByTechId(with(handler));
                        will(handler);
                    }
                });
    }
    
    private void prepareFindChildrenOrComponentIds(final EntityGraphGenerator g)
    {
        class FindChildrenIdsMockHandler
        {
            private Collection<TechId> ids;

            private long relationshipTypeId;

            Matcher<Collection<TechId>> getIdsMatcher()
            {
                return new BaseMatcher<Collection<TechId>>()
                    {
                        @SuppressWarnings("unchecked")
                        @Override
                        public boolean matches(Object arg0)
                        {
                            ids = (Collection<TechId>) arg0;
                            return true;
                        }

                        @Override
                        public void describeTo(Description description)
                        {
                            description.appendValue(ids);
                        }
                    };
            }

            Matcher<Long> getTypeMatcher()
            {
                return new BaseMatcher<Long>()
                    {
                        @Override
                        public boolean matches(Object arg0)
                        {
                            relationshipTypeId = (Long) arg0;
                            return true;
                        }

                        @Override
                        public void describeTo(Description description)
                        {
                            description.appendValue(relationshipTypeId);
                        }
                    };
            }

            Action getReturnAction()
            {
                return new Action()
                    {

                        @Override
                        public Object invoke(Invocation invocation) throws Throwable
                        {
                            String methodName;
                            List<TechId> dataSetIds;
                            if (relationshipTypeId == CHILDREN_PARENT_RELATIONSHIP_ID)
                            {
                                methodName = "getChildrenDataSetIdsByDataSetIds";
                                dataSetIds = g.getChildrenDataSetIdsByDataSetIds(ids);
                            } else if (relationshipTypeId == COMPONENT_CONTAINER_RELATIONSHIP_ID)
                            {
                                methodName = "getComponentDataSetIdsByDataSetIds";
                                dataSetIds = g.getComponentDataSetIdsByDataSetIds(ids);
                            } else
                            {
                                throw new AssertionError("Unknown relationship id: " + relationshipTypeId);
                            }
                            print(methodName + "(" + ids + ") = " + dataSetIds);
                            return new LinkedHashSet<TechId>(dataSetIds);
                        }

                        @Override
                        public void describeTo(Description arg0)
                        {
                        }
                    };
            }
        }
        final FindChildrenIdsMockHandler handler = new FindChildrenIdsMockHandler();
        context.checking(new Expectations()
            {
                {
                    allowing(dataDAO).findChildrenIds(with(handler.getIdsMatcher()), with(handler.getTypeMatcher()));
                    will(handler.getReturnAction());
                }
            });
    }

    private void prepareListSamples(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<ListOrSearchSampleCriteria> handler = new AbstractMockHandler<ListOrSearchSampleCriteria>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    List<Sample> samples = new ArrayList<Sample>();
                    Map<Long, SampleNode> sampleNodes = g.getSamples();
                    for (Long id : argument.getSampleIds())
                    {
                        SampleNode sampleNode = sampleNodes.get(id);
                        samples.add(Utils.createSample(sampleNode));
                    }
                    print("list(" + argument + ") = " + samples);
                    return samples;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(sampleLister).list(with(handler));
                    will(handler);
                }
            });
    }
    
    private void prepareListByDataSetIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<Collection<Long>> handler = new AbstractMockHandler<Collection<Long>>()
                {
            @Override
            public Object invoke(Invocation invocation) throws Throwable
            {
                List<AbstractExternalData> dataSets = new ArrayList<AbstractExternalData>();
                Map<Long, DataSetNode> dataSetNodes = g.getDataSets();
                for (Long id : argument)
                {
                    DataSetNode dataSetNode = dataSetNodes.get(id);
                    dataSets.add(Utils.createExternalData(dataSetNode));
                }
                print("listByDataSetIds(" + argument + ") = " + dataSets);
                return dataSets;
            }
                };
                context.checking(new Expectations()
                {
                    {
                        allowing(datasetLister).listByDatasetIds(with(handler), with(TrashBO.DATA_SET_FETCH_OPTIONS));
                        will(handler);
                    }
                });
    }

    private void prepareListDataSetContainerIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<Collection<Long>> handler = new AbstractMockHandler<Collection<Long>>()
                {
            @Override
            public Object invoke(Invocation invocation) throws Throwable
            {
                Map<Long, Set<Long>> idsMap = g.getContainerDataSetIdsMap(
                        TechId.createList(new ArrayList<Long>(argument)));
                print("listContainerIds(" + argument + ") = " + idsMap);
                return idsMap;
            }
                };
                context.checking(new Expectations()
                {
                    {
                        allowing(datasetLister).listContainerIds(with(handler));
                        will(handler);
                    }
                });
    }
    
    private void prepareListDataSetComponentIds(final EntityGraphGenerator g)
    {
        final AbstractMockHandler<Collection<Long>> handler = new AbstractMockHandler<Collection<Long>>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    Map<Long, Set<Long>> idsMap = g.getComponentDataSetIdsMap(
                            TechId.createList(new ArrayList<Long>(argument)));
                    print("listComponentIds(" + argument + ") = " + idsMap);
                    return idsMap;
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(datasetLister).listComponetIds(with(handler));
                    will(handler);
                }
            });
    }
    
    private void prepareGetDataSetsAndNonDeletableDataSets(final EntityGraphGenerator g)
    {
        class DataSetTableMockHandler extends BaseMatcher<List<TechId>>
        {
            private List<DataPE> dataSets = new ArrayList<DataPE>();
            private List<ExternalDataPE> nonDeletableDataSets = new ArrayList<ExternalDataPE>();

            @Override
            public void describeTo(Description description)
            {
                description.appendText("<" + dataSets + ">");
            }

            @SuppressWarnings("unchecked")
            @Override
            public boolean matches(Object obj)
            {
                print("DataSetTable.loadByIds(" + obj + ")");
                dataSets.clear();
                nonDeletableDataSets.clear();
                Map<Long, DataSetNode> dataSetNodes = g.getDataSets();
                for (TechId id : (List<TechId>) obj)
                {
                    DataSetNode dataSetNode = dataSetNodes.get(id.getId());
                    if (dataSetNode != null)
                    {
                        ExternalDataPE data = Utils.createData(dataSetNode);
                        dataSets.add(data);
                        if (dataSetNode.isDeletable() == false)
                        {
                            nonDeletableDataSets.add(data);
                        }
                    }
                }
                return true;
            }
            
        }

        final DataSetTableMockHandler handler = new DataSetTableMockHandler();
        context.checking(new Expectations()
            {
                {
                    NamedSequence dataSetTableSequence = new NamedSequence("DATA SET TABLE " + dataSetTableSequenceId++);
                    allowing(dataSetTable).loadByIds(with(handler));
                    inSequence(dataSetTableSequence);

                    allowing(dataSetTable).getNonDeletableExternalDataSets();
                    will(new ReturnValueAction(handler.nonDeletableDataSets));
                    inSequence(dataSetTableSequence);

                    allowing(dataSetTable).getDataSets();
                    will(new ReturnValueAction(handler.dataSets));
                    inSequence(dataSetTableSequence);
                }
            });
    }
    
    private static abstract class AbstractMockHandler<T> extends BaseMatcher<T> implements Action
    {
        protected T argument;

        @Override
        public void describeTo(Description description)
        {
            description.appendText("<" + argument + ">");
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean matches(Object obj)
        {
            argument = (T) obj;
            return true;
        }
    }
    
    private List<TechId> asIds(IIdHolder...entities)
    {
        return TechId.createList(Arrays.asList(entities));
    }
    
    private static final void print(Object message)
    {
        String methodName = null;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace)
        {
            if (stackTraceElement.getClassName().equals(TrashBOTest.class.getName()))
            {
                methodName = stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName();
            }
        }
        System.out.println(methodName + ": " + message);
    }
    
}