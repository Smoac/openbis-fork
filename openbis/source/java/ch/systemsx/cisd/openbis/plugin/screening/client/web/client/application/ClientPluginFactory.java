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
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Dialog;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ISampleViewClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.GenericSampleViewer;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.SampleTypeCode;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * {@link IClientPluginFactory} implementation for <i>Screening</i> technology.
 * <p>
 * Currently, this implementation only runs for a sample of type {@link SampleTypeCode#CELL_PLATE}.
 * </p>
 * 
 * @author Christian Ribeaud
 */
public final class ClientPluginFactory extends
        AbstractClientPluginFactory<IScreeningClientServiceAsync>
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
    protected final IViewContext<IScreeningClientServiceAsync> createViewContext(
            final IViewContext<IGenericClientServiceAsync> originalViewContext)
    {
        return new ScreeningViewContext(originalViewContext);
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
        return Collections.singleton(SampleTypeCode.CELL_PLATE.getCode());
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
            final IViewContext<IScreeningClientServiceAsync> viewContext = getViewContext();
            if (sampleInfoCallback != null)
            {
                sampleInfoCallback.destroy();
            }
            sampleInfoCallback = new SampleInfoCallback(viewContext);
            viewContext.getService().getSampleInfo(sampleIdentifier, sampleInfoCallback);
        }

    }

    private final static class SampleInfoCallback extends AbstractAsyncCallback<SampleGeneration>
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
        protected final void process(final SampleGeneration result)
        {
            final String title = result.getGenerator().getCode();
            dialog = new GenericSampleViewer(title, viewContext.getMessageProvider(), result);
            dialog.show();
        }
    }
}
