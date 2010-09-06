/*
 * Copyright 2008 ETH Zuerich, CISD
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

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.DisposableEntityChooser;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.material.MaterialBrowserToolbar;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListMaterialDisplayCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * A {@link MaterialBrowserGrid} extension for showing materials used in wells of an experiment.
 * 
 * @author Piotr Buczek
 */
public class ExperimentWellMaterialBrowserGrid extends MaterialBrowserGrid
{
    /**
     * Creates a browser with a toolbar which allows to choose the material type. Allows to show or
     * edit material details.
     */
    public static DisposableEntityChooser<Material> createForExperiment(
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            TechId experimentId)
    {
        return createWithTypeChooser(screeningViewContext, experimentId);
    }

    private static DisposableEntityChooser<Material> createWithTypeChooser(
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            final TechId experimentId)
    {
        final MaterialBrowserToolbar toolbar =
                new MaterialBrowserToolbar(screeningViewContext.getCommonViewContext(), null);
        final ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider = toolbar;
        final ExperimentWellMaterialBrowserGrid browserGrid =
                createBrowserGrid(screeningViewContext, criteriaProvider, experimentId);
        browserGrid.addGridRefreshListener(toolbar);
        browserGrid.extendBottomToolbar(true);
        return browserGrid.asDisposableWithToolbar(toolbar);
    }

    private static ExperimentWellMaterialBrowserGrid createBrowserGrid(
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            final ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider,
            final TechId experimentId)
    {
        return new ExperimentWellMaterialBrowserGrid(screeningViewContext, true, criteriaProvider,
                experimentId);
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final TechId experimentId;

    protected ExperimentWellMaterialBrowserGrid(
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            boolean refreshAutomatically,
            ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider, TechId experimentId)
    {
        super(screeningViewContext.getCommonViewContext(), refreshAutomatically, criteriaProvider);
        this.screeningViewContext = screeningViewContext;
        this.experimentId = experimentId;
    }

    @Override
    protected ICriteriaProvider<ListMaterialDisplayCriteria> getCriteriaProvider()
    {
        return criteriaProvider;
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, Material> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Material>> callback)
    {
        criteria.copyPagingConfig(resultSetConfig);
        screeningViewContext.getService().listExperimentMaterials(experimentId, criteria, callback);
    }

    // TODO 2010-09-06, Piotr Buczek: check
    @Override
    protected void prepareExportEntities(TableExportCriteria<Material> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        viewContext.getService().prepareExportMaterials(exportCriteria, callback);
    }

    // TODO 2010-09-06, Piotr Buczek: set experiment
    // @Override
    // protected void showEntityViewer(Material material, boolean editMode, boolean active)
    // {
    // showEntityInformationHolderViewer(material, editMode, active);
    // }

}
