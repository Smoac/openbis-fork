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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.collections.CollectionUtils.ICollectionFilter;
import ch.systemsx.cisd.common.collections.GroupByMap;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.IWellData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialAllReplicasFeatureVectors;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialBiologicalReplicateFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialSimpleFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialTechnicalReplicateFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellDataCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialBiologicalReplicateFeatureSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaFeatureSummaryResult;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaSummaryAggregationType;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;

/**
 * For the specified material in the specified experiment loads feature vectors (details and
 * statistics).<br>
 * Considers only one dataset of a particular type per plate.
 * 
 * @author Tomasz Pylak
 */
public class MaterialFeatureVectorSummaryLoader extends ExperimentFeatureVectorSummaryLoader
{
    public static List<MaterialSimpleFeatureVectorSummary> loadMaterialFeatureVectorsFromAllAssays(
            Session session, IScreeningBusinessObjectFactory businessObjectFactory,
            IDAOFactory daoFactory, TechId materialId, MaterialSummarySettings settings)
    {
        // Probably the result DTO has to be converted (here?) to fit the GUI needs better.
        // Note that different experiments can have different set of features!
        return new MaterialFeatureVectorSummaryLoader(session, businessObjectFactory, daoFactory,
                settings).loadMaterialFeatureVectorsFromAllAssays(materialId);
    }

    /** Note that different experiments can have different set of features! */
    private List<MaterialSimpleFeatureVectorSummary> loadMaterialFeatureVectorsFromAllAssays(
            TechId materialId)
    {
        // TODO 2011-05-20, Tomasz Pylak: implement me!
        return null;
    }

    /**
     * For comments {@See MaterialFeatureVectorSummaryLoader}.
     */
    public static MaterialReplicaFeatureSummaryResult loadMaterialFeatureVectors(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId materialId, TechId experimentId, MaterialSummarySettings settings)
    {
        MaterialAllReplicasFeatureVectors resultOrNull =
                new MaterialFeatureVectorSummaryLoader(session, businessObjectFactory, daoFactory,
                        settings).tryLoadMaterialFeatureVectors(materialId, experimentId);

        List<MaterialReplicaFeatureSummary> replicaRows = convertToFeatureRows(resultOrNull);
        List<String> subgroupLabels = tryGetSubgroupLabels(resultOrNull);

        int materialsInExperiment =
                (resultOrNull != null ? resultOrNull.getGeneralSummary()
                        .getNumberOfMaterialsInExperiment() : 0);
        return new MaterialReplicaFeatureSummaryResult(subgroupLabels, replicaRows,
                materialsInExperiment);
    }

