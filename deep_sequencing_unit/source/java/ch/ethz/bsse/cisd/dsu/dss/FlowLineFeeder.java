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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.Constants;
import ch.systemsx.cisd.common.TimingParameters;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FastRecursiveHardLinkMaker;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.common.utilities.ExtendedProperties;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PropertyType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifierFactory;

/**
 * Post registration data set handler which makes a hard-link copy of all flow-line files
 * to associated drop boxes.
 *
 * @author Franz-Josef Elmer
 */
class FlowLineFeeder implements IPostRegistrationDatasetHandler
{
    static final String META_DATA_FILE_TYPE = ".tsv";
    static final String TRANSFER_PREFIX = "transfer.";
    static final String AFFILIATION_KEY = "AFFILIATION";
    static final String EXTERNAL_SAMPLE_NAME_KEY = "EXTERNAL_SAMPLE_NAME";
    static final String FLOW_LINE_DROP_BOX_TEMPLATE = "flow-line-drop-box-template";
    static final String ENTITY_SEPARATOR_KEY = "entity-separator";
    static final String DEFAULT_ENTITY_SEPARATOR = "_";
    static final String FILE_TYPE = ".srf";
    
    private final static Logger operationLog =
            LogFactory.getLogger(LogCategory.OPERATION, FlowLineFeeder.class);
    
    private final IEncapsulatedOpenBISService service;
    private final MessageFormat flowLineDropBoxTemplate;
    private final String entitySepaparator;
    private final IImmutableCopier copier;
    private final IFileOperations fileOperations;
    private final List<File> createdFiles = new ArrayList<File>();
    private final Map<String, File> transferDropBoxes = new HashMap<String, File>();
    
