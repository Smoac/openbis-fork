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

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.google.gwt.user.client.ui.Frame;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.SingleSectionPanel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.DataSetReportGenerator;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.DataSetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DataStoreServiceKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DatastoreServiceDescription;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

/**
 * Section panel presenting data from Data Store Server.
 * 
 * @author Izabela Adamczyk
 */
public class DataViewSection extends SingleSectionPanel
{

    public DataViewSection(final IViewContext<?> viewContext, final ExternalData dataset)
    {
        super(viewContext.getMessage(Dict.DATA_VIEW));

        Button topButton = new Button(viewContext.getMessage(Dict.BUTTON_TOP));
        final CheckBox autoResolveCheckbox = new CheckBox();
        autoResolveCheckbox.setBoxLabel(viewContext.getMessage(Dict.AUTO_RESOLVE_LABEL));
        autoResolveCheckbox.setValue(true);

        final Frame iFrame = new Frame(createUrl(viewContext, dataset, autoResolveCheckbox));

        autoResolveCheckbox.addListener(Events.Change, new Listener<BaseEvent>()
            {
                public void handleEvent(BaseEvent be)
                {
                    iFrame.setUrl(createUrl(viewContext, dataset, autoResolveCheckbox));
                }
            });
        topButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    autoResolveCheckbox.disableEvents(true);
                    autoResolveCheckbox.setValue(false);
                    autoResolveCheckbox.disableEvents(false);
                    iFrame.setUrl(createUrl(viewContext, dataset, autoResolveCheckbox));
                }
            });

        getHeader().addTool(autoResolveCheckbox);
        getHeader().addTool(topButton);
        viewContext.getCommonService().listDataStoreServices(DataStoreServiceKind.QUERIES,
                new ReportPluginsCallback(viewContext, dataset, getHeader()));
        add(iFrame);
    }

    private String createUrl(final IViewContext<?> viewContext, final ExternalData dataset,
            CheckBox autoResolve)
    {
        return DataSetUtils.createDataViewUrl(dataset, viewContext.getModel(), "simpleHtml",
                autoResolve.getValue());
    }

    /**
     * Adds buttons with services relevant to given data set to the header.
     * 
     * @author Izabela Adamczyk
     */
    static private final class ReportPluginsCallback extends
            AbstractAsyncCallback<List<DatastoreServiceDescription>>
    {
        private final ExternalData dataset;

        private final Header header;

        private ReportPluginsCallback(IViewContext<?> viewContext, ExternalData dataset,
                Header header)
        {
            super(viewContext);
            this.dataset = dataset;
            this.header = header;
        }

        @Override
        protected void process(List<DatastoreServiceDescription> result)
        {
            List<DatastoreServiceDescription> relevant =
                    new ArrayList<DatastoreServiceDescription>();
            for (DatastoreServiceDescription service : result)
            {
                if (service.getDatastoreCode().equals(dataset.getDataStore().getCode())
                        && (Arrays.asList(service.getDatasetTypeCodes())).contains(dataset
                                .getDataSetType().getCode()))
                {
                    relevant.add(service);
                }
            }

            for (final DatastoreServiceDescription service : relevant)
            {
                header.addTool(createServiceButton(service));
            }
        }

        private Button createServiceButton(final DatastoreServiceDescription service)
        {
            return new Button(service.getLabel(), new SelectionListener<ButtonEvent>()
                {
                    @Override
                    public void componentSelected(ButtonEvent ce)
                    {
                        DisplayedOrSelectedDatasetCriteria criteria =
                                DisplayedOrSelectedDatasetCriteria.createSelectedItems(Arrays
                                        .asList(dataset.getCode()));
                        DataSetReportGenerator.generate(service, criteria, viewContext
                                .getCommonViewContext());
                    }
                });
        }
    }

}
