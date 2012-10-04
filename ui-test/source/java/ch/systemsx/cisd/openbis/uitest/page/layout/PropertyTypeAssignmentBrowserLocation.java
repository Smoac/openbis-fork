/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.uitest.page.layout;

import ch.systemsx.cisd.openbis.uitest.infra.application.GuiApplicationRunner;
import ch.systemsx.cisd.openbis.uitest.page.menu.AdminMenu;
import ch.systemsx.cisd.openbis.uitest.page.menu.TopBar;
import ch.systemsx.cisd.openbis.uitest.page.tab.PropertyTypeAssignmentBrowser;

/**
 * @author anttil
 */
public class PropertyTypeAssignmentBrowserLocation implements
        Location<PropertyTypeAssignmentBrowser>
{

    @Override
    public void moveTo(GuiApplicationRunner openbis)
    {
        openbis.load(TopBar.class).admin();
        openbis.load(AdminMenu.class).browsePropertyTypeAssignments();
    }

    @Override
    public String getTabName()
    {
        return "Property Type Assignments";
    }

    @Override
    public Class<PropertyTypeAssignmentBrowser> getPage()
    {
        return PropertyTypeAssignmentBrowser.class;
    }
}