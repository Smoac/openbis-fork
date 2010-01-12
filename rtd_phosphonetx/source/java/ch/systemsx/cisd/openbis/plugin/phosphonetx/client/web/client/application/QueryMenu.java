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

package ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.application;

import com.extjs.gxt.ui.client.widget.menu.Menu;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.IActionMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * @author Franz-Josef Elmer
 */
public class QueryMenu extends TopMenuItem
{
    public static final String ID = GenericConstants.ID_PREFIX + "-phosphonetx-";

    private static enum ActionMenuKind implements IActionMenuItem
    {
        ALL_PROTEINS_OF_AN_EXPERIMENT()
        {
            @Override
            IDisposableComponent createComponent(
                    IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
            {
                return ProteinByExperimentBrowserGrid.create(viewContext);
            }
        },
        ALL_RAW_DATA_SAMPLES()
        {
            @Override
            IDisposableComponent createComponent(
                    IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
            {
                return RawDataSampleGrid.create(viewContext);
            }
        };

        public String getMenuId()
        {
            return ID + "_" + this.name();
        }

        public String getMenuText(IMessageProvider messageProvider)
        {
            return messageProvider.getMessage(this.name() + "_menu_item");
        }

        String getTabLabelKey()
        {
            return this.name() + "_tab_label";
        }

        ActionMenu createActionMenu(final IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
        {
            return new ActionMenu(this, viewContext, new ITabItemFactory()
                {
                    public String getId()
                    {
                        return ID + getTabLabelKey();
                    }

                    public ITabItem create()
                    {
                        String menuItemText = viewContext.getMessage(getTabLabelKey());
                        return DefaultTabItem.create(menuItemText, createComponent(viewContext),
                                viewContext);
                    }

                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return HelpPageIdentifier.createSpecific(getMenuText(viewContext));
                    }
                });
        }

        abstract IDisposableComponent createComponent(
                IViewContext<IPhosphoNetXClientServiceAsync> viewContext);
    }

    public QueryMenu(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        super(viewContext.getMessage(Dict.QUERY_MENU_TITLE));
        setIconStyle(TopMenuItem.ICON_STYLE);

        Menu submenu = new Menu();
        for (ActionMenuKind actionMenuKind : ActionMenuKind.values())
        {
            submenu.add(actionMenuKind.createActionMenu(viewContext));
        }
        setMenu(submenu);
    }
}
