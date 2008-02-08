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
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.CheckboxConfig;
import com.gwtext.client.widgets.form.ColumnConfig;
import com.gwtext.client.widgets.form.Form;
import com.gwtext.client.widgets.form.FormConfig;
import com.gwtext.client.widgets.form.Radio;
import com.gwtext.client.widgets.form.TextArea;
import com.gwtext.client.widgets.form.TextAreaConfig;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.TextFieldConfig;
import com.gwtext.client.widgets.form.VType;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.dto.User;

/**
 * @author Basil Neff
 */
public abstract class UserWidget extends Form
{
    private static final String ID_PREFIX = "CreateUserWidget-";

    protected final ViewContext context;

    protected final IMessageResources messageResources;

    /** If the <code>User</code> is set, the values from the user are used in the fields. */
    protected final User editUser;

    private static final int FIELD_WIDTH = 175;

    private static final int COLUMN_WIDTH = 300;

    /* Protected, that the subclasses can get the value. */
    protected TextField emailField;

    /* Protected, that the subclasses can get the value. */
    protected TextField usernameField;

    /* Protected, that the subclasses can get the value. */
    protected TextField userCodeField;

    /* Protected, that the subclasses can get the value. */
    protected TextField passwordField;

    /* Protected, that the subclasses can get the value. */
    protected TextField validatePasswordField;

    /* Protected, that the subclasses can get the value. */
    protected Radio adminRadioButton;

    /* Protected, that the subclasses can get the value. */
    protected Radio permanentRadioButton;

    /* Protected, that the subclasses can get the value. */
    protected Radio temporaryRadioButton;

    /* Protected, that the subclasses can get the value. */
    protected TextArea commentArea;

    protected final boolean allowPermanentUsers;

    public UserWidget(final ViewContext context, final boolean allowPermanentUsers)
    {
        super(Ext.generateId(ID_PREFIX), createFormConfig());
        this.context = context;
        this.messageResources = context.getMessageResources();
        this.allowPermanentUsers = allowPermanentUsers;
        this.editUser = null;
        createCreateUserForm();
    }

    /**
     * Constructor with a given user. The fields to the user are already filled out, which the user can change.
     * 
     * @param context
     * @param allowPermanentUsers
     * @param user
     */
    public UserWidget(final ViewContext context, final boolean allowPermanentUsers, final User user)
    {
        super(Ext.generateId(ID_PREFIX), createFormConfig());
        this.context = context;
        this.messageResources = context.getMessageResources();
        this.allowPermanentUsers = allowPermanentUsers;
        this.editUser = user;
        createCreateUserForm();
    }

    protected final void createCreateUserForm()
    {
        final ColumnConfig leftColumn = new ColumnConfig();
        leftColumn.setWidth(COLUMN_WIDTH);
        column(leftColumn);

        userCodeField = createUserCodeField();
        add(userCodeField);

        usernameField = createUsernameField();
        add(usernameField);

        // only add it, if a new user is created, not when editing a user.
        if(editUser == null){
            commentArea = createCommentArea();
            add(commentArea);            
        }
        
        end();

        ColumnConfig rightColumn = new ColumnConfig();
        rightColumn.setWidth(COLUMN_WIDTH);
        column(rightColumn);

        emailField = createEmailField();
        add(emailField);

        passwordField = createPasswordField();
        add(passwordField);

        validatePasswordField = createValidatePasswordField();
        add(validatePasswordField);
        end();

        ColumnConfig lastColumn = new ColumnConfig();
        lastColumn.setWidth(COLUMN_WIDTH - 150);
        column(lastColumn);

        if (allowPermanentUsers)
        {
            adminRadioButton = createAdminRadioButton();
            permanentRadioButton = createPermanentRadioButton();
            temporaryRadioButton = createTemporaryRadioButton();
            add(adminRadioButton);
            add(permanentRadioButton);
            add(temporaryRadioButton);
        }
        end();
        render();
    }

    /**
     * Gets the button to submit the form. This method is separatly, that you can also add it to a Dialog or on a
     * different location.
     */
    // 2008-2-6 Basil Neff I had problem to add the Button to a DialogBox, after submiting, I always got an Exception,
    // when the RootPanel is cleared. The same problem, when the Button was when you add the Button in the same Widget.
    public Button getSubmitButton()
    {
        Button button = new Button(getSubmitButtonLabel());
        button.addButtonListener(new ButtonListenerAdapter()
            {
                public final void onClick(final Button but, final EventObject e)
                {
                    submitForm();
                }

            });
        return button;
    }

