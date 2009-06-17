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

import com.extjs.gxt.ui.client.widget.form.TextField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.Dict;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.field.VarcharField;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.util.IMessageProvider;

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

    protected TextField<String> reason;

    public AbstractDataListDeletionConfirmationDialog(IMessageProvider messageProvider, List<T> data)
    {
        super(messageProvider, data, Dict.DELETE_CONFIRMATION_TITLE);
    }

    @Override
    protected final void extendForm()
    {
        formPanel.setLabelWidth(LABEL_WIDTH);
        formPanel.setFieldWidth(FIELD_WIDTH);

        reason = new VarcharField(messageProvider.getMessage(Dict.REASON), true);
        reason.focus();
        reason.setMaxLength(250);
        reason.addKeyListener(keyListener);
        formPanel.add(reason);
    }

    @Override
    protected String createMessage()
    {
        return messageProvider.getMessage(Dict.DELETE_CONFIRMATION_MESSAGE_WITH_REASON,
                data.size(), getEntityName());
    }

    protected abstract String getEntityName();
}
