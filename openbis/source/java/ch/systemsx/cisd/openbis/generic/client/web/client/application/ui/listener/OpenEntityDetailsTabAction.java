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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.listener;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DispatcherHelper;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;

/**
 * A {@link IDelegatedAction} that opens entity details tab.
 * 
 * @author Piotr Buczek
 */
public final class OpenEntityDetailsTabAction implements IDelegatedAction
{
    private final IEntityInformationHolder entity;

    private final IViewContext<?> viewContext;

    public OpenEntityDetailsTabAction(IEntityInformationHolder entity,
            final IViewContext<?> viewContext)
    {
        this.entity = entity;
        this.viewContext = viewContext;
    }

    public void execute()
    {
        final EntityKind entityKind = entity.getEntityKind();
        final IClientPluginFactory clientPluginFactory =
                viewContext.getClientPluginFactoryProvider().getClientPluginFactory(entityKind,
                        entity.getEntityType());
        final IClientPlugin<EntityType, IIdentifierHolder> createClientPlugin =
                clientPluginFactory.createClientPlugin(entityKind);
        final ITabItemFactory tabView = createClientPlugin.createEntityViewer(entity);

        DispatcherHelper.dispatchNaviEvent(tabView);
    }
}