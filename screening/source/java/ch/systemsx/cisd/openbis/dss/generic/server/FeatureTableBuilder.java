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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import ch.systemsx.cisd.common.exceptions.UserFailureException;
import ch.systemsx.cisd.openbis.dss.generic.shared.IEncapsulatedOpenBISService;
import ch.systemsx.cisd.openbis.generic.shared.dto.identifier.SampleIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.FeatureVectorDatasetWellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.WellPosition;
import ch.systemsx.cisd.openbis.plugin.screening.shared.dto.PlateFeatureValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.IImagingQueryDAO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgContainerDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgDatasetDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureDefDTO;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.dataaccess.ImgFeatureValuesDTO;

/**
 * Builder for a table of feature vectors. After building a list of feature names and a list of
 * {@link FeatureTableRow}s are available. Feature vectores are retrieved from
 * {@link IImagingQueryDAO}.
 * 
 * @author Franz-Josef Elmer
 */
public class FeatureTableBuilder
{
    private static final class Bundle
    {
        private ImgDatasetDTO dataSet;

        private FeatureVectorDatasetWellReference reference;

        private Map<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>> featureDefToValuesMap;
    }

    private final IImagingQueryDAO dao;

    private final IEncapsulatedOpenBISService service;

    private final List<Bundle> bundles;

    private final Map<String, Integer> featureNameToIndexMap;

    private final Set<String> featureNames;
    
    private final boolean useAllFeatures;

    /**
     * Creates an instance for specified DAO and openBIS service.
     */
    public FeatureTableBuilder(IImagingQueryDAO dao, IEncapsulatedOpenBISService service)
    {
        this(Collections.<String> emptyList(), dao, service);
    }

    /**
     * Creates an instance for specified DAO and openBIS service but filters on specified features.
     * 
     * @param featureNames And empty list means no filtering.
     */
    public FeatureTableBuilder(List<String> featureNames, IImagingQueryDAO dao,
            IEncapsulatedOpenBISService service)
    {
        this.dao = dao;
        this.service = service;
        bundles = new ArrayList<Bundle>();
        featureNameToIndexMap = new LinkedHashMap<String, Integer>();
        this.featureNames = new LinkedHashSet<String>(featureNames);
        this.useAllFeatures = featureNames.isEmpty();
    }

    /**
     * Adds feature vectors for specified data set, marking the well position.
     */
    public Bundle addFeatureVectorsOfDataSet(FeatureVectorDatasetWellReference reference)
    {
        final Bundle bundle = addFeatureVectorsOfDataSet(reference.getDatasetCode());
        bundle.reference = reference;
        return bundle;
    }

    /**
     * Adds feature vectors for specified data set.
     */
    public Bundle addFeatureVectorsOfDataSet(String dataSetCode)
    {
        final ImgDatasetDTO dataSet = dao.tryGetDatasetByPermId(dataSetCode);
        if (dataSet == null)
        {
            throw new UserFailureException("Unkown data set " + dataSetCode);
        }
        final Bundle bundle = new Bundle();
        final List<ImgFeatureDefDTO> featureDefinitions =
                dao.listFeatureDefsByDataSetId(dataSet.getId());
        final Map<String, ImgFeatureDefDTO> featureNameToDefMap =
                new LinkedHashMap<String, ImgFeatureDefDTO>();
        for (ImgFeatureDefDTO def : featureDefinitions)
        {
            featureNameToDefMap.put(def.getName(), def);
        }
        bundle.dataSet = dataSet;
        bundle.featureDefToValuesMap = new HashMap<ImgFeatureDefDTO, List<ImgFeatureValuesDTO>>();
        bundles.add(bundle);
        if (useAllFeatures)
        {
            featureNames.addAll(featureNameToDefMap.keySet());
        }
        for (String featureName : featureNames)
        {
            final ImgFeatureDefDTO featureDefinition = featureNameToDefMap.get(featureName);
            if (featureDefinition != null)
            {
                if (featureNameToIndexMap.containsKey(featureName) == false)
                {
                    featureNameToIndexMap.put(featureName,
                            new Integer(featureNameToIndexMap.size()));
                }
                List<ImgFeatureValuesDTO> featureValueSets =
                        dao.getFeatureValues(featureDefinition);
                if (featureValueSets.isEmpty())
                {
                    throw new UserFailureException("At least one set of values for feature "
                            + featureName + " of data set " + dataSetCode + " expected.");
                }
                bundle.featureDefToValuesMap.put(featureDefinition, featureValueSets);
            }
        }
        return bundle;
    }

    /**
     * Returns all feature names found. If the feature name list in the constructor is not empty the
     * result will a subset of this list.
     */
    public List<String> getFeatureNames()
    {
        return new ArrayList<String>(featureNameToIndexMap.keySet());
    }

    /**
     * Returns all features per well coordinates.
     */
    public List<FeatureTableRow> createFeatureTableRows()
    {
        List<FeatureTableRow> rows = new ArrayList<FeatureTableRow>();
        for (Bundle bundle : bundles)
        {
            String dataSetCode = bundle.dataSet.getPermId();
            ImgContainerDTO container = dao.getContainerById(bundle.dataSet.getContainerId());
            SampleIdentifier identifier = service.tryToGetSampleIdentifier(container.getPermId());
            if (bundle.reference == null)
            {
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
            } else
            {
                final FeatureTableRow row =
                        createFeatureTableRow(bundle.featureDefToValuesMap, dataSetCode,
                                identifier, bundle.reference, bundle.reference.getWellPosition());
                rows.add(row);

            }
        }
        return rows;
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
        float[] valueArray = new float[featureNameToIndexMap.size()];
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
            Integer index = featureNameToIndexMap.get(featureDefinition.getName());
            assert index != null : "No index for feature " + featureDefinition.getName();
            valueArray[index] =
                    featureValues.getForWellLocation(wellPosition.getWellRow(), wellPosition
                            .getWellColumn());
        }
        row.setFeatureValues(valueArray);
        return row;
    }
}
