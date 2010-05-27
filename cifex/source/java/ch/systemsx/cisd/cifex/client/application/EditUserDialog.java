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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;

import ch.systemsx.cisd.cifex.client.application.grid.GridWidget;
import ch.systemsx.cisd.cifex.client.application.model.UserGridModel;
import ch.systemsx.cisd.cifex.client.application.page.UserGridRefresherCallback;
import ch.systemsx.cisd.cifex.client.application.ui.DefaultLayoutDialog;
import ch.systemsx.cisd.cifex.client.application.ui.EditUserWidget;
import ch.systemsx.cisd.cifex.client.application.ui.UserWidget;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * A <code>AbstractLayoutDialog</code> to edit an user.
 * <p>
 * This dialog window comes when the user is a <i>regular</i> one or an <i>administrator</i>.
 * </p>
 * 
 * @author Basil Neff
 */
public final class EditUserDialog extends DefaultLayoutDialog
{
    private final EditUserWidget editUserWidget;

    public EditUserDialog(final ViewContext context, final UserInfoDTO user,
            final GridWidget<UserGridModel> userGrid)
    {
        super(context.getMessageResources(), getInternationalizedLabel(EDIT_USER_DIALOG_TITLE, user
                .getUserCode()), UserWidget.TOTAL_WIDTH + 30, getHeight(context));
        editUserWidget =
                new EditUserWidget(context, context.getModel().getUser().isAdmin(), user, false)
                    {

                        @Override
                        protected final void finishEditing()
                        {
                            new UserGridRefresherCallback(context, userGrid).onSuccess(null);
                        }
                    };
        add(editUserWidget);
        createUpdateButton();
    }

    private static int getHeight(final ViewContext context)
    {
        // If the system has external authentication the dialog has one more field and needs to be
        // higher.
        final boolean hasExternalAuthentication =
                context.getModel().getConfiguration().getSystemHasExternalAuthentication();
        return hasExternalAuthentication ? 330 : 320;
    }

    private void createUpdateButton()
    {
        final Button button = new Button(editUserWidget.getSubmitButtonLabel());
        button.addSelectionListener(new SelectionListener<ButtonEvent>()
            {

                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    editUserWidget.submitForm();
                    hide();
                }
            });
        addButton(button);
    }

}
