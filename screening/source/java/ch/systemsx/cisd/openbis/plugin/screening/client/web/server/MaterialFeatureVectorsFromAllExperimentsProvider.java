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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.server;

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.MaterialFeatureVectorsFromAllExperimentsGridColumnIDs.EXPERIMENT;

import java.util.List;

import ch.systemsx.cisd.openbis.generic.client.web.server.resultset.AbstractTableModelProvider;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.CodeAndLabel;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTableCell;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TypedTableModel;
import ch.systemsx.cisd.openbis.generic.shared.util.IColumnGroup;
import ch.systemsx.cisd.openbis.generic.shared.util.TypedTableModelBuilder;
import ch.systemsx.cisd.openbis.plugin.screening.shared.IScreeningServer;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSimpleFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchByProjectCriteria;

/**
 * A provider for material replica feature summaries.
 * 
 * @author Kaloyan Enimanev
 */
class MaterialFeatureVectorsFromAllExperimentsProvider extends
        AbstractTableModelProvider<MaterialSimpleFeatureVectorSummary>
{
    private final IScreeningServer server;

    private final String sessionToken;

    private final TechId materialId;

    private final ExperimentSearchByProjectCriteria experimentSearchCriteria;

    public MaterialFeatureVectorsFromAllExperimentsProvider(IScreeningServer server,
            String sessionToken, TechId materialId,
            ExperimentSearchByProjectCriteria experimentSearchCriteria)
    {
        this.server = server;
        this.sessionToken = sessionToken;
        this.materialId = materialId;
        this.experimentSearchCriteria = experimentSearchCriteria;
    }

    @Override
    public TypedTableModel<MaterialSimpleFeatureVectorSummary> createTableModel()
    {
        TypedTableModelBuilder<MaterialSimpleFeatureVectorSummary> builder =
                new TypedTableModelBuilder<MaterialSimpleFeatureVectorSummary>();
        List<MaterialSimpleFeatureVectorSummary> summaries =
                server.getMaterialFeatureVectorsFromAllExperiments(sessionToken, materialId,
                        experimentSearchCriteria);

        builder.addColumn(EXPERIMENT);
        createFeatureColumns(builder, summaries);
        for (MaterialSimpleFeatureVectorSummary row : summaries)
        {
            addRow(builder, row);
        }

        return builder.getModel();
    }

    private void createFeatureColumns(
            TypedTableModelBuilder<MaterialSimpleFeatureVectorSummary> builder,
            List<MaterialSimpleFeatureVectorSummary> summaries)
    {
        IColumnGroup columnGroup = builder.columnGroup("FEATURES");
        for (MaterialSimpleFeatureVectorSummary summary : summaries)
        {
            for (CodeAndLabel codeAndLabel : summary.getFeatureDescriptions())
            {
                columnGroup.column(codeAndLabel.getCode()).withTitle(codeAndLabel.getLabel());
            }
        }
    }

    private void addRow(TypedTableModelBuilder<MaterialSimpleFeatureVectorSummary> builder,
            MaterialSimpleFeatureVectorSummary row)
    {
        builder.addRow(row);
        ExperimentReference exp = row.getExperiment();
        EntityTableCell experimentCell =
                new EntityTableCell(EntityKind.EXPERIMENT, exp.getPermId(), exp.getIdentifier());
        experimentCell.setLinkText(exp.getCode());
        builder.column(EXPERIMENT).withEntityKind(EntityKind.EXPERIMENT).addValue(experimentCell);

        float[] features = row.getFeatureVectorSummary();
        List<CodeAndLabel> descriptions = row.getFeatureDescriptions();
        for (int i = 0; i < features.length; i++)
        {
            CodeAndLabel description = descriptions.get(i);
            builder.column(description.getCode()).addDouble((double) features[i]);
        }
    }
}
