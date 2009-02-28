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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.util;

import junit.framework.Assert;

import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.AbstractBrowserGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.BrowserGridPagingToolBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.testframework.GWTTestUtil;

/**
 * Utility methods to test subclasses of {@link AbstractBrowserGrid}
 * 
 * @author Tomasz Pylak
 */
public class GridTestUtils
{
    /** simulates pressing the refresh button for the currently opened grid */
    public static final void pressRefreshGridButton()
    {
        final Button refresh =
                (Button) GWTTestUtil.getWidgetWithID(BrowserGridPagingToolBar.REFRESH_BUTTON_ID);
        Assert.assertTrue(refresh.isEnabled());
        GWTTestUtil.clickButtonWithID(BrowserGridPagingToolBar.REFRESH_BUTTON_ID);
    }
}
