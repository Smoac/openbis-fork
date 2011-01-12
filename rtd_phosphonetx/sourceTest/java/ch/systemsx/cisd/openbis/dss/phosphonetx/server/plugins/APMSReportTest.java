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

package ch.systemsx.cisd.openbis.dss.phosphonetx.server.plugins;

import static ch.systemsx.cisd.openbis.dss.phosphonetx.server.plugins.APMSReport.DEFAULT_PROTEIN_PROPERTY_CODE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;

import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelColumnHeader;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRow;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.builders.SampleBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.builders.DatasetDescriptionBuilder;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = APMSReport.class)
public class APMSReportTest extends AbstractFileSystemTestCase
{
    private static final String EXAMPLE_PROTEINS =
            "# Protein abundances computed from file 'result.consensusXML'\n"
                    + "# Parameters (relevant only): top=3, average=median, include_all, consensus:normalize\n"
                    + "# Files/samples associated with abundance values below:"
                    + " 0: '/cluster/s1.featureXML',"
                    + " 1: '/cluster/s2.featureXML',"
                    + " 2: '/cluster/s3.featureXML'\n"
                    + "\"protein\",\"n_proteins\",\"protein_score\",\"n_peptides\",\"abundance_0\",\"abundance_1\",\"abundance_2\"\n"
                    + "\"P1\",1,1,5,1.5,2.5,3.5\n"
                    + "\"P2\",1,1,6,11.5,0,13.5\n"
                    + "\"P3\",1,1,14,21.5,22.5,0\n";

    private Mockery context;

    private APMSReport report;

    private IEncapsulatedOpenBISService service;

    private IMailClient mailClient;
    
    @BeforeMethod
    public void beforeMethod()
    {
        File dataSetFolder = createDataSet("1");
        FileUtilities.writeToFile(new File(dataSetFolder, APMSReport.PROTEIN_FILE_NAME),
                EXAMPLE_PROTEINS);
        createDataSet("2");
        
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        mailClient = context.mock(IMailClient.class);
        report = new APMSReport(new Properties(), workingDirectory);
        report.setService(service);
    }

    private File createDataSet(String location)
    {
        File originalFolder = new File(new File(workingDirectory, location), "original");
        originalFolder.mkdirs();
        File dataSetFolder = new File(originalFolder, "data-set-folder");
        dataSetFolder.mkdirs();
        return dataSetFolder;
    }

