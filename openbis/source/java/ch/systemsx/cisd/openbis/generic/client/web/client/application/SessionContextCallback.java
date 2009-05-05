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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener.OpenEntityDetailsTabAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.exception.UserFailureException;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * Callback class which handles return value
 * {@link ICommonClientService#tryToGetCurrentSessionContext()}.
 * 
 * @author Franz-Josef Elmer
 */
public final class SessionContextCallback extends AbstractAsyncCallback<SessionContext>
{
    SessionContextCallback(final CommonViewContext viewContext)
    {
        super(viewContext);
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
            openInitialTab();
        }
    }

    /** parameter key used to open an initial tab */
    private static final String INITIAL_TAB_OPEN_KEY = "show";

    private static final String PARAM_VALUE_SEPARATOR = "/";

    /** opens an initial tab if a parameter is specified in URL */
    private final void openInitialTab()
    {
        String paramValueOrNull = viewContext.getModel().getUrlParams().get(INITIAL_TAB_OPEN_KEY);
        if (paramValueOrNull != null)
        {
            try
            {
                String paramParts[] = paramValueOrNull.split(PARAM_VALUE_SEPARATOR);
                assert paramParts.length == 2 : "There should be only two parts.";

                final String entityKindPart = paramParts[0];
                final String identifierPart = paramParts[1];

                final EntityKind entityKind = EntityKind.valueOf(entityKindPart);
                final String identifier = identifierPart;

                openEntityDetailsTab(entityKind, identifier);
            } catch (Throwable exception)
            {
                throw new UserFailureException("Invalid URL parameter.");
                // TODO 2009-05-05, Piotr Buczek: show InfoBox (cannot add it anywhere now)
                // InfoBox infoBox = new InfoBox();
                // infoBox.displayError("Invalid URL parameter.");
            }
        }
    }

    private void openEntityDetailsTab(EntityKind entityKind, String identifier)
    {
        viewContext.getCommonService().getEntityInformationHolder(entityKind, identifier,
                new OpenEntityDetailsTabCallback(viewContext));

    }

    private final class OpenEntityDetailsTabCallback extends
            AbstractAsyncCallback<IEntityInformationHolder>
    {

        private OpenEntityDetailsTabCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Opens the tab with <var>result</var> entity details.
         */
        @Override
        protected final void process(final IEntityInformationHolder result)
        {
            new OpenEntityDetailsTabAction(result, viewContext).execute();
        }
    }

}