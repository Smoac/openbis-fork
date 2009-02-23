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
import com.gwtext.client.data.SimpleStore;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.Checkbox;
import com.gwtext.client.widgets.form.CheckboxConfig;
import com.gwtext.client.widgets.form.ColumnConfig;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.ComboBoxConfig;
import com.gwtext.client.widgets.form.Form;
import com.gwtext.client.widgets.form.FormConfig;
import com.gwtext.client.widgets.form.TextArea;
import com.gwtext.client.widgets.form.TextAreaConfig;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.TextFieldConfig;
import com.gwtext.client.widgets.form.VType;

import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.utils.CifexValidator;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.Constants;

/**
 * A <code>Form</code> extension as base class to edit or create an user.
 * 
 * @author Basil Neff
 */
public abstract class UserWidget extends Form
{
    private static final String STATUS_FIELD = "status";

    private static final String ID_PREFIX = "UserWidget-";

    private static final int FIELD_WIDTH = 175;

    private static final int COLUMN_WIDTH = 350;

    private static final int LABEL_WIDTH = 120;

    public static final int TOTAL_WIDTH = 700;

    protected final ViewContext context;

    /** If the <code>User</code> is set, the values from the user are used in the fields. */
    protected final UserInfoDTO editUser;

    protected final boolean addStatusField;

    /*
     * Button to submit the form. <p> Note that this button can be <code>null</code> if <code>withButton==false</code>
     * in the constructor. </p>
     */
    protected Button buttonOrNull;

    protected TextField emailField;

    protected TextField usernameField;

    protected TextField userCodeField;

    protected TextField passwordField;

    protected TextField validatePasswordField;

    protected TextArea commentArea;

    protected Checkbox sendUpdateInformation;

    /**
     * Status of the user.
     * <p>
     * An user can have one of following roles: <i>Administrator</i>, <i>Regular</i> or
     * <i>Temporary</i>.
     * </p>
     */
    protected ComboBox statusField;

    /**
     * Whether a submit button should be added to this form.
     * <p>
     * If not, then we assume that you will provide one.
     * </p>
     */
    private final boolean withButton;

    public UserWidget(final ViewContext context, final boolean addStatusField)
    {
        this(context, addStatusField, null, true);
    }

    /**
     * Constructor with a given user.
     * <p>
     * The fields to the user are already filled out, which the user can change.
     * </p>
     */
    public UserWidget(final ViewContext context, final boolean addStatusField, final UserInfoDTO user,
            final boolean withButton)
    {
        super(Ext.generateId(ID_PREFIX), createFormConfig());
        this.context = context;
        this.addStatusField = addStatusField;
        this.editUser = user;
        this.withButton = withButton;
        createCreateUserForm();
    }

    protected final void createCreateUserForm()
    {
        final ColumnConfig leftColumn = new ColumnConfig();
        leftColumn.setWidth(COLUMN_WIDTH);
        leftColumn.setLabelWidth(LABEL_WIDTH);

        column(leftColumn);

        userCodeField = createUserCodeField();
        add(userCodeField);

        usernameField = createUsernameField();
        add(usernameField);

        // only add it, if a new user is created, not when editing a user.
        if (editUser == null)
        {
            commentArea = createCommentArea();
            add(commentArea);
        } else if (editingMyself() == false)
        {
            sendUpdateInformation = createSendUserInformationCheckbox();
            add(sendUpdateInformation);
        }

        end();

        final ColumnConfig rightColumn = new ColumnConfig();
        rightColumn.setWidth(COLUMN_WIDTH);
        rightColumn.setLabelWidth(LABEL_WIDTH);
        column(rightColumn);

        emailField = createEmailField();
        add(emailField);

        passwordField = createPasswordField();
        add(passwordField);

        validatePasswordField = createValidatePasswordField();
        add(validatePasswordField);

        if (addStatusField)
        {
            statusField = createStatusComboBox();
            add(statusField);
        }

        end();

        createButton();
        render();
        if (editUser != null && editUser.isExternallyAuthenticated())
        {
            disableInternalFields(true);
        }
    }

    protected boolean editingMyself()
    {
        return editUser != null
                && editUser.getUserCode().equals(context.getModel().getUser().getUserCode());
    }

    private final void createButton()
    {
        if (withButton == false)
        {
            return;
        }
        buttonOrNull = addButton(getSubmitButtonLabel());
        buttonOrNull.addButtonListener(new ButtonListenerAdapter()
            {

                //
                // ButtonListenerAdapter
                //

                public final void onClick(final Button but, final EventObject e)
                {
                    submitForm();
                }

            });
    }

    protected final boolean arePasswordsEqual()
    {
        if (passwordField.getValueAsString().equals(validatePasswordField.getValueAsString()) == false)
        {
            MessageBox.alert(getMessageResources().getMessageBoxErrorTitle(), getMessageResources()
                    .getPasswordMissmatchMessage());
            return false;
        }
        return true;
    }

    /** Whether status specified by {@link #statusField} equals given <var>status</var>. */
    private final boolean isStatus(final String status)
    {
        assert statusField != null : "Undefined status field.";
        return statusField.getValue().equals(status);
    }

    protected final boolean isPermanentStatus()
    {
        return isStatus(getMessageResources().getPermanentRoleName());
    }

    protected final boolean isAdminStatus()
    {
        return isStatus(getMessageResources().getAdminRoleName());
    }