    @AfterMethod
    public void afterMethod()
    {
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void testProcess() throws IOException
    {
        Sample b2 = new SampleBuilder("/S/B2").id(12).getSample();
        prepareGetSample(new SampleBuilder("/MS_DATA/S1").id(1).childOf(b2).getSample());
        prepareGetSample(new SampleBuilder("/MS_DATA/S2").id(2).childOf(b2).getSample());
        prepareGetSample(new SampleBuilder("/MS_DATA/S3").id(3).childOf(b2).getSample());
        DatasetDescriptionBuilder ds1 =
                new DatasetDescriptionBuilder("ds1").location("1").space("s").project("p")
                        .experiment("e");
        DatasetDescriptionBuilder ds2 = new DatasetDescriptionBuilder("ds2").location("2");
        List<DatasetDescription> dataSets =
                Arrays.asList(ds1.getDatasetDescription(), ds2.getDatasetDescription());
        DataSetProcessingContext processingContext =
                new DataSetProcessingContext(Collections.<String, String> emptyMap(), mailClient,
                        "a@bc.de");
        final RecordingMatcher<String> subjectMatcher = new RecordingMatcher<String>();
        final RecordingMatcher<String> contentMatcher = new RecordingMatcher<String>();
        final RecordingMatcher<String> fileNameMatcher = new RecordingMatcher<String>();
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        context.checking(new Expectations()
            {
                {
                    one(mailClient).sendEmailMessageWithAttachment(with(subjectMatcher),
                            with(contentMatcher), with(fileNameMatcher), with(attachmentMatcher),
                            with(new IsNull<EMailAddress>()), with(new IsNull<EMailAddress>()),
                            with(addressesMatcher));
                }
            });

        ProcessingStatus status = report.process(dataSets, processingContext);
        assertEquals("Protein APMS Report", subjectMatcher.recordedObject());
        assertEquals("Dear User\n\n"
                + "Enclosed you will find the Protein APMS report file for experiment s/p/e.\n"
                + "Data Set: ds1", contentMatcher.recordedObject());
        @SuppressWarnings("unchecked")
        List<String> fileLines =
                IOUtils.readLines(attachmentMatcher.recordedObject().getInputStream());
        assertEquals("Sample ID,Bait,Prey,freq of obs,"
                + "avg MS1 intensities (normalized for the bait),STDV MS1 intensity",
                fileLines.get(0));
        assertEquals(4, fileLines.size());
        assertEquals("[ERROR: \"Exception occured: "
                + "ch.systemsx.cisd.common.exceptions.UserFailureException: File "
                + APMSReport.PROTEIN_FILE_NAME + " missing.\"]", status.getErrorStatuses()
                .toString());

        context.assertIsSatisfied();
    }
    
    @Test
    public void testCreateReport()
    {
        Sample master =
                new SampleBuilder("/master").property(DEFAULT_PROTEIN_PROPERTY_CODE, "Q1")
                        .getSample();
        Sample b1 = new SampleBuilder("/S/B1").id(11).childOf(master).getSample();
        Sample b2 = new SampleBuilder("/S/B2").id(12).getSample();
        prepareGetSample(new SampleBuilder("/MS_DATA/S1").id(1).childOf(b1).getSample());
        prepareGetSample(new SampleBuilder("/MS_DATA/S2").id(2).childOf(b1).getSample());
        prepareGetSample(new SampleBuilder("/MS_DATA/S3").id(3).childOf(b2).getSample());
        DatasetDescriptionBuilder ds1 = new DatasetDescriptionBuilder("ds1").location("1");
        List<DatasetDescription> dataSets = Arrays.asList(ds1.getDatasetDescription());

        TableModel table = report.createReport(dataSets);

        List<TableModelColumnHeader> headers = table.getHeader();
        assertEquals("[Sample ID, Bait, Prey, freq of obs, "
                        + "avg MS1 intensities (normalized for the bait), STDV MS1 intensity]",
                headers.toString());
        List<TableModelRow> rows = table.getRows();
        assertEquals("[B1, Q1, P1, 1.0, 2.0, 0.5]", rows.get(0).getValues().toString());
        assertEquals("[B2, , P1, 1.0, 3.5, 0.0]", rows.get(1).getValues().toString());
        assertEquals("[B1, Q1, P2, 0.5, 5.75, 5.75]", rows.get(2).getValues().toString());
        assertEquals("[B2, , P2, 1.0, 13.5, 0.0]", rows.get(3).getValues().toString());
        assertEquals("[B1, Q1, P3, 1.0, 22.0, 0.5]", rows.get(4).getValues().toString());
        assertEquals("[B2, , P3, 0.0, 0.0, 0.0]", rows.get(5).getValues().toString());
        assertEquals(3 * 2, rows.size());
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testCreateReportForMissingProteinsFile()
    {
        DatasetDescriptionBuilder ds = new DatasetDescriptionBuilder("ds2").location("2");
        try
        {
            report.createReport(Arrays.asList(ds.getDatasetDescription()));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("File " + APMSReport.PROTEIN_FILE_NAME + " missing.", ex.getMessage());
        }
        
        context.assertIsSatisfied();
    }
    
    @Test
    public void testCreateReportForTwoDataSets()
    {
        DatasetDescriptionBuilder ds1 = new DatasetDescriptionBuilder("ds1").location("1");
        DatasetDescriptionBuilder ds2 = new DatasetDescriptionBuilder("ds2").location("2");
        try
        {
            report.createReport(Arrays.asList(ds1.getDatasetDescription(),
                    ds2.getDatasetDescription()));
            fail("UserFailureException expected");
        } catch (UserFailureException ex)
        {
            assertEquals("Chosen plugin works with exactly one data set. 2 data sets selected.",
                    ex.getMessage());
        }

        context.assertIsSatisfied();
    }
    
    private void prepareGetSample(final Sample sample)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetSampleWithExperiment(
                            SampleIdentifierFactory.parse(sample.getIdentifier()));
                    will(returnValue(sample));
                }
            });
        prepareGetAncestors(sample);
    }

    protected void prepareGetAncestors(final Sample sample)
    {
        List<IEntityProperty> properties = sample.getProperties();
        for (IEntityProperty property : properties)
        {
            if (property.getPropertyType().getCode().equals(DEFAULT_PROTEIN_PROPERTY_CODE))
            {
                return;
            }
        }
        final Set<Sample> parents = sample.getParents();
        context.checking(new Expectations()
            {
                {
                    one(service).listSamples(with(new BaseMatcher<ListSampleCriteria>()
                        {
                            public boolean matches(Object item)
                            {
                                return sample.getId() == ((ListSampleCriteria) item)
                                        .getChildSampleId().getId();
                            }

                            public void describeTo(Description description)
                            {
                                description.appendText("parents of " + sample.getIdentifier());
                            }
                        }));
                    will(returnValue(new ArrayList<Sample>(parents)));
                }
            });
        for (Sample parent : parents)
        {
            prepareGetAncestors(parent);
        }
    }

}
