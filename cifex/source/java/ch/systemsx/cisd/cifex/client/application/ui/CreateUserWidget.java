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

import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.core.Position;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.Checkbox;
import com.gwtext.client.widgets.form.CheckboxConfig;
import com.gwtext.client.widgets.form.ColumnConfig;
import com.gwtext.client.widgets.form.Form;
import com.gwtext.client.widgets.form.FormConfig;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.TextFieldConfig;
import com.gwtext.client.widgets.form.VType;

import ch.systemsx.cisd.cifex.client.ICIFEXServiceAsync;
import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;

/**
 * @author Basil Neff
 */
public class CreateUserWidget extends Form
{
    private static final String ID_PREFIX = "CreateUserWidget-";

    private final ViewContext context;

    private static final int FIELD_WIDTH = 175;

    private static final int COLUMN_WIDTH = 300;

    private TextField emailField;

    private TextField usernameField;

    private TextField passwordField;

    private TextField validatePasswordField;

    private Checkbox adminCheckbox;

    private Checkbox permanentCheckbox;

    private Button submitButton;

    public CreateUserWidget(final ViewContext context)
    {
        super(Ext.generateId(ID_PREFIX), createFormConfig());
        this.context = context;
        createCreateUserForm();
    }

    private final void createCreateUserForm()
    {
        IMessageResources messageResources = context.getMessageResources();

        final ColumnConfig leftColumn = new ColumnConfig();
        leftColumn.setWidth(COLUMN_WIDTH);
        column(leftColumn);

        emailField = createEmailField();
        add(emailField);

        passwordField = createPasswordField();
        add(passwordField);

        adminCheckbox = createAdminCheckbox();
        add(adminCheckbox);

        end();

        final ColumnConfig rightColumn = new ColumnConfig();
        rightColumn.setWidth(COLUMN_WIDTH);
        column(rightColumn);

        usernameField = createUsernameField();
        add(usernameField);

        validatePasswordField = createValidatePasswordField();
        add(validatePasswordField);

        permanentCheckbox = createPermanentCheckbox();
        add(permanentCheckbox);

        end();

        // TODO 2008-1-28 Basil Neff: Get Field from MessageResource
        submitButton = addButton("Create");
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
        formConfig.setWidth(600);
        formConfig.setLabelAlign(Position.LEFT);
        formConfig.setButtonAlign(Position.RIGHT);
        return formConfig;
    }

    private final TextField createEmailField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        // TODO 2008-1-28 Basil Neff: Get Field from MessageResource
        fieldConfig.setFieldLabel("Email");
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setVtype(VType.EMAIL);
        fieldConfig.setName("Email");
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidateOnBlur(false);
        fieldConfig.setTabIndex(1);
        return new TextField(fieldConfig);
    }

    private final TextField createUsernameField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        // TODO 2008-1-28 Basil Neff: Get Field from MessageResource
        fieldConfig.setFieldLabel("Username");
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName("Username");
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidateOnBlur(false);
        fieldConfig.setTabIndex(2);
        return new TextField(fieldConfig);
    }

    private final TextField createPasswordField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        // TODO 2008-1-28 Basil Neff: Get Field from MessageResource
        fieldConfig.setFieldLabel("Password");
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName("Password");
        fieldConfig.setPassword(true);
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidateOnBlur(false);
        fieldConfig.setTabIndex(3);
        fieldConfig.setMinLength(4);
        return new TextField(fieldConfig);
    }

    private final TextField createValidatePasswordField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        // TODO 2008-1-28 Basil Neff: Get Field from MessageResource
        fieldConfig.setFieldLabel("Validate Password");
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName("Validate Password");
        fieldConfig.setPassword(true);
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidateOnBlur(false);
        fieldConfig.setTabIndex(4);
        fieldConfig.setMinLength(4);
        return new TextField(fieldConfig);
    }

    private final Checkbox createAdminCheckbox()
    {
        final CheckboxConfig checkboxConfig = new CheckboxConfig();
        // TODO 2008-1-28 Basil Neff: Get Field from MessageResource
        checkboxConfig.setFieldLabel("Administrator");
        checkboxConfig.setWidth(FIELD_WIDTH);
        checkboxConfig.setName("Administrator");
        checkboxConfig.setChecked(false);
        checkboxConfig.setTabIndex(5);
        return new Checkbox(checkboxConfig);
    }

    private final Checkbox createPermanentCheckbox()
    {
        final CheckboxConfig checkboxConfig = new CheckboxConfig();
        // TODO 2008-1-28 Basil Neff: Get Field from MessageResource
        checkboxConfig.setFieldLabel("Is Permanent");
        checkboxConfig.setWidth(FIELD_WIDTH);
        checkboxConfig.setName("Is Permanent");
        checkboxConfig.setChecked(true);
        checkboxConfig.setTabIndex(6);
        return new Checkbox(checkboxConfig);
    }

    protected void submitForm()
    {
        submitButton.disable();
        // Validate Password if they are equal
        if (passwordField.getValueAsString().equals(validatePasswordField.getValueAsString()) == false)
        {
            // TODO 2008-1-28 Basil Neff: Get Field from MessageResource
            MessageBox.alert("Password did not match", "The 2 Password fields did not match!");
            submitButton.enable();
            return;
        }

        if (usernameField.validate() && passwordField.validate() && emailField.validate())
        {
            String email = emailField.getText();
            String username = usernameField.getText();
            String password = passwordField.getText();
            boolean admin = adminCheckbox.getValue();
            boolean permanent = permanentCheckbox.getValue();

            ICIFEXServiceAsync cifexService = context.getCifexService();
            cifexService.tryToCreateUser(email, username, password, permanent, admin, new CreateUserAsyncCallBack());
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
            context.getPageController().createMainPage();
        }
    }

}