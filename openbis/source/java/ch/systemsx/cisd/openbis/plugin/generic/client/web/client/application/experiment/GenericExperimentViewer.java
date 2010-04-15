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

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AttachmentVersionsSection;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.CompositeDatabaseModificationObserverWithMainObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.grid.IDisposableComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExperimentType;
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

    protected final IIdentifiable experimentId;

    protected final ExperimentType experimentType;

    private ExperimentPropertiesPanel propertiesPanelOrNull;

    private List<DisposableSectionPanel> rightPanelSectionsOrNull;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final ExperimentType experimentType, final IIdentifiable identifiable)
    {
        GenericExperimentViewer viewer =
                new GenericExperimentViewer(viewContext, experimentType, identifiable);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    protected GenericExperimentViewer(final IViewContext<IGenericClientServiceAsync> viewContext,
            final ExperimentType experimentType, final IIdentifiable experimentId)
    {
        super(viewContext, createId(experimentId));

        int logID = viewContext.log("create experiment viewer");

        this.experimentId = experimentId;
        this.experimentType = experimentType;
        this.viewContext = viewContext;
        setLayout(new BorderLayout());
        extendToolBar();

        reloadAllData();

        viewContext.logStop(logID);
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
        reloadExperiment(new AbstractAsyncCallback<Experiment>(viewContext)
            {
                @Override
                protected final void process(final Experiment result)
                {
                    layoutExperimentDetailView(result);
                }
            });
    }

    protected void reloadExperiment(AbstractAsyncCallback<Experiment> callback)
    {
        viewContext.getService().getExperimentInfo(new TechId(experimentId), callback);
    }

    private void layoutExperimentDetailView(Experiment experiment)
    {
        int logId = viewContext.log("layoutExperimentDetailView");
        updateOriginalData(experiment);
        removeAll();

        this.propertiesPanelOrNull = new ExperimentPropertiesPanel(experiment, viewContext, this);
        add(propertiesPanelOrNull, createLeftBorderLayoutData());

        final Html loadingLabel = new Html(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
        add(loadingLabel, createRightBorderLayoutData());
        layout();
        viewContext.logStop(logId);

        executeDelayed(new IDelegatedAction()
            {
                public void execute()
                {
                    remove(loadingLabel);
                    GenericExperimentViewer.this.rightPanelSectionsOrNull = createRightPanel();
                    SectionsPanel rightPanel = layoutSections(rightPanelSectionsOrNull);
                    add(rightPanel, createRightBorderLayoutData());
                    layout();
                }
            });
    }

    private static void executeDelayed(final IDelegatedAction delegatedAction)
    {
        DelayedTask delayedTask = new DelayedTask(new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    delegatedAction.execute();
                }
            });
        delayedTask.delay(1);
    }

    public static final String createId(final IIdentifiable identifiable)
    {
        return createId(TechId.create(identifiable));
    }

    public static final String createId(final TechId experimentId)
    {
        return ID_PREFIX + experimentId;
    }

    @Override
    public void updateOriginalData(Experiment newData)
    {
        super.updateOriginalData(newData);
    }

    private AttachmentVersionsSection createAttachmentsSection()
    {
        final IAttachmentHolder newExperiment = asExperimentAttachmentHolder(experimentId);
        return new AttachmentVersionsSection(viewContext.getCommonViewContext(), newExperiment);
    }

    private static IAttachmentHolder asExperimentAttachmentHolder(final IIdentifiable identifiable)
    {
        return new IAttachmentHolder()
            {
                public AttachmentHolderKind getAttachmentHolderKind()
                {
                    return AttachmentHolderKind.EXPERIMENT;
                }

                public Long getId()
                {
                    return identifiable.getId();
                }

                public String getCode()
                {
                    return identifiable.getCode();
                }
            };
    }

    private static final String getDisplayIdSuffix(String suffix)
    {
        return GENERIC_EXPERIMENT_VIEWER + "-" + suffix;
    }

    private List<DisposableSectionPanel> createRightPanel()
    {
        final String displayIdSuffix = getDisplayIdSuffix(experimentType.getCode());
        List<DisposableSectionPanel> allPanels = new ArrayList<DisposableSectionPanel>();

        allPanels.addAll(createAdditionalBrowserSectionPanels(displayIdSuffix));

        final ExperimentSamplesSection sampleSection =
                new ExperimentSamplesSection(viewContext, experimentType, experimentId);
        sampleSection.setDisplayID(DisplayTypeIDGenerator.SAMPLE_SECTION, displayIdSuffix);
        allPanels.add(sampleSection);

        final DisposableSectionPanel dataSection = createExperimentDataSetSection();
        dataSection.setDisplayID(DisplayTypeIDGenerator.DATA_SET_SECTION, displayIdSuffix);
        allPanels.add(dataSection);

        final AttachmentVersionsSection attachmentsSection = createAttachmentsSection();
        attachmentsSection.setDisplayID(DisplayTypeIDGenerator.ATTACHMENT_SECTION, displayIdSuffix);
        allPanels.add(attachmentsSection);

        return allPanels;
    }

    private DisposableSectionPanel createExperimentDataSetSection()
    {
        return new DisposableSectionPanel("Data Sets", viewContext)
            {
                @Override
                protected IDisposableComponent createDisposableContent()
                {
                    return ExperimentDataSetBrowser.create(viewContext, new TechId(experimentId),
                            experimentType);
                }
            };
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

    protected List<DisposableSectionPanel> createAdditionalBrowserSectionPanels(
            String displyIdSuffix)
    {
        return Collections.emptyList();
    }

    // this observer should not be cached - some sections may become visible in the future
    private IDatabaseModificationObserver createModificationObserver()
    {
        if (propertiesPanelOrNull != null)
        {
            assert rightPanelSectionsOrNull != null : "right panel not layouted";
            CompositeDatabaseModificationObserverWithMainObserver modificationObserver =
                    new CompositeDatabaseModificationObserverWithMainObserver(propertiesPanelOrNull
                            .getDatabaseModificationObserver());
            for (DisposableSectionPanel panel : rightPanelSectionsOrNull)
            {
                modificationObserver.addObserver(panel.tryGetDatabaseModificationObserver());
            }
            return modificationObserver;
        } else
        {
            return new CompositeDatabaseModificationObserver();
        }

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
