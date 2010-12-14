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

package ch.ethz.bsse.cisd.dsu.dss.plugins;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.hamcrest.core.IsNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import ch.rinn.restrictions.Friend;
import ch.systemsx.cisd.base.tests.AbstractFileSystemTestCase;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.logging.BufferedAppender;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.common.test.RecordingMatcher;
import ch.systemsx.cisd.common.utilities.Template;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * @author Franz-Josef Elmer
 */
@Friend(toClasses = DataSetToSOFT.class)
public class DataSetToSOFTTest extends AbstractFileSystemTestCase
{
    private static final String USER_EMAIL = "user@ho.me";

    private static final String SRF_FILE_NAME = "sample.srf";

    private static final String SRF_FILE_CONTENT = "hello world!";

    private static final String DATASET_CODE = "ds-1";

    private static final String FLOW_LANE_SAMPLE_CODE = "flow-lane:1";

    private BufferedAppender logRecorder;

    private Mockery context;

    private IEncapsulatedOpenBISService service;

    private IMailClient mailClient;

    private DataSetProcessingContext dataSetProcessingContext;

    private IProcessingPluginTask processingPlugin;

    @BeforeMethod
    public void beforeMethod()
    {
        logRecorder = new BufferedAppender("%-5p %c - %m%n", Level.DEBUG);
        context = new Mockery();
        service = context.mock(IEncapsulatedOpenBISService.class);
        mailClient = context.mock(IMailClient.class);
        dataSetProcessingContext =
                new DataSetProcessingContext(new HashMap<String, String>(), mailClient, USER_EMAIL);
        File testFolder = new File(workingDirectory, "test");
        testFolder.mkdirs();
        File sampleSrfFile = new File(testFolder, SRF_FILE_NAME);
        FileUtilities.writeToFile(sampleSrfFile, SRF_FILE_CONTENT);
        processingPlugin = new DataSetToSOFT(new Properties(), workingDirectory, service);
    }

    @AfterMethod
    public void afterMethod()
    {
        logRecorder.reset();
        // To following line of code should also be called at the end of each test method.
        // Otherwise one do not known which test failed.
        context.assertIsSatisfied();
    }

