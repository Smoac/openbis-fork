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

import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.data.AbstractExternalDataGrid.SelectedAndDisplayedItems;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedDatasetCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.DeletionUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ExternalData;

public final class DataSetListDeletionConfirmationDialog extends
        AbstractDataListDeletionConfirmationDialog<ExternalData>
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final SelectedAndDisplayedItems selectedAndDisplayedItemsOrNull;

    private final ExternalData singleData;

    public DataSetListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext,
            AbstractAsyncCallback<Void> callback,
            SelectedAndDisplayedItems selectedAndDisplayedItems)
    {
        super(viewContext, selectedAndDisplayedItems.getSelectedItems(), callback);
        this.withDeletion();
        this.withRadio();
        this.viewContext = viewContext;
        this.singleData = null;
        this.selectedAndDisplayedItemsOrNull = selectedAndDisplayedItems;
    }

    public DataSetListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext,
            AbstractAsyncCallback<Void> permanentDeletionCallback,
            AbstractAsyncCallback<Void> deletionCallback, ExternalData data)
    {
        super(viewContext, Collections.singletonList(data), permanentDeletionCallback);
        if (DeletionUtils.isDeleted(data) == false)
        {
            this.withDeletion(deletionCallback);
        }
        this.viewContext = viewContext;
        this.singleData = data;
        this.selectedAndDisplayedItemsOrNull = null;
    }

    @Override
    protected void executeDeletion(AsyncCallback<Void> deletionCallback)
    {
        final DeletionType deletionType = getDeletionType();
        if (selectedAndDisplayedItemsOrNull != null)
        {
            final DisplayedOrSelectedDatasetCriteria uploadCriteria =
                    selectedAndDisplayedItemsOrNull.createCriteria(isOnlySelected());
            viewContext.getCommonService().deleteDataSets(uploadCriteria, reason.getValue(),
                    deletionType, deletionCallback);
        } else
        {
            viewContext.getCommonService().deleteDataSet(singleData.getCode(), reason.getValue(),
                    deletionType, deletionCallback);
        }
    }

    @Override
    protected String getEntityName()
    {
        return EntityKind.DATA_SET.getDescription();
    }

    @Override
    protected final RadioGroup createRadio()
    {
        return WidgetUtils.createAllOrSelectedRadioGroup(
                onlySelectedRadioOrNull =
                        WidgetUtils.createRadio(viewContext.getMessage(Dict.ONLY_SELECTED_RADIO,
                                data.size())), WidgetUtils.createRadio(viewContext.getMessage(
                        Dict.ALL_RADIO, selectedAndDisplayedItemsOrNull.getDisplayedItemsCount())),
                viewContext.getMessage(Dict.DATA_SETS_RADIO_GROUP_LABEL), data.size());
    }
}
