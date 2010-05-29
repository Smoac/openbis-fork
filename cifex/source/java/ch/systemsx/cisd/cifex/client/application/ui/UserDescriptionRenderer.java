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

import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.BasicUserInfoDTO;

/**
 * A <code>Renderer</code> implementation that create an email anchor element with inner text being
 * a user description, which is either the full name, or, if not available, user code.
 * 
 * @author Bernd Rinn
 */
public final class UserDescriptionRenderer implements
        IGridCellRendererNonPlainText<ModelData>
{

    /** The unique instance of <code>UserDescriptionRenderer</code>. */
    public final static UserDescriptionRenderer USER_DESCRIPTION_RENDERER =
            new UserDescriptionRenderer();

    private UserDescriptionRenderer()
    {
        // Can not be instantiated.
    }

    public Object render(ModelData model, String property, ColumnData config,
            int rowIndex, int colIndex, ListStore<ModelData> store,
            Grid<ModelData> grid)
    {
        final BasicUserInfoDTO user = (BasicUserInfoDTO) model.get(property);
        if (user == null)
        {
            return Constants.TABLE_NULL_VALUE;
        }
        final String userDescription =
                PlainUserDescriptionRenderer.PLAIN_USER_DESCRIPTION_RENDERER.getDescription(user);
        final String email = user.getEmail();
        if (StringUtils.isBlank(userDescription))
        {
            return Constants.TABLE_NULL_VALUE;
        }
        if (StringUtils.isBlank(email))
        {
            return userDescription;
        } else
        {
            return DOMUtils.createEmailAnchor(email, userDescription);
        }
    }

    public GridCellRenderer<ModelData> getPlainTextRenderer()
    {
        return PlainUserDescriptionRenderer.PLAIN_USER_DESCRIPTION_RENDERER;
    }

}
