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
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.TimingParameters;
import ch.systemsx.cisd.common.exceptions.ConfigurationFailureException;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.filesystem.FastRecursiveHardLinkMaker;
import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.common.filesystem.IFileOperations;
import ch.systemsx.cisd.common.filesystem.IImmutableCopier;
import ch.systemsx.cisd.common.utilities.PropertyUtils;
import ch.systemsx.cisd.openbis.dss.generic.shared.IPostRegistrationDatasetHandler;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * Post registration data set handler which makes a hard-link copy of all flow-line files
 * to associated drop boxes.
 *
 * @author Franz-Josef Elmer
 */
class FlowLineFeeder implements IPostRegistrationDatasetHandler
{
    static final String FLOW_LINE_DROP_BOX_TEMPLATE = "flow-line-drop-box-template";
    static final String FILE_TYPE = ".srf";
    
    private final MessageFormat flowLineDropBoxTemplate;
    private final IImmutableCopier copier;
    private final IFileOperations fileOperations;
    private final List<File> flowLineDataSets = new ArrayList<File>();
    
    FlowLineFeeder(Properties properties)
    {
        flowLineDropBoxTemplate =
                new MessageFormat(PropertyUtils.getMandatoryProperty(properties,
                        FLOW_LINE_DROP_BOX_TEMPLATE));
        copier = FastRecursiveHardLinkMaker.tryCreate(TimingParameters.getDefaultParameters());
        fileOperations = FileOperations.getInstance();
    }

    public void handle(File originalData, DataSetInformation dataSetInformation)
    {
        String flowcellID = originalData.getName();
        List<File> files = new ArrayList<File>();
        findFiles(originalData, files);
        for (File file : files)
        {
            String flowLine = extractFlowLine(file);
            File dropBox = createDropBoxFile(flowLine);
            File flowLineDataSet = new File(dropBox, flowcellID + "_" + flowLine);
            flowLineDataSets.add(flowLineDataSet);
            boolean success = flowLineDataSet.mkdir();
            if (success == false)
            {
                throw new EnvironmentFailureException("Couldn't create folder '"
                        + flowLineDataSet.getAbsolutePath() + "'.");
            }
            success = copier.copyImmutably(file, flowLineDataSet, null);
            if (success == false)
            {
                throw new EnvironmentFailureException("Couldn't create a hard-link copy of '"
                        + file.getAbsolutePath() + "' in folder '"
                        + flowLineDataSet.getAbsolutePath() + "'.");
            }
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
        for (File file : flowLineDataSets)
        {
            fileOperations.deleteRecursively(file);
        }
    }

}
