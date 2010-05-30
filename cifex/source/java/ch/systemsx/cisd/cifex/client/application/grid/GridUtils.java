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

package ch.systemsx.cisd.cifex.client.application.grid;

import java.util.List;

import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;

import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.model.AbstractUserGridModel;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * Helper to create different types of grids.
 * 
 * @author Tomasz Pylak
 */
public class GridUtils
{
    /** creates a grid with specified users */
    public static GridWidget<UserGridModel> createUserGrid(final List<UserInfoDTO> users,
            ViewContext viewContext)
    {
        List<UserGridModel> data = UserGridModel.convert(viewContext, users);
        List<ColumnConfig> columnConfigs = UserGridModel.getColumnConfigs();
        List<AbstractFilterField<UserGridModel>> filterItems =
                AbstractUserGridModel.createFilterItems(columnConfigs);

        GridWidget<UserGridModel> gridWidget = GridWidget.create(columnConfigs, data, filterItems);
        return gridWidget;
    }
}
