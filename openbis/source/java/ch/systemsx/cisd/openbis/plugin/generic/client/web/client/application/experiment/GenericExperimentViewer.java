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

import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AttachmentVersionsSection;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.DisposableTabContent;
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
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.GWTUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.IAttachmentHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdAndCodeHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.AttachmentHolderKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.BasicEntityType;
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

    protected final IIdAndCodeHolder experimentId;

    protected final BasicEntityType experimentType;

    protected Experiment experiment;

    private ExperimentPropertiesPanel propertiesPanelOrNull;

    private List<DisposableTabContent> rightPanelSectionsOrNull;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final BasicEntityType experimentType, final IIdAndCodeHolder identifiable)
    {
        GenericExperimentViewer viewer =
                new GenericExperimentViewer(viewContext, experimentType, identifiable);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    protected GenericExperimentViewer(final IViewContext<IGenericClientServiceAsync> viewContext,
            final BasicEntityType experimentType, final IIdAndCodeHolder experimentId)
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
        if (viewContext.isSimpleMode())
        {
            return;
        }
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
                    experiment = result;
                    layoutExperimentDetailView();
                }
            });
    }

    protected void reloadExperiment(AbstractAsyncCallback<Experiment> callback)
    {
        viewContext.getCommonService().getExperimentInfo(new TechId(experimentId), callback);
    }

    private void layoutExperimentDetailView()
    {
        int logId = viewContext.log("layoutExperimentDetailView");
        updateOriginalData(experiment);
        removeAll();

        this.propertiesPanelOrNull = new ExperimentPropertiesPanel(experiment, viewContext, this);
        add(propertiesPanelOrNull, createLeftBorderLayoutData());
        final String displayIdSuffix = this.experimentType.getCode();

        configureLeftPanel(displayIdSuffix);

        final Html loadingLabel = new Html(viewContext.getMessage(Dict.LOAD_IN_PROGRESS));
        add(loadingLabel, createRightBorderLayoutData());
        layout();
        viewContext.logStop(logId);

        GWTUtils.executeDelayed(new IDelegatedAction()
            {
                public void execute()
                {
                    remove(loadingLabel);
                    GenericExperimentViewer.this.rightPanelSectionsOrNull = createRightPanel();
                    SectionsPanel rightPanel =
                            layoutSections(rightPanelSectionsOrNull, displayIdSuffix);
                    moduleSectionManager.initialize(rightPanel, experiment);
                    add(rightPanel, createRightBorderLayoutData());
                    layout();
                }
            });
    }

    /**
     * Adds listeners and sets up the initial left panel state.
     */
    private void configureLeftPanel(String displayIdSuffix)
    {
        if (isLeftPanelInitiallyCollapsed(displayIdSuffix))
        {
            ((BorderLayout) getLayout()).collapse(com.extjs.gxt.ui.client.Style.LayoutRegion.WEST);
        }

        // Add the listeners after configuring the panel, so as not to cause confusion
        addLeftPanelCollapseExpandListeners(displayIdSuffix);
    }

    public static final String createId(final IIdAndCodeHolder identifiable)
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
        final IAttachmentHolder attachmentHolder = asExperimentAttachmentHolder(experimentId);
        return new AttachmentVersionsSection(viewContext.getCommonViewContext(), attachmentHolder);
    }

    private static IAttachmentHolder asExperimentAttachmentHolder(
            final IIdAndCodeHolder identifiable)
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

    private List<DisposableTabContent> createRightPanel()
    {

        List<DisposableTabContent> allPanels = new ArrayList<DisposableTabContent>();

        allPanels.addAll(createAdditionalBrowserSectionPanels());

        final ExperimentSamplesSection sampleSection =
                new ExperimentSamplesSection(viewContext, experimentType, experimentId);
        allPanels.add(sampleSection);

        final DisposableTabContent dataSection = createExperimentDataSetSection();
        dataSection.setIds(DisplayTypeIDGenerator.DATA_SETS_SECTION);
        allPanels.add(dataSection);

        final AttachmentVersionsSection attachmentsSection = createAttachmentsSection();
        allPanels.add(attachmentsSection);

        return allPanels;
    }

    private DisposableTabContent createExperimentDataSetSection()
    {
        return new DisposableTabContent("Data Sets", viewContext, experimentId)
            {
                @Override
                protected IDisposableComponent createDisposableContent()
                {
                    return ExperimentDataSetBrowser.create(viewContext, new TechId(experimentId),
                            experimentType);
                }
            };
    }

    private SectionsPanel layoutSections(List<DisposableTabContent> allPanels,
            String displayIdSuffix)
    {
        final SectionsPanel container =
                new SectionsPanel(viewContext.getCommonViewContext(), ID_PREFIX + experimentId);
        container.setDisplayID(DisplayTypeIDGenerator.GENERIC_EXPERIMENT_VIEWER, displayIdSuffix);
        for (DisposableTabContent panel : allPanels)
        {
            container.addSection(panel);
        }
        container.layout();
        return container;
    }

    protected List<DisposableTabContent> createAdditionalBrowserSectionPanels()
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
            for (DisposableTabContent panel : rightPanelSectionsOrNull)
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
