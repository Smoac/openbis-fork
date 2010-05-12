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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.QueryType;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.IQueryClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module.AbstractQueryProviderToolbar;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module.QueryModuleDatabaseMenuItem;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module.QueryViewer;
import ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.module.RunCannedQueryToolbar;

/**
 * @author Piotr Buczek
 */
public class QueryModule implements IModule
{
    public static final String ID = GenericConstants.ID_PREFIX;

    private final IViewContext<IQueryClientServiceAsync> viewContext;

    // number of configured and initialized DBs
    // (0 means that this module shouldn't provide any functionality)
    private int databases;

    QueryModule(IViewContext<IQueryClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
    }

    public String getName()
    {
        return viewContext
                .getMessage(ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict.MODULE_MENU_TITLE);
    }

    public void initialize(AsyncCallback<Void> callback)
    {
        viewContext.getService().initDatabases(
                new DatabasesInitializationCallback(viewContext, callback));
    }

    public List<? extends MenuItem> getMenuItems()
    {
        if (databases == 0)
        {
            return Collections.emptyList();
        } else
        {
            return Collections.singletonList(new QueryModuleDatabaseMenuItem(viewContext));
        }
    }

    private final class DatabasesInitializationCallback extends AbstractAsyncCallback<Integer>
    {
        private final AsyncCallback<Void> delegate;

        public DatabasesInitializationCallback(final IViewContext<?> viewContext,
                final AsyncCallback<Void> delegate)
        {
            super(viewContext);
            this.delegate = delegate;
        }

        @Override
        protected void process(Integer result)
        {
            viewContext.log(" query database(s) configured:" + result);
            databases = result;
            delegate.onSuccess(null);
        }

        @Override
        public void finishOnFailure(Throwable caught)
        {
            delegate.onFailure(caught);
        }

    }

    public Collection<? extends DisposableSectionPanel> getExperimentSections(
            IEntityInformationHolderWithIdentifier entity)
    {
        ArrayList<DisposableSectionPanel> result = new ArrayList<DisposableSectionPanel>();
        final IViewContext<IQueryClientServiceAsync> queryModuleContext = viewContext;
        result.add(createExperimentSectionPanel(queryModuleContext, entity));
        return result;
    }

    private DisposableSectionPanel createExperimentSectionPanel(
            final IViewContext<IQueryClientServiceAsync> queryModuleContext,
            final IEntityInformationHolderWithIdentifier entity)
    {
        DisposableSectionPanel panel =
                new DisposableSectionPanel(
                        viewContext
                                .getMessage(ch.systemsx.cisd.openbis.plugin.query.client.web.client.application.Dict.MODULE_MENU_TITLE),
                        queryModuleContext)
                    {
                        @Override
                        protected IDisposableComponent createDisposableContent()
                        {
                            HashMap<String, String> parameters = new HashMap<String, String>();
                            parameters.put("_key", entity.getPermId());
                            AbstractQueryProviderToolbar toolbar =
                                    new RunCannedQueryToolbar(queryModuleContext, null, parameters,
                                            QueryType.EXPERIMENT);
                            final DatabaseModificationAwareComponent viewer =
                                    QueryViewer.create(queryModuleContext, toolbar);
                            return new IDisposableComponent()
                                {
                                    public void dispose()
                                    {// FIXME
                                    }

                                    public Component getComponent()
                                    {
                                        return viewer.get();
                                    }

                                    public DatabaseModificationKind[] getRelevantModifications()
                                    {
                                        return viewer.getRelevantModifications();
                                    }

                                    public void update(
                                            Set<DatabaseModificationKind> observedModifications)
                                    {
                                        viewer.update(observedModifications);
                                    }
                                };
                        }
                    };
        return panel;
    }
}
