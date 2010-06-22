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

import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.sample.SampleBrowserGrid.DisplayedAndSelectedSamples;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedIdHolderCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Sample;

public final class SampleListDeletionConfirmationDialog extends
        AbstractDataListDeletionConfirmationDialog<Sample>
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final AsyncCallback<Void> callback;

    private final DisplayedAndSelectedSamples selectedAndDisplayedItemsOrNull;

    private final Sample singleDataOrNull;

    public SampleListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext, List<Sample> data,
            AsyncCallback<Void> callback, DisplayedAndSelectedSamples selectedAndDisplayedItems)
    {
        super(viewContext, data, true);
        this.viewContext = viewContext;
        this.callback = callback;
        this.singleDataOrNull = null;
        this.selectedAndDisplayedItemsOrNull = selectedAndDisplayedItems;
    }

    public SampleListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext, List<Sample> data,
            AbstractAsyncCallback<Void> callback, Sample sample)
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
        if (selectedAndDisplayedItemsOrNull != null)
        {
            final DisplayedOrSelectedIdHolderCriteria<Sample> uploadCriteria =
                    selectedAndDisplayedItemsOrNull.createCriteria(isOnlySelected());
            viewContext.getCommonService().deleteSamples(uploadCriteria, reason.getValue(),
                    callback);
        } else
        {
            viewContext.getCommonService().deleteSample(TechId.create(singleDataOrNull),
                    reason.getValue(), callback);
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
        return WidgetUtils.createAllOrSelectedRadioGroup(onlySelectedRadioOrNull =
                WidgetUtils.createRadio(viewContext.getMessage(Dict.ONLY_SELECTED_RADIO, data
                        .size())), WidgetUtils.createRadio(viewContext.getMessage(Dict.ALL_RADIO,
                selectedAndDisplayedItemsOrNull.getDisplayedItemsCount())), viewContext
                .getMessage(Dict.SAMPLES_RADIO_GROUP_LABEL), data.size(),
                createRefreshMessageAction());
    }

}
