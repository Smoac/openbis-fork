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

package ch.systemsx.cisd.openbis.systemtest.api.v3;

import static org.testng.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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
import ch.ethz.sis.openbis.generic.shared.api.v3.dto.id.experiment.ExperimentPermId;

/**
 * @author pkupczyk
 */
public class ExperimentListTest extends AbstractExperimentTest
{

    @Test
    public void testListExperimentsByTechId()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        List<Experiment> experiments =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        new ExperimentFetchOptions());

        assertEquals(1, experiments.size());
        assertEquals(experiments.get(0).getIdentifier().toString(), "/CISD/NEMO/EXP1");
        v3api.logout(sessionToken);
    }

    @Test
    public void testListExperimentsByPermId()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        List<Experiment> experiments =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        new ExperimentFetchOptions());

        assertEquals(1, experiments.size());
        assertEquals(experiments.get(0).getIdentifier().toString(), "/CISD/NEMO/EXP1");
        v3api.logout(sessionToken);
    }

    @Test
    public void testListExperimentsByDifferentIds()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        List<Experiment> experiments =
                v3api.listExperiments(sessionToken,
                        Arrays.asList(new ExperimentPermId("200811050951882-1028"), new ExperimentPermId("200811050952663-1029"),
                                new ExperimentPermId(
                                        "200811050952663-1030")),
                        new ExperimentFetchOptions());
        assertEquals(3, experiments.size());
        assertEquals(experiments.get(0).getIdentifier().toString(), "/CISD/NEMO/EXP1");
        assertEquals(experiments.get(1).getPermId().toString(), "200811050952663-1029");
        assertEquals(experiments.get(2).getIdentifier().toString(), "/CISD/NEMO/EXP11");
        v3api.logout(sessionToken);
    }

    @Test
    public void testListExperimentsWithoutFetchOptions()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        List<Experiment> experiments =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        new ExperimentFetchOptions());

        assertEquals(1, experiments.size());

        Experiment experiment = experiments.get(0);
        assertEquals(experiment.getPermId().toString(), "200811050951882-1028");
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");
        assertEquals(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(experiment.getRegistrationDate()), "2008-11-05 09:21:51");

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
    public void testListExperimentsWithType()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchType();

        List<Experiment> experiments =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        fetchOptions);

        assertEquals(1, experiments.size());

        Experiment experiment = experiments.get(0);
        assertEquals(experiment.getPermId().toString(), "200811050951882-1028");
        assertEquals(experiment.getCode(), "EXP1");
        assertEquals(experiment.getIdentifier().toString(), "/CISD/NEMO/EXP1");

        ExperimentType type = experiment.getType();
        assertEquals(type.getPermId().toString(), "SIRNA_HCS");
        assertEquals(type.getCode(), "SIRNA_HCS");
        assertEquals(type.getDescription(), "Small Interfering RNA High Content Screening");

        assertProjectNotFetched(experiment);
        assertPropertiesNotFetched(experiment);
        assertTagsNotFetched(experiment);
        assertRegistratorNotFetched(experiment);
        assertModifierNotFetched(experiment);
        assertAttachmentsNotFetched(experiment);
        v3api.logout(sessionToken);
    }

    @Test
    public void testListExperimentsWithAttachment()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchAttachments().fetchPreviousVersion().fetchPreviousVersion().fetchContent();

        List<Experiment> experiments =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        fetchOptions);

        assertEquals(1, experiments.size());

        Experiment experiment = experiments.get(0);

        assertEquals(experiment.getPermId().toString(), "200811050951882-1028");
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
    public void testListExperimentsWithProject()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchProject();

        List<Experiment> experiments =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        fetchOptions);

        assertEquals(1, experiments.size());

        Experiment experiment = experiments.get(0);
        assertEquals(experiment.getPermId().toString(), "200811050951882-1028");
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
    public void testListExperimentsWithProperties()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchProperties();

        List<Experiment> experiments =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        fetchOptions);

        assertEquals(1, experiments.size());

        Experiment experiment = experiments.get(0);
        assertEquals(experiment.getPermId().toString(), "200811050951882-1028");
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
    public void testListExperimentsWithRegistrator()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchRegistrator();

        List<Experiment> experiments =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        fetchOptions);

        assertEquals(1, experiments.size());

        Experiment experiment = experiments.get(0);
        assertEquals(experiment.getPermId().toString(), "200811050951882-1028");
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
    public void testListExperimentsWithModifier()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchModifier();

        List<Experiment> experiments =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050951882-1028")),
                        fetchOptions);

        assertEquals(1, experiments.size());

        Experiment experiment = experiments.get(0);
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
    public void testListExperimentsWithTags()
    {
        String sessionToken = v3api.login(TEST_USER, TEST_USER_PASSWORD);
        ExperimentFetchOptions fetchOptions = new ExperimentFetchOptions();
        fetchOptions.fetchTags();

        List<Experiment> experiments =
                v3api.listExperiments(sessionToken, Collections.singletonList(new ExperimentPermId("200811050952663-1030")),
                        fetchOptions);

        assertEquals(1, experiments.size());

        Experiment experiment = experiments.get(0);
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
