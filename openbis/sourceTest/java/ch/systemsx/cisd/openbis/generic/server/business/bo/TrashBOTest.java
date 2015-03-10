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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
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
import ch.systemsx.cisd.openbis.generic.server.business.bo.trashtesthelper.DataSetNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.trashtesthelper.EntityNodeGenerator;
import ch.systemsx.cisd.openbis.generic.server.business.bo.trashtesthelper.ExperimentNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.trashtesthelper.SampleNode;
import ch.systemsx.cisd.openbis.generic.server.business.bo.trashtesthelper.Utils;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.RelationshipTypePE;
import ch.systemsx.cisd.openbis.generic.shared.dto.SamplePE;
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

    private static Long COMPONENT_CONTAINER_RELATIONSHIP_ID = 1234L;
    private static Long CHILDREN_PARENT_RELATIONSHIP_ID = 1235L;

    private static TechId EXAMPLE_ID = new TechId(1L);

    private static List<TechId> EXAMPLE_ID_LIST = TechId.createList(1, 2, 3);

    private ITrashBO trashBO;

    private ICommonBusinessObjectFactory boFactory;

    private IDataSetTable dataSetTable;

    private NamedSequence dataSetTableSequence;

    @Override
    @BeforeMethod
    public void beforeMethod()
    {
        super.beforeMethod();
        boFactory = context.mock(ICommonBusinessObjectFactory.class);
        dataSetTable = context.mock(IDataSetTable.class);
        trashBO =
                new TrashBO(daoFactory, boFactory, ManagerTestTool.EXAMPLE_SESSION,
                        managedPropertyEvaluatorFactory);
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
                    
                    allowing(boFactory).createDatasetLister(ManagerTestTool.EXAMPLE_SESSION);
                    will(returnValue(datasetLister));
                }
            });
        dataSetTableSequence = new NamedSequence("DATA SET TABLE");

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
        final DeletionPE deletion = createDeletion();
        final List<TechId> experimentIds = EXAMPLE_ID_LIST;
        final List<TechId> sampleIds = TechId.createList(50, 51);
        final List<TechId> dataSetIds = TechId.createList(60, 61);
        prepareListSampleIdsByExperimentIds(experimentIds, sampleIds);
        prepareListDataSetIdsByExperimentIds(experimentIds, dataSetIds);
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).listSampleIdsByContainerIds(sampleIds);
                    will(returnValue(Arrays.asList()));

                    one(dataDAO).listDataSetIdsBySampleIds(sampleIds);
                    will(returnValue(Arrays.asList()));
                }
            });
        prepareFindDataSetComponentIds(dataSetIds, Arrays.<Long> asList());
        prepareListDataSetContainerIds(dataSetIds, new LinkedHashMap<Long, Set<Long>>());
        ExperimentPE experiment = new ExperimentPE();
        experiment.setId(experimentIds.get(0).getId());
        prepareGetDataSetsAndNonDeletableDataSets(dataSetIds, experiment);
        prepareTrash(deletion, EntityKind.EXPERIMENT, true, experimentIds);
        prepareTrash(deletion, EntityKind.SAMPLE, false, sampleIds);
        prepareTrash(deletion, EntityKind.DATA_SET, false, dataSetIds);

        trashBO.trashExperiments(experimentIds);

        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashExperimentsWithDeepDependencies()
    {
        final DeletionPE deletion = createDeletion();
        final List<TechId> experimentIds = EXAMPLE_ID_LIST;
        final List<TechId> sampleIds = TechId.createList(50, 51);
        final List<TechId> componentSampleIds = TechId.createList(52, 53);
        final List<TechId> dataSetIds = TechId.createList(60, 61);
        prepareListSampleIdsByExperimentIds(experimentIds, sampleIds);
        prepareListDataSetIdsByExperimentIds(experimentIds, dataSetIds);
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).listSampleIdsByContainerIds(sampleIds);
                    will(returnValue(componentSampleIds));
                }
            });
        prepareTrash(deletion, EntityKind.EXPERIMENT, true, experimentIds);
        prepareTrash(deletion, EntityKind.SAMPLE, false, sampleIds);
        prepareTrash(deletion, EntityKind.SAMPLE, false, componentSampleIds);
        prepareFindDataSetComponentIds(dataSetIds, Arrays.<Long> asList());
        prepareListDataSetContainerIds(dataSetIds, new LinkedHashMap<Long, Set<Long>>());
        ExperimentPE experiment = new ExperimentPE();
        experiment.setId(experimentIds.get(0).getId());
        prepareGetDataSetsAndNonDeletableDataSets(dataSetIds, experiment);
        prepareTrash(deletion, EntityKind.DATA_SET, false, dataSetIds);

        List<TechId> sampleDataSets = TechId.createList(70L, 71L);
        prepareListDataSetIdsBySampleIds(sampleIds, sampleDataSets);
        prepareFindDataSetComponentIds(sampleDataSets, Arrays.<Long> asList());
        prepareListDataSetContainerIds(sampleDataSets, new LinkedHashMap<Long, Set<Long>>());
        SamplePE sample = new SamplePE();
        sample.setId(sampleIds.get(0).getId());
        prepareGetDataSetsAndNonDeletableDataSets(sampleDataSets, sample);
        prepareTrash(deletion, EntityKind.DATA_SET, false, sampleDataSets);

        prepareListDataSetIdsBySampleIds(componentSampleIds, TechId.createList());

        trashBO.trashExperiments(experimentIds);

        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashExperimentWithSamplesAndDataSetsAndNoExternalLinks()
    {
        EntityNodeGenerator g = new EntityNodeGenerator();
        g.parse("E1, samples: [S1], data sets: [DS1]\n"
                + "E2, data sets: [DS2]\n"
                + "S1, experiment: E1, data sets: [DS1]\n"
                + "DS1, experiment: E1, sample: S1, components: [DS2]\n"
                + "DS2, experiment: E2, containers: [DS1]\n");
        ExperimentNode e1 = g.e(1);
        SampleNode s1 = g.s(1);
        ExperimentNode e2 = g.e(2);
        DataSetNode ds1 = g.ds(1);
        DataSetNode ds2 = g.ds(2);
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashExperiments(deletion, true, e1, e2);
        prepareTrashSamples(deletion, false, s1);
        prepareTrashDataSets(deletion, false, ds1);
        prepareTrashDataSets(deletion, false, ds1, ds2);

        trashBO.trashExperiments(asIds(e1, e2));

        context.assertIsSatisfied();
    }
    
