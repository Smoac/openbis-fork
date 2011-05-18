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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.detailviewers;

import static ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs.RANK_PREFIX;

import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.FeatureVectorSummaryGridColumnIDs;

/**
 * A grid showing feature vector summary for an experiment.
 * 
 * @author Kaloyan Enimanev
 */
public class ExperimentAnalysisSummaryGrid extends TypedTableGrid<MaterialFeatureVectorSummary>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX
            + "experiment-feature-vector-summary";

    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + TypedTableGrid.GRID_POSTFIX;

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final IEntityInformationHolderWithIdentifier experiment;

    public static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            IEntityInformationHolderWithIdentifier experiment)
    {
        return new ExperimentAnalysisSummaryGrid(viewContext, experiment)
                .asDisposableWithoutToolbar();
    }

    private ICellListenerAndLinkGenerator<MaterialFeatureVectorSummary> createMaterialReplicaSummaryLinkGenerator()
    {
        return new ICellListenerAndLinkGenerator<MaterialFeatureVectorSummary>()
            {

                public void handle(TableModelRowWithObject<MaterialFeatureVectorSummary> rowItem,
                        boolean specialKeyPressed)
                {
                    Material material = rowItem.getObjectOrNull().getMaterial();
                    openMaterialDetailViewer(material);
                }

                public String tryGetLink(MaterialFeatureVectorSummary entity,
                        ISerializableComparable comparableValue)
                {
                    Material material = entity.getMaterial();
                    return ScreeningLinkExtractor.tryCreateMaterialDetailsLink(material,
                            getExperimentAsSearchCriteria());
                }
            };
    }

    private void openMaterialDetailViewer(Material material)
    {
        ClientPluginFactory.openImagingMaterialViewer(material, getExperimentAsSearchCriteria(),
                screeningViewContext);
    }

    private ExperimentSearchCriteria getExperimentAsSearchCriteria()
    {
        return ExperimentSearchCriteria.createExperiment(experiment);
    }

    ExperimentAnalysisSummaryGrid(IViewContext<IScreeningClientServiceAsync> viewContext,
            final IEntityInformationHolderWithIdentifier experiment)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, true,
                DisplayTypeIDGenerator.EXPERIMENT_FEATURE_VECTOR_SUMMARY_SECTION);
        this.screeningViewContext = viewContext;
        this.experiment = experiment;

        ICellListenerAndLinkGenerator<MaterialFeatureVectorSummary> linkGenerator =
                createMaterialReplicaSummaryLinkGenerator();
        registerListenerAndLinkGenerator(FeatureVectorSummaryGridColumnIDs.MATERIAL_ID,
                linkGenerator);
        registerListenerAndLinkGenerator(FeatureVectorSummaryGridColumnIDs.DETAILS, linkGenerator);

        setBorders(true);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<MaterialFeatureVectorSummary>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<MaterialFeatureVectorSummary>> callback)
    {
        screeningViewContext.getService().listExperimentFeatureVectorSummary(resultSetConfig,
                new TechId(experiment), callback);

    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<MaterialFeatureVectorSummary>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        screeningViewContext.getService().prepareExportFeatureVectorSummary(exportCriteria,
                callback);
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        String id = columnID;
        if (columnID.startsWith(RANK_PREFIX))
        {
            id = RANK_PREFIX;
        }
        return Dict.EXPERIMENT_FEATURE_VECTOR_SUMMARY_SECTION.toLowerCase() + "_"
                + id.toUpperCase();
    }

    public void dispose()
    {
        asDisposableWithoutToolbar().dispose();
    }
}
