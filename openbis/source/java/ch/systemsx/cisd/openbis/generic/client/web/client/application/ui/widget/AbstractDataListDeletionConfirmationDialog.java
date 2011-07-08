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

package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget;

import java.util.List;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.google.gwt.user.client.rpc.AsyncCallback;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.AsyncCallbackWithProgressBar;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.CheckBoxField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ReasonField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.DeletionType;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.WebClientConfiguration;

/**
 * {@link AbstractDataConfirmationDialog} abstract implementation for deleting given list of data on
 * confirm.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractDataListDeletionConfirmationDialog<T> extends
        AbstractDataConfirmationDialog<List<T>>
{
    private static final int LABEL_WIDTH = 70;

    private static final int FIELD_WIDTH = 180;

    private static final String ALL_EMPHASIZED = "<b>ALL</b> displayed ";

    private static final String SELECTED = " selected ";

    private final IViewContext<?> viewContext;

    private final AbstractAsyncCallback<Void> permanentDeletionCallback;

    private boolean withRadio = false;

    private boolean withDeletionOption = false;

    private AbstractAsyncCallback<Void> deletionCallbackOrNull;

    protected Radio onlySelectedRadioOrNull;

    protected CheckBoxField permanentCheckBoxOrNull;

    protected ReasonField reason;

    public AbstractDataListDeletionConfirmationDialog(IViewContext<?> viewContext, List<T> data,
            AbstractAsyncCallback<Void> permanentDeletionCallback)
    {
        super(viewContext, data, viewContext.getMessage(Dict.DELETE_CONFIRMATION_TITLE));
        this.viewContext = viewContext;
        this.permanentDeletionCallback = permanentDeletionCallback;
    }

    // optional initialization

    /** adds radio group for selecting between deletion of all or only selected data */
    protected void withRadio()
    {
        this.withRadio = true;
    }

    /**
     * adds deletion option to the dialog with the same callback as the one used for permanent
     * deletion
     */
    protected void withDeletion()
    {
        withDeletion(permanentDeletionCallback);
    }

    /** adds deletion option to the dialog with fiven callback */
    protected void withDeletion(AbstractAsyncCallback<Void> deletionCallback)
    {
        if (getWebClientConfiguration().getEnableTrash())
        {
            this.withDeletionOption = true;
            this.deletionCallbackOrNull = deletionCallback;
        }
    }

    private WebClientConfiguration getWebClientConfiguration()
    {
        return viewContext.getModel().getApplicationInfo().getWebClientConfiguration();
    }

    //

    @Override
    protected void extendForm()
    {
        formPanel.setLabelWidth(LABEL_WIDTH);
        formPanel.setFieldWidth(FIELD_WIDTH);

        reason = new ReasonField(messageProvider, true);
        reason.focus();
        reason.addKeyListener(keyListener);
        if (withDeletionOption)
        {
            formPanel.add(permanentCheckBoxOrNull = createDeletePermanentlyCheckBox());
        }
        if (withRadio)
        {
            formPanel.add(createRadio());
        }
        formPanel.add(reason);
    }

    @Override
    protected String createMessage()
    {
        String deletedObjects;
        if (withRadio == false)
        {
            deletedObjects = data.size() + " " + getEntityName();
        } else
        {
            deletedObjects =
                    (isOnlySelected() ? data.size() + SELECTED : ALL_EMPHASIZED) + getEntityName();
        }
        final String operationName =
                messageProvider.getMessage(isPermanentDeletion() ? Dict.DELETING_PERMANENTLY
                        : Dict.DELETING);
        return messageProvider.getMessage(Dict.DELETE_CONFIRMATION_MESSAGE_WITH_REASON_TEMPLATE,
                operationName, deletedObjects);
    }

    protected abstract String getEntityName();

    protected abstract void executeDeletion(AsyncCallback<Void> callback);

    @Override
    protected final void executeConfirmedAction()
    {
        executeDeletion(getCallbackWithProgressBar());
    }

    /**
     * This method should be overriden in subclasses if dialog is supposed to use a radio and set
     * {@link #onlySelectedRadioOrNull}.
     */
    protected RadioGroup createRadio()
    {
        return null;
    }

    private CheckBoxField createDeletePermanentlyCheckBox()
    {
        CheckBoxField result = new CheckBoxField(messageProvider.getMessage(Dict.PERMANENT), false);
        result.setValue(true);
        result.addListener(Events.Change, new Listener<FieldEvent>()
            {
                public void handleEvent(FieldEvent fe)
                {
                    refreshMessage();
                }
            });
        return result;
    }

    protected final boolean isOnlySelected()
    {
        return WidgetUtils.isSelected(onlySelectedRadioOrNull);
    }

    protected final boolean isPermanentDeletion()
    {
        return permanentCheckBoxOrNull == null || permanentCheckBoxOrNull.getValue();
    }

    protected final DeletionType getDeletionType()
    {
        return isPermanentDeletion() ? DeletionType.PERMANENT : DeletionType.TRASH;
    }

    /**
     * Returns deletion callback and shows a progress bar that will be hidden when the callback is
     * finished.
     */
    private AsyncCallback<Void> getCallbackWithProgressBar()
    {
        if (isPermanentDeletion())
        {
            return AsyncCallbackWithProgressBar.decorate(permanentDeletionCallback, messageProvider
                    .getMessage(Dict.DELETE_PERMANENTLY_PROGRESS_MESSAGE, getEntityName()));
        } else
        {
            assert deletionCallbackOrNull != null;
            return AsyncCallbackWithProgressBar.decorate(deletionCallbackOrNull,
                    messageProvider.getMessage(Dict.DELETE_PROGRESS_MESSAGE, getEntityName()));
        }
    }
}
