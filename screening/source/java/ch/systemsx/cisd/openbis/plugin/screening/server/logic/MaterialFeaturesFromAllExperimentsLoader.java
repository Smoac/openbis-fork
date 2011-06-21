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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.time.StopWatch;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.collections.CollectionUtils.ICollectionMappingFunction;
import ch.systemsx.cisd.openbis.generic.server.business.bo.datasetlister.IDatasetLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.BasicWellContentQueryResult;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.ExperimentReferenceQueryResult;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.IScreeningQuery;
import ch.systemsx.cisd.openbis.plugin.screening.server.dataaccess.PatternMatchingUtils;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.IWellData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialIdFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSimpleFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;

/**
 * Finds all experiments where the given material is present and calculates summaries for feature
 * vectors (if analysis data are available) .
 * 
 * @author Tomasz Pylak
 */
public class MaterialFeaturesFromAllExperimentsLoader extends AbstractContentLoader
{
    private static final Integer MAX_ANALYSIS_DATASETS_IN_BATCH = 100;

    public static List<MaterialSimpleFeatureVectorSummary> loadMaterialFeatureVectorsFromAllAssays(
            Session session, IScreeningBusinessObjectFactory businessObjectFactory,
            IDAOFactory daoFactory, TechId materialId, TechId projectTechIdOrNull,
            MaterialSummarySettings settings)
    {
        IScreeningQuery dao = createDAO(daoFactory);
        List<ExperimentReference> experiments =
                fetchExperiments(materialId, projectTechIdOrNull, dao);

        return new MaterialFeaturesFromAllExperimentsLoader(session, businessObjectFactory,
                daoFactory, dao, settings).loadMaterialFeatureVectorsFromAllAssays(materialId,
                experiments);
    }

    private static List<ExperimentReference> fetchExperiments(TechId materialId,
            TechId projectTechIdOrNull, IScreeningQuery dao)
    {
        List<ExperimentReferenceQueryResult> experimentRefs;
        if (projectTechIdOrNull == null)
        {
            experimentRefs = dao.getExperimentsWithMaterial(materialId.getId());
        } else
        {
            // load results only for experiments within a given project
            experimentRefs =
                    dao.getExperimentsWithMaterial(materialId.getId(), projectTechIdOrNull.getId());
        }
        return asExperimentReferences(experimentRefs);
    }

    private static List<ExperimentReference> asExperimentReferences(
            List<ExperimentReferenceQueryResult> experiments)
    {
        return CollectionUtils
                .map(experiments,
                        new ICollectionMappingFunction<ExperimentReference, ExperimentReferenceQueryResult>()
                            {
                                public ExperimentReference map(
                                        ExperimentReferenceQueryResult experiment)
                                {
                                    return createExperimentReference(experiment);
                                }
                            });
    }

    private static ExperimentReference createExperimentReference(ExperimentReferenceQueryResult exp)
    {
        return new ExperimentReference(exp.exp_id, exp.exp_perm_id, exp.exp_code,
                exp.exp_type_code, exp.proj_code, exp.space_code);
    }

    private final MaterialSummarySettings settings;

    private final IScreeningQuery screeningQuery;

    private MaterialFeaturesFromAllExperimentsLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            IScreeningQuery screeningQuery, MaterialSummarySettings settings)
    {
        super(session, businessObjectFactory, daoFactory);
        this.settings = settings;
        this.screeningQuery = screeningQuery;
    }

    private List<BasicWellContentQueryResult> fetchWellLocations(
            List<ExperimentReference> experiments)
    {
        StopWatch watch = createWatchAndStart();
        String materialTypePattern =
                PatternMatchingUtils.asPostgresSimilarExpression(settings
                        .getReplicaMatrialTypeSubstrings());
        List<BasicWellContentQueryResult> wells =
                screeningQuery.getPlateLocationsForExperiment(extractIds(experiments),
                        materialTypePattern);
        operationLog.info("[" + watch.getTime() + " msec] Fetching " + wells.size() + " wells.");
        return wells;
    }

    private static long[] extractIds(List<ExperimentReference> experiments)
    {
        long[] ids = new long[experiments.size()];
        int i = 0;
        for (ExperimentReference experiment : experiments)
        {
            ids[i++] = experiment.getId();
        }
        return ids;
    }

    /**
     * Note that different experiments can have different set of features!
     */
    private List<MaterialSimpleFeatureVectorSummary> loadMaterialFeatureVectorsFromAllAssays(
            TechId materialId, List<ExperimentReference> experiments)
    {
        List<MaterialSimpleFeatureVectorSummary> summaries =
                new ArrayList<MaterialSimpleFeatureVectorSummary>();
        StopWatch globalWatch = createWatchAndStart();

        List<ExperimentReference> experimentsBatch = new ArrayList<ExperimentReference>();
        long datasetsInBatch = 0;
        for (int i = 0; i < experiments.size(); i++)
        {
            ExperimentReference experiment = experiments.get(i);
            int datasetsNum = countAnalysisDatasets(experiment);
            operationLog.info(String.format("Experiment %s contains %d analysis datasets",
                    experiment, datasetsNum));
            if (datasetsNum == 0)
            {
                // avoid loading all the wells if there are no analysis datasets
                summaries.add(new MaterialSimpleFeatureVectorSummary(experiment));
            } else
            {
                datasetsInBatch += datasetsNum;
                experimentsBatch.add(experiment);
            }
            if (datasetsInBatch >= MAX_ANALYSIS_DATASETS_IN_BATCH
                    || (i == experiments.size() - 1 && experimentsBatch.isEmpty() == false))
            {
                operationLog.info(String.format("Processing %d experiments with %d datasets: %s",
                        experimentsBatch.size(), datasetsInBatch, experimentsBatch));
                List<MaterialSimpleFeatureVectorSummary> batchSummaries =
                        loadMaterialFeatureVectorsFromAllAssaysBatch(materialId, experimentsBatch);
                summaries.addAll(batchSummaries);
                experimentsBatch.clear();
                datasetsInBatch = 0;
            }
        }
        operationLog.info(String.format(
                "[%d msec] Fetching and calculating analysis summaries for %d experiments.",
                globalWatch.getTime(), summaries.size()));
        return summaries;
    }

    // load all experiments in a constant number of selects
    private List<MaterialSimpleFeatureVectorSummary> loadMaterialFeatureVectorsFromAllAssaysBatch(
            TechId materialId, List<ExperimentReference> experiments)
    {
        List<MaterialSimpleFeatureVectorSummary> summaries =
                new ArrayList<MaterialSimpleFeatureVectorSummary>();
        StopWatch watch = createWatchAndStart();
        int totalWellsLoaded = 0;
        int totalFeatureVectorsLoaded = 0;

        List<BasicWellContentQueryResult> allWells = fetchWellLocations(experiments);
        totalWellsLoaded += allWells.size();
        Set<PlateIdentifier> plates = extractPlates(allWells);
        WellFeatureCollection<FeatureVectorValues> allWellFeaturesOrNull =
                tryLoadWellSingleFeatureVectors(plates);

        if (allWellFeaturesOrNull == null)
        {
            addEmptySummaries(summaries, experiments);
        } else
        {
            for (ExperimentReference experiment : experiments)
            {
                totalFeatureVectorsLoaded += allWellFeaturesOrNull.getFeatures().size();
                Map<WellReference, Long/* material id */> wellToMaterialMap =
                        createWellToMaterialMapForExperiment(allWells, experiment);

                MaterialSimpleFeatureVectorSummary summary =
                        calculateExperimentFeatureVectorSummary(materialId, experiment,
                                allWellFeaturesOrNull, wellToMaterialMap);
                summaries.add(summary);
            }
        }
        operationLog.info(String.format(
                "[%d msec] Experiments batch: %d, wells loaded: %d, feature vectors: %d.",
                watch.getTime(), experiments.size(), totalWellsLoaded, totalFeatureVectorsLoaded));
        return summaries;
    }

    private void addEmptySummaries(List<MaterialSimpleFeatureVectorSummary> summaries,
            List<ExperimentReference> experiments)
    {
        for (ExperimentReference experiment : experiments)
        {
            summaries.add(new MaterialSimpleFeatureVectorSummary(experiment));
        }
    }

    private int countAnalysisDatasets(ExperimentReference experiment)
    {
        List<TechId> experiments = Arrays.asList(new TechId(experiment.getId()));
        IDatasetLister lister = businessObjectFactory.createDatasetLister(session);
        List<ExternalData> datasets = lister.listByExperimentTechIds(experiments);
        return ScreeningUtils.filterImageAnalysisDatasets(datasets).size();
    }

    private StopWatch createWatchAndStart()
    {
        StopWatch watch = new StopWatch();
        watch.start();
        return watch;
    }

    private static Map<WellReference, Long/* material id */> createWellToMaterialMapForExperiment(
            List<BasicWellContentQueryResult> wells, ExperimentReference experiment)
    {
        Map<WellReference, Long> wellToMaterialMap = new HashMap<WellReference, Long>();
        for (BasicWellContentQueryResult well : wells)
        {
            if (belongsToExperiment(well, experiment))
            {
                WellReference wellReference = createWellReference(well);
                wellToMaterialMap.put(wellReference, well.material_content_id);
            }
        }
        return wellToMaterialMap;
    }

    private static boolean belongsToExperiment(BasicWellContentQueryResult well,
            ExperimentReference experiment)
    {
        return well.exp_perm_id.equals(experiment.getPermId());
    }

    private static WellReference createWellReference(BasicWellContentQueryResult well)
    {
        WellLocation wellLocation = WellLocation.parseLocationStr(well.well_code);
        return new WellReference(wellLocation, well.plate_perm_id);
    }

    /**
     * @param experimentWellToMaterialMap wells belonging to the specified experiment
     * @param allFeatures all features, not only for the specified experiment but possibly also for
     *            few others
     */
    private MaterialSimpleFeatureVectorSummary calculateExperimentFeatureVectorSummary(
            TechId materialId, ExperimentReference experiment,
            WellFeatureCollection<FeatureVectorValues> allFeatures,
            Map<WellReference, Long> experimentWellToMaterialMap)
    {
        // we have to calculate summaries for all materials in the experiment to get the right
        // ranking of the specified material
        List<IWellData> experimentWellData =
                createWellData(experimentWellToMaterialMap, allFeatures.getFeatures());
        List<MaterialIdFeatureVectorSummary> experimentSummaries =
                WellReplicaSummaryCalculator.calculateReplicasFeatureVectorSummaries(
                        experimentWellData, settings.getAggregationType(), false);

        // select the summary of the right material
        MaterialIdFeatureVectorSummary materialSummary =
                findMaterialSummary(materialId, experimentSummaries);
        return new MaterialSimpleFeatureVectorSummary(experiment,
                allFeatures.getFeatureCodesAndLabels(), materialSummary.getFeatureVectorSummary(),
                materialSummary.getFeatureVectorRanks());
    }

    private MaterialIdFeatureVectorSummary findMaterialSummary(TechId materialId,
            List<MaterialIdFeatureVectorSummary> summaries)
    {
        for (MaterialIdFeatureVectorSummary summary : summaries)
        {
            if (summary.getMaterial().equals(materialId.getId()))
            {
                return summary;
            }
        }
        throw new IllegalStateException("It should not happen: no summary found for material "
                + materialId);
    }

    // connects each well with a material with its feature vector
    private static List<IWellData> createWellData(
            Map<WellReference, Long/* material id */> experimentWellToMaterialMap,
            List<FeatureVectorValues> allFeatures)
    {
        List<IWellData> experimentWellDataList = new ArrayList<IWellData>();
        for (FeatureVectorValues feature : allFeatures)
        {
            Long materialId = experimentWellToMaterialMap.get(feature.getWellReference());
            if (materialId != null)
            {
                float[] values = WellFeatureCollectionLoader.asFeatureVectorValues(feature);
                IWellData wellData = new WellData(materialId, values);
                experimentWellDataList.add(wellData);
            }
        }
        return experimentWellDataList;
    }

    private static Set<PlateIdentifier> extractPlates(List<BasicWellContentQueryResult> allWells)
    {
        Set<PlateIdentifier> plates = new HashSet<PlateIdentifier>();
        for (BasicWellContentQueryResult well : allWells)
        {
            String platePermId = well.plate_perm_id;
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