    protected final IMessageResources getMessageResources()
    {
        return context.getMessageResources();
    }

    private final TextField createUserCodeField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(getMessageResources().getUserCodeLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName(getMessageResources().getUserCodeLabel());
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidator(CifexValidator.getUserCodeFieldValidator());
        fieldConfig.setInvalidText(Constants.VALID_USER_CODE_DESCRIPTION);
        fieldConfig.setValidateOnBlur(false);
        final TextField textField = new TextField(fieldConfig);
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
        formConfig.setWidth(TOTAL_WIDTH);
        formConfig.setLabelAlign(Position.LEFT);
        formConfig.setButtonAlign(Position.LEFT);
        formConfig.setLabelWidth(LABEL_WIDTH);
        return formConfig;
    }

    private final TextField createEmailField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(getMessageResources().getUserEmailLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setVtype(VType.EMAIL);
        fieldConfig.setName(getMessageResources().getUserEmailLabel());
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValidateOnBlur(false);
        final TextField textField = new TextField(fieldConfig);
        if (editUser != null && editUser.getEmail() != null)
        {
            textField.setValue(editUser.getEmail());

        }
        return textField;
    }

    private void disableInternalFields(final boolean disabled)
    {
        usernameField.setDisabled(disabled);
        passwordField.setDisabled(disabled);
        validatePasswordField.setDisabled(disabled);
        emailField.setDisabled(disabled);
    }

    private final TextField createUsernameField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(getMessageResources().getUserFullNameLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName(getMessageResources().getUserFullNameLabel());
        fieldConfig.setAllowBlank(true);
        fieldConfig.setValidateOnBlur(false);
        final TextField textField = new TextField(fieldConfig);
        if (editUser != null && editUser.getUserFullName() != null)
        {
            textField.setValue(editUser.getUserFullName());
        }
        return textField;
    }

    private final TextField createPasswordField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(getMessageResources().getPasswordLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName(getMessageResources().getPasswordLabel());
        fieldConfig.setPassword(true);
        fieldConfig.setAllowBlank(true);
        fieldConfig.setValidateOnBlur(false);
        fieldConfig.setMinLength(4);
        final TextField textField = new TextField(fieldConfig);
        return textField;
    }

    private final TextField createValidatePasswordField()
    {
        final TextFieldConfig fieldConfig = new TextFieldConfig();
        fieldConfig.setFieldLabel(getMessageResources().getValidatePasswordLabel());
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName(getMessageResources().getValidatePasswordLabel());
        fieldConfig.setPassword(true);
        fieldConfig.setAllowBlank(true);
        fieldConfig.setValidateOnBlur(false);
        fieldConfig.setMinLength(4);
        final TextField textField = new TextField(fieldConfig);
        return textField;
    }

    private final TextArea createCommentArea()
    {
        final TextAreaConfig textAreaConfig = new TextAreaConfig();
        textAreaConfig.setAllowBlank(true);
        textAreaConfig.setFieldLabel(getMessageResources().getCommentLabel());
        textAreaConfig.setName("user-comment");
        textAreaConfig.setGrow(false);
        textAreaConfig.setPreventScrollbars(true);
        textAreaConfig.setWidth(FIELD_WIDTH);
        return new TextArea(textAreaConfig);

    }

    private final Checkbox createSendUserInformationCheckbox()
    {
        final CheckboxConfig checkboxConfig = new CheckboxConfig();
        checkboxConfig.setChecked(true);
        checkboxConfig.setName("send-user-information");
        checkboxConfig.setFieldLabel(getMessageResources().getSendUserUpdateInformationLabel());
        checkboxConfig.setWidth(FIELD_WIDTH);
        return new Checkbox(checkboxConfig);
    }

    private final ComboBox createStatusComboBox()
    {
        final ComboBoxConfig comboBoxConfig = new ComboBoxConfig();
        final String adminRoleName = getMessageResources().getAdminRoleName();
        final String permanentRoleName = getMessageResources().getPermanentRoleName();
        final String temporaryRoleName = getMessageResources().getTemporaryRoleName();
        final SimpleStore store = new SimpleStore(STATUS_FIELD, new String[][]
            { new String[]
                { adminRoleName }, new String[]
                { permanentRoleName }, new String[]
                { temporaryRoleName } });
        store.load();
        comboBoxConfig.setStore(store);
        comboBoxConfig.setDisplayField(STATUS_FIELD);
        comboBoxConfig.setWidth(FIELD_WIDTH);
        comboBoxConfig.setMode(ComboBox.LOCAL);
        comboBoxConfig.setTriggerAction(ComboBox.ALL);
        comboBoxConfig.setFieldLabel(getMessageResources().getUserStatusLabel());
        comboBoxConfig.setForceSelection(true);
        comboBoxConfig.setEditable(false);
        comboBoxConfig.setAllowBlank(false);
        final ComboBox comboBox = new ComboBox(comboBoxConfig);
        String value = permanentRoleName;
        if (editUser != null)
        {
            if (editUser.isAdmin() == true)
            {
                value = adminRoleName;
            } else if (editUser.isPermanent() == false)
            {
                value = temporaryRoleName;
            }
        }
        comboBox.setValue(value);
        return comboBox;
    }

    //
    // Abstract methods
    //

    /** Returns the label of the form submit button. */
    abstract String getSubmitButtonLabel();

    /** Submits given form. */
    abstract void submitForm();

}