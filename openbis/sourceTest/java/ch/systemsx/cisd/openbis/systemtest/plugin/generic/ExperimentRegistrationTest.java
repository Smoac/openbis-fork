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

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.mock.web.MockMultipartFile;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListSampleDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSetWithEntityTypes;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.client.web.server.UploadedFilesBean;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentWithContent;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewExperiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
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
            assertEquals("Value of mandatory property 'DESCRIPTION' not specified.", ex
                    .getMessage());
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
            { property("DESCRIPTION", "my experiment") });
        genericClientService.registerExperiment(ATTACHMENTS_SESSION_KEY, SAMPLES_SESSION_KEY, newExperiment);

        Experiment experiment = genericClientService.getExperimentInfo(experimentIdentifier);
        assertEquals(experimentCode, experiment.getCode());
        assertEquals(experimentIdentifier.toUpperCase(), experiment.getIdentifier());
        assertEquals("SIRNA_HCS", experiment.getExperimentType().getCode());
        List<IEntityProperty> properties = experiment.getProperties();
        assertEquals("DESCRIPTION", properties.get(0).getPropertyType().getCode());
        assertEquals("my experiment", properties.get(0).tryGetAsString());
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
        genericClientService.registerExperiment(ATTACHMENTS_SESSION_KEY, SAMPLES_SESSION_KEY, newExperiment);

        Experiment experiment = genericClientService.getExperimentInfo(experimentIdentifier);
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
        
        HttpSession session = request.getSession();
        UploadedFilesBean uploadedFilesBean = new UploadedFilesBean();
        String batchSamplesFileContent = "identifier\torganism\n" +
        		                         "S1001\tfly\n" +
        	                             "S1002\tdog\n";
        uploadedFilesBean.addMultipartFile(new MockMultipartFile("samples.txt", 
                batchSamplesFileContent.getBytes()));
        session.setAttribute(SAMPLES_SESSION_KEY, uploadedFilesBean);
        String experimentCode = commonClientService.generateCode("EXP");
        String experimentIdentifier = "/cisd/default/" + experimentCode;
        NewExperiment newExperiment = new NewExperiment(experimentIdentifier, "SIRNA_HCS");
        newExperiment.setProperties(new IEntityProperty[]
            { property("DESCRIPTION", "my experiment") });
        newExperiment.setRegisterSamples(true);
        SampleType sampleType = new SampleType();
        sampleType.setCode("CELL_PLATE");
        newExperiment.setSampleType(sampleType);
        genericClientService.registerExperiment(ATTACHMENTS_SESSION_KEY, SAMPLES_SESSION_KEY, newExperiment);

        Experiment experiment = genericClientService.getExperimentInfo(experimentIdentifier);
        TechId experimentId = new TechId(experiment.getId());
        ListSampleCriteria listCriteria = ListSampleCriteria.createForExperiment(experimentId);
        ResultSetWithEntityTypes<Sample> samples =
                commonClientService.listSamples(new ListSampleDisplayCriteria(listCriteria));

        assertEquals("[CELL_PLATE]", samples.getAvailableEntityTypes().toString());
        Sample sample = samples.getResultSet().getList().get(0).getOriginalObject();
        assertEquals("S1001", sample.getCode());
        assertEquals("FLY", sample.getProperties().get(0).tryGetAsString());
        assertEquals(experiment.getId(), sample.getExperiment().getId());
        sample = samples.getResultSet().getList().get(1).getOriginalObject();
        assertEquals("S1002", sample.getCode());
        assertEquals("DOG", sample.getProperties().get(0).tryGetAsString());
        assertEquals(experiment.getId(), sample.getExperiment().getId());
        assertEquals(2, samples.getResultSet().getList().size());
    }
    
    @Test
    public void testRegisterExperimentAndAttachments()
    {
        String sessionToken = logIntoCommonClientService().getSessionID();
        
        HttpSession session = request.getSession();
        UploadedFilesBean uploadedFilesBean = new UploadedFilesBean();
        MockMultipartFile multipartFile =
                new MockMultipartFile("hello.txt", "hello.txt", "", "hello world".getBytes());
        uploadedFilesBean.addMultipartFile(multipartFile);
        session.setAttribute(ATTACHMENTS_SESSION_KEY, uploadedFilesBean);
        logIntoCommonClientService();
        String experimentCode = commonClientService.generateCode("EXP");
        String experimentIdentifier = "/cisd/default/" + experimentCode;
        NewExperiment newExperiment = new NewExperiment(experimentIdentifier, "SIRNA_HCS");
        newExperiment.setProperties(new IEntityProperty[]
            { property("DESCRIPTION", "my experiment") });
        newExperiment.setAttachments(Arrays.asList(new NewAttachment("hello.txt", "hello", "test attachment")));
        genericClientService.registerExperiment(ATTACHMENTS_SESSION_KEY, SAMPLES_SESSION_KEY, newExperiment);

        Experiment experiment = genericClientService.getExperimentInfo(experimentIdentifier);
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

    private IEntityProperty property(String type, String value)
    {
        EntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(type);
        property.setPropertyType(propertyType);
        property.setValue(value);
        return property;
    }
}
