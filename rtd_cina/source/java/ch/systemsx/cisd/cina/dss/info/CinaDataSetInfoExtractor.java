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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Properties;
import java.util.Map.Entry;

import ch.systemsx.cisd.cina.dss.info.FolderOracle.FolderMetadata;
import ch.systemsx.cisd.cina.dss.info.FolderOracle.FolderType;
import ch.systemsx.cisd.common.exceptions.EnvironmentFailureException;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.etlserver.IDataSetInfoExtractor;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;

/**
 * CINA uses the data store server to register experiments and samples in addition to data sets. The
 * CinaDataSetInfoExtractor follows the conventions agreed to by the CINA project to determine if
 * the file represents an experiment, sample, or data set and takes according action.
 * <p>
 * In the case of data representing experiments and samples, the extractor may need to register the
 * experiment/sample with openBIS before continuing to process the data set.
 * <p>
 * The extractor expects that certain things have been set up in the database. In particular:
 * <ul>
 * <li>A space called CINA</li>
 * </ul>
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class CinaDataSetInfoExtractor implements IDataSetInfoExtractor
{

    public CinaDataSetInfoExtractor(final Properties globalProperties)
    {

    }

    public DataSetInformation getDataSetInformation(File incomingDataSetFile,
            IEncapsulatedOpenBISService openbisService) throws UserFailureException,
            EnvironmentFailureException
    {
        final DataSetInformation dataSetInformation = new DataSetInformation();

        final FolderOracle folderOracle = new FolderOracle();
        FolderMetadata metadata = folderOracle.getMetadataForFolder(incomingDataSetFile);
        FolderType folderType = metadata.getType();

        switch (folderType)
        {
            case DATA_SET:
                processDataSetFolder(metadata.tryMetadataFile(), dataSetInformation, openbisService);
                break;
            case EXPERIMENT:
                processExperimentFolder(metadata.tryMetadataFile(), dataSetInformation,
                        openbisService);
                break;
            case SAMPLE:
                processSampleFolder(metadata.tryMetadataFile(), dataSetInformation, openbisService);
                break;
            case UNKNOWN:
                break;
        }

        return dataSetInformation;
    }

    /**
     * See if the incoming data set folder is contains an experiment and, if so, register the
     * experiment.
     */
    private void processExperimentFolder(final File metadataFile,
            final DataSetInformation dataSetInformation, IEncapsulatedOpenBISService openbisService)
    {
        try
        {
            HashMap<String, String> experimentMetadata = convertMetadataFileToMap(metadataFile);
            ExperimentMetadataExtractor extractor =
                    new ExperimentMetadataExtractor(dataSetInformation, experimentMetadata,
                            getEntityCodeSuffix(), openbisService);
            extractor.processMetadataAndFillDataSetInformation();
        } catch (IOException ex)
        {
            UserFailureException userFailure =
                    new UserFailureException("Could not register experiment", ex);
            throw userFailure;
        }
    }

    /**
     * See if the incoming data set folder is contains a sample and, if so, register the sample.
     */
    private void processSampleFolder(final File metadataFile,
            final DataSetInformation dataSetInformation, IEncapsulatedOpenBISService openbisService)
    {

    }

    /**
     * See if the incoming data set folder is contains a data set and, if so, register the data set.
     */
    private void processDataSetFolder(final File metadataFile,
            final DataSetInformation dataSetInformation, IEncapsulatedOpenBISService openbisService)
    {
    }

    /**
     * Create a suffix for the entity code.
     */
    private String getEntityCodeSuffix()
    {
        Calendar now = GregorianCalendar.getInstance();
        StringBuffer sb = new StringBuffer();
        sb.append(now.get(Calendar.YEAR));
        sb.append(now.get(Calendar.MONTH));
        sb.append(now.get(Calendar.DAY_OF_MONTH));
        sb.append(now.get(Calendar.HOUR_OF_DAY));
        sb.append(now.get(Calendar.MINUTE));
        return sb.toString();
    }

    /**
     * Parse the metadata file and return it as a hashmap. Currently properties files are supported,
     * but CINA may prefer an XML file using the XML schema that LabView supports.
     */
    private HashMap<String, String> convertMetadataFileToMap(File metadataFile) throws IOException,
            FileNotFoundException
    {
        Properties properties = new Properties();
        properties.load(new FileInputStream(metadataFile));

        HashMap<String, String> map = new HashMap<String, String>();
        for (Entry<Object, Object> entry : properties.entrySet())
        {
            map.put((String) entry.getKey(), (String) entry.getValue());
        }
        return map;
    }
}
