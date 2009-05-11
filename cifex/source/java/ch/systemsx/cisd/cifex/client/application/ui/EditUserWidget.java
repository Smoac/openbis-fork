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

import ch.systemsx.cisd.cifex.client.ICIFEXServiceAsync;
import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;

/**
 * A <code>UserWidget</code> extension suitable for user editing.
 * <p>
 * This widget is used when the user edits his own profile or/and when the administrator edits
 * registered users.
 * </p>
 * 
 * @author Basil Neff
 */
public class EditUserWidget extends UserWidget
{
    public EditUserWidget(final ViewContext context, final boolean addStatusField, final UserInfoDTO user,
            final boolean withButton)
    {
        super(context, addStatusField, user, withButton);
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

    //
    // UserWidget
    //

    public final void submitForm()
    {

        final ICIFEXServiceAsync cifexService = context.getCifexService();
        if (arePasswordsEqual() == false)
        {
            return;
        }
        if (isValid())
        {
            if (buttonOrNull != null)
            {
                buttonOrNull.disable();
            }
            final UserInfoDTO user = createFromFields();
            cifexService.updateUser(user, StringUtils.nullIfBlank(passwordField.getText()),
                    (sendUpdateInformation != null && sendUpdateInformation.getValue()),
                    new UpdateUserAsyncCallBack());
        }
    }

    public final String getSubmitButtonLabel()
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
            if (buttonOrNull != null)
            {
                buttonOrNull.enable();
            }
        }

        public final void onSuccess(final Object result)
        {
            if (buttonOrNull != null)
            {
                buttonOrNull.enable();
            }
            final UserInfoDTO user = context.getModel().getUser();
            // Update current user, if it was the one who has been changed.
            if (user.getUserCode().equals(userCodeField.getText())
                    && user.isExternallyAuthenticated() == false)
            {
                user.setEmail(emailField.getText());
                user.setUserFullName(usernameField.getValueAsString());
                context.getModel().setUser(user);
            }
            finishEditing();
        }
    }
}
