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

package ch.systemsx.cisd.openbis.dss.generic.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.dss.generic.shared.utils.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingReadonlyQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;

/**
 * Builder for a table of feature vectors. After building a list of feature codes and a list of
 * {@link FeatureTableRow}s are available. Feature vectors are retrieved from
 * {@link IImagingReadonlyQueryDAO}.
 * 
 * @author Franz-Josef Elmer
 */
public class FeatureTableBuilder
{
    // stores all feature vectors of one dataset
    private static final class DatasetFeaturesBundle
    {
        private ImgDatasetDTO dataSet;

        private Map<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>> featureDefToValuesMap;
    }

    private final IImagingReadonlyQueryDAO dao;

    private final IEncapsulatedOpenBISService service;

    private final List<DatasetFeaturesBundle> bundles;

    private final Map<CodeAndLabel, Integer> featureCodeLabelToIndexMap;

    private final Set<String> featureCodes;

    private final boolean useAllFeatures;

    /** fetches all features of specified wells */
    public static WellFeatureCollection fetchWellFeatures(
            List<FeatureVectorDatasetWellReference> references, IImagingReadonlyQueryDAO dao,
            IEncapsulatedOpenBISService service)
    {
        return fetchWellFeatures(references, new ArrayList<String>(), dao, service);
    }

    /**
     * fetches specified features of specified wells
     * 
     * @param featureCodes empty list means no filtering.
     */
    public static WellFeatureCollection fetchWellFeatures(
            List<FeatureVectorDatasetWellReference> references, List<String> featureCodes,
            IImagingReadonlyQueryDAO dao, IEncapsulatedOpenBISService service)
    {
        FeatureTableBuilder builder = new FeatureTableBuilder(featureCodes, dao, service);
        Set<String> datasetCodes = extractDatasetCodes(references);
        for (String datasetCode : datasetCodes)
        {
            builder.addFeatureVectorsOfDataSet(datasetCode);
        }
        List<FeatureTableRow> features = builder.createFeatureTableRows(references);
        return new WellFeatureCollection(features, builder.getCodesAndLabels());
    }

    /**
     * fetches specified features of all wells
     * 
     * @param featureCodes empty list means no filtering.
     */
    public static WellFeatureCollection fetchDatasetFeatures(List<String> datasetCodes,
            List<String> featureCodes, IImagingReadonlyQueryDAO dao,
            IEncapsulatedOpenBISService service)
    {
        FeatureTableBuilder builder = new FeatureTableBuilder(featureCodes, dao, service);
        for (String datasetCode : datasetCodes)
        {
            builder.addFeatureVectorsOfDataSet(datasetCode);
        }
        List<FeatureTableRow> features = builder.createFeatureTableRows();
        return new WellFeatureCollection(features, builder.getCodesAndLabels());
    }

    public static class WellFeatureCollection
    {
        private final List<FeatureTableRow> features;

        private final List<CodeAndLabel> featureNames;

        public WellFeatureCollection(List<FeatureTableRow> features, List<CodeAndLabel> featureNames)
        {
            this.features = features;
            this.featureNames = featureNames;
        }

        public List<FeatureTableRow> getFeatures()
        {
            return features;
        }

        public List<CodeAndLabel> getFeatureCodesAndLabels()
        {
            return featureNames;
        }

        public List<String> getFeatureCodes()
        {
            List<String> codes = new ArrayList<String>();
            for (CodeAndLabel codeAndTitle : featureNames)
            {
                codes.add(codeAndTitle.getCode());
            }
            return codes;
        }

        public List<String> getFeatureLabels()
        {
            List<String> labels = new ArrayList<String>();
            for (CodeAndLabel codeAndTitle : featureNames)
            {
                labels.add(codeAndTitle.getLabel());
            }
            return labels;
        }
    }

    private static Set<String> extractDatasetCodes(
            List<FeatureVectorDatasetWellReference> references)
    {
        Set<String> datasetCodes = new HashSet<String>();
        for (FeatureVectorDatasetWellReference ref : references)
        {
            datasetCodes.add(ref.getDatasetCode());
        }
        return datasetCodes;
    }

