/*
 * Copyright 2014 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.dss.etl.jython.v2;

import java.io.File;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSet;
import ch.systemsx.cisd.etlserver.registrator.api.v2.IDataSetRegistrationTransactionV2;
import ch.systemsx.cisd.etlserver.registrator.api.v2.impl.DataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.impl.ImageDataSetInformation;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v2.impl.FeatureVectorContainerDataSet;
import ch.systemsx.cisd.openbis.dss.etl.dto.api.v2.impl.ImageContainerDataSet;

/**
 * Helper class performing move and copy operations for on a {@link IDataSetRegistrationTransactionV2} instance. 
 * If we are dealing with the image dataset container then the file action is delegated to the original dataset. 
 * Otherwise a default implementation is used.
 * 
 * @author Franz-Josef Elmer
 */
class ImagingDataSetRegistrationFileOperations
{
    private final IDataSetRegistrationFileOperations operations;
    private final String originalDirName;

    ImagingDataSetRegistrationFileOperations(IDataSetRegistrationFileOperations operations, String originalDirName)
    {
        this.operations = operations;
        this.originalDirName = originalDirName;
    }
    
    public String moveFile(String src, IDataSet dst)
    {
        return moveFile(src, dst, new File(src).getName());
    }

    public String moveFile(String src, IDataSet dst, String dstInDataset)
    {
        return performFileAction(new IFileAction()
            {
                @Override
                public String perform(String source, IDataSet dataSet, String destinationInDataSet)
                {
                    return operations.moveFile(source, dataSet, destinationInDataSet);
                }
                
                @Override
                public String getName()
                {
                    return "move";
                }
            }, src, dst, dstInDataset);
    }

    public String copyFile(String src, IDataSet dst, String dstInDataset, final boolean hardLink)
    {
        return performFileAction(new IFileAction()
            {
                
                @Override
                public String perform(String source, IDataSet dataSet, String destinationInDataSet)
                {
                    return operations.copyFile(source, dataSet, destinationInDataSet, hardLink);
                }
                
                @Override
                public String getName()
                {
                    return "copy";
                }
            }, src, dst, dstInDataset);
    }
    
    private static interface IFileAction
    {
        public String perform(String source, IDataSet dataSet, String destinationInDataSet);
        
        public String getName();
    }
    
    private String performFileAction(IFileAction action, String src, IDataSet dst, String dstInDataset)
    {
        ImageContainerDataSet imageContainerDataset = tryAsImageContainerDataset(dst);

        if (imageContainerDataset != null)
        {
            String destination = getDestinationInOriginal(dstInDataset);
            DataSet<ImageDataSetInformation> originalDataset =
                    imageContainerDataset.getOriginalDataset();
            if (originalDataset == null)
            {
                throw new UserFailureException(
                        "Cannot " + action.getName() + " the files because the original dataset is missing: " + src);
            }

            ImageDataSetInformation dataSetInformation =
                    originalDataset.getRegistrationDetails().getDataSetInformation();

            if (dataSetInformation.getDatasetRelativeImagesFolderPath() == null)
            {
                dataSetInformation.setDatasetRelativeImagesFolderPath(new File(destination));
            }

            return action.perform(src, originalDataset, destination);
        }

        FeatureVectorContainerDataSet featureContainer = tryAsFeatureVectorContainerDataset(dst);
        if (featureContainer != null)
        {
            IDataSet originalDataSet = featureContainer.getOriginalDataset();

            if (originalDataSet == null)
            {
                throw new UserFailureException(
                        "Cannot " + action.getName() + " the files because the original dataset is missing: " + src);
            }

            return action.perform(src, originalDataSet, dstInDataset);
        }

        return action.perform(src, dst, dstInDataset);
    }
    
    private String getDestinationInOriginal(String dstInDataset)
    {
        String destination = dstInDataset;
        if (destination.startsWith(originalDirName) == false)
        {
            destination = prependOriginalDirectory(destination).getPath();
        }
        return destination;
    }
    
    private File prependOriginalDirectory(String directoryPath)
    {
        return new File(originalDirName + File.separator + directoryPath);
    }

    private static FeatureVectorContainerDataSet tryAsFeatureVectorContainerDataset(IDataSet dataset)
    {
        if (dataset instanceof FeatureVectorContainerDataSet)
        {
            return (FeatureVectorContainerDataSet) dataset;
        }
        return null;
    }

    private static ImageContainerDataSet tryAsImageContainerDataset(IDataSet dataset)
    {
        if (dataset instanceof ImageContainerDataSet)
        {
            return (ImageContainerDataSet) dataset;
        } else
        {
            return null;
        }
    }

}
