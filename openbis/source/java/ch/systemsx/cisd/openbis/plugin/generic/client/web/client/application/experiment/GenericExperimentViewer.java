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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.layout.BorderLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AttachmentVersionsSection;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserverWithMainObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> experiment viewer.
 * 
 * @author Izabela Adamczyk
 */
public class GenericExperimentViewer extends AbstractViewer<Experiment> implements
        IDatabaseModificationObserver
{
    private static final String GENERIC_EXPERIMENT_VIEWER = "generic-experiment-viewer";

    private static final String PREFIX = GENERIC_EXPERIMENT_VIEWER + "_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    protected final IIdentifiable identifiable;

    protected Experiment experimentOrNull;

    private List<DisposableSectionPanel> rightPanelSections;

    private ExperimentPropertiesPanel propertiesPanel;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final IIdentifiable identifiable)
    {
        GenericExperimentViewer viewer = new GenericExperimentViewer(viewContext, identifiable);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    protected GenericExperimentViewer(final IViewContext<IGenericClientServiceAsync> viewContext,
            final IIdentifiable experiment)
    {
        super(viewContext, createId(experiment));
        this.identifiable = experiment;
        this.viewContext = viewContext;
        setLayout(new BorderLayout());
        extendToolBar();
        reloadAllData();
    }

    private void extendToolBar()
    {
        addToolBarButton(createDeleteButton(new IDelegatedAction()
            {
                public void execute()
                {
                    new ExperimentListDeletionConfirmationDialog(
                            viewContext.getCommonViewContext(), createDeletionCallback(),
                            getOriginalData()).show();
                }
            }));
    }

    private void reloadAllData()
    {
        reloadData(new AbstractAsyncCallback<Experiment>(viewContext)
            {
                @Override
                protected final void process(final Experiment result)
                {
                    layoutExperimentDetailView(result);
                }
            });
    }

    public static final String createId(final IIdentifiable identifiable)
    {
        return createId(TechId.create(identifiable));
    }

    public static final String createId(final TechId experimentId)
    {
        return ID_PREFIX + experimentId;
    }

    /**
     * Load the experiment information.
     */
    protected void reloadData(AbstractAsyncCallback<Experiment> callback)
    {
        TechId experimentId = TechId.create(identifiable);
        viewContext.getService().getExperimentInfo(experimentId, callback);
    }

    @Override
    public void updateOriginalData(Experiment newData)
    {
        super.updateOriginalData(newData);
    }

    private ExperimentPropertiesPanel createExperimentPropertiesPanel(final Experiment newExperiment)
    {
        return new ExperimentPropertiesPanel(newExperiment, viewContext, this);
    }

    private AttachmentVersionsSection<Experiment> createAttachmentsSection(
            final Experiment newExperiment)
    {
        return new AttachmentVersionsSection<Experiment>(viewContext.getCommonViewContext(),
                newExperiment);
    }

    /**
     * Sets the {@link Experiment} for this <var>generic</var> experiment viewer.
     * <p>
     * This method triggers the whole <i>GUI</i> construction.
     * </p>
     */
    private void layoutExperimentDetailView(final Experiment result)
    {
        this.experimentOrNull = result;
        this.updateOriginalData(result);
        this.removeAll();

        // Left panel
        this.propertiesPanel = createExperimentPropertiesPanel(result);
        add(this.propertiesPanel, createLeftBorderLayoutData());
        // Right panel
        this.rightPanelSections = createRightPanel(result);
        SectionsPanel rightPanel = layoutSections(this.rightPanelSections);
        add(rightPanel, createRightBorderLayoutData());

        layout();
    }

    private static final String getDisplayIdSuffix(String suffix)
    {
        return GENERIC_EXPERIMENT_VIEWER + "-" + suffix;
    }

    private List<DisposableSectionPanel> createRightPanel(Experiment result)
    {
        final String displayIdSuffix = getDisplayIdSuffix(result.getExperimentType().getCode());
        List<DisposableSectionPanel> allPanels = new ArrayList<DisposableSectionPanel>();

        allPanels.addAll(createAdditionalBrowserSectionPanels(displayIdSuffix));

        final ExperimentSamplesSection sampleSection =
                new ExperimentSamplesSection(viewContext, result);
        sampleSection.setDisplayID(DisplayTypeIDGenerator.SAMPLE_SECTION, displayIdSuffix);
        allPanels.add(sampleSection);

        final ExperimentDataSetSection dataSection =
                new ExperimentDataSetSection(result, viewContext);
        dataSection.setDisplayID(DisplayTypeIDGenerator.DATA_SET_SECTION, displayIdSuffix);
        allPanels.add(dataSection);

        final AttachmentVersionsSection<Experiment> attachmentsSection =
                createAttachmentsSection(result);
        attachmentsSection.setDisplayID(DisplayTypeIDGenerator.ATTACHMENT_SECTION, displayIdSuffix);
        allPanels.add(attachmentsSection);

        return allPanels;
    }

    private SectionsPanel layoutSections(List<DisposableSectionPanel> allPanels)
    {
        final SectionsPanel container = new SectionsPanel(viewContext.getCommonViewContext());
        for (DisposableSectionPanel panel : allPanels)
        {
            container.addPanel(panel);
        }
        container.layout();
        return container;
    }

    protected List<DisposableSectionPanel> createAdditionalBrowserSectionPanels(String displyIdSuffix)
    {
        return Collections.emptyList();
    }

    // this observer should not be cached - some sections may become visible in the future
    private IDatabaseModificationObserver createModificationObserver()
    {
        CompositeDatabaseModificationObserverWithMainObserver modificationObserver =
                new CompositeDatabaseModificationObserverWithMainObserver();
        modificationObserver.addMainObserver(propertiesPanel.getDatabaseModificationObserver());
        for (DisposableSectionPanel panel : rightPanelSections)
        {
            modificationObserver.addObserver(panel.tryGetDatabaseModificationObserver());
        }
        return modificationObserver;
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return createModificationObserver().getRelevantModifications();
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        createModificationObserver().update(observedModifications);
    }
}
