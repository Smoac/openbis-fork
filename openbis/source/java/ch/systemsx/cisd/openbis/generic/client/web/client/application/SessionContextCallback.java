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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.IEditableEntity;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SessionContext;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataSetType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityProperty;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityTypePropertyType;

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

    /** opens an initial tab if a parameter is specified in URL */
    private final void openInitialTab()
    {
        String paramValueOrNull = viewContext.getModel().getUrlParams().get(INITIAL_TAB_OPEN_KEY);
        if (paramValueOrNull != null)
        {
            final EntityKind entityKind = EntityKind.DATA_SET;
            final String dataSetIdentifier = paramValueOrNull;
            DataSetType dataSetType = new DataSetType();
            dataSetType.setCode("");
            ITabItemFactory tabView;
            final IClientPluginFactory clientPluginFactory =
                    viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                            dataSetType);
            final IClientPlugin<EntityType, EntityTypePropertyType<EntityType>, EntityProperty<EntityType, EntityTypePropertyType<EntityType>>, IIdentifierHolder, IEditableEntity<EntityType, EntityTypePropertyType<EntityType>, EntityProperty<EntityType, EntityTypePropertyType<EntityType>>>> createClientPlugin =
                    clientPluginFactory.createClientPlugin(entityKind);
            tabView = createClientPlugin.createEntityViewer(new IIdentifierHolder()
                {
                    public String getIdentifier()
                    {
                        return dataSetIdentifier;
                    }
                });
            DispatcherHelper.dispatchNaviEvent(tabView);
        }
    }

}