/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer.RealNumberRenderer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionKind;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.IColumnDefinitionUI;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractSimpleBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ColumnDefsAndConfigs;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListener;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ResultSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.GridRowModel;
import ch.systemsx.cisd.openbis.generic.shared.basic.IColumnDefinition;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.columns.InternalAbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application.columns.ProteinColDefKind;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.dto.ListProteinByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.ProteinInfo;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.shared.basic.dto.Treatment;

/**
 * @author Franz-Josef Elmer
 */
class ProteinByExperimentBrowserGrid extends AbstractSimpleBrowserGrid<ProteinInfo>
{
    private static final String ABUNDANCE_PROPERTY_KEY = "ABUNDANCE";

    private static final String PREFIX = GenericConstants.ID_PREFIX
            + "protein-by-experiment-browser";

    // browser consists of the grid and additional toolbars (paging, filtering)
    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + "_grid";

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    private final ProteinByExperimentBrowerToolBar toolbar;

    private ListProteinByExperimentCriteria criteria;

    private List<AbundanceColumnDefinition> abundanceColumnDefinitions;

    private IDataRefreshCallback postRefreshCallback = new IDataRefreshCallback()
        {
            public void postRefresh(boolean wasSuccessful)
            {
            }
        };

    static IDisposableComponent create(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext,
            BasicEntityType experimentType, Experiment experiment)
    {
        final IDisposableComponent summaryGrid = ProteinSummaryGrid.create(viewContext);
        ProteinByExperimentBrowserGrid browserGrid =
                new ProteinByExperimentBrowserGrid(viewContext, experiment);
        final IDisposableComponent disposableBrowerGrid = browserGrid.asDisposableWithoutToolbar();
        final ProteinByExperimentBrowerToolBar toolBar = browserGrid.toolbar;
        toolBar.setSummaryGrid((ProteinSummaryGrid) summaryGrid.getComponent());
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());
        container.add(toolBar);
        TabPanel tabPanel = new TabPanel();
        tabPanel.add(createTab(disposableBrowerGrid, viewContext, Dict.PROTEIN_BROWSER));
        tabPanel.add(createTab(summaryGrid, viewContext, Dict.PROTEIN_SUMMARY));
        container.add(tabPanel, new RowData(1, 1));
        toolBar.update();
        return new IDisposableComponent()
            {
                public void update(Set<DatabaseModificationKind> observedModifications)
                {
                    disposableBrowerGrid.update(observedModifications);
                    summaryGrid.update(observedModifications);
                }

                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return disposableBrowerGrid.getRelevantModifications();
                }

                public Component getComponent()
                {
                    return container;
                }

