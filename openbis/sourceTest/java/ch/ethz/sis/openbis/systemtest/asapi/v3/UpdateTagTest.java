/*
 * Copyright 2016 ETH Zuerich, CISD
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

package ch.ethz.sis.openbis.systemtest.asapi.v3;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Map;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.dataset.id.DataSetPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.material.id.MaterialPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.sample.id.SampleIdentifier;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.Tag;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.fetchoptions.TagFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.ITagId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.update.TagUpdate;
import ch.systemsx.cisd.common.action.IDelegatedAction;
import ch.systemsx.cisd.common.exceptions.UserFailureException;

/**
 * @author pkupczyk
 */
@Test(groups = { "before remote api" })
public class UpdateTagTest extends AbstractTest
{

    @Test(expectedExceptions = UserFailureException.class, expectedExceptionsMessageRegExp = ".*Tag id cannot be null.*")
    public void testUpdateWithTagIdNull()
    {
        TagUpdate update = new TagUpdate();
        updateTag(TEST_USER, PASSWORD, update);
    }

    @Test
    public void testUpdateWithTagIdNonexistent()
    {
        final ITagId id = new TagPermId(TEST_USER, "IDONTEXIST");
        assertObjectNotFoundException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagUpdate update = new TagUpdate();
                    update.setTagId(id);
                    updateTag(TEST_USER, PASSWORD, update);
                }
            }, id);
    }

    @Test
    public void testUpdateWithTagUnauthorized()
    {
        final ITagId id = new TagPermId(TEST_USER, "TEST_METAPROJECTS");
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    TagUpdate update = new TagUpdate();
                    update.setTagId(id);
                    updateTag(TEST_SPACE_USER, PASSWORD, update);
                }
            }, id);
    }

    @Test
    public void testUpdateWithDescription()
    {
        Tag before = getTag(TEST_USER, PASSWORD, new TagPermId(TEST_USER, "TEST_METAPROJECTS"));
        assertEquals(before.getDescription(), "Example metaproject no. 1");

        TagUpdate update = new TagUpdate();
        update.setTagId(before.getPermId());
        update.setDescription("brand new description");

        Tag after = updateTag(TEST_USER, PASSWORD, update);

        assertEquals(after.getDescription(), update.getDescription().getValue());
    }

    @Test
    public void testUpdateWithExperimentsAdd()
    {
        Tag before = getTag(TEST_USER, PASSWORD, new TagPermId(TEST_USER, "TEST_METAPROJECTS"));
        assertExperimentIdentifiers(before.getExperiments(), "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        TagUpdate update = new TagUpdate();
        update.setTagId(before.getPermId());
        update.getExperimentIds().add(new ExperimentIdentifier("/CISD/NEMO/EXP10"));

        Tag after = updateTag(TEST_USER, PASSWORD, update);

        assertExperimentIdentifiers(after.getExperiments(), "/CISD/NEMO/EXP10", "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testUpdateWithExperimentsRemove()
    {
        Tag before = getTag(TEST_USER, PASSWORD, new TagPermId(TEST_USER, "TEST_METAPROJECTS"));
        assertExperimentIdentifiers(before.getExperiments(), "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        TagUpdate update = new TagUpdate();
        update.setTagId(before.getPermId());
        update.getExperimentIds().remove(new ExperimentIdentifier("/CISD/NEMO/EXP11"));

        Tag after = updateTag(TEST_USER, PASSWORD, update);

        assertExperimentIdentifiers(after.getExperiments(), "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
    }

    @Test
    public void testUpdateWithExperimentsSet()
    {
        Tag before = getTag(TEST_USER, PASSWORD, new TagPermId(TEST_USER, "TEST_METAPROJECTS"));
        assertExperimentIdentifiers(before.getExperiments(), "/CISD/NEMO/EXP11", "/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");

        TagUpdate update = new TagUpdate();
        update.setTagId(before.getPermId());
        update.getExperimentIds().set(new ExperimentIdentifier("/CISD/NEMO/EXP10"));

        Tag after = updateTag(TEST_USER, PASSWORD, update);

        assertExperimentIdentifiers(after.getExperiments(), "/CISD/NEMO/EXP10");
    }

    @Test
    public void testUpdateWithExperimentsUnauthorized()
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    Tag before = getTag(TEST_SPACE_USER, PASSWORD, new TagPermId(TEST_SPACE_USER, "TEST_METAPROJECTS"));
                    assertEquals(before.getExperiments().size(), 0);

                    TagUpdate update = new TagUpdate();
                    update.setTagId(before.getPermId());
                    update.getExperimentIds().add(new ExperimentIdentifier("/CISD/NEMO/EXP10"));

                    updateTag(TEST_SPACE_USER, PASSWORD, update);
                }
            }, new ExperimentIdentifier("/CISD/NEMO/EXP10"));
    }

    @Test
    public void testUpdateWithSamplesAdd()
    {
        Tag before = getTag(TEST_USER, PASSWORD, new TagPermId(TEST_USER, "TEST_METAPROJECTS"));
        assertSampleIdentifiers(before.getSamples(), "/TEST-SPACE/EV-TEST");

        TagUpdate update = new TagUpdate();
        update.setTagId(before.getPermId());
        update.getSampleIds().add(new SampleIdentifier("/TEST-SPACE/FV-TEST"));

        Tag after = updateTag(TEST_USER, PASSWORD, update);

        assertSampleIdentifiers(after.getSamples(), "/TEST-SPACE/EV-TEST", "/TEST-SPACE/FV-TEST");
    }

    @Test
    public void testUpdateWithSamplesRemove()
    {
        Tag before = getTag(TEST_USER, PASSWORD, new TagPermId(TEST_USER, "TEST_METAPROJECTS"));
        assertSampleIdentifiers(before.getSamples(), "/TEST-SPACE/EV-TEST");

        TagUpdate update = new TagUpdate();
        update.setTagId(before.getPermId());
        update.getSampleIds().remove(new SampleIdentifier("/TEST-SPACE/EV-TEST"));

        Tag after = updateTag(TEST_USER, PASSWORD, update);

        assertEquals(after.getSamples().size(), 0);
    }

    @Test
    public void testUpdateWithSamplesSet()
    {
        Tag before = getTag(TEST_USER, PASSWORD, new TagPermId(TEST_USER, "TEST_METAPROJECTS"));
        assertSampleIdentifiers(before.getSamples(), "/TEST-SPACE/EV-TEST");

        TagUpdate update = new TagUpdate();
        update.setTagId(before.getPermId());
        update.getSampleIds().set(new SampleIdentifier("/TEST-SPACE/FV-TEST"));

        Tag after = updateTag(TEST_USER, PASSWORD, update);

        assertSampleIdentifiers(after.getSamples(), "/TEST-SPACE/FV-TEST");
    }

    @Test
    public void testUpdateWithSamplesUnauthorized()
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    Tag before = getTag(TEST_SPACE_USER, PASSWORD, new TagPermId(TEST_SPACE_USER, "TEST_METAPROJECTS"));
                    assertSampleIdentifiers(before.getSamples(), "/TEST-SPACE/EV-TEST", "/TEST-SPACE/FV-TEST");

                    TagUpdate update = new TagUpdate();
                    update.setTagId(before.getPermId());
                    update.getSampleIds().add(new SampleIdentifier("/CISD/CP-TEST-1"));

                    updateTag(TEST_SPACE_USER, PASSWORD, update);
                }
            }, new SampleIdentifier("/CISD/CP-TEST-1"));
    }

    @Test
    public void testUpdateWithDataSetsAdd()
    {
        Tag before = getTag(TEST_USER, PASSWORD, new TagPermId(TEST_USER, "TEST_METAPROJECTS"));
        assertDataSetCodes(before.getDataSets(), "20120619092259000-22");

        TagUpdate update = new TagUpdate();
        update.setTagId(before.getPermId());
        update.getDataSetIds().add(new DataSetPermId("20081105092159111-1"));

        Tag after = updateTag(TEST_USER, PASSWORD, update);

        assertDataSetCodes(after.getDataSets(), "20120619092259000-22", "20081105092159111-1");
    }

    @Test
    public void testUpdateWithDataSetsRemove()
    {
        Tag before = getTag(TEST_USER, PASSWORD, new TagPermId(TEST_USER, "TEST_METAPROJECTS"));
        assertDataSetCodes(before.getDataSets(), "20120619092259000-22");

        TagUpdate update = new TagUpdate();
        update.setTagId(before.getPermId());
        update.getDataSetIds().remove(new DataSetPermId("20120619092259000-22"));

        Tag after = updateTag(TEST_USER, PASSWORD, update);

        assertEquals(after.getDataSets().size(), 0);
    }

    @Test
    public void testUpdateWithDataSetsSet()
    {
        Tag before = getTag(TEST_USER, PASSWORD, new TagPermId(TEST_USER, "TEST_METAPROJECTS"));
        assertDataSetCodes(before.getDataSets(), "20120619092259000-22");

        TagUpdate update = new TagUpdate();
        update.setTagId(before.getPermId());
        update.getDataSetIds().set(new DataSetPermId("20081105092159111-1"));

        Tag after = updateTag(TEST_USER, PASSWORD, update);

        assertDataSetCodes(after.getDataSets(), "20081105092159111-1");
    }

    @Test
    public void testUpdateWithDataSetsUnauthorized()
    {
        assertUnauthorizedObjectAccessException(new IDelegatedAction()
            {
                @Override
                public void execute()
                {
                    Tag before = getTag(TEST_SPACE_USER, PASSWORD, new TagPermId(TEST_SPACE_USER, "TEST_METAPROJECTS"));
                    assertDataSetCodes(before.getDataSets(), "20120619092259000-22", "20120628092259000-24");

                    TagUpdate update = new TagUpdate();
                    update.setTagId(before.getPermId());
                    // data set connected to experiment /CISD/NEMO/EXP-TEST-1
                    update.getDataSetIds().add(new DataSetPermId("20081105092159111-1"));

                    updateTag(TEST_SPACE_USER, PASSWORD, update);
                }
            }, new DataSetPermId("20081105092159111-1"));
    }

    @Test
    public void testUpdateWithMaterialAdd()
    {
        Tag before = getTag(TEST_USER, PASSWORD, new TagPermId(TEST_USER, "TEST_METAPROJECTS"));
        assertMaterialPermIds(before.getMaterials(), new MaterialPermId("AD3", "VIRUS"));

        TagUpdate update = new TagUpdate();
        update.setTagId(before.getPermId());
        update.getMaterialIds().add(new MaterialPermId("AD5", "VIRUS"));

        Tag after = updateTag(TEST_USER, PASSWORD, update);

        assertMaterialPermIds(after.getMaterials(), new MaterialPermId("AD3", "VIRUS"), new MaterialPermId("AD5", "VIRUS"));
    }

    @Test
    public void testUpdateWithMaterialRemove()
    {
        Tag before = getTag(TEST_USER, PASSWORD, new TagPermId(TEST_USER, "TEST_METAPROJECTS"));
        assertMaterialPermIds(before.getMaterials(), new MaterialPermId("AD3", "VIRUS"));

        TagUpdate update = new TagUpdate();
        update.setTagId(before.getPermId());
        update.getMaterialIds().remove(new MaterialPermId("AD3", "VIRUS"));

        Tag after = updateTag(TEST_USER, PASSWORD, update);

        assertEquals(after.getMaterials().size(), 0);
    }

    @Test
    public void testUpdateWithMaterialSet()
    {
        Tag before = getTag(TEST_USER, PASSWORD, new TagPermId(TEST_USER, "TEST_METAPROJECTS"));
        assertMaterialPermIds(before.getMaterials(), new MaterialPermId("AD3", "VIRUS"));

        TagUpdate update = new TagUpdate();
        update.setTagId(before.getPermId());
        update.getMaterialIds().set(new MaterialPermId("AD5", "VIRUS"));

        Tag after = updateTag(TEST_USER, PASSWORD, update);

        assertMaterialPermIds(after.getMaterials(), new MaterialPermId("AD5", "VIRUS"));
    }

    @Test
    public void testUpdateWithMaterialUnauthorized()
    {
        // nothing to test as the materials can be accessed by every user
    }

    private Tag getTag(String user, String password, ITagId tagId)
    {
        TagFetchOptions fetchOptions = new TagFetchOptions();
        fetchOptions.withExperiments();
        fetchOptions.withSamples();
        fetchOptions.withDataSets();
        fetchOptions.withMaterials();
        fetchOptions.withOwner();

        String sessionToken = v3api.login(user, password);
        Map<ITagId, Tag> tags = v3api.mapTags(sessionToken, Arrays.asList(tagId), fetchOptions);
        return tags.get(tagId);
    }

    private Tag updateTag(String user, String password, TagUpdate update)
    {
        String sessionToken = v3api.login(user, password);

        v3api.updateTags(sessionToken, Arrays.asList(update));

        return getTag(user, password, update.getTagId());
    }

}
