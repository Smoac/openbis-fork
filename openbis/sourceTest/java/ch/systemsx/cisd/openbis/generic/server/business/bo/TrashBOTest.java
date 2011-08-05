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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jmock.Expectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.generic.server.business.ManagerTestTool;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.DeletionPE;
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

    @Override
    @BeforeMethod
    public void beforeMethod()
    {
        super.beforeMethod();
        trashBO = new TrashBO(daoFactory, ManagerTestTool.EXAMPLE_SESSION);
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
    public final void testTrashExperientsAlreadyTrashed()
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
                    oneOf(deletionDAO).trash(with(same(EntityKind.DATA_SET)), with(dsIdsMatcher),
                            with(same(deletion)));
                    will(returnValue(0));
                }
            });
        trashBO.trashExperiments(experimentIds);

        // Check that the data set ids match
        List<TechId> usedDsIds = dsIdsMatcher.getRecordedObjects().get(0);
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
                    one(deletionDAO).trash(EntityKind.EXPERIMENT, experimentIds, deletion);
                    will(returnValue(experimentIds.size()));

                    // trash dependent samples - see prepareDeletionOfSamplesWithDependencies
                    one(sampleDAO).listSampleIdsByExperimentIds(experimentIds);
                    will(returnValue(sampleIds));

                    // trash dependent data sets
                    one(dataDAO).listDataSetIdsByExperimentIds(experimentIds);
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
                    RecordingMatcher<List<TechId>> sampleIdsMatcher =
                            new RecordingMatcher<List<TechId>>();

                    one(deletionDAO).trash(with(same(EntityKind.SAMPLE)), with(sampleIdsMatcher),
                            with(same(deletion)));
                    will(returnValue(sampleIds.size()));

                    // trash dependent children
                    List<TechId> childrenIds = TechId.createList(50, 51);
                    Set<TechId> childrenIdSet = new HashSet<TechId>(childrenIds);
                    one(sampleDAO).listSampleIdsByParentIds(with(sampleIdsMatcher));
                    will(returnValue(childrenIdSet));
                    one(deletionDAO).trash(EntityKind.SAMPLE, new ArrayList<TechId>(childrenIdSet),
                            deletion);
                    will(returnValue(0));

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
                    one(deletionDAO).trash(with(same(EntityKind.DATA_SET)),
                            with(dataSetIdsMatcher), with(same(deletion)));
                    will(returnValue(2));
                }
            });
    }

    @Test
    public final void testTrashDataSets()
    {
        final DeletionPE deletion = createDeletion();
        final List<TechId> dataSetIds = EXAMPLE_ID_LIST;
        context.checking(new Expectations()
            {
                {
                    one(deletionDAO).trash(EntityKind.DATA_SET, dataSetIds, deletion);
                    will(returnValue(EXAMPLE_ID_LIST.size()));
                }
            });
        trashBO.trashDataSets(dataSetIds);
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