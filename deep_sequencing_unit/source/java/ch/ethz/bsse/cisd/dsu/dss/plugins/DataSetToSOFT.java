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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.util.ByteArrayDataSource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.base.exceptions.CheckedExceptionTunnel;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.mail.EMailAddress;
import ch.systemsx.cisd.common.utilities.MD5ChecksumCalculator;
import ch.systemsx.cisd.common.utilities.Template;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.IProcessingPluginTask;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.ProcessingStatus;
import ch.systemsx.cisd.openbis.dss.generic.shared.DataSetProcessingContext;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.DatasetDescription;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Processing plugin for exporting meta data of a flow lane as a SOFT file.
 * 
 * @author Franz-Josef Elmer
 */
public class DataSetToSOFT implements IProcessingPluginTask
{
    @Private
    static final String EXTERNAL_SAMPLE_NAME_PROPERTY = "EXTERNAL_SAMPLE_NAME";

    private static final String EMPTY = "<<<NEED_TO_BE_FILLED>>>";

    @Private
    static final Template SOFT_FILE_NAME_TEMPLATE = new Template(
            "${flow-lane}_${external-sample-name}_SOFT.txt");

    @Private
    static final Template E_MAIL_SUBJECT_TEMPLATE = new Template(
            "SOFT file for '${external-sample-name}'");

    @Private
    static final Template E_MAIL_CONTENT_TEMPLATE = new Template("Dear User\n\n"
            + "Enclosed you will find the SOFT file for '${external-sample-name}'.\n"
            + "Flow lane: ${flow-lane}\nData Set: ${data-set}");

    private static final class SOFTBuilder
    {
        private final StringBuilder builder = new StringBuilder();

        void addSample(Sample sample, String propertyTypeCode)
        {
            addLine('^', "SAMPLE", getProperty(sample, propertyTypeCode));
        }

        void addSampleProperty(String key, Sample sample, String propertyTypeCode,
                Map<String, String> translation)
        {
            String property = getProperty(sample, propertyTypeCode);
            String translatedProperty = translation.get(property);
            addProperty(key, translatedProperty == null ? property : translatedProperty);
        }

        void addSampleProperty(String key, Sample sample, String propertyTypeCode)
        {
            addProperty(key, getProperty(sample, propertyTypeCode));
        }

        void addProperty(String key, String property)
        {
            addLine('!', "Sample_" + key, property);
        }

        private void addLine(char prefix, String key, String value)
        {
            builder.append(prefix).append(key).append(" = ").append(value).append('\n');
        }

        private String getProperty(Sample sample, String propertyTypeCode)
        {
            String property = tryToGetProperty(sample, propertyTypeCode);
            return property == null ? EMPTY : property;
        }

        @Override
        public String toString()
        {
            return builder.toString();
        }
    }

    private static final String tryToGetProperty(Sample sample, String propertyTypeCode)
    {
        List<IEntityProperty> properties = sample.getProperties();
        for (IEntityProperty property : properties)
        {
            if (property.getPropertyType().getCode().equals(propertyTypeCode))
            {
                return property.tryGetAsString();
            }
        }
        return null;
    }

