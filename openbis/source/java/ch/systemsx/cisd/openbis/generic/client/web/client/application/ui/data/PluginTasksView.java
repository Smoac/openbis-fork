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

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.PluginTaskDescriptionModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * Implements {@link DatastoreServiceDescription} listing functionality.
 * 
 * @author Pitor Buczek
 */
class PluginTasksView extends ContentPanel
{
    public static final String ID = GenericConstants.ID_PREFIX + "plugin_tasks-view";

    private static final String HEADING = "Data Store Services";

    private static final String LABEL = "Name";

    private static final String DATA_SET_TYPES = "Handled Data Set Types";

    public static final String ADD_BUTTON_ID = ID + "_add-button";

    public static final String TABLE_ID = ID + "_table";

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final List<Listener<SelectionEvent<ModelData>>> gridSelectionChangedListeners =
            new ArrayList<Listener<SelectionEvent<ModelData>>>();

    private final DataStoreServiceKind pluginTaskKind;

    private Grid<PluginTaskDescriptionModel> grid;

    public PluginTasksView(IViewContext<ICommonClientServiceAsync> viewContext,
            DataStoreServiceKind pluginTaskKind)
    {
        this.viewContext = viewContext;
        this.pluginTaskKind = pluginTaskKind;
        setLayout(new FitLayout());

        setWidth(4 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH);
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

    private void display(final List<DatastoreServiceDescription> plugins)
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
        codesNameColumnConfig.setWidth(3 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH - 15);
        configs.add(codesNameColumnConfig);

        final ColumnModel cm = new ColumnModel(configs);

        final ListStore<PluginTaskDescriptionModel> store =
                new ListStore<PluginTaskDescriptionModel>();
        store.add(getPluginTaskModels(plugins));

        final ContentPanel cp = new ContentPanel();

        cp.setBodyBorder(false);
        cp.setHeaderVisible(false);
        cp.setButtonAlign(HorizontalAlignment.CENTER);

        cp.setLayout(new FitLayout());

        grid = new Grid<PluginTaskDescriptionModel>(store, cm);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.getSelectionModel().addListener(Events.SelectionChange,
                createGridSelectionChangeListener());
        grid.setId(TABLE_ID);
        grid.setBorders(true);
        GWTUtils.setAutoExpandOnLastVisibleColumn(grid);
        String displayTypeID =
                DisplayTypeIDGenerator.PLUGIN_TASKS_BROWSER_GRID.createID(null, null);
        viewContext.getDisplaySettingsManager().prepareGrid(displayTypeID, grid);
        cp.add(grid);

        add(cp);
        layout();

    }

    List<PluginTaskDescriptionModel> getPluginTaskModels(
            final List<DatastoreServiceDescription> services)
    {
        final List<PluginTaskDescriptionModel> result = new ArrayList<PluginTaskDescriptionModel>();
        boolean multipleDataStoresPresent = multipleDataStoresPresent(services);
        for (final DatastoreServiceDescription p : services)
        {
            result.add(new PluginTaskDescriptionModel(p, multipleDataStoresPresent));
        }
        return result;
    }

    // do the srevices come form more than one datastore?
    private static boolean multipleDataStoresPresent(List<DatastoreServiceDescription> services)
    {
        if (services.size() == 0)
        {
            return false;
        }
        String dss = services.get(0).getDatastoreCode();
        for (DatastoreServiceDescription service : services)
        {
            if (dss.equals(service.getDatastoreCode()) == false)
            {
                return true;
            }
        }
        return false;
    }

    final void registerGridSelectionChangeListener(Listener<SelectionEvent<ModelData>> listener)
    {
        gridSelectionChangedListeners.add(listener);
    }

    /** Creates grid selection change listener that handles all registered listeners. */
    private Listener<SelectionEvent<ModelData>> createGridSelectionChangeListener()
    {
        return new Listener<SelectionEvent<ModelData>>()
            {
                public void handleEvent(SelectionEvent<ModelData> be)
                {
                    for (Listener<SelectionEvent<ModelData>> listener : gridSelectionChangedListeners)
                    {
                        listener.handleEvent(be);
                    }
                }
            };
    }

    /**
     * Returns one selected item or null if nothing selected.
     */
    public final DatastoreServiceDescription tryGetSelectedItem()
    {
        if (grid == null)
        {
            return null;
        }
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
        viewContext.getService().listDataStoreServices(pluginTaskKind,
                new ListPluginTaskDescriptionsCallback(viewContext));
    }

    //
    // Helper classes
    //

    public final class ListPluginTaskDescriptionsCallback extends
            AbstractAsyncCallback<List<DatastoreServiceDescription>>
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
        public final void process(final List<DatastoreServiceDescription> plugins)
        {
            display(plugins);
        }
    }
}
