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
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.collections.TableMap.UniqueKeyViolationStrategy;
import ch.systemsx.cisd.openbis.generic.server.business.bo.materiallister.IMaterialLister;
import ch.systemsx.cisd.openbis.generic.server.business.bo.samplelister.ISampleLister;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.IEntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListOrSearchSampleCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.IWellData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.IWellExtendedData;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialIdFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellDataCollection;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellExtendedData;
import ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto.PlateIdentifier;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.FeatureVectorValues;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellLocation;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.imaging.FeatureVectorLoader.WellFeatureCollection;

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

    private List<MaterialFeatureVectorSummary> calculateReplicasFeatureVectorSummaries(
            WellDataCollection wellDataCollection)
    {
        List<? extends IWellData> wellDataList = wellDataCollection.getWellDataList();
        List<MaterialIdFeatureVectorSummary> summaries =
                calculateReplicasFeatureVectorSummaries(wellDataList);
        return enrichWithMaterial(summaries, wellDataCollection);
    }

    private final List<MaterialIdFeatureVectorSummary> calculateReplicasFeatureVectorSummaries(
            List<? extends IWellData> wellDataList)
    {
        return WellReplicaSummaryCalculator.calculateReplicasFeatureVectorSummaries(wellDataList,
                settings.getAggregationType(), false);
    }

    private static List<MaterialFeatureVectorSummary> enrichWithMaterial(
            List<MaterialIdFeatureVectorSummary> summaries, WellDataCollection wellDataCollection)
    {
        final TableMap<Long, Material> materialMap = createMaterialMap(wellDataCollection);
        return CollectionUtils
                .map(summaries,
                        new ICollectionMappingFunction<MaterialFeatureVectorSummary, MaterialIdFeatureVectorSummary>()
                            {
                                public MaterialFeatureVectorSummary map(
                                        MaterialIdFeatureVectorSummary summary)
                                {
                                    return convert(summary, materialMap);
                                }
                            });
    }

    private static MaterialFeatureVectorSummary convert(MaterialIdFeatureVectorSummary summary,
            TableMap<Long, Material> materialMap)
    {
        Material material = materialMap.getOrDie(summary.getMaterial());
        return summary.createWithMaterial(material);
    }

    private static TableMap<Long/* material id */, Material> createMaterialMap(
            WellDataCollection wellDataCollection)
    {
        List<Material> materials =
                CollectionUtils.map(wellDataCollection.getWellDataList(),
                        new ICollectionMappingFunction<Material, IWellExtendedData>()
                            {
                                public Material map(IWellExtendedData wellData)
                                {
                                    return wellData.getMaterial();
                                }
                            });
        return new TableMap<Long, Material>(materials, new IKeyExtractor<Long, Material>()
            {
                public Long getKey(Material material)
                {
                    return material.getId();
                }
            }, UniqueKeyViolationStrategy.KEEP_FIRST);
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

        Set<PlateIdentifier> plateIdentifiers = extractIdentifiers(plates);
        WellFeatureCollection<FeatureVectorValues> featureVectorsCollection =
                tryLoadWellSingleFeatureVectors(plateIdentifiers);
        if (featureVectorsCollection == null)
        {
            return null; // no feature vector datasets connected to plates in this experiment
        }

        List<IWellExtendedData> wellDataList = asWellData(wells, featureVectorsCollection);
        return new WellDataCollection(wellDataList,
                featureVectorsCollection.getFeatureCodesAndLabels());
    }

    private WellFeatureCollection<FeatureVectorValues> tryLoadWellSingleFeatureVectors(
            Set<PlateIdentifier> plateIdentifiers)
    {
        return new WellFeatureCollectionLoader(session, businessObjectFactory, daoFactory)
                .tryLoadWellSingleFeatureVectors(plateIdentifiers, settings.getFeatureCodes());
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

    private List<IWellExtendedData> asWellData(List<Sample> wells,
            WellFeatureCollection<FeatureVectorValues> featureVectorsCollection)
    {
        Map<WellReference, FeatureVectorValues> featureVectors =
                createWellToFeatureVectorMap(featureVectorsCollection);
        List<String> orderedFeatureLabels = featureVectorsCollection.getFeatureLabels();
        List<IWellExtendedData> wellDataList = new ArrayList<IWellExtendedData>();
        for (Sample well : wells)
        {
            IWellExtendedData wellData =
                    tryCreateWellData(well, featureVectors, orderedFeatureLabels);
            if (wellData != null)
            {
                wellDataList.add(wellData);
            }
        }
        return wellDataList;
    }

    private IWellExtendedData tryCreateWellData(Sample well,
            Map<WellReference, FeatureVectorValues> featureVectorsMap,
            List<String> orderedFeatureLabels)
    {
        final float[] featureVectorNumbers =
                tryExtractFeatureVectorValues(well, featureVectorsMap, orderedFeatureLabels);
        if (featureVectorNumbers == null)
        {
            return null;
        }
        final Material replicaMaterial = tryFindReplicaMaterial(well, settings);
        if (replicaMaterial == null)
        {
            return null;
        }
        return new WellExtendedData(replicaMaterial.getId(), featureVectorNumbers, well,
                replicaMaterial);
    }

    private static Material tryFindReplicaMaterial(Sample well, MaterialSummarySettings settings)
    {
        String replicaMatrialTypePattern =
                ScreeningUtils.asJavaRegExpr(settings.getReplicaMatrialTypeSubstrings());
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

    private static float[] tryExtractFeatureVectorValues(Sample well,
            Map<WellReference, FeatureVectorValues> featureVectorsMap,
            List<String> orderedFeatureLabels)
    {
        WellReference wellReference = asWellReference(well);
        FeatureVectorValues featureVector = featureVectorsMap.get(wellReference);
        if (featureVector == null)
        {
            return null;
        }
        return WellFeatureCollectionLoader.asFeatureVectorValues(featureVector);
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

}