    private static final Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            DataSetToSOFT.class);

    private static final long serialVersionUID = 1L;

    private final File storeRoot;

    private final Map<String, String> translation;

    private IEncapsulatedOpenBISService service;

    public DataSetToSOFT(Properties properties, File storeRoot)
    {
        this(properties, storeRoot, null);
    }

    DataSetToSOFT(Properties properties, File storeRoot, IEncapsulatedOpenBISService service)
    {
        this.storeRoot = storeRoot;
        this.service = service;
        translation = new HashMap<String, String>();
        translation.put("GENOMIC_DNA", "genomic");
        translation.put("FRAGMENTED_GENOMIC_DNA", "genomic");
        translation.put("CHIP", "genomic");
        translation.put("BISULFITE", "genomic");
        translation.put("NON_GENOMIC", "non-genomic");
        translation.put("SYNTHETIC", "synthetic");
        translation.put("VIRAL_RNA", "viral RNA");
        translation.put("SMALL_RNA", "other");
        translation.put("TOTAL_RNA", "other");
        translation.put("MRNA", "other");
        translation.put("PROCESSED_DNA_LIBRARY", EMPTY);
    }

    public ProcessingStatus process(List<DatasetDescription> datasets,
            DataSetProcessingContext context)
    {
        EMailAddress address = new EMailAddress(context.getUserEmailOrNull());
        ProcessingStatus status = new ProcessingStatus();
        for (DatasetDescription datasetDescription : datasets)
        {
            try
            {
                String dataSetCode = datasetDescription.getDatasetCode();
                if (operationLog.isInfoEnabled())
                {
                    operationLog.info("Create SOFT file for data set " + dataSetCode);
                }
                ExternalData srfDataSet = getService().tryGetDataSet(dataSetCode);
                Sample flowLaneSample = getFlowLaneSample(srfDataSet);
                Sample flowCellSample = getFlowCellSample(flowLaneSample);
                Sample sequencingSample = getSequencingSample(flowLaneSample);
                File srfFile = tryToFindSrfFile(datasetDescription);
                if (srfFile == null)
                {
                    status.addDatasetStatus(datasetDescription,
                            Status.createError("Data set " + dataSetCode + " has no srf file."));
                    continue;
                }
                String checkSum = calculateCheckSum(srfFile);

                SOFTBuilder softBuilder = new SOFTBuilder();
                softBuilder.addSample(sequencingSample, EXTERNAL_SAMPLE_NAME_PROPERTY);
                softBuilder.addProperty("type", "SRA");
                softBuilder.addSampleProperty("title", sequencingSample,
                        EXTERNAL_SAMPLE_NAME_PROPERTY);
                softBuilder
                        .addSampleProperty("source_name", sequencingSample, "SAMPLE_SOURCE_NAME");
                softBuilder.addSampleProperty("organism", sequencingSample,
                        "NCBI_ORGANISM_TAXONOMY");
                softBuilder.addSampleProperty("characteristics", sequencingSample,
                        "SAMPLE_CHARACTERISTICS");
                softBuilder.addSampleProperty("biomaterial_provider", sequencingSample,
                        "CONTACT_PERSON_NAME");
                softBuilder.addSampleProperty("molecule", sequencingSample, "SAMPLE_MOLECULE");
                softBuilder.addSampleProperty("extract_protocol", sequencingSample,
                        "SAMPLE_EXTRACT_PROTOCOL");
                softBuilder.addSampleProperty("data_processing", sequencingSample,
                        "SAMPLE_DATA_PROCESSING");
                softBuilder.addSampleProperty("library_strategy", sequencingSample,
                        "SAMPLE_LIBRARY_STRATEGY");
                softBuilder.addSampleProperty("library_source", sequencingSample, "SAMPLE_KIND",
                        translation);
                softBuilder.addSampleProperty("library_selection", sequencingSample,
                        "SAMPLE_LIBRARY_SELECTION");
                softBuilder
                        .addSampleProperty("instrument_model", flowCellSample, "GENOME_ANALYZER");
                softBuilder.addProperty("raw_file_1", srfFile.getName());
                softBuilder.addProperty("raw_file_type_1", "srf");
                softBuilder.addProperty("file_checksum_1", checkSum);

                String subject = createSubject(sequencingSample);
                String content = createContent(sequencingSample, flowLaneSample, srfDataSet);
                String fileName = createSoftFileName(sequencingSample, flowLaneSample);
                DataSource dataSource = createDataSource(softBuilder.toString());
                context.getMailClient().sendEmailMessageWithAttachment(subject, content, fileName,
                        new DataHandler(dataSource), null, null, address);
                status.addDatasetStatus(datasetDescription, Status.OK);
            } catch (Exception ex)
            {
                status.addDatasetStatus(datasetDescription,
                        Status.createError("Exception occured: " + ex));
                operationLog.error("Exception occured while processing " + datasetDescription, ex);
            }
        }
        return status;
    }

    private String createSubject(Sample sequencingSample)
    {
        Template template = E_MAIL_SUBJECT_TEMPLATE.createFreshCopy();
        bindExternalSampleName(template, sequencingSample);
        return template.createText();
    }

    private String createContent(Sample sequencingSample, Sample flowLaneSample,
            ExternalData dataSet)
    {
        Template template = E_MAIL_CONTENT_TEMPLATE.createFreshCopy();
        bindExternalSampleName(template, sequencingSample);
        template.bind("flow-lane", flowLaneSample.getIdentifier());
        template.bind("data-set", dataSet.getCode());
        return template.createText();
    }

    private String createSoftFileName(Sample sequencingSample, Sample flowLaneSample)
    {
        Template template = SOFT_FILE_NAME_TEMPLATE.createFreshCopy();
        String externalSampleName =
                tryToGetProperty(sequencingSample, EXTERNAL_SAMPLE_NAME_PROPERTY);
        template.bind("external-sample-name", externalSampleName.replace(' ', '_'));
        template.bind("flow-lane", flowLaneSample.getCode().replace(':', '-'));
        return template.createText();
    }

    private void bindExternalSampleName(Template template, Sample sequencingSample)
    {
        String externalSampleName =
                tryToGetProperty(sequencingSample, EXTERNAL_SAMPLE_NAME_PROPERTY);
        template.bind("external-sample-name", externalSampleName);
    }

    private String calculateCheckSum(File srfFile)
    {
        String checkSum;
        try
        {
            checkSum = MD5ChecksumCalculator.calculate(FileUtils.readFileToByteArray(srfFile));
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
        return checkSum;
    }

    private File tryToFindSrfFile(DatasetDescription datasetDescription)
    {
        File root = new File(storeRoot, datasetDescription.getDataSetLocation());
        return tryToFindSrfFile(root);
    }

    private File tryToFindSrfFile(File file)
    {
        if (file.isFile() && file.getName().endsWith(".srf"))
        {
            return file;
        }
        if (file.isDirectory())
        {
            File[] files = file.listFiles();
            for (File child : files)
            {
                File srfFile = tryToFindSrfFile(child);
                if (srfFile != null)
                {
                    return srfFile;
                }
            }
        }
        return null;
    }

    private Sample getFlowLaneSample(ExternalData dataSet)
    {
        SampleIdentifier identifier = SampleIdentifierFactory.parse(dataSet.getSampleIdentifier());
        return getService().tryGetSampleWithExperiment(identifier);
    }

    private Sample getFlowCellSample(Sample flowLaneSample)
    {
        SampleIdentifier identifier =
                SampleIdentifierFactory.parse(flowLaneSample.getContainer().getIdentifier());
        return getService().tryGetSampleWithExperiment(identifier);
    }

    private Sample getSequencingSample(Sample flowLaneSample)
    {
        List<Sample> parents =
                getService().listSamples(
                        ListSampleCriteria.createForChild(new TechId(flowLaneSample.getId())));
        return parents.get(0);
    }

    private DataSource createDataSource(final String softData)
    {
        try
        {
            return new ByteArrayDataSource(softData, "text/plain");
        } catch (IOException ex)
        {
            throw CheckedExceptionTunnel.wrapIfNecessary(ex);
        }
    }

    private IEncapsulatedOpenBISService getService()
    {
        if (service == null)
        {
            service = ServiceProvider.getOpenBISService();
        }
        return service;
    }

}
