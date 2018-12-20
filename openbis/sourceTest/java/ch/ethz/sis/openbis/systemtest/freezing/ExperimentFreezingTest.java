/*
 * Copyright 2018 ETH Zuerich, SIS
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

package ch.ethz.sis.openbis.systemtest.freezing;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.attachment.id.AttachmentFileName;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.deletion.id.IDeletionId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.create.ExperimentCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.delete.ExperimentDeletionOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.id.ExperimentPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.experiment.update.ExperimentUpdate;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.create.TagCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.tag.id.TagPermId;

/**
 * @author Franz-Josef Elmer
 */
public class ExperimentFreezingTest extends FreezingTest
{
    private static final String PREFIX = "EFT-";

    private static final String EXPERIMENT_1 = PREFIX + "1";

    private static final String EXPERIMENT_2 = PREFIX + "2";

    private ExperimentPermId experiment1;

    private ExperimentPermId experiment2;

    private TagPermId blueTag;

    @BeforeMethod
    public void createExamples()
    {
        ExperimentCreation e1 = experiment(DEFAULT_PROJECT_ID, EXPERIMENT_1);
        e1.setProperty("DESCRIPTION", "testing");
        e1.setAttachments(Arrays.asList(attachment("f1.txt", "T1", "my t1", "abcdefgh")));
        ExperimentCreation e2 = experiment(DEFAULT_PROJECT_ID, EXPERIMENT_2);
        List<ExperimentPermId> experiments = v3api.createExperiments(systemSessionToken, Arrays.asList(e1, e2));
        experiment1 = experiments.get(0);
        experiment2 = experiments.get(1);

        TagCreation tagCreation = new TagCreation();
        tagCreation.setCode("blue");
        tagCreation.setExperimentIds(Arrays.asList(experiment2));
        blueTag = v3api.createTags(systemSessionToken, Arrays.asList(tagCreation)).get(0);
    }

