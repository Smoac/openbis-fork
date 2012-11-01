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
import java.util.HashSet;
import java.util.List;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetArchivingStatus;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
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
        trashBO = new TrashBO(daoFactory, boFactory, ManagerTestTool.EXAMPLE_SESSION);
        context.checking(new Expectations()
            {
                {
                    allowing(boFactory).createDataSetTable(ManagerTestTool.EXAMPLE_SESSION);
                    will(returnValue(dataSetTable));

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
    @SuppressWarnings("deprecation")
    public final void testCreateDeletion()
    {
        final String reason = EXAMPLE_REASON;
        DeletionPE deletionPE = createDeletion(reason);
        assertEquals(reason, deletionPE.getReason());
        assertEquals(ManagerTestTool.EXAMPLE_SESSION.getPerson(), deletionPE.getRegistrator());

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

                    one(deletionDAO).revert(dummyDeletion);
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
                    one(deletionDAO).trash(EntityKind.EXPERIMENT, experimentIds, deletion);
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
        final List<TechId> dataSetIds = TechId.createList(60, 61);
        final RecordingMatcher<List<TechId>> dsIdsMatcher = new RecordingMatcher<List<TechId>>();
        context.checking(new Expectations()
            {
                {
                    one(dataSetTable).loadByIds(dataSetIds);
                    one(dataSetTable).getNonDeletableExternalDataSets();
                    will(returnValue(Arrays.asList()));

                    one(deletionDAO).trash(EntityKind.EXPERIMENT, experimentIds, deletion);
                    will(returnValue(experimentIds.size()));

                    // trash dependent samples
                    List<TechId> sampleIds = TechId.createList(50, 51);
                    one(sampleDAO).listSampleIdsByExperimentIds(experimentIds);
                    will(returnValue(sampleIds));
                    RecordingMatcher<List<TechId>> sampleIdsMatcher =
                            new RecordingMatcher<List<TechId>>();
                    one(deletionDAO).trash(with(same(EntityKind.SAMPLE)), with(sampleIdsMatcher),
                            with(same(deletion)));
                    will(returnValue(0));

                    // trash dependent data sets
                    one(dataDAO).listDataSetIdsByExperimentIds(experimentIds);
                    will(returnValue(dataSetIds));

                    one(dataDAO).listContainedDataSetsRecursively(with(dsIdsMatcher));
                    will(returnValue(dataSetIds));

                    oneOf(deletionDAO).trash(with(same(EntityKind.DATA_SET)), with(dsIdsMatcher),
                            with(same(deletion)));
                    will(returnValue(0));
                }
            });
        trashBO.trashExperiments(experimentIds);

        // Check that the data set ids match
        List<TechId> usedDsIds = dsIdsMatcher.getRecordedObjects().get(1);
        assertEquals(new HashSet<TechId>().addAll(dataSetIds),
                new HashSet<TechId>().addAll(usedDsIds));
        context.assertIsSatisfied();
    }

    @Test
    public final void testTrashExperimentsWithDeepDependencies()
    {
        final DeletionPE deletion = createDeletion();
        final List<TechId> experimentIds = EXAMPLE_ID_LIST;
        final List<TechId> sampleIds = TechId.createList(50, 51);
        final RecordingMatcher<List<TechId>> dataSetIdsMatcher =
                new RecordingMatcher<List<TechId>>();
        final List<TechId> dataSetIds = TechId.createList(60, 61);

        context.checking(new Expectations()
            {
                {
                    one(dataSetTable).loadByIds(dataSetIds);
                    one(dataSetTable).getNonDeletableExternalDataSets();
                    will(returnValue(Arrays.asList()));

                    one(deletionDAO).trash(EntityKind.EXPERIMENT, experimentIds, deletion);
                    will(returnValue(experimentIds.size()));

                    // trash dependent samples - see prepareDeletionOfSamplesWithDependencies
                    one(sampleDAO).listSampleIdsByExperimentIds(experimentIds);
                    will(returnValue(sampleIds));

                    // trash dependent data sets
                    one(dataDAO).listDataSetIdsByExperimentIds(experimentIds);
                    will(returnValue(dataSetIds));

                    one(dataDAO).listContainedDataSetsRecursively(with(dataSetIdsMatcher));
                    will(returnValue(dataSetIds));

                    one(deletionDAO).trash(with(same(EntityKind.DATA_SET)),
                            with(dataSetIdsMatcher), with(same(deletion)));
                    will(returnValue(dataSetIds.size()));
                }
            });
        prepareDeletionOfSamplesWithDependencies(deletion, sampleIds);
        trashBO.trashExperiments(experimentIds);
        verifyRecordedLists(dataSetIds, dataSetIdsMatcher);
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
                    one(deletionDAO).trash(EntityKind.SAMPLE, sampleIds, deletion);
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
        prepareDeletionOfSamplesWithDependencies(deletion, sampleIds);
        trashBO.trashSamples(sampleIds);
        context.assertIsSatisfied();
    }

    private void prepareDeletionOfSamplesWithDependencies(final DeletionPE deletion,
            final List<TechId> sampleIds)
    {
        context.checking(new Expectations()
            {
                {
                    one(dataSetTable).loadByIds(TechId.createList(70, 71, 72, 73));
                    one(dataSetTable).getNonDeletableExternalDataSets();
                    will(returnValue(Arrays.asList()));

                    RecordingMatcher<List<TechId>> sampleIdsMatcher =
                            new RecordingMatcher<List<TechId>>();

                    one(deletionDAO).trash(with(same(EntityKind.SAMPLE)), with(sampleIdsMatcher),
                            with(same(deletion)));
                    will(returnValue(sampleIds.size()));

                    // don't trash dependent children

                    // trash dependent components
                    List<TechId> componentIds = TechId.createList(60, 61);
                    one(sampleDAO).listSampleIdsByContainerIds(with(sampleIdsMatcher));
                    will(returnValue(componentIds));
                    RecordingMatcher<List<TechId>> componentIdsMatcher =
                            new RecordingMatcher<List<TechId>>();
                    one(deletionDAO).trash(with(same(EntityKind.SAMPLE)),
                            with(componentIdsMatcher), with(same(deletion)));
                    will(returnValue(0));

                    // trash dependent data sets
                    List<TechId> dataSetIds = TechId.createList(70, 71, 72);
                    one(dataDAO).listDataSetIdsBySampleIds(with(sampleIdsMatcher));
                    will(returnValue(dataSetIds));

                    RecordingMatcher<List<TechId>> dataSetIdsMatcher =
                            new RecordingMatcher<List<TechId>>();

                    one(dataDAO).listContainedDataSetsRecursively(with(dataSetIdsMatcher));
                    will(returnValue(TechId.createList(70, 71, 72, 73)));

                    one(deletionDAO).trash(with(same(EntityKind.DATA_SET)),
                            with(dataSetIdsMatcher), with(same(deletion)));
                    will(returnValue(2));

                    verifyRecordedLists(sampleIds, sampleIdsMatcher);
                }
            });
    }

    @Test
    public final void testTrashDataSets()
    {
        final DeletionPE deletion = createDeletion();
        final List<TechId> dataSetIds = TechId.createList(1, 2, 3);
        final List<TechId> allIds = TechId.createList(1, 2, 3, 5, 6);
        context.checking(new Expectations()
            {
                {
                    one(dataSetTable).loadByIds(allIds);
                    one(dataSetTable).getNonDeletableExternalDataSets();
                    will(returnValue(Arrays.asList()));

                    one(dataDAO).listContainedDataSetsRecursively(dataSetIds);
                    will(returnValue(allIds));

                    one(deletionDAO).trash(EntityKind.DATA_SET, allIds, deletion);
                    will(returnValue(allIds.size()));
                }
            });
        trashBO.trashDataSets(dataSetIds);
        context.assertIsSatisfied();
    }

    @Test
    public final void testTrashUnavailableDataSets()
    {
        final List<TechId> dataSetIds = TechId.createList(1, 2, 3);
        final List<TechId> allIds = TechId.createList(1, 2, 3, 5, 6);
        context.checking(new Expectations()
            {
                {
                    one(dataSetTable).loadByIds(allIds);
                    one(dataSetTable).getNonDeletableExternalDataSets();
                    ExternalDataPE dataSet = new ExternalDataPE();
                    dataSet.setCode("ds1");
                    dataSet.setStatus(DataSetArchivingStatus.ARCHIVE_PENDING);
                    will(returnValue(Arrays.asList(dataSet)));

                    one(dataDAO).listContainedDataSetsRecursively(dataSetIds);
                    will(returnValue(allIds));

                }
            });

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

    private static void verifyRecordedLists(List<TechId> expected,
            RecordingMatcher<List<TechId>> matcher)
    {
        for (List<TechId> recorded : matcher.getRecordedObjects())
        {
            assertTrue(recorded.containsAll(expected));
            assertTrue(expected.containsAll(recorded));
        }
    }
}