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

package ch.systemsx.cisd.openbis.dss.etl.genedata;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import ch.systemsx.cisd.common.filesystem.FileUtilities;
import ch.systemsx.cisd.etlserver.DefaultStorageProcessor;
import ch.systemsx.cisd.openbis.dss.etl.HCSContainerDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.AbstractFeatureVectorMigrator;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.CanonicalFeatureVector;
import ch.systemsx.cisd.openbis.dss.etl.featurevector.FeatureVectorUploader;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;

/**
 * Imports Genedata feature vectors into the database.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public class GenedataFeatureVectorMigrator extends AbstractFeatureVectorMigrator
{

    private static final String GENEDATA_FEATURES_EXTENSION = ".stat";

    /**
     * @param properties
     */
    public GenedataFeatureVectorMigrator(Properties properties)
    {
        super(properties);
    }

    @Override
    protected AbstractImageDbImporter createImporter(HCSContainerDatasetInfo dataSetInfo,
            File fileToMigrate)
    {
        AbstractImageDbImporter importer;
        importer = new ImporterGenedata(dao, dataSetInfo, fileToMigrate);

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

            for (File file : files)
            {
                if (file.getName().endsWith(GENEDATA_FEATURES_EXTENSION))
                {
                    return file;
                }
            }

            return null;
        }
    }

    /**
     * Helper class for importing genedata feature vecotr files.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    private static class ImporterGenedata extends AbstractImageDbImporter
    {

        /**
         * @param fileToMigrate
         */
        private ImporterGenedata(IImagingQueryDAO dao,
                HCSContainerDatasetInfo screeningDataSetInfo, File fileToMigrate)
        {
            super(dao, screeningDataSetInfo, fileToMigrate);
        }

        @Override
        public void doImport()
        {
            List<String> lines = FileUtilities.loadToStringList(fileToMigrate);
            GenedataFormatToCanonicalFeatureVector convertor =
                    new GenedataFormatToCanonicalFeatureVector(lines,
                            FeatureStorageProcessor.LAYER_PREFIX);
            ArrayList<CanonicalFeatureVector> fvecs = convertor.convert();

            FeatureVectorUploader uploader = new FeatureVectorUploader(dao, screeningDataSetInfo);
            uploader.uploadFeatureVectors(fvecs);
            dao.commit();
            isSuccessful = true;
        }
    }

}
