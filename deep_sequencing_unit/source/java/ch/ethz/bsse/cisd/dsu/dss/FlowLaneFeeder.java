/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.ethz.bsse.cisd.dsu.dss;

import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.concurrent.ConcurrencyUtilities;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.Status;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.filesystem.FastRecursiveHardLinkMaker;
import ch.systemsx.cisd.common.filesystem.FileConstants;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.process.ProcessExecutionHelper;
import ch.systemsx.cisd.common.process.ProcessIOStrategy;
import ch.systemsx.cisd.common.process.ProcessResult;
import ch.systemsx.cisd.common.properties.ExtendedProperties;
import ch.systemsx.cisd.common.properties.PropertyUtils;
import ch.systemsx.cisd.common.time.TimingParameters;
import ch.systemsx.cisd.etlserver.AbstractPostRegistrationDataSetHandlerForFileBasedUndo;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.RSyncConfig;
import ch.systemsx.cisd.openbis.generic.shared.basic.BasicConstant;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.NewAttachment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.generic.shared.dto.SampleUpdatesDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.ExperimentIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Post registration data set handler which makes a hard-link copy of all flow-lane files to associated drop boxes.
 * 
 * @author Franz-Josef Elmer
 */
class FlowLaneFeeder extends AbstractPostRegistrationDataSetHandlerForFileBasedUndo
{
    static final String META_DATA_FILE_TYPE = ".tsv";

    static final String META_DATA_FILE_PREFIX = "meta-data-file-prefix";

    static final String TRANSFER_PREFIX = "transfer.";

    static final String AFFILIATION_KEY = "AFFILIATION";

    static final String DATA_TRANSFERRED = "DATA_TRANSFERRED";

    static final String EXTERNAL_SAMPLE_NAME_KEY = "EXTERNAL_SAMPLE_NAME";

    static final String FLOW_LANE_DROP_BOX_TEMPLATE = "flow-lane-drop-box-template";

    static final String SRF_INFO_PATH = "srf-info-path";

    static final String ENTITY_SEPARATOR_KEY = "entity-separator";

    static final String DEFAULT_ENTITY_SEPARATOR = "_";

