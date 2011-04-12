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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.collections.CollectionUtils.ICollectionMappingFunction;
import ch.systemsx.cisd.common.collections.GroupByMap;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.collections.TableMap.UniqueKeyViolationStrategy;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.generic.shared.util.StopWatchLogger;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.IWellData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellDataCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.DatasetReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureValue;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.IHCSFeatureVectorLoader;

/**
 * {@See #loadExperimentFeatureVectors}.
 * 
 * @author Tomasz Pylak
 */
public class ExperimentFeatureVectorSummaryLoader extends AbstractContentLoader
{
    /**
     * Loads feature vectors summaries for all the materials in the specified experiment.
     */
    public static ExperimentFeatureVectorSummary loadExperimentFeatureVectors(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId experimentId, MaterialSummarySettings settings)
    {
        return new ExperimentFeatureVectorSummaryLoader(session, businessObjectFactory, daoFactory,
                settings).loadExperimentFeatureVectors(experimentId);
    }

    protected final MaterialSummarySettings settings;

    protected ExperimentFeatureVectorSummaryLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            MaterialSummarySettings settings)
    {
        super(session, businessObjectFactory, daoFactory);
        this.settings = settings;
    }

    private ExperimentFeatureVectorSummary loadExperimentFeatureVectors(TechId experimentId)
    {
        WellDataCollection wellDataCollection = tryLoadWellData(experimentId);
        ExperimentReference experiment = loadExperimentByTechId(experimentId);

        if (wellDataCollection == null)
        {
            return createEmptySummary(experiment);
        }
        List<MaterialFeatureVectorSummary> featureSummaries =
                calculateReplicasFeatureVectorSummaries(wellDataCollection);

        return new ExperimentFeatureVectorSummary(experiment, featureSummaries,
                wellDataCollection.getFeatureDescriptions());
    }

    protected final List<MaterialFeatureVectorSummary> calculateReplicasFeatureVectorSummaries(
            WellDataCollection wellDataCollection)
    {
        StopWatchLogger watch = StopWatchLogger.createAndStart();
        List<MaterialFeatureVectorSummary> summaries =
                WellReplicaSummaryCalculator.calculateReplicasFeatureVectorSummaries(
                        wellDataCollection.getWellDataList(), settings.getAggregationType());
        watch.logAndReset("calculateReplicasFeatureVectorSummaries");
        return summaries;
    }

    protected final WellDataCollection tryLoadWellData(TechId experimentId)
    {
        ISampleLister sampleLister = businessObjectFactory.createSampleLister(session);
        List<Sample> plates = sampleLister.list(createExperientCriteria(experimentId.getId()));
        List<Sample> wells = sampleLister.list(createWellsCriteria(plates));
        if (wells.isEmpty())
        {
            return null; // no wells in this experiment
        }
        enrichWithMaterialProperties(wells);

        List<String> featureCodes = settings.getFeatureCodes();
        WellFeatureCollection<FeatureVectorValues> featureVectorsCollection =
                tryLoadWellSingleFeatureVectors(extractIdentifiers(plates), featureCodes);
        if (featureVectorsCollection == null)
        {
            return null; // no feature vector datasets connected to plates in this experiment
        }

        List<IWellData> wellDataList = asWellData(wells, featureVectorsCollection);
        return new WellDataCollection(wellDataList,
                featureVectorsCollection.getFeatureCodesAndLabels());
    }

    private static ExperimentFeatureVectorSummary createEmptySummary(ExperimentReference experiment)
    {
        List<MaterialFeatureVectorSummary> materialsSummary = Collections.emptyList();
        List<CodeAndLabel> featureDescriptions = Collections.emptyList();
        return new ExperimentFeatureVectorSummary(experiment, materialsSummary, featureDescriptions);
    }

    private void enrichWithMaterialProperties(List<Sample> samples)
    {
        IMaterialLister materialLister = businessObjectFactory.createMaterialLister(session);
        List<Material> containedMaterials = extractMaterialsWithDuplicates(samples);
        materialLister.enrichWithProperties(containedMaterials);
    }

    private static List<Material> extractMaterialsWithDuplicates(List<Sample> samples)
    {
        List<Material> materials = new ArrayList<Material>();
        for (Sample sample : samples)
        {
            materials.addAll(extractMaterials(sample));
        }
        return materials;
    }

    private static Collection<? extends Material> extractMaterials(Sample sample)
    {
        List<Material> materials = new ArrayList<Material>();
        List<IEntityProperty> properties = sample.getProperties();
        for (IEntityProperty property : properties)
        {
            Material material = property.getMaterial();
            if (material != null)
            {
                materials.add(material);
            }
        }
        return materials;
    }

    private List<IWellData> asWellData(List<Sample> wells,
            WellFeatureCollection<FeatureVectorValues> featureVectorsCollection)
    {
        Map<WellReference, FeatureVectorValues> featureVectors =
                createWellToFeatureVectorMap(featureVectorsCollection);
        List<String> orderedFeatureLabels = featureVectorsCollection.getFeatureLabels();
        List<IWellData> wellDataList = new ArrayList<IWellData>();
        for (Sample well : wells)
        {
            IWellData wellData = tryCreateWellData(well, featureVectors, orderedFeatureLabels);
            if (wellData != null)
            {
                wellDataList.add(wellData);
            }
        }
        return wellDataList;
    }

    private IWellData tryCreateWellData(Sample well,
            Map<WellReference, FeatureVectorValues> featureVectorsMap,
            List<String> orderedFeatureLabels)
    {
        final float[] featureVectorNumbers =
                tryExtractFeatureVectorNumbers(well, featureVectorsMap, orderedFeatureLabels);
        if (featureVectorNumbers == null)
        {
            return null;
        }
        final Material replicaMaterial = tryFindReplicaMaterial(well, settings);
        if (replicaMaterial == null)
        {
            return null;
        }
        return new WellData(replicaMaterial.getId(), featureVectorNumbers, well, replicaMaterial);
    }

    private static Material tryFindReplicaMaterial(Sample well, MaterialSummarySettings settings)
    {
        String replicaMatrialTypePattern = settings.getReplicaMatrialTypePattern();
        if (replicaMatrialTypePattern == null)
        {
            return null;
        }
        for (Material material : extractMaterials(well))
        {
            if (material.getEntityType().getCode().matches(replicaMatrialTypePattern))
            {
                return material;
            }
        }
        return null;
    }

    private static float[] tryExtractFeatureVectorNumbers(Sample well,
            Map<WellReference, FeatureVectorValues> featureVectorsMap,
            List<String> orderedFeatureLabels)
    {
        WellReference wellReference = asWellReference(well);
        FeatureVectorValues featureVector = featureVectorsMap.get(wellReference);
        if (featureVector == null)
        {
            return null;
        }
        return asFeatureVectorNumbers(featureVector, orderedFeatureLabels);
    }

    private static float[] asFeatureVectorNumbers(FeatureVectorValues featureVector,
            List<String> orderedFeatureLabels)
    {
        Map<String, FeatureValue> featureMap = featureVector.getFeatureMap();
        float[] values = new float[featureMap.size()];
        int i = 0;
        for (String featureLabel : orderedFeatureLabels)
        {
            FeatureValue featureValue = featureMap.get(featureLabel);
            values[i++] = asFloat(featureValue);
        }
        return values;
    }

    private static float asFloat(FeatureValue featureValue)
    {
        return featureValue.isFloat() ? featureValue.asFloat() : Float.NaN;
    }

    private static WellReference asWellReference(Sample well)
    {
        WellLocation location =
                ScreeningUtils.tryCreateLocationFromMatrixCoordinate(well.getSubCode());
        WellReference wellReference = new WellReference(location, well.getContainer().getPermId());
        return wellReference;
    }

    private static Set<PlateIdentifier> extractIdentifiers(List<Sample> plates)
    {
        Set<PlateIdentifier> idents = new HashSet<PlateIdentifier>();
        for (Sample plate : plates)
        {
            idents.add(PlateIdentifier.createFromPermId(plate.getPermId()));
        }
        return idents;
    }

    // private static List<WellContent> convert(List<Sample> wells, ExperimentReference experiment)
    // {
    // List<WellContent> wellContents = new ArrayList<WellContent>();
    // for (Sample well : wells)
    // {
    // wellContents.add(convert(well, experiment));
    // }
    // return wellContents;
    // }
    //
    // private static WellContent convert(Sample well, ExperimentReference experiment)
    // {
    // WellLocation location =
    // ScreeningUtils.tryCreateLocationFromMatrixCoordinate(well.getSubCode());
    // EntityReference wellReference = asEntityReference(well);
    // EntityReference plate = asEntityReference(well.getContainer());
    //
    // return new WellContent(location, wellReference, plate, experiment);
    // }
    //
    // private static EntityReference asEntityReference(Sample sample)
    // {
    // return new EntityReference(sample.getId(), sample.getSubCode(), sample.getSampleType()
    // .getCode(), EntityKind.SAMPLE, sample.getPermId());
    // }

    private static ListOrSearchSampleCriteria createWellsCriteria(List<Sample> plates)
    {
        Collection<Long> plateIds = new ArrayList<Long>();
        for (Sample plate : plates)
        {
            plateIds.add(plate.getId());
        }
        ListOrSearchSampleCriteria criteria =
                new ListOrSearchSampleCriteria(
                        ListOrSearchSampleCriteria.createForContainers(plateIds));
        return criteria;
    }

    private static ListOrSearchSampleCriteria createExperientCriteria(long expId)
    {
        return new ListOrSearchSampleCriteria(
                ListOrSearchSampleCriteria.createForExperiment(new TechId(expId)));
    }

    /**
     * Fetches feature vectors from different datastores and merges them (assuming that feature
     * codes are the same).
     */
    private static class FeatureVectorRetriever
    {
        public static WellFeatureCollection<FeatureVectorValues> tryFetch(
                Collection<DatasetReference> datasets, List<String> featureCodes,
                IScreeningBusinessObjectFactory businessObjectFactory)
        {
            assert datasets.size() > 0 : "No feature vector datasets specified.";
            return new FeatureVectorRetriever(businessObjectFactory, featureCodes)
                    .tryFetch(datasets);
        }

        private final IScreeningBusinessObjectFactory businessObjectFactory;

        private final List<String> featureCodes;

        public FeatureVectorRetriever(IScreeningBusinessObjectFactory businessObjectFactory,
                List<String> featureCodes)
        {
            this.businessObjectFactory = businessObjectFactory;
            this.featureCodes = featureCodes;
        }

        private WellFeatureCollection<FeatureVectorValues> tryFetch(
                Collection<DatasetReference> datasets)
        {
            GroupByMap<String/* datastore code */, DatasetReference> datastoreToDatasetsMap =
                    GroupByMap.create(datasets, new IKeyExtractor<String, DatasetReference>()
                        {
                            public String getKey(DatasetReference datasetReference)
                            {
                                return datasetReference.getDatastoreCode();
                            }
                        });
            WellFeatureCollection<FeatureVectorValues> allFeatures = null;
            for (String datastoreCode : datastoreToDatasetsMap.getKeys())
            {
                List<DatasetReference> datasetsForDatastore =
                        datastoreToDatasetsMap.getOrDie(datastoreCode);
                WellFeatureCollection<FeatureVectorValues> features =
                        fetchFromDatastore(datastoreCode, datasetsForDatastore);
                if (allFeatures == null)
                {
                    allFeatures = features;
                } else
                {
                    mergeFeatures(allFeatures, features);
                }
            }
            return allFeatures;
        }

        private static void mergeFeatures(WellFeatureCollection<FeatureVectorValues> allFeatures,
                WellFeatureCollection<FeatureVectorValues> features)
        {
            if (allFeatures.getFeatureCodes().equals(features.getFeatureCodes()) == false)
            {
                throw new IllegalStateException(
                        "Cannot merge feature vectors from different datastores because the have different set of features: '"
                                + allFeatures.getFeatureCodes()
                                + "' and '"
                                + features.getFeatureCodes() + "'.");
            }
            allFeatures.getFeatures().addAll(features.getFeatures());
        }

        private WellFeatureCollection<FeatureVectorValues> fetchFromDatastore(String datastoreCode,
                List<DatasetReference> datasets)
        {
            IHCSFeatureVectorLoader loader =
                    businessObjectFactory.createHCSFeatureVectorLoader(datastoreCode);
            return loader.fetchDatasetFeatureValues(extractCodes(datasets), featureCodes);
        }

        private static List<String> extractCodes(List<DatasetReference> datasets)
        {
            return CollectionUtils.map(datasets,
                    new ICollectionMappingFunction<String, DatasetReference>()
                        {
                            public String map(DatasetReference element)
                            {
                                return element.getCode();
                            }
                        });
        }
    }

    private WellFeatureCollection<FeatureVectorValues> tryLoadWellSingleFeatureVectors(
            Set<PlateIdentifier> plates, List<String> featureCodes)
    {
        FeatureVectorDatasetLoader datasetsRetriever = createFeatureVectorDatasetsRetriever(plates);
        Collection<ExternalData> featureVectorDatasets =
                datasetsRetriever.getFeatureVectorDatasets();
        if (featureVectorDatasets.isEmpty())
        {
            return null;
        }
        List<DatasetReference> datasetPerPlate = chooseSingleDatasetForPlate(featureVectorDatasets);
        return FeatureVectorRetriever
                .tryFetch(datasetPerPlate, featureCodes, businessObjectFactory);
    }

    private static Map<WellReference, FeatureVectorValues> createWellToFeatureVectorMap(
            WellFeatureCollection<FeatureVectorValues> featureVectors)
    {
        Map<WellReference, FeatureVectorValues> wellToFeatureVectorMap =
                new HashMap<WellReference, FeatureVectorValues>();
        for (FeatureVectorValues featureValues : featureVectors.getFeatures())
        {
            WellReference wellReference = featureValues.getWellReference();
            wellToFeatureVectorMap.put(wellReference, featureValues);
        }
        return wellToFeatureVectorMap;
    }

    // TODO 2011-04-04, Tomasz Pylak: here if the plate has more than one dataset assigned, we
    // take the first and ignore the rest. The clean solution would be to introduce analysis
    // runs, where each plate has at most one analysis dataset in each run. {@link
    // UniqueKeyViolationStrategy} could be set to {@link UniqueKeyViolationStrategy.ERROR} in
    // such a case.
    protected static List<DatasetReference> chooseSingleDatasetForPlate(
            Collection<ExternalData> datasets)
    {
        TableMap<String, ExternalData> plateToDatasetMap =
                new TableMap<String, ExternalData>(datasets,
                        new IKeyExtractor<String, ExternalData>()
                            {
                                public String getKey(ExternalData externalData)
                                {
                                    Sample plate = externalData.getSample();
                                    return plate != null ? plate.getPermId() : null;
                                }
                            }, UniqueKeyViolationStrategy.KEEP_FIRST);

        List<DatasetReference> datasetPerPlate = new ArrayList<DatasetReference>();
        for (String platePermId : plateToDatasetMap.keySet())
        {
            if (platePermId != null)
            {
                ExternalData dataset = plateToDatasetMap.getOrDie(platePermId);
                DatasetReference datasetReference = ScreeningUtils.createDatasetReference(dataset);
                datasetPerPlate.add(datasetReference);
            }
        }
        return datasetPerPlate;
    }
}
