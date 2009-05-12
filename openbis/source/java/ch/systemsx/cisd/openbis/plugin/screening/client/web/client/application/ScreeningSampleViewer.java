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

import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Sample;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.SampleGeneration;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.sample.GenericSampleViewer;
import ch.systemsx.cisd.openbis.plugin.screening.client.web.client.IScreeningClientServiceAsync;

/**
 * The <i>screening</i> sample viewer.
 * 
 * @author Christian Ribeaud
 */
public final class ScreeningSampleViewer extends AbstractViewer<IScreeningClientServiceAsync>
{
    private static final String PREFIX = "screening-sample-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final String sampleIdentifier;

    private Sample originalSample;

    public ScreeningSampleViewer(final IViewContext<IScreeningClientServiceAsync> viewContext,
            final String sampleIdentifier)
    {
        super(viewContext, "Sample " + sampleIdentifier, createId(sampleIdentifier));
        this.sampleIdentifier = sampleIdentifier;
        reloadData();
    }

    public static final String createId(String sampleIdentifier)
    {
        return ID_PREFIX + sampleIdentifier;
    }

    private final Widget createUI(final SampleGeneration sampleGeneration)
    {
        return GenericSampleViewer.createPropertyGrid(sampleIdentifier, sampleGeneration,
                viewContext);
    }

    /**
     * Load the sample information.
     */
    protected void reloadData()
    {
        SampleInfoCallback callback = new SampleInfoCallback(viewContext, this);
        viewContext.getService().getSampleInfo(sampleIdentifier, callback);
    }

    //
    // Helper classes
    //

    public final class SampleInfoCallback extends AbstractAsyncCallback<SampleGeneration>
    {
        private final ScreeningSampleViewer screeningSampleViewer;

        private SampleInfoCallback(final IViewContext<IScreeningClientServiceAsync> viewContext,
                ScreeningSampleViewer screeningSampleViewer)
        {
            super(viewContext);
            this.screeningSampleViewer = screeningSampleViewer;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Sets the {@link SampleGeneration} for this <var>generic</var> sample viewer.
         * <p>
         * This method triggers the whole <i>GUI</i> construction.
         * </p>
         */
        @Override
        protected final void process(final SampleGeneration result)
        {
            setOriginalSample(result.getGenerator());
            enableEdit(true);
            screeningSampleViewer.removeAll();
            screeningSampleViewer.add(screeningSampleViewer.createUI(result));
            screeningSampleViewer.layout();
        }
    }

    void setOriginalSample(Sample result)
    {
        this.originalSample = result;
    }

    @Override
    protected void showEntityEditor()
    {
        assert originalSample != null;
        showEntityEditor(viewContext, EntityKind.SAMPLE, originalSample.getSampleType(),
                originalSample);
    }
}