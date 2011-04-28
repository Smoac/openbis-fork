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
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.base.exceptions.IOExceptionUnchecked;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.openbis.dss.etl.HCSContainerDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CsvFeatureVectorParser.CsvFeatureVectorParserConfiguration;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CsvFileReaderHelper;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.DatasetFileLines;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class CsvFeatureVectorMigrator extends AbstractFeatureVectorMigrator
{
    protected final FeatureVectorStorageProcessorConfiguration configuration;

    protected final CsvFeatureVectorParserConfiguration convertorConfig;

    /**
     * @param properties
     */
    public CsvFeatureVectorMigrator(Properties properties)
    {
        super(properties);

        this.configuration = new FeatureVectorStorageProcessorConfiguration(properties);
        convertorConfig = new CsvFeatureVectorParserConfiguration(configuration);
    }

    @Override
    protected AbstractImageDbImporter createImporter(HCSContainerDatasetInfo dataSetInfo,
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

        private final CsvFeatureVectorParserConfiguration convertorConfig;

        protected ImporterCsv(IImagingQueryDAO dao, HCSContainerDatasetInfo screeningDataSetInfo,
                File fileToMigrate, FeatureVectorStorageProcessorConfiguration configuration,
                CsvFeatureVectorParserConfiguration convertorConfig)
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
                        new CsvToCanonicalFeatureVector(fileLines, convertorConfig,
                                screeningDataSetInfo.getContainerRows(),
                                screeningDataSetInfo.getContainerColumns());
                List<CanonicalFeatureVector> fvecs = convertor.convert();

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
