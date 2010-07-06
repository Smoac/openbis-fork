/*
 * Copyright 2010 ETH Zuerich, CISD
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

import static ch.systemsx.cisd.cifex.client.application.utils.InfoDictionary.*;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.*;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.shared.basic.dto.CurrentUserInfoDTO;
import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;

/**
 * This class uses a variant of a trick described in the GWT discussion forum to support autofill.
 * Browsers do not support autofill on input fields that are generated on the client by javascript,
 * so it is necessary for the page to statically contain the input fields we want to autofill. These
 * fields are unhidden and used on the login page.
 * 
 * @see <a href
 *      ="http://groups.google.com/group/Google-Web-Toolkit/browse%5Fthread/thread/2b2ce0b6aaa82461">GWT
 *      Discussion Forum</a>
 * @author Chandrasekhar Ramakrishnan
 */
// TODO 2010-03-10, CR: This implementation currently supports Firefox, but not Safari or Chrome. To
// support Safari, we cannot use javascript in the action, instead we need to have the login post
// data to a server.
public class LoginPanelAutofill extends VerticalPanel
{
    private final ViewContext context;

    private final FormPanel formPanel;

    private static final String LOGIN_FORM_ID = "cifex_login_form";

    private static final String USERNAME_ID = "cifex-username";

    private static final String PASSWORD_ID = "cifex-password";

    private static final String SUBMIT_ID = "cifex-submit";

    private static LoginPanelAutofill singleton = null;

    /**
     * Method to get the singleton instance of the login autofill panel
     */
    public static LoginPanelAutofill get(final ViewContext context)
    {
        if (singleton == null)
        {
            singleton = new LoginPanelAutofill(context);
        }
        return singleton;
    }

    private LoginPanelAutofill(final ViewContext context)
    {
        this.context = context;

        setHorizontalAlign(HorizontalAlignment.CENTER);

        Element formElement = Document.get().getElementById(LOGIN_FORM_ID);
        if (formElement == null)
        {
            // This is an error and should not happen
            formPanel = null;
            return;
        }
        formPanel = FormPanel.wrap(formElement, false);

        formPanel.addSubmitHandler(new SubmitHandler()
            {
                public void onSubmit(SubmitEvent event)
                {
                    if (isUserInputValid() == false)
                    {
                        event.cancel();
                    } else
                    {
                        doLogin();
                    }
                }
            });

        add(formPanel);

        Text label = new Text(info(START_PAGE_WELCOME_NOTE));
        label.setStyleName("cifex-welcome-warning");

        add(label);
    }

    private final boolean isUserInputValid()
    {
        String username = getUsernameElement().getValue();
        String password = getPasswordElement().getValue();
        // Validate the input -- neither field can be blank
        return StringUtils.isBlank(username) == false && StringUtils.isBlank(password) == false;
    }

    private void giveFocusToFirstField()
    {
        getUsernameElement().focus();
    }

    @Override
    protected final void onLoad()
    {
        super.onLoad();
        getButtonElement().setDisabled(false);
        giveFocusToFirstField();
    }

    private final void doLogin()
    {
        getButtonElement().setDisabled(true);

        InputElement usernameElement = getUsernameElement();
        InputElement passwordElement = getPasswordElement();

        final String user = usernameElement.getValue();
        final String password = passwordElement.getValue();

        context.getCifexService().tryLogin(user, password, new LoginAsyncCallBack());
    }

    public InputElement getPasswordElement()
    {
        return InputElement.as(Document.get().getElementById(PASSWORD_ID));
    }

    public InputElement getUsernameElement()
    {
        return InputElement.as(Document.get().getElementById(USERNAME_ID));
    }

    public final InputElement getButtonElement()
    {
        return InputElement.as(Document.get().getElementById(SUBMIT_ID));
    }

    /**
     * Method that gets called when everything goes right.
     * <p>
     * By default this method does nothing.
     * </p>
     */
    protected void loginSuccessful(final CurrentUserInfoDTO currentUser)
    {
        // Clear the password
        getPasswordElement().setValue(getPasswordElement().getDefaultValue());
        PageControllerHelper.activatePageBasedOnCurrentContext(context, currentUser);
    }

    //
    // Helper classes
    //

    private final class LoginAsyncCallBack extends AbstractAsyncCallback<CurrentUserInfoDTO>
    {

        LoginAsyncCallBack()
        {
            super(context);
        }

        @Override
        public final void onFailure(final Throwable caught)
        {
            super.onFailure(caught);
            getButtonElement().setDisabled(false);
        }

        public final void onSuccess(final CurrentUserInfoDTO result)
        {
            if (result != null)
            {
                loginSuccessful(result);
            } else
            {
                final String title = msg(MESSAGE_BOX_WARNING_TITLE);
                MessageBox.alert(title, msg(LOGIN_FAILED_MSG), null);
                getButtonElement().setDisabled(false);
            }
        }
    }
}
