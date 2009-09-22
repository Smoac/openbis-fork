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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import com.extjs.gxt.ui.client.mvc.Dispatcher;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientService;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AppEvents;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;

/**
 * Callback class which handles return value
 * {@link ICommonClientService#tryToGetCurrentSessionContext()}.
 * 
 * @author     Franz-Josef Elmer
 */
public final class SessionContextCallback extends AbstractAsyncCallback<SessionContext>
{
    private final IDelegatedAction afterInitAction;

    /**
     * @param afterInitAction action executed after application init (if user is logged in)
     */
    SessionContextCallback(final CommonViewContext viewContext,
            final IDelegatedAction afterInitAction)
    {
        super(viewContext);
        this.afterInitAction = afterInitAction;

    }

    //
    // AbstractAsyncCallback
    //

    @Override
    public final void process(final SessionContext sessionContext)
    {
        final Dispatcher dispatcher = Dispatcher.get();
        if (sessionContext == null)
        {
            dispatcher.dispatch(AppEvents.LOGIN);
        } else
        {
            viewContext.getModel().setSessionContext(sessionContext);
            dispatcher.dispatch(AppEvents.INIT);
            afterInitAction.execute();
            GWTUtils.setAllowConfirmOnExit(true);
        }
    }
}
