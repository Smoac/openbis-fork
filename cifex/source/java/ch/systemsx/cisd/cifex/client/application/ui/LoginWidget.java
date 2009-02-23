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

import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.core.Position;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.event.KeyListener;
import com.gwtext.client.widgets.form.Form;
import com.gwtext.client.widgets.form.FormConfig;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.TextFieldConfig;

import ch.systemsx.cisd.cifex.client.ICIFEXServiceAsync;
import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.Model;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;

/**
 * A <code>Form</code> extension that is the login panel we use to login to the <i>openBIS</i>
 * system.
 * 
 * @author Christian Ribeaud
 */
public class LoginWidget extends Form
{
    private static final String ID_PREFIX = "LoginWidget-";

    private static final int FIELD_WIDTH = 175;

    private final ViewContext context;

    private TextField userField;

    private TextField passwordField;

    private Button button;

    public LoginWidget(final ViewContext context)
    {
        super(Ext.generateId(ID_PREFIX), createFormConfig());
        this.context = context;
        createLoginForm();
    }

    private final void createLoginForm()
    {
        final IMessageResources messageResources = context.getMessageResources();
        fieldset(messageResources.getLoginLegend());
        userField = createUserField();
        add(userField);
        passwordField = createPasswordField();
        add(passwordField);

        // Do NOT use addButton(Button) here.
        // This does not seem to work correctly (while clearing RootPanel, we get an exception).
        button = addButton(messageResources.getLoginButtonLabel());
        button.addButtonListener(new ButtonListenerAdapter()
            {

                //
                // ButtonListenerAdapter
                //

                public final void onClick(final Button but, final EventObject e)
                {
                    submitForm();
                }

            });
        // Add a key listener for return key
        getEl().addKeyListener(EventObject.ENTER, new KeyListener()
            {
                //
                // KeyListener
                //

                public final void onKey(final int key, final EventObject e)
                {
                    submitForm();
                }
            });
        end();
        render();
    }

    private final static FormConfig createFormConfig()
    {
        final FormConfig formConfig = new FormConfig();
        formConfig.setWidth(300);
        formConfig.setLabelAlign(Position.LEFT);
        formConfig.setButtonAlign(Position.RIGHT);
        formConfig.setLabelWidth(75);
        return formConfig;
    }

    private final TextField createPasswordField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(context.getMessageResources().getLoginPasswordLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setPassword(true);
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidateOnBlur(false);
        return new TextField(fieldConfig);
    }

    private final TextField createUserField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(context.getMessageResources().getLoginUserLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidateOnBlur(false);
        return new TextField(fieldConfig);
    }

    /** Returns the button that will starts the login process. */
    public final Button getButton()
    {
        return button;
    }

    /** Submits given <var>loginForm</var>. */
    protected void submitForm()
    {
        if (isValid())
        {
            button.disable();
            final String username = userField.getText();
            final String password = passwordField.getText();
            final ICIFEXServiceAsync cifexService = context.getCifexService();
            cifexService.tryLogin(username, password, new LoginAsyncCallBack());
        }
    }

    /**
     * Method that gets called when everything goes right.
     * <p>
     * By default this method does nothing.
     * </p>
     */
    protected void loginSuccessful(final UserInfoDTO user)
    {
        final Model model = context.getModel();
        model.setUser(user);
        FileDownloadHelper.startFileDownload(model);
        context.getPageController().createMainPage();
    }

    public final TextField getUserField()
    {
        return userField;
    }

    public final TextField getPasswordField()
    {
        return passwordField;
    }

    //
    // Helper classes
    //

    private final class LoginAsyncCallBack extends AbstractAsyncCallback
    {

        LoginAsyncCallBack()
        {
            super(context);
        }

        //
        // AsyncCallback
        //

        public final void onFailure(final Throwable caught)
        {
            super.onFailure(caught);
            getButton().enable();
        }

        public final void onSuccess(final Object result)
        {
            if (result != null)
            {
                loginSuccessful((UserInfoDTO) result);
            } else
            {
                final IMessageResources messageResources = context.getMessageResources();
                final String title = messageResources.getMessageBoxWarningTitle();
                MessageBox.alert(title, messageResources.getLoginFailedMessage());
                getButton().enable();
            }
        }
    }
}