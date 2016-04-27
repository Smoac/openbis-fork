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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application;

import java.util.Arrays;
import java.util.List;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.TypedTableGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.ICellListenerAndLinkGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.IDataRefreshCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DefaultResultSetConfig;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TableExportCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.TypedTableResultSet;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ListProteinByExperimentCriteria;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.dto.ProteinBrowserColumnIDs;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AbundanceColumnDefinition;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.AggregateFunction;
import ch.systemsx.cisd.openbis.plugin.proteomics.shared.basic.dto.ProteinInfo;

/**
 * @author Franz-Josef Elmer
 */
class ProteinByExperimentBrowserGrid extends TypedTableGrid<ProteinInfo>
{
    private static final String PREFIX = GenericConstants.ID_PREFIX
            + "protein-by-experiment-browser";

    // browser consists of the grid and additional toolbars (paging, filtering)
    public static final String BROWSER_ID = PREFIX + "_main";

    public static final String GRID_ID = PREFIX + TypedTableGrid.GRID_POSTFIX;

    private final IViewContext<IPhosphoNetXClientServiceAsync> specificViewContext;

    private final ProteinByExperimentBrowerToolBar toolbar;

    private ListProteinByExperimentCriteria criteria;

    private IDataRefreshCallback postRefreshCallback = new IDataRefreshCallback()
        {
            @Override
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
                @Override
                public void update(Set<DatabaseModificationKind> observedModifications)
                {
                    disposableBrowerGrid.update(observedModifications);
                    summaryGrid.update(observedModifications);
                }

                @Override
                public DatabaseModificationKind[] getRelevantModifications()
                {
                    return disposableBrowerGrid.getRelevantModifications();
                }

                @Override
                public Component getComponent()
                {
                    return container;
                }

                @Override
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
            final IViewContext<IPhosphoNetXClientServiceAsync> viewContext, final Experiment experiment)
    {
        super(viewContext.getCommonViewContext(), BROWSER_ID, true,
                PhosphoNetXDisplayTypeIDGenerator.PROTEIN_BY_EXPERIMENT_BROWSER_GRID);
        specificViewContext = viewContext;
        toolbar = new ProteinByExperimentBrowerToolBar(viewContext, experiment);
        toolbar.setBrowserGrid(this);
        registerListenerAndLinkGenerator(ProteinBrowserColumnIDs.ACCESSION_NUMBER,
                new ICellListenerAndLinkGenerator<ProteinInfo>()
                    {
                        @Override
                        public void handle(TableModelRowWithObject<ProteinInfo> rowItem,
                                boolean keyPressed)
                        {
                            AbstractTabItemFactory tabItemFactory =
                                    ProteinViewer.createTabItemFactory(viewContext,
                                            toolbar.getExperimentOrNull(),
                                            rowItem.getObjectOrNull());
                            tabItemFactory.setInBackground(keyPressed);
                            DispatcherHelper.dispatchNaviEvent(tabItemFactory);
                        }

                        @Override
                        public String tryGetLink(ProteinInfo entity,
                                ISerializableComparable comparableValue)
                        {
                            return ProteinViewLocatorResolver.createLink(experiment, entity);
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
        refresh(postRefreshCallback, true);
    }

    void setPostRefreshCallback(IDataRefreshCallback postRefreshCallback)
    {
        this.postRefreshCallback = postRefreshCallback;
    }

    @Override
    protected String translateColumnIdToDictionaryKey(String columnID)
    {
        return columnID.toLowerCase();
    }

    @Override
    protected List<String> getColumnIdsOfFilters()
    {
        return Arrays.asList(ProteinBrowserColumnIDs.PROTEIN_DESCRIPTION);
    }

    @Override
    protected void listTableRows(
            DefaultResultSetConfig<String, TableModelRowWithObject<ProteinInfo>> resultSetConfig,
            AbstractAsyncCallback<TypedTableResultSet<ProteinInfo>> callback)
    {
        if (criteria == null)
        {
            criteria = toolbar.getCriteria();
        }
        criteria.copyPagingConfig(resultSetConfig);
        specificViewContext.getService().listProteinsByExperiment(criteria, callback);
    }

    @Override
    protected void prepareExportEntities(TableExportCriteria<TableModelRowWithObject<ProteinInfo>> exportCriteria,
            AbstractAsyncCallback<String> callback)
    {
        specificViewContext.getService().prepareExportProteins(exportCriteria, callback);
    }

    @Override
    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        toolbar.update();
    }

    @Override
    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
        { DatabaseModificationKind.createOrDelete(ObjectKind.DATA_SET),
                DatabaseModificationKind.edit(ObjectKind.SAMPLE) };
    }

}
