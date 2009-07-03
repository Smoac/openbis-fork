package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.AdapterToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.PluginTaskDescriptionModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnFilter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginTaskDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.PluginTaskKind;
import ch.systemsx.cisd.openbis.plugin.demo.client.web.client.application.Dict;

/**
 * Implements {@link PluginTaskDescription} listing functionality.
 * 
 * @author Pitor Buczek
 */
public class PluginTasksView extends ContentPanel
{
    public static final String ID = GenericConstants.ID_PREFIX + "plugin_tasks-view";

    private static final String HEADING = "Plugin Tasks";

    private static final String LABEL = "Label";

    private static final String DATA_SET_TYPES = "Handled Data Set Types";

    public static final String ADD_BUTTON_ID = ID + "_add-button";

    public static final String TABLE_ID = ID + "_table";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final PluginTaskKind pluginTaskKind;

    private Grid<PluginTaskDescriptionModel> grid;

    public PluginTasksView(IViewContext<ICommonClientServiceAsync> viewContext,
            PluginTaskKind pluginTaskKind)
    {
        this.viewContext = viewContext;
        this.pluginTaskKind = pluginTaskKind;
        setLayout(new FitLayout());

        setWidth(400);
        setHeight(200);

        setHeaderVisible(true);
        setHeading(HEADING);
        setId(ID);
    }

    @Override
    protected void onRender(final Element parent, final int pos)
    {
        super.onRender(parent, pos);
        refresh();
    }

    private void display(final List<PluginTaskDescription> plugins)
    {
        removeAll();

        final List<ColumnConfig> configs = new ArrayList<ColumnConfig>();

        final ColumnConfig labelColumnConfig = new ColumnConfig();
        labelColumnConfig.setId(ModelDataPropertyNames.LABEL);
        labelColumnConfig.setHeader(LABEL);
        labelColumnConfig.setWidth(ColumnConfigFactory.DEFAULT_COLUMN_WIDTH);
        configs.add(labelColumnConfig);

        final ColumnConfig codesNameColumnConfig = new ColumnConfig();
        codesNameColumnConfig.setId(ModelDataPropertyNames.DATA_SET_TYPES);
        codesNameColumnConfig.setHeader(DATA_SET_TYPES);
        codesNameColumnConfig.setWidth(2 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH);
        configs.add(codesNameColumnConfig);

        final ColumnModel cm = new ColumnModel(configs);

        final ListStore<PluginTaskDescriptionModel> store =
                new ListStore<PluginTaskDescriptionModel>();
        store.add(getPluginTaskModels(plugins));

        final ContentPanel cp = new ContentPanel();

        // cp.setWidth(500);
        // cp.setHeight(500);

        cp.setBodyBorder(false);
        cp.setHeaderVisible(false);
        cp.setButtonAlign(HorizontalAlignment.CENTER);

        cp.setLayout(new FitLayout());
        // cp.setSize("90%", "90%");

        grid = new Grid<PluginTaskDescriptionModel>(store, cm);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.setId(TABLE_ID);
        grid.setBorders(true);
        GWTUtils.setAutoExpandOnLastVisibleColumn(grid);
        String displayTypeID =
                DisplayTypeIDGenerator.PLUGIN_TASKS_BROWSER_GRID.createID(null, null);
        viewContext.getDisplaySettingsManager().prepareGrid(displayTypeID, grid);
        cp.add(grid);

        final ToolBar toolBar = new ToolBar();
        toolBar.add(new LabelToolItem(viewContext.getMessage(Dict.FILTER)
                + GenericConstants.LABEL_SEPARATOR));
        toolBar.add(new AdapterToolItem(new ColumnFilter<PluginTaskDescriptionModel>(store,
                ModelDataPropertyNames.LABEL, LABEL)));
        toolBar.add(new AdapterToolItem(new ColumnFilter<PluginTaskDescriptionModel>(store,
                ModelDataPropertyNames.DATA_SET_TYPES, "Data Set Type")));
        cp.setBottomComponent(toolBar);

        add(cp);
        layout();

    }

    List<PluginTaskDescriptionModel> getPluginTaskModels(
            final List<PluginTaskDescription> pluginTasks)
    {
        final List<PluginTaskDescriptionModel> result = new ArrayList<PluginTaskDescriptionModel>();
        for (final PluginTaskDescription p : pluginTasks)
        {
            result.add(new PluginTaskDescriptionModel(p));
        }
        return result;
    }

    /**
     * Returns one selected item or null if nothing selected.
     */
    public final PluginTaskDescription tryGetSelectedItem()
    {
        List<PluginTaskDescriptionModel> model = grid.getSelectionModel().getSelectedItems();
        if (model.size() == 0)
        {
            return null;
        } else
        {
            assert model.size() == 1 : "more than one plugin selected";
            return model.get(0).getBaseObject();
        }
    }

    public void refresh()
    {
        removeAll();
        add(new Text("data loading..."));
        viewContext.getService().listPluginTaskDescriptions(pluginTaskKind,
                new ListPluginTaskDescriptionsCallback(viewContext));
    }

    //
    // Helper classes
    //

    public final class ListPluginTaskDescriptionsCallback extends
            AbstractAsyncCallback<List<PluginTaskDescription>>
    {
        private ListPluginTaskDescriptionsCallback(
                final IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        public final void process(final List<PluginTaskDescription> plugins)
        {
            for (PluginTaskDescription p : plugins)
            {
                System.err.println(p);
            }
            display(plugins);
        }
    }
}
