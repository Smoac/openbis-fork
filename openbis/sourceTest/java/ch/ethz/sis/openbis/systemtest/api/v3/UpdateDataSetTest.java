/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
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

package ch.ethz.sis.openbis.systemtest.api.v3;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateAction;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateActionAdd;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.ListUpdateValue.ListUpdateActionRemove;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.ExternalDataUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.FileFormatTypePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.ITagId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.tag.TagCode;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author pkupczyk
 */
public class UpdateDataSetTest extends AbstractSampleTest
{

    @Test
    public void testUpdateWithDataSetIdNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        assertUserFailureException(new IDelegatedAction()
            {

                @Override
                public void execute()
                {
                    DataSetUpdate update = new DataSetUpdate();
                    SamplePermId sampleId = new SamplePermId("201206191219327-1058");
                    update.setSampleId(sampleId);

                    v3api.updateDataSets(sessionToken, Collections.singletonList(update));
                }
            }, "Data set id cannot be null.");
    }

    @Test
    public void testUpdateWithDataSetExisting()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.setProperty("COMMENT", "Updated description");

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        DataSet result = v3api.mapDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        assertEquals(result.getProperties().get("COMMENT"), "Updated description");
    }

    @Test
    public void testUpdateWithDataSetNonexistent()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IDataSetId dataSetId = new DataSetPermId("IDONTEXIST");
        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);

        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateDataSets(sessionToken, Arrays.asList(update));
                }
            }, dataSetId);
    }

    @Test
    public void testUpdateWithSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        SamplePermId sampleId = new SamplePermId("201206191219327-1058");
        update.setSampleId(sampleId);
        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withSample();
        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Arrays.asList(dataSetId), fe);

        DataSet result = map.get(dataSetId);
        assertEquals(result.getSample().getPermId(), sampleId);
    }

    // @Test broken
    public void testUpdateWithSampleWithoutAnExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        SamplePermId sampleId = new SamplePermId("200811050943584-1024");
        update.setSampleId(sampleId);
        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withSample();
        fe.withExperiment();
        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Arrays.asList(dataSetId), fe);

        DataSet result = map.get(dataSetId);
        assertEquals(result.getSample().getPermId(), sampleId);
        assertEquals(result.getExperiment(), null);
    }

    @Test
    public void testUpdateWithSampleUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        final IDataSetId dataSetId = new DataSetPermId("20081105092159111-1");
        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateDataSets(sessionToken, Arrays.asList(update));
                }
            }, dataSetId);
    }

    @Test
    public void testUpdateWithSampleNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092159111-1");
        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withExperiment();
        fe.withSample();

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Arrays.asList(dataSetId), fe);

        DataSet result = map.get(dataSetId);
        assertEquals(result.getExperiment().getPermId().getPermId(), "200902091239077-1033");
        assertEquals(result.getSample().getPermId().getPermId(), "200902091219327-1025");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.setSampleId(null);

        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        map = v3api.mapDataSets(sessionToken, Arrays.asList(dataSetId), fe);

        result = map.get(dataSetId);
        assertEquals(result.getExperiment().getPermId().getPermId(), "200902091239077-1033");
        assertNull(result.getSample());
    }

    @Test
    public void testUpdateWithExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.setExperimentId(new ExperimentPermId("200811050951882-1028"));

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        fe.withExperiment();
        DataSet result = v3api.mapDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        assertEquals(result.getExperiment().getPermId().getPermId(), "200811050951882-1028");
    }

    @Test
    public void testUpdateWithExperimentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20120619092259000-22");
        IExperimentId experimentId = new ExperimentIdentifier("/CISD/NEMO/EXP1");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.setExperimentId(experimentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateDataSets(sessionToken, Collections.singletonList(update));
                }
            }, experimentId);
    }

    @Test
    public void testUpdateWithExperimentNull()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.setExperimentId(null);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateDataSets(sessionToken, Collections.singletonList(update));
                }
            }, "Data set '20081105092259000-18' cannot have experiment set to null");
    }

    @Test
    public void testUpdateWithExperimentAndSample()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092159111-1");
        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withExperiment();
        fe.withSample();

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Arrays.asList(dataSetId), fe);

        DataSet result = map.get(dataSetId);
        assertEquals(result.getExperiment().getPermId().getPermId(), "200902091239077-1033");
        assertEquals(result.getSample().getPermId().getPermId(), "200902091219327-1025");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.setExperimentId(new ExperimentPermId("200902091258949-1034"));
        update.setSampleId(new SamplePermId("200902091250077-1026"));

        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        map = v3api.mapDataSets(sessionToken, Arrays.asList(dataSetId), fe);

        result = map.get(dataSetId);
        assertEquals(result.getExperiment().getPermId().getPermId(), "200902091258949-1034");
        assertEquals(result.getSample().getPermId().getPermId(), "200902091250077-1026");
    }

    @Test
    public void testUpdateWithExternalDataSet()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        ExternalDataUpdate edupt = new ExternalDataUpdate();
        edupt.setFileFormatTypeId(new FileFormatTypePermId("PLKPROPRIETARY"));
        update.setExternalData(edupt);

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        fe.withExternalData().withFileFormatType();
        DataSet result = v3api.mapDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        assertEquals(result.getExternalData().getFileFormatType().getCode(), "PLKPROPRIETARY");
    }

    @Test
    public void testUpdateWithParentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20120619092259000-22");
        final DataSetPermId parentId = new DataSetPermId("20081105092159111-1");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getParentIds().add(parentId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateDataSets(sessionToken, Collections.singletonList(update));
                }
            }, parentId);
    }

    @Test
    public void testUpdateWithParentCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-8");
        final DataSetPermId parentId = new DataSetPermId("20081105092259000-9");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getParentIds().add(parentId);
        update.getChildIds().add(parentId);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateDataSets(sessionToken, Collections.singletonList(update));
                }
            }, "Circular dependency found: 20081105092259000-8");
    }

    @Test
    public void testUpdateWithParentAdd()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-18");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        ListUpdateAction<IDataSetId> addAction = new ListUpdateActionAdd<IDataSetId>();
        addAction.setItems(Collections.singletonList(new DataSetPermId("20081105092259000-20")));

        update.setParentActions(Collections.singletonList(addAction));

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        fe.withExperiment();
        fe.withParents();
        DataSet result = v3api.mapDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        AssertionUtil.assertSize(result.getParents(), 2);
    }

    @Test
    public void testUpdateWithParentRemove()
    {
        DataSetPermId originalChild = new DataSetPermId("20081105092259000-18");
        DataSetPermId originalParent = new DataSetPermId("20110805092359990-17");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(originalChild);
        ListUpdateAction<IDataSetId> removeAction = new ListUpdateActionRemove<IDataSetId>();
        removeAction.setItems(Collections.singletonList(originalParent));
        update.setParentActions(Collections.singletonList(removeAction));

        assertRemovingParent(originalChild, originalParent, update);
    }

    @Test
    public void testUpdateWithChildrenUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20120619092259000-22");
        final DataSetPermId childId = new DataSetPermId("20081105092159111-1");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getChildIds().add(childId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateDataSets(sessionToken, Collections.singletonList(update));
                }
            }, childId);
    }

    @Test
    public void testUpdateWithChildrenRemove()
    {
        DataSetPermId originalChild = new DataSetPermId("20081105092259000-18");
        DataSetPermId originalParent = new DataSetPermId("20110805092359990-17");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(originalParent);
        ListUpdateAction<IDataSetId> removeAction = new ListUpdateActionRemove<IDataSetId>();
        removeAction.setItems(Collections.singletonList(originalChild));
        update.setChildActions(Collections.singletonList(removeAction));

        assertRemovingParent(originalChild, originalParent, update);
    }

    @Test
    public void testUpdateWithComponentUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20120619092259000-22");
        final DataSetPermId containedId = new DataSetPermId("20081105092159111-1");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getContainedIds().add(containedId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateDataSets(sessionToken, Collections.singletonList(update));
                }
            }, containedId);
    }

    @Test
    public void testUpdateWithComponentCircularDependency()
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20081105092259000-8");
        final DataSetPermId componentId = new DataSetPermId("20081105092259000-9");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getContainedIds().add(componentId);
        update.getContainerIds().add(componentId);

        assertUserFailureException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateDataSets(sessionToken, Collections.singletonList(update));
                }
            }, "Circular dependency found: 20081105092259000-8");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateWithComponentAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("CONTAINER_1");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        ListUpdateActionAdd<IDataSetId> addAction = new ListUpdateActionAdd<IDataSetId>();
        ListUpdateAction<IDataSetId> removeAction = new ListUpdateActionRemove<IDataSetId>();
        addAction.setItems(Collections.singletonList(new DataSetPermId("COMPONENT_2A")));
        removeAction.setItems(Collections.singletonList(new DataSetPermId("COMPONENT_1A")));

        update.setContainedActions(Arrays.asList(addAction, removeAction));

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withContained();
        DataSet result = v3api.mapDataSets(sessionToken, Collections.singletonList(dataSetId), fe).get(dataSetId);

        AssertionUtil.assertCollectionContainsOnly(dataSetCodes(result.getContained()), "COMPONENT_1B", "COMPONENT_2A");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateWithContainerAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("COMPONENT_1A");

        DataSetPermId cont2 = new DataSetPermId("CONTAINER_2");
        DataSetPermId cont3a = new DataSetPermId("CONTAINER_3A");
        DataSetPermId cont1 = new DataSetPermId("CONTAINER_1");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        ListUpdateActionAdd<IDataSetId> addAction = new ListUpdateActionAdd<IDataSetId>();
        ListUpdateAction<IDataSetId> removeAction = new ListUpdateActionRemove<IDataSetId>();
        addAction.setItems(Arrays.asList(cont2, cont3a));
        removeAction.setItems(Collections.singletonList(cont1));

        update.setContainerActions(Arrays.asList(addAction, removeAction));

        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withContainers();
        fe.withContained();
        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Arrays.asList(dataSetId, cont2, cont3a, cont1), fe);

        DataSet result = map.get(dataSetId);

        AssertionUtil.assertCollectionContainsOnly(dataSetCodes(result.getContainers()), cont2.getPermId(), cont3a.getPermId());
        AssertionUtil.assertCollectionDoesntContain(dataSetCodes(map.get(cont1).getContained()), dataSetId.getPermId());
        AssertionUtil.assertCollectionContains(dataSetCodes(map.get(cont2).getContained()), dataSetId.getPermId());
        AssertionUtil.assertCollectionContains(dataSetCodes(map.get(cont3a).getContained()), dataSetId.getPermId());
    }

    @Test
    public void testUpdateWithContainerUnauthorized()
    {
        final String sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);

        DataSetPermId dataSetId = new DataSetPermId("20120619092259000-22");
        final DataSetPermId containerId = new DataSetPermId("20081105092159111-1");

        final DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(dataSetId);
        update.getContainerIds().add(containerId);

        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    v3api.updateDataSets(sessionToken, Collections.singletonList(update));
                }
            }, containerId);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateWithTagsWithSetAddRemove()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withTags();

        DataSetPermId dataSet = new DataSetPermId("20120619092259000-22");

        TagCode existingTag = new TagCode("TEST_METAPROJECTS");
        TagCode newTag1 = new TagCode("NEW_TAG_1");
        TagCode newTag2 = new TagCode("NEW_TAG_2");

        // add tag1
        DataSetUpdate firstUpdate = new DataSetUpdate();
        firstUpdate.setDataSetId(dataSet);
        ListUpdateAction<ITagId> addAction = new ListUpdateActionAdd<ITagId>();
        addAction.setItems(Arrays.asList(newTag1));
        firstUpdate.setTagActions(Arrays.asList(addAction));
        v3api.updateDataSets(sessionToken, Arrays.asList(firstUpdate));

        Map<IDataSetId, DataSet> result = v3api.mapDataSets(sessionToken, Arrays.asList(dataSet), fe);
        AssertionUtil.assertCollectionContainsOnly(tagCodes(result.get(dataSet).getTags()), existingTag.getCode(), newTag1.getCode());

        // remove test_metaprojects and add a new tag2
        DataSetUpdate secondUpdate = new DataSetUpdate();
        secondUpdate.setDataSetId(dataSet);
        addAction = new ListUpdateActionAdd<ITagId>();
        ListUpdateAction<ITagId> removeAction = new ListUpdateActionRemove<ITagId>();
        addAction.setItems(Arrays.asList(newTag2));
        removeAction.setItems(Arrays.asList(existingTag));
        secondUpdate.setTagActions(Arrays.asList(addAction, removeAction));
        v3api.updateDataSets(sessionToken, Arrays.asList(secondUpdate));

        result = v3api.mapDataSets(sessionToken, Arrays.asList(dataSet), fe);
        AssertionUtil.assertCollectionContainsOnly(tagCodes(result.get(dataSet).getTags()), newTag1.getCode(), newTag2.getCode());
    }

    private Collection<String> dataSetCodes(Collection<? extends DataSet> list)
    {
        LinkedList<String> result = new LinkedList<String>();
        for (DataSet dataSet : list)
        {
            result.add(dataSet.getCode());
        }
        return result;
    }

    private Collection<String> tagCodes(Collection<? extends Tag> list)
    {
        LinkedList<String> result = new LinkedList<String>();
        for (Tag tag : list)
        {
            result.add(tag.getCode());
        }
        return result;
    }

    private void assertRemovingParent(DataSetPermId originalChild, DataSetPermId originalParent, DataSetUpdate update)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        // assert parent to begin with
        DataSetFetchOptions fe = new DataSetFetchOptions();
        fe.withProperties();
        fe.withParents();
        fe.withChildren();
        Map<IDataSetId, DataSet> result = v3api.mapDataSets(sessionToken, Arrays.asList(originalChild, originalParent), fe);

        AssertionUtil.assertCollectionContains(dataSetCodes(result.get(originalParent).getChildren()), originalChild.getPermId());
        AssertionUtil.assertCollectionContains(dataSetCodes(result.get(originalChild).getParents()), originalParent.getPermId());

        // update
        v3api.updateDataSets(sessionToken, Collections.singletonList(update));

        // assert parent removed
        result = v3api.mapDataSets(sessionToken, Arrays.asList(originalChild, originalParent), fe);

        AssertionUtil.assertCollectionDoesntContain(dataSetCodes(result.get(originalParent).getChildren()), originalChild.getPermId());
        AssertionUtil.assertCollectionDoesntContain(dataSetCodes(result.get(originalChild).getParents()), originalParent.getPermId());
    }

}
