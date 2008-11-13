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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample_browser;

import com.extjs.gxt.ui.client.widget.PagingToolBar;

/**
 * A small {@link PagingToolBar} extension which does not contain the <i>Refresh</i> button.
 * 
 * @author Christian Ribeaud
 */
public final class PagingToolBarWithoutRefresh extends PagingToolBar
{

    public PagingToolBarWithoutRefresh(int pageSize)
    {
        super(pageSize);
    }

    //
    // PagingToolBar
    //

    @Override
    protected final void afterRender()
    {
        final int refreshIndex = toolBar.indexOf(refresh);
        if (refreshIndex > -1)
        {
            toolBar.remove(refresh);
        }
        super.afterRender();
    }
}
