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

import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplaySettingsManager;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.PluginTaskDescriptionModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.ColumnConfigFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStore;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;

/**
 * Implements {@link DatastoreServiceDescription} listing functionality.
 * 
 * @author Piotr Buczek
 */
class DataStoreServicesGrid extends ContentPanel
{
    private static final String ID = GenericConstants.ID_PREFIX + "datastore-services-view";

    private static final String TABLE_ID = ID + "_table";

    private static final String HEADING = "Data Store Services";

    private static final String LABEL = "Name";

    private static final String DATA_SET_TYPES = "Handled Data Set Types";

    private final DisplaySettingsManager displaySettingsManager;

    private final List<Listener<SelectionChangedEvent<ModelData>>> gridSelectionChangedListeners =
            new ArrayList<Listener<SelectionChangedEvent<ModelData>>>();

    // null if not fetched asynchronously yet
    private List<DatastoreServiceDescription> servicesOrNull;

    private Grid<PluginTaskDescriptionModel> grid;

    public DataStoreServicesGrid(IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.displaySettingsManager = viewContext.getDisplaySettingsManager();
        setLayout(new FitLayout());

        setWidth(4 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH);
        // - setting auto width causes some grid resize problems
        // setAutoWidth(true);
        setHeight(200);

        setHeaderVisible(true);
        setHeading(HEADING);
        setId(ID);

        add(new Text("data loading..."));
    }

    public void display(final List<DatastoreServiceDescription> fetchedServices)
    {
        this.servicesOrNull = fetchedServices;
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
        codesNameColumnConfig.setWidth(2 * ColumnConfigFactory.DEFAULT_COLUMN_WIDTH - 30);
        configs.add(codesNameColumnConfig);

        final ColumnModel cm = new ColumnModel(configs);
        ListStore<PluginTaskDescriptionModel> store = new ListStore<PluginTaskDescriptionModel>();
        setStoreContent(servicesOrNull, store);

        grid = new Grid<PluginTaskDescriptionModel>(store, cm);
        grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        grid.getSelectionModel().addListener(Events.SelectionChange,
                createGridSelectionChangeListener());
        grid.setId(TABLE_ID);
        grid.setBorders(true);
        // - setting auto width causes some grid resize problems
        // - setting height does not help because form height is fixed
        // grid.setAutoExpandColumn(cm.getColumnId(cm.getColumnCount() - 1));
        // grid.setAutoWidth(true);

        String displayTypeID = DisplayTypeIDGenerator.PLUGIN_TASKS_BROWSER_GRID.createID();
        displaySettingsManager.prepareGrid(displayTypeID, grid);

        add(grid);
        layout();

    }

    public void filterServicesByDataStore(DataStore dataStoreOrNull)
    {
        List<DatastoreServiceDescription> filteredPlugins = servicesOrNull;
        if (filteredPlugins != null)
        {
            if (dataStoreOrNull != null)
            {
                filteredPlugins = filterPlugins(dataStoreOrNull, filteredPlugins);
            }
            setStoreContent(filteredPlugins, grid.getStore());
        }
    }

    private static List<DatastoreServiceDescription> filterPlugins(DataStore dataStore,
            List<DatastoreServiceDescription> services)
    {
        List<DatastoreServiceDescription> result = new ArrayList<DatastoreServiceDescription>();
        for (DatastoreServiceDescription service : services)
        {
            if (service.getDatastoreCode().equals(dataStore.getCode()))
            {
                result.add(service);
            }
        }
        return result;
    }

    private static void setStoreContent(final List<DatastoreServiceDescription> plugins,
            ListStore<PluginTaskDescriptionModel> store)
    {
        store.removeAll();
        store.add(getPluginTaskModels(plugins));
        store.sort(ModelDataPropertyNames.LABEL, SortDir.ASC);
    }

    private static List<PluginTaskDescriptionModel> getPluginTaskModels(
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

    final void registerGridSelectionChangeListener(
            Listener<SelectionChangedEvent<ModelData>> listener)
    {
        gridSelectionChangedListeners.add(listener);
    }

    /** Creates grid selection change listener that handles all registered listeners. */
    private Listener<SelectionChangedEvent<ModelData>> createGridSelectionChangeListener()
    {
        return new Listener<SelectionChangedEvent<ModelData>>()
            {
                public void handleEvent(SelectionChangedEvent<ModelData> be)
                {
                    for (Listener<SelectionChangedEvent<ModelData>> listener : gridSelectionChangedListeners)
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
}
