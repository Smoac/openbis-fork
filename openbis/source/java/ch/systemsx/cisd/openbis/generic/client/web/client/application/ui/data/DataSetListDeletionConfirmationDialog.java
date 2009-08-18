/*
 * Copyright 2009 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data;

import java.util.Collections;

import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.ExternalData;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

public final class DataSetListDeletionConfirmationDialog extends
        AbstractDataListDeletionConfirmationDialog<ExternalData>
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final AbstractAsyncCallback<Void> callback;

    private Radio onlySelectedRadio;

    private final SelectedAndDisplayedItems selectedAndDisplayedItemsOrNull;

    private final ExternalData singleData;

    public DataSetListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext,
            AbstractAsyncCallback<Void> callback,
            SelectedAndDisplayedItems selectedAndDisplayedItems)
    {
        super(viewContext, selectedAndDisplayedItems.getSelectedItems());
        this.viewContext = viewContext;
        this.callback = callback;
        singleData = null;
        this.selectedAndDisplayedItemsOrNull = selectedAndDisplayedItems;
    }

    public DataSetListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext, ExternalData data,
            AbstractAsyncCallback<Void> callback)
    {
        super(viewContext, Collections.singletonList(data));
        this.viewContext = viewContext;
        this.callback = callback;
        singleData = data;
        selectedAndDisplayedItemsOrNull = null;
    }

    @Override
    protected void executeConfirmedAction()
    {
        if (selectedAndDisplayedItemsOrNull != null)
        {
            final boolean onlySelected = WidgetUtils.isSelected(onlySelectedRadio);
            final DisplayedOrSelectedDatasetCriteria uploadCriteria =
                    selectedAndDisplayedItemsOrNull.createCriteria(onlySelected);
            viewContext.getCommonService().deleteDataSets(uploadCriteria, reason.getValue(),
                    callback);
        } else
        {
            viewContext.getCommonService().deleteDataSet(singleData.getCode(), reason.getValue(),
                    callback);
        }
    }

    @Override
    protected String getEntityName()
    {
        return EntityKind.DATA_SET.getDescription();
    }

    @Override
    protected void extendForm()
    {
        super.extendForm();
        if (selectedAndDisplayedItemsOrNull != null)
            formPanel.add(createDataSetsRadio());
    }

    private final RadioGroup createDataSetsRadio()
    {// FIXME: externalize strings
        return WidgetUtils.createAllOrSelectedRadioGroup(onlySelectedRadio =
                WidgetUtils.createRadio("selected (" + data.size() + ")"), WidgetUtils
                .createRadio("all"), "Data Sets", data.size());
    }

}