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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.dataset.DataSet;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.sample.Sample;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;
import ch.systemsx.cisd.common.test.AssertionUtil;

/**
 * @author pkupczyk
 */
public class MapExperimentTest extends AbstractExperimentTest
{

    @Test
    public void testMapByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId permId1 = new ExperimentPermId("200811050951882-1028");
        ExperimentPermId permId2 = new ExperimentPermId("200811050952663-1029");

        Map<IExperimentId, Experiment> map =
                v3api.mapExperiments(sessionToken, Arrays.asList(permId1, permId2),
                        new ExperimentFetchOptions());

        assertEquals(2, map.size());

        Iterator<Experiment> iter = map.values().iterator();
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdentifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NOE/EXP-TEST-2");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/TEST-SPACE/NOE/EXP-TEST-2");
        ExperimentIdentifier identifier3 = new ExperimentIdentifier("/CISD/NEMO/EXP10");

        Map<IExperimentId, Experiment> map =
                v3api.mapExperiments(sessionToken,
                        Arrays.asList(identifier1, identifier2, identifier3),
                        new ExperimentFetchOptions());

        assertEquals(3, map.size());

        Iterator<Experiment> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getIdentifier(), identifier2);
        assertEquals(iter.next().getIdentifier(), identifier3);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);
        assertEquals(map.get(identifier3).getIdentifier(), identifier3);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsNonexistent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NOE/EXP-TEST-2");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/TEST-SPACE/NOE/EXP-TEST-2");
        ExperimentIdentifier identifier3 = new ExperimentIdentifier("/CISD/NEMO/EXP10");
        ExperimentIdentifier identifier4 = new ExperimentIdentifier("/NONEXISTENT_SPACE/NEMO/EXP1");
        ExperimentIdentifier identifier5 = new ExperimentIdentifier("/CISD/NONEXISTENT_PROJECT/EXP1");
        ExperimentIdentifier identifier6 = new ExperimentIdentifier("/CISD/NEMO/EXP11");
        ExperimentPermId permId1 = new ExperimentPermId("200811050951882-1028");
        ExperimentPermId permId2 = new ExperimentPermId("200811050952663-1029");
        ExperimentIdentifier identifier7 = new ExperimentIdentifier("/CISD/NONEXISTENT_PROJECT/EXP1");
        ExperimentPermId permId3 = new ExperimentPermId("NONEXISTENT_EXPERIMENT");
        ExperimentIdentifier identifier8 = new ExperimentIdentifier("/CISD/NEMO/NONEXISTENT_EXPERIMENT");

        Map<IExperimentId, Experiment> map =
                v3api.mapExperiments(sessionToken,
                        Arrays.asList(identifier1, identifier2, identifier3, identifier4, identifier5, identifier6, permId1, permId2, identifier7,
                                permId3, identifier8),
                        new ExperimentFetchOptions());

        assertEquals(6, map.size());

        Iterator<Experiment> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getIdentifier(), identifier2);
        assertEquals(iter.next().getIdentifier(), identifier3);
        assertEquals(iter.next().getIdentifier(), identifier6);
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getPermId(), permId2);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);
        assertEquals(map.get(identifier3).getIdentifier(), identifier3);
        assertEquals(map.get(identifier6).getIdentifier(), identifier6);
        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(permId2).getPermId(), permId2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsDifferent()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        ExperimentPermId permId = new ExperimentPermId("200811050952663-1029");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/CISD/NEMO/EXP11");

        Map<IExperimentId, Experiment> map =
                v3api.mapExperiments(sessionToken, Arrays.asList(identifier1, permId, identifier2), new ExperimentFetchOptions());

        assertEquals(3, map.size());

        Iterator<Experiment> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getPermId(), permId);
        assertEquals(iter.next().getIdentifier(), identifier2);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(permId).getPermId(), permId);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsDuplicated()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        // "/CISD/NEMO/EXP1" and "200811050951882-1028" is the same experiment
        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        ExperimentPermId permId1 = new ExperimentPermId("200811050951882-1028");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/CISD/NEMO/EXP11");
        ExperimentPermId permId2 = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map =
                v3api.mapExperiments(sessionToken, Arrays.asList(identifier1, permId1, identifier2, permId2), new ExperimentFetchOptions());

        assertEquals(3, map.size());

        Iterator<Experiment> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier1);
        assertEquals(iter.next().getPermId(), permId1);
        assertEquals(iter.next().getIdentifier(), identifier2);

        assertEquals(map.get(identifier1).getIdentifier(), identifier1);
        assertEquals(map.get(permId1).getPermId(), permId1);
        assertEquals(map.get(identifier2).getIdentifier(), identifier2);

        assertTrue(map.get(identifier1) == map.get(permId1));

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapByIdsUnauthorized()
    {
        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/TEST-SPACE/TEST-PROJECT/EXP-SPACE-TEST");
        ExperimentIdentifier identifier3 = new ExperimentIdentifier("/CISD/NEMO/EXP11");
        ExperimentIdentifier identifier4 = new ExperimentIdentifier("/TEST-SPACE/NOE/EXP-TEST-2");

        List<? extends IExperimentId> ids = Arrays.asList(identifier1, identifier2, identifier3, identifier4);

        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, ids, new ExperimentFetchOptions());

        assertEquals(map.size(), 4);
        v3api.logout(sessionToken);

        sessionToken = v3api.login(TEST_SPACE_USER, PASSWORD);
        map = v3api.mapExperiments(sessionToken, ids, new ExperimentFetchOptions());

        assertEquals(map.size(), 2);

        Iterator<Experiment> iter = map.values().iterator();
        assertEquals(iter.next().getIdentifier(), identifier2);
        assertEquals(iter.next().getIdentifier(), identifier4);

        assertEquals(map.get(identifier2).getIdentifier(), identifier2);
        assertEquals(map.get(identifier4).getIdentifier(), identifier4);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithFetchOptionsEmpty()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Collections.singletonList(permId), new ExperimentFetchOptions());

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId(), permId);
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(experiment.getRegistrationDate()), "2008-11-05 09:21:51");
        assertNotNull(experiment.getModificationDate());

        assertTypeNotFetched(experiment);
        assertProjectNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertAttachmentsNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        assertModifierNotFetched(experiment);

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withType();

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId().toString(), "200811050951882-1028");
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");

        ExperimentType type = experiment.getType();
        assertEquals(type.getPermId().toString(), "SIRNA_HCS");
        assertEquals(type.getCode(), "SIRNA_HCS");
        assertEquals(type.getDescription(), "Small Interfering RNA High Content Screening");
        assertNotNull(type.getModificationDate());

        assertProjectNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        assertModifierNotFetched(experiment);
        assertAttachmentsNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithTypeReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withType();

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/CISD/NEMO/EXP11");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Arrays.asList(identifier1, identifier2), fetchOptions);

        assertEquals(2, map.size());
        Experiment experiment1 = map.get(identifier1);
        Experiment experiment2 = map.get(identifier2);

        assertFalse(experiment1 == experiment2);
        assertEquals(experiment1.getType().getCode(), "SIRNA_HCS");
        assertEquals(experiment2.getType().getCode(), "SIRNA_HCS");
        assertTrue(experiment1.getType() == experiment2.getType());

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithAttachment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withAttachments().withPreviousVersion().withPreviousVersion().withContent();

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId(), permId);
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");

        List<Attachment> attachments = experiment.getAttachments(); // 1 of them

        Attachment attachment4 = attachments.get(0);
        assertAttachmentContentNotFetched(attachment4);

        Attachment attachment3 = attachment4.getPreviousVersion();
        assertAttachmentContentNotFetched(attachment3);

        Attachment attachment2 = attachment3.getPreviousVersion();
        assertPreviousAttachmentNotFetched(attachment2);
        assertEquals(attachment2.getContent().length, 228);

        assertTypeNotFetched(experiment);
        assertProjectNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        assertModifierNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test()
    public void testMapWithProject()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProject();

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId(), permId);
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");

        Project project = experiment.getProject();
        assertEquals(project.getPermId().toString(), "20120814110011738-103");
        assertEquals(project.getCode(), "NEMO");
        assertEquals(project.getIdentifier().toString(), "/CISD/NEMO");
        assertEquals(project.getDescription(), "nemo description");

        assertTypeNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        assertModifierNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test()
    public void testMapWithProjectReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProject();

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/CISD/NEMO/EXP11");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Arrays.asList(identifier1, identifier2), fetchOptions);

        assertEquals(2, map.size());
        Experiment experiment1 = map.get(identifier1);
        Experiment experiment2 = map.get(identifier2);

        assertFalse(experiment1 == experiment2);
        assertEquals(experiment1.getProject().getCode(), "NEMO");
        assertEquals(experiment2.getProject().getCode(), "NEMO");
        assertTrue(experiment1.getProject() == experiment2.getProject());

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withProperties();

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId(), permId);
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");

        Map<String, String> properties = experiment.getProperties();
        assertEquals(properties.size(), 2);
        assertEquals(properties.get("DESCRIPTION"), "A simple experiment");
        assertEquals(properties.get("GENDER"), "MALE");

        assertTypeNotFetched(experiment);
        assertProjectNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        assertModifierNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithRegistrator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withRegistrator();

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId(), permId);
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");

        assertEquals(experiment.getRegistrator().getUserId(), "test");
        assertRegistratorNotFetched(experiment.getRegistrator());

        assertTypeNotFetched(experiment);
        assertProjectNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertModifierNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test()
    public void testMapWithRegistratorReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withRegistrator();

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/CISD/NEMO/EXP11");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Arrays.asList(identifier1, identifier2), fetchOptions);

        assertEquals(2, map.size());
        Experiment experiment1 = map.get(identifier1);
        Experiment experiment2 = map.get(identifier2);

        assertFalse(experiment1 == experiment2);
        assertEquals(experiment1.getRegistrator().getUserId(), "test");
        assertEquals(experiment2.getRegistrator().getUserId(), "test");
        assertTrue(experiment1.getRegistrator() == experiment2.getRegistrator());

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithModifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withModifier();

        ExperimentPermId permId = new ExperimentPermId("200811050951882-1028");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId().toString(), "200811050951882-1028");
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");

        assertEquals(experiment.getModifier().getUserId(), "test_role");
        assertRegistratorNotFetched(experiment.getModifier());

        assertTypeNotFetched(experiment);
        assertProjectNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test()
    public void testMapWithModifierReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withModifier();

        ExperimentIdentifier identifier1 = new ExperimentIdentifier("/CISD/NEMO/EXP1");
        ExperimentIdentifier identifier2 = new ExperimentIdentifier("/CISD/NEMO/EXP10");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Arrays.asList(identifier1, identifier2), fetchOptions);

        assertEquals(2, map.size());
        Experiment experiment1 = map.get(identifier1);
        Experiment experiment2 = map.get(identifier2);

        assertFalse(experiment1 == experiment2);
        assertEquals(experiment1.getModifier().getUserId(), "test_role");
        assertEquals(experiment2.getModifier().getUserId(), "test_role");
        assertTrue(experiment1.getModifier() == experiment2.getModifier());

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithTags()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withTags();

        ExperimentPermId permId = new ExperimentPermId("200811050952663-1030");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Collections.singletonList(permId), fetchOptions);

        assertEquals(1, map.size());

        Experiment experiment = map.get(permId);
        assertEquals(experiment.getPermId().toString(), "200811050952663-1030");
        assertEquals(experiment.getCode(), "EXP11");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP11");
        assertEquals(experiment.getTags().size(), 2);

        Set<String> actualTags = new HashSet<String>();
        for (Tag tag : experiment.getTags())
        {
            actualTags.add(tag.getPermId().getPermId());
        }
        assertEquals(actualTags, new HashSet<String>(Arrays.asList("/test/TEST_METAPROJECTS", "/test/ANOTHER_TEST_METAPROJECTS")));

        assertTypeNotFetched(experiment);
        assertProjectNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        assertModifierNotFetched(experiment);
        assertAttachmentsNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test
    public void testMapWithTagsReused()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withTags();

        ExperimentPermId permId1 = new ExperimentPermId("200811050952663-1030");
        ExperimentPermId permId2 = new ExperimentPermId("201206190940555-1032");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Arrays.asList(permId1, permId2), fetchOptions);

        assertEquals(2, map.size());
        Experiment experiment1 = map.get(permId1);
        Experiment experiment2 = map.get(permId2);

        assertEquals(experiment1.getTags().size(), 2);
        assertEquals(experiment2.getTags().size(), 1);
        assertContainSameObjects(experiment1.getTags(), experiment2.getTags(), 1);

        v3api.logout(sessionToken);
    }

    @Test
    public void testWithDataSets()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withDataSets();

        ExperimentPermId permId = new ExperimentPermId("200902091239077-1033");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Arrays.asList(permId), fetchOptions);

        Experiment experiment = map.get(permId);

        List<DataSet> dataSets = experiment.getDataSets();
        assertEquals(dataSets.size(), 1);
        DataSet dataSet = dataSets.get(0);
        assertEquals(dataSet.getPermId().getPermId(), "20081105092159111-1");

        assertTypeNotFetched(dataSet);
        assertTagsNotFetched(dataSet);

        v3api.logout(sessionToken);
    }

    @Test
    public void testWithSamples()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withSamples();

        ExperimentPermId permId = new ExperimentPermId("200902091239077-1033");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Arrays.asList(permId), fetchOptions);

        Experiment experiment = map.get(permId);
        List<Sample> samples = experiment.getSamples();
        Collection<String> codes = CollectionUtils.collect(samples, new Transformer<Sample, String>()
            {
                @Override
                public String transform(Sample input)
                {
                    return input.getCode();
                }
            });
        AssertionUtil.assertCollectionContainsOnly(codes, "CP-TEST-1", "DYNA-TEST-1");
        v3api.logout(sessionToken);
    }

    public void testWithDataSetsAndDataSetFetchOptionsViaSample()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.withDataSets().withTags();
        fetchOptions.withSamples().withDataSets().withType();

        ExperimentPermId permId = new ExperimentPermId("200902091239077-1033");

        Map<IExperimentId, Experiment> map = v3api.mapExperiments(sessionToken, Arrays.asList(permId), fetchOptions);

        Experiment experiment = map.get(permId);

        List<DataSet> dataSets = experiment.getDataSets();
        AssertionUtil.assertCollectionSize(dataSets, 1);
        DataSet dataSet = dataSets.get(0);
        assertEquals(dataSet.getPermId().getPermId(), "20081105092159111-1");

        assertEquals(dataSet.getType().getCode(), "HCS_IMAGE");
        dataSet.getTags();

        v3api.logout(sessionToken);
    }

}