    private TextField createUserCodeField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();

        fieldConfig.setFieldLabel(messageResources.getUserCodeLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName(messageResources.getUserCodeLabel());
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidateOnBlur(false);
        TextField textField = new TextField(fieldConfig);
        if (editUser != null && editUser.getUserCode() != null)
        {
            textField.setValue(editUser.getUserCode());
            textField.setDisabled(true);
        }
        return textField;
    }

    private static FormConfig createFormConfig()
    {
        final FormConfig formConfig = new FormConfig();
        formConfig.setWidth(750);
        formConfig.setLabelAlign(Position.LEFT);
        formConfig.setButtonAlign(Position.RIGHT);
        return formConfig;
    }

    protected TextField createEmailField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(messageResources.getUserEmailLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setVtype(VType.EMAIL);
        fieldConfig.setName(messageResources.getUserEmailLabel());
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidateOnBlur(false);
        TextField textField = new TextField(fieldConfig);
        if (editUser != null && editUser.getEmail() != null)
        {
            textField.setValue(editUser.getEmail());
        }
        return textField;
    }

    protected TextField createUsernameField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();

        fieldConfig.setFieldLabel(messageResources.getUserFullNameLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName(messageResources.getUserFullNameLabel());
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidateOnBlur(false);
        TextField textField = new TextField(fieldConfig);
        if (editUser != null && editUser.getUserFullName() != null)
        {
            textField.setValue(editUser.getUserFullName());
        }
        return textField;
    }

    protected TextField createPasswordField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(messageResources.getPasswordLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName(messageResources.getPasswordLabel());
        fieldConfig.setPassword(true);
        fieldConfig.setAllowBlank(true);
        fieldConfig.setValidateOnBlur(false);
        fieldConfig.setMinLength(4);
        return new TextField(fieldConfig);
    }

    protected TextField createValidatePasswordField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(messageResources.getValidatePasswordLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName(messageResources.getValidatePasswordLabel());
        fieldConfig.setPassword(true);
        fieldConfig.setAllowBlank(true);
        fieldConfig.setValidateOnBlur(false);
        fieldConfig.setMinLength(4);
        return new TextField(fieldConfig);
    }
    
    private final TextArea createCommentArea(){
        final TextAreaConfig textAreaConfig = new TextAreaConfig();
        textAreaConfig.setAllowBlank(true);
        textAreaConfig.setFieldLabel(messageResources.getCommentLabel());
        textAreaConfig.setName("user-comment");
        textAreaConfig.setGrow(true);
        textAreaConfig.setPreventScrollbars(true);
        textAreaConfig.setWidth(FIELD_WIDTH);
        return new TextArea(textAreaConfig);
        
    }

    protected Radio createAdminRadioButton()
    {
        final CheckboxConfig checkboxConfig = new CheckboxConfig();
        checkboxConfig.setChecked(false);
        if (editUser != null)
        {
            if (editUser.isAdmin() == true)
            {
                checkboxConfig.setChecked(true);
            }
        }
        checkboxConfig.setFieldLabel(messageResources.getAdminRoleName());
        checkboxConfig.setName("role");
        return new Radio(checkboxConfig);
    }

    protected Radio createPermanentRadioButton()
    {
        final CheckboxConfig checkboxConfig = new CheckboxConfig();
        checkboxConfig.setChecked(true);
        if (editUser != null)
        {
            if (editUser.isPermanent() == true && editUser.isAdmin() == false)
            {
                checkboxConfig.setChecked(true);
            } else
            {
                checkboxConfig.setChecked(false);
            }
        }
        checkboxConfig.setFieldLabel(messageResources.getPermanentRoleName());
        checkboxConfig.setName("role");
        return new Radio(checkboxConfig);
    }

    protected Radio createTemporaryRadioButton()
    {
        final CheckboxConfig checkboxConfig = new CheckboxConfig();
        checkboxConfig.setChecked(false);
        if (editUser != null)
        {
            if (editUser.isPermanent() == false)
            {
                checkboxConfig.setChecked(true);
            }
        }
        checkboxConfig.setFieldLabel(messageResources.getTemporaryRoleName());
        checkboxConfig.setName("role");
        return new Radio(checkboxConfig);
    }

    //
    // Abstract Methods
    //
    abstract String getSubmitButtonLabel();

    abstract void submitForm();
}