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
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IDelegatedAction;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdentifiable;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> dataset viewer.
 * 
 * @author Piotr Buczek
 */
public final class GenericDataSetViewer extends
        AbstractViewer<IGenericClientServiceAsync, ExternalData> implements
        IDatabaseModificationObserver
{
    private static final String PREFIX = "generic-dataset-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    public static final String VIEW_BUTTON_ID_SUFFIX = "_view-button";

    private final BrowseButtonHolder browseButtonHolder;

    private final TechId datasetId;

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
                            getOriginalDataAsSingleton(), createDeletionCallback()).show();
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

    private static void addSection(final LayoutContainer lc, final Widget w)
    {
        lc.add(w, new RowData(-1, -1, new Margins(5)));
    }

    /**
     * Load the dataset information.
     */
    protected void reloadData()
    {
        viewContext.getService().getDataSetInfo(datasetId, getBaseIndexURL(),
                new DataSetInfoCallback(viewContext, this));
    }

    public static final class DataSetInfoCallback extends AbstractAsyncCallback<ExternalData>
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
            genericDataSetViewer.setScrollMode(Scroll.AUTO);
            addSection(genericDataSetViewer, new DataSetPropertiesSection(result, viewContext));
            genericDataSetViewer.layout();
        }

        @Override
        protected void finishOnFailure(Throwable caught)
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
                    DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM) };
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        reloadData(); // reloads everything
    }

}