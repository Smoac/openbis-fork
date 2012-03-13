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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.systemsx.cisd.common.collections.CollectionUtils;
import ch.systemsx.cisd.common.collections.CollectionUtils.ICollectionMappingFunction;
import ch.systemsx.cisd.common.collections.IKeyExtractor;
import ch.systemsx.cisd.common.collections.TableMap;
import ch.systemsx.cisd.common.collections.TableMap.UniqueKeyViolationStrategy;
import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ListMaterialCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.WellDataLoader.MaterialIdSummariesAndFeatures;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.MaterialIdFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;

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
            TechId experimentId, AnalysisProcedureCriteria analysisProcedureCriteria,
            MaterialSummarySettings settings)
    {
        return new ExperimentFeatureVectorSummaryLoader(session, businessObjectFactory, daoFactory,
                settings).loadExperimentFeatureVectors(experimentId, analysisProcedureCriteria);
    }

    protected final MaterialSummarySettings settings;

    private final WellDataLoader wellDataLoader;

    protected ExperimentFeatureVectorSummaryLoader(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            MaterialSummarySettings settings)
    {
        super(session, businessObjectFactory, daoFactory);
        this.settings = settings;
        this.wellDataLoader =
                new WellDataLoader(session, businessObjectFactory, daoFactory, settings);
    }

    private ExperimentFeatureVectorSummary loadExperimentFeatureVectors(TechId experimentId,
            AnalysisProcedureCriteria analysisProcedureCriteria)
    {

        MaterialIdSummariesAndFeatures summaries =
                wellDataLoader.tryCalculateExperimentFeatureVectorSummaries(experimentId,
                        settings.getReplicaMaterialTypeSubstrings(), analysisProcedureCriteria,
                        false);
        ExperimentReference experiment = loadExperimentByTechId(experimentId);
        if (summaries == null)
        {
            return createEmptySummary(experiment);
        }
        List<MaterialFeatureVectorSummary> enrichedFeatureSummaries =
                enrichWithMaterials(summaries.getFeatureSummaries());
        return new ExperimentFeatureVectorSummary(experiment, enrichedFeatureSummaries,
                summaries.getFeatureNames());
    }

    private List<MaterialFeatureVectorSummary> enrichWithMaterials(
            List<MaterialIdFeatureVectorSummary> summaries)
    {
        Set<Long> materialIds = extractMaterialIds(summaries);
        List<Material> materials = fetchMaterials(materialIds);
        return enrichWithMaterials(summaries, materials);
    }

    private static List<MaterialFeatureVectorSummary> enrichWithMaterials(
            List<MaterialIdFeatureVectorSummary> summaries, List<Material> materials)
    {
        final TableMap<Long, Material> materialMap = createMaterialMap(materials);
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

    private List<Material> fetchMaterials(Set<Long> materialIds)
    {
        return businessObjectFactory.createMaterialLister(session).list(
                ListMaterialCriteria.createFromMaterialIds(materialIds), true);
    }

    private static Set<Long> extractMaterialIds(List<MaterialIdFeatureVectorSummary> summaries)
    {
        Set<Long> ids = new HashSet<Long>();
        for (MaterialIdFeatureVectorSummary summary : summaries)
        {
            ids.add(summary.getMaterial());
        }
        return ids;
    }

    private static MaterialFeatureVectorSummary convert(MaterialIdFeatureVectorSummary summary,
            TableMap<Long, Material> materialMap)
    {
        Material material = materialMap.getOrDie(summary.getMaterial());
        return summary.createWithMaterial(material);
    }

    private static TableMap<Long/* material id */, Material> createMaterialMap(
            List<Material> materials)
    {
        return new TableMap<Long, Material>(materials, new IKeyExtractor<Long, Material>()
            {
                public Long getKey(Material material)
                {
                    return material.getId();
                }
            }, UniqueKeyViolationStrategy.ERROR);
    }

    private static ExperimentFeatureVectorSummary createEmptySummary(ExperimentReference experiment)
    {
        List<MaterialFeatureVectorSummary> materialsSummary = Collections.emptyList();
        List<CodeAndLabel> featureDescriptions = Collections.emptyList();
        return new ExperimentFeatureVectorSummary(experiment, materialsSummary, featureDescriptions);
    }
}
