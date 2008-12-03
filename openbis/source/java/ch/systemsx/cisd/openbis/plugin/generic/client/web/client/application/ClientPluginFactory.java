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

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IExperimentViewClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ISampleViewClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;
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
    private ISampleViewClientPlugin sampleViewClientPlugin;

    private IExperimentViewClientPlugin experimentViewClientPlugin;

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
        throw new UnsupportedOperationException(
                "Generic plugin factory supports every sample type.");
    }

    //
    // Helper classes
    //

    private final class SampleViewClientPlugin implements ISampleViewClientPlugin
    {

        //
        // ISampleViewClientPlugin
        //

        public final ITabItem createSampleViewer(final String sampleIdentifier)
        {
            final GenericSampleViewer sampleViewer =
                    new GenericSampleViewer(getViewContext(), sampleIdentifier);
            return new DefaultTabItem(sampleIdentifier, sampleViewer)
                {

                    //
                    // DefaultTabItem
                    //

                    @Override
                    public final void initialize()
                    {
                        sampleViewer.loadSampleInfo();
                    }
                };
        }

        public final Widget createRegistrationForSampleType(final SampleType sampleType)
        {
            return new GenericSampleRegistrationForm(getViewContext(), sampleType);
        }

        public final Widget createBatchRegistrationForSampleType(final SampleType sampleType)
        {
            return new GenericSampleBatchRegistrationForm(getViewContext(), sampleType);
        }
    }

    private final class ExperimentViewClientPlugin implements IExperimentViewClientPlugin
    {

        public final ITabItem createExperimentViewer(final String experimentIdentifier)
        {
            final GenericExperimentViewer experimentViewer =
                    new GenericExperimentViewer(getViewContext(), experimentIdentifier);
            return new DefaultTabItem(experimentIdentifier, experimentViewer)
                {

                    @Override
                    public final void initialize()
                    {
                        experimentViewer.loadExperimentInfo();
                    }
                };
        }

    }

    public IExperimentViewClientPlugin createViewClientForExperimentType(
            final String experimentTypeCode)
    {
        if (experimentViewClientPlugin == null)
        {
            experimentViewClientPlugin = new ExperimentViewClientPlugin();
        }
        return experimentViewClientPlugin;
    }

    public Set<String> getExperimentTypeCodes()
    {
        throw new UnsupportedOperationException(
                "Generic plugin factory supports every experiment type.");
    }
}
