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

package ch.systemsx.cisd.openbis.dss.etl.bdsmigration;

import static ch.systemsx.cisd.openbis.dss.etl.bdsmigration.BDSMigrationMaintananceTask.DIR_SEP;
import static ch.systemsx.cisd.openbis.dss.etl.bdsmigration.BDSMigrationMaintananceTask.METADATA_DIR;

import java.io.File;

import ch.systemsx.cisd.common.filesystem.FileOperations;
import ch.systemsx.cisd.openbis.dss.etl.ScreeningContainerDatasetInfo;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.ServiceProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SpaceIdentifier;

/**
 * Extract the dataset metadata from BDS and by asking openBIS. Used to migrate BDS to imaging db.
 * 
 * @author Tomasz Pylak
 */
class ScreeningDatasetInfoExtractor
{
    public static ScreeningContainerDatasetInfo tryCreateInfo(File dataset)
    {
        Sample sample = tryGetSampleWithExperiment(dataset);
        if (sample == null)
        {
            return null;
        }
        try
        {
            return createInfo(dataset, sample);
        } catch (Exception ex)
        {
            ex.printStackTrace();
            BDSMigrationMaintananceTask.logError(dataset, "Unexpected exception: "
                    + ex.getMessage());
            return null;
        }
    }

    private static Sample tryGetSampleWithExperiment(File dataset)
    {
        IEncapsulatedOpenBISService openBISService = ServiceProvider.getOpenBISService();
        SampleIdentifier sampleIdentifier = createSampleIdentifier(dataset);
        Sample sample = openBISService.tryGetSampleWithExperiment(sampleIdentifier);
        if (sample == null)
        {
            BDSMigrationMaintananceTask.logError(dataset, "Sample '" + sampleIdentifier
                    + "' cannot be found in openBIS");
        }
        return sample;
    }

    private static SampleIdentifier createSampleIdentifier(File dataset)
    {
        File sampleDir =
                new File(dataset, BDSMigrationMaintananceTask.METADATA_DIR
                        + BDSMigrationMaintananceTask.DIR_SEP + "sample");
        String databaseInstanceCode = contentAsString(new File(sampleDir, "instance_code"));
        String spaceCode = contentAsString(new File(sampleDir, "space_code"));
        String sampleCode = contentAsString(new File(sampleDir, "code"));

        SpaceIdentifier spaceIdentifier = new SpaceIdentifier(databaseInstanceCode, spaceCode);
        return new SampleIdentifier(spaceIdentifier, sampleCode);
    }

    private static ScreeningContainerDatasetInfo createInfo(File dataset, Sample sample)
    {
        int rows = extractGeometryDim(dataset, "plate_geometry", "rows");
        int columns = extractGeometryDim(dataset, "plate_geometry", "columns");
        int tileRows = extractGeometryDim(dataset, "well_geometry", "rows");
        int tileColumns = extractGeometryDim(dataset, "well_geometry", "columns");

        ScreeningContainerDatasetInfo info = new ScreeningContainerDatasetInfo();
        info.setContainerRows(rows);
        info.setContainerColumns(columns);
        info.setTileRows(tileRows);
        info.setTileColumns(tileColumns);
        info.setDatasetPermId(extractDatasetPermId(dataset));
        info.setContainerPermId(sample.getPermId());
        info.setExperimentPermId(sample.getExperiment().getPermId());

        return info;
    }

    private static int extractGeometryDim(File dataset, String geometryName, String fieldName)
    {
        File parentDir =
                new File(dataset, METADATA_DIR + DIR_SEP + "parameters" + DIR_SEP + geometryName);
        return contentAsNumber(new File(parentDir, fieldName));
    }

    private static String extractDatasetPermId(File dataset)
    {
        File file =
                new File(dataset, BDSMigrationMaintananceTask.METADATA_DIR + DIR_SEP + "data_set"
                        + DIR_SEP + "code");
        return contentAsString(file);
    }

    private static int contentAsNumber(File file)
    {
        return Integer.parseInt(contentAsString(file));
    }

    private static String contentAsString(File file)
    {
        return FileOperations.getInstance().getContentAsString(file).trim();
    }
}