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

package ch.systemsx.cisd.openbis.plugin.screening.shared.imaging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.common.logging.LogCategory;
import ch.systemsx.cisd.common.logging.LogFactory;
import ch.systemsx.cisd.openbis.generic.shared.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.FeatureTableRow;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.WellFeatureVectorReference;
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
 * @author Tomasz Pylak
 */
public class FeatureVectorLoader
{
    private final static Logger operationLog = LogFactory.getLogger(LogCategory.OPERATION,
            FeatureVectorLoader.class);

    // stores all feature vectors of one dataset
    private static final class DatasetFeaturesBundle
    {
        private ImgDatasetDTO dataSet;

        private Map<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>> featureDefToValuesMap;
    }

    public static interface IMetadataProvider
    {
        /** fetches sample identifier from openBIS */
        SampleIdentifier tryGetSampleIdentifier(String samplePermId);
    }

    private final IImagingReadonlyQueryDAO dao;

    private final IMetadataProvider metadataProviderOrNull;

    private final List<DatasetFeaturesBundle> bundles;

    private final Map<CodeAndLabel, Integer> featureCodeLabelToIndexMap;

    private final Set<String> featureCodes;

    private final boolean useAllFeatures;

    /**
     * fetches specified features of specified wells
     * 
     * @param featureCodes empty list means no filtering.
     * @throws UserFailureException if one of the specified datasets contains no feature vectors or
     *             does not exist.
     */
    public static WellFeatureCollection<FeatureTableRow> fetchWellFeatures(
            List<FeatureVectorDatasetWellReference> references, List<String> featureCodes,
            IImagingReadonlyQueryDAO dao, IMetadataProvider metadataProviderOrNull)
    {
        FeatureVectorLoader builder =
                new FeatureVectorLoader(featureCodes, dao, metadataProviderOrNull);
        Set<String> datasetCodes = extractDatasetCodes(references);
        builder.addFeatureVectorsOfDataSetsOrDie(datasetCodes);
        List<FeatureTableRow> features = builder.createFeatureTableRows(references);
        return new WellFeatureCollection<FeatureTableRow>(features, builder.getCodesAndLabels());
    }

    /**
     * fetches specified features of all wells
     * 
     * @param featureCodes empty list means no filtering.
     * @throws UserFailureException if one of the specified datasets contains no feature vectors or
     *             does not exist.
     */
    public static WellFeatureCollection<FeatureTableRow> fetchDatasetFeatures(
            List<String> datasetCodes, List<String> featureCodes, IImagingReadonlyQueryDAO dao,
            IMetadataProvider metadataProviderOrNull)
    {
        FeatureVectorLoader builder =
                new FeatureVectorLoader(featureCodes, dao, metadataProviderOrNull);
        builder.addFeatureVectorsOfDataSetsOrDie(datasetCodes);
        List<FeatureTableRow> features = builder.createFeatureTableRows();
        return new WellFeatureCollection<FeatureTableRow>(features, builder.getCodesAndLabels());
    }

    /**
     * Fetches all features of specified wells. Uses basic data types. If a reference to a dataset
     * without any feature vectors is specified, it is silently ignored.
     */
    public static WellFeatureCollection<FeatureVectorValues> fetchWellFeatureValuesIfPossible(
            List<WellFeatureVectorReference> references, IImagingReadonlyQueryDAO dao)
    {
        FeatureVectorLoader builder = new FeatureVectorLoader(new ArrayList<String>(), dao, null);
        Set<String> datasetCodes = extractDatasetCodesFromSimpleReferences(references);
        builder.addFeatureVectorsOfDataSetsIfPossible(datasetCodes);
        List<FeatureVectorValues> features =
                builder.createFeatureVectorValuesIfPossible(references);
        return new WellFeatureCollection<FeatureVectorValues>(features, builder.getCodesAndLabels());
    }

    /** stores feature vectors for a set of wells */
    public static class WellFeatureCollection<T extends FeatureVectorValues>
    {
        private final List<T> features;

        private final List<CodeAndLabel> featureNames;

        public WellFeatureCollection(List<T> features, List<CodeAndLabel> featureNames)
        {
            this.features = features;
            this.featureNames = featureNames;
        }

        public List<T> getFeatures()
        {
            return features;
        }

        /** codes and labels of all features in this collection */
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

    private void addFeatureVectorsOfDataSetsOrDie(Collection<String> datasetCodes)
    {
        for (String datasetCode : datasetCodes)
        {
            addFeatureVectorsOfDataSetOrDie(datasetCode);
        }
    }

    private void addFeatureVectorsOfDataSetsIfPossible(Collection<String> datasetCodes)
    {
        for (String datasetCode : datasetCodes)
        {
            if (addFeatureVectorsOfDataSet(datasetCode) == false)
            {
                operationLog.warn("Dataset " + datasetCode
                        + " contains no feature vectors or does not exist.");
            }
        }
    }

    private static Set<String> extractDatasetCodesFromSimpleReferences(
            List<WellFeatureVectorReference> references)
    {
        Set<String> datasetCodes = new HashSet<String>();
        for (WellFeatureVectorReference ref : references)
        {
            datasetCodes.add(ref.getDatasetCode());
        }
        return datasetCodes;
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
     * @param metadataProviderOrNull if null info about plates will not be fetched from openBIS
     */
    FeatureVectorLoader(List<String> featureCodes, IImagingReadonlyQueryDAO dao,
            IMetadataProvider metadataProviderOrNull)
    {
        this.dao = dao;
        this.metadataProviderOrNull = metadataProviderOrNull;
        bundles = new ArrayList<DatasetFeaturesBundle>();
        featureCodeLabelToIndexMap = new LinkedHashMap<CodeAndLabel, Integer>();
        this.featureCodes = new LinkedHashSet<String>(featureCodes);
        this.useAllFeatures = featureCodes.isEmpty();
    }

    /**
     * Adds feature vectors for specified feature vector data set code.
     * 
     * @throws UserFailureException if dataset with the specified code contains no feature vectors
     *             or does not exist.
     */
    void addFeatureVectorsOfDataSetOrDie(String dataSetCode)
    {
        boolean added = addFeatureVectorsOfDataSet(dataSetCode);
        if (added == false)
        {
            throw new UserFailureException("Unkown data set " + dataSetCode);
        }
    }

    /**
     * Adds feature vectors for specified feature vector data set code.
     * 
     * @return false if dataset with the specified code contains no feature vectors or does not
     *         exist.<br>
     *         true if feature vectors have been added.
     */
    boolean addFeatureVectorsOfDataSet(String dataSetCode)
    {
        final ImgDatasetDTO dataSet = dao.tryGetDatasetByPermId(dataSetCode);
        if (dataSet == null)
        {
            return false;
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
        return true;
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
            ImgContainerDTO container = dao.getContainerById(bundle.dataSet.getContainerId());
            SampleIdentifier identifier = tryGetSampleIdentifier(container);
            for (int rowIndex = 1; rowIndex <= container.getNumberOfRows(); rowIndex++)
            {
                for (int colIndex = 1; colIndex <= container.getNumberOfColumns(); colIndex++)
                {
                    final FeatureTableRow row =
                            createFeatureTableRow(bundle, identifier, null, new WellPosition(
                                    rowIndex, colIndex));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    private SampleIdentifier tryGetSampleIdentifier(ImgContainerDTO container)
    {
        if (metadataProviderOrNull != null)
        {
            return metadataProviderOrNull.tryGetSampleIdentifier(container.getPermId());
        } else
        {
            return null;
        }
    }

    /**
     * Returns all features for the specified wells in previously loaded datasets. Operates on very
     * basic data types. Ignores the references for which no feature vectors exist.
     */
    private List<FeatureVectorValues> createFeatureVectorValuesIfPossible(
            List<WellFeatureVectorReference> references)
    {
        Map<String/* dataset code */, DatasetFeaturesBundle> bundleMap = createBundleMap(bundles);
        List<FeatureVectorValues> featureVectors = new ArrayList<FeatureVectorValues>();
        for (WellFeatureVectorReference reference : references)
        {
            String dataSetCode = reference.getDatasetCode();
            DatasetFeaturesBundle bundle = bundleMap.get(dataSetCode);
            if (bundle != null)
            {
                FeatureVectorValues featureVector =
                        createFeatureVector(bundle, reference.getWellPosition());
                featureVectors.add(featureVector);
            }
        }
        return featureVectors;
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
            DatasetFeaturesBundle bundle = getDatasetFeaturesBundleOrDie(bundleMap, dataSetCode);
            ImgContainerDTO container = dao.getContainerById(bundle.dataSet.getContainerId());
            SampleIdentifier identifier = tryGetSampleIdentifier(container);
            final FeatureTableRow row =
                    createFeatureTableRow(bundle, identifier, reference,
                            reference.getWellPosition());
            rows.add(row);
        }
        return rows;
    }

    private DatasetFeaturesBundle getDatasetFeaturesBundleOrDie(
            Map<String, DatasetFeaturesBundle> bundleMap, String dataSetCode)
    {
        DatasetFeaturesBundle bundle = bundleMap.get(dataSetCode);
        if (bundle == null)
        {
            throw new IllegalStateException("Dataset has not been loaded: " + dataSetCode);
        }
        return bundle;
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

    private FeatureTableRow createFeatureTableRow(DatasetFeaturesBundle bundle,
            SampleIdentifier identifierOrNull, FeatureVectorDatasetWellReference reference,
            WellPosition wellPosition)
    {
        FeatureVectorValues featureVector = createFeatureVector(bundle, wellPosition);
        FeatureTableRow row = new FeatureTableRow(featureVector);
        row.setPlateIdentifier(identifierOrNull);
        row.setReference(reference);
        return row;
    }

    private FeatureVectorValues createFeatureVector(DatasetFeaturesBundle bundle,
            WellPosition wellPosition)
    {
        String permId = bundle.dataSet.getPermId();
        float[] valueArray = createFeatureValueArray(bundle.featureDefToValuesMap, wellPosition);
        return new FeatureVectorValues(permId, wellPosition, valueArray);
    }

    private float[] createFeatureValueArray(
            Map<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>> featureDefToValuesMap,
            WellPosition wellPosition)
    {
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
        return valueArray;
    }

    private CodeAndLabel getCodeAndLabel(final ImgFeatureDefDTO featureDefinition)
    {
        return new CodeAndLabel(featureDefinition.getCode(), featureDefinition.getLabel());
    }

}
