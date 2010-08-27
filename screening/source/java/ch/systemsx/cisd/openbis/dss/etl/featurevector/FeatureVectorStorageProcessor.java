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

package ch.systemsx.cisd.openbis.dss.etl.featurevector;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.mail.IMailClient;
import ch.systemsx.cisd.etlserver.AbstractDelegatingStorageProcessor;
import ch.systemsx.cisd.etlserver.ITypeExtractor;
import ch.systemsx.cisd.openbis.dss.etl.ScreeningContainerDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CsvToCanonicalFeatureVector.CsvToCanonicalFeatureVectorConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.dss.generic.shared.dto.DataSetInformation;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.utils.CsvFileReaderHelper;

/**
 * Extract features from the file and store them in the database.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class FeatureVectorStorageProcessor extends AbstractDelegatingStorageProcessor
{
    private static final String ORIGINAL_DIR = ScreeningConstants.ORIGINAL_DATA_DIR;

    private final FeatureVectorStorageProcessorConfiguration configuration;

    private final CsvToCanonicalFeatureVectorConfiguration convertorConfig;

    private final DataSource dataSource;

    private final IEncapsulatedOpenBISService openBisService;

    // Execution state of this object -- set to null after an execution is finished.
    private IImagingQueryDAO dataAccessObject = null;

    public FeatureVectorStorageProcessor(Properties properties)
    {
        super(properties);
        this.configuration = new FeatureVectorStorageProcessorConfiguration(properties);
        convertorConfig =
                new CsvToCanonicalFeatureVectorConfiguration(configuration.getWellRow(),
                        configuration.getWellColumn());
        this.dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);
        this.openBisService = ServiceProvider.getOpenBISService();
    }

    @Override
    public File storeData(final DataSetInformation dataSetInformation,
            final ITypeExtractor typeExtractor, final IMailClient mailClient,
            final File incomingDataSetDirectory, final File rootDir)
    {
        File answer =
                super.storeData(dataSetInformation, typeExtractor, mailClient,
                        incomingDataSetDirectory, rootDir);
        // Import into the data base
        File parent = new File(answer, ORIGINAL_DIR);
        File dataSet = new File(parent, incomingDataSetDirectory.getName());

        try
        {
            loadDataSetIntoDatabase(dataSet, dataSetInformation);
        } catch (IOException ex)
        {
            throw new IOExceptionUnchecked(ex);
        }

        return answer;
    }

    private void loadDataSetIntoDatabase(File dataSet, DataSetInformation dataSetInformation)
            throws IOException
    {
        ScreeningContainerDatasetInfo datasetInfo = createScreeningDatasetInfo(dataSetInformation);
        DatasetFileLines fileLines = getDatasetFileLines(dataSet);
        CsvToCanonicalFeatureVector convertor =
                new CsvToCanonicalFeatureVector(fileLines, convertorConfig, datasetInfo
                        .getContainerRows(), datasetInfo.getContainerColumns());
        List<CanonicalFeatureVector> fvecs = convertor.convert();

        dataAccessObject = createDAO();
        FeatureVectorUploader uploader = new FeatureVectorUploader(dataAccessObject, datasetInfo);
        uploader.uploadFeatureVectors(fvecs);
    }

    private ScreeningContainerDatasetInfo createScreeningDatasetInfo(
            DataSetInformation dataSetInformation)
    {
        Sample sampleOrNull = tryFindSampleForDataSet(dataSetInformation);
        return ScreeningContainerDatasetInfo.createScreeningDatasetInfoWithSample(
                dataSetInformation, sampleOrNull);
    }

    private Sample tryFindSampleForDataSet(DataSetInformation dataSetInformation)
    {
        Sample sampleOrNull = dataSetInformation.tryToGetSample();
        if (null == sampleOrNull)
        {
            // check the parent data sets for a sample
            List<String> parentDataSetCodes = dataSetInformation.getParentDataSetCodes();
            for (String dataSetCode : parentDataSetCodes)
            {
                ExternalData externalData = openBisService.tryGetDataSetForServer(dataSetCode);
                if (externalData == null)
                {
                    throw new UserFailureException("Cannot find a parent dataset in openBIS: "
                            + dataSetCode);
                }
                if (externalData.getSample() != null)
                {
                    sampleOrNull = externalData.getSample();
                    break;
                }
            }
        }
        return sampleOrNull;
    }

    private IImagingQueryDAO createDAO()
    {
        return QueryTool.getQuery(dataSource, IImagingQueryDAO.class);
    }

    @Override
    public void commit()
    {
        super.commit();

        if (null == dataAccessObject)
        {
            return;
        }

        dataAccessObject.commit();
        closeDataAccessObject();
    }

    /**
     * Close the DAO and set it to null to make clear that it is not initialized.
     */
    private void closeDataAccessObject()
    {
        dataAccessObject.close();
        dataAccessObject = null;
    }

    @Override
    public UnstoreDataAction rollback(final File incomingDataSetDirectory,
            final File storedDataDirectory, Throwable exception)
    {
        // Delete the data from the database
        if (null != dataAccessObject)
        {
            dataAccessObject.rollback();
            closeDataAccessObject();
        }

        return super.rollback(incomingDataSetDirectory, storedDataDirectory, exception);
    }

    /**
     * Return the tabular data as a DatasetFileLines.
     */
    private DatasetFileLines getDatasetFileLines(File file) throws IOException
    {
        return CsvFileReaderHelper.getDatasetFileLines(file, configuration);
    }

}
