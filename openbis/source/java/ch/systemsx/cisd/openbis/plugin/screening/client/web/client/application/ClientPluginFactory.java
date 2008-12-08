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

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IClientPluginFactory;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IExperimentViewClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ISampleViewClientPlugin;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DefaultTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DummyComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ITabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.ViewerTabItem;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleType;
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

    private IExperimentViewClientPlugin experimentViewClientPlugin;

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
        return Collections.singleton(SampleTypeCode.MASTER_PLATE.getCode());
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
            final ScreeningSampleViewer sampleViewer =
                    new ScreeningSampleViewer(getViewContext(), sampleIdentifier);
            return new ViewerTabItem(sampleIdentifier, sampleViewer);
        }

        public final Widget createRegistrationForSampleType(final SampleType sampleTypeCode)
        {
            return new DummyComponent();
        }

        public final Widget createBatchRegistrationForSampleType(SampleType sampleType)
        {
            return new DummyComponent();
        }
    }

    private final static class ExperimentViewClientPlugin implements IExperimentViewClientPlugin
    {

        //
        // IExperimentViewClientPlugin
        //

        public final ITabItem createExperimentViewer(final String experimentIdentifier)
        {
            final DummyComponent experimentViewer = new DummyComponent();
            return new DefaultTabItem(experimentIdentifier, experimentViewer);
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
        return Collections.emptySet();
    }
}
