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

package ch.systemsx.cisd.cifex.client.application.ui;

import com.gwtext.client.widgets.MessageBox;

import ch.systemsx.cisd.cifex.client.ICIFEXServiceAsync;
import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * @author Basil Neff
 */
public class CreateUserWidget extends UserWidget
{

    public CreateUserWidget(final ViewContext context, final boolean allowPermanentUsers)
    {
        super(context, allowPermanentUsers);
    }

    protected void submitForm()
    {
        // Check if passwords are equal.
        if (passwordField.getValueAsString().equals(validatePasswordField.getValueAsString()) == false)
        {
            MessageBox
                    .alert(messageResources.getMessageBoxErrorTitle(), messageResources.getPasswordMissmatchMessage());
            return;
        }

        if (usernameField.validate() && passwordField.validate() && emailField.validate())
        {

            User user = new User();
            user.setEmail(emailField.getText());
            user.setUserFullName(usernameField.getText());
            user.setUserCode(userCodeField.getText());
            String comment = null;
            if (commentArea != null)
            {
                comment = commentArea.getText();
            }
            if (allowPermanentUsers)
            {
                if (adminRadioButton.getValue())
                {
                    user.setAdmin(true);
                    user.setPermanent(true);
                } else if (permanentRadioButton.getValue())
                {
                    user.setAdmin(false);
                    user.setPermanent(true);
                } else
                {
                    user.setAdmin(false);
                    user.setPermanent(false);
                }
            } else
            {
                user.setAdmin(false);
                user.setPermanent(false);
            }

            String password = passwordField.getText();

            ICIFEXServiceAsync cifexService = context.getCifexService();
            cifexService.createUser(user, password, context.getModel().getUser(), comment,
                    new CreateUserAsyncCallBack());
        }
    }

    //
    // Helper classes
    //

    private final class CreateUserAsyncCallBack extends AbstractAsyncCallback
    {

        CreateUserAsyncCallBack()
        {
            super(context);
        }

        //
        // AsyncCallback
        //

        public final void onFailure(final Throwable caught)
        {
            super.onFailure(caught);
        }

        public final void onSuccess(final Object result)
        {
            context.getPageController().createAdminPage();
        }
    }

    String getSubmitButtonLabel()
    {
        if (context.getModel().getUser().isAdmin())
        {
            return messageResources.getAdminCreateUserLabel();
        } else
        {
            return messageResources.getCreateUserLabel();
        }
    }

}