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

import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.core.Position;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.CheckboxConfig;
import com.gwtext.client.widgets.form.ColumnConfig;
import com.gwtext.client.widgets.form.Form;
import com.gwtext.client.widgets.form.FormConfig;
import com.gwtext.client.widgets.form.Radio;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.TextFieldConfig;
import com.gwtext.client.widgets.form.VType;

import ch.systemsx.cisd.cifex.client.ICIFEXServiceAsync;
import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * @author Basil Neff
 */
public class CreateUserWidget extends Form
{
    private static final String ID_PREFIX = "CreateUserWidget-";

    private final ViewContext context;

    private final IMessageResources messageResources;

    private static final int FIELD_WIDTH = 175;

    private static final int COLUMN_WIDTH = 300;

    private TextField userCodeField;
    
    private TextField emailField;

    private TextField fullNameField;

    private TextField passwordField;

    private TextField validatePasswordField;

    private Radio adminRadioButton;

    private Radio permanentRadioButton;

    private Radio temporaryRadioButton;

    private Button submitButton;

    private final boolean allowPermanentUsers;

    public CreateUserWidget(final ViewContext context, final boolean allowPermanentUsers)
    {
        super(Ext.generateId(ID_PREFIX), createFormConfig());
        this.context = context;
        this.messageResources = context.getMessageResources();
        this.allowPermanentUsers = allowPermanentUsers;
        createCreateUserForm();
    }

    private final void createCreateUserForm()
    {
        final ColumnConfig leftColumn = new ColumnConfig();
        leftColumn.setWidth(COLUMN_WIDTH);
        column(leftColumn);

        userCodeField = createCodeField();
        add(userCodeField);
        
        emailField = createEmailField();
        add(emailField);

        passwordField = createPasswordField();
        add(passwordField);

        end();

        final ColumnConfig rightColumn = new ColumnConfig();
        rightColumn.setWidth(COLUMN_WIDTH);
        column(rightColumn);

        fullNameField = createFullNameField();
        add(fullNameField);

        validatePasswordField = createValidatePasswordField();
        add(validatePasswordField);
        end();

        final ColumnConfig lastColumn = new ColumnConfig();
        lastColumn.setWidth(COLUMN_WIDTH - 150);
        column(lastColumn);

        if (allowPermanentUsers)
        {
            adminRadioButton = createAdminRadioButton();
            add(adminRadioButton);

            permanentRadioButton = createPermanentRadioButton();
            add(permanentRadioButton);

            temporaryRadioButton = createTemporaryRadioButton();
            add(temporaryRadioButton);
        }
        end();

        submitButton = addButton(messageResources.getUserCreateButton());
        submitButton.addButtonListener(new ButtonListenerAdapter()
            {

                //
                // ButtonListenerAdapter
                //

                public final void onClick(final Button but, final EventObject e)
                {
                    submitForm();
                }

            });
        render();
    }

    private static FormConfig createFormConfig()
    {
        final FormConfig formConfig = new FormConfig();
        formConfig.setWidth(750);
        formConfig.setLabelAlign(Position.LEFT);
        formConfig.setButtonAlign(Position.RIGHT);
        return formConfig;
    }

    private final TextField createCodeField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(messageResources.getUserCodeLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setVtype(VType.ALPHANUM);
        fieldConfig.setName(messageResources.getUserCodeLabel());
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidateOnBlur(false);
        fieldConfig.setTabIndex(1);
        return new TextField(fieldConfig);
    }

    private final TextField createEmailField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(messageResources.getUserEmailLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setVtype(VType.EMAIL);
        fieldConfig.setName(messageResources.getUserEmailLabel());
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidateOnBlur(false);
        fieldConfig.setTabIndex(1);
        return new TextField(fieldConfig);
    }

    private final TextField createFullNameField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(messageResources.getUserFullNameLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName(messageResources.getUserFullNameLabel());
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidateOnBlur(false);
        fieldConfig.setTabIndex(2);
        return new TextField(fieldConfig);
    }

    private final TextField createPasswordField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(messageResources.getPasswordLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName(messageResources.getPasswordLabel());
        fieldConfig.setPassword(true);
        fieldConfig.setAllowBlank(true);
        fieldConfig.setValidateOnBlur(false);
        fieldConfig.setTabIndex(3);
        fieldConfig.setMinLength(4);
        return new TextField(fieldConfig);
    }

    private final TextField createValidatePasswordField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(messageResources.getValidatePasswordLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName(messageResources.getValidatePasswordLabel());
        fieldConfig.setPassword(true);
        fieldConfig.setAllowBlank(true);
        fieldConfig.setValidateOnBlur(false);
        fieldConfig.setTabIndex(4);
        fieldConfig.setMinLength(4);
        return new TextField(fieldConfig);
    }

    private final Radio createAdminRadioButton()
    {

        final CheckboxConfig checkboxConfig = new CheckboxConfig();
        checkboxConfig.setFieldLabel(messageResources.getAdminRoleName());
        checkboxConfig.setName("role");
        checkboxConfig.setChecked(false);
        checkboxConfig.setTabIndex(5);
        return new Radio(checkboxConfig);
    }

    private final Radio createPermanentRadioButton()
    {
        final CheckboxConfig checkboxConfig = new CheckboxConfig();
        checkboxConfig.setFieldLabel(messageResources.getPermanentRoleName());
        checkboxConfig.setName("role");
        checkboxConfig.setChecked(true);
        checkboxConfig.setTabIndex(6);
        return new Radio(checkboxConfig);
    }

    private final Radio createTemporaryRadioButton()
    {
        final CheckboxConfig checkboxConfig = new CheckboxConfig();
        checkboxConfig.setFieldLabel(messageResources.getTemporaryRoleName());
        checkboxConfig.setName("role");
        checkboxConfig.setChecked(false);
        checkboxConfig.setTabIndex(7);
        return new Radio(checkboxConfig);
    }

    private void submitForm()
    {
        submitButton.disable();
        // Check if passwords are equal.
        if (passwordField.getValueAsString().equals(validatePasswordField.getValueAsString()) == false)
        {
            MessageBox.alert(messageResources.getMessageBoxErrorTitle(), messageResources.getPasswordMissmatchMessage());
            submitButton.enable();
            return;
        }

        if (userCodeField.validate() && fullNameField.validate() && passwordField.validate() && emailField.validate())
        {

            User user = new User();
            user.setUserCode(userCodeField.getText());
            user.setEmail(emailField.getText());
            user.setUserFullName(fullNameField.getText());
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
            cifexService.tryToCreateUser(user, password, context.getModel().getUser(), new CreateUserAsyncCallBack());
        } else
        {
            submitButton.enable();
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
            submitButton.enable();
        }

        public final void onSuccess(final Object result)
        {
            String title = messageResources.getMessageBoxInfoTitle();
            MessageBox.alert(title, messageResources.getUserCreationSuccessMessage(emailField.getText()));
            context.getPageController().createMainPage();
        }
    }

}