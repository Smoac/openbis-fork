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

package ch.systemsx.cisd.cifex.client.application.ui;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.cifex.client.application.model.AbstractUserGridModel;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;

/**
 * A <code>Renderer</code> implementation that create an email anchor element with inner text
 * <code>value.toString()</code>.
 * 
 * @author Christian Ribeaud
 */
public final class EmailLinkRenderer implements IGridCellRendererNonPlainText<AbstractUserGridModel>
{

    /** The unique instance of <code>UserRenderer</code>. */
    public final static EmailLinkRenderer USER_RENDERER = new EmailLinkRenderer();

    private EmailLinkRenderer()
    {
        // Can not be instantiated.
    }

    public Object render(AbstractUserGridModel model, String property, ColumnData config,
            int rowIndex, int colIndex, ListStore<AbstractUserGridModel> store,
            Grid<AbstractUserGridModel> grid)
    {
        String value = String.valueOf(model.get(property));
        if (value == null)
        {
            return Constants.TABLE_NULL_VALUE;
        }
        return DOMUtils.createEmailAnchor(value, null);
    }

    public GridCellRenderer<AbstractUserGridModel> getPlainTextRenderer()
    {
        return null;
    }
}
