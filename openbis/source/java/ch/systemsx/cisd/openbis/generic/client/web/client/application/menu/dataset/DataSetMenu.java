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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.dataset;

import com.extjs.gxt.ui.client.widget.menu.Menu;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ComponentProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TopMenuItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

/**
 * Data Set top menu.
 * 
 * @author Piotr Buczek
 */
public class DataSetMenu extends TopMenuItem
{

    public DataSetMenu(final IViewContext<ICommonClientServiceAsync> viewContext,
            ComponentProvider componentProvider)
    {
        super(viewContext.getMessage(Dict.MENU_DATA_SET));

        Menu submenu = new Menu();
        IMessageProvider messageProvider = viewContext;
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.DATA_SET_MENU_SEARCH, messageProvider,
                componentProvider.getDataSetSearch()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.DATA_SET_MENU_TYPES, messageProvider,
                componentProvider.getDataSetTypeBrowser()));
        submenu.add(new ActionMenu(TopMenu.ActionMenuKind.DATA_SET_MENU_MASS_UPDATE,
                messageProvider, componentProvider.getDataSetBatchUpdate()));
        boolean cifexConfigured =
                StringUtils
                        .isBlank(viewContext.getModel().getApplicationInfo().getCifexRecipient()) == false
                        && StringUtils.isBlank(viewContext.getModel().getApplicationInfo()
                                .getCIFEXURL()) == false;
        if (cifexConfigured)
        {
            submenu.add(new ActionMenu(TopMenu.ActionMenuKind.DATA_SET_MENU_UPLOAD,
                    messageProvider, componentProvider.getDataSetUploadTab(null)));
        }
        setMenu(submenu);
    }
}
