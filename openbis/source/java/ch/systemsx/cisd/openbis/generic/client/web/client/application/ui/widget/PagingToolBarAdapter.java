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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.widget.PagingToolBar;
import com.extjs.gxt.ui.client.widget.toolbar.ToolItem;

/**
 * Use this subclass instead of the {@link PagingToolBar}. It is compatible with our UI testing
 * framework. It also allows to remove the default refresh button.
 * 
 * @author Tomasz Pylak
 */
public class PagingToolBarAdapter extends PagingToolBar
{

    public PagingToolBarAdapter(int pageSize)
    {
        super(pageSize);
    }

    protected final void removeOriginalRefreshButton()
    {
        final int refreshIndex = toolBar.indexOf(refresh);
        if (refreshIndex > -1)
        {
            toolBar.remove(refresh);
        }
    }

    /** Exposes items of the toolbar. NOTE: use only for testing! */
    public List<ToolItem> getItems()
    {

        return toolBar != null ? toolBar.getItems() : new ArrayList<ToolItem>();
    }
}
