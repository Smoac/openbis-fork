/*
 * Copyright 2008 ETH Zuerich, CISD
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

package ch.systemsx.cisd.cifex.client.application;

import com.google.gwt.user.client.ui.Widget;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.LayoutDialog;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.ui.ModelBasedGrid;
import ch.systemsx.cisd.cifex.client.application.ui.RecipientsEmailsForm;

/**
 * A dialog to add an user.
 * 
 * @author Izabela Adamczyk
 */

class FileShareAddUserDialog extends DefaultLayoutDialog
{

    final ViewContext viewContext;

    final RecipientsEmailsForm emailsForm;

    public FileShareAddUserDialog(ViewContext viewContext, String fileId,
            String fileName, ModelBasedGrid userGrid, LayoutDialog parent)
    {
        super(viewContext.getMessageResources(), viewContext.getMessageResources()
                .getFileSharingAddUserTitle(), DEFAULT_WIDTH, 180);
        this.viewContext = viewContext;
        this.emailsForm = new RecipientsEmailsForm(viewContext, userGrid, fileId, parent);
        createOkButton();
        addContentPanel();

    }

    protected Widget createContentWidget()
    {

        return emailsForm;
    }

    protected final String getCloseButtonLabel()
    {
        return messageResources.getActionCancelLabel();
    }

    private final void createOkButton()
    {
        final Button button = addButton("Ok");
        button.addButtonListener(new ButtonListenerAdapter()
            {

                public void onClick(final Button b, final EventObject e)
                {
                    emailsForm.submitForm();
                    hide();
                }
            });
    }

}
