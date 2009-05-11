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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.action;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.user.LoggedUserMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.user.ChangeUserHomeGroupDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;

/**
 * An {@link IDelegatedAction} that opens a {@link ChangeUserHomeGroupDialog}.
 * 
 * @author Piotr Buczek
 */
public class ChangeHomeGroupAction implements IDelegatedAction
{
    private final IViewContext<?> viewContext;

    private final LoggedUserMenu menu;

    public ChangeHomeGroupAction(final IViewContext<?> viewContext, LoggedUserMenu menu)
    {
        this.viewContext = viewContext;
        this.menu = menu;
    }

    public void execute()
    {
        ChangeUserHomeGroupDialog dialog =
                new ChangeUserHomeGroupDialog(viewContext, new IDelegatedAction()
                    {
                        public void execute()
                        {
                            menu.refreshTitle();
                        }
                    });
        dialog.show();
    }
}
