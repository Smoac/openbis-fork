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
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.GenericConstants;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.DatabaseModificationAwareComponent;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.framework.IDatabaseModificationObserver;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.AbstractViewer;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind.ObjectKind;
import ch.systemsx.cisd.openbis.plugin.generic.client.web.client.IGenericClientServiceAsync;

/**
 * The <i>generic</i> dataset viewer.
 * 
 * @author Piotr Buczek
 */
public final class GenericDataSetViewer extends AbstractViewer<IGenericClientServiceAsync>
        implements IDatabaseModificationObserver
{
    private static final String PREFIX = "generic-dataset-viewer_";

    public static final String ID_PREFIX = GenericConstants.ID_PREFIX + PREFIX;

    private final String datasetIdentifier;

    public static DatabaseModificationAwareComponent create(
            final IViewContext<IGenericClientServiceAsync> viewContext,
            final String datasetIdentifier)
    {
        GenericDataSetViewer viewer = new GenericDataSetViewer(viewContext, datasetIdentifier);
        return new DatabaseModificationAwareComponent(viewer, viewer);
    }

    private GenericDataSetViewer(final IViewContext<IGenericClientServiceAsync> viewContext,
            final String datasetIdentifier)
    {
        super(viewContext, EntityKind.DATA_SET.getDescription() + " " + datasetIdentifier,
                createId(datasetIdentifier));
        this.datasetIdentifier = datasetIdentifier;
        reloadData();
    }

    public static String createId(String datasetIdentifier)
    {
        return ID_PREFIX + datasetIdentifier;
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
        viewContext.getService().getDataSetInfo(datasetIdentifier, getBaseIndexURL(),
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
    }

    public DatabaseModificationKind[] getRelevantModifications()
    {
        return new DatabaseModificationKind[]
            { DatabaseModificationKind.edit(ObjectKind.DATA_SET),
                    DatabaseModificationKind.createOrDelete(ObjectKind.PROPERTY_TYPE_ASSIGNMENT),
                    DatabaseModificationKind.createOrDelete(ObjectKind.VOCABULARY_TERM) };
    }

    public void update(Set<DatabaseModificationKind> observedModifications)
    {
        reloadData(); // reloads everything
    }

}