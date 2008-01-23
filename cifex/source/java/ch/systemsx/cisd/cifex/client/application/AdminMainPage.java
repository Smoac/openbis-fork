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

import com.gwtext.client.widgets.layout.ContentPanel;

import ch.systemsx.cisd.cifex.client.application.ui.AdminToolbarPanel;

/**
 * 
 *
 * @author Franz-Josef Elmer
 */
class AdminMainPage extends AbstractMainPage
{
    AdminMainPage(ViewContext context)
    {
        super(context);
    }

    protected ContentPanel createToolbarPanel()
    {
        return new AdminToolbarPanel(context);
    }

}
