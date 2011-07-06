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
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.MaterialIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ui.columns.specific.ScreeningLinkExtractor;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.ExperimentReference;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.MaterialSimpleFeatureVectorSummary;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchByProjectCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.WellSearchCriteria.ExperimentSearchCriteria;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.grids.MaterialFeatureVectorsFromAllExperimentsGridColumnIDs;

/**
 * A grid showing feature vector summaries for a material from all corresponding experiments.
 * 
 * @author Kaloyan Enimanev
 */
public class MaterialFeaturesFromAllExperimentsGrid extends
        TypedTableGrid<MaterialSimpleFeatureVectorSummary>
{
    private static final String ID = "material_features_from_all_experiments";

    private static final String PREFIX = GenericConstants.ID_PREFIX + ID;

    public static final String BROWSER_ID = PREFIX + "_main";

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final IEntityInformationHolderWithPermId material;

    private final ExperimentSearchByProjectCriteria experimentSearchCriteria;

    public static IDisposableComponent create(
            IViewContext<IScreeningClientServiceAsync> viewContext,
            IEntityInformationHolderWithPermId material,
            ExperimentSearchByProjectCriteria experimentSearchCriteria)
    {
        return new MaterialFeaturesFromAllExperimentsGrid(viewContext, material,
                experimentSearchCriteria).asDisposableWithoutToolbar();
    }

    MaterialFeaturesFromAllExperimentsGrid(IViewContext<IScreeningClientServiceAsync> viewContext,
            IEntityInformationHolderWithPermId material,
            ExperimentSearchByProjectCriteria experimentSearchCriteria)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, true,
                DisplayTypeIDGenerator.MATERIAL_REPLICA_SUMMARY_SECTION);
        this.screeningViewContext = viewContext;
        this.material = material;
        this.experimentSearchCriteria = experimentSearchCriteria;
        setBorders(true);
        linkExperiment();
        linkMaterialInExperiment();
    }

    private void linkExperiment()
    {
        registerListenerAndLinkGenerator(
                MaterialFeatureVectorsFromAllExperimentsGridColumnIDs.EXPERIMENT,
                new ICellListenerAndLinkGenerator<MaterialSimpleFeatureVectorSummary>()
                    {
                        public void handle(
                                TableModelRowWithObject<MaterialSimpleFeatureVectorSummary> rowItem,
                                boolean specialKeyPressed)
                        {
                            ClientPluginFactory.openImagingExperimentViewer(rowItem
                                    .getObjectOrNull().getExperiment(),
                                    getRestrictGlobalScopeLinkToProject(), screeningViewContext);
                        }

                        public String tryGetLink(MaterialSimpleFeatureVectorSummary entity,
                                ISerializableComparable value)
                        {
                            return ClientPluginFactory.createImagingExperimentViewerLink(
                                    entity.getExperiment(), getRestrictGlobalScopeLinkToProject(),
                                    screeningViewContext);
                        }
                    });
    }

    private void linkMaterialInExperiment()
    {
        registerListenerAndLinkGenerator(
                MaterialFeatureVectorsFromAllExperimentsGridColumnIDs.EXPERIMENT,
                new ICellListenerAndLinkGenerator<MaterialSimpleFeatureVectorSummary>()
                    {
                        public void handle(
                                TableModelRowWithObject<MaterialSimpleFeatureVectorSummary> rowItem,
                                boolean specialKeyPressed)
                        {
                            MaterialSimpleFeatureVectorSummary summaryOrNull =
                                    rowItem.getObjectOrNull();
                            if (summaryOrNull == null)
                            {
                                return;
                            }
                            String experimentPermId = summaryOrNull.getExperiment().getPermId();

                            // NOTE: even in not-embedded mode we open specific standalone summary
                            // view instead of material detail view (which contains the summary view
                            // as one of its tabs). The reason is that in such a case we are already
                            // in material detail view and the possibility of switching tabs is not
                            // implemented there.
                            MaterialReplicaSummaryViewer.openTab(screeningViewContext,
                                    experimentPermId, getRestrictGlobalScopeLinkToProject(),
                                    new MaterialIdentifier(material));
                        }

                        public String tryGetLink(MaterialSimpleFeatureVectorSummary entity,
                                ISerializableComparable value)
                        {
                            ExperimentSearchCriteria experiment = getExperimentCriteria(entity);
                            String link =
                                    ScreeningLinkExtractor.createMaterialDetailsLink(material,
                                            experiment);
                            return link;
                        }
                    });
    }

    private boolean getRestrictGlobalScopeLinkToProject()
    {
        return WellSearchCriteria.shouldRestrictScopeToProject(experimentSearchCriteria);
    }

    private ExperimentSearchCriteria getExperimentCriteria(
            MaterialSimpleFeatureVectorSummary summary)
    {
        ExperimentReference experimentRef = summary.getExperiment();
        return ExperimentSearchCriteria.createExperiment(experimentRef,
                getRestrictGlobalScopeLinkToProject());
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<MaterialSimpleFeatureVectorSummary>> resultSetConfig,
            AsyncCallback<TypedTableResultSet<MaterialSimpleFeatureVectorSummary>> callback)
    {
        screeningViewContext.getService().listMaterialFeaturesFromAllExperiments(resultSetConfig,
                new TechId(material), experimentSearchCriteria, callback);
    }

    @Override
    protected void prepareExportEntities(
            TableExportCriteria<TableModelRowWithObject<MaterialSimpleFeatureVectorSummary>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        screeningViewContext.getService().prepareExportMaterialFeaturesFromAllExperiments(
                exportCriteria, callback);
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return ID + "_" + columnID.toUpperCase();
    }

    public void dispose()
    {
        asDisposableWithoutToolbar().dispose();
    }
}