    /**
     * Creates an instance for specified DAO and openBIS service but filters on specified features.
     * 
     * @param featureCodes empty list means no filtering.
     */
    FeatureTableBuilder(List<String> featureCodes, IImagingReadonlyQueryDAO dao,
            IEncapsulatedOpenBISService service)
    {
        this.dao = dao;
        this.service = service;
        bundles = new ArrayList<DatasetFeaturesBundle>();
        featureCodeLabelToIndexMap = new LinkedHashMap<CodeAndLabel, Integer>();
        this.featureCodes = new LinkedHashSet<String>(featureCodes);
        this.useAllFeatures = featureCodes.isEmpty();
    }

    /**
     * Adds feature vectors for specified data set.
     */
    DatasetFeaturesBundle addFeatureVectorsOfDataSet(String dataSetCode)
    {
        final ImgDatasetDTO dataSet = dao.tryGetDatasetByPermId(dataSetCode);
        if (dataSet == null)
        {
            throw new UserFailureException("Unkown data set " + dataSetCode);
        }
        final DatasetFeaturesBundle bundle = new DatasetFeaturesBundle();
        final Map<String, ImgFeatureDefDTO> featureCodeToDefMap =
                createFeatureCodeToDefMap(dao, dataSet);
        bundle.dataSet = dataSet;
        bundle.featureDefToValuesMap = new HashMap<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>>();
        bundles.add(bundle);
        if (useAllFeatures)
        {
            featureCodes.addAll(featureCodeToDefMap.keySet());
        }
        for (String featureCode : featureCodes)
        {
            final ImgFeatureDefDTO featureDefinition = featureCodeToDefMap.get(featureCode);
            if (featureDefinition != null)
            {
                CodeAndLabel codeAndLabel = getCodeAndLabel(featureDefinition);
                if (featureCodeLabelToIndexMap.containsKey(codeAndLabel) == false)
                {
                    featureCodeLabelToIndexMap.put(codeAndLabel, new Integer(
                            featureCodeLabelToIndexMap.size()));
                }
                List<ImgFeatureValuesDTO> featureValueSets =
                        dao.getFeatureValues(featureDefinition);
                if (featureValueSets.isEmpty())
                {
                    throw new UserFailureException("At least one set of values for feature "
                            + featureCode + " of data set " + dataSetCode + " expected.");
                }
                bundle.featureDefToValuesMap.put(featureDefinition, featureValueSets);
            }
        }
        return bundle;
    }

    private static Map<String, ImgFeatureDefDTO> createFeatureCodeToDefMap(
            IImagingReadonlyQueryDAO dao, final ImgDatasetDTO dataSet)
    {
        final List<ImgFeatureDefDTO> featureDefinitions =
                dao.listFeatureDefsByDataSetId(dataSet.getId());
        final Map<String, ImgFeatureDefDTO> featureCodeToDefMap =
                new LinkedHashMap<String, ImgFeatureDefDTO>();
        for (ImgFeatureDefDTO def : featureDefinitions)
        {
            featureCodeToDefMap.put(def.getCode(), def);
        }
        return featureCodeToDefMap;
    }

    /**
     * Returns all feature codes/labels found. If the feature code list in the constructor is not
     * empty the result will a list where the codes are a subset of this list.
     */
    List<CodeAndLabel> getCodesAndLabels()
    {
        return new ArrayList<CodeAndLabel>(featureCodeLabelToIndexMap.keySet());
    }

