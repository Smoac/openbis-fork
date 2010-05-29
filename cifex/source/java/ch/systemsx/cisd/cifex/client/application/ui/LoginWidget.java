/*
 * Copyright 2007 ETH Zuerich, CISD
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
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.VerticalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.shared.basic.dto.CurrentUserInfoDTO;

/**
 * A <code>Form</code> extension that is the login panel we use to login to the <i>openBIS</i>
 * system.
 * 
 * @author Christian Ribeaud
 */
public class LoginWidget extends VerticalPanel
{

    private static final String PASSWORD_NAME_AND_ID = "password";

    private static final String USERNAME_NAME_AND_ID = "username";

    private final ViewContext context;

    private final TextField<String> userField;

    private final TextField<String> passwordField;

    private final Button button;

    private final FormPanel formPanel;

    public LoginWidget(final ViewContext context)
    {
        this.context = context;
        // setSpacing(10);

        formPanel = createFormPanel();
        userField = createUserField();
        formPanel.add(userField);
        passwordField = createPasswordField();
        formPanel.add(passwordField);
        button = createButton();
        formPanel.addButton(button);

        // NOTE: it would be better to invoke it on reset but it somehow doesn't
        // have any effect
        focusOnFirstField();

        add(formPanel);

        Text label = new Text(info(START_PAGE_WELCOME_NOTE));
        label.setStyleName("cifex-welcome-warning");

        add(label);
    }

    private final Button createButton()
    {
        final Button b = new Button(msg(LOGIN_BUTTON_LABEL));
        b.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    // Need the sumbit so Firefox knows that it can remember the password
                    // information.
                    formPanel.submit();
                    doLogin(context);
                }
            });
        return b;
    }

    private void focusOnFirstField()
    {
        formPanel.getItem(0).focus();
    }

    private final TextField<String> createUserField()
    {
        final TextField<String> field = new TextField<String>();
        field.setFieldLabel(msg(LOGIN_USER_LABEL));
        field.setAllowBlank(false);
        field.setValidateOnBlur(true);
        field.setName(USERNAME_NAME_AND_ID);
        field.setId(USERNAME_NAME_AND_ID);
        addEnterKeyListener(field, context);
        return field;
    }

    private final void addEnterKeyListener(final Field<String> field, final ViewContext viewContext)
    {
        field.addKeyListener(new EnterKeyListener()
            {

                @Override
                protected final void onEnterKey()
                {
                    doLogin(viewContext);
                }
            });
    }

    public final void resetFields()
    {
        userField.reset();
        passwordField.reset();
        button.enable();
    }

    private final TextField<String> createPasswordField()
    {
        final TextField<String> field = new TextField<String>();
        field.setPassword(true);
        field.setFieldLabel(msg(LOGIN_PASSWORD_LABEL));
        field.setAllowBlank(false);
        field.setValidateOnBlur(true);
        field.setName(PASSWORD_NAME_AND_ID);
        field.setId(PASSWORD_NAME_AND_ID);
        addEnterKeyListener(field, context);
        return field;
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
        resetFields();
    }

    private final void doLogin(final ViewContext viewContext)
    {
        if (formPanel.isValid())
        {
            button.disable();
            final String user = userField.getValue();
            final String password = passwordField.getValue();
            viewContext.getCifexService().tryLogin(user, password, new LoginAsyncCallBack());
        }
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

    public final TextField<String> getUserField()
    {
        return userField;
    }

    public final TextField<String> getPasswordField()
    {
        return passwordField;
    }

    private final static FormPanel createFormPanel()
    {
        final FormPanel formPanel = new FormPanel();
        // This style draws a strange-looking and unnecessary border.
        // formPanel.setStyleName("cifex-login-widget");
        formPanel.setBodyBorder(false);
        formPanel.setBorders(false);
        formPanel.setHeaderVisible(false);
        formPanel.setFieldWidth(130);
        formPanel.setWidth(250);
        formPanel.setButtonAlign(HorizontalAlignment.RIGHT);
        formPanel.setMethod(Method.POST);
        return formPanel;
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
                final String title = msg(MESSAGE_BOX_WARNING_TITLE);
                MessageBox.alert(title, msg(LOGIN_FAILED_MSG), null);
                getButton().enable();
            }
        }
    }
}