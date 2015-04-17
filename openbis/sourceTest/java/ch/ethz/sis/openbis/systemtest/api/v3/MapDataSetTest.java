/*
 * Copyright 2015 ETH Zuerich, CISD
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetKind;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSetUpdate;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.DataSetRelationType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.HistoryEntry;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.PropertyHistoryEntry;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.history.RelationHistoryEntry;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.material.Material;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.dataset.DataSetFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.tag.TagFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.DataSetPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.dataset.IDataSetId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.material.MaterialPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SampleIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.sample.SamplePermId;

/**
 * @author pkupczyk
 */
public class MapDataSetTest extends AbstractDataSetTest
{

    @Test
    public void testMapByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId1 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId2 = new DataSetPermId("20110509092359990-10");

        Map<IDataSetId, DataSet> map =
                v3api.mapDataSets(sessionToken, Arrays.asList(permId1, permId2),
                        new DataSetFetchOptions());

        assertEquals(map.size(), 2);

        Iterator<DataSet> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId1 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId2 = new DataSetPermId("NONEXISTENT");
        DataSetPermId permId3 = new DataSetPermId("20110509092359990-10");

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Arrays.asList(permId1, permId2, permId3), new DataSetFetchOptions());

        assertEquals(map.size(), 2);

        Iterator<DataSet> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId3);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId3).getPermId(), permId3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId1 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId2 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId3 = new DataSetPermId("20110509092359990-10");

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Arrays.asList(permId1, permId2, permId3), new DataSetFetchOptions());

        assertEquals(map.size(), 2);

        Iterator<DataSet> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId3);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId3).getPermId(), permId3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsUnauthorized()
    {
        DataSetPermId permId1 = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId2 = new DataSetPermId("20120619092259000-22");

        List<? extends IDataSetId> ids = Arrays.asList(permId1, permId2);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, ids, new DataSetFetchOptions());

        assertEquals(map.size(), 2);
        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        map = v3api.mapDataSets(sessionToken, ids, new DataSetFetchOptions());

        assertEquals(map.size(), 1);

        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithFetchOptionsEmpty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Collections.singletonList(permId), new DataSetFetchOptions());

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);
        assertEquals(dataSet.getPermId().toString(), "20081105092159111-1");
        assertEquals(dataSet.getCode(), "20081105092159111-1");
        assertEqualsDate(dataSet.getAccessDate(), "2011-04-01 09:56:25");
        assertEqualsDate(dataSet.getModificationDate(), "2009-03-23 15:34:44");
        assertEqualsDate(dataSet.getRegistrationDate(), "2009-02-09 12:20:21");

        assertTypeNotFetched(dataSet);
        assertExternalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertContainedNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test(enabled = false)
    public void testMapWithType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");
        DataSetPermId permId2 = new DataSetPermId("ROOT_CONTAINER");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withType();

        Map<IDataSetId, DataSet> map =
                v3api.mapDataSets(sessionToken, Arrays.asList(permId, permId2),
                        fetchOptions);

        assertEquals(map.size(), 2);

        DataSet dataSet = map.get(permId);

        assertEquals(dataSet.getType().getCode(), "HCS_IMAGE");
        assertEquals(dataSet.getType().getPermId().getPermId(), "HCS_IMAGE");
        assertEquals(dataSet.getType().getDescription(), "High Content Screening Image");
        assertEquals(dataSet.getType().getKind(), DataSetKind.PHYSICAL);

        assertExternalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertContainedNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);

        DataSet dataSet2 = map.get(permId2);

        assertEquals(dataSet2.getType().getCode(), "CONTAINER_TYPE");
        assertEquals(dataSet2.getType().getPermId().getPermId(), "CONTAINER_TYPE");
        assertEquals(dataSet2.getType().getDescription(), "A container (virtual) data set type");
        assertEquals(dataSet2.getType().getKind(), DataSetKind.CONTAINER);

        assertExternalDataNotFetched(dataSet2);
        assertExperimentNotFetched(dataSet2);
        assertSampleNotFetched(dataSet2);
        assertPropertiesNotFetched(dataSet2);
        assertParentsNotFetched(dataSet2);
        assertChildrenNotFetched(dataSet2);
        assertContainedNotFetched(dataSet2);
        assertContainersNotFetched(dataSet2);
        assertModifierNotFetched(dataSet2);
        assertRegistratorNotFetched(dataSet2);
        assertTagsNotFetched(dataSet2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithExperiment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withExperiment();

        Map<IDataSetId, DataSet> map =
                v3api.mapDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        assertEquals(dataSet.getExperiment().getIdentifier().getIdentifier(), "/CISD/NEMO/EXP-TEST-1");

        assertTypeNotFetched(dataSet);
        assertExternalDataNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertContainedNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withSample();

        Map<IDataSetId, DataSet> map =
                v3api.mapDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        assertEquals(dataSet.getSample().getIdentifier().getIdentifier(), "/CISD/CP-TEST-1");

        assertTypeNotFetched(dataSet);
        assertExternalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertContainedNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withProperties();

        Map<IDataSetId, DataSet> map =
                v3api.mapDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        Map<String, String> properties = dataSet.getProperties();
        assertEquals(properties.get("COMMENT"), "no comment");
        assertEquals(properties.get("GENDER"), "FEMALE");
        assertEquals(properties.get("BACTERIUM"), "BACTERIUM1 (BACTERIUM)");
        assertEquals(properties.get("ANY_MATERIAL"), "1000_C (SIRNA)");

        assertTypeNotFetched(dataSet);
        assertExternalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertContainedNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithChildren()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withChildren();

        Map<IDataSetId, DataSet> map =
                v3api.mapDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        List<DataSet> children = dataSet.getChildren();

        assertEquals(children.size(), 1);

        DataSet child = children.get(0);
        assertEquals(child.getCode(), "20081105092259000-9");

        assertTypeNotFetched(dataSet);
        assertExternalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertContainedNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithParents()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092259000-9");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withParents();

        Map<IDataSetId, DataSet> map =
                v3api.mapDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        List<DataSet> parents = dataSet.getParents();

        assertEquals(parents.size(), 3);

        assertIdentifiers(parents, "20081105092159111-1", "20081105092159222-2", "20081105092159333-3");

        assertTypeNotFetched(dataSet);
        assertExternalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertContainedNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithContained()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20110509092359990-10");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withContained();

        Map<IDataSetId, DataSet> map =
                v3api.mapDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        List<DataSet> contained = dataSet.getContained();

        assertEquals(contained.size(), 2);

        assertIdentifiers(contained, "20110509092359990-11", "20110509092359990-12");

        assertTypeNotFetched(dataSet);
        assertExternalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithContainers()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20110509092359990-11");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withContainers();

        Map<IDataSetId, DataSet> map =
                v3api.mapDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        List<DataSet> containers = dataSet.getContainers();

        assertEquals(containers.size(), 1);

        assertIdentifiers(containers, "20110509092359990-10");

        assertTypeNotFetched(dataSet);
        assertExternalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertContainedNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithExternalData()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withExternalData();

        Map<IDataSetId, DataSet> map =
                v3api.mapDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(1, map.size());

        DataSet dataSet = map.get(permId);

        assertEquals(dataSet.getExternalData().getLocation(), "a/1");
        assertEquals(dataSet.getExternalData().getShareId(), "42");
        assertEquals(dataSet.getExternalData().getSize(), Long.valueOf(4711));
        assertEquals(dataSet.getExternalData().getSpeedHint(), Integer.valueOf(42));

        assertTypeNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertContainedNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithModifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withModifier();

        Map<IDataSetId, DataSet> map =
                v3api.mapDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(1, map.size());

        DataSet dataSet = map.get(permId);

        assertEquals(dataSet.getModifier().getUserId(), "test");

        assertTypeNotFetched(dataSet);
        assertExternalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertContainedNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithRegistrator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetPermId permId = new DataSetPermId("20081105092259000-19");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withRegistrator();

        Map<IDataSetId, DataSet> map =
                v3api.mapDataSets(sessionToken, Arrays.asList(permId),
                        fetchOptions);

        assertEquals(1, map.size());

        DataSet dataSet = map.get(permId);

        assertEquals(dataSet.getRegistrator().getUserId(), "test");

        assertTypeNotFetched(dataSet);
        assertExternalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertContainedNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertTagsNotFetched(dataSet);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithTags()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        TagFetchOptions tagfe = fetchOptions.withTags();
        tagfe.withOwner();

        DataSetPermId permId = new DataSetPermId("20120619092259000-22");

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(map.size(), 1);

        DataSet dataSet = map.get(permId);

        Set<Tag> tags = dataSet.getTags();

        assertEquals(tags.size(), 1);

        Tag tag = tags.iterator().next();
        assertEquals(tag.getOwner().getUserId(), TEST_USER);
        assertEquals(tag.getCode(), "TEST_METAPROJECTS");
        assertEquals(tag.getPermId().getPermId(), "/" + TEST_USER + "/TEST_METAPROJECTS");

        assertTypeNotFetched(dataSet);
        assertExternalDataNotFetched(dataSet);
        assertExperimentNotFetched(dataSet);
        assertSampleNotFetched(dataSet);
        assertPropertiesNotFetched(dataSet);
        assertParentsNotFetched(dataSet);
        assertChildrenNotFetched(dataSet);
        assertContainedNotFetched(dataSet);
        assertContainersNotFetched(dataSet);
        assertModifierNotFetched(dataSet);
        assertRegistratorNotFetched(dataSet);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithMaterialProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withMaterialProperties().withRegistrator();
        fetchOptions.withProperties();

        DataSetPermId permId = new DataSetPermId("20081105092159111-1");

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Arrays.asList(permId), fetchOptions);

        DataSet data = map.get(permId);

        assertEquals(data.getProperties().get("ANY_MATERIAL"), "1000_C (SIRNA)");
        assertEquals(data.getProperties().get("BACTERIUM"), "BACTERIUM1 (BACTERIUM)");

        Map<String, Material> materialProperties = data.getMaterialProperties();

        Material gene = materialProperties.get("ANY_MATERIAL");
        assertEquals(gene.getPermId(), new MaterialPermId("1000_C", "SIRNA"));
        assertEquals(gene.getRegistrator().getUserId(), "test");
        assertTagsNotFetched(gene);

        Material bacterium = materialProperties.get("BACTERIUM");
        assertEquals(bacterium.getPermId(), new MaterialPermId("BACTERIUM1", "BACTERIUM"));
        assertEquals(bacterium.getRegistrator().getUserId(), "test");
        assertTagsNotFetched(bacterium);
    }

    @Test
    public void testMapWithHistoryEmpty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        IDataSetId id = new DataSetPermId("COMPONENT_1A");

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withHistory();

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Arrays.asList(id), fetchOptions);

        assertEquals(map.size(), 1);
        DataSet dataSet = map.get(id);

        List<HistoryEntry> history = dataSet.getHistory();
        assertEquals(history, Collections.emptyList());

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithHistoryProperty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        IDataSetId id = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.setProperty("COMMENT", "new comment");

        v3api.updateDataSets(sessionToken, Arrays.asList(update));

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withHistory();

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Arrays.asList(id), fetchOptions);

        assertEquals(map.size(), 1);
        DataSet dataSet = map.get(id);

        List<HistoryEntry> history = dataSet.getHistory();
        assertEquals(history.size(), 1);

        PropertyHistoryEntry entry = (PropertyHistoryEntry) history.get(0);
        assertEquals(entry.getPropertyName(), "COMMENT");
        assertEquals(entry.getPropertyValue(), "co comment");

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithHistoryExperiment()
    {
        IDataSetId id = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.setExperimentId(new ExperimentIdentifier("/CISD/NEMO/EXP1"));

        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(id);
        update2.setExperimentId(new ExperimentIdentifier("/CISD/DEFAULT/EXP-REUSE"));

        List<HistoryEntry> history = testMapWithHistory(update, update2);
        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), DataSetRelationType.EXPERIMENT);
        assertEquals(entry.getRelatedObjectId(), new ExperimentPermId("200811050951882-1028"));
    }

    @Test
    public void testMapWithHistorySample()
    {
        IDataSetId id = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.setSampleId(new SampleIdentifier("/CISD/3VCP5"));

        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(id);
        update2.setSampleId(new SampleIdentifier("/CISD/3VCP6"));

        List<HistoryEntry> history = testMapWithHistory(update, update2);
        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), DataSetRelationType.SAMPLE);
        assertEquals(entry.getRelatedObjectId(), new SamplePermId("200811050946559-979"));
    }

    @Test
    public void testMapWithHistoryContainer()
    {
        IDataSetId id = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.getContainerIds().set(new DataSetPermId("CONTAINER_2"));

        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(id);
        update2.getContainerIds().set(new DataSetPermId("CONTAINER_1"));

        List<HistoryEntry> history = testMapWithHistory(update, update2);
        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), DataSetRelationType.CONTAINER);
        assertEquals(entry.getRelatedObjectId(), new DataSetPermId("CONTAINER_2"));
    }

    @Test
    public void testMapWithHistoryContained()
    {
        IDataSetId id = new DataSetPermId("CONTAINER_1");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.getContainedIds().set(new DataSetPermId("COMPONENT_2A"));

        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(id);
        update2.getContainedIds().set(new DataSetPermId("COMPONENT_1A"));

        List<HistoryEntry> history = testMapWithHistory(update, update2);
        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), DataSetRelationType.CONTAINED);
        assertEquals(entry.getRelatedObjectId(), new DataSetPermId("COMPONENT_2A"));
    }

    @Test
    public void testMapWithHistoryParent()
    {
        IDataSetId id = new DataSetPermId("COMPONENT_1A");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.getParentIds().set(new DataSetPermId("CONTAINER_2"));

        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(id);
        update2.getParentIds().set(new DataSetPermId("CONTAINER_1"));

        List<HistoryEntry> history = testMapWithHistory(update, update2);
        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), DataSetRelationType.PARENT);
        assertEquals(entry.getRelatedObjectId(), new DataSetPermId("CONTAINER_2"));
    }

    @Test
    public void testMapWithHistoryChild()
    {
        IDataSetId id = new DataSetPermId("CONTAINER_1");

        DataSetUpdate update = new DataSetUpdate();
        update.setDataSetId(id);
        update.getChildIds().set(new DataSetPermId("COMPONENT_2A"));

        DataSetUpdate update2 = new DataSetUpdate();
        update2.setDataSetId(id);
        update2.getChildIds().set(new DataSetPermId("COMPONENT_1A"));

        List<HistoryEntry> history = testMapWithHistory(update, update2);
        assertEquals(history.size(), 1);

        RelationHistoryEntry entry = (RelationHistoryEntry) history.get(0);
        assertEquals(entry.getRelationType(), DataSetRelationType.CHILD);
        assertEquals(entry.getRelatedObjectId(), new DataSetPermId("COMPONENT_2A"));
    }

    private List<HistoryEntry> testMapWithHistory(DataSetUpdate... updates)
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        IDataSetId id = updates[0].getDataSetId();

        for (DataSetUpdate update : updates)
        {
            v3api.updateDataSets(sessionToken, Arrays.asList(update));
        }

        DataSetFetchOptions fetchOptions = new DataSetFetchOptions();
        fetchOptions.withHistory();

        Map<IDataSetId, DataSet> map = v3api.mapDataSets(sessionToken, Arrays.asList(id), fetchOptions);

        assertEquals(map.size(), 1);
        DataSet dataSet = map.get(id);

        v3api.logout(sessionToken);

        return dataSet.getHistory();
    }

}
