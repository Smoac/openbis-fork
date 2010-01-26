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

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.utils.CifexDict;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.dto.CurrentUserInfoDTO;

/**
 * @author Chandrasekhar Ramakrishnan
 */
public class LoginPanelAutofill extends VerticalPanel
{
    private final ViewContext context;

    private final Button button;

    private final FormPanel formPanel;

    private static final String LOGIN_FORM_ID = "cifex_login_form";

    private static final String USERNAME_ID = "cifex-username";

    private static final String PASSWORD_ID = "cifex-password";

    public LoginPanelAutofill(final ViewContext context)
    {
        this.context = context;

        // RootPanel rootPanel = RootPanel.get(LOGIN_FORM_ID);
        // System.err.println(rootPanel);
        // rootPanel.setVisible(true);

        Element formElement = Document.get().getElementById(LOGIN_FORM_ID);
        // UIObject.setVisible(formElement, true);
        if (formElement == null)
        {
            System.err.println("Formelement is null!");
            formPanel = null;
            button = null;
            return;
        }
        formPanel = FormPanel.wrap(Document.get().getElementById(LOGIN_FORM_ID), false);
        button = createButton();
        formPanel.add(button);
        formPanel.addSubmitHandler(new SubmitHandler()
            {

                public void onSubmit(SubmitEvent event)
                {
                    if (!isUserInputValid())
                        event.cancel();
                    else
                        doLogin(context);
                }

            });

        // NOTE: it would be better to invoke it on reset but it somehow doesn't
        // have any effect
        focusOnFirstField();

        add(formPanel);

        Text label = new Text(CifexDict.get(CifexDict.WELCOME_NOTE));
        label.setStyleName("cifex-welcome-warning");

        add(label);
    }

    private final Button createButton()
    {
        final Button b = new Button(context.getMessageResources().getLoginButtonLabel());
        b.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    // Need the sumbit so Firefox knows that it can remember the password
                    // information.
                    formPanel.submit();
                }
            });
        return b;
    }

    private final boolean isUserInputValid()
    {
        String username = getUsernameElement().getValue();
        String password = getPasswordElement().getValue();
        // Validate the input -- neither field can be blank
        return !StringUtils.isBlank(username) && !StringUtils.isBlank(password);
    }

    private void focusOnFirstField()
    {
        getUsernameElement().focus();
    }

    /** Returns the button that will starts the login process. */
    public final Button getButton()
    {
        return button;
    }

    @Override
    protected final void onLoad()
    {
        super.onLoad();
        button.enable();
        System.err.println("onLoad " + Document.get().getElementById(LOGIN_FORM_ID));
    }

    private final void doLogin(final ViewContext viewContext)
    {
        button.disable();

        InputElement usernameElement = getUsernameElement();
        InputElement passwordElement = getPasswordElement();

        final String user = usernameElement.getValue();
        final String password = passwordElement.getValue();

        viewContext.getCifexService().tryLogin(user, password, new LoginAsyncCallBack());
    }

    public InputElement getPasswordElement()
    {
        return InputElement.as(Document.get().getElementById(PASSWORD_ID));
    }

    public InputElement getUsernameElement()
    {
        return InputElement.as(Document.get().getElementById(USERNAME_ID));
    }

    /**
     * Method that gets called when everything goes right.
     * <p>
     * By default this method does nothing.
     * </p>
     */
    protected void loginSuccessful(final CurrentUserInfoDTO currentUser)
    {
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
            getButton().enable();
        }

        public final void onSuccess(final CurrentUserInfoDTO result)
        {
            if (result != null)
            {
                loginSuccessful(result);
            } else
            {
                final IMessageResources messageResources = context.getMessageResources();
                final String title = messageResources.getMessageBoxWarningTitle();
                MessageBox.alert(title, messageResources.getLoginFailedMessage(), null);
                getButton().enable();
            }
        }
    }
}