//    @Test
    public final void testTrashExperimentWithARelatedDataSetComponentWithExternalExperiment()
    {
        EntityNodeGenerator g = new EntityNodeGenerator();
        g.parse("E1, samples: [S1], data sets: [DS1]\n"
                + "E2, data sets: [DS2]\n"
                + "S1, experiment: E1, data sets: [DS1]\n"
                + "DS1, experiment: E1, sample: S1, components: [DS2]\n"
                + "DS2, experiment: E2, containers: [DS1]\n");
        ExperimentNode e1 = g.e(1);
        SampleNode s1 = g.s(1);
        DataSetNode ds2 = g.ds(2);
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashExperiments(deletion, true, e1);
        prepareTrashSamples(deletion, false, s1);
        
        failTrashExperiments(Arrays.asList(ds2), e1);
        
        context.assertIsSatisfied();
    }
    
    @Test
    public final void testTrashExperimentWithARelatedDataSetInAnExternalContainer()
    {
        EntityNodeGenerator g = new EntityNodeGenerator();
        g.parse("E1, samples: [S1], data sets: [DS1]\n"
                + "E2, data sets: [DS2]\n"
                + "S1, experiment: E1, data sets: [DS1]\n"
                + "DS1, experiment: E1, sample: S1, components: [DS2]\n"
                + "DS2, experiment: E2, containers: [DS1]\n");
        ExperimentNode e2 = g.e(2);
        DataSetNode ds2 = g.ds(2);
        prepareEntityGraph(g);
        final DeletionPE deletion = createDeletion();
        prepareTrashExperiments(deletion, true, e2);
        
        failTrashExperiments(Arrays.asList(ds2), e2);
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testTrashPublishedExperiment()
    {
        EntityNodeGenerator g = new EntityNodeGenerator();
        g.parse("E1, data sets: [DS1, DS2, DS3, DS4]\n"
                + "DS1, experiment: E1, components: [DS3], containers: [DS5]\n"
                + "DS2, experiment: E1, children: [DS4], containers: [DS6]\n"
                + "DS3, experiment: E1, containers: [DS1]\n"
                + "DS4, experiment: E1, parents: [DS2]\n"
                + "E2, data sets: [DS5, DS6]\n"
                + "DS5, experiment: E2, components: [DS1]\n"
                + "DS6, experiment: E2, components: [DS2]\n");
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
    public final void testTrashExperimentsWithContainerDataSetWithPhysicalDataSetFromAnotherExperiment()
    {
        final DeletionPE deletion = createDeletion();
        final List<TechId> experimentIds = TechId.createList(123);
        final List<TechId> dataSetIds = TechId.createList(1);
        final List<TechId> componentDataSetIds = TechId.createList(2);
        prepareListSampleIdsByExperimentIds(experimentIds, Arrays.<TechId>asList());
        prepareListDataSetIdsByExperimentIds(experimentIds, dataSetIds);
        prepareTrash(deletion, EntityKind.EXPERIMENT, true, experimentIds);
        prepareFindDataSetComponentIds(dataSetIds, TechId.asLongs(componentDataSetIds));
        prepareFindDataSetComponentIds(componentDataSetIds, Arrays.<Long> asList());
        LinkedHashMap<Long, Set<Long>> containerIds = new LinkedHashMap<Long, Set<Long>>();
        containerIds.put(2L, new HashSet<Long>(TechId.asLongs(dataSetIds)));
        prepareListDataSetContainerIds(TechId.createList(1, 2), containerIds);
        ExperimentPE experiment = new ExperimentPE();
        experiment.setId(experimentIds.get(0).getId());
        ExternalDataPE dataSet1 = new ExternalDataPE();
        dataSet1.setId(1L);
        dataSet1.setCode("DS-1");
        dataSet1.setExperiment(experiment);
        ExternalDataPE dataSet2 = new ExternalDataPE();
        dataSet2.setId(2L);
        dataSet2.setCode("DS-2");
        prepareGetDataSetsAndNonDeletableDataSets(TechId.createList(1, 2), Arrays.<DataPE> asList(dataSet1, dataSet2),
                Arrays.<ExternalDataPE> asList());
        prepareTrash(deletion, EntityKind.DATA_SET, false, dataSetIds);

        trashBO.trashExperiments(experimentIds);

        context.assertIsSatisfied();
    }

    @Test
    public final void testTrashSamplesAlreadyTrashed()
    {
        final DeletionPE deletion = createDeletion();
        final List<TechId> sampleIds = EXAMPLE_ID_LIST;
        context.checking(new Expectations()
            {
                {
                    one(deletionDAO).trash(EntityKind.SAMPLE, sampleIds, deletion, true);
                    will(returnValue(0));
                }
            });
        trashBO.trashSamples(sampleIds);
        context.assertIsSatisfied();
    }

    @Test
    public final void testTrashSamplesWithOneLevelOfDependencies()
    {
        final DeletionPE deletion = createDeletion();
        final List<TechId> sampleIds = EXAMPLE_ID_LIST;
        final List<TechId> sampleComponentIds = TechId.createList(20L);
        final List<TechId> sampleDataSetIds = TechId.createList(60);
        final List<TechId> sampleComponentDataSetIds = TechId.createList(61);
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).listSampleIdsByContainerIds(sampleIds);
                    will(returnValue(sampleComponentIds));

                    one(dataDAO).listDataSetIdsBySampleIds(sampleIds);
                    will(returnValue(sampleDataSetIds));

                    one(dataDAO).listDataSetIdsBySampleIds(sampleComponentIds);
                    will(returnValue(sampleComponentDataSetIds));
                }
            });
        prepareTrash(deletion, EntityKind.SAMPLE, true, sampleIds);
        prepareTrash(deletion, EntityKind.SAMPLE, false, sampleComponentIds);
        prepareFindDataSetComponentIds(sampleDataSetIds, Arrays.<Long> asList());
        prepareListDataSetContainerIds(sampleDataSetIds, new LinkedHashMap<Long, Set<Long>>());
        prepareTrash(deletion, EntityKind.DATA_SET, false, sampleDataSetIds);
        prepareFindDataSetComponentIds(sampleComponentDataSetIds, Arrays.<Long> asList());
        prepareListDataSetContainerIds(sampleComponentDataSetIds, new LinkedHashMap<Long, Set<Long>>());
        SamplePE sample2 = new SamplePE();
        sample2.setId(sampleComponentIds.get(0).getId());
        prepareGetDataSetsAndNonDeletableDataSets(sampleComponentDataSetIds, sample2);
        SamplePE sample = new SamplePE();
        sample.setId(sampleIds.get(0).getId());
        prepareGetDataSetsAndNonDeletableDataSets(sampleDataSetIds, sample);
        prepareTrash(deletion, EntityKind.DATA_SET, false, sampleComponentDataSetIds);

        trashBO.trashSamples(sampleIds);

        context.assertIsSatisfied();
    }

    @Test
    public final void testTrashDataSets()
    {
        final DeletionPE deletion = createDeletion();
        final List<TechId> dataSetIds = TechId.createList(1, 2, 3);
        prepareFindDataSetComponentIds(dataSetIds, Arrays.<Long> asList(5L, 6L));
        prepareFindDataSetComponentIds(TechId.createList(5, 6), Arrays.<Long> asList());
        final List<TechId> allIds = TechId.createList(1, 2, 3, 5, 6);
        Map<Long, Set<Long>> containerIds = new LinkedHashMap<Long, Set<Long>>();
        containerIds.put(5L, new LinkedHashSet<Long>(Arrays.asList(1L)));
        containerIds.put(6L, new LinkedHashSet<Long>(Arrays.asList(2L)));
        prepareListDataSetContainerIds(allIds, containerIds);
        final List<TechId> someIds = TechId.createList(5, 6);
        prepareGetNondeletableDataSets(allIds);
        prepareTrash(deletion, EntityKind.DATA_SET, true, dataSetIds);
        prepareTrash(deletion, EntityKind.DATA_SET, false, someIds);

        trashBO.trashDataSets(dataSetIds);

        context.assertIsSatisfied();
    }

    @Test
    public void testTrashDataSetsWithDataSetInAContainer()
    {
        DeletionPE deletion = createDeletion();
        createDeletion();
        List<TechId> dataSetIds = TechId.createList(1);
        prepareFindDataSetComponentIds(dataSetIds, Arrays.<Long> asList(3L));
        prepareFindDataSetComponentIds(TechId.createList(3), Arrays.<Long> asList());
        Map<Long, Set<Long>> containerIds = new LinkedHashMap<Long, Set<Long>>();
        containerIds.put(1L, new LinkedHashSet<Long>(Arrays.asList(2L)));
        containerIds.put(3L, new LinkedHashSet<Long>(Arrays.asList(1L)));
        List<TechId> dataSetsToBeTrashed = TechId.createList(1, 3);
        prepareListDataSetContainerIds(dataSetsToBeTrashed, containerIds);
        prepareGetNondeletableDataSets(dataSetsToBeTrashed);
        prepareTrash(deletion, EntityKind.DATA_SET, true, dataSetIds);
        prepareTrash(deletion, EntityKind.DATA_SET, false, TechId.createList(3));

        trashBO.trashDataSets(dataSetIds);

        context.assertIsSatisfied();
    }

    @Test
    public void testTrashDataSetsWithDataSetComponentIndirectlyDependentOnOutsideContainer()
    {
        DeletionPE deletion = createDeletion();
        List<TechId> rootDataSetIds = TechId.createList(1);
        List<TechId> levelOneDataSetIds = TechId.createList(3, 4, 5, 6);
        prepareFindDataSetComponentIds(rootDataSetIds, TechId.asLongs(levelOneDataSetIds));
        prepareFindDataSetComponentIds(levelOneDataSetIds, Arrays.<Long> asList());
        Map<Long, Set<Long>> containerIds = new LinkedHashMap<Long, Set<Long>>();
        containerIds.put(3L, new LinkedHashSet<Long>(Arrays.asList(1L)));
        containerIds.put(4L, new LinkedHashSet<Long>(Arrays.asList(1L, 2L)));
        containerIds.put(5L, new LinkedHashSet<Long>(Arrays.asList(1L, 4L)));
        containerIds.put(6L, new LinkedHashSet<Long>(Arrays.asList(4L)));
        prepareListDataSetContainerIds(TechId.createList(1, 3, 4, 5, 6), containerIds);
        prepareGetNondeletableDataSets(TechId.createList(1, 3));
        prepareTrash(deletion, EntityKind.DATA_SET, true, rootDataSetIds);
        prepareTrash(deletion, EntityKind.DATA_SET, false, TechId.createList(3));

        trashBO.trashDataSets(rootDataSetIds);

        context.assertIsSatisfied();
    }

    @Test
    public final void testTrashUnavailableDataSets()
    {
        final List<TechId> dataSetIds = TechId.createList(1, 2, 3);
        prepareFindDataSetComponentIds(dataSetIds, Arrays.<Long> asList());
        prepareListDataSetContainerIds(dataSetIds, new LinkedHashMap<Long, Set<Long>>());
        ExternalDataPE dataSet = new ExternalDataPE();
        dataSet.setCode("ds1");
        dataSet.setStatus(DataSetArchivingStatus.ARCHIVE_PENDING);
        prepareGetNondeletableDataSets(dataSetIds, dataSet);

        createDeletion();
        try
        {
            trashBO.trashDataSets(dataSetIds);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals(
                    "Deletion not possible because the following data sets are not deletable:\n"
                            + " Status: ARCHIVE_PENDING, data sets: [ds1]", ex.getMessage());
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

    private void prepareGetNondeletableDataSets(final List<TechId> ids, final ExternalDataPE... nonDeletableDataSets)
    {
        prepareGetDataSetsAndNonDeletableDataSets(ids, Arrays.<DataPE> asList(), Arrays.asList(nonDeletableDataSets));
    }

    private void prepareGetDataSetsAndNonDeletableDataSets(final List<TechId> ids, IIdHolder idHolder,
            final ExternalDataPE... nonDeletableDataSets)
    {
        List<DataPE> dataSets = new ArrayList<DataPE>();
        for (TechId techId : ids)
        {
            ExternalDataPE dataSet = new ExternalDataPE();
            dataSet.setId(techId.getId());
            dataSet.setCode("DS-" + techId.getId());
            if (idHolder instanceof ExperimentPE)
            {
                dataSet.setExperiment((ExperimentPE) idHolder);
            }
            if (idHolder instanceof SamplePE)
            {
                dataSet.setSample((SamplePE) idHolder);
            }
            dataSets.add(dataSet);
        }
        prepareGetDataSetsAndNonDeletableDataSets(ids, dataSets, Arrays.asList(nonDeletableDataSets));
    }

    private void prepareGetDataSetsAndNonDeletableDataSets(final List<TechId> ids, final List<DataPE> dataSets,
            final List<ExternalDataPE> nonDeletableDataSets)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetTable).loadByIds(ids);
                    inSequence(dataSetTableSequence);

                    one(dataSetTable).getNonDeletableExternalDataSets();
                    will(returnValue(nonDeletableDataSets));
                    inSequence(dataSetTableSequence);

                    if (dataSets.isEmpty() == false)
                    {
                        one(dataSetTable).getDataSets();
                        will(returnValue(dataSets));
                        inSequence(dataSetTableSequence);
                    }
                }
            });
    }
    
    private void prepareListSampleIdsByExperimentIds(final List<TechId> experimentIds, final List<TechId> sampleIds)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(sampleDAO).listSampleIdsByExperimentIds(experimentIds);
                    will(returnValue(sampleIds));
                }
            });
    }

    private void prepareListDataSetIdsByExperimentIds(final List<TechId> experimentIds, final List<TechId> dataSetIds)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataDAO).listDataSetIdsByExperimentIds(experimentIds);
                    will(returnValue(dataSetIds));
                }
            });
    }
    
    private void prepareListDataSetIdsBySampleIds(final List<TechId> ids, final List<TechId> dataSetIds)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataDAO).listDataSetIdsBySampleIds(ids);
                    will(returnValue(dataSetIds));
                }
            });
    }

    private void prepareListDataSetContainerIds(final List<TechId> ids, final Map<Long, Set<Long>> containerIds)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(datasetLister).listContainerIds(TechId.asLongs(ids));
                    will(returnValue(containerIds));
                }
            });
    }
    
    private void prepareFindDataSetComponentIds(final List<TechId> ids, final List<Long> componentIds)
    {
        prepareFindChildrenIds(ids, componentIds, COMPONENT_CONTAINER_RELATIONSHIP_ID);
    }
    
    private void prepareFindChildrenIds(final List<TechId> ids, final List<Long> childrenIds, final Long relationshipTypeId)
    {
        context.checking(new Expectations()
            {
                {
                    allowing(dataDAO).findChildrenIds(new LinkedHashSet<TechId>(ids), relationshipTypeId);
                    will(returnValue(new LinkedHashSet<TechId>(TechId.createList(childrenIds))));
                }
            });
    }
    
    private void failTrashExperiments(List<DataSetNode> expectedNonDeletableDataSets, ExperimentNode...experiments)
    {
        try
        {
            trashBO.trashExperiments(asIds(experiments));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("The following related data sets couldn't be deleted because they "
                    + "are contained in data sets outside the deletion set: " 
                    + TechId.asLongs(TechId.createList(expectedNonDeletableDataSets)), ex.getMessage());
        }
    }
    
    private void prepareEntityGraph(EntityNodeGenerator g)
    {
        g.assertConsistency();
        prepareListSampleIdsByExperimentIds(g);
        prepareListDataSetIdsByExperimentIds(g);
        prepareListDataSetIdsBySampleIds(g);
        prepareFindChildrenOrComponentIds(g, CHILDREN_PARENT_RELATIONSHIP_ID);
        prepareFindChildrenOrComponentIds(g, COMPONENT_CONTAINER_RELATIONSHIP_ID);
        prepareListDataSetContainerIds(g);
        prepareGetDataSetsAndNonDeletableDataSets(g);
        prepareListSampleIdsByContainerIds(g);
    }

    private void prepareListSampleIdsByExperimentIds(final EntityNodeGenerator g)
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

    private void prepareListSampleIdsByContainerIds(final EntityNodeGenerator g)
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
    
    private void prepareListDataSetIdsByExperimentIds(final EntityNodeGenerator g)
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
    
    private void prepareListDataSetIdsBySampleIds(final EntityNodeGenerator g)
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
    
    private void prepareFindChildrenOrComponentIds(final EntityNodeGenerator g, 
            final Long relationshipTypeId)
    {
        final AbstractMockHandler<Collection<TechId>> handler = new AbstractMockHandler<Collection<TechId>>()
            {
                @Override
                public Object invoke(Invocation invocation) throws Throwable
                {
                    String methodName;
                    List<TechId> dataSetIds;
                    if (relationshipTypeId == CHILDREN_PARENT_RELATIONSHIP_ID)
                    {
                        methodName = "getChildrenDataSetIdsByDataSetIds";
                        dataSetIds = g.getChildrenDataSetIdsByDataSetIds(argument);
                    } else if (relationshipTypeId == COMPONENT_CONTAINER_RELATIONSHIP_ID)
                    {
                        methodName = "getChildrenDataSetIdsByDataSetIds";
                        dataSetIds = g.getComponentDataSetIdsByDataSetIds(argument);
                    } else
                    {
                        throw new AssertionError("Unknown relationship id: " + relationshipTypeId);
                    }
                    print(methodName + "(" + argument + ") = " + dataSetIds);
                    return new LinkedHashSet<TechId>(dataSetIds);
                }
            };
        context.checking(new Expectations()
            {
                {
                    allowing(dataDAO).findChildrenIds(with(handler), with(any(Long.class)));
                    will(handler);
                }
            });
    }

    private void prepareListDataSetContainerIds(final EntityNodeGenerator g)
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

    private void prepareGetDataSetsAndNonDeletableDataSets(final EntityNodeGenerator g)
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
                    allowing(dataSetTable).loadByIds(with(handler));

                    allowing(dataSetTable).getNonDeletableExternalDataSets();
                    will(new ReturnValueAction(handler.nonDeletableDataSets));

                    allowing(dataSetTable).getDataSets();
                    will(new ReturnValueAction(handler.dataSets));
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