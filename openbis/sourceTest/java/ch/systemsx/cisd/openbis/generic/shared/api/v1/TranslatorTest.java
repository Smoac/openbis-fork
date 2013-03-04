/*
 * Copyright 2011 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1;

import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import org.testng.AssertJUnit;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Attachment;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.DataSet.Connections;
import ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.id.experiment.ExperimentPermIdId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalDataManagementSystem;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Project;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ContainerDataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.DataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.ExperimentBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.LinkDataSetBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleTypeBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.AttachmentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExperimentPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.PersonPE;

/**
 * @author Franz-Josef Elmer
 */
public class TranslatorTest extends AssertJUnit
{
    private DataSetBuilder ds1;

    private DataSetBuilder ds2;

    private ContainerDataSetBuilder dsContainer;

    private LinkDataSetBuilder dsLink;

    private Experiment experiment;

    private Sample sample;

    private Project project;

    private ExternalDataManagementSystem edms;

    @BeforeMethod
    public void setUp()
    {
        edms = new ExternalDataManagementSystem();
        edms.setCode("EDMS1");
        edms.setUrlTemplate("http://www.${code}.ch");

        experiment =
                new ExperimentBuilder().id(1).permID("e-1").identifier("/S/P/E1").type("my-type")
                        .property("a", "1").date(new Date(101)).modificationDate(new Date(102))
                        .getExperiment();
        project = experiment.getProject();
        project.setRegistrationDate(new Date(300));
        sample =
                new SampleBuilder("/S/S1").id(2).permID("s-2").property("b", "2")
                        .type(new SampleTypeBuilder().code("ms").id(4).getSampleType())
                        .date(new Date(201)).modificationDate(new Date(202)).getSample();

        ds1 =
                new DataSetBuilder().code("ds1").type("T1").experiment(experiment).sample(sample)
                        .property("A", "42").registrationDate(new Date(42))
                        .modificationDate(new Date(43));
        ds2 =
                new DataSetBuilder().code("ds2").type("T2").experiment(experiment)
                        .property("B", "true");

        dsContainer =
                new ContainerDataSetBuilder().code("ds-container").type("T3")
                        .experiment(experiment).sample(sample).contains(ds1.getDataSet())
                        .contains(ds2.getDataSet());
        dsLink =
                new LinkDataSetBuilder().code("lds").type("L1").experiment(experiment)
                        .sample(sample).registrationDate(new Date(123456789L))
                        .modificationDate(new Date(123459999L)).externalCode("EX_CODE").edms(edms);
    }

    @Test
    public void testTranslateProject()
    {
        ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Project translatedProject =
                Translator.translate(project);
        assertEquals(project.getCode(), translatedProject.getCode());
        assertEquals(project.getSpace().getCode(), translatedProject.getSpaceCode());
        assertEquals(project.getRegistrationDate(), translatedProject.getRegistrationDetails()
                .getRegistrationDate());
    }

    @Test
    public void testTranslateExperiment()
    {
        ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Experiment translateExperiment =
                Translator.translate(experiment);
        assertEquals(experiment.getCode(), translateExperiment.getCode());
        assertEquals(experiment.getId(), translateExperiment.getId());
        assertEquals(experiment.getPermId(), translateExperiment.getPermId());
        assertEquals(experiment.getIdentifier(), translateExperiment.getIdentifier());
        assertEquals(experiment.getExperimentType().getCode(),
                translateExperiment.getExperimentTypeCode());
        assertEquals(experiment.getRegistrationDate(), translateExperiment.getRegistrationDetails()
                .getRegistrationDate());
        assertEquals(experiment.getModificationDate(), translateExperiment.getRegistrationDetails()
                .getModificationDate());
        assertEquals("{a=1}", translateExperiment.getProperties().toString());
    }

    @Test
    public void testTranslateSample()
    {
        ch.systemsx.cisd.openbis.generic.shared.api.v1.dto.Sample translateSample =
                Translator.translate(sample);
        assertEquals(sample.getCode(), translateSample.getCode());
        assertEquals(sample.getId(), translateSample.getId());
        assertEquals(sample.getPermId(), translateSample.getPermId());
        assertEquals(sample.getIdentifier(), translateSample.getIdentifier());
        assertEquals(sample.getSampleType().getCode(), translateSample.getSampleTypeCode());
        assertEquals(sample.getRegistrationDate(), translateSample.getRegistrationDetails()
                .getRegistrationDate());
        assertEquals(sample.getModificationDate(), translateSample.getRegistrationDetails()
                .getModificationDate());
        assertEquals("{b=2}", translateSample.getProperties().toString());
    }

    @Test
    public void testTranslateContainerDataSetWithNoConnectionsAndRetrievingNoConnections()
    {
        DataSet translated =
                Translator.translate(dsContainer.getContainerDataSet(),
                        EnumSet.noneOf(DataSet.Connections.class));
        assertTrue(translated.isContainerDataSet());
        assertBasicAttributes(ds1.getDataSet(), translated.getContainedDataSets().get(0));
        assertBasicAttributes(ds2.getDataSet(), translated.getContainedDataSets().get(1));
        assertChildrenNotRetrieved(translated);
        assertParentsNotRetrieved(translated);
    }

    @Test
    public void testTranslateExternalDataWithNoConnectionsAndRetrievingNoConnections()
    {
        DataSet translated =
                Translator.translate(ds1.getDataSet(), EnumSet.noneOf(DataSet.Connections.class));
        assertBasicAttributes(ds1.getDataSet(), translated);
        assertChildrenNotRetrieved(translated);
        assertParentsNotRetrieved(translated);
    }

    @Test
    public void testTranslateExternalDataWithNoConnectionsAndRetrievingAllConnections()
    {
        DataSet translated =
                Translator.translate(ds1.getDataSet(),
                        EnumSet.of(Connections.CHILDREN, Connections.PARENTS));
        assertBasicAttributes(ds1.getDataSet(), translated);
        assertEquals(0, translated.getChildrenCodes().size());
        assertEquals(0, translated.getParentCodes().size());
    }

    @Test
    public void testTranslateExternalDataWithConnectionsAndRetrievingNoConnections()
    {
        ds1.child(ds2.getDataSet());
        DataSet translated =
                Translator.translate(ds1.getDataSet(), EnumSet.noneOf(DataSet.Connections.class));
        assertBasicAttributes(ds1.getDataSet(), translated);
        assertChildrenNotRetrieved(translated);
        assertParentsNotRetrieved(translated);
    }

    @Test
    public void testTranslateExternalDataWithConnectionsRetrievingOnlyChildrenConnections()
    {
        ds1.child(ds2.getDataSet());
        DataSet translated =
                Translator.translate(ds1.getDataSet(), EnumSet.of(Connections.CHILDREN));
        assertBasicAttributes(ds1.getDataSet(), translated);
        assertEquals("[ds2]", translated.getChildrenCodes().toString());
        assertParentsNotRetrieved(translated);
    }

    @Test
    public void testTranslateExternalDataWithConnectionsRetrievingOnlyParentConnections()
    {
        ds2.parent(ds1.getDataSet());
        DataSet translated =
                Translator.translate(ds2.getDataSet(), EnumSet.of(Connections.PARENTS));
        assertBasicAttributes(ds2.getDataSet(), translated);
        assertChildrenNotRetrieved(translated);
        assertEquals("[ds1]", translated.getParentCodes().toString());
    }

    @Test
    public void testTranslateExternalDataWithConnectionsRetrievingAllConnections()
    {
        ds2.parent(ds1.getDataSet());
        ds2.child(ds2.getDataSet());
        DataSet translated =
                Translator.translate(ds2.getDataSet(),
                        EnumSet.of(Connections.CHILDREN, Connections.PARENTS));
        assertBasicAttributes(ds2.getDataSet(), translated);
        assertEquals("[ds2]", translated.getChildrenCodes().toString());
        assertEquals("[ds1]", translated.getParentCodes().toString());
    }

    @Test
    public void testTranslateLinkDataSet()
    {
        DataSet translated =
                Translator.translate(dsLink.getLinkDataSet(),
                        EnumSet.of(Connections.CHILDREN, Connections.PARENTS));

        assertBasicAttributes(dsLink.getLinkDataSet(), translated);
        assertTrue(translated.isLinkDataSet());
        assertEquals(dsLink.getLinkDataSet().getExternalCode(), translated.getExternalDataSetCode());
        assertEquals("http://www.EX_CODE.ch", translated.getExternalDataSetLink());
        assertEquals(edms, translated.getExternalDataManagementSystem());
    }

    @Test
    public void testTranslateAttachmentsWithAllVersions()
    {
        AttachmentPE a1 = attachment("test.txt", 1);
        AttachmentPE a2 = attachment("test.txt", 2);
        ExperimentPE attachmentHolder = new ExperimentPE();
        attachmentHolder.setId(1234L);

        List<Attachment> attachments =
                Translator.translateAttachments("st-1", new ExperimentPermIdId("perm1"),
                        attachmentHolder, Arrays.asList(a1, a2), true);

        assertEquals("[Attachment [fileName=test.txt, version=2, title=Title, "
                + "description=File:test.txt, version:2, "
                + "registrationDate=Thu Jan 01 01:00:04 CET 1970, "
                + "userFirstName=Albert, userLastName=Einstein, "
                + "userEmail=ae@ae.ch, userId=ae, "
                + "downloadLink=/openbis/openbis/attachment-download?sessionID=st-1&"
                + "attachmentHolder=EXPERIMENT&id=1234&fileName=test.txt&version=2], "
                + "Attachment [fileName=test.txt, version=1, title=Title, "
                + "description=File:test.txt, version:1, "
                + "registrationDate=Thu Jan 01 01:00:04 CET 1970, "
                + "userFirstName=Albert, userLastName=Einstein, userEmail=ae@ae.ch, userId=ae, "
                + "downloadLink=/openbis/openbis/attachment-download?sessionID=st-1&"
                + "attachmentHolder=EXPERIMENT&id=1234&fileName=test.txt&version=1]]",
                attachments.toString());
        assertEquals(2, attachments.size());
    }

    @Test
    public void testTranslateAttachmentsWithLatestVersions()
    {
        AttachmentPE a1 = attachment("test.txt", 1);
        AttachmentPE a2 = attachment("greetings.txt", 1);
        AttachmentPE a3 = attachment("greetings.txt", 2);
        AttachmentPE a4 = attachment("test.txt", 2);
        AttachmentPE a5 = attachment("test.txt", 3);
        ExperimentPE attachmentHolder = new ExperimentPE();
        attachmentHolder.setId(1234L);

        List<Attachment> attachments =
                Translator.translateAttachments("st-1", new ExperimentPermIdId("perm1"),
                        attachmentHolder, Arrays.asList(a1, a2, a3, a4, a5), false);

        assertEquals("[Attachment [fileName=greetings.txt, version=2, title=Title, "
                + "description=File:greetings.txt, version:2, "
                + "registrationDate=Thu Jan 01 01:00:04 CET 1970, "
                + "userFirstName=Albert, userLastName=Einstein, "
                + "userEmail=ae@ae.ch, userId=ae, "
                + "downloadLink=/openbis/openbis/attachment-download?sessionID=st-1&"
                + "attachmentHolder=EXPERIMENT&id=1234&fileName=greetings.txt&version=2], "
                + "Attachment [fileName=test.txt, version=3, title=Title, "
                + "description=File:test.txt, version:3, "
                + "registrationDate=Thu Jan 01 01:00:04 CET 1970, "
                + "userFirstName=Albert, userLastName=Einstein, userEmail=ae@ae.ch, userId=ae, "
                + "downloadLink=/openbis/openbis/attachment-download?sessionID=st-1&"
                + "attachmentHolder=EXPERIMENT&id=1234&fileName=test.txt&version=3]]",
                attachments.toString());
        assertEquals(2, attachments.size());

    }

    private AttachmentPE attachment(String fileName, int version)
    {
        AttachmentPE attachment = new AttachmentPE();
        attachment.setFileName(fileName);
        attachment.setVersion(version);
        attachment.setTitle("Title");
        attachment.setDescription("File:" + fileName + ", version:" + version);
        PersonPE registrator = new PersonPE();
        registrator.setUserId("ae");
        registrator.setFirstName("Albert");
        registrator.setLastName("Einstein");
        registrator.setEmail("ae@ae.ch");
        attachment.setRegistrator(registrator);
        attachment.setRegistrationDate(new Date(4711));
        return attachment;
    }

    private void assertBasicAttributes(
            ch.systemsx.cisd.openbis.generic.shared.basic.dto.AbstractExternalData originalDataSet,
            DataSet translatedDataSet)
    {
        assertEquals(originalDataSet.getCode(), translatedDataSet.getCode());
        assertEquals(originalDataSet.getDataSetType().getCode(),
                translatedDataSet.getDataSetTypeCode());
        assertEquals(originalDataSet.getExperiment().getIdentifier(),
                translatedDataSet.getExperimentIdentifier());
        assertEquals(originalDataSet.getSampleIdentifier(),
                translatedDataSet.getSampleIdentifierOrNull());
        assertEquals(originalDataSet.getRegistrationDate(), translatedDataSet
                .getRegistrationDetails().getRegistrationDate());
        assertEquals(originalDataSet.getModificationDate(), translatedDataSet
                .getRegistrationDetails().getModificationDate());
        List<IEntityProperty> originalProperties = originalDataSet.getProperties();
        HashMap<String, String> translatedProperties = translatedDataSet.getProperties();
        for (IEntityProperty property : originalProperties)
        {
            assertEquals(property.getValue(),
                    translatedProperties.get(property.getPropertyType().getCode()));
        }
        assertEquals(originalProperties.size(), translatedProperties.size());
        assertEquals(originalDataSet.isContainer(), translatedDataSet.isContainerDataSet());
        assertEquals(originalDataSet.isLinkData(), translatedDataSet.isLinkDataSet());
    }

    private void assertChildrenNotRetrieved(DataSet dataSet)
    {
        try
        {
            dataSet.getChildrenCodes();
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Children codes were not retrieved for data set " + dataSet.getCode()
                    + ".", ex.getMessage());
        }
    }

    private void assertParentsNotRetrieved(DataSet dataSet)
    {
        try
        {
            dataSet.getParentCodes();
            fail("IllegalArgumentException expected.");
        } catch (IllegalArgumentException ex)
        {
            assertEquals("Parent codes were not retrieved for data set " + dataSet.getCode() + ".",
                    ex.getMessage());
        }
    }

}
