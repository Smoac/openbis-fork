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

package ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.locator.ViewLocator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ActionMenu;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.ITabActionMenuItemDefinition;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.menu.TabActionMenuItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithIdentifier;
import ch.systemsx.cisd.openbis.generic.shared.basic.URLMethodWithParameters;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.IPhosphoNetXClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.proteomics.client.web.client.application.wizard.MsInjectionSampleAnnotationWizard;

/**
 * @author Franz-Josef Elmer
 */
public class PhosphoNetXModule implements IModule
{
    public static final String ID = GenericConstants.ID_PREFIX + "-proteomics-";

    private final IViewContext<IPhosphoNetXClientServiceAsync> viewContext;

    public PhosphoNetXModule(IViewContext<IPhosphoNetXClientServiceAsync> viewContext)
    {
        this.viewContext = viewContext;
    }

    @Override
    public List<? extends MenuItem> getMenuItems()
    {
        ActionMenu msInjectionSampleAnnotatingMenuItem = TabActionMenuItemFactory.createActionMenu(viewContext, ID,
                new ITabActionMenuItemDefinition<IPhosphoNetXClientServiceAsync>()
                    {

                        @Override
                        public String getName()
                        {
                            return "ANNOTATE_MS_INJECTION_SAMPLES";
                        }

                        @Override
                        public String getHelpPageTitle()
                        {
                            return "Wizard for annotation MS INJECTION " + viewContext.getMessage(
                                    ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict.SAMPLES).toLowerCase();
                        }

                        @Override
                        public DatabaseModificationAwareComponent createComponent(
                                IViewContext<IPhosphoNetXClientServiceAsync> context)
                        {
                            return DatabaseModificationAwareComponent.create(new MsInjectionSampleAnnotationWizard(context));
                        }

                        @Override
                        public String tryGetLink()
                        {
                            URLMethodWithParameters url = new URLMethodWithParameters("");
                            url.addParameter(ViewLocator.ACTION_PARAMETER, getName());
                            return url.toString().substring(1);
                        }
                    });
        ActionMenu msInjectionSampleBrowserMenuItem = TabActionMenuItemFactory.createActionMenu(viewContext, ID,
                new ITabActionMenuItemDefinition<IPhosphoNetXClientServiceAsync>()
                    {

                        @Override
                        public String getName()
                        {
                            return "ALL_RAW_DATA_SAMPLES";
                        }

                        @Override
                        public String getHelpPageTitle()
                        {
                            return "MS INJECTION Data Overview";
                        }

                        @Override
                        public DatabaseModificationAwareComponent createComponent(
                                IViewContext<IPhosphoNetXClientServiceAsync> context)
                        {
                            return RawDataSampleGrid.create(context);
                        }

                        @Override
                        public String tryGetLink()
                        {
                            return null;
                        }
                    });
        return Arrays.asList(msInjectionSampleAnnotatingMenuItem, msInjectionSampleBrowserMenuItem);
    }

    @Override
    public String getName()
    {
        return viewContext.getMessage(Dict.QUERY_MENU_TITLE);
    }

    @Override
    public void initialize(AsyncCallback<Void> callback)
    {
        callback.onSuccess(null);
    }

    @Override
    public Collection<? extends DisposableTabContent> getSections(
            IEntityInformationHolderWithIdentifier entity)
    {
        return new ArrayList<DisposableTabContent>();
    }

}
