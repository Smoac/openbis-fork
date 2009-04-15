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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AttachmentsSection;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> experiment viewer.
 * 
 * @author Izabela Adamczyk
 */
public final class GenericExperimentViewer extends AbstractViewer<IGenericClientServiceAsync>
{
    private static final String PREFIX = "generic-experiment-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final String experimentIdentifier;

    private final CompositeDatabaseModificationObserver modificationObserver;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final String experimentIdentifier)
    {
        GenericExperimentViewer viewer =
                new GenericExperimentViewer(viewContext, experimentIdentifier);
        return new DatabaseModificationAwareComponent(viewer, viewer.modificationObserver);
    }

    private GenericExperimentViewer(final IViewContext<IGenericClientServiceAsync> viewContext,
            final String experimentIdentifier)
    {
        super(viewContext);
        setId(createId(experimentIdentifier));
        this.experimentIdentifier = experimentIdentifier;
        this.modificationObserver = new CompositeDatabaseModificationObserver();
        setHeading("Experiment " + experimentIdentifier);
        reloadData();
    }

    public static String createId(String experimentIdentifier)
    {
        return ID_PREFIX + experimentIdentifier;
    }

    private static void addSection(final LayoutContainer lc, final Widget w)
    {
        lc.add(w, new RowData(-1, -1, new Margins(5)));
    }

    /**
     * Load the experiment information.
     */
    protected void reloadData()
    {
        viewContext.getService().getExperimentInfo(experimentIdentifier,
                new ExperimentInfoCallback(viewContext, this, modificationObserver));
    }

    public static final class ExperimentInfoCallback extends AbstractAsyncCallback<Experiment>
    {
        private final GenericExperimentViewer genericExperimentViewer;

        private final CompositeDatabaseModificationObserver modificationObserver;

        private ExperimentInfoCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
                final GenericExperimentViewer genericSampleViewer,
                final CompositeDatabaseModificationObserver modificationObserver)
        {
            super(viewContext);
            this.genericExperimentViewer = genericSampleViewer;
            this.modificationObserver = modificationObserver;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Sets the {@link Experiment} for this <var>generic</var> experiment viewer.
         * <p>
         * This method triggers the whole <i>GUI</i> construction.
         * </p>
         */
        @Override
        // TODO 2009-04-01, Tomasz Pylak: add attachments and properies auto-refresh
        protected final void process(final Experiment result)
        {
            genericExperimentViewer.removeAll();
            genericExperimentViewer.setScrollMode(Scroll.AUTO);
            addSection(genericExperimentViewer,
                    new ExperimentPropertiesSection(result, viewContext));
            addSection(genericExperimentViewer, new AttachmentsSection(result, viewContext));

            ExperimentSamplesSection sampleSection =
                    new ExperimentSamplesSection(result, viewContext);
            addSection(genericExperimentViewer, sampleSection);
            modificationObserver.addObserver(sampleSection.getDatabaseModificationObserver());

            ExperimentDataSetSection dataSection =
                    new ExperimentDataSetSection(result, viewContext);
            addSection(genericExperimentViewer, dataSection);
            modificationObserver.addObserver(dataSection.getDatabaseModificationObserver());

            genericExperimentViewer.layout();
        }
    }
}