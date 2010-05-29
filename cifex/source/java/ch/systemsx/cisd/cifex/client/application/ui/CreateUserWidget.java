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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import ch.systemsx.cisd.cifex.client.ICIFEXServiceAsync;
import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * A <code>UserWidget</code> extension suitable for user creation.
 * <p>
 * This widget is used when a <i>regular</i> user creates new <i>temporary</i> users or/and when the
 * administrator creates new users (<i>Administrator</i>, <i>Regular</i> or <i>Temporary</i>).
 * </p>
 * 
 * @author Basil Neff
 */
public class CreateUserWidget extends UserWidget
{

    public CreateUserWidget(final ViewContext context, final boolean addStatusField)
    {
        super(context, addStatusField);
    }

    //
    // UserWidget
    //

    @Override
    final void submitForm()
    {
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
            String comment = null;
            if (commentArea != null)
            {
                comment = commentArea.getValue();
            }
            final ICIFEXServiceAsync cifexService = context.getCifexService();
            cifexService.createUser(user, StringUtils.nullIfBlank(passwordField.getValue()),
                    comment, new CreateUserAsyncCallBack());
        }
    }

    @Override
    final String getSubmitButtonLabel()
    {
        if (context.getModel().getUser().isAdmin())
        {
            return msg(CREATE_USER_LABEL);
        } else
        {
            return msg(CREATE_TEMP_USER_LABEL);
        }
    }

    //
    // Helper classes
    //

    private final class CreateUserAsyncCallBack extends AbstractAsyncCallback<UserInfoDTO>
    {

        CreateUserAsyncCallBack()
        {
            super(context);
        }

        @Override
        public final void onFailure(final Throwable caught)
        {
            super.onFailure(caught);
            if (buttonOrNull != null)
            {
                buttonOrNull.enable();
            }
        }

        public final void onSuccess(final UserInfoDTO result)
        {
            context.getPageController().refreshMainPage();
        }
    }
}