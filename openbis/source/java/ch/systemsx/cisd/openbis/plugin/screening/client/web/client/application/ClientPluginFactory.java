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

package ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DummyComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.ClientPluginAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.GenericViewContext;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.ScreeningConstants;

/**
 * {@link IClientPluginFactory} implementation for <i>screening</i> plugin.
 * <p>
 * Currently, this implementation only runs for a sample of type SampleTypeCode#CELL_PLATE.
 * </p>
 * 
 * @author Tomasz Pylak
 */
public final class ClientPluginFactory extends
        AbstractClientPluginFactory<IScreeningClientServiceAsync>
{

    public ClientPluginFactory(final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    protected final IViewContext<IScreeningClientServiceAsync> createViewContext(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        return new ScreeningViewContext(originalViewContext);
    }

    //
    // IClientPluginFactory
    //

    public final Set<String> getEntityTypeCodes(final EntityKind entityKind)
    {
        if (entityKind == EntityKind.SAMPLE)
        {
            Set<String> types = new HashSet<String>();
            types.add(ScreeningConstants.PLATE_PLUGIN_TYPE_CODE);
            types.add(ScreeningConstants.WELL_PLUGIN_TYPE_CODE);
            return types;
        }
        return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityType, I extends IIdentifiable> IClientPlugin<T, I> createClientPlugin(
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

    private final class SampleClientPlugin implements IClientPlugin<SampleType, IIdentifiable>
    {
        //
        // IViewClientPlugin
        //

        public final ITabItemFactory createEntityViewer(final IIdentifiable identifiable)
        {
            final TechId sampleId = TechId.create(identifiable);
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final ScreeningSampleViewer sampleViewer =
                                new ScreeningSampleViewer(getViewContext(), sampleId);
                        return DefaultTabItem.createUnaware(identifiable.getCode(), sampleViewer,
                                false);
                    }

                    public String getId()
                    {
                        return ScreeningSampleViewer.createId(sampleId);
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

        public ITabItemFactory createEntityEditor(final IIdentifiable identifiable)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        return createDummyTab(identifiable.getCode());
                    }

                    public String getId()
                    {
                        return DummyComponent.ID;
                    }
                };
        }

    }

    private final static class ExperimentClientPlugin extends
            ClientPluginAdapter<ExperimentType, IIdentifiable>
    {

        //
        // IViewClientPlugin
        //

        @Override
        public final ITabItemFactory createEntityViewer(final IIdentifiable identifiable)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        return createDummyTab(identifiable.getCode());
                    }

                    public String getId()
                    {
                        return DummyComponent.ID;
                    }
                };
        }
    }

    private static ITabItem createDummyTab(final String identifier)
    {
        Component component = new DummyComponent();
        return DefaultTabItem.createUnaware(identifier, component, false);
    }

    // @Override
    // public IModule tryGetModule()
    // {
    // return new ScreeningModule(getViewContext());
    // }
}
