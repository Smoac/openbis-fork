/*
 * Copyright 2011 ETH Zuerich, CISD
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
import java.util.Collections;
import java.util.List;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.collections.CollectionUtils.ICollectionFilter;
import ch.systemsx.cisd.common.collections.GroupByMap;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.IWellData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialAllReplicasFeatureVectors;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialReplicaSubgroupFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialSingleReplicaFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellDataCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummaryResult;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaSubgroupFeatureSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaSummaryAggregationType;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;

/**
 * Loads feature vectors (details and statistics) for the specified material in the specified
 * experiment.
 * 
 * @author Tomasz Pylak
 */
public class MaterialFeatureVectorSummaryLoader extends ExperimentFeatureVectorSummaryLoader
{
    /**
     * For comments {@See MaterialFeatureVectorSummaryLoader}.
     */
    public static MaterialReplicaFeatureSummaryResult loadMaterialFeatureVectors(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId materialId, TechId experimentId, MaterialSummarySettings settings)
    {
        MaterialAllReplicasFeatureVectors result =
                new MaterialFeatureVectorSummaryLoader(session, businessObjectFactory, daoFactory,
                        settings).tryLoadMaterialFeatureVectors(materialId, experimentId);
        if (result == null)
        {
            return createEmptyMaterialReplicaFeatureSummaryResult();
        }
        return convert(result);
    }

    private static MaterialReplicaFeatureSummaryResult createEmptyMaterialReplicaFeatureSummaryResult()
    {
        return new MaterialReplicaFeatureSummaryResult(new ArrayList<String>(),
                new ArrayList<MaterialReplicaFeatureSummary>());
    }

    private static MaterialReplicaFeatureSummaryResult convert(
            MaterialAllReplicasFeatureVectors backendResult)
    {
        List<String> subgroupLabels = new ArrayList<String>();
        final List<MaterialReplicaSubgroupFeatureVector> backendSubgroups =
                backendResult.getSubgroups();
        for (MaterialReplicaSubgroupFeatureVector backendSubgroup : backendSubgroups)
        {
            subgroupLabels.add(backendSubgroup.getSubgroupLabel());
        }

        List<MaterialReplicaFeatureSummary> replicaRows =
                new ArrayList<MaterialReplicaFeatureSummary>();
        float[] featureVectorDeviatons =
                backendResult.getGeneralSummary().getFeatureVectorDeviations();
        float[] featureVectorSummaries =
                backendResult.getGeneralSummary().getFeatureVectorSummary();
        int[] featureVectorRanks = backendResult.getGeneralSummary().getFeatureVectorRanks();

        final List<CodeAndLabel> featureDescriptions = backendResult.getFeatureDescriptions();

        int numFeatures = featureDescriptions.size();
        for (int i = 0; i < numFeatures; i++)
        {
            MaterialReplicaFeatureSummary replicaRow = new MaterialReplicaFeatureSummary();
            replicaRows.add(replicaRow);

            replicaRow.setFeatureVectorDeviation(featureVectorDeviatons[i]);
            replicaRow.setFeatureVectorSummary(featureVectorSummaries[i]);
            replicaRow.setFeatureVectorRank(featureVectorRanks[i]);
            replicaRow.setFeatureDescription(featureDescriptions.get(i));

            float[] defaultFeatureValues = extractFeatureValues(i, backendResult.getReplicas());
            if (defaultFeatureValues != null)
            {
                MaterialReplicaSubgroupFeatureSummary defaultReplica =
                        new MaterialReplicaSubgroupFeatureSummary(defaultFeatureValues, 0,
                                MaterialReplicaSummaryAggregationType.MEDIAN);
                replicaRow.setDefaultSubgroup(defaultReplica);
            }

            List<MaterialReplicaSubgroupFeatureSummary> subgroups =
                    new ArrayList<MaterialReplicaSubgroupFeatureSummary>();
            replicaRow.setReplicaSubgroups(subgroups);
            for (int tmp = 0; tmp < backendSubgroups.size(); tmp++)
            {
                MaterialReplicaSubgroupFeatureVector backendGroup = backendSubgroups.get(tmp);
                final float[] aggregatedSummaries = backendGroup.getAggregatedSummary();
                float[] featureValues =
                        extractFeatureValues(i, backendGroup.getSingleReplicaValues());
                MaterialReplicaSubgroupFeatureSummary subgroup =
                        new MaterialReplicaSubgroupFeatureSummary(featureValues,
                                aggregatedSummaries[i], backendGroup.getSummaryAggregationType());
                subgroups.add(subgroup);
            }
        }

        return new MaterialReplicaFeatureSummaryResult(subgroupLabels, replicaRows);

    }

    private static float[] extractFeatureValues(int i,
            List<MaterialSingleReplicaFeatureVector> replicas)
    {
        float[] result = new float[replicas.size()];
        for (int pos = 0; pos < result.length; pos++)
        {
            float[] aggregatedValues = replicas.get(pos).getFeatueVectorSummary();
            result[pos] = aggregatedValues[i];

        }
        return result;
    }

    // ----------------------

    @Private
    MaterialFeatureVectorSummaryLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            MaterialSummarySettings settings)
    {
        super(session, businessObjectFactory, daoFactory, settings);
    }

    private MaterialAllReplicasFeatureVectors tryLoadMaterialFeatureVectors(TechId materialId,
            TechId experimentId)
    {
        WellDataCollection wellDataCollection = tryLoadWellData(experimentId);
        if (wellDataCollection == null)
        {
            return null;
        }
        return tryLoadMaterialFeatureVectors(materialId, wellDataCollection);
    }

    @Private
    MaterialAllReplicasFeatureVectors tryLoadMaterialFeatureVectors(TechId materialId,
            WellDataCollection wellDataCollection)
    {
        MaterialFeatureVectorSummary materialGeneralSummary =
                tryCalculateMaterialSummary(materialId, wellDataCollection);
        if (materialGeneralSummary == null)
        {
            return null;
        }
        List<IWellData> materialWellDataList =
                filterWellsByMaterial(wellDataCollection.getWellDataList(), materialId);
        GroupByMap<Double, IWellData> subgroupMap = groupBySubgroup(materialWellDataList);

        List<MaterialReplicaSubgroupFeatureVector> subgroups = Collections.emptyList();
        List<MaterialSingleReplicaFeatureVector> replicas = Collections.emptyList();
        if (hasNoSubgroups(subgroupMap))
        {
            replicas = createReplicas(materialWellDataList);
        } else
        {
            subgroups = createSubgroups(subgroupMap);
        }
        return new MaterialAllReplicasFeatureVectors(wellDataCollection.getFeatureDescriptions(),
                materialGeneralSummary, subgroups, replicas);
    }

    private List<MaterialReplicaSubgroupFeatureVector> createSubgroups(
            GroupByMap<Double, IWellData> subgroupMap)
    {
        List<MaterialReplicaSubgroupFeatureVector> subgroups =
                new ArrayList<MaterialReplicaSubgroupFeatureVector>();
        MaterialReplicaSummaryAggregationType aggregationType = settings.getAggregationType();
        int subgroupSequenceNumber = 1;
        Collection<Double> sortedKeys = sortSubgroupKeys(subgroupMap.getKeys());
        for (Double subgroupKey : sortedKeys)
        {
            if (subgroupKey != null)
            {
                List<IWellData> subgroupWellDataList = subgroupMap.getOrDie(subgroupKey);
                MaterialReplicaSubgroupFeatureVector subgroup =
                        createSubgroup(subgroupWellDataList, subgroupSequenceNumber,
                                aggregationType);
                subgroups.add(subgroup);
                subgroupSequenceNumber++;
            }
        }
        return subgroups;
    }

    private static Collection<Double> sortSubgroupKeys(Set<Double> keys)
    {
        ArrayList<Double> sortedKeys = new ArrayList<Double>(keys);
        Collections.sort(sortedKeys);
        return sortedKeys;
    }

    private MaterialReplicaSubgroupFeatureVector createSubgroup(
            List<IWellData> subgroupWellDataList, int subgroupSequenceNumber,
            MaterialReplicaSummaryAggregationType aggregationType)
    {
        float[] aggregatedSummary =
                WellReplicaSummaryCalculator.calculateSummaryFeatureVector(subgroupWellDataList,
                        aggregationType);
        List<MaterialSingleReplicaFeatureVector> replicas = createReplicas(subgroupWellDataList);
        String subgroupLabel = getSubgroupLabel(subgroupWellDataList, subgroupSequenceNumber);
        return new MaterialReplicaSubgroupFeatureVector(replicas, aggregatedSummary,
                aggregationType, subgroupLabel);
    }

    private String getSubgroupLabel(List<IWellData> subgroupWellDataList, int subgroupSequenceNumber)
    {
        assert subgroupWellDataList.size() > 0 : "empty subgroup";
        Sample well = subgroupWellDataList.get(0).getWell();
        IEntityProperty subgroupProperty = tryFindSubgroupProperty(well);
        assert subgroupProperty != null : "cannot fnd the subgroup property";

        String propertyLabel = subgroupProperty.getPropertyType().getLabel();
        Material subgroupMaterial = subgroupProperty.getMaterial();
        if (subgroupMaterial != null)
        {
            return propertyLabel + " " + subgroupSequenceNumber;
        } else
        {
            return propertyLabel + " " + subgroupProperty.tryGetAsString();
        }
    }

    /**
     * A subgroup can be e.g. oligo or compound concentration.
     */
    private GroupByMap<Double, IWellData> groupBySubgroup(List<IWellData> materialWellDataList)
    {
        return GroupByMap.create(materialWellDataList, new IKeyExtractor<Double, IWellData>()
            {
                public Double getKey(IWellData wellData)
                {
                    return tryFindSubgroup(wellData.getWell());
                }
            });
    }

    private Double tryFindSubgroup(Sample well)
    {
        IEntityProperty subgroupProperty = tryFindSubgroupProperty(well);
        if (subgroupProperty == null)
        {
            return null;
        }
        return tryExtractSubgroupValue(subgroupProperty);
    }

    private IEntityProperty tryFindSubgroupProperty(Sample well)
    {
        List<String> subgroupPropertyTypeCodes = settings.getSubgroupPropertyTypeCodes();
        if (subgroupPropertyTypeCodes == null)
        {
            return null;
        }
        return tryFindProperty(well.getProperties(), subgroupPropertyTypeCodes);
    }

    private MaterialFeatureVectorSummary tryCalculateMaterialSummary(TechId materialId,
            WellDataCollection wellDataCollection)
    {
        List<MaterialFeatureVectorSummary> featureSummaries =
                calculateReplicasFeatureVectorSummaries(wellDataCollection);
        return tryFindMaterialSummary(materialId, featureSummaries);
    }

    private static List<MaterialSingleReplicaFeatureVector> createReplicas(
            List<IWellData> materialWellDataList)
    {
        List<MaterialSingleReplicaFeatureVector> replicas =
                new ArrayList<MaterialSingleReplicaFeatureVector>();
        int replicaSequenceNumber = 1;
        for (IWellData wellData : materialWellDataList)
        {
            MaterialSingleReplicaFeatureVector featureVector =
                    new MaterialSingleReplicaFeatureVector(replicaSequenceNumber++,
                            wellData.getFeatureVector());
            replicas.add(featureVector);
        }
        return replicas;
    }

    private static boolean hasNoSubgroups(GroupByMap<Double, IWellData> subgroupMap)
    {
        return subgroupMap.getKeys().size() == 1 && subgroupMap.getKeys().contains(null);
    }

    private static List<IWellData> filterWellsByMaterial(List<IWellData> wellDataList,
            final TechId materialId)
    {
        return CollectionUtils.filter(wellDataList, new ICollectionFilter<IWellData>()
            {
                public boolean isPresent(IWellData element)
                {
                    return element.getMaterial().getId().equals(materialId.getId());
                }
            });
    }

    private static MaterialFeatureVectorSummary tryFindMaterialSummary(TechId materialId,
            List<MaterialFeatureVectorSummary> featureSummaries)
    {
        for (MaterialFeatureVectorSummary summary : featureSummaries)
        {
            if (summary.getMaterial().getId().equals(materialId.getId()))
            {
                return summary;
            }
        }
        return null;
    }

    private static Double tryExtractSubgroupValue(IEntityProperty subgroupProperty)
    {
        Material subgroupMaterial = subgroupProperty.getMaterial();
        if (subgroupMaterial != null)
        {
            return new Double(subgroupMaterial.getId());
        }
        try
        {
            return new Double(subgroupProperty.tryGetAsString());
        } catch (NumberFormatException ex)
        {
            return null;
        }
    }

    private static IEntityProperty tryFindProperty(List<IEntityProperty> properties,
            List<String> subgroupPropertyTypeCodes)
    {
        for (IEntityProperty property : properties)
        {
            if (subgroupPropertyTypeCodes.contains(property.getPropertyType().getCode()))
            {
                return property;
            }
        }
        return null;
    }
}
