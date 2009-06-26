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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DisplaySettings;

/**
 * An {@link IDelegatedAction} that causes user logout.
 * 
 * @author Piotr Buczek
 */
public class LogoutAction implements IDelegatedAction
{
    private final IViewContext<ICommonClientServiceAsync> viewContext;

    public LogoutAction(final IViewContext<ICommonClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
    }

    public void execute()
    {
        DisplaySettings displaySettings =
                viewContext.getModel().getSessionContext().getDisplaySettings();
        viewContext.getService().logout(displaySettings, new LogoutCallback(viewContext));
    }

    public final class LogoutCallback extends AbstractAsyncCallback<Void>
    {
        LogoutCallback(IViewContext<ICommonClientServiceAsync> viewContext)
        {
            super(viewContext);
        }

        @Override
        public final void process(final Void result)
        {
            viewContext.getPageController().reload(true);
            GWTUtils.setAllowConfirmOnExit(false);
        }
    }
}