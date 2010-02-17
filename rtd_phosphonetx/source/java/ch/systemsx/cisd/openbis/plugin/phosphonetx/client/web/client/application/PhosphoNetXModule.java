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

import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ITabActionMenuItemDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TabActionMenuItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * @author Franz-Josef Elmer
 */
public class PhosphoNetXModule implements IModule
{
    public static final String ID = GenericConstants.ID_PREFIX + "-phosphonetx-";
    private final IViewContext<IPhosphoNetXClientServiceAsync> viewContext;

    public PhosphoNetXModule(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
    }

    public List<? extends MenuItem> getMenuItems()
    {
        return Collections.singletonList(TabActionMenuItemFactory.createActionMenu(viewContext, ID,
                new ITabActionMenuItemDefinition<IPhosphoNetXClientServiceAsync>()
                    {

                        public String getName()
                        {
                            return "ALL_RAW_DATA_SAMPLES";
                        }

                        public String getHelpPageTitle()
                        {
                            return "MS INJECTION Data Overview";
                        }

                        public DatabaseModificationAwareComponent createComponent(
                                IViewContext<IPhosphoNetXClientServiceAsync> context)
                        {
                            return RawDataSampleGrid.create(context);
                        }
                    }));
    }

    public String getName()
    {
        return viewContext.getMessage(Dict.QUERY_MENU_TITLE);
    }

    public void initialize(AsyncCallback<Void> callback)
    {
        callback.onSuccess(null);
    }

}
