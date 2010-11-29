/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.systemtest.plugin.generic;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.BatchRegistrationResult;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;

/**
 * Head-less system test for experiment registration
 * 
 * @author Franz-Josef Elmer
 */
@Test(groups = "system test")
public class ExperimentRegistrationTest extends GenericSystemTestCase
{
    private static final String ATTACHMENTS_SESSION_KEY = "attachments";

    private static final String SAMPLES_SESSION_KEY = "samples";

    private static final String EXPERIMENTS_SESSION_KEY = "EXPERIMENTS";

    @Test
    public void testRegisterExperimentWithoutMissingMandatoryProperty()
    {
        logIntoCommonClientService();
        String experimentCode = commonClientService.generateCode("EXP");
        String experimentIdentifier = "/cisd/default/" + experimentCode;
        NewExperiment newExperiment = new NewExperiment(experimentIdentifier, "SIRNA_HCS");

        try
        {
            genericClientService.registerExperiment(ATTACHMENTS_SESSION_KEY, SAMPLES_SESSION_KEY,
                    newExperiment);
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Value of mandatory property 'DESCRIPTION' not specified.",
                    ex.getMessage());
        }
    }

    @Test
    public void testRegisterExperiment()
    {
        logIntoCommonClientService();
        String experimentCode = commonClientService.generateCode("EXP");
        String experimentIdentifier = "/cisd/default/" + experimentCode;
        NewExperiment newExperiment = new NewExperiment(experimentIdentifier, "SIRNA_HCS");
        newExperiment.setProperties(new IEntityProperty[]
            { property("DESCRIPTION", "my éxpériment") });
        genericClientService.registerExperiment(ATTACHMENTS_SESSION_KEY, SAMPLES_SESSION_KEY,
                newExperiment);

        Experiment experiment = commonClientService.getExperimentInfo(experimentIdentifier);
        assertEquals(experimentCode, experiment.getCode());
        assertEquals(experimentIdentifier.toUpperCase(), experiment.getIdentifier());
        assertEquals("SIRNA_HCS", experiment.getExperimentType().getCode());
        List<IEntityProperty> properties = experiment.getProperties();
        assertEquals("DESCRIPTION", properties.get(0).getPropertyType().getCode());
        // Make sure the string is escaped
        assertEquals(StringEscapeUtils.escapeHtml("my éxpériment"), properties.get(0)
                .tryGetAsString());
        assertEquals(1, properties.size());
    }

    @Test
    public void testRegisterExperimentWithSamples()
    {
        logIntoCommonClientService();
        String experimentCode = commonClientService.generateCode("EXP");
        String experimentIdentifier = "/cisd/default/" + experimentCode;
        NewExperiment newExperiment = new NewExperiment(experimentIdentifier, "SIRNA_HCS");
        newExperiment.setProperties(new IEntityProperty[]
            { property("DESCRIPTION", "my experiment") });
        newExperiment.setSamples(new String[]
            { "3vcp8" });
        genericClientService.registerExperiment(ATTACHMENTS_SESSION_KEY, SAMPLES_SESSION_KEY,
                newExperiment);

        Experiment experiment = commonClientService.getExperimentInfo(experimentIdentifier);
        TechId experimentId = new TechId(experiment.getId());
        ListSampleCriteria listCriteria = ListSampleCriteria.createForExperiment(experimentId);
        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));

        assertEquals("[CELL_PLATE]", samples.getAvailableEntityTypes().toString());
        Sample sample = samples.getResultSet().getList().get(0).getOriginalObject();
        assertEquals("3VCP8", sample.getCode());
        assertEquals(experiment.getId(), sample.getExperiment().getId());
        assertEquals(1, samples.getResultSet().getList().size());
    }

    @Test
    public void testRegisterExperimentAndSamples()
    {
        logIntoCommonClientService();

        String batchSamplesFileContent = "identifier\torganism\n" + "S1001\tfly\n" + "S1002\tdog\n";
        addMultiPartFile(SAMPLES_SESSION_KEY, "samples.txt", batchSamplesFileContent.getBytes());
        String experimentCode = commonClientService.generateCode("EXP");
        String experimentIdentifier = "/cisd/default/" + experimentCode;
        NewExperiment newExperiment = new NewExperiment(experimentIdentifier, "SIRNA_HCS");
        newExperiment.setProperties(new IEntityProperty[]
            { property("DESCRIPTION", "my experiment") });
        newExperiment.setRegisterSamples(true);
        SampleType sampleType = new SampleType();
        sampleType.setCode("CELL_PLATE");
        newExperiment.setSampleType(sampleType);
        genericClientService.registerExperiment(ATTACHMENTS_SESSION_KEY, SAMPLES_SESSION_KEY,
                newExperiment);

        Experiment experiment = commonClientService.getExperimentInfo(experimentIdentifier);
        TechId experimentId = new TechId(experiment.getId());
        ListSampleCriteria listCriteria = ListSampleCriteria.createForExperiment(experimentId);
        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));

        assertEquals("[CELL_PLATE]", samples.getAvailableEntityTypes().toString());
        List<GridRowModel<Sample>> list =
                new ArrayList<GridRowModel<Sample>>(samples.getResultSet().getList());
        Collections.sort(list, new Comparator<GridRowModel<Sample>>()
            {
                public int compare(GridRowModel<Sample> o1, GridRowModel<Sample> o2)
                {
                    return o1.getOriginalObject().getCode()
                            .compareTo(o2.getOriginalObject().getCode());
                }
            });
        Sample sample = list.get(0).getOriginalObject();
        assertEquals("S1001", sample.getCode());
        assertEquals("FLY", sample.getProperties().get(0).tryGetAsString());
        assertEquals(experiment.getId(), sample.getExperiment().getId());
        sample = list.get(1).getOriginalObject();
        assertEquals("S1002", sample.getCode());
        assertEquals("DOG", sample.getProperties().get(0).tryGetAsString());
        assertEquals(experiment.getId(), sample.getExperiment().getId());
        assertEquals(2, list.size());
    }

    @Test
    public void testRegisterExperimentAndAttachments()
    {
        String sessionToken = logIntoCommonClientService().getSessionID();

        addMultiPartFile(ATTACHMENTS_SESSION_KEY, "hello.txt", "hello world".getBytes());
        String experimentCode = commonClientService.generateCode("EXP");
        String experimentIdentifier = "/cisd/default/" + experimentCode;
        NewExperiment newExperiment = new NewExperiment(experimentIdentifier, "SIRNA_HCS");
        newExperiment.setProperties(new IEntityProperty[]
            { property("DESCRIPTION", "my experiment") });
        newExperiment.setAttachments(Arrays.asList(new NewAttachment("hello.txt", "hello",
                "test attachment")));
        genericClientService.registerExperiment(ATTACHMENTS_SESSION_KEY, SAMPLES_SESSION_KEY,
                newExperiment);

        Experiment experiment = commonClientService.getExperimentInfo(experimentIdentifier);
        assertEquals(experimentCode, experiment.getCode());
        List<Attachment> attachments = experiment.getAttachments();
        assertEquals("hello.txt", attachments.get(0).getFileName());
        assertEquals("test attachment", attachments.get(0).getDescription());
        assertEquals("hello", attachments.get(0).getTitle());
        assertEquals(1, attachments.get(0).getVersion());
        assertEquals(1, attachments.size());

        TechId experimentID = new TechId(experiment.getId());
        AttachmentWithContent attachment =
                genericServer.getExperimentFileAttachment(sessionToken, experimentID, "hello.txt",
                        1);
        assertEquals("hello.txt", attachment.getFileName());
        assertEquals("test attachment", attachment.getDescription());
        assertEquals("hello", attachment.getTitle());
        assertEquals(1, attachment.getVersion());
        assertEquals("hello world", new String(attachment.getContent()));
    }

    @Test
    public void testBulkUpdateExperiments()
    {
        logIntoCommonClientService();

        int expCount = 10;
        // Create some experiments to update
        ArrayList<String> expIds = registerNewExperiments(expCount);
        String bulkUpdateString = createBulkUpdateString(expIds);

        // Update the experiments
        addMultiPartFile(EXPERIMENTS_SESSION_KEY, "experiments.txt", bulkUpdateString.getBytes());
        ExperimentType experimentType = new ExperimentType();
        experimentType.setCode("SIRNA_HCS");
        List<BatchRegistrationResult> results =
                genericClientService.updateExperiments(experimentType, EXPERIMENTS_SESSION_KEY);

        // Check the return value
        assertEquals(1, results.size());
        assertEquals("Update of " + expCount + " experiment(s) is complete.", results.get(0)
                .getMessage());

        // Verify the results
        verifyBulkUpdate(expIds);
    }

    /**
     * Register &lt;count&gt; new experiments, returning the identifiers of the new experiments
     */
    private ArrayList<String> registerNewExperiments(int count)
    {
        ArrayList<String> expIds = new ArrayList<String>();
        for (int i = 0; i < count; ++i)
        {
            String experimentCode = commonClientService.generateCode("EXP");
            String experimentIdentifier = "/cisd/default/" + experimentCode;
            NewExperiment newExperiment = new NewExperiment(experimentIdentifier, "SIRNA_HCS");
            newExperiment.setProperties(new IEntityProperty[]
                { property("DESCRIPTION", "my éxpériment") });
            genericClientService.registerExperiment(ATTACHMENTS_SESSION_KEY, SAMPLES_SESSION_KEY,
                    newExperiment);
            expIds.add(experimentIdentifier);
        }
        return expIds;
    }

    private String createBulkUpdateString(ArrayList<String> expIds)
    {
        StringBuilder sb = new StringBuilder();
        // Write the header
        sb.append("identifier\tDESCRIPTION\n");
        for (String experimentId : expIds)
        {
            sb.append(experimentId);
            sb.append("\t");
            sb.append("New déscription\n");
        }

        return sb.toString();
    }

    private void verifyBulkUpdate(ArrayList<String> expIds)
    {

        for (String experimentId : expIds)
        {
            Experiment experiment = commonClientService.getExperimentInfo(experimentId);
            assertEquals("SIRNA_HCS", experiment.getExperimentType().getCode());
            List<IEntityProperty> properties = experiment.getProperties();
            assertEquals("DESCRIPTION", properties.get(0).getPropertyType().getCode());
            // Make sure the string is escaped
            assertEquals(StringEscapeUtils.escapeHtml("New déscription"), properties.get(0)
                    .tryGetAsString());
            assertEquals(1, properties.size());
        }

    }
}
