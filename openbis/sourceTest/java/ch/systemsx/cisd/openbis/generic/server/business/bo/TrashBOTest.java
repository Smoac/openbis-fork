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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
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

    private static Long COMPONENT_CONTAINER_RELATIONSHIP_ID = 1234L;

    private static TechId EXAMPLE_ID = new TechId(1L);

    private static List<TechId> EXAMPLE_ID_LIST = TechId.createList(1, 2, 3);

    private ITrashBO trashBO;

    private ICommonBusinessObjectFactory boFactory;

    private IDataSetTable dataSetTable;

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
        final DeletionPE deletion = createDeletion();
        final List<TechId> experimentIds = EXAMPLE_ID_LIST;
        final List<TechId> sampleIds = TechId.createList(50, 51);
        final List<TechId> dataSetIds = TechId.createList(60, 61);
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).listSampleIdsByExperimentIds(experimentIds);
                    will(returnValue(sampleIds));

                    one(sampleDAO).listSampleIdsByContainerIds(sampleIds);
                    will(returnValue(Arrays.asList()));

                    one(dataDAO).listDataSetIdsByExperimentIds(experimentIds);
                    will(returnValue(dataSetIds));

                    one(dataDAO).listDataSetIdsBySampleIds(sampleIds);
                    will(returnValue(Arrays.asList()));
                }
            });
        prepareFindDataSetComponentIds(dataSetIds, Arrays.<Long> asList());
        prepareListDataSetContainerIds(dataSetIds, new LinkedHashMap<Long, Set<Long>>());
        prepareGetNondeletableDataSets(dataSetIds);
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
        context.checking(new Expectations()
            {
                {
                    one(sampleDAO).listSampleIdsByExperimentIds(experimentIds);
                    will(returnValue(sampleIds));

                    one(dataDAO).listDataSetIdsByExperimentIds(experimentIds);
                    will(returnValue(dataSetIds));

                    one(sampleDAO).listSampleIdsByContainerIds(sampleIds);
                    will(returnValue(componentSampleIds));
                }
            });
        prepareTrash(deletion, EntityKind.EXPERIMENT, true, experimentIds);
        prepareTrash(deletion, EntityKind.SAMPLE, false, sampleIds);
        prepareTrash(deletion, EntityKind.SAMPLE, false, componentSampleIds);
        prepareFindDataSetComponentIds(dataSetIds, Arrays.<Long> asList());
        prepareListDataSetContainerIds(dataSetIds, new LinkedHashMap<Long, Set<Long>>());
        prepareGetNondeletableDataSets(dataSetIds);
        prepareTrash(deletion, EntityKind.DATA_SET, false, dataSetIds);

        List<TechId> sampleDataSets = TechId.createList(70L, 71L);
        prepareListDataSetIdsBySampleIds(sampleIds, sampleDataSets);
        prepareFindDataSetComponentIds(sampleDataSets, Arrays.<Long> asList());
        prepareListDataSetContainerIds(sampleDataSets, new LinkedHashMap<Long, Set<Long>>());
        prepareGetNondeletableDataSets(sampleDataSets);
        prepareTrash(deletion, EntityKind.DATA_SET, false, sampleDataSets);

        prepareListDataSetIdsBySampleIds(componentSampleIds, TechId.createList());

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
        prepareGetNondeletableDataSets(sampleDataSetIds);
        prepareTrash(deletion, EntityKind.DATA_SET, false, sampleDataSetIds);
        prepareFindDataSetComponentIds(sampleComponentDataSetIds, Arrays.<Long> asList());
        prepareListDataSetContainerIds(sampleComponentDataSetIds, new LinkedHashMap<Long, Set<Long>>());
        prepareGetNondeletableDataSets(sampleComponentDataSetIds);
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

    private void prepareTrash(final DeletionPE deletion, final EntityKind entityKind, final boolean isOriginalDeletion,
            final List<TechId> entityIds)
    {
        context.checking(new Expectations()
            {
                {
                    one(deletionDAO).trash(entityKind, entityIds, deletion, isOriginalDeletion);
                    will(returnValue(entityIds.size()));
                }
            });
    }

    private void prepareGetNondeletableDataSets(final List<TechId> ids, final ExternalDataPE... dataSets)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetTable).loadByIds(ids);
                    one(dataSetTable).getNonDeletableExternalDataSets();
                    will(returnValue(Arrays.asList(dataSets)));
                }
            });
    }

    private void prepareListDataSetIdsBySampleIds(final List<TechId> ids, final List<TechId> dataSetIds)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).listDataSetIdsBySampleIds(ids);
                    will(returnValue(dataSetIds));
                }
            });
    }

    private void prepareListDataSetContainerIds(final List<TechId> ids, final Map<Long, Set<Long>> containerIds)
    {
        context.checking(new Expectations()
            {
                {
                    one(datasetLister).listContainerIds(TechId.asLongs(ids));
                    will(returnValue(containerIds));
                }
            });
    }

    private void prepareFindDataSetComponentIds(final List<TechId> ids, final List<Long> childrenIds)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataDAO).findChildrenIds(new LinkedHashSet<TechId>(ids), COMPONENT_CONTAINER_RELATIONSHIP_ID);
                    will(returnValue(new LinkedHashSet<TechId>(TechId.createList(childrenIds))));
                }
            });
    }

}