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

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Controller for creating pages. 
 *
 * @author Franz-Josef Elmer
 */
class PageController implements IPageController
{
    private ViewContext viewContext;
    
    final void setViewContext(ViewContext viewContext)
    {
        this.viewContext = viewContext;
    }

    /**
     * This method clears <code>RootPanel</code> adds the specified page.
     */
    private final void setPage(Widget page)
    {
        final RootPanel rootPanel = RootPanel.get();
        rootPanel.clear();
        rootPanel.add(page);
    }
    
    public final void createLoginPage()
    {
        setPage(new LoginPage(viewContext));
    }

    public final void createMainPage()
    {
        // TODO 2008-01-21, Christian Ribeaud: Make something more useful here.
        setPage(new Label("Welcome " + viewContext.getModel().getUser().getEmail()));
    }
    
}
