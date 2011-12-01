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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server.resultset;

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs.MATERIAL_ID;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs.MATERIAL_PROPS_GROUP;
import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs.EXPERIMENT_PERM_ID;

import java.util.ArrayList;
import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataTypeCode;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.AnalysisProcedureCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs;

/**
 * A provider for material feature vector summaries.
 * 
 * @author Kaloyan Enimanev
 */
public class FeatureVectorSummaryProvider extends
        AbstractTableModelProvider<MaterialFeatureVectorSummary>
{
    private static final String FEATURE_VALUE_PREFIX = "FEATURE_VALUE-";

    private final IScreeningServer server;

    private final String sessionToken;

    private final TechId experimentId;

    private final AnalysisProcedureCriteria analysisProcedureCriteria;

    public FeatureVectorSummaryProvider(IScreeningServer server, String sessionToken,
            TechId experimentId, AnalysisProcedureCriteria analysisProcedureCriteria)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.experimentId = experimentId;
        this.analysisProcedureCriteria = analysisProcedureCriteria;
    }

    @Override
    public TypedTableModel<MaterialFeatureVectorSummary> createTableModel()
    {
        TypedTableModelBuilder<MaterialFeatureVectorSummary> builder =
                new TypedTableModelBuilder<MaterialFeatureVectorSummary>();
        ExperimentFeatureVectorSummary fvSummary =
                server.getExperimentFeatureVectorSummary(sessionToken, experimentId,
                        analysisProcedureCriteria);

        builder.addColumn(MATERIAL_ID);
        builder.addColumn(EXPERIMENT_PERM_ID).hideByDefault();

        builder.columnGroup(MATERIAL_PROPS_GROUP);

        List<CodeAndLabel> featureDescriptions = fvSummary.getFeatureDescriptions();
        List<String> featureColumnIds = new ArrayList<String>();
        List<String> rankColumnIds = new ArrayList<String>();

        for (CodeAndLabel featureDescription : featureDescriptions)
        {
            String featureCode = featureDescription.getCode();
            String featureColumnId = getFeatureColumnId(featureCode);
            String featureLabel = featureDescription.getLabel();
            builder.addColumn(featureColumnId).withTitle(featureLabel)
                    .withDataType(DataTypeCode.REAL);
            featureColumnIds.add(featureColumnId);

            String rankColumnId = getRankColumnId(featureCode);
            String rankTitle = ScreeningProviderMessages.RANK_COLUMN_MSG;
            builder.addColumn(rankColumnId).withTitle(rankTitle).withDataType(DataTypeCode.INTEGER);
            rankColumnIds.add(rankColumnId);
        }

        for (MaterialFeatureVectorSummary summary : fvSummary.getMaterialsSummary())
        {
            addRow(builder, fvSummary.getExperiment(), summary, featureColumnIds, rankColumnIds);
        }

        return builder.getModel();
    }

    private void addRow(TypedTableModelBuilder<MaterialFeatureVectorSummary> builder,
            ExperimentReference experiment, MaterialFeatureVectorSummary summary,
            List<String> featureColumnIds, List<String> rankColumnIds)
    {
        builder.addRow(summary);

        Material material = summary.getMaterial();
        builder.column(MATERIAL_ID).addString(material.getCode());
        builder.column(EXPERIMENT_PERM_ID).addString(
                experiment != null ? experiment.getPermId() : null);

        if (material.getProperties() != null)
        {
            builder.columnGroup(MATERIAL_PROPS_GROUP).addProperties(material.getProperties());
        }

        float[] featureSummaries = summary.getFeatureVectorSummary();
        int[] ranksValues = summary.getFeatureVectorRanks();

        for (int pos = 0; pos < featureSummaries.length; pos++)
        {
            String featureColumnId = featureColumnIds.get(pos);
            builder.column(featureColumnId).addDouble((double) featureSummaries[pos]);

            String rankColumnId = rankColumnIds.get(pos);
            builder.column(rankColumnId).addInteger((long) ranksValues[pos]);
        }
    }

    private String getFeatureColumnId(String code)
    {
        return FEATURE_VALUE_PREFIX + code;
    }

    private String getRankColumnId(String code)
    {
        return FeatureVectorSummaryGridColumnIDs.RANK_PREFIX + code;
    }
}
