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
import java.util.Set;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareWidget;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DummyComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.help.HelpPageIdentifier;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IModule;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.phosphonetx.client.web.client.IPhosphoNetXClientServiceAsync;

/**
 * @author Franz-Josef Elmer
 */
public class ClientPluginFactory extends
        AbstractClientPluginFactory<IPhosphoNetXClientServiceAsync>
{

    public ClientPluginFactory(IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    @Override
    protected IViewContext<IPhosphoNetXClientServiceAsync> createViewContext(
            IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        return new ViewContext(originalViewContext);
    }

    @Override
    public IModule tryGetModule()
    {
        return new PhosphoNetXModule(getViewContext());
    }

    public Set<String> getEntityTypeCodes(EntityKind entityKind)
    {
        if (entityKind == EntityKind.SAMPLE)
        {
            return Collections.singleton("TEST");
        }
        return Collections.emptySet();
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityType, I extends IIdentifiable> IClientPlugin<T, I> createClientPlugin(
            EntityKind entityKind)
    {
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
                        return DefaultTabItem.createUnaware(identifiable.getCode(),
                                new DummyComponent("PhosphoNetX plugin coming soon."), false);
                    }

                    public String getId()
                    {
                        return "phosphonetx-viewer-" + sampleId;
                    }

                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return HelpPageIdentifier.createSpecific("PhosphoNetX Sample Viewer");
                    }
                };
        }

        public final DatabaseModificationAwareWidget createRegistrationForEntityType(
                final SampleType sampleTypeCode)
        {
            return DatabaseModificationAwareWidget.wrapUnaware(new DummyComponent(
                    "Creating of a sample for PhosphoNetX is coming soon."));
        }

        public final Widget createBatchRegistrationForEntityType(final SampleType sampleType)
        {
            return new DummyComponent();
        }

        public final Widget createBatchUpdateForEntityType(final SampleType sampleType)
        {
            return new DummyComponent();
        }

        public ITabItemFactory createEntityEditor(final IIdentifiable identifiable)
        {
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        return DefaultTabItem.createUnaware(identifiable.getCode(),
                                new DummyComponent(), false);
                    }

                    public String getId()
                    {
                        return DummyComponent.ID;
                    }

                    public HelpPageIdentifier getHelpPageIdentifier()
                    {
                        return HelpPageIdentifier.createSpecific("PhosphoNetX Sample Edition");
                    }
                };
        }

    }

}
