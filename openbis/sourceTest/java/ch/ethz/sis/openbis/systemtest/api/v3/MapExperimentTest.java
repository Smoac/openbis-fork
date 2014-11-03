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
import static org.testng.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.attachment.Attachment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.Experiment;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.experiment.ExperimentType;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.project.Project;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.entity.tag.Tag;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.fetchoptions.experiment.ExperimentFetchOptions;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentIdentifier;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.IExperimentId;

/**
 * @author pkupczyk
 */
public class MapExperimentTest extends AbstractExperimentTest
{

    @Test
    public void testMapExperimentsByPermId()
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
    public void testMapExperimentsByIdentifier()
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
    public void testMapExperimentsByNonexistentIds()
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
    public void testMapExperimentsByDifferentIds()
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
    public void testMapExperimentsByDuplicatedIds()
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

        v3api.logout(sessionToken);
    }

    @Test
    public void testMapExperimentsByUnauthorizedIds()
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
    public void testMapExperimentsWithoutFetchOptions()
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
    public void testMapExperimentsWithType()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchType();

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
    public void testMapExperimentsWithAttachment()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchAttachments().fetchPreviousVersion().fetchPreviousVersion().fetchContent();

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
    public void testMapExperimentsWithProject()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchProject();

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

    @Test
    public void testMapExperimentsWithProperties()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchProperties();

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
    public void testMapExperimentsWithRegistrator()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchRegistrator();

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

    @Test
    public void testMapExperimentsWithModifier()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchModifier();

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

    @Test
    public void testMapExperimentsWithTags()
    {
        String sessionToken = v3api.login(TEST_USER, PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchTags();

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

}
