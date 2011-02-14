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
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.DataPE;
import ch.systemsx.cisd.openbis.generic.shared.dto.ExternalDataPE;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ImageDatasetParameters;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ScreeningConstants;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IImageDatasetLoader;

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
        Experiment experiment = dataset.getExperiment();
        return new DatasetReference(dataset.getId(), dataset.getCode(), dataTypeCode,
                dataset.getRegistrationDate(), fileTypeCode, dataStore.getCode(),
                dataStore.getHostUrl(), experiment.getPermId(), experiment.getIdentifier());
    }

    public static List<ExternalDataPE> filterImageAnalysisDatasets(List<ExternalDataPE> datasets)
    {
        return filterDatasetsByTypePattern(datasets,
                ScreeningConstants.HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN);
    }

    public static <T extends DataPE> List<T> filterImageOverlayDatasets(Collection<T> datasets)
    {
        return filterDatasetsByTypePattern(datasets,
                ScreeningConstants.HCS_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN,
                ScreeningConstants.MICROSCOPY_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN);
    }

    /** excludes overlay image data sets even when they match to the image dataset pattern */
    public static List<ExternalDataPE> filterImageDatasets(List<ExternalDataPE> datasets)
    {
        List<ExternalDataPE> allDatasets =
                filterDatasetsByTypePattern(datasets,
                        ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN,
                        ScreeningConstants.ANY_MICROSCOPY_IMAGE_DATASET_TYPE_PATTERN);

        allDatasets =
                excludeDatasetsByTypePattern(allDatasets,
                        ScreeningConstants.HCS_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN,
                        ScreeningConstants.MICROSCOPY_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN);
        return allDatasets;
    }

    /** chooses datasets of unknown types */
    public static List<ExternalDataPE> filterUnknownDatasets(List<ExternalDataPE> datasets)
    {
        return excludeDatasetsByTypePattern(datasets,
                ScreeningConstants.HCS_IMAGE_ANALYSIS_DATASET_TYPE_PATTERN,
                ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN,
                ScreeningConstants.ANY_MICROSCOPY_IMAGE_DATASET_TYPE_PATTERN);
    }

    private static <T extends DataPE> List<T> excludeDatasetsByTypePattern(List<T> datasets,
            String... datasetTypeCodePatterns)
    {
        return filterDatasetsByTypePattern(datasets, false, datasetTypeCodePatterns);
    }

    public static <T extends DataPE> List<T> filterDatasetsByTypePattern(Collection<T> datasets,
            String... datasetTypeCodePatterns)
    {
        return filterDatasetsByTypePattern(datasets, true, datasetTypeCodePatterns);
    }

    private static <T extends DataPE> List<T> filterDatasetsByTypePattern(Collection<T> datasets,
            boolean doesMatch, String... datasetTypeCodePatterns)
    {
        final List<T> chosenDatasets = new ArrayList<T>();
        for (T dataset : datasets)
        {
            if (isOneOfTypesMatching(dataset, datasetTypeCodePatterns) == doesMatch)
            {
                chosenDatasets.add(dataset);
            }
        }
        return chosenDatasets;
    }

    private static boolean isOneOfTypesMatching(DataPE dataset, String... datasetTypeCodePatterns)
    {
        for (String datasetTypeCodePattern : datasetTypeCodePatterns)
        {
            if (isTypeMatching(dataset, datasetTypeCodePattern))
            {
                return true;
            }
        }
        return false;
    }

    private static boolean isTypeMatching(DataPE dataset, String datasetTypeCodePattern)
    {
        return dataset.getDataSetType().getCode().matches(datasetTypeCodePattern);
    }

    /**
     * true if a dataset contains HCS images. Such a dataset can be a parent of a feature vector
     * dataset or overlay dataset.
     */
    public static boolean isHcsImageDataset(ExternalData externalData)
    {
        return isOneOfTypesMatching(externalData,
                ScreeningConstants.ANY_HCS_IMAGE_DATASET_TYPE_PATTERN);
    }

    public static boolean isRawHcsImageDataset(ExternalData externalData)
    {
        return isTypeMatching(externalData, ScreeningConstants.HCS_RAW_IMAGE_DATASET_TYPE_PATTERN)
                || ScreeningConstants.HCS_RAW_IMAGE_LEGACY_DATASET_TYPE.equals(externalData
                        .getDataSetType().getCode());
    }

    public static boolean isSegmentationHcsImageDataset(ExternalData externalData)
    {
        return isOneOfTypesMatching(externalData,
                ScreeningConstants.HCS_SEGMENTATION_IMAGE_DATASET_TYPE_PATTERN);
    }

    public static boolean isBasicHcsImageDataset(ExternalData externalData)
    {
        return isHcsImageDataset(externalData)
                && isSegmentationHcsImageDataset(externalData) == false;
    }

    public static List<ExternalData> filterExternalDataByTypePattern(
            Collection<ExternalData> datasets, String... datasetTypeCodePatterns)
    {
        List<ExternalData> chosenDatasets = new ArrayList<ExternalData>();
        for (ExternalData dataset : datasets)
        {
            if (isOneOfTypesMatching(dataset, datasetTypeCodePatterns))
            {
                chosenDatasets.add(dataset);
            }
        }
        return chosenDatasets;
    }

    public static boolean isTypeMatching(ExternalData dataset, String datasetTypeCodePattern)
    {
        return dataset.getDataSetType().getCode().matches(datasetTypeCodePattern);
    }

    private static boolean isOneOfTypesMatching(ExternalData dataset,
            String... datasetTypeCodePatterns)
    {
        for (String datasetTypeCodePattern : datasetTypeCodePatterns)
        {
            if (isTypeMatching(dataset, datasetTypeCodePattern))
            {
                return true;
            }
        }
        return false;
    }

    /** Loads dataset metadata from the imaging database */
    public static ImageDatasetParameters loadImageParameters(ExternalData dataset,
            IScreeningBusinessObjectFactory businessObjectFactory)
    {
        IImageDatasetLoader loader = createHCSDatasetLoader(dataset, businessObjectFactory);
        ImageDatasetParameters params = loader.getImageParameters();
        return params;
    }

    private static IImageDatasetLoader createHCSDatasetLoader(ExternalData dataSet,
            IScreeningBusinessObjectFactory businessObjectFactory)
    {
        String datastoreCode = dataSet.getDataStore().getCode();
        String datasetCode = dataSet.getCode();
        return businessObjectFactory.createImageDatasetLoader(datasetCode, datastoreCode);
    }

}
