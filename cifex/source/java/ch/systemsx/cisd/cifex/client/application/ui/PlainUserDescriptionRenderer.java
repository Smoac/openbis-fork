/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application.ui;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.BasicUserInfoDTO;

/**
 * A <code>Renderer</code> implementation that create a user description, which is either the full
 * name, or, if not available, user code.
 * 
 * @author Bernd Rinn
 */
public final class PlainUserDescriptionRenderer implements GridCellRenderer<ModelData>
{

    /** The unique instance of <code>PlainUserDescriptionRenderer</code>. */
    public final static PlainUserDescriptionRenderer PLAIN_USER_DESCRIPTION_RENDERER =
            new PlainUserDescriptionRenderer();

    private PlainUserDescriptionRenderer()
    {
        // Can not be instantiated.
    }
    
    public Object render(ModelData model, String property, ColumnData config,
            int rowIndex, int colIndex, ListStore<ModelData> store,
            Grid<ModelData> grid)
    {
        final BasicUserInfoDTO user = (BasicUserInfoDTO) model.get(property);
        return StringUtils.defaultIfBlank(getDescription(user), Constants.TABLE_NULL_VALUE);
    }

    String getDescription(BasicUserInfoDTO user)
    {
        if (user == null)
        {
            return null;
        }
        final String fullName = user.getUserFullName();
        final String userCode = user.getUserCode();
        final String userDescription = StringUtils.isBlank(fullName) ? userCode : fullName;
        if (StringUtils.isBlank(userDescription))
        {
            return Constants.TABLE_NULL_VALUE;
        }
        return userDescription;
    }

}
