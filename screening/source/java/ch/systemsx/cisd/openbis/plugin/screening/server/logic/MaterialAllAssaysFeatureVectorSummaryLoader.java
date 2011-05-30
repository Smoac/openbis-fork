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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.collections.CollectionUtils.ICollectionMappingFunction;
import ch.systemsx.cisd.common.collections.GroupByMap;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.IWellData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentSetCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSimpleFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellContent;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;

/**
 * Finds all experiments where the given material is present and calculates summaries for feature
 * vectors (if analysis data are available) .
 * 
 * @author Tomasz Pylak
 */
public class MaterialAllAssaysFeatureVectorSummaryLoader extends AbstractContentLoader
{
    public static List<MaterialSimpleFeatureVectorSummary> loadMaterialFeatureVectorsFromAllAssays(
            Session session, IScreeningBusinessObjectFactory businessObjectFactory,
            IDAOFactory daoFactory, TechId materialId, ExperimentSetCriteria experiments,
            MaterialSummarySettings settings)
    {
        // FIXME 2011-05-30, Tomasz Pylak: implement restriction to a set of experiments
        return loadMaterialFeatureVectorsFromAllAssays(session, businessObjectFactory, daoFactory,
                materialId, settings);
    }

    public static List<MaterialSimpleFeatureVectorSummary> loadMaterialFeatureVectorsFromAllAssays(
            Session session, IScreeningBusinessObjectFactory businessObjectFactory,
            IDAOFactory daoFactory, TechId materialId, MaterialSummarySettings settings)
    {
        List<WellContent> allAssayWellsForMaterial =
                WellContentLoader.loadOnlyMetadata(session, businessObjectFactory, daoFactory,
                        materialId);
        return new MaterialAllAssaysFeatureVectorSummaryLoader(session, businessObjectFactory,
                daoFactory, settings).loadMaterialFeatureVectorsFromAllAssays(materialId,
                allAssayWellsForMaterial);
    }

    private final MaterialSummarySettings settings;

    private MaterialAllAssaysFeatureVectorSummaryLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            MaterialSummarySettings settings)
    {
        super(session, businessObjectFactory, daoFactory);
        this.settings = settings;
    }

    /**
     * Note that different experiments can have different set of features!
     */
    private List<MaterialSimpleFeatureVectorSummary> loadMaterialFeatureVectorsFromAllAssays(
            TechId materialId, List<WellContent> allAssayWellsForMaterial)
    {
        Set<PlateIdentifier> plates = extractPlates(allAssayWellsForMaterial);
        WellFeatureCollection<FeatureVectorValues> allWellFeaturesOrNull =
                tryLoadWellSingleFeatureVectors(plates);

        Map<ExperimentReference, Set<WellReference>> wellsForExperimentMap =
                groupWellsByExperiment(allAssayWellsForMaterial);

        List<MaterialSimpleFeatureVectorSummary> summaries =
                new ArrayList<MaterialSimpleFeatureVectorSummary>();
        for (ExperimentReference experiment : wellsForExperimentMap.keySet())
        {
            Set<WellReference> experimentWells = wellsForExperimentMap.get(experiment);

            MaterialSimpleFeatureVectorSummary summary =
                    calculateExperimentFeatureVectorSummary(materialId, experiment,
                            experimentWells, allWellFeaturesOrNull);
            summaries.add(summary);
        }
        return summaries;
    }

    private MaterialSimpleFeatureVectorSummary calculateExperimentFeatureVectorSummary(
            TechId materialId, ExperimentReference experiment, Set<WellReference> experimentWells,
            WellFeatureCollection<FeatureVectorValues> allWellFeaturesOrNull)
    {
        if (allWellFeaturesOrNull == null)
        {
            return new MaterialSimpleFeatureVectorSummary(experiment);
        }

        List<IWellData> experimentWellData =
                selectExperimentWellData(experimentWells, allWellFeaturesOrNull, materialId);
        float[] summaryFeatureVector =
                WellReplicaSummaryCalculator.calculateSummaryFeatureVector(experimentWellData,
                        settings.getAggregationType());
        return new MaterialSimpleFeatureVectorSummary(experiment,
                allWellFeaturesOrNull.getFeatureCodesAndLabels(), summaryFeatureVector);
    }

    private static List<IWellData> selectExperimentWellData(Set<WellReference> experimentWells,
            WellFeatureCollection<FeatureVectorValues> allWellFeatures, TechId materialId)
    {
        List<String> orderedFeatureLabels = allWellFeatures.getFeatureLabels();

        List<IWellData> experimentWellDataList = new ArrayList<IWellData>();
        List<FeatureVectorValues> features = allWellFeatures.getFeatures();
        for (FeatureVectorValues feature : features)
        {
            if (experimentWells.contains(feature.getWellReference()))
            {
                float[] values =
                        WellFeatureCollectionLoader.asFeatureVectorValues(feature,
                                orderedFeatureLabels);
                IWellData wellData = new WellData(materialId.getId(), values);
                experimentWellDataList.add(wellData);
            }
        }
        return experimentWellDataList;
    }

    private static Map<ExperimentReference, Set<WellReference>> groupWellsByExperiment(
            List<WellContent> allAssayWellsForMaterial)
    {
        GroupByMap<ExperimentReference, WellContent> expToWellContentMap =
                GroupByMap.create(allAssayWellsForMaterial,
                        new IKeyExtractor<ExperimentReference, WellContent>()
                            {
                                public ExperimentReference getKey(WellContent wellContent)
                                {
                                    return wellContent.getExperiment();
                                }
                            });
        return convertToWellReferences(expToWellContentMap);
    }

    private static Map<ExperimentReference, Set<WellReference>> convertToWellReferences(
            GroupByMap<ExperimentReference, WellContent> expToWellContentMap)
    {
        Map<ExperimentReference, Set<WellReference>> map =
                new HashMap<ExperimentReference, Set<WellReference>>();
        for (ExperimentReference exp : expToWellContentMap.getKeys())
        {
            List<WellContent> wellContents = expToWellContentMap.getOrDie(exp);
            map.put(exp, asWellReferences(wellContents));
        }
        return map;
    }

    private static Set<WellReference> asWellReferences(List<WellContent> wellContents)
    {
        List<WellReference> wells =
                CollectionUtils.map(wellContents,
                        new ICollectionMappingFunction<WellReference, WellContent>()
                            {
                                public WellReference map(WellContent wellContent)
                                {
                                    return new WellReference(wellContent.tryGetLocation(),
                                            wellContent.getPlate().getPermId());
                                }
                            });
        return new HashSet<WellReference>(wells);
    }

    private static Set<PlateIdentifier> extractPlates(List<WellContent> wells)
    {
        Set<PlateIdentifier> plates = new HashSet<PlateIdentifier>();
        for (WellContent well : wells)
        {
            String platePermId = well.getPlate().getPermId();
            PlateIdentifier plateIdent = PlateIdentifier.createFromPermId(platePermId);
            plates.add(plateIdent);
        }
        return plates;
    }

    private WellFeatureCollection<FeatureVectorValues> tryLoadWellSingleFeatureVectors(
            Set<PlateIdentifier> plateIdentifiers)
    {
        return new WellFeatureCollectionLoader(session, businessObjectFactory, daoFactory)
                .tryLoadWellSingleFeatureVectors(plateIdentifiers, settings.getFeatureCodes());
    }

}