    private static List<MaterialReplicaFeatureSummary> convertToFeatureRows(
            MaterialAllReplicasFeatureVectors backendResult)
    {
        List<MaterialReplicaFeatureSummary> replicaRows =
                new ArrayList<MaterialReplicaFeatureSummary>();
        if (backendResult == null)
        {
            return replicaRows;
        }

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

            float[] defaultFeatureValues =
                    extractFeatureValues(i, backendResult.getDirectTechnicalReplicates());
            if (defaultFeatureValues != null)
            {
                MaterialBiologicalReplicateFeatureSummary defaultReplica =
                        new MaterialBiologicalReplicateFeatureSummary(defaultFeatureValues, 0,
                                MaterialReplicaSummaryAggregationType.MEDIAN);
                replicaRow.setDirectTechnicalReplicates(defaultReplica);
            }

            final List<MaterialBiologicalReplicateFeatureVector> backendSubgroups =
                    backendResult.getBiologicalReplicates();
            List<MaterialBiologicalReplicateFeatureSummary> subgroups =
                    new ArrayList<MaterialBiologicalReplicateFeatureSummary>();
            for (int tmp = 0; tmp < backendSubgroups.size(); tmp++)
            {
                MaterialBiologicalReplicateFeatureVector backendGroup = backendSubgroups.get(tmp);
                final float[] aggregatedSummaries = backendGroup.getAggregatedSummary();
                float[] featureValues =
                        extractFeatureValues(i, backendGroup.getTechnicalReplicatesValues());
                MaterialBiologicalReplicateFeatureSummary subgroup =
                        new MaterialBiologicalReplicateFeatureSummary(featureValues,
                                aggregatedSummaries[i], backendGroup.getSummaryAggregationType());
                subgroups.add(subgroup);
            }
            replicaRow.setBiologicalRelicates(subgroups);
        }
        return replicaRows;
    }

    private static List<String> tryGetSubgroupLabels(
            MaterialAllReplicasFeatureVectors backendResultOrNull)
    {
        List<String> subgroupLabels = new ArrayList<String>();
        if (backendResultOrNull == null)
        {
            return subgroupLabels;
        }
        for (MaterialBiologicalReplicateFeatureVector backendSubgroup : backendResultOrNull
                .getBiologicalReplicates())
        {
            subgroupLabels.add(backendSubgroup.getSubgroupLabel());
        }
        return subgroupLabels;
    }

    private static float[] extractFeatureValues(int i,
            List<MaterialTechnicalReplicateFeatureVector> replicas)
    {
        float[] result = new float[replicas.size()];
        for (int pos = 0; pos < result.length; pos++)
        {
            float[] featureVector = replicas.get(pos).getFeatueVector();
            result[pos] = featureVector[i];
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
        WellDataCollection experimentWells = tryLoadWellData(experimentId);
        if (experimentWells == null)
        {
            return null;
        }
        return tryLoadMaterialFeatureVectors(materialId, experimentWells);
    }

    @Private
    MaterialAllReplicasFeatureVectors tryLoadMaterialFeatureVectors(TechId materialId,
            WellDataCollection experimentWells)
    {
        MaterialFeatureVectorSummary materialGeneralSummary =
                tryCalculateMaterialSummary(materialId, experimentWells);
        if (materialGeneralSummary == null)
        {
            return null;
        }
        List<IWellData> materialWells =
                filterWellsByMaterial(experimentWells.getWellDataList(), materialId);
        ReplicateSequenceProvider replicaSequences =
                new ReplicateSequenceProvider(materialWells,
                        settings.getBiologicalReplicatePropertyTypeCodes());

        List<MaterialBiologicalReplicateFeatureVector> subgroups =
                createBiologicalReplicates(materialWells, replicaSequences);
        List<MaterialTechnicalReplicateFeatureVector> replicas =
                filterDirectTechnicalReplicas(materialWells, replicaSequences);

        return new MaterialAllReplicasFeatureVectors(experimentWells.getFeatureDescriptions(),
                materialGeneralSummary, subgroups, replicas);
    }

    private List<MaterialBiologicalReplicateFeatureVector> createBiologicalReplicates(
            List<IWellData> materialWells, ReplicateSequenceProvider replicaSequences)
    {
        GroupByMap<Integer, IWellData> biologicalReplicateMap =
                groupByBiologicalReplicate(materialWells, replicaSequences);

        List<MaterialBiologicalReplicateFeatureVector> subgroups =
                new ArrayList<MaterialBiologicalReplicateFeatureVector>();
        MaterialReplicaSummaryAggregationType aggregationType = settings.getAggregationType();
        for (Integer biologicalReplicateSeq : replicaSequences.getBiologicalReplicateSequences())
        {
            List<IWellData> technicalReplicateWells =
                    biologicalReplicateMap.getOrDie(biologicalReplicateSeq);
            MaterialBiologicalReplicateFeatureVector subgroup =
                    createBiologicalReplicate(technicalReplicateWells, replicaSequences,
                            aggregationType);
            subgroups.add(subgroup);
        }
        return subgroups;
    }

    private MaterialBiologicalReplicateFeatureVector createBiologicalReplicate(
            List<IWellData> technicalReplicateWells, ReplicateSequenceProvider replicaSequences,
            MaterialReplicaSummaryAggregationType aggregationType)
    {
        float[] aggregatedSummary =
                WellReplicaSummaryCalculator.calculateSummaryFeatureVector(technicalReplicateWells,
                        aggregationType);
        List<MaterialTechnicalReplicateFeatureVector> replicas =
                createTechnicalReplicates(technicalReplicateWells, replicaSequences);
        String subgroupLabel = getSubgroupLabel(technicalReplicateWells, replicaSequences);
        return new MaterialBiologicalReplicateFeatureVector(replicas, aggregatedSummary,
                aggregationType, subgroupLabel);
    }

    private String getSubgroupLabel(List<IWellData> subgroupWellDataList,
            ReplicateSequenceProvider replicaSequences)
    {
        assert subgroupWellDataList.size() > 0 : "empty subgroup";
        // all wells belong to the same subgroup, so it does not matter which one we take
        Sample well = subgroupWellDataList.get(0).getWell();

        String label = replicaSequences.tryGetBiologicalReplicateLabel(well);
        assert label != null : "no biological replicates!";
        return label;
    }

    private MaterialFeatureVectorSummary tryCalculateMaterialSummary(TechId materialId,
            WellDataCollection experimentWellDataList)
    {
        List<MaterialFeatureVectorSummary> featureSummaries =
                calculateReplicasFeatureVectorSummaries(experimentWellDataList);
        return tryFindMaterialSummary(materialId, featureSummaries);
    }

    // chooses wells which have no information about which biological replicate they are
    private static List<MaterialTechnicalReplicateFeatureVector> filterDirectTechnicalReplicas(
            List<IWellData> materialWellDataList, final ReplicateSequenceProvider replicaSequences)
    {
        List<IWellData> directTechnicalReplicas =
                CollectionUtils.filter(materialWellDataList, new ICollectionFilter<IWellData>()
                    {
                        public boolean isPresent(IWellData element)
                        {
                            return replicaSequences.isBiologicalReplicate(element) == false;
                        }
                    });
        sortByTechnicalReplicateSequence(directTechnicalReplicas, replicaSequences);
        return createTechnicalReplicates(directTechnicalReplicas, replicaSequences);
    }

    private static List<MaterialTechnicalReplicateFeatureVector> createTechnicalReplicates(
            List<IWellData> wells, final ReplicateSequenceProvider replicaSequences)
    {
        List<MaterialTechnicalReplicateFeatureVector> replicas =
                new ArrayList<MaterialTechnicalReplicateFeatureVector>();
        for (IWellData wellData : wells)
        {
            int replicaSequenceNumber = replicaSequences.getTechnicalReplicateSequence(wellData);
            MaterialTechnicalReplicateFeatureVector featureVector =
                    new MaterialTechnicalReplicateFeatureVector(replicaSequenceNumber,
                            wellData.getFeatureVector());
            replicas.add(featureVector);
        }
        return replicas;
    }

    private static void sortByTechnicalReplicateSequence(List<IWellData> materialWellDataList,
            final ReplicateSequenceProvider replicaSequences)
    {
        Collections.sort(materialWellDataList, new Comparator<IWellData>()
            {
                public int compare(IWellData w1, IWellData w2)
                {
                    Integer replicaSequenceNumber1 =
                            replicaSequences.getTechnicalReplicateSequence(w1);
                    Integer replicaSequenceNumber2 =
                            replicaSequences.getTechnicalReplicateSequence(w2);
                    return replicaSequenceNumber1.compareTo(replicaSequenceNumber2);
                }
            });
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

    /**
     * A subgroup can be e.g. oligo or compound concentration.
     */
    private GroupByMap<Integer, IWellData> groupByBiologicalReplicate(
            List<IWellData> materialWellDataList, final ReplicateSequenceProvider replicaSequences)
    {
        return GroupByMap.create(materialWellDataList, new IKeyExtractor<Integer, IWellData>()
            {
                public Integer getKey(IWellData wellData)
                {
                    return replicaSequences.tryGetBiologicalReplicateSequence(wellData.getWell());
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
}