    /**
     * Returns all features for previously loaded datasets. Features for all plate wells are
     * returned.
     */
    List<FeatureTableRow> createFeatureTableRows()
    {
        List<FeatureTableRow> rows = new ArrayList<FeatureTableRow>();
        for (DatasetFeaturesBundle bundle : bundles)
        {
            String dataSetCode = bundle.dataSet.getPermId();
            ImgContainerDTO container = dao.getContainerById(bundle.dataSet.getContainerId());
            SampleIdentifier identifier = service.tryToGetSampleIdentifier(container.getPermId());
            for (int rowIndex = 1; rowIndex <= container.getNumberOfRows(); rowIndex++)
            {
                for (int colIndex = 1; colIndex <= container.getNumberOfColumns(); colIndex++)
                {
                    final FeatureTableRow row =
                            createFeatureTableRow(bundle.featureDefToValuesMap, dataSetCode,
                                    identifier, null, new WellPosition(rowIndex, colIndex));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    /**
     * Returns all features for the specified wells in previously loaded datasets.
     */
    private List<FeatureTableRow> createFeatureTableRows(
            List<FeatureVectorDatasetWellReference> references)
    {
        Map<String/* dataset code */, DatasetFeaturesBundle> bundleMap = createBundleMap(bundles);
        List<FeatureTableRow> rows = new ArrayList<FeatureTableRow>();
        for (FeatureVectorDatasetWellReference reference : references)
        {
            String dataSetCode = reference.getDatasetCode();
            DatasetFeaturesBundle bundle = bundleMap.get(dataSetCode);
            if (bundle == null)
            {
                throw new IllegalStateException("Dataset has not been loaded: " + dataSetCode);
            }
            ImgContainerDTO container = dao.getContainerById(bundle.dataSet.getContainerId());
            SampleIdentifier identifier = service.tryToGetSampleIdentifier(container.getPermId());
            final FeatureTableRow row =
                    createFeatureTableRow(bundle.featureDefToValuesMap, dataSetCode, identifier,
                            reference, reference.getWellPosition());
            rows.add(row);
        }
        return rows;
    }

    private static HashMap<String, DatasetFeaturesBundle> createBundleMap(
            List<DatasetFeaturesBundle> bundles)
    {
        HashMap<String, DatasetFeaturesBundle> map = new HashMap<String, DatasetFeaturesBundle>();
        for (DatasetFeaturesBundle bundle : bundles)
        {
            map.put(bundle.dataSet.getPermId(), bundle);
        }
        return map;
    }

    private FeatureTableRow createFeatureTableRow(
            Map<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>> featureDefToValuesMap,
            String dataSetCode, SampleIdentifier identifier,
            FeatureVectorDatasetWellReference reference, WellPosition wellPosition)
    {
        FeatureTableRow row = new FeatureTableRow();
        row.setDataSetCode(dataSetCode);
        row.setPlateIdentifier(identifier);
        row.setReference(reference);
        row.setWellPosition(wellPosition);
        float[] valueArray = new float[featureCodeLabelToIndexMap.size()];
        Arrays.fill(valueArray, Float.NaN);
        for (Entry<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>> entry : featureDefToValuesMap
                .entrySet())
        {
            ImgFeatureDefDTO featureDefinition = entry.getKey();
            List<ImgFeatureValuesDTO> featureValueSets = entry.getValue();
            // We take only the first set of feature value sets
            ImgFeatureValuesDTO featureValueDTO = featureValueSets.get(0);
            PlateFeatureValues featureValues = featureValueDTO.getValues();
            if (wellPosition.getWellRow() > featureValues.getGeometry().getNumberOfRows()
                    || wellPosition.getWellColumn() > featureValues.getGeometry()
                            .getNumberOfColumns())
            {
                break;
            }
            Integer index = featureCodeLabelToIndexMap.get(getCodeAndLabel(featureDefinition));
            assert index != null : "No index for feature " + featureDefinition.getCode();
            valueArray[index] =
                    featureValues.getForWellLocation(wellPosition.getWellRow(),
                            wellPosition.getWellColumn());
        }
        row.setFeatureValues(valueArray);
        return row;
    }

    private CodeAndLabel getCodeAndLabel(final ImgFeatureDefDTO featureDefinition)
    {
        return new CodeAndLabel(featureDefinition.getCode(), featureDefinition.getLabel());
    }

}
