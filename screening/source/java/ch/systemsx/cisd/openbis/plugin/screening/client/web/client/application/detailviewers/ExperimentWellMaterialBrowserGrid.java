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
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Material;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.ClientPluginFactory;
import ch.systemsx.cisd.openbis.plugin.screening.shared.basic.dto.PlateMaterialsSearchCriteria.ExperimentSearchCriteria;

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
            final IEntityInformationHolderWithIdentifier experiment)
    {
        final MaterialBrowserToolbar toolbar =
                new MaterialBrowserToolbar(screeningViewContext.getCommonViewContext(),
                        "experiment-well-material-browser-toolbar");
        final ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider = toolbar;
        final ExperimentWellMaterialBrowserGrid browserGrid =
                createBrowserGrid(screeningViewContext, criteriaProvider, experiment);
        browserGrid.addGridRefreshListener(toolbar);
        browserGrid.extendBottomToolbar(true);
        return browserGrid.asDisposableWithToolbar(toolbar);
    }

    private static ExperimentWellMaterialBrowserGrid createBrowserGrid(
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            final ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider,
            final IEntityInformationHolderWithIdentifier experiment)
    {
        return new ExperimentWellMaterialBrowserGrid(screeningViewContext, true, criteriaProvider,
                experiment);
    }

    private final IViewContext<IScreeningClientServiceAsync> screeningViewContext;

    private final IEntityInformationHolderWithIdentifier experiment;

    protected ExperimentWellMaterialBrowserGrid(
            final IViewContext<IScreeningClientServiceAsync> screeningViewContext,
            boolean refreshAutomatically,
            ICriteriaProvider<ListMaterialDisplayCriteria> criteriaProvider,
            IEntityInformationHolderWithIdentifier experiment)
    {
        super(screeningViewContext.getCommonViewContext(), refreshAutomatically, criteriaProvider);
        this.screeningViewContext = screeningViewContext;
        this.experiment = experiment;
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
        screeningViewContext.getService().listExperimentMaterials(TechId.create(experiment),
                criteria, callback);
    }

    @Override
    protected void showEntityViewer(Material material, boolean editMode, boolean active)
    {
        if (editMode == false)
        {
            ExperimentSearchCriteria experimentCriteria =
                    ExperimentSearchCriteria.createExperiment(experiment.getId(), experiment
                            .getIdentifier());
            ClientPluginFactory.openPlateLocationsMaterialViewer(material, experimentCriteria,
                    screeningViewContext);
        } else
        {
            super.showEntityViewer(material, editMode, active);
        }
    }

}