    static final String SRF_FILE_EXTENSION = "srf";

    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FlowLaneFeeder.class);

    private final IEncapsulatedOpenBISService service;

    private final MessageFormat flowLaneDropBoxTemplate;

    private final String metaDataFilePrefix;

    private final String entitySepaparator;

    private final IImmutableCopier copier;

    private final Map<String, File> transferDropBoxes = new HashMap<String, File>();

    private final String srfInfoPathOrNull;

    private boolean updateDataTransferredProperty = true;

    FlowLaneFeeder(Properties properties, IEncapsulatedOpenBISService service)
    {
        super(FileOperations.getInstance());
        this.service = service;
        flowLaneDropBoxTemplate =
                new MessageFormat(PropertyUtils.getMandatoryProperty(properties,
                        FLOW_LANE_DROP_BOX_TEMPLATE));
        metaDataFilePrefix = PropertyUtils.getMandatoryProperty(properties, META_DATA_FILE_PREFIX);
        entitySepaparator = properties.getProperty(ENTITY_SEPARATOR_KEY, DEFAULT_ENTITY_SEPARATOR);
        srfInfoPathOrNull = properties.getProperty(SRF_INFO_PATH);
        if (srfInfoPathOrNull != null)
        {
            File srfInfo = new File(srfInfoPathOrNull);
            if (srfInfo.isFile() == false)
            {
                throw new ConfigurationFailureException("File '" + srfInfo.getAbsolutePath()
                        + "' does not exists or is a folder.");
            }
        }
        copier = FastRecursiveHardLinkMaker.tryCreate(TimingParameters.getDefaultParameters(),
                RSyncConfig.getInstance().getAdditionalCommandLineOptions());
        Properties transferDropBoxMapping =
                ExtendedProperties.getSubset(properties, TRANSFER_PREFIX, true);
        Set<Entry<Object, Object>> entries = transferDropBoxMapping.entrySet();
        for (Entry<Object, Object> entry : entries)
        {
            String affiliation = entry.getKey().toString();
            String dropBoxPath = entry.getValue().toString();
            File dropBox = new File(dropBoxPath);
            if (dropBox.isDirectory() == false)
            {
                throw new EnvironmentFailureException("Transfer drop box for " + affiliation
                        + " doen't exist or isn't a folder: " + dropBox.getAbsolutePath());
            }
            transferDropBoxes.put(affiliation, dropBox);
        }
    }

    void setUpdateDataTransferredProperty(boolean updateDataTransferredProperty)
    {
        this.updateDataTransferredProperty = updateDataTransferredProperty;
    }

    @Override
    public Status handle(File originalData, DataSetInformation dataSetInformation,
            Map<String, String> parameterBindings)
    {
        Map<String, Sample> flowLaneSampleMap = createFlowLaneSampleMap(dataSetInformation);
        String flowcellID = originalData.getName();
        List<File> srfFiles = new ArrayList<File>();
        FileUtilities.findFiles(originalData, srfFiles, createSrfFileFilter());
        if (srfFiles.size() < flowLaneSampleMap.size())
        {
            throw new EnvironmentFailureException("Only " + srfFiles.size()
                    + " flow lane files found instead of " + flowLaneSampleMap.size() + ".");
        }
        Set<String> processedFlowLanes = new LinkedHashSet<String>();
        for (File srfFile : srfFiles)
        {
            List<String> srfInfo = getSRFInfo(srfFile);
            String flowLane = extractFlowLane(srfFile);
            if (processedFlowLanes.contains(flowLane))
            {
                throw new UserFailureException("Flow lane " + flowLane + " already registered.");
            }
            Sample flowLaneSample = flowLaneSampleMap.get(flowLane);
            if (flowLaneSample == null)
            {
                throw new UserFailureException("No flow lane sample for flow lane " + flowLane
                        + " found.");
            }
            File flowLaneDropBox = createDropBoxFile(flowLane);
            String fileName =
                    flowLaneSample.getSpace().getCode() + entitySepaparator + flowcellID
                            + FlowLaneDataSetInfoExtractor.FLOW_LANE_NUMBER_SEPARATOR + flowLane;
            File flowLaneDataSet = new File(flowLaneDropBox, fileName);
            if (flowLaneDataSet.exists())
            {
                throw new EnvironmentFailureException("There is already a data set for flow lane "
                        + flowLane + ".");
            }
            addFileForUndo(flowLaneDataSet);
            boolean success = flowLaneDataSet.mkdir();
            if (success == false)
            {
                throw new EnvironmentFailureException("Couldn't create folder '"
                        + flowLaneDataSet.getAbsolutePath() + "'.");
            }

            createHardLink(srfFile, flowLaneDataSet);
            createMetaDataFileAndHardLinkInTransferDropBox(flowLaneDataSet, flowLaneSample,
                    flowLane, srfInfo);

            // store additional flow lane files
            List<File> flowLaneFiles = new ArrayList<File>();
            FileUtilities.findFiles(originalData, flowLaneFiles,
                    createPrefixFileFilter(FilenameUtils.getBaseName(srfFile.getName())));
            for (File file : flowLaneFiles)
            {
                if (FilenameUtils.getExtension(file.getName()).equals(SRF_FILE_EXTENSION) == false)
                {
                    createHardLink(file, flowLaneDataSet);
                }
            }

            if (updateDataTransferredProperty)
            {
                service.updateSample(createUpdates(flowLaneSample,
                        createDataTransferredProperty(flowLaneSample)));
            }

            File markerFile =
                    new File(flowLaneDropBox, FileConstants.IS_FINISHED_PREFIX + fileName);
            addFileForUndo(markerFile);
            FileUtilities.writeToFile(markerFile, "");
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Flow lane file '" + srfFile
                        + "' successfully dropped into drop box '" + flowLaneDropBox + "' as '"
                        + flowLaneDataSet.getName() + "'.");
            }
            processedFlowLanes.add(flowLane);
        }
        return Status.OK;
    }

    private List<String> getSRFInfo(File file)
    {
        if (srfInfoPathOrNull == null)
        {
            return Collections.emptyList();
        }
        List<String> command = Arrays.asList(srfInfoPathOrNull, "-l1", file.getAbsolutePath());
        ProcessResult result =
                ProcessExecutionHelper.run(command, operationLog, operationLog,
                        ConcurrencyUtilities.NO_TIMEOUT, ProcessIOStrategy.DEFAULT_IO_STRATEGY,
                        true);
        List<String> output = result.getOutput();
        if (result.isOK() == false)
        {
            StringBuilder builder = new StringBuilder();
            String startupFailureMessage = result.getStartupFailureMessage();
            if (StringUtils.isNotBlank(startupFailureMessage))
            {
                builder.append("\nStartup failure message:").append(startupFailureMessage);
            }
            builder.append("\nStandard out and error:");
            for (String outputLine : output)
            {
                builder.append("\n").append(outputLine);
            }
            throw new UserFailureException("Invalid SRF file '" + file.getAbsolutePath() + "':"
                    + builder);
        }
        return output;
    }

    private Map<String, Sample> createFlowLaneSampleMap(DataSetInformation dataSetInformation)
    {
        SampleIdentifier sampleIdentifier = dataSetInformation.getSampleIdentifier();
        Sample flowCell = service.tryGetSampleWithExperiment(sampleIdentifier);
        if (flowCell == null)
        {
            throw new UserFailureException("Unkown flow cell sample: " + sampleIdentifier);
        }
        TechId flowCellID = new TechId(flowCell.getId());
        ListSampleCriteria criteria = ListSampleCriteria.createForContainer(flowCellID);
        List<Sample> flowLaneSamples = service.listSamples(criteria);
        Map<String, Sample> flowLaneSampleMap = new LinkedHashMap<String, Sample>();
        for (Sample flowLaneSample : flowLaneSamples)
        {
            flowLaneSampleMap.put(flowLaneSample.getSubCode(), flowLaneSample);
        }
        return flowLaneSampleMap;
    }

    private void createMetaDataFileAndHardLinkInTransferDropBox(File flowLaneDataSet,
            Sample flowLaneSample, String flowLane, List<String> srfInfo)
    {
        if (flowLaneSample == null)
        {
            throw new UserFailureException("No flow lane sample for flow lane " + flowLane
                    + " exists");
        }
        StringBuilder builder = new StringBuilder();
        addLine(builder, "Parent", flowLaneSample.getGeneratedFrom().getIdentifier());
        addLine(builder, "Code", flowLaneSample.getCode());
        addLine(builder, "Contact Person Email", flowLaneSample.getRegistrator().getEmail());
        SampleIdentifier identifier = SampleIdentifierFactory.parse(flowLaneSample.getIdentifier());
        IEntityProperty[] propertiesOrNull =
                service.tryGetPropertiesOfTopSample(identifier);
        File dropBox = null;
        if (propertiesOrNull != null)
        {
            for (IEntityProperty property : propertiesOrNull)
            {
                PropertyType propertyType = property.getPropertyType();
                String value = property.tryGetAsString();
                addLine(builder, propertyType.getCode(), value);
                String code = propertyType.getCode();
                if (code.equals(AFFILIATION_KEY))
                {
                    dropBox = transferDropBoxes.get(value);
                }
            }
        }
        if (srfInfo.isEmpty() == false)
        {
            builder.append("\n==== SRF Info ====\t\n");
            for (String line : srfInfo)
            {
                builder.append(line).append("\t\n");
            }
        }
        String sampleCode = flowLaneSample.getCode();
        String metaFileName =
                metaDataFilePrefix + escapeSampleCode(sampleCode) + META_DATA_FILE_TYPE;
        FileUtilities.writeToFile(new File(flowLaneDataSet, metaFileName), builder.toString());
        copyToDropBox(dropBox, flowLaneDataSet);
    }

    private IEntityProperty createDataTransferredProperty(Sample flowLaneSample)
    {
        final PropertyType propertyType = extractDataTransferredProperty(flowLaneSample);
        final String currentDate =
                DateFormatUtils.format(new Date(), BasicConstant.CANONICAL_DATE_FORMAT_PATTERN);

        final IEntityProperty result = new EntityProperty();
        result.setPropertyType(propertyType);
        result.setValue(currentDate);
        return result;
    }

    private PropertyType extractDataTransferredProperty(Sample flowLaneSample)
    {
        SampleType flowLaneType = service.getSampleType(flowLaneSample.getSampleType().getCode());
        PropertyType propertyType = null;
        for (EntityTypePropertyType<SampleType> entityTypePropertyType : flowLaneType
                .getAssignedPropertyTypes())
        {
            if (entityTypePropertyType.getPropertyType().getCode().equals(DATA_TRANSFERRED))
            {
                propertyType = entityTypePropertyType.getPropertyType();
            }
        }
        if (propertyType == null)
        {
            throw new UserFailureException(DATA_TRANSFERRED
                    + " property type doesn't exist or is not assigned to Flow Lane samples");
        }
        return propertyType;
    }

    public static SampleUpdatesDTO createUpdates(Sample sample, IEntityProperty newEntityProperty)
    {
        final TechId sampleId = TechId.create(sample);
        final List<IEntityProperty> properties =
                new ArrayList<IEntityProperty>(sample.getProperties());
        properties.add(newEntityProperty);
        final ExperimentIdentifier experimentIdentifierOrNull =
                (sample.getExperiment() != null) ? new ExperimentIdentifier(sample.getExperiment())
                        : null;
        final Collection<NewAttachment> attachments = new ArrayList<NewAttachment>();
        final int version = sample.getVersion();
        final SampleIdentifier sampleIdentifier =
                SampleIdentifierFactory.parse(sample.getIdentifier());
        final String containerIdentifierOrNull =
                (sample.getContainer() != null) ? sample.getContainer().getIdentifier() : null;
        final String[] modifiedParentCodesOrNull = null;
        return new SampleUpdatesDTO(sampleId, properties, experimentIdentifierOrNull, null, attachments,
                version, sampleIdentifier, containerIdentifierOrNull, modifiedParentCodesOrNull);
    }

    private void copyToDropBox(File dropBoxOrNull, File flowLaneDataSet)
    {
        if (dropBoxOrNull != null)
        {
            File[] files = flowLaneDataSet.listFiles();
            for (File file : files)
            {
                createHardLink(file, dropBoxOrNull);
                addFileForUndo(new File(dropBoxOrNull, file.getName()));
            }
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Flow lane data set '" + flowLaneDataSet.getName()
                        + "' successfully transfered to drop box '" + dropBoxOrNull + "'");
            }
        }
    }

    static String escapeSampleCode(String sampleCode)
    {
        return sampleCode == null ? null : sampleCode.replace(
                SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING, "_");
    }

    private void addLine(StringBuilder builder, String key, String value)
    {
        builder.append(key).append('\t').append(value).append('\n');
    }

    private void createHardLink(File file, File folder)
    {
        final Status status = copier.copyImmutably(file, folder, null);
        if (status.isError())
        {
            throw new EnvironmentFailureException("Couldn't create a hard-link copy of '"
                    + file.getAbsolutePath() + "' in folder '" + folder.getAbsolutePath() + "'. ["
                    + status + "]");
        }
    }

    private File createDropBoxFile(String flowLane)
    {
        File dropBox = new File(flowLaneDropBoxTemplate.format(new Object[] { flowLane }));
        if (dropBox.exists() == false)
        {
            throw new ConfigurationFailureException("Drop box '" + dropBox + "' does not exist.");
        }
        if (dropBox.isDirectory() == false)
        {
            throw new ConfigurationFailureException("Drop box '" + dropBox
                    + "' is not a directory.");
        }
        return dropBox;
    }

    private String extractFlowLane(File file)
    {
        String name = file.getName();
        String nameWithoutType = name.substring(0, name.lastIndexOf('.'));
        int lastIndexOfUnderScore = nameWithoutType.lastIndexOf('_');
        String flowLane = nameWithoutType;
        if (lastIndexOfUnderScore >= 0)
        {
            flowLane = nameWithoutType.substring(lastIndexOfUnderScore + 1);
        }
        return flowLane;
    }

    private FileFilter createSrfFileFilter()
    {
        return new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    return file.isFile()
                            && FilenameUtils.getExtension(file.getName())
                                    .equals(SRF_FILE_EXTENSION);
                }
            };
    }

    private FileFilter createPrefixFileFilter(final String prefix)
    {
        return new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    return file.getName().startsWith(prefix);
                }
            };
    }

}