    FlowLineFeeder(Properties properties, IEncapsulatedOpenBISService service)
    {
        this.service = service;
        flowLineDropBoxTemplate =
                new MessageFormat(PropertyUtils.getMandatoryProperty(properties,
                        FLOW_LINE_DROP_BOX_TEMPLATE));
        entitySepaparator = properties.getProperty(ENTITY_SEPARATOR_KEY, DEFAULT_ENTITY_SEPARATOR);
        copier = FastRecursiveHardLinkMaker.tryCreate(TimingParameters.getDefaultParameters());
        fileOperations = FileOperations.getInstance();
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

    public void handle(File originalData, DataSetInformation dataSetInformation)
    {
        Map<String, Sample> flowLineSampleMap = createFlowLineSampleMap(dataSetInformation);
        String flowcellID = originalData.getName();
        List<File> files = new ArrayList<File>();
        findFiles(originalData, files);
        for (File file : files)
        {
            String flowLine = extractFlowLine(file);
            Sample flowLineSample = flowLineSampleMap.get(flowLine);
            File dropBox = createDropBoxFile(flowLine);
            String fileName =
                    flowLineSample.getGroup().getCode() + entitySepaparator + flowcellID
                            + SampleIdentifier.CONTAINED_SAMPLE_CODE_SEPARARTOR_STRING + flowLine;
            File flowLineDataSet = new File(dropBox, fileName);
            if (flowLineDataSet.exists())
            {
                throw new EnvironmentFailureException("There is already a data set for flow line "
                        + flowLine + ".");
            }
            createdFiles.add(flowLineDataSet);
            boolean success = flowLineDataSet.mkdir();
            if (success == false)
            {
                throw new EnvironmentFailureException("Couldn't create folder '"
                        + flowLineDataSet.getAbsolutePath() + "'.");
            }
            createHartLink(file, flowLineDataSet);
            createMetaDataFileAndHartLinkInTransferDropBox(flowLineDataSet, flowLineSample, flowLine);
            File markerFile = new File(dropBox, Constants.IS_FINISHED_PREFIX + fileName);
            createdFiles.add(markerFile);
            FileUtilities.writeToFile(markerFile, "");
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Flow line file '" + file
                        + "' successfully dropped into drop box '" + dropBox
                        + "' as '" + flowLineDataSet.getName() + "'.");
            }
        }

    }

    private Map<String, Sample> createFlowLineSampleMap(DataSetInformation dataSetInformation)
    {
        SampleIdentifier sampleIdentifier = dataSetInformation.getSampleIdentifier();
        Sample flowCell = service.tryGetSampleWithExperiment(sampleIdentifier);
        if (flowCell == null)
        {
            throw new UserFailureException("Unkown flow cell sample: " + sampleIdentifier);
        }
        TechId flowCellID = new TechId(flowCell.getId());
        ListSampleCriteria criteria = ListSampleCriteria.createForContainer(flowCellID);
        List<Sample> flowLineSamples = service.listSamples(criteria);
        Map<String, Sample> flowLineSampleMap = new LinkedHashMap<String, Sample>();
        for (Sample flowLineSample : flowLineSamples)
        {
            flowLineSampleMap.put(flowLineSample.getSubCode(), flowLineSample);
        }
        return flowLineSampleMap;
    }

    private void createMetaDataFileAndHartLinkInTransferDropBox(File flowLineDataSet,
            Sample flowLineSample, String flowLine)
    {
        if (flowLineSample == null)
        {
            throw new UserFailureException("No flow line sample for flow line " + flowLine + " exists");
        }
        StringBuilder builder = new StringBuilder();
        addLine(builder, "Parent", flowLineSample.getGeneratedFrom().getIdentifier());
        addLine(builder, "Code", flowLineSample.getCode());
        addLine(builder, "Contact Person Email", flowLineSample.getRegistrator().getEmail());
        SampleIdentifier identifier = SampleIdentifierFactory.parse(flowLineSample.getIdentifier());
        IEntityProperty[] properties = service.getPropertiesOfTopSampleRegisteredFor(identifier);
        File dropBox = null;
        String externalSampleName = null;
        for (IEntityProperty property : properties)
        {
            PropertyType propertyType = property.getPropertyType();
            String value = property.tryGetAsString();
            addLine(builder, propertyType.getLabel(), value);
            String code = propertyType.getCode();
            if (code.equals(AFFILIATION_KEY))
            {
                dropBox = transferDropBoxes.get(value);
            }
            if (code.equals(EXTERNAL_SAMPLE_NAME_KEY))
            {
                externalSampleName = value;
            }
        }
        String metaFileName =
                flowLineSample.getCode()
                        + (externalSampleName == null ? "" : "_" + externalSampleName) + META_DATA_FILE_TYPE;
        FileUtilities.writeToFile(new File(flowLineDataSet, metaFileName), builder.toString());
        if (dropBox != null)
        {
            createHartLink(flowLineDataSet, dropBox);
            createdFiles.add(new File(dropBox, flowLineDataSet.getName()));
            if (operationLog.isInfoEnabled())
            {
                operationLog.info("Flow line data set '" + flowLineDataSet.getName()
                        + "' successfully transfered to drop box '" + dropBox + "'");
            }
        }
    }
    
    private void addLine(StringBuilder builder, String key, String value)
    {
        builder.append(key).append('\t').append(value).append('\n');
    }

    private void createHartLink(File file, File folder)
    {
        boolean success;
        success = copier.copyImmutably(file, folder, null);
        if (success == false)
        {
            throw new EnvironmentFailureException("Couldn't create a hard-link copy of '"
                    + file.getAbsolutePath() + "' in folder '"
                    + folder.getAbsolutePath() + "'.");
        }
    }
    
    private File createDropBoxFile(String flowLine)
    {
        File dropBox = new File(flowLineDropBoxTemplate.format(new Object[] {flowLine}));
        if (dropBox.exists() == false)
        {
            throw new ConfigurationFailureException("Drop box '" + dropBox + "' does not exist.");
        }
        if (dropBox.isDirectory() == false)
        {
            throw new ConfigurationFailureException("Drop box '" + dropBox + "' is not a directory.");
        }
        return dropBox;
    }

    private String extractFlowLine(File file)
    {
        String name = file.getName();
        String nameWithoutType = name.substring(0, name.lastIndexOf('.'));
        int lastIndexOfUnderScore = nameWithoutType.lastIndexOf('_');
        String flowLine = nameWithoutType;
        if (lastIndexOfUnderScore >= 0)
        {
            flowLine = nameWithoutType.substring(lastIndexOfUnderScore + 1);
        }
        return flowLine;
    }
    
    private void findFiles(File file, List<File> files)
    {
        if (file.isFile() && file.getName().endsWith(FILE_TYPE))
        {
            files.add(file);
        }
        if (file.isDirectory())
        {
            for (File child : file.listFiles())
            {
                findFiles(child, files);
            }
        }
    }

    public void undoLastOperation()
    {
        if (operationLog.isInfoEnabled())
        {
            operationLog.info("Undo last operation by deleting following files: " + createdFiles);
        }
            
        for (File file : createdFiles)
        {
            if (file.exists())
            {
                fileOperations.deleteRecursively(file);
            }
        }
    }

}
