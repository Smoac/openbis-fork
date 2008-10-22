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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ISampleViewClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyGrid;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.PropertyValueRenderers;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.amc.AbstractDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.application.SampleTypeCode;

/**
 * {@link IClientPluginFactory} implementation for <i>Generic</i> technology.
 * 
 * @author Christian Ribeaud
 */
public final class ClientPluginFactory extends
        AbstractClientPluginFactory<IGenericClientServiceAsync>
{
    private ISampleViewClientPlugin sampleViewClientPlugin;

    public ClientPluginFactory(final IViewContext<IGenericClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    protected final IViewContext<IGenericClientServiceAsync> createViewContext(
            final IViewContext<IGenericClientServiceAsync> originalViewContext)
    {
        return originalViewContext;
    }

    //
    // IClientPluginFactory
    //

    public final ISampleViewClientPlugin createViewClientForSampleType(final String sampleTypeCode)
    {
        if (sampleViewClientPlugin == null)
        {
            sampleViewClientPlugin = new SampleViewClientPlugin();
        }
        return sampleViewClientPlugin;
    }

    public final Set<String> getSampleTypeCodes()
    {
        return Collections.singleton(SampleTypeCode.CONTROL_LAYOUT.getCode());
    }

    //
    // Helper classes
    //

    private final class SampleViewClientPlugin implements ISampleViewClientPlugin
    {

        SampleInfoCallback sampleInfoCallback;

        //
        // ISampleViewClientPlugin
        //

        public final void viewSample(final String sampleIdentifier)
        {
            final IViewContext<IGenericClientServiceAsync> viewContext = getViewContext();
            if (sampleInfoCallback != null)
            {
                sampleInfoCallback.destroy();
            }
            sampleInfoCallback = new SampleInfoCallback(viewContext);
            viewContext.getService().getSampleInfo(sampleIdentifier, sampleInfoCallback);
        }
    }

    private final static class SampleInfoCallback extends AbstractAsyncCallback<Sample>
    {
        private Dialog dialog;

        private SampleInfoCallback(final IViewContext<?> viewContext)
        {
            super(viewContext);
        }

        final void destroy()
        {
            if (dialog != null)
            {
                dialog.close();
            }
        }

        //
        // AbstractAsyncCallback
        //

        @Override
        protected final void process(final Sample result)
        {
            final String title = result.getCode();
            dialog = new GenericSampleViewer(title, viewContext, result);
            dialog.show();
        }
    }

    private final static class GenericSampleViewer extends AbstractDialog
    {
        private final Sample sample;

        private final IMessageProvider messageProvider;

        private GenericSampleViewer(final String heading, final IMessageProvider messageProvider,
                final Sample sample)
        {
            super(heading);
            this.sample = sample;
            this.messageProvider = messageProvider;
            addWidget();
        }

        private final static Map<String, Object> createProperties(
                final IMessageProvider messageProvider, final Sample sample)
        {
            final Map<String, Object> properties = new LinkedHashMap<String, Object>();
            properties.put(messageProvider.getMessage("sample"), sample);
            properties.put(messageProvider.getMessage("sample_type"), sample.getSampleType());
            properties.put(messageProvider.getMessage("registrator"), sample.getRegistrator());
            properties.put(messageProvider.getMessage("registration_date"), sample
                    .getRegistrationDate());
            return properties;
        }

        //
        // AbstractDialog
        //

        @Override
        public final Widget getWidget()
        {
            final Map<String, Object> properties = createProperties(messageProvider, sample);
            final PropertyGrid propertyGrid = new PropertyGrid(messageProvider, properties.size());
            propertyGrid.registerPropertyValueRenderer(SampleType.class, PropertyValueRenderers
                    .getSampleTypePropertyValueRenderer(messageProvider));
            propertyGrid.registerPropertyValueRenderer(Sample.class, PropertyValueRenderers
                    .getSamplePropertyValueRenderer(messageProvider));
            propertyGrid.setProperties(properties);
            return propertyGrid;
        }
    }

}
