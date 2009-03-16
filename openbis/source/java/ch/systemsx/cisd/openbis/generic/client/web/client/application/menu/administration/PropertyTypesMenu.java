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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.administration;

import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Property Types top submenu.
 * 
 * @author Piotr Buczek
 */
public class PropertyTypesMenu extends MenuItem
{

    public PropertyTypesMenu(IMessageProvider messageProvider, ComponentProvider componentProvider)
    {
        super("Property Type");
        setIconStyle("icon-menu-show");
        Menu menu = new Menu();
        menu.add(new ActionMenu(TopMenu.ActionMenuKind.PROPERTY_TYPES_MENU_BROWSE_PROPERTY_TYPES,
                "Browse Property Types", componentProvider.getPropertyTypeBrowser()));
        menu.add(new ActionMenu(TopMenu.ActionMenuKind.PROPERTY_TYPES_MENU_BROWSE_ASSIGNMENTS,
                "Browse Assignments", componentProvider.getPropertyTypeAssignmentBrowser()));
        menu.add(new ActionMenu(TopMenu.ActionMenuKind.PROPERTY_TYPES_MENU_NEW_PROPERTY_TYPES,
                "New Property Type", componentProvider.getPropertyTypeRegistration()));
        menu.add(new ActionMenu(
                TopMenu.ActionMenuKind.PROPERTY_TYPES_MENU_ASSIGN_TO_EXPERIMENT_TYPE,
                "Assign to Experiment Type", componentProvider
                        .getPropertyTypeExperimentTypeAssignmentForm()));
        menu.add(new ActionMenu(TopMenu.ActionMenuKind.PROPERTY_TYPES_MENU_ASSIGN_TO_SAMPLE_TYPE,
                "Assign to Sample Type", componentProvider
                        .getPropertyTypeSampleTypeAssignmentForm()));
        setSubMenu(menu);
    }
}