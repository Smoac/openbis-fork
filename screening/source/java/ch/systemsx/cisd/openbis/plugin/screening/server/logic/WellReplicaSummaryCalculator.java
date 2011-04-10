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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ch.rinn.restrictions.Private;
import ch.systemsx.cisd.common.collections.GroupByMap;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.IWellData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ReplicaSummaryAggregationType;

/**
 * Calculates summaries and ranks for each group and subgroup of well replicas. Usually a replica is
 * determined by the material in the well, e.g. gene or compound. A subgroup can be e.g. oligo or
 * compound concentration.
 * 
 * @author Tomasz Pylak
 */
public class WellReplicaSummaryCalculator
{
    public static List<MaterialFeatureVectorSummary> calculateReplicasFeatureVectorSummaries(
            List<IWellData> wellDataList, ReplicaSummaryAggregationType aggregationType)
    {
        validate(wellDataList);
        return new WellReplicaSummaryCalculator(wellDataList, aggregationType)
                .calculateReplicasFeatureVectorSummaries();
    }

    private static void validate(List<IWellData> wellDataList)
    {
        int numberOfFeatures = getNumberOfFeatures(wellDataList);
        for (IWellData wellData : wellDataList)
        {
            if (wellData.getFeatureVector() == null)
            {
                throw new IllegalStateException(
                        String.format("No feature vector found for material "
                                + wellData.getMaterial()));
            }
            if (wellData.getFeatureVector().length != numberOfFeatures)
            {
                throw new IllegalStateException(
                        String.format(
                                "Each well should have the same amount features, but some have %d and some have %d.",
                                numberOfFeatures, wellData.getFeatureVector().length));
            }
        }

    }

    private final GroupByMap<Long/* replica id */, IWellData> replicaToWellDataMap;

    private final ReplicaSummaryAggregationType aggregationType;

    private final int numberOfFeatures;

    private WellReplicaSummaryCalculator(List<IWellData> wellDataList,
            ReplicaSummaryAggregationType aggregationType)
    {
        this.numberOfFeatures = getNumberOfFeatures(wellDataList);
        this.aggregationType = aggregationType;
        this.replicaToWellDataMap =
                GroupByMap.create(wellDataList, new IKeyExtractor<Long, IWellData>()
                    {
                        public Long getKey(IWellData wellData)
                        {
                            return wellData.getReplicaId();
                        }
                    });
    }

    private List<MaterialFeatureVectorSummary> calculateReplicasFeatureVectorSummaries()
    {
        Map<Long/* replica id */, SummaryFeatureVector> replicaToSummaryMap =
                calculateSummaryFeatures();
        Map<Long/* replica id */, int[]/* ranks for each feature */> ranks =
                calculateRanks(replicaToSummaryMap, numberOfFeatures);

        List<MaterialFeatureVectorSummary> summaries =
                new ArrayList<MaterialFeatureVectorSummary>();
        Set<Long> replicaIds = replicaToWellDataMap.getKeys();
        for (Long replicaId : replicaIds)
        {
            List<IWellData> replicaWells = replicaToWellDataMap.getOrDie(replicaId);
            SummaryFeatureVector summaryFeatures = replicaToSummaryMap.get(replicaId);
            // array not empty, all materials are the same
            Material material = replicaWells.get(0).getMaterial();
            MaterialFeatureVectorSummary summary =
                    new MaterialFeatureVectorSummary(material, summaryFeatures.getAggregates(),
                            summaryFeatures.getDeviation(), ranks.get(replicaId));
            summaries.add(summary);
        }
        return summaries;
    }

    @Private
    static Map<Long/* replica id */, int[]/* ranks for each feature */> calculateRanks(
            Map<Long/* replica id */, SummaryFeatureVector> replicaToSummaryMap,
            int numberOfFeatures)
    {
        List<IWellData> summaryWellDataList = createSummaryWellData(replicaToSummaryMap);
        Map<Long, int[]> ranks = createEmptyRankingMap(replicaToSummaryMap, numberOfFeatures);
        for (int featureIx = 0; featureIx < numberOfFeatures; featureIx++)
        {
            sortBySelectedFeature(summaryWellDataList, featureIx);
            Float prevValue = null;
            int rank = 0;
            // browse sorted data and fix the rankings for a current feature
            for (int i = 0; i < summaryWellDataList.size(); i++)
            {
                IWellData rankWellData = summaryWellDataList.get(i);
                float value = getFeatureValue(rankWellData, featureIx);
                if (isDifferent(prevValue, value))
                {
                    rank = i + 1;
                }
                int[] replicaRanks = ranks.get(rankWellData.getReplicaId());
                replicaRanks[featureIx] = rank;
                prevValue = value;
            }
        }
        return ranks;
    }

    private static boolean isDifferent(Float prevValueOrNull, float value)
    {
        if (prevValueOrNull == null)
        {
            return true;
        }
        float prevValue = prevValueOrNull.floatValue();
        if (isNumerical(value) != isNumerical(prevValue))
        {
            return true;
        }
        if (isNumerical(value) == false)
        {
            // both are not numerical, we treat them in the same way
            return false;
        }
        return value != prevValue;
    }

    /** Wrap features summary by the {@link IWellData} interface. */
    private static List<IWellData> createSummaryWellData(
            Map<Long, SummaryFeatureVector> replicaToSummaryMap)
    {
        List<IWellData> summaryWellDataList = new ArrayList<IWellData>();
        for (Entry<Long, SummaryFeatureVector> entry : replicaToSummaryMap.entrySet())
        {
            Long replicaId = entry.getKey();
            float[] aggregates = entry.getValue().getAggregates();
            IWellData summaryWellData = new WellData(replicaId, null, aggregates, null);
            summaryWellDataList.add(summaryWellData);
        }
        return summaryWellDataList;
    }

    private static Map<Long, int[]> createEmptyRankingMap(
            Map<Long, SummaryFeatureVector> replicaToSummaryMap, int numberOfFeatures)
    {
        Map<Long, int[]> ranks = new HashMap<Long, int[]>();
        Set<Long> replicaIds = replicaToSummaryMap.keySet();
        for (Long replicaId : replicaIds)
        {
            ranks.put(replicaId, new int[numberOfFeatures]);
        }
        return ranks;
    }

    private Map<Long, SummaryFeatureVector> calculateSummaryFeatures()
    {
        Map<Long/* replica id */, SummaryFeatureVector> replicaToSummaryMap =
                new HashMap<Long, SummaryFeatureVector>();
        Set<Long> replicaIds = replicaToWellDataMap.getKeys();
        for (Long replicaId : replicaIds)
        {
            List<IWellData> replicaWells = replicaToWellDataMap.getOrDie(replicaId);
            SummaryFeatureVector summary = calculateSummaryFeatures(replicaWells);
            replicaToSummaryMap.put(replicaId, summary);
        }
        return replicaToSummaryMap;
    }

    @Private
    static class SummaryFeatureVector
    {
        private final float[] aggregate;

        private final float[] deviation;

        public SummaryFeatureVector(float[] aggregate, float[] deviation)
        {
            this.aggregate = aggregate;
            this.deviation = deviation;
        }

        public float[] getAggregates()
        {
            return aggregate;
        }

        public float[] getDeviation()
        {
            return deviation;
        }
    }

    private SummaryFeatureVector calculateSummaryFeatures(List<IWellData> replicaWells)
    {
        switch (aggregationType)
        {
            case MEDIAN:
                return calculateMedianVector(replicaWells, numberOfFeatures);
            default:
                throw new IllegalStateException("Unhandled aggregation type: " + aggregationType);
        }
    }

    private static SummaryFeatureVector calculateMedianVector(List<IWellData> replicaWells,
            int numberOfFeatures)
    {
        float[] medians = new float[numberOfFeatures];
        float[] deviations = new float[numberOfFeatures];
        for (int featureIx = 0; featureIx < numberOfFeatures; featureIx++)
        {
            medians[featureIx] = calculateMedian(replicaWells, featureIx);
            deviations[featureIx] =
                    calculateMedianAbsoluteDeviation(medians[featureIx], replicaWells, featureIx);
        }
        return new SummaryFeatureVector(medians, deviations);
    }

    private static void sortBySelectedFeature(List<IWellData> replicaWells, int featureIx)
    {
        Collections.sort(replicaWells, createSelectedFeatureDescendingComparator(featureIx));
    }

    private static int getNumberOfFeatures(List<IWellData> replicaWells)
    {
        if (replicaWells.size() == 0)
        {
            return 0;
        } else
        {
            return replicaWells.get(0).getFeatureVector().length;
        }
    }

    // NOTE: we calculate the median from the values which are neither NaN nor infinity
    @Private
    static float calculateMedian(List<IWellData> replicaWells, int featureIx)
    {
        sortBySelectedFeature(replicaWells, featureIx);
        int firstIx = 0;
        int lastIx = replicaWells.size() - 1;
        while (firstIx <= lastIx && isNumerical(replicaWells, firstIx, featureIx) == false)
        {
            firstIx++;
        }
        while (lastIx >= firstIx && isNumerical(replicaWells, lastIx, featureIx) == false)
        {
            lastIx--;
        }
        if (lastIx < firstIx)
        {
            return Float.NaN;
        }
        int medianIx = firstIx + ((lastIx - firstIx) / 2);
        return getFeatureValue(replicaWells.get(medianIx), featureIx);
    }

    private static boolean isNumerical(List<IWellData> replicaWells, int replicaIx, int featureIx)
    {
        return isNumerical(getFeatureValue(replicaWells.get(replicaIx), featureIx));
    }

    private static float getFeatureValue(IWellData wellData, int featureIx)
    {
        return wellData.getFeatureVector()[featureIx];
    }

    @Private
    static float calculateMedianAbsoluteDeviation(float median, List<IWellData> replicaWells,
            int featureIx)
    {
        if (isNumerical(median) == false)
        {
            return Float.NaN;
        }
        List<Float> absDeviations = new ArrayList<Float>();
        for (IWellData wellData : replicaWells)
        {
            float value = getFeatureValue(wellData, featureIx);
            if (isNumerical(value))
            {
                float absDev = Math.abs(value - median);
                absDeviations.add(absDev);
            }
        }
        if (absDeviations.isEmpty())
        {
            return Float.NaN;
        }
        Collections.sort(absDeviations);
        int medianIx = absDeviations.size() / 2;
        return absDeviations.get(medianIx);
    }

    private static boolean isNumerical(float value)
    {
        return Float.isInfinite(value) == false && Float.isNaN(value) == false;
    }

    private static Comparator<IWellData> createSelectedFeatureDescendingComparator(
            final int featureIx)
    {
        return new Comparator<IWellData>()
            {
                public int compare(IWellData w1, IWellData w2)
                {
                    float v1 = getFeatureValue(w1, featureIx);
                    float v2 = getFeatureValue(w2, featureIx);
                    if (isNumerical(v1) != isNumerical(v2))
                    {
                        // all non-numerical numbers are equal and should be at the end of the list
                        return isNumerical(v1) ? -1 : 1;
                    }
                    return -Float.compare(v1, v2);
                }
            };
    }
}