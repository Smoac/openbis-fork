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
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AttachmentVersionsSection;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserverWithMainObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.IEntityInformationHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
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

    private final TechId experimentId;

    private final CompositeDatabaseModificationObserverWithMainObserver modificationObserver;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final IIdentifiable identifiable)
    {
        GenericExperimentViewer viewer = new GenericExperimentViewer(viewContext, identifiable);
        return new DatabaseModificationAwareComponent(viewer, viewer.modificationObserver);
    }

    private GenericExperimentViewer(final IViewContext<IGenericClientServiceAsync> viewContext,
            final IIdentifiable identifiable)
    {
        super(viewContext, createId(identifiable));
        this.experimentId = TechId.create(identifiable);
        this.modificationObserver = new CompositeDatabaseModificationObserverWithMainObserver();
        reloadAllData();
    }

    private void reloadAllData()
    {
        reloadData(new ExperimentInfoCallback(viewContext, this, modificationObserver));
    }

    public static final String createId(final IIdentifiable identifiable)
    {
        return createId(TechId.create(identifiable));
    }

    public static final String createId(final TechId experimentId)
    {
        return ID_PREFIX + experimentId;
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
        viewContext.getService().getExperimentInfo(experimentId, getBaseIndexURL(), callback);
    }

    @Override
    public void updateOriginalData(IEntityInformationHolder newData)
    {
        super.updateOriginalData(newData);
    }

    private final Component createLeftPanel(final Experiment experiment,
            final CompositeDatabaseModificationObserverWithMainObserver observer)
    {
        final ExperimentPropertiesPanel panel = createExperimentPropertiesPanel(experiment);
        panel.setScrollMode(Scroll.AUTOY);
        observer.addMainObserver(panel.getDatabaseModificationObserver());
        return panel;
    }

    private ExperimentPropertiesPanel createExperimentPropertiesPanel(final Experiment experiment)
    {
        return new ExperimentPropertiesPanel(experiment, viewContext, this);
    }

    private AttachmentVersionsSection<Experiment> createAttachmentsSection(
            final Experiment experiment)
    {
        return new AttachmentVersionsSection<Experiment>(viewContext.getCommonViewContext(),
                experiment);
    }

    public static final class ExperimentInfoCallback extends AbstractAsyncCallback<Experiment>
    {
        private final GenericExperimentViewer genericExperimentViewer;

        private final CompositeDatabaseModificationObserverWithMainObserver observer;

        private ExperimentInfoCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
                final GenericExperimentViewer genericSampleViewer,
                final CompositeDatabaseModificationObserverWithMainObserver modificationObserver)
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
            genericExperimentViewer.setLayout(new BorderLayout());

            // Left panel
            final Component leftPanel = genericExperimentViewer.createLeftPanel(result, observer);
            genericExperimentViewer.add(leftPanel, createLeftBorderLayoutData());
            // Right panel
            final Component rightPanel = genericExperimentViewer.createRightPanel(result, observer);
            genericExperimentViewer.add(rightPanel, createRightBorderLayoutData());

            genericExperimentViewer.layout();
        }
    }

    public Component createRightPanel(Experiment result,
            CompositeDatabaseModificationObserverWithMainObserver observer)
    {
        final LayoutContainer container = new LayoutContainer();
        container.setLayout(new RowLayout());

        AttachmentVersionsSection<Experiment> attachmentsSection = createAttachmentsSection(result);
        addSection(container, attachmentsSection);
        observer.addObserver(attachmentsSection.getDatabaseModificationObserver());

        ExperimentSamplesSection sampleSection = new ExperimentSamplesSection(result, viewContext);
        addSection(container, sampleSection);
        observer.addObserver(sampleSection.getDatabaseModificationObserver());

        ExperimentDataSetSection dataSection = new ExperimentDataSetSection(result, viewContext);
        addSection(container, dataSection);
        observer.addObserver(dataSection.getDatabaseModificationObserver());

        container.layout();
        return container;
    }
}