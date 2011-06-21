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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment;

import java.util.Collections;

import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.experiment.ExperimentBrowserGrid.DisplayedAndSelectedExperiments;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.AbstractDataListDeletionConfirmationDialog;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.dto.DisplayedOrSelectedIdHolderCriteria;
import ch.systemsx.cisd.openbis.generic.shared.basic.InvalidationUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.TechId;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Experiment;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;

public final class ExperimentListDeletionConfirmationDialog extends
        AbstractDataListDeletionConfirmationDialog<Experiment>
{

    private final IViewContext<ICommonClientServiceAsync> viewContext;

    private final DisplayedAndSelectedExperiments selectedAndDisplayedItemsOrNull;

    private final Experiment singleDataOrNull;

    public ExperimentListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext,
            AbstractAsyncCallback<Void> callback,
            DisplayedAndSelectedExperiments selectedAndDisplayedItems)
    {
        super(viewContext, selectedAndDisplayedItems.getExperiments(), callback);
        this.withInvalidation();
        this.withRadio();
        this.viewContext = viewContext;
        this.singleDataOrNull = null;
        this.selectedAndDisplayedItemsOrNull = selectedAndDisplayedItems;
    }

    public ExperimentListDeletionConfirmationDialog(
            IViewContext<ICommonClientServiceAsync> viewContext,
            AbstractAsyncCallback<Void> deletionCallback,
            AbstractAsyncCallback<Void> invalidationCallback, Experiment experiment)
    {
        super(viewContext, Collections.singletonList(experiment), deletionCallback);
        if (InvalidationUtils.isInvalid(experiment) == false)
        {
            this.withInvalidation(invalidationCallback);
        }
        this.viewContext = viewContext;
        this.singleDataOrNull = experiment;
        this.selectedAndDisplayedItemsOrNull = null;
    }

    @Override
    protected void executeDeletion(AsyncCallback<Void> deletionCallback)
    {
        final DeletionType deletionType = getDeletionType();
        if (selectedAndDisplayedItemsOrNull != null)
        {
            final DisplayedOrSelectedIdHolderCriteria<TableModelRowWithObject<Experiment>> uploadCriteria =
                    selectedAndDisplayedItemsOrNull.createCriteria(isOnlySelected());
            viewContext.getCommonService().deleteExperiments(uploadCriteria, reason.getValue(),
                    deletionType, deletionCallback);
        } else
        {
            viewContext.getCommonService().deleteExperiment(TechId.create(singleDataOrNull),
                    reason.getValue(), deletionType, deletionCallback);
        }
    }

    @Override
    protected String getEntityName()
    {
        return EntityKind.EXPERIMENT.getDescription();
    }

    @Override
    protected final RadioGroup createRadio()
    {
        return WidgetUtils.createAllOrSelectedRadioGroup(
                onlySelectedRadioOrNull =
                        WidgetUtils.createRadio(viewContext.getMessage(Dict.ONLY_SELECTED_RADIO,
                                data.size())), WidgetUtils.createRadio(viewContext.getMessage(
                        Dict.ALL_RADIO, selectedAndDisplayedItemsOrNull.getDisplayedItemsCount())),
                viewContext.getMessage(Dict.EXPERIMENTS_RADIO_GROUP_LABEL), data.size());
    }

}
