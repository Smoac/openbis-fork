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
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * A <code>UserWidget</code> extension suitable for user editing.
 * <p>
 * This widget is used when the user edits his own profile or/and when the administrator edits registered users.
 * </p>
 * 
 * @author Basil Neff
 */
public class EditUserWidget extends UserWidget
{
    public EditUserWidget(final ViewContext context, final boolean allowPermanentUsers, final User user)
    {
        super(context, allowPermanentUsers, user);
    }

    //
    // UserWidget
    //

    final void submitForm()
    {
        if (arePasswordsEqual() == false)
        {
            return;
        }
        if (isValid())
        {
            button.disable();
            final User user = new User();
            user.setEmail(emailField.getText());
            user.setUserFullName(usernameField.getText());
            user.setUserCode(userCodeField.getText());
            if (addStatusField)
            {
                user.setAdmin(isAdminStatus());
                user.setPermanent(isTemporaryStatus() == false);
            } else
            {
                user.setAdmin(editUser.isAdmin());
                user.setPermanent(editUser.isPermanent());
            }
            final ICIFEXServiceAsync cifexService = context.getCifexService();
            cifexService.updateUser(user, StringUtils.nullIfBlank(passwordField.getText()),
                    new UpdateUserAsyncCallBack());
        }
    }

    /**
     * This method is called after the editing has been performed and was successful.
     * <p>
     * By default, this method does nothing.
     * </p>
     */
    protected void finishEditing()
    {

    }

    final String getSubmitButtonLabel()
    {
        return getMessageResources().getEditUserButtonLabel();
    }

    //
    // Helper classes
    //

    private final class UpdateUserAsyncCallBack extends AbstractAsyncCallback
    {

        UpdateUserAsyncCallBack()
        {
            super(context);
        }

        //
        // AbstractAsyncCallback
        //

        public final void onFailure(final Throwable caught)
        {
            super.onFailure(caught);
            button.enable();
        }

        public final void onSuccess(final Object result)
        {
            button.enable();
            final User user = context.getModel().getUser();
            // Update current user, if it was the one who has been changed.
            if (user.getUserCode().equals(userCodeField.getText()) && user.isExternallyAuthenticated() == false)
            {
                user.setEmail(emailField.getText());
                user.setUserFullName(usernameField.getValueAsString());
                context.getModel().setUser(user);
            }
            final IMessageResources messageResources = getMessageResources();
            MessageBox.alert(messageResources.getMessageBoxInfoTitle(), messageResources
                    .getEditUserSuccessfulMessage(userCodeField.getText()));
            finishEditing();
        }
    }
}
