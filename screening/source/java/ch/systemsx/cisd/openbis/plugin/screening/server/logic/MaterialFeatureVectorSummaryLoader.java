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
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.IWellExtendedData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialAllReplicasFeatureVectors;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialBiologicalReplicateFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialIdFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialTechnicalReplicateFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellDataCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialBiologicalReplicateFeatureSummary;
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

        MaterialIdFeatureVectorSummary generalSummary = backendResult.getGeneralSummary();
        float[] featureVectorDeviatons = generalSummary.getFeatureVectorDeviations();
        float[] featureVectorSummaries = generalSummary.getFeatureVectorSummary();
        int[] featureVectorRanks = generalSummary.getFeatureVectorRanks();

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
        List<IWellExtendedData> experimentWellsData = experimentWells.getWellDataList();
        MaterialIdFeatureVectorSummary materialGeneralSummary =
                tryCalculateMaterialSummary(materialId, experimentWellsData);
        if (materialGeneralSummary == null)
        {
            return null;
        }
        List<IWellExtendedData> materialWellsData =
                filterWellsByMaterial(experimentWellsData, materialId);
        ReplicateSequenceProvider replicaSequences =
                new ReplicateSequenceProvider(materialWellsData,
                        settings.getBiologicalReplicatePropertyTypeCodes());

        List<MaterialBiologicalReplicateFeatureVector> subgroups =
                createBiologicalReplicates(materialWellsData, replicaSequences);
        List<MaterialTechnicalReplicateFeatureVector> replicas =
                filterDirectTechnicalReplicas(materialWellsData, replicaSequences);

        return new MaterialAllReplicasFeatureVectors(experimentWells.getFeatureDescriptions(),
                materialGeneralSummary, subgroups, replicas);
    }

    private List<MaterialBiologicalReplicateFeatureVector> createBiologicalReplicates(
            List<IWellExtendedData> materialWells, ReplicateSequenceProvider replicaSequences)
    {
        GroupByMap<Integer, IWellExtendedData> biologicalReplicateMap =
                groupByBiologicalReplicate(materialWells, replicaSequences);

        List<MaterialBiologicalReplicateFeatureVector> subgroups =
                new ArrayList<MaterialBiologicalReplicateFeatureVector>();
        MaterialReplicaSummaryAggregationType aggregationType = settings.getAggregationType();
        for (Integer biologicalReplicateSeq : replicaSequences.getBiologicalReplicateSequences())
        {
            List<IWellExtendedData> technicalReplicateWells =
                    biologicalReplicateMap.getOrDie(biologicalReplicateSeq);
            MaterialBiologicalReplicateFeatureVector subgroup =
                    createBiologicalReplicate(technicalReplicateWells, replicaSequences,
                            aggregationType);
            subgroups.add(subgroup);
        }
        return subgroups;
    }

    private MaterialBiologicalReplicateFeatureVector createBiologicalReplicate(
            List<IWellExtendedData> technicalReplicateWells,
            ReplicateSequenceProvider replicaSequences,
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

    private String getSubgroupLabel(List<IWellExtendedData> subgroupWellDataList,
            ReplicateSequenceProvider replicaSequences)
    {
        assert subgroupWellDataList.size() > 0 : "empty subgroup";
        // all wells belong to the same subgroup, so it does not matter which one we take
        Sample well = subgroupWellDataList.get(0).getWell();

        String label = replicaSequences.tryGetBiologicalReplicateLabel(well);
        assert label != null : "no biological replicates!";
        return label;
    }

    private MaterialIdFeatureVectorSummary tryCalculateMaterialSummary(TechId materialId,
            List<? extends IWellData> experimentWellDataList)
    {
        List<MaterialIdFeatureVectorSummary> featureSummaries =
                WellReplicaSummaryCalculator.calculateReplicasFeatureVectorSummaries(
                        experimentWellDataList, settings.getAggregationType());
        return tryFindMaterialSummary(materialId, featureSummaries);
    }

    // chooses wells which have no information about which biological replicate they are
    private static List<MaterialTechnicalReplicateFeatureVector> filterDirectTechnicalReplicas(
            List<IWellExtendedData> materialWellDataList,
            final ReplicateSequenceProvider replicaSequences)
    {
        List<IWellExtendedData> directTechnicalReplicas =
                CollectionUtils.filter(materialWellDataList,
                        new ICollectionFilter<IWellExtendedData>()
                            {
                                public boolean isPresent(IWellExtendedData element)
                                {
                                    return replicaSequences.isBiologicalReplicate(element) == false;
                                }
                            });
        sortByTechnicalReplicateSequence(directTechnicalReplicas, replicaSequences);
        return createTechnicalReplicates(directTechnicalReplicas, replicaSequences);
    }

    private static List<MaterialTechnicalReplicateFeatureVector> createTechnicalReplicates(
            List<IWellExtendedData> wells, final ReplicateSequenceProvider replicaSequences)
    {
        List<MaterialTechnicalReplicateFeatureVector> replicas =
                new ArrayList<MaterialTechnicalReplicateFeatureVector>();
        for (IWellExtendedData wellData : wells)
        {
            int replicaSequenceNumber = replicaSequences.getTechnicalReplicateSequence(wellData);
            MaterialTechnicalReplicateFeatureVector featureVector =
                    new MaterialTechnicalReplicateFeatureVector(replicaSequenceNumber,
                            wellData.getFeatureVector());
            replicas.add(featureVector);
        }
        return replicas;
    }

    private static void sortByTechnicalReplicateSequence(
            List<IWellExtendedData> materialWellDataList,
            final ReplicateSequenceProvider replicaSequences)
    {
        Collections.sort(materialWellDataList, new Comparator<IWellExtendedData>()
            {
                public int compare(IWellExtendedData w1, IWellExtendedData w2)
                {
                    Integer replicaSequenceNumber1 =
                            replicaSequences.getTechnicalReplicateSequence(w1);
                    Integer replicaSequenceNumber2 =
                            replicaSequences.getTechnicalReplicateSequence(w2);
                    return replicaSequenceNumber1.compareTo(replicaSequenceNumber2);
                }
            });
    }

    private static List<IWellExtendedData> filterWellsByMaterial(
            List<IWellExtendedData> wellDataList, final TechId materialTechId)
    {
        final Long materialId = materialTechId.getId();
        return CollectionUtils.filter(wellDataList, new ICollectionFilter<IWellExtendedData>()
            {
                public boolean isPresent(IWellExtendedData element)
                {
                    return materialId.equals(element.getReplicaMaterialId());
                }
            });
    }

    /**
     * A subgroup can be e.g. oligo or compound concentration.
     */
    private GroupByMap<Integer, IWellExtendedData> groupByBiologicalReplicate(
            List<IWellExtendedData> materialWellDataList,
            final ReplicateSequenceProvider replicaSequences)
    {
        return GroupByMap.create(materialWellDataList,
                new IKeyExtractor<Integer, IWellExtendedData>()
                    {
                        public Integer getKey(IWellExtendedData wellData)
                        {
                            return replicaSequences.tryGetBiologicalReplicateSequence(wellData
                                    .getWell());
                        }
                    });
    }

    private static MaterialIdFeatureVectorSummary tryFindMaterialSummary(TechId materialId,
            List<MaterialIdFeatureVectorSummary> featureSummaries)
    {
        for (MaterialIdFeatureVectorSummary summary : featureSummaries)
        {
            if (summary.getMaterial().equals(materialId.getId()))
            {
                return summary;
            }
        }
        return null;
    }
}
