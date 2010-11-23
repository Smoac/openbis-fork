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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ActionContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.ClientPluginAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractRegistrationForm;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.columns.framework.LinkExtractor;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolderWithPermId;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentEditForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentViewer;

/**
 * @author Franz-Josef Elmer
 */
public class ClientPluginFactory extends AbstractClientPluginFactory<ViewContext>
{

    public ClientPluginFactory(IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    @Override
    protected ViewContext createViewContext(
            IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        return new ViewContext(originalViewContext);
    }

    @Override
    public IModule maybeCreateModule()
    {
        return new PhosphoNetXModule(getViewContext());
    }

    public Set<String> getEntityTypeCodes(EntityKind entityKind)
    {
        if (entityKind == EntityKind.EXPERIMENT)
        {
            return new HashSet<String>(Arrays.asList("MS_SEARCH", "MS_QUANTIFICATION"));
        }
        return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    public <T extends BasicEntityType, I extends IIdAndCodeHolder> IClientPlugin<T, I> createClientPlugin(
            EntityKind entityKind)
    {
        if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new ExperimentClientPlugin();
        }
        throw new UnsupportedOperationException("IClientPlugin for entity kind '" + entityKind
                + "' not implemented yet.");
    }

    //
    // Helper classes
    //

    private final class ExperimentClientPlugin extends
            ClientPluginAdapter<ExperimentType, IIdAndCodeHolder>
    {
        //
        // IViewClientPlugin
        //

        @Override
        public final AbstractTabItemFactory createEntityViewer(
                final IEntityInformationHolderWithPermId entity)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DatabaseModificationAwareComponent experimentViewer =
                                ExperimentViewer.create(getViewContext(), entity.getEntityType(),
                                        entity);
                        return DefaultTabItem.create(getTabTitle(), experimentViewer,
                                getViewContext(), false);
                    }

                    @Override
                    public String getId()
                    {
                        return GenericExperimentViewer.createId(entity);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT,
                                HelpPageAction.VIEW);
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getViewerTitle(Dict.EXPERIMENT, entity);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return LinkExtractor.tryExtract(entity);
                    }
                };
        }

        @Override
        public DatabaseModificationAwareWidget createRegistrationForEntityType(
                ExperimentType entityType, ActionContext context)
        {
            GenericExperimentRegistrationForm form =
                    new GenericExperimentRegistrationForm(getGenericViewContext(), context,
                            entityType);
            return new DatabaseModificationAwareWidget(form, form);
        }

        @Override
        public AbstractTabItemFactory createEntityEditor(final IIdAndCodeHolder identifiable)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        DatabaseModificationAwareComponent component =
                                GenericExperimentEditForm.create(getGenericViewContext(),
                                        identifiable);
                        return DefaultTabItem.create(getTabTitle(), component, getViewContext(),
                                true);
                    }

                    @Override
                    public String getId()
                    {
                        return GenericExperimentEditForm.createId(identifiable,
                                EntityKind.EXPERIMENT);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT,
                                HelpPageAction.EDIT);
                    }

                    @Override
                    public String getTabTitle()
                    {
                        return getEditorTitle(Dict.EXPERIMENT, identifiable);
                    }

                    @Override
                    public String tryGetLink()
                    {
                        return null;
                    }
                };
        }

        private String getViewerTitle(final String entityKindDictKey,
                final IIdAndCodeHolder identifiable)
        {
            return AbstractViewer.getTitle(getViewContext(), entityKindDictKey, identifiable);
        }

        private String getEditorTitle(final String entityKindDictKey,
                final IIdAndCodeHolder identifiable)
        {
            return AbstractRegistrationForm.getEditTitle(getViewContext(), entityKindDictKey,
                    identifiable);
        }

        private IViewContext<IGenericClientServiceAsync> getGenericViewContext()
        {
            return new GenericViewContext(getViewContext().getCommonViewContext());
        }
    }

}
