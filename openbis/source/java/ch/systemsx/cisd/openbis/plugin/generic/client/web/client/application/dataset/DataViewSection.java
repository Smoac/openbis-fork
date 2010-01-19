/*
 * Copyright 2010 ETH Zuerich, CISD
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.widget.toolbar.LabelToolItem;
import com.google.gwt.user.client.ui.Frame;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.NonHierarchicalBaseModelData;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetReportGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.DropDownList;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatabaseModificationKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * Section panel presenting data from Data Store Server.
 * 
 * @author Izabela Adamczyk
 * @author Piotr Buczek
 */
public class DataViewSection extends SingleSectionPanel
{

    private static String FILES_SMART_VIEW = "Files (Smart View)";

    private static String FILES_HOME_VIEW = "Files (Home)";

    public DataViewSection(final IViewContext<?> viewContext, final ExternalData dataset)
    {
        super(viewContext.getMessage(Dict.DATA_VIEW));

        final Frame iFrame = new Frame();

        final DatastoreServiceViewerSelectionWidget serviceSelectionWidget =
                new DatastoreServiceViewerSelectionWidget(viewContext, dataset);
        getHeader().addTool(new LabelToolItem(serviceSelectionWidget.getFieldLabel() + ":&nbsp;"));
        getHeader().addTool(serviceSelectionWidget);
        serviceSelectionWidget
                .addSelectionChangedListener(new SelectionChangedListener<DatastoreServiceDescriptionModel>()
                    {

                        @Override
                        public void selectionChanged(
                                SelectionChangedEvent<DatastoreServiceDescriptionModel> se)
                        {
                            final DatastoreServiceDescriptionModel selectedItem =
                                    se.getSelectedItem();
                            if (selectedItem != null)
                            {
                                DatastoreServiceDescription service = selectedItem.getBaseObject();

                                // TODO 2010-01-19, PTR: remove after testing with DS
                                System.err.println("selected " + service.getLabel());

                                if (service.getLabel().equals(FILES_SMART_VIEW))
                                {
                                    showDataSetFilesView(true);
                                } else if (service.getLabel().equals(FILES_HOME_VIEW))
                                {
                                    showDataSetFilesView(false);
                                } else
                                {
                                    DisplayedOrSelectedDatasetCriteria criteria =
                                            DisplayedOrSelectedDatasetCriteria
                                                    .createSelectedItems(Arrays.asList(dataset
                                                            .getCode()));
                                    DataSetReportGenerator.generate(service, criteria, viewContext
                                            .getCommonViewContext());
                                }
                            }

                        }

                        private void showDataSetFilesView(boolean autoResolve)
                        {
                            // TODO 2010-01-19, PTR: remove after testing with DS
                            System.err.println("autoResolve " + autoResolve);
                            iFrame.setUrl(DataSetUtils.createDataViewUrl(dataset, viewContext
                                    .getModel(), "simpleHtml", autoResolve));
                        }

                    });

        add(iFrame);
    }

    private static class DatastoreServiceViewerSelectionWidget extends
            DropDownList<DatastoreServiceDescriptionModel, DatastoreServiceDescription>
    {

        private final IViewContext<?> viewContext;

        private final ExternalData dataset;

        public DatastoreServiceViewerSelectionWidget(final IViewContext<?> viewContext,
                final ExternalData dataset)
        {
            super(viewContext, ("data-set_" + dataset.getCode() + "_viewer"), Dict.BUTTON_SHOW,
                    ModelDataPropertyNames.LABEL, "viewer", "viewers");
            this.viewContext = viewContext;
            this.dataset = dataset;
            setAutoSelectFirst(true); // TODO 2010-01-19, PTR: use saved display settings
        }

        @Override
        protected List<DatastoreServiceDescriptionModel> convertItems(
                List<DatastoreServiceDescription> result)
        {
            List<DatastoreServiceDescriptionModel> models =
                    DatastoreServiceDescriptionModel.convert(result, dataset);
            models.add(0, createServiceDescription(FILES_SMART_VIEW));
            models.add(1, createServiceDescription(FILES_HOME_VIEW));
            return models;
        }

        @Override
        protected void loadData(AbstractAsyncCallback<List<DatastoreServiceDescription>> callback)
        {
            viewContext.getCommonService().listDataStoreServices(DataStoreServiceKind.QUERIES,
                    callback);
        }

        public DatabaseModificationKind[] getRelevantModifications()
        {
            return new DatabaseModificationKind[0]; // don't update
        }

    }

    private static DatastoreServiceDescriptionModel createServiceDescription(String label)
    {
        final DatastoreServiceDescription service =
                new DatastoreServiceDescription(null, label, null, null);
        return new DatastoreServiceDescriptionModel(service);
    }

    /**
     * {@link ModelData} for {@link DatastoreServiceDescription}.
     * 
     * @author Piotr Buczek
     */
    public static class DatastoreServiceDescriptionModel extends NonHierarchicalBaseModelData
    {

        private static final long serialVersionUID = 1L;

        public DatastoreServiceDescriptionModel(final DatastoreServiceDescription description)
        {
            set(ModelDataPropertyNames.OBJECT, description);
            set(ModelDataPropertyNames.LABEL, description.getLabel());
        }

        public final static List<DatastoreServiceDescriptionModel> convert(
                final List<DatastoreServiceDescription> services, final ExternalData dataset)
        {
            final List<DatastoreServiceDescriptionModel> result =
                    new ArrayList<DatastoreServiceDescriptionModel>();
            for (final DatastoreServiceDescription service : services)
            {
                if (service.getDatastoreCode().equals(dataset.getDataStore().getCode())
                        && (Arrays.asList(service.getDatasetTypeCodes())).contains(dataset
                                .getDataSetType().getCode()))
                {
                    result.add(new DatastoreServiceDescriptionModel(service));
                }
            }
            return result;
        }

        public final DatastoreServiceDescription getBaseObject()
        {
            return get(ModelDataPropertyNames.OBJECT);
        }

    }

}
