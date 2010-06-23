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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import net.lemnik.eodsql.QueryTool;

import ch.systemsx.cisd.etlserver.plugins.IMigrator;
import ch.systemsx.cisd.openbis.dss.etl.ScreeningContainerDatasetInfo;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.IImagingUploadDAO;
import ch.systemsx.cisd.openbis.dss.etl.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.SimpleDataSetInformationDTO;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;

/**
 * Imports individual data sets into the imaging db.
 * 
 * @author Chandrasekhar Ramakrishnan
 */
public abstract class AbstractFeatureVectorMigrator implements IMigrator
{
    protected final IImagingUploadDAO dao;

    protected final IEncapsulatedOpenBISService openBisService;

    protected final List<SimpleDataSetInformationDTO> knownDataSets;

    protected final HashMap<String, SimpleDataSetInformationDTO> knownDataSetsByCode;

    public AbstractFeatureVectorMigrator(Properties properties)
    {
        DataSource dataSource = ServiceProvider.getDataSourceProvider().getDataSource(properties);
        dao = QueryTool.getQuery(dataSource, IImagingUploadDAO.class);
        openBisService = ServiceProvider.getOpenBISService();
        knownDataSets = openBisService.listDataSets();
        knownDataSetsByCode =
                new HashMap<String, SimpleDataSetInformationDTO>(knownDataSets.size());

        initializeDataSetsByCode();
    }

    /**
     * Create a map of associations from code to data set info. Assumes knownDataSets and been
     * initialized.
     */
    private void initializeDataSetsByCode()
    {
        for (SimpleDataSetInformationDTO dataSetInfo : knownDataSets)
        {
            knownDataSetsByCode.put(dataSetInfo.getDataSetCode(), dataSetInfo);
        }
    }

    public String getDescription()
    {
        return "uploading feature vectors to the imaging database";
    }

    public boolean migrate(File dataset)
    {
        AbstractMigrationDecision decision = createMigrationDecision(dataset);
        decision.process();
        if (false == decision.shouldMigrate)
        {
            // Vacuously true
            return true;
        }

        ImgDatasetDTO imgDataset = dao.tryGetDatasetByPermId(decision.dataSetInfo.getDataSetCode());
        if (null != imgDataset)
        {
            // Has already been imported into the db
            return true;
        }

        AbstractImageDbImporter importer;
        importer =
                createImporter(createScreeningDatasetInfo(decision.getDataSetInfo()),
                        decision.fileToMigrate);

        importer.doImport();

        return importer.isSuccessful;
    }

    protected abstract AbstractMigrationDecision createMigrationDecision(File dataset);

    protected abstract AbstractImageDbImporter createImporter(
            ScreeningContainerDatasetInfo dataSetInfo, File fileToMigrate);

    private ScreeningContainerDatasetInfo createScreeningDatasetInfo(
            SimpleDataSetInformationDTO dataSetInfo)
    {
        Sample sample = findSampleCodeForDataSet(dataSetInfo);
        assert sample != null : "no sample connected to a dataset";

        Experiment experiment = sample.getExperiment();
        ScreeningContainerDatasetInfo info = new ScreeningContainerDatasetInfo();
        info.setExperimentPermId(experiment.getPermId());
        info.setContainerPermId(sample.getPermId());
        info.setDatasetPermId(dataSetInfo.getDataSetCode());

        return info;
    }

    private Sample findSampleCodeForDataSet(SimpleDataSetInformationDTO dataSetInfo)
    {
        String sampleCodeOrNull = dataSetInfo.getSampleCode();
        Sample sample = null;
        if (null == sampleCodeOrNull)
        {
            // check the parent data sets for a sample
            Collection<String> parentDataSetCodes = dataSetInfo.getParentDataSetCodes();
            for (String dataSetCode : parentDataSetCodes)
            {
                ExternalData externalData = openBisService.tryGetDataSetForServer(dataSetCode);
                if (externalData.getSample() != null)
                {
                    sample = externalData.getSample();
                    break;
                }
            }
        } else
        {
            SampleIdentifier sampleId =
                    new SampleIdentifier(new SpaceIdentifier(dataSetInfo.getDatabaseInstanceCode(),
                            dataSetInfo.getGroupCode()), dataSetInfo.getSampleCode());
            sample = openBisService.tryGetSampleWithExperiment(sampleId);
        }
        return sample;
    }

    public void close()
    {
        // close the dao
        dao.close();
    }

    /**
     * Helper class for figuring out what to do with files
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    protected abstract static class AbstractMigrationDecision
    {
        protected final File dataset;

        private final HashMap<String, SimpleDataSetInformationDTO> knownDataSetsByCode;

        private SimpleDataSetInformationDTO dataSetInfo;

        private boolean shouldMigrate = false;

        private File fileToMigrate = null;

        public AbstractMigrationDecision(File dataset,
                HashMap<String, SimpleDataSetInformationDTO> knownDataSetsByCode)
        {
            this.dataset = dataset;
            this.knownDataSetsByCode = knownDataSetsByCode;
        }

        protected void setDataSetInfo(SimpleDataSetInformationDTO dataSetInfo)
        {
            this.dataSetInfo = dataSetInfo;
        }

        public SimpleDataSetInformationDTO getDataSetInfo()
        {
            return dataSetInfo;
        }

        // Figure out what to do with this file
        public void process()
        {
            setDataSetInfo(tryDataSetInformation());
            if (null == getDataSetInfo())
            {
                shouldMigrate = false;
                return;
            }

            // Only import this data set if it is of an analysis type
            if (false == ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE.equals(getDataSetInfo()
                    .getDataSetType()))
            {
                shouldMigrate = false;
                return;
            }

            // Figure out which file we need to migrate
            fileToMigrate = tryFileToMigrate();

            if (null == fileToMigrate)
            {
                shouldMigrate = false;
                return;
            }

            shouldMigrate = true;
            return;
        }

        protected abstract File tryFileToMigrate();

        private SimpleDataSetInformationDTO tryDataSetInformation()
        {
            String dataSetCode = dataset.getName();
            SimpleDataSetInformationDTO dsInfo = knownDataSetsByCode.get(dataSetCode);
            return dsInfo;
        }
    }

    /**
     * Helper class for importing data into the image db.
     * 
     * @author Chandrasekhar Ramakrishnan
     */
    protected abstract static class AbstractImageDbImporter
    {
        protected final IImagingUploadDAO dao;

        protected final ScreeningContainerDatasetInfo screeningDataSetInfo;

        protected final File fileToMigrate;

        protected boolean isSuccessful = false;

        protected AbstractImageDbImporter(IImagingUploadDAO dao,
                ScreeningContainerDatasetInfo screeningDataSetInfo, File fileToMigrate)
        {
            this.dao = dao;
            this.screeningDataSetInfo = screeningDataSetInfo;
            this.fileToMigrate = fileToMigrate;
        }

        public abstract void doImport();
    }
}
