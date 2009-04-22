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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.util;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ColumnSetting;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * Manager of {@link DisplaySettings}. The manager itself is stateless. It only changes the wrapped
 * {@link DisplaySettings} object.
 *
 * @author Franz-Josef Elmer
 */
public class DisplaySettingsManager
{
    private final DisplaySettings displaySettings;

    /**
     * Creates an instance for the specified display settings.
     */
    public DisplaySettingsManager(DisplaySettings displaySettings)
    {
        if (displaySettings == null)
        {
            throw new IllegalArgumentException("Unspecified display manager.");
        }
        this.displaySettings = displaySettings;
    }
    
    /**
     * Synchronizes the {@link ColumnModel} of the specified grid with the {@link ColumnSetting}s.
     * The grid ID is used to get the appropriated column settings. If there are no settings found
     * nothing will be done.
     */
    public void synchronizeColumnModel(Grid<?> grid)
    {
        List<ColumnSetting> columnSettings = displaySettings.getColumnSettings().get(grid.getId());
        if (columnSettings == null)
        {
            return;
        }
        ColumnModel columnModel = grid.getColumnModel();
        for (ColumnSetting columnSetting : columnSettings)
        {
            ColumnConfig columnConfig = columnModel.getColumnById(columnSetting.getColumnID());
            columnConfig.setHidden(columnSetting.isHidden());
        }
    }
    
    /**
     * Updates the column settings for the specified grid. The grid ID will be used to identify
     * its column settings in the method {@link #synchronizeColumnModel(Grid)}.
     */
    public void updateColumnSettings(Grid<?> grid)
    {
        ColumnModel columnModel = grid.getColumnModel();
        List<ColumnSetting> columnSettings = new ArrayList<ColumnSetting>();
        for (int i = 0; i < columnModel.getColumnCount(); i++)
        {
            ColumnSetting columnSetting = new ColumnSetting();
            columnSetting.setColumnID(columnModel.getColumnId(i));
            columnSetting.setHidden(columnModel.isHidden(i));
            columnSettings.add(columnSetting);
        }
        displaySettings.getColumnSettings().put(grid.getId(), columnSettings);
    }
}