    @Test
    public void test() throws IOException
    {
        DatasetDescription datasetDescription = new DatasetDescription();
        datasetDescription.setDatasetCode(DATASET_CODE);
        datasetDescription.setDataSetLocation(".");
        List<DatasetDescription> dataSets = Arrays.asList(datasetDescription);
        final Sample flowLaneSample = sample(42, FLOW_LANE_SAMPLE_CODE);
        flowLaneSample.setCode(FLOW_LANE_SAMPLE_CODE);
        prepareGetDataSet(DATASET_CODE, flowLaneSample);
        Sample flowCellSample = sample(43, "flow-cell");
        addProperty(flowCellSample, "GENOME_ANALYZER", "my-analyzer");
        final Sample sequencingSample = sample(44, "sequencing");
        addProperty(sequencingSample, DataSetToSOFT.EXTERNAL_SAMPLE_NAME_PROPERTY, "my sample");
        addProperty(sequencingSample, "SAMPLE_SOURCE_NAME", "source");
        addProperty(sequencingSample, "NCBI_ORGANISM_TAXONOMY", "organism");
        addProperty(sequencingSample, "CONTACT_PERSON_NAME", "person");
        addProperty(sequencingSample, "SAMPLE_MOLECULE", "total_rna");
        addProperty(sequencingSample, "SEQUENCING_APPLICATION", "application");
        addProperty(sequencingSample, "SAMPLE_EXTRACT_PROTOCOL", "protocol");
        addProperty(sequencingSample, "SAMPLE_DATA_PROCESSING", "processing");
        addProperty(sequencingSample, "SAMPLE_LIBRARY_SELECTION", "cDNA");
        addProperty(sequencingSample, "SAMPLE_LIBRARY_STRATEGY", "RNA-Seq");
        addProperty(sequencingSample, "SAMPLE_KIND", "CHIP");
        flowLaneSample.setContainer(flowCellSample);
        prepareGetSample(flowLaneSample);
        prepareGetSample(flowCellSample);
        final RecordingMatcher<ListSampleCriteria> listSampleCriteriaMatcher =
                new RecordingMatcher<ListSampleCriteria>();
        final RecordingMatcher<String> subjectMatcher = new RecordingMatcher<String>();
        final RecordingMatcher<String> contentMatcher = new RecordingMatcher<String>();
        final RecordingMatcher<String> fileNameMatcher = new RecordingMatcher<String>();
        final RecordingMatcher<DataHandler> attachmentMatcher = new RecordingMatcher<DataHandler>();
        final RecordingMatcher<EMailAddress[]> addressesMatcher =
                new RecordingMatcher<EMailAddress[]>();
        context.checking(new Expectations()
            {
                {
                    one(service).listSamples(with(listSampleCriteriaMatcher));
                    will(returnValue(Arrays.asList(sequencingSample)));

                    one(mailClient).sendEmailMessageWithAttachment(with(subjectMatcher),
                            with(contentMatcher), with(fileNameMatcher), with(attachmentMatcher),
                            with(new IsNull<EMailAddress>()), with(new IsNull<EMailAddress>()),
                            with(addressesMatcher));
                }
            });

        ProcessingStatus status = processingPlugin.process(dataSets, dataSetProcessingContext);

        assertEquals(0, status.getErrorStatuses().size());
        assertEquals(flowLaneSample.getId(), listSampleCriteriaMatcher.getRecordedObjects().get(0)
                .getChildSampleId().getId());
        Template template = initTemplate(DataSetToSOFT.E_MAIL_SUBJECT_TEMPLATE);
        assertEquals("[" + template.createText() + "]", subjectMatcher.getRecordedObjects()
                .toString());
        Template content = initTemplate(DataSetToSOFT.E_MAIL_CONTENT_TEMPLATE);
        content.bind("flow-lane", flowLaneSample.getCode());
        content.bind("data-set", DATASET_CODE);
        assertEquals("[" + content.createText() + "]", contentMatcher.getRecordedObjects()
                .toString());
        Template fileNameTemplate = DataSetToSOFT.SOFT_FILE_NAME_TEMPLATE.createFreshCopy();
        fileNameTemplate.bind("external-sample-name", "my_sample");
        fileNameTemplate.bind("flow-lane", flowLaneSample.getCode().replace(':', '-'));
        assertEquals("[" + fileNameTemplate.createText() + "]", fileNameMatcher
                .getRecordedObjects().toString());
        List<EMailAddress[]> emailAddresses = addressesMatcher.getRecordedObjects();
        assertEquals(USER_EMAIL, emailAddresses.get(0)[0].tryGetEmailAddress());
        DataSource dataSource = attachmentMatcher.getRecordedObjects().get(0).getDataSource();
        String attachmentContent = getContent(dataSource);
        assertEquals("^SAMPLE = my sample\n" + "!Sample_type = SRA\n"
                + "!Sample_title = my sample\n" + "!Sample_source_name = source\n"
                + "!Sample_organism = organism\n"
                + "!Sample_characteristics = <<<NEED_TO_BE_FILLED>>>\n"
                + "!Sample_biomaterial_provider = person\n" + "!Sample_molecule = total_rna\n"
                + "!Sample_extract_protocol = protocol\n"
                + "!Sample_data_processing = processing\n" + "!Sample_library_strategy = RNA-Seq\n"
                + "!Sample_library_source = genomic\n" + "!Sample_library_selection = cDNA\n"
                + "!Sample_instrument_model = my-analyzer\n" + "!Sample_raw_file_1 = sample.srf\n"
                + "!Sample_raw_file_type_1 = srf\n"
                + "!Sample_file_checksum_1 = fc3ff98e8c6a0d3087d515c0473f8677\n", attachmentContent);
        context.assertIsSatisfied();
    }

    String getContent(DataSource dataSource) throws IOException
    {
        StringBuilder builder = new StringBuilder();
        @SuppressWarnings("rawtypes")
        List fileLines = IOUtils.readLines(dataSource.getInputStream());
        for (Object object : fileLines)
        {
            builder.append(object).append('\n');
        }
        return builder.toString();
    }

    Template initTemplate(Template originalTemplate)
    {
        Template template = originalTemplate.createFreshCopy();
        template.bind("external-sample-name", "my sample");
        return template;
    }

    private void prepareGetDataSet(final String dataSetCode, final Sample flowLaneSample)
    {
        context.checking(new Expectations()
            {
                {
                    one(service).tryGetDataSet(dataSetCode);
                    ExternalData dataSet = new ExternalData();
                    dataSet.setCode(dataSetCode);
                    dataSet.setSample(flowLaneSample);
                    will(returnValue(dataSet));
                }
            });
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
    }

    private void addProperty(Sample sample, String propertyTypeCode, String value)
    {
        List<IEntityProperty> properties = sample.getProperties();
        if (properties == null)
        {
            properties = new ArrayList<IEntityProperty>();
            sample.setProperties(properties);
        }
        EntityProperty property = new EntityProperty();
        PropertyType propertyType = new PropertyType();
        propertyType.setCode(propertyTypeCode);
        property.setPropertyType(propertyType);
        property.setValue(value);
        properties.add(property);
    }

    private Sample sample(long id, String identifier)
    {
        Sample sample = new Sample();
        sample.setId(id);
        sample.setIdentifier(identifier);
        return sample;
    }
}
