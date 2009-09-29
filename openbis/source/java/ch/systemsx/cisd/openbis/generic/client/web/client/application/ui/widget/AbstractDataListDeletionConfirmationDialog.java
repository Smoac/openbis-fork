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

import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.ReasonField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.WidgetUtils;

/**
 * {@link AbstractDataConfirmationDialog} abstract implementation for deleting given list of data on
 * confirm.
 * 
 * @author Piotr Buczek
 */
public abstract class AbstractDataListDeletionConfirmationDialog<T> extends
        AbstractDataConfirmationDialog<List<T>>
{
    private static final int LABEL_WIDTH = 60;

    private static final int FIELD_WIDTH = 180;

    private static final String ALL_EMPHASIZED = "<b>ALL</b> displayed ";

    private static final String SELECTED = "selected ";

    private final boolean withRadio;

    protected Radio onlySelectedRadioOrNull;

    protected ReasonField reason;

    public AbstractDataListDeletionConfirmationDialog(IMessageProvider messageProvider,
            List<T> data, boolean withRadio)
    {
        super(messageProvider, data, messageProvider.getMessage(Dict.DELETE_CONFIRMATION_TITLE));
        this.withRadio = withRadio;
    }

    // without radio
    public AbstractDataListDeletionConfirmationDialog(IMessageProvider messageProvider, List<T> data)
    {
        this(messageProvider, data, false);
    }

    @Override
    protected void extendForm()
    {
        formPanel.setLabelWidth(LABEL_WIDTH);
        formPanel.setFieldWidth(FIELD_WIDTH);

        reason = new ReasonField(messageProvider, true);
        reason.focus();
        reason.addKeyListener(keyListener);
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
        return messageProvider.getMessage(Dict.DELETE_CONFIRMATION_MESSAGE_WITH_REASON,
                deletedObjects);
    }

    protected abstract String getEntityName();

    /**
     * This method should be overriden in subclasses if dialog is supposed to use a radio and set
     * {@link #onlySelectedRadioOrNull}.
     */
    protected RadioGroup createRadio()
    {
        return null;
    }

    protected final boolean isOnlySelected()
    {
        return WidgetUtils.isSelected(onlySelectedRadioOrNull);
    }
}
