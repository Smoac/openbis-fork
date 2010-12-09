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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample;

import java.util.List;

import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AsyncCallbackWithProgressBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.DisplayedAndSelectedEntities;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedIdHolderCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.IIdHolder;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

public final class SampleListDeletionConfirmationDialog<T extends IIdHolder> extends
        AbstractDataListDeletionConfirmationDialog<T>
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final AsyncCallback<Void> callback;

    private final DisplayedAndSelectedEntities<T> selectedAndDisplayedItemsOrNull;

    private final T singleDataOrNull;

    public SampleListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext, List<T> data,
            AsyncCallback<Void> callback, DisplayedAndSelectedEntities<T> selectedAndDisplayedItems)
    {
        super(viewContext, data, true);
        this.viewContext = viewContext;
        this.callback = callback;
        this.singleDataOrNull = null;
        this.selectedAndDisplayedItemsOrNull = selectedAndDisplayedItems;
    }

    public SampleListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext, List<T> data,
            AbstractAsyncCallback<Void> callback, T sample)
    {
        super(viewContext, data, false);
        this.viewContext = viewContext;
        this.callback = callback;
        this.singleDataOrNull = sample;
        this.selectedAndDisplayedItemsOrNull = null;
    }

    @Override
    protected void executeConfirmedAction()
    {
        AsyncCallback<Void> callbackWithProgressBar =
                AsyncCallbackWithProgressBar.decorate(callback, "Deleting samples...");
        if (selectedAndDisplayedItemsOrNull != null)
        {
            final DisplayedOrSelectedIdHolderCriteria<T> uploadCriteria =
                    selectedAndDisplayedItemsOrNull.createCriteria(isOnlySelected());
            viewContext.getCommonService().deleteSamples(uploadCriteria, reason.getValue(),
                    callbackWithProgressBar);
        } else
        {
            viewContext.getCommonService().deleteSample(TechId.create(singleDataOrNull),
                    reason.getValue(), callbackWithProgressBar);
        }
    }

    @Override
    protected String getEntityName()
    {
        return EntityKind.SAMPLE.getDescription();
    }

    @Override
    protected final RadioGroup createRadio()
    {
        int dataSize = data.size();
        Radio one =
                WidgetUtils.createRadio(viewContext.getMessage(Dict.ONLY_SELECTED_RADIO, dataSize));
        onlySelectedRadioOrNull = one;
        Radio all =
                WidgetUtils.createRadio(viewContext.getMessage(Dict.ALL_RADIO,
                        selectedAndDisplayedItemsOrNull.getDisplayedItemsCount()));
        String label = viewContext.getMessage(Dict.SAMPLES_RADIO_GROUP_LABEL);
        return WidgetUtils.createAllOrSelectedRadioGroup(one, all, label, dataSize,
                createRefreshMessageAction());
    }

}
