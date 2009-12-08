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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.dataset;

import java.util.Set;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DisplayTypeIDGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.SectionsPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> dataset viewer.
 * 
 * @author Piotr Buczek
 */
public final class GenericDataSetViewer extends AbstractViewer<ExternalData> implements
        IDatabaseModificationObserver
{
    private static final String PREFIX = "generic-dataset-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String VIEW_BUTTON_ID_SUFFIX = "_view-button";

    private final BrowseButtonHolder browseButtonHolder;

    private final TechId datasetId;

    private final IViewContext<IGenericClientServiceAsync> viewContext;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final IIdentifiable identifiable)
    {
        GenericDataSetViewer viewer = new GenericDataSetViewer(viewContext, identifiable);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private GenericDataSetViewer(final IViewContext<IGenericClientServiceAsync> viewContext,
            final IIdentifiable identifiable)
    {
        super(viewContext, createId(identifiable));
        setLayout(new BorderLayout());
        this.viewContext = viewContext;
        this.datasetId = TechId.create(identifiable);
        this.browseButtonHolder = new BrowseButtonHolder();
        extendToolBar();
        reloadData();
    }

    private void extendToolBar()
    {
        addToolBarButton(browseButtonHolder.getButton());

        addToolBarButton(createDeleteButton(new IDelegatedAction()
            {
                public void execute()
                {
                    new DataSetListDeletionConfirmationDialog(viewContext.getCommonViewContext(),
                            getOriginalData(), createDeletionCallback()).show();
                }

            }));
    }

    public static final String createId(final IIdentifiable identifiable)
    {
        return createId(TechId.create(identifiable));
    }

    public static final String createId(final TechId datasetId)
    {
        return ID_PREFIX + datasetId;
    }

    private final String createChildId(String childIdSuffix)
    {
        return getId() + childIdSuffix;
    }

    /**
     * Load the dataset information.
     */
    protected void reloadData()
    {
        viewContext.getService().getDataSetInfo(datasetId,
                new DataSetInfoCallback(viewContext, this));
    }

    private final Component createLeftPanel(final ExternalData dataset)
    {
        final ContentPanel panel = createDataSetPropertiesPanel(dataset);
        panel.setScrollMode(Scroll.AUTOY);
        return panel;
    }

    private ContentPanel createDataSetPropertiesPanel(final ExternalData dataset)
    {
        return new DataSetPropertiesPanel(dataset, viewContext);
    }

    private final Component createRightPanel(final ExternalData dataset)
    {
        final SectionsPanel container = new SectionsPanel(viewContext.getCommonViewContext());
        final String displayIdSuffix = getDisplayIdSuffix(dataset.getDataSetType().getCode());

        // parents
        final SingleSectionPanel parentsSection = new DataSetParentsSection(viewContext, dataset);
        parentsSection.setDisplayID(DisplayTypeIDGenerator.DATA_SET_PARENTS_SECTION,
                displayIdSuffix);
        container.addPanel(parentsSection);

        // children
        final SingleSectionPanel childrenSection = new DataSetChildrenSection(viewContext, dataset);
        childrenSection.setDisplayID(DisplayTypeIDGenerator.DATA_SET_CHILDREN_SECTION,
                displayIdSuffix);
        container.addPanel(childrenSection);

        container.layout();
        return container;
    }

    private static final String getDisplayIdSuffix(String suffix)
    {
        return PREFIX + suffix;
    }

    private static final class DataSetInfoCallback extends AbstractAsyncCallback<ExternalData>
    {
        private final GenericDataSetViewer genericDataSetViewer;

        private DataSetInfoCallback(final IViewContext<IGenericClientServiceAsync> viewContext,
                final GenericDataSetViewer genericSampleViewer)
        {
            super(viewContext);
            this.genericDataSetViewer = genericSampleViewer;
        }

        //
        // AbstractAsyncCallback
        //

        /**
         * Sets the {@link ExternalData} for this <var>generic</var> dataset viewer.
         * <p>
         * This method triggers the whole <i>GUI</i> construction.
         * </p>
         */
        @Override
        protected final void process(final ExternalData result)
        {
            genericDataSetViewer.updateOriginalData(result);
            genericDataSetViewer.removeAll();

            // Left panel
            final Component leftPanel = genericDataSetViewer.createLeftPanel(result);
            genericDataSetViewer.add(leftPanel, createLeftBorderLayoutData());
            // Right panel
            final Component rightPanel = genericDataSetViewer.createRightPanel(result);
            genericDataSetViewer.add(rightPanel, createRightBorderLayoutData());

            genericDataSetViewer.layout();
        }

        @Override
        public void finishOnFailure(Throwable caught)
        {
            genericDataSetViewer.setupRemovedEntityView();
        }
    }

    @Override
    protected void updateOriginalData(final ExternalData result)
    {
        super.updateOriginalData(result);
        browseButtonHolder.setupData(result);
    }

    /**
     * Holder of a {@link Button} that goes to external data browsing on selection. The button is
     * disabled until data is successfully loaded by the viewer.
     */
    private class BrowseButtonHolder
    {
        private final Button button;

        public BrowseButtonHolder()
        {
            this.button = createBrowseButton();
        }

        private Button createBrowseButton()
        {
            Button result = new Button(viewContext.getMessage(Dict.BUTTON_VIEW));
            result.setTitle(viewContext.getMessage(Dict.TOOLTIP_VIEW_DATASET));
            result.setId(createChildId(VIEW_BUTTON_ID_SUFFIX));
            result.disable();
            return result;
        }

        public Button getButton()
        {
            return this.button;
        }

        /** @param data external data that will be browsed after selection */
        public void setupData(final ExternalData data)
        {
            button.addSelectionListener(new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce)
                    {
                        DataSetUtils.showDataSet(data, viewContext.getModel());
                    }
                });
        }
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.edit(ObjectKind.DATA_SET),
                    DatabaseModificationKind.createOrDelete(ObjectKind.DATA_SET),
                    DatabaseModificationKind.createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    DatabaseModificationKind.edit(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM),
                    DatabaseModificationKind.edit(ObjectKind.VOCABULARY_TERM), };
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        reloadData(); // reloads everything
    }

}