                public void dispose()
                {
                    disposableBrowerGrid.dispose();
                    summaryGrid.dispose();
                }
            };
    }

    private static TabItem createTab(IDisposableComponent disposableComponent,
            IMessageProvider messageProvider, String titleKey)
    {
        TabItem tabItem = new TabItem(messageProvider.getMessage(titleKey));
        tabItem.setLayout(new FitLayout());
        Component component = disposableComponent.getComponent();
        component.setHeight("100%");
        tabItem.add(component);
        return tabItem;
    }

    private ProteinByExperimentBrowserGrid(
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext, Experiment experiment)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, GRID_ID, false,
                PhosphoNetXDisplayTypeIDGenerator.PROTEIN_BY_EXPERIMENT_BROWSER_GRID);
        specificViewContext = viewContext;
        toolbar = new ProteinByExperimentBrowerToolBar(viewContext, experiment);
        toolbar.setBrowserGrid(this);
        registerLinkClickListenerFor(ProteinColDefKind.ACCESSION_NUMBER.id(),
                new ICellListener<ProteinInfo>()
                    {
                        public void handle(ProteinInfo rowItem, boolean keyPressed)
                        {
                            AbstractTabItemFactory tabItemFactory =
                                    ProteinViewer.createTabItemFactory(viewContext,
                                            toolbar.getExperimentOrNull(), rowItem);
                            tabItemFactory.setInBackground(keyPressed);
                            DispatcherHelper.dispatchNaviEvent(tabItemFactory);
                        }
                    });
    }

    void update(TechId experimentID, double falseDiscoveryRate,
            AggregateFunction aggregateFunction, String treatmentTypeCode,
            boolean aggregateOriginal, List<AbundanceColumnDefinition> definitions)
    {
        criteria = new ListProteinByExperimentCriteria();
        criteria.setExperimentID(experimentID);
        criteria.setFalseDiscoveryRate(falseDiscoveryRate);
        criteria.setAggregateFunction(aggregateFunction);
        criteria.setTreatmentTypeCode(treatmentTypeCode);
        criteria.setAggregateOriginal(aggregateOriginal);
        abundanceColumnDefinitions = definitions;
        refresh(postRefreshCallback, true);
    }

    void setPostRefreshCallback(IDataRefreshCallback postRefreshCallback)
    {
        this.postRefreshCallback = postRefreshCallback;
    }

    @Override
    protected IColumnDefinitionKind<ProteinInfo>[] getStaticColumnsDefinition()
    {
        return ProteinColDefKind.values();
    }

    @Override
    protected ColumnDefsAndConfigs<ProteinInfo> createColumnsDefinition()
    {
        ColumnDefsAndConfigs<ProteinInfo> definitions = super.createColumnsDefinition();
        List<IColumnDefinitionUI<ProteinInfo>> columns =
                new ArrayList<IColumnDefinitionUI<ProteinInfo>>();
        List<String> abundanceColumnIDs = new ArrayList<String>();
        for (AbundanceColumnDefinition definition : abundanceColumnDefinitions)
        {
            String header = definition.getSampleCode();
            Map<String, String> properties = new HashMap<String, String>();
            properties.put(ABUNDANCE_PROPERTY_KEY, header);
            List<Treatment> treatments = definition.getTreatments();
            if (treatments.isEmpty() == false)
            {
                header = "";
                String delim = "";
                for (Treatment treatment : treatments)
                {
                    header += delim + treatment;
                    delim = ", ";
                    properties.put(treatment.getTypeCode(), treatment.getValue());
                }
            }
            final long sampleID = definition.getID();
            IColumnDefinitionUI<ProteinInfo> columnDefinition =
                    new InternalAbundanceColumnDefinition(header, properties, 100, false, sampleID);
            abundanceColumnIDs.add(columnDefinition.getIdentifier());
            columns.add(columnDefinition);
        }
        definitions.addColumns(columns);
        definitions.setGridCellRendererFor(ProteinColDefKind.ACCESSION_NUMBER.id(),
                createInternalLinkCellRenderer());
        RealNumberRenderer renderer =
                new RealNumberRenderer(viewContext.getDisplaySettingsManager()
                        .getRealNumberFormatingParameters());
        for (String abundanceColumneID : abundanceColumnIDs)
        {
            definitions.setGridCellRendererFor(abundanceColumneID, renderer);
        }
        definitions.setGridCellRendererFor(ProteinColDefKind.COVERAGE.id(), renderer);
        return definitions;
    }

    @Override
    protected BaseEntityModel<ProteinInfo> createModel(GridRowModel<ProteinInfo> entity)
    {
        Set<IColumnDefinition<ProteinInfo>> columnDefs = createColumnsDefinition().getColumnDefs();
        return new BaseEntityModel<ProteinInfo>(entity,
                new ArrayList<IColumnDefinition<ProteinInfo>>(columnDefs));
    }

    @Override
    protected List<IColumnDefinition<ProteinInfo>> getInitialFilters()
    {
        return asColumnFilters(new ProteinColDefKind[]
            { ProteinColDefKind.DESCRIPTION });
    }

    @Override
    protected void listEntities(DefaultResultSetConfig<String, ProteinInfo> resultSetConfig,
            AbstractAsyncCallback<ResultSet<ProteinInfo>> callback)
    {
        if (criteria != null)
        {
            criteria.copyPagingConfig(resultSetConfig);
            specificViewContext.getService().listProteinsByExperiment(criteria, callback);
        }
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<ProteinInfo> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        specificViewContext.getService().prepareExportProteins(exportCriteria, callback);
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        toolbar.update();
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.createOrDelete(ObjectKind.DATA_SET),
                    DatabaseModificationKind.edit(ObjectKind.SAMPLE) };
    }

}
