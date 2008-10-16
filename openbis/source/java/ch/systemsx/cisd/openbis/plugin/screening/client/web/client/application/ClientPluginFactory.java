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
import java.util.TreeSet;

import com.extjs.gxt.ui.client.widget.MessageBox;

import ch.systemsx.cisd.openbis.generic.client.web.client.IGenericClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ISampleViewClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * {@link IClientPluginFactory} implementation for <i>Screening</i> technology.
 * 
 * @author Christian Ribeaud
 */
public final class ClientPluginFactory extends
        AbstractClientPluginFactory<IScreeningClientServiceAsync>
{
    private final static Set<String> sampleTypes = createSampleTypes();

    public ClientPluginFactory(final IViewContext<IGenericClientServiceAsync> originalViewContext)
    {
        super(originalViewContext);
    }

    private final static Set<String> createSampleTypes()
    {
        final Set<String> set = new TreeSet<String>();
        for (final SampleTypeCode sampleTypeCode : SampleTypeCode.values())
        {
            if (sampleTypeCode.equals(SampleTypeCode.CONTROL_LAYOUT))
            {
                continue;
            }
            set.add(sampleTypeCode.getCode());
        }
        return set;
    }

    //
    // AbstractClientPluginFactory
    //

    @Override
    protected final IViewContext<IScreeningClientServiceAsync> createViewContext(
            final IViewContext<IGenericClientServiceAsync> originalViewContext)
    {
        return new ScreeningViewContext(originalViewContext.getImageBundle(), originalViewContext
                .getModel(), originalViewContext.getPageController());
    }

    //
    // IClientPluginFactory
    //

    public final ISampleViewClientPlugin createViewClientForSampleType(final String sampleTypeCode)
    {
        return new SampleViewClientPlugin();
    }

    public final Set<String> getSampleTypeCodes()
    {
        return Collections.unmodifiableSet(sampleTypes);
    }

    //
    // Helper classes
    //

    private final static class SampleViewClientPlugin implements ISampleViewClientPlugin
    {

        //
        // ISampleViewClientPlugin
        //

        public final void viewSample(final String sampleIdentifier)
        {
            MessageBox.alert("Screening", sampleIdentifier, null);
        }
    }
}
