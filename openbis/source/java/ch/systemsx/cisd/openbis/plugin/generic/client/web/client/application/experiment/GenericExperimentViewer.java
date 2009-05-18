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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
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
        super(viewContext, "Experiment " + experimentIdentifier, createId(experimentIdentifier));
        this.experimentIdentifier = experimentIdentifier;
        this.modificationObserver = new CompositeDatabaseModificationObserver();
        reloadAllData();
    }

    private void reloadAllData()
    {
        reloadData(new ExperimentInfoCallback(viewContext, this, modificationObserver));
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
    protected void reloadData(AbstractAsyncCallback<Experiment> callback)
    {
        viewContext.getService().getExperimentInfo(experimentIdentifier, callback);
    }

    private ExperimentPropertiesSection createExperimentPropertiesSection(
            final Experiment experiment)
    {
        return new ExperimentPropertiesSection(experiment, viewContext);
    }

    private AttachmentsSection<Experiment> createAttachmentsSection(final Experiment experiment)
    {
        final AttachmentsSection<Experiment> attachmentsSection =
                new AttachmentsSection<Experiment>(experiment, viewContext);
        attachmentsSection.setReloadDataAction(new IDelegatedAction()
            {

                public void execute()
                {
                    reloadData(attachmentsSection.getReloadDataCallback());
                }
            });
        return attachmentsSection;
    }

    public static final class ExperimentInfoCallback extends AbstractAsyncCallback<Experiment>
    {
        private final GenericExperimentViewer genericExperimentViewer;

        private final CompositeDatabaseModificationObserver observer;

        private ExperimentInfoCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
                final GenericExperimentViewer genericSampleViewer,
                final CompositeDatabaseModificationObserver modificationObserver)
        {
            super(viewContext);
            this.genericExperimentViewer = genericSampleViewer;
            this.observer = modificationObserver;
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
        protected final void process(final Experiment result)
        {
            genericExperimentViewer.updateOriginalData(result);
            genericExperimentViewer.removeAll();
            genericExperimentViewer.setScrollMode(Scroll.AUTO);

            ExperimentPropertiesSection propertiesSection =
                    genericExperimentViewer.createExperimentPropertiesSection(result);
            addSection(genericExperimentViewer, propertiesSection);
            observer.addObserver(propertiesSection.getDatabaseModificationObserver());

            AttachmentsSection<Experiment> attachmentsSection =
                    genericExperimentViewer.createAttachmentsSection(result);
            addSection(genericExperimentViewer, attachmentsSection);
            observer.addObserver(attachmentsSection.getDatabaseModificationObserver());

            ExperimentSamplesSection sampleSection =
                    new ExperimentSamplesSection(result, viewContext);
            addSection(genericExperimentViewer, sampleSection);
            observer.addObserver(sampleSection.getDatabaseModificationObserver());

            ExperimentDataSetSection dataSection =
                    new ExperimentDataSetSection(result, viewContext);
            addSection(genericExperimentViewer, dataSection);
            observer.addObserver(dataSection.getDatabaseModificationObserver());

            genericExperimentViewer.layout();
        }
    }

}