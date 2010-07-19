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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.openbis.dss.etl.ScreeningContainerDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CsvToCanonicalFeatureVector.CsvToCanonicalFeatureVectorConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.server.plugins.tasks.DatasetFileLines;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.utils.CsvFileReaderHelper;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CsvFeatureVectorMigrator extends AbstractFeatureVectorMigrator
{
    protected final FeatureVectorStorageProcessorConfiguration configuration;

    protected final CsvToCanonicalFeatureVectorConfiguration convertorConfig;

    /**
     * @param properties
     */
    public CsvFeatureVectorMigrator(Properties properties)
    {
        super(properties);

        this.configuration = new FeatureVectorStorageProcessorConfiguration(properties);
        convertorConfig =
                new CsvToCanonicalFeatureVectorConfiguration(configuration.getWellRow(),
                        configuration.getWellColumn());
    }

    @Override
    protected AbstractImageDbImporter createImporter(ScreeningContainerDatasetInfo dataSetInfo,
            File fileToMigrate)
    {
        AbstractImageDbImporter importer;

        importer = new ImporterCsv(dao, dataSetInfo, fileToMigrate, configuration, convertorConfig);

        return importer;
    }

    @Override
    protected AbstractMigrationDecision createMigrationDecision(File dataset)
    {
        AbstractMigrationDecision decision = new MigrationDecision(dataset, knownDataSetsByCode);
        return decision;
    }

    /**
     * Helper class for deciding if a file needs to be migrated.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class MigrationDecision extends AbstractMigrationDecision
    {

        /**
         * @param dataset
         * @param knownDataSetsByCode
         */
        public MigrationDecision(File dataset,
                HashMap<String, SimpleDataSetInformationDTO> knownDataSetsByCode)
        {
            super(dataset, knownDataSetsByCode);
        }

        @Override
        protected File tryFileToMigrate()
        {
            File originalDataset = DefaultStorageProcessor.getOriginalDirectory(dataset);
            File[] files = originalDataset.listFiles();

            if (files.length == 1)
            {
                File file = files[0];
                if (file.isDirectory())
                {
                    return null;
                }
                return file;
            }
            return null;
        }

    }

    /**
     * Helper class for importing CSV feature vector files
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class ImporterCsv extends AbstractImageDbImporter
    {
        private final FeatureVectorStorageProcessorConfiguration configuration;

        private final CsvToCanonicalFeatureVectorConfiguration convertorConfig;

        protected ImporterCsv(IImagingQueryDAO dao,
                ScreeningContainerDatasetInfo screeningDataSetInfo, File fileToMigrate,
                FeatureVectorStorageProcessorConfiguration configuration,
                CsvToCanonicalFeatureVectorConfiguration convertorConfig)
        {
            super(dao, screeningDataSetInfo, fileToMigrate);
            this.configuration = configuration;
            this.convertorConfig = convertorConfig;
        }

        @Override
        public void doImport()
        {
            DatasetFileLines fileLines;
            try
            {
                fileLines = getDatasetFileLines(fileToMigrate);
                CsvToCanonicalFeatureVector convertor =
                        new CsvToCanonicalFeatureVector(fileLines, convertorConfig);
                ArrayList<CanonicalFeatureVector> fvecs = convertor.convert();

                FeatureVectorUploader uploader =
                        new FeatureVectorUploader(dao, screeningDataSetInfo);
                uploader.uploadFeatureVectors(fvecs);
                dao.commit();
                isSuccessful = true;
            } catch (IOException ex)
            {
                throw new IOExceptionUnchecked(ex);
            }

        }

        /**
         * Return the tabular data as a DatasetFileLines.
         */
        private DatasetFileLines getDatasetFileLines(File file) throws IOException
        {
            return CsvFileReaderHelper.getDatasetFileLines(file, configuration);
        }

    }

}
