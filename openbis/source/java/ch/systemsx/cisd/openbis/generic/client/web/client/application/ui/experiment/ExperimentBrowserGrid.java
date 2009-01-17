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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import java.util.List;

import com.extjs.gxt.ui.client.widget.LayoutContainer;

import ch.systemsx.cisd.openbis.generic.client.shared.ExperimentType;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ExperimentModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.columns.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ListExperimentsCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Project;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;

/**
 * A {@link LayoutContainer} which contains the grid where the experiments are displayed.
 * 
 * @author Tomasz Pylak
 */
public final class ExperimentBrowserGrid extends AbstractBrowserGrid<Experiment, ExperimentModel>
{
    private static final String PREFIX = "experiment-browser";

    public static final String BROWSER_ID = GenericConstants.ID_PREFIX + PREFIX;

    public static final String GRID_ID = BROWSER_ID + "_grid";

    private ListExperimentsCriteria criteria;

    ExperimentBrowserGrid(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        super(viewContext, GRID_ID);
        setId(BROWSER_ID);
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String> resultSetConfig,
            AbstractAsyncCallback<ResultSet<Experiment>> callback)
    {
        copyPagingConfig(resultSetConfig);
        viewContext.getService().listExperiments(criteria, callback);
    }

    private void copyPagingConfig(DefaultResultSetConfig<String> resultSetConfig)
    {
        criteria.setLimit(resultSetConfig.getLimit());
        criteria.setOffset(resultSetConfig.getOffset());
        criteria.setSortInfo(resultSetConfig.getSortInfo());
        criteria.setResultSetKey(resultSetConfig.getResultSetKey());
    }

    @Override
    protected void showEntityViewer(ExperimentModel experimentModel)
    {
        final Experiment experiment = experimentModel.getBaseObject();
        final EntityKind entityKind = EntityKind.EXPERIMENT;
        final ITabItemFactory tabView =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        experiment.getExperimentType()).createClientPlugin(entityKind)
                        .createEntityViewer(experiment);
        DispatcherHelper.dispatchNaviEvent(tabView);
    }

    @Override
    protected List<ExperimentModel> createModels(List<Experiment> entities)
    {
        return ExperimentModel.asExperimentModels(entities);
    }

    @Override
    protected ColumnDefsAndConfigs<Experiment> createColumnsDefinition()
    {
        return ExperimentModel.createColumnsSchema(viewContext, criteria.getExperimentType());
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<Experiment> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        // TODO 2009--, Tomasz Pylak: !!!!!!!!!!!!!!!

        // viewContext.getService().prepareExportExperiments(exportCriteria, callback);
    }

    private static final String createHeader(ListExperimentsCriteria criteria)
    {
        final StringBuilder builder = new StringBuilder("Experiments");
        builder.append(" of type ");
        builder.append(criteria.getExperimentType().getCode());
        builder.append(" belonging to the project ");
        builder.append(criteria.getProjectCode());
        builder.append(" from group ");
        builder.append(criteria.getGroupCode());
        return builder.toString();
    }

    public final void refresh(final IDataRefreshCallback newRefreshCallback,
            final ExperimentType newSelectedType, final Project selectedProject)
    {
        boolean refreshColumnsDefinition = hasColumnsDefinitionChanged(newSelectedType);
        this.criteria = createListCriteria(newSelectedType, selectedProject);
        String newHeader = createHeader(criteria);

        super.refresh(newRefreshCallback, newHeader, refreshColumnsDefinition);
    }

    private static ListExperimentsCriteria createListCriteria(final ExperimentType selectedType,
            final Project selectedProject)
    {
        ListExperimentsCriteria criteria = new ListExperimentsCriteria();
        criteria.setExperimentType(selectedType);
        criteria.setProjectCode(selectedProject.getCode());
        criteria.setGroupCode(selectedProject.getGroup().getCode());
        return criteria;
    }

    private boolean hasColumnsDefinitionChanged(ExperimentType entityType)
    {
        return (criteria == null || entityType.equals(criteria.getExperimentType()) == false);
    }
}
