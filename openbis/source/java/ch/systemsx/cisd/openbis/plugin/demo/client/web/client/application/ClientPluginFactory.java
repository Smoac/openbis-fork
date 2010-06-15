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

package ch.systemsx.cisd.openbis.plugin.demo.client.web.client.application;

import java.util.Collections;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.AbstractTabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DummyComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier.HelpPageDomain;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.ClientPluginAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleRegistrationForm;

/**
 * {@link IClientPluginFactory} implementation for <i>demo</i> plugin.
 * <p>
 * Currently, this implementation only runs for a sample of type SampleTypeCode#CELL_PLATE.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ClientPluginFactory extends AbstractClientPluginFactory<DemoViewContext>
{

    public ClientPluginFactory(final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    protected final DemoViewContext createViewContext(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        return new DemoViewContext(originalViewContext);
    }

    //
    // IClientPluginFactory
    //

    public final Set<String> getEntityTypeCodes(final EntityKind entityKind)
    {
        if (entityKind == EntityKind.SAMPLE)
        {
            return Collections.singleton("DEMO_PLUGIN");
        }
        return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    public <T extends BasicEntityType, I extends IIdAndCodeHolder> IClientPlugin<T, I> createClientPlugin(
            final EntityKind entityKind)
    {
        if (EntityKind.EXPERIMENT.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new ExperimentClientPlugin();
        }
        if (EntityKind.SAMPLE.equals(entityKind))
        {
            return (IClientPlugin<T, I>) new SampleClientPlugin();
        }
        throw new UnsupportedOperationException("IClientPlugin for entity kind '" + entityKind
                + "' not implemented yet.");
    }

    //
    // Helper classes
    //

    private final class SampleClientPlugin implements IClientPlugin<SampleType, IIdAndCodeHolder>
    {
        //
        // IViewClientPlugin
        //

        public final AbstractTabItemFactory createEntityViewer(final BasicEntityType sampleType,
                final IIdAndCodeHolder identifiable)
        {
            final TechId sampleId = TechId.create(identifiable);
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        final DemoSampleViewer sampleViewer =
                                new DemoSampleViewer(getViewContext(), sampleId);
                        return DefaultTabItem.createUnaware(identifiable.getCode(), sampleViewer,
                                false);
                    }

                    @Override
                    public String getId()
                    {
                        return DemoSampleViewer.createId(sampleId);
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.VIEW);
                    }
                };
        }

        public final DatabaseModificationAwareWidget createRegistrationForEntityType(
                final SampleType sampleType)
        {
            GenericSampleRegistrationForm form =
                    new GenericSampleRegistrationForm(new GenericViewContext(getViewContext()
                            .getCommonViewContext()), sampleType);
            return new DatabaseModificationAwareWidget(form, form);
        }

        public final Widget createBatchRegistrationForEntityType(final SampleType sampleType)
        {
            return new DummyComponent();
        }

        public final Widget createBatchUpdateForEntityType(final SampleType sampleType)
        {
            return new DummyComponent();
        }

        public AbstractTabItemFactory createEntityEditor(final IIdAndCodeHolder identifiable)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        return createDummyTab(identifiable.getCode());
                    }

                    @Override
                    public String getId()
                    {
                        return DummyComponent.ID;
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.SAMPLE, HelpPageAction.EDIT);
                    }
                };
        }

    }

    private final static class ExperimentClientPlugin extends
            ClientPluginAdapter<ExperimentType, IIdAndCodeHolder>
    {

        //
        // IViewClientPlugin
        //

        @Override
        public final AbstractTabItemFactory createEntityViewer(
                final BasicEntityType experimentType, final IIdAndCodeHolder identifiable)
        {
            return new AbstractTabItemFactory()
                {
                    @Override
                    public ITabItem create()
                    {
                        return createDummyTab(identifiable.getCode());
                    }

                    @Override
                    public String getId()
                    {
                        return DummyComponent.ID;
                    }

                    @Override
                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return new HelpPageIdentifier(HelpPageDomain.EXPERIMENT,
                                HelpPageAction.VIEW);
                    }
                };
        }
    }

    private static ITabItem createDummyTab(final String identifier)
    {
        Component component = new DummyComponent();
        return DefaultTabItem.createUnaware(identifier, component, false);
    }

    @Override
    public IModule maybeCreateModule()
    {
        // uncomment to test DemoModule
        // return new DemoModule(getViewContext());
        return null;
    }
}
