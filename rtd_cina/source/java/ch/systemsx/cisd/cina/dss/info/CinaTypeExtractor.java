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

package ch.systemsx.cisd.cina.dss.info;

import java.io.File;
import java.util.Properties;

import ch.systemsx.cisd.cina.dss.info.FolderOracle.FolderType;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.FileFormatType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.LocatorType;

/**
 * The extractor expects that certain things have been set up in the database. In particular:
 * <ul>
 * <li>A data set type with code CINA_EXP_PREP &mdash; this is the data set for preparation
 * information for an experiment</li>
 * <li>A data set type with code CINA_SAMPLE_PREP &mdash; this is the data set for preparation
 * information for a sample</li>
 * </ul>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaTypeExtractor implements ITypeExtractor
{

    private final String EXPERIMENT_METADATA_DATASET_TYPE = "CINA_EXP_PREP";

    private final String SAMPLE_METADATA_DATASET_TYPE = "CINA_SAMPLE_PREP";

    private final String UNKNOWN_METADATA_DATASET_TYPE = "UNKNOWN";

    public CinaTypeExtractor(final Properties properties)
    {

    }

    public DataSetType getDataSetType(File incomingDataSetPath)
    {
        FolderOracle folderOracle = new FolderOracle();
        FolderType folderType = folderOracle.getTypeForFolder(incomingDataSetPath);
        DataSetType datasetType = null;
        switch (folderType)
        {
            case DATA_SET:
                datasetType = new DataSetType(UNKNOWN_METADATA_DATASET_TYPE);
                break;
            case EXPERIMENT:
                datasetType = new DataSetType(EXPERIMENT_METADATA_DATASET_TYPE);
                break;
            case SAMPLE:
                datasetType = new DataSetType(SAMPLE_METADATA_DATASET_TYPE);
                break;
            case UNKNOWN:
                datasetType = new DataSetType(UNKNOWN_METADATA_DATASET_TYPE);
                break;

        }
        return datasetType;
    }

    public FileFormatType getFileFormatType(File incomingDataSetPath)
    {
        return new FileFormatType("PROPRIETARY");
    }

    public LocatorType getLocatorType(File incomingDataSetPath)
    {
        return new LocatorType("RELATIVE_LOCATION");
    }

    public String getProcessorType(File incomingDataSetPath)
    {
        return null;
    }

    public boolean isMeasuredData(File incomingDataSetPath)
    {
        FolderOracle folderOracle = new FolderOracle();
        FolderType folderType = folderOracle.getTypeForFolder(incomingDataSetPath);
        boolean isMeasuredData = false;
        switch (folderType)
        {
            case DATA_SET:
                isMeasuredData = true;
                break;
            case EXPERIMENT:
                isMeasuredData = false;
                break;
            case SAMPLE:
                isMeasuredData = false;
                break;
            case UNKNOWN:
                isMeasuredData = false;
                break;

        }
        return isMeasuredData;
    }

}
