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

package ch.systemsx.cisd.openbis.plugin.screening.server.logic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ch.systemsx.cisd.bds.hcs.Location;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateImageParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSDatasetLoader;

/**
 * Helper methods to operate on screening specific objects.
 * 
 * @author Tomasz Pylak
 */
public class ScreeningUtils
{
    public static WellLocation tryCreateLocationFromMatrixCoordinate(String wellCode)
    {
        Location loc = Location.tryCreateLocationFromTransposedMatrixCoordinate(wellCode);
        if (loc == null)
        {
            return null;
        } else
        {
            return new WellLocation(loc.getY(), loc.getX());
        }
    }

    public static DatasetReference createDatasetReference(ExternalData dataset)
    {
        DataStore dataStore = dataset.getDataStore();
        String dataTypeCode = dataset.getDataSetType().getCode();
        String fileTypeCode = dataset.getFileFormatType().getCode();
        return new DatasetReference(dataset.getId(), dataset.getCode(), dataTypeCode,
                dataset.getRegistrationDate(), fileTypeCode, dataStore.getCode(),
                dataStore.getHostUrl());
    }

    public static List<ExternalDataPE> filterImageAnalysisDatasets(List<ExternalDataPE> datasets)
    {
        return filterDatasetsByType(datasets, ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE);
    }

    public static List<ExternalDataPE> filterImageDatasets(List<ExternalDataPE> datasets)
    {
        return filterDatasetsByType(datasets, ScreeningConstants.IMAGE_DATASET_TYPE);
    }

    /** chooses datasets of unknown types */
    public static List<ExternalDataPE> filterUnknownDatasets(List<ExternalDataPE> datasets)
    {
        List<ExternalDataPE> chosenDatasets = new ArrayList<ExternalDataPE>();
        for (ExternalDataPE dataset : datasets)
        {
            if (isTypeEqual(dataset, ScreeningConstants.IMAGE_ANALYSIS_DATASET_TYPE) == false
                    && isTypeEqual(dataset, ScreeningConstants.IMAGE_DATASET_TYPE) == false)
            {
                chosenDatasets.add(dataset);
            }
        }
        return chosenDatasets;
    }

    public static List<ExternalDataPE> filterDatasetsByType(List<ExternalDataPE> datasets,
            String datasetTypeCode)
    {
        List<ExternalDataPE> chosenDatasets = new ArrayList<ExternalDataPE>();
        for (ExternalDataPE dataset : datasets)
        {
            if (isTypeEqual(dataset, datasetTypeCode))
            {
                chosenDatasets.add(dataset);
            }
        }
        return chosenDatasets;
    }

    private static boolean isTypeEqual(ExternalDataPE dataset, String datasetType)
    {
        return dataset.getDataSetType().getCode().equals(datasetType);
    }

    public static List<ExternalData> filterExternalDataByType(Collection<ExternalData> datasets,
            String... datasetTypeCodes)
    {
        List<ExternalData> chosenDatasets = new ArrayList<ExternalData>();
        for (ExternalData dataset : datasets)
        {
            if (isTypeEqual(dataset, datasetTypeCodes))
            {
                chosenDatasets.add(dataset);
            }
        }
        return chosenDatasets;
    }

    public static boolean isTypeEqual(ExternalData dataset, String... datasetTypeCodes)
    {
        for (String datasetTypeCode : datasetTypeCodes)
        {
            if (dataset.getDataSetType().getCode().equals(datasetTypeCode))
            {
                return true;
            }
        }
        return false;
    }

    /** Loads dataset metadata from the imaging database */
    public static PlateImageParameters loadImageParameters(ExternalData dataset,
            IScreeningBusinessObjectFactory businessObjectFactory)
    {
        IHCSDatasetLoader loader = createHCSDatasetLoader(dataset, businessObjectFactory);
        PlateImageParameters params = loader.getImageParameters();
        return params;
    }

    private static IHCSDatasetLoader createHCSDatasetLoader(ExternalData dataSet,
            IScreeningBusinessObjectFactory businessObjectFactory)
    {
        String datastoreCode = dataSet.getDataStore().getCode();
        String datasetCode = dataSet.getCode();
        return businessObjectFactory.createHCSDatasetLoader(datasetCode, datastoreCode);
    }

}
