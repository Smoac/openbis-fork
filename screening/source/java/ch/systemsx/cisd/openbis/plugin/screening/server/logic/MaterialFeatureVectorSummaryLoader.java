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
import java.util.List;

import ch.systemsx.cisd.openbis.generic.server.dataaccess.IDAOFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.dto.Session;
import ch.systemsx.cisd.openbis.plugin.screening.server.IScreeningBusinessObjectFactory;
import ch.systemsx.cisd.openbis.plugin.screening.server.logic.dto.WellDataCollection;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialAllReplicasFeatureVectors;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialReplicaSubgroupFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSingleReplicaFeatureVector;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSummarySettings;

/**
 * {@See #tryLoadMaterialFeatureVectors}.
 * 
 * @author Tomasz Pylak
 */
public class MaterialFeatureVectorSummaryLoader extends ExperimentFeatureVectorSummaryLoader
{

    /**
     * Loads feature vectors (details and statistics) for the specified material in the specified
     * experiment.
     */
    public static MaterialAllReplicasFeatureVectors tryLoadMaterialFeatureVectors(Session session,
            IScreeningBusinessObjectFactory businessObjectFactory, IDAOFactory daoFactory,
            TechId materialId, TechId experimentId, MaterialSummarySettings settings)
    {
        return new MaterialFeatureVectorSummaryLoader(session, businessObjectFactory, daoFactory,
                settings).tryLoadMaterialFeatureVectors(materialId, experimentId);
    }

    private MaterialFeatureVectorSummaryLoader(Session session,
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
        MaterialFeatureVectorSummary materialGeneralSummary =
                tryCalculateMaterialSummary(materialId, wellDataCollection);
        if (materialGeneralSummary == null)
        {
            return null;
        }

        // TODO 2011-04-10, Tomasz Pylak: implement me!!!!!!!!!
        List<MaterialReplicaSubgroupFeatureVector> subgroups =
                new ArrayList<MaterialReplicaSubgroupFeatureVector>();
        List<MaterialSingleReplicaFeatureVector> replicas = null;
        return new MaterialAllReplicasFeatureVectors(wellDataCollection.getFeatureDescriptions(),
                materialGeneralSummary, subgroups, replicas);
    }

    private MaterialFeatureVectorSummary tryCalculateMaterialSummary(TechId materialId,
            WellDataCollection wellDataCollection)
    {
        List<MaterialFeatureVectorSummary> featureSummaries =
                calculateReplicasFeatureVectorSummaries(wellDataCollection);
        return tryFindMaterialSummary(materialId, featureSummaries);
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
