/*
 * Copyright 2010 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.plugin.query.client.web.client.application;

import java.util.Collections;
import java.util.List;

import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.plugin.demo.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module.QueryModuleDatabaseMenuItem;

/**
 * @author Piotr Buczek
 */
public class QueryModule implements IModule
{
    public static final String ID = GenericConstants.ID_PREFIX;

    private final IViewContext<IQueryClientServiceAsync> viewContext;

    // If after initialization is finished this is still null it means that DB was not configured
    // and this module shouldn't provide any functionality.
    private String databaseLabelOrNull;

    QueryModule(IViewContext<IQueryClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
    }

    public String getName()
    {
        return viewContext.getMessage(Dict.MODULE_MENU_TITLE);
    }

    public void initialize(AsyncCallback<Void> callback)
    {
        viewContext.getService().tryToGetQueryDatabaseLabel(
                new DatabaseLabelCallback(viewContext, callback));
    }

    public List<? extends MenuItem> getMenuItems()
    {
        if (databaseLabelOrNull == null)
        {
            return Collections.emptyList();
        } else
        {
            return Collections.singletonList(new QueryModuleDatabaseMenuItem(viewContext,
                    databaseLabelOrNull));
        }
    }

    private final class DatabaseLabelCallback extends AbstractAsyncCallback<String>
    {
        private final AsyncCallback<Void> delegate;

        public DatabaseLabelCallback(final IViewContext<?> viewContext,
                final AsyncCallback<Void> delegate)
        {
            super(viewContext);
            this.delegate = delegate;
        }

        @Override
        protected void process(String result)
        {
            databaseLabelOrNull = result;
            delegate.onSuccess(null);
        }

        @Override
        public void finishOnFailure(Throwable caught)
        {
            delegate.onFailure(caught);
        }

    }

}
