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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.framework;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ColumnModelEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * Manager of {@link DisplaySettings}. The manager itself is stateless. It only changes the wrapped
 * {@link DisplaySettings} object. The two attributes of this class are assumed to be de facto
 * singletons. The display setting manager will be created as often as components to be managed are
 * created.
 * 
 * @author Franz-Josef Elmer
 */
public class DisplaySettingsManager
{
    private final DisplaySettings displaySettings;

    private final IUpdater updater;

    /**
     * Creates an instance for the specified display settings.
     */
    public DisplaySettingsManager(DisplaySettings displaySettings, IUpdater updater)
    {
        if (displaySettings == null)
        {
            throw new IllegalArgumentException("Unspecified display manager.");
        }
        this.displaySettings = displaySettings;
        this.updater = updater;
    }

    /**
     * This method is deprecated and will be removed when AbstractGridBrowser will be used
     * everywhere.
     */
    public <M extends ModelData> void prepareGrid(final String displayTypeID, final Grid<M> grid)
    {
        final ArrayList<String> emptyFilters = new ArrayList<String>();
        IDisplaySettingsGetter displaySettingsUpdater = new IDisplaySettingsGetter()
            {
                public ColumnModel getColumnModel()
                {
                    return grid.getColumnModel();
                }

                public List<String> getFilteredColumnIds()
                {
                    return emptyFilters;
                }
            };
        GridDisplaySettings settings =
                tryApplySettings(displayTypeID, grid.getColumnModel(), emptyFilters);
        if (settings != null)
        {
            grid.reconfigure(grid.getStore(), new ColumnModel(settings.getColumnConfigs()));
        }
        registerGridSettingsChangesListener(displayTypeID, displaySettingsUpdater);
    }

    /**
     * Register listeners which monitors all the column configuration changes and makes them
     * persistent.
     */
    public void registerGridSettingsChangesListener(final String displayTypeID,
            final IDisplaySettingsGetter grid)
    {

        Listener<ColumnModelEvent> listener = new Listener<ColumnModelEvent>()
            {
                public void handleEvent(ColumnModelEvent event)
                {
                    storeSettings(displayTypeID, grid);
                }
            };
        ColumnModel columnModel = grid.getColumnModel();
        columnModel.addListener(Events.HiddenChange, listener);
        columnModel.addListener(Events.WidthChange, listener);
        columnModel.addListener(AppEvents.ColumnMove, listener);
    }

    /**
     * Synchronizes the initial grid display settings with the settings stored at the specified
     * display type ID. Stored settings (if any) override the current settings.
     */
    public GridDisplaySettings tryApplySettings(String displayTypeID, ColumnModel columnModel,
            List<String> filteredColumnIds)
    {
        List<ColumnSetting> columnSettings = displaySettings.getColumnSettings().get(displayTypeID);
        if (columnSettings == null)
        {
            return null;
        }
        return tryApplySettings(columnSettings, columnModel, filteredColumnIds);
    }

    public static class GridDisplaySettings
    {
        List<ColumnConfig> columnConfigs;

        List<String> filteredColumnIds;

        public GridDisplaySettings(List<ColumnConfig> columnConfigs, List<String> filteredColumnIds)
        {
            this.columnConfigs = columnConfigs;
            this.filteredColumnIds = filteredColumnIds;
        }

        public List<ColumnConfig> getColumnConfigs()
        {
            return columnConfigs;
        }

        public void setColumnConfigs(List<ColumnConfig> columnConfigs)
        {
            this.columnConfigs = columnConfigs;
        }

        public List<String> getFilteredColumnIds()
        {
            return filteredColumnIds;
        }

        public void setFilteredColumnIds(List<String> filteredColumnIds)
        {
            this.filteredColumnIds = filteredColumnIds;
        }
    }

    /**
     * Update grid columns and filters by applying the specified settings.
     * 
     * @param filteredColumnIds used only to check if the user settings are different form the
     *            defaults
     */
    private static GridDisplaySettings tryApplySettings(List<ColumnSetting> columnSettings,
            ColumnModel columnModel, List<String> filteredColumnIds)
    {
        boolean refreshNeeded = false;
        List<ColumnConfig> newColumnConfigList = new ArrayList<ColumnConfig>();
        Set<Integer> indices = new HashSet<Integer>();
        List<String> newFilteredColumnIds = new ArrayList<String>();
        for (int i = 0; i < columnSettings.size(); i++)
        {
            ColumnSetting columnSetting = columnSettings.get(i);
            // update column using the settings stored for it
            String columnID = columnSetting.getColumnID();
            int index = columnModel.getIndexById(columnID);
            if (index >= 0)
            {
                if (i != index)
                {
                    refreshNeeded = true;
                }
                indices.add(index);
                ColumnConfig columnConfig = columnModel.getColumn(index);
                boolean hidden = columnSetting.isHidden();
                if (columnConfig.isHidden() != hidden)
                {
                    columnConfig.setHidden(hidden);
                    refreshNeeded = true;
                }
                int width = columnSetting.getWidth();
                if (columnConfig.getWidth() != width)
                {
                    columnConfig.setWidth(width);
                    refreshNeeded = true;
                }
                newColumnConfigList.add(columnConfig);
                if (columnSetting.hasFilter())
                {
                    newFilteredColumnIds.add(columnID);
                }
            }
        }
        // add columns for which no settings were stored at the end
        for (int i = 0; i < columnModel.getColumnCount(); i++)
        {
            if (indices.contains(i) == false)
            {
                newColumnConfigList.add(columnModel.getColumn(i));
            }
        }
        if (newFilteredColumnIds.equals(filteredColumnIds) == false)
        {
            refreshNeeded = true;
        }
        if (refreshNeeded)
        {
            return new GridDisplaySettings(newColumnConfigList, newFilteredColumnIds);
        } else
        {
            return null;
        }
    }

    public void storeSettings(final String displayTypeID, final IDisplaySettingsGetter grid)
    {
        storeSettings(displayTypeID, grid.getColumnModel(), grid.getFilteredColumnIds());
    }

    public void storeSectionSettings(String displayTypeID, boolean display)
    {
        displaySettings.getSectionSettings().put(displayTypeID, display);
        updater.update();
    }

    private void storeSettings(String displayTypeID, ColumnModel columnModel,
            List<String> filteredColumnIds)
    {
        List<ColumnSetting> columnSettings = createColumnsSettings(columnModel, filteredColumnIds);
        displaySettings.getColumnSettings().put(displayTypeID, columnSettings);
        updater.update();
    }

    private static List<ColumnSetting> createColumnsSettings(ColumnModel columnModel,
            List<String> filteredColumnIdsList)
    {
        Set<String> filteredColumnIds = new HashSet<String>(filteredColumnIdsList);
        List<ColumnSetting> columnSettings = new ArrayList<ColumnSetting>();
        for (int i = 0; i < columnModel.getColumnCount(); i++)
        {
            ColumnConfig columnConfig = columnModel.getColumn(i);
            ColumnSetting columnSetting = new ColumnSetting();
            columnSetting.setColumnID(columnConfig.getId());
            columnSetting.setHidden(columnConfig.isHidden());
            columnSetting.setWidth(columnConfig.getWidth());
            boolean hasFilter = filteredColumnIds.contains(columnConfig.getId());
            columnSetting.setHasFilter(hasFilter);
            columnSettings.add(columnSetting);
        }
        return columnSettings;
    }
}