    @Test
    public void testTrash()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment1);
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("test");

        // When
        assertUserFailureException(Void -> v3api.deleteExperiments(systemSessionToken, Arrays.asList(experiment1), deletionOptions),
                // Then
                "ERROR: Operation TRASH is not allowed because experiment " + EXPERIMENT_1 + " is frozen.");
    }

    @Test
    public void testDeletePermanently()
    {
        // Given
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteExperiments(systemSessionToken, Arrays.asList(experiment1), deletionOptions);
        setFrozenFlagForExperiments(true, experiment1);

        // When
        assertUserFailureException(Void -> v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId)),
                // Then
                "ERROR: Operation DELETE PROPERTY is not allowed because experiment " + EXPERIMENT_1 + " is frozen.");
    }

    @Test
    public void testDeletePermanentlyExperimentWithoutPropertiesAndAttachments()
    {
        // Given
        ExperimentDeletionOptions deletionOptions = new ExperimentDeletionOptions();
        deletionOptions.setReason("test");
        IDeletionId deletionId = v3api.deleteExperiments(systemSessionToken, Arrays.asList(experiment2), deletionOptions);
        setFrozenFlagForExperiments(true, experiment2);

        // When
        assertUserFailureException(Void -> v3api.confirmDeletions(systemSessionToken, Arrays.asList(deletionId)),
                // Then
                "ERROR: Operation DELETE is not allowed because experiment " + EXPERIMENT_2 + " is frozen.");
    }

    @Test
    public void testAddTag()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment1);
        assertEquals(getExperiment(experiment1).getTags().toString(), "[]");
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment1);
        experimentUpdate.getTagIds().add(blueTag);

        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));

        // Then
        assertEquals(getExperiment(experiment1).getTags().toString(), "[Tag blue]");
    }

    @Test
    public void testRemoveTag()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment2);
        assertEquals(getExperiment(experiment2).getTags().toString(), "[Tag blue]");
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment2);
        experimentUpdate.getTagIds().remove(blueTag);

        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));

        // Then
        assertEquals(getExperiment(experiment2).getTags().toString(), "[]");
    }

    @Test
    public void testAddProperty()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment2);
        assertEquals(getExperiment(experiment2).getProperty("DESCRIPTION"), null);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment2);
        experimentUpdate.setProperty("DESCRIPTION", "description added");

        // When
        assertUserFailureException(Void -> v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate)),
                // Then
                "ERROR: Operation INSERT PROPERTY is not allowed because experiment " + EXPERIMENT_2 + " is frozen.");
    }

    @Test
    public void testAddPropertyForMoltenExperiment()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment2);
        assertEquals(getExperiment(experiment2).getProperty("DESCRIPTION"), null);
        setFrozenFlagForExperiments(false, experiment2);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment2);
        experimentUpdate.setProperty("DESCRIPTION", "description added");

        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));

        // Then
        assertEquals(getExperiment(experiment2).getProperty("DESCRIPTION"), "description added");
    }

    @Test
    public void testChangeProperty()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment1);
        assertEquals(getExperiment(experiment1).getProperty("DESCRIPTION"), "testing");
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment1);
        experimentUpdate.setProperty("DESCRIPTION", "description added");

        // When
        assertUserFailureException(Void -> v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate)),
                // Then
                "ERROR: Operation UPDATE PROPERTY is not allowed because experiment " + EXPERIMENT_1 + " is frozen.");
    }

    @Test
    public void testChangePropertyForMoltenExperiment()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment1);
        assertEquals(getExperiment(experiment1).getProperty("DESCRIPTION"), "testing");
        setFrozenFlagForExperiments(false, experiment1);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment1);
        experimentUpdate.setProperty("DESCRIPTION", "description added");

        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));

        // Then
        assertEquals(getExperiment(experiment1).getProperty("DESCRIPTION"), "description added");
    }

    @Test
    public void testDeleteProperty()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment1);
        assertEquals(getExperiment(experiment1).getProperty("DESCRIPTION"), "testing");
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment1);
        experimentUpdate.setProperty("DESCRIPTION", null);

        // When
        assertUserFailureException(Void -> v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate)),
                // Then
                "ERROR: Operation DELETE PROPERTY is not allowed because experiment " + EXPERIMENT_1 + " is frozen.");
    }

    @Test
    public void testDeletePropertyForMoltenExperiment()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment1);
        assertEquals(getExperiment(experiment1).getProperty("DESCRIPTION"), "testing");
        setFrozenFlagForExperiments(false, experiment1);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment1);
        experimentUpdate.setProperty("DESCRIPTION", null);

        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));

        // Then
        assertEquals(getExperiment(experiment1).getProperty("DESCRIPTION"), null);
    }

    @Test
    public void testAddAttachment()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment2);
        assertEquals(getExperiment(experiment2).getAttachments().toString(), "[]");
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment2);
        experimentUpdate.getAttachments().add(attachment("f2.txt", "F2", "my f2", "3.14159"));

        // When
        assertUserFailureException(Void -> v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate)),
                // Then
                "ERROR: Operation INSERT ATTACHMENT is not allowed because experiment " + EXPERIMENT_2 + " is frozen.");
    }

    @Test
    public void testAddAttachmentForMoltenExperiment()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment2);
        assertEquals(getExperiment(experiment2).getAttachments().toString(), "[]");
        setFrozenFlagForExperiments(false, experiment2);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment2);
        experimentUpdate.getAttachments().add(attachment("f2.txt", "F2", "my f2", "3.14159"));

        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));

        // Then
        assertEquals(getExperiment(experiment2).getAttachments().get(0).getDescription(), "my f2");
    }

    @Test
    public void testDeleteAttachment()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment1);
        assertEquals(getExperiment(experiment1).getAttachments().get(0).getDescription(), "my t1");
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment1);
        experimentUpdate.getAttachments().remove(new AttachmentFileName("f1.txt"));

        // When
        assertUserFailureException(Void -> v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate)),
                // Then
                "ERROR: Operation DELETE ATTACHMENT is not allowed because experiment " + EXPERIMENT_1 + " is frozen.");
    }
    @Test
    public void testDeleteAttachmentForMoltenExperiment()
    {
        // Given
        setFrozenFlagForExperiments(true, experiment1);
        assertEquals(getExperiment(experiment1).getAttachments().get(0).getDescription(), "my t1");
        setFrozenFlagForExperiments(false, experiment1);
        ExperimentUpdate experimentUpdate = new ExperimentUpdate();
        experimentUpdate.setExperimentId(experiment1);
        experimentUpdate.getAttachments().remove(new AttachmentFileName("f1.txt"));
        
        // When
        v3api.updateExperiments(systemSessionToken, Arrays.asList(experimentUpdate));

        // Then
        assertEquals(getExperiment(experiment1).getAttachments().size(), 0);
    }
}
