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

package ch.systemsx.cisd.cifex.client.application;

import com.gwtext.client.core.Ext;
import com.gwtext.client.widgets.layout.BorderLayout;
import com.gwtext.client.widgets.layout.ContentPanel;
import com.gwtext.client.widgets.layout.LayoutRegionConfig;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
abstract class AbstractMainPage extends BorderLayout
{
    private final static LayoutRegionConfig createCenterRegion()
    {
        LayoutRegionConfig center = new LayoutRegionConfig();
        center.setTitlebar(false);
        center.setAutoScroll(true);
        return center;
    }

    private final static LayoutRegionConfig createNorthRegion()
    {
        LayoutRegionConfig north = new LayoutRegionConfig();
        north.setSplit(false);
        north.setInitialSize(30);
        north.setTitlebar(false);
        north.setAutoScroll(false);
        return north;
    }

    protected final ViewContext context;

    AbstractMainPage(ViewContext context)
    {
        super("100%", "100%", createNorthRegion(), null, null, null, createCenterRegion());
        this.context = context;
        add(LayoutRegionConfig.NORTH, createToolbarPanel());
        add(LayoutRegionConfig.CENTER, createMainPanel());
    }
    
    protected ContentPanel createMainPanel()
    {
        return new ContentPanel(Ext.generateId());
    }
    
    protected abstract ContentPanel createToolbarPanel();
}
