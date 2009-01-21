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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application;

import java.util.Set;

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.shared.EntityType;
import ch.systemsx.cisd.openbis.generic.client.shared.ExperimentType;
import ch.systemsx.cisd.openbis.generic.client.shared.IIdentifierHolder;
import ch.systemsx.cisd.openbis.generic.client.shared.SampleType;
import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItemFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ViewerTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.ClientPluginAdapter;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.plugin.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment.GenericExperimentViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleBatchRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleRegistrationForm;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer;

/**
 * {@link IClientPluginFactory} implementation for <i>Generic</i> technology.
 * 
 * @author Christian Ribeaud
 */
public final class ClientPluginFactory extends
        AbstractClientPluginFactory<IGenericClientServiceAsync>
{
    public ClientPluginFactory(final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    protected final IViewContext<IGenericClientServiceAsync> createViewContext(
            final IViewContext<ICommonClientServiceAsync> originalViewContext)
    {
        return new GenericViewContext(originalViewContext);
    }

    //
    // IClientPluginFactory
    //

    @SuppressWarnings("unchecked")
    public final <T extends EntityType, I extends IIdentifierHolder> IClientPlugin<T, I> createClientPlugin(
            EntityKind entityKind)
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

    public final Set<String> getEntityTypeCodes(EntityKind entityKind)
    {
        throw new UnsupportedOperationException(
                "Generic plugin factory supports every sample type.");
    }

    //
    // Helper classes
    //

    private final class SampleClientPlugin implements IClientPlugin<SampleType, IIdentifierHolder>
    {

        //
        // IViewClientPlugin
        //

        public ITabItemFactory createEntityViewer(final IIdentifierHolder sample)
        {
            final String identifier = sample.getIdentifier();
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final GenericSampleViewer sampleViewer =
                                new GenericSampleViewer(getViewContext(), identifier);
                        return new ViewerTabItem(identifier, sampleViewer, false);
                    }

                    public String getId()
                    {
                        return GenericSampleViewer.createId(identifier);
                    }
                };
        }

        public final Widget createRegistrationForEntityType(final SampleType sampleType)
        {
            return new GenericSampleRegistrationForm(getViewContext(), sampleType);
        }

        public final Widget createBatchRegistrationForEntityType(final SampleType sampleType)
        {
            return new GenericSampleBatchRegistrationForm(getViewContext(), sampleType);
        }
    }

    private final class ExperimentClientPlugin extends
            ClientPluginAdapter<ExperimentType, IIdentifierHolder>
    {

        //
        // IViewClientPlugin
        //

        @Override
        public final ITabItemFactory createEntityViewer(final IIdentifierHolder identifiable)
        {
            final String identifier = identifiable.getIdentifier();
            return new ITabItemFactory()
                {
                    public ITabItem create()
                    {
                        final GenericExperimentViewer experimentViewer =
                                new GenericExperimentViewer(getViewContext(), identifier);
                        return new ViewerTabItem(identifier, experimentViewer, false);
                    }

                    public String getId()
                    {
                        return GenericExperimentViewer.createId(identifier);
                    }
                };
        }

        @Override
        public Widget createRegistrationForEntityType(ExperimentType entityType)
        {
            return new GenericExperimentRegistrationForm(getViewContext(), entityType);
        }
    }
}
