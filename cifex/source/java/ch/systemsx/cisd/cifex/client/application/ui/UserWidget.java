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

import java.util.Date;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.form.Validator;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import ch.systemsx.cisd.cifex.client.Configuration;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.utils.CifexValidator;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * A <code>Form</code> extension as base class to edit or create an user.
 * 
 * @author Basil Neff
 */
public abstract class UserWidget extends LayoutContainer
{

    private static final int FIELD_WIDTH = 175;

    public static final int TOTAL_WIDTH = 700;

    protected final ViewContext context;

    /** If the <code>User</code> is set, the values from the user are used in the fields. */
    protected final UserInfoDTO editUser;

    protected final boolean addStatusField;

    /*
     * Button to submit the form. <p> Note that this button can be <code>null</code> if
     * <code>withButton==false</code> in the constructor. </p>
     */
    protected Button buttonOrNull;

    protected TextField<String> emailField;

    protected TextField<String> usernameField;

    protected TextField<String> userCodeField;

    protected TextField<String> passwordField;

    protected TextField<String> validatePasswordField;

    protected TextArea commentArea;

    protected CheckBox sendUpdateInformation;

    protected CheckBox userIsActiveField;

    protected TextField<String> maxFileSizeField;

    protected TextField<String> maxFileCountField;

    protected TextField<String> fileRetentionField;

    protected TextField<String> userRetentionField;

    /**
     * Status of the user.
     * <p>
     * An user can have one of following roles: <i>Administrator</i>, <i>Regular</i> or
     * <i>Temporary</i>.
     * </p>
     */
    protected SimpleComboBox<String> statusField;

    /**
     * Whether a submit button should be added to this form.
     * <p>
     * If not, then we assume that you will provide one.
     * </p>
     */
    private final boolean withButton;

    private FormPanel formPanel;

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
    UserWidget(final ViewContext context, final boolean addStatusField, final UserInfoDTO user,
            final boolean withButton)
    {
        setLayout(new FlowLayout(5));
        setBorders(false);
        setScrollMode(Scroll.AUTO);
        setWidth(TOTAL_WIDTH);
        this.context = context;
        this.addStatusField = addStatusField;
        this.editUser = user;
        this.withButton = withButton;
        createCreateUserForm();
    }

    protected UserInfoDTO createFromFields()
    {
        final Configuration config = context.getModel().getConfiguration();
        final UserInfoDTO user = new UserInfoDTO();
        if (editUser != null)
        {
            user.setAdmin(editUser.isAdmin());
            user.setExpirationDate(editUser.getExpirationDate());
            user.setExternallyAuthenticated(editUser.isExternallyAuthenticated());
            user.setFileRetention(editUser.getFileRetention());
            user.setCustomFileRetention(editUser.isCustomFileRetention());
            user.setUserRetention(editUser.getUserRetention());
            user.setCustomUserRetention(editUser.isCustomUserRetention());
            user.setMaxFileSizePerQuotaGroupInMB(editUser.getMaxFileSizePerQuotaGroupInMB());
            user.setCustomMaxFileSizePerQuotaGroup(editUser.isCustomMaxFileSizePerQuotaGroup());
            user.setMaxFileCountPerQuotaGroup(editUser.getMaxFileCountPerQuotaGroup());
            user.setCustomMaxFileCountPerQuotaGroup(editUser.isCustomMaxFileCountPerQuotaGroup());
            user.setActive(editUser.isActive());
            user.setRegistrator(editUser.getRegistrator());
        }
        user.setEmail(emailField.getValue());
        user.setUserFullName(usernameField.getValue());
        user.setUserCode(userCodeField.getValue());
        if (addStatusField)
        {
            user.setAdmin(isAdminStatus());
            if (isTemporaryStatus())
            {
                final Date expirationDate = new Date();
                CalendarUtil.addDaysToDate(expirationDate, config.getFileRetention());
                user.setExpirationDate(expirationDate);
            } else
            {
                user.setExpirationDate(null);
            }
        }
        if (maxFileCountField != null)
        {
            String text = maxFileCountField.getValue();
            if (StringUtils.isBlank(text))
            {
                user.setMaxFileCountPerQuotaGroup(config.getMaxFileCountPerQuotaGroup());
                user.setCustomMaxFileCountPerQuotaGroup(false);
            } else
            {
                user.setMaxFileCountPerQuotaGroup(Constants.UNLIMITED_VALUE.equals(text) ? null
                        : new Integer(text));
                user.setCustomMaxFileCountPerQuotaGroup(true);
            }
        }
        if (maxFileSizeField != null)
        {
            String text = maxFileSizeField.getValue();
            if (StringUtils.isBlank(text))
            {
                user.setMaxFileSizePerQuotaGroupInMB(config.getMaxFileSizePerQuotaGroupInMB());
                user.setCustomMaxFileSizePerQuotaGroup(false);
            } else
            {
                user.setMaxFileSizePerQuotaGroupInMB(Constants.UNLIMITED_VALUE.equals(text) ? null
                        : new Long(text));
                user.setCustomMaxFileSizePerQuotaGroup(true);
            }
        }
        if (userIsActiveField != null)
        {
            user.setActive(userIsActiveField.getValue());
        }
        if (fileRetentionField != null)
        {
            String text = fileRetentionField.getValue();
            if (StringUtils.isBlank(text))
            {
                user.setFileRetention(config.getFileRetention());
                user.setCustomFileRetention(false);
            } else
            {
                user.setFileRetention(new Integer(text));
                user.setCustomFileRetention(true);
            }
            user.setFileRetention(StringUtils.isBlank(text) ? null : new Integer(text));
        }
        if (userRetentionField != null)
        {
            String text = userRetentionField.getValue();
            if (StringUtils.isBlank(text))
            {
                user.setUserRetention(config.getUserRetention());
                user.setCustomUserRetention(false);
            } else
            {
                user.setUserRetention(new Integer(text));
                user.setCustomUserRetention(true);
            }
            user.setUserRetention(StringUtils.isBlank(text) ? null : new Integer(text));
        }
        return user;
    }

    private FormColumn createRigthColumn(FormData formData)
    {
        FormColumn right = new FormColumn(formData);
        right.setStyleAttribute("paddingLeft", "10px");
        if (context.getModel().getUser().isAdmin())
        {
            right.addField(fileRetentionField = createFileRetention());
            right.addField(userRetentionField = createUserRetention());
        }
        right.addField(emailField = createEmailField());
        right.addField(passwordField = createPasswordField());
        right.addField(validatePasswordField = createValidatePasswordField());
        if (addStatusField)
        {
            right.addField(statusField = createStatusComboBox());
        }
        // For creation we have more space on the right side.
        if (editUser == null && context.getModel().getUser().isAdmin())
        {
            right.addField(userIsActiveField = createUserIsActiveCheckbox());
        }
        return right;
    }

    private FormColumn createLeftColumn(FormData formData)
    {
        FormColumn left = new FormColumn(formData);
        left.setStyleAttribute("paddingRight", "10px");
        left.addField(userCodeField = createUserCodeField());
        left.addField(usernameField = createUsernameField());
        // only add it, if a new user is created, not when editing a user.
        if (editUser == null)
        {
            left.addField(commentArea = createCommentArea());
        } else if (editingMyself() == false)
        {
            left.addField(sendUpdateInformation = createSendUserInformationCheckbox());
        }
        if (context.getModel().getUser().isAdmin())
        {
            left.addField(maxFileSizeField = createMaxFileSizeField());
            left.addField(maxFileCountField = createMaxFileCountField());
        }
        // For editing we have more space on the left side.
        if (editUser != null && editingMyself() == false && context.getModel().getUser().isAdmin())
        {
            left.addField(userIsActiveField = createUserIsActiveCheckbox());
        }
        return left;
    }

    protected final void createCreateUserForm()
    {
        FormData formData = new FormData("95%");
        formPanel = new FormPanel();
        formPanel.setHeaderVisible(false);
        formPanel.setFrame(false);
        formPanel.setBodyBorder(false);
        formPanel.setBorders(false);
        formPanel.setButtonAlign(HorizontalAlignment.CENTER);

        LayoutContainer main = new LayoutContainer();
        main.setLayout(new ColumnLayout());
        main.add(createLeftColumn(formData), new ColumnData(.5));
        main.add(createRigthColumn(formData), new ColumnData(.5));

        formPanel.add(main);

        tryCreateButton(formPanel);
        if (editUser != null && editUser.isExternallyAuthenticated())
        {
            disableInternalFields(true);
        }
        add(formPanel);

    }

    public boolean isValid()
    {
        return formPanel.isValid();
    }

    protected boolean editingMyself()
    {
        return editUser != null
                && editUser.getUserCode().equals(context.getModel().getUser().getUserCode());
    }

    private final void tryCreateButton(LayoutContainer panel)
    {
        if (withButton == false)
        {
            return;
        }
        panel.add(buttonOrNull = new Button(getSubmitButtonLabel()));
        buttonOrNull.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    submitForm();
                }

            });
    }

    protected final boolean arePasswordsEqual()
    {
        if (equalOrBothNull() == false)
        {
            MessageBox.alert(getMessageResources().getMessageBoxErrorTitle(), getMessageResources()
                    .getPasswordMissmatchMessage(), null);
            return false;
        }
        return true;
    }

    private boolean equalOrBothNull()
    {
        return passwordField.getValue() == null && validatePasswordField.getValue() == null
                || passwordField.getValue().equals(validatePasswordField.getValue());
    }

    /** Whether status specified by {@link #statusField} equals given <var>status</var>. */
    private final boolean isStatus(final String status)
    {
        assert statusField != null : "Undefined status field.";
        return statusField.getSimpleValue().equals(status);
    }

    protected final boolean isTemporaryStatus()
    {
        return isStatus(getMessageResources().getTemporaryRoleName());
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

    private final TextField<String> createUserCodeField()
    {
        final TextField<String> textField =
                createTextField(getMessageResources().getUserCodeLabel());
        textField.setAllowBlank(false);
        textField.setValidator(CifexValidator.getUserCodeFieldValidator(context
                .getMessageResources()));
        textField.setValidateOnBlur(false);
        if (editUser != null && editUser.getUserCode() != null)
        {
            textField.setValue(editUser.getUserCode());
            textField.setEnabled(false);
        }
        return textField;
    }

    private TextField<String> createTextField(String label)
    {
        final TextField<String> fieldConfig = new TextField<String>();
        fieldConfig.setFieldLabel(label);
        fieldConfig.setWidth(FIELD_WIDTH);
        fieldConfig.setName(label);
        return fieldConfig;
    }

    private final TextField<String> createEmailField()
    {
        final TextField<String> textField =
                createTextField(getMessageResources().getUserEmailLabel());
        textField
                .setValidator(CifexValidator.getEmailFieldValidator(context.getMessageResources()));
        textField.setAllowBlank(false);
        if (editUser != null && editUser.getEmail() != null)
        {
            textField.setValue(editUser.getEmail());

        }
        return textField;
    }

    private void disableInternalFields(final boolean isDisabled)
    {
        boolean enabled = isDisabled == false;
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        validatePasswordField.setEnabled(enabled);
        emailField.setEnabled(enabled);
    }

    private final TextField<String> createUsernameField()
    {
        final TextField<String> textField =
                createTextField(getMessageResources().getUserFullNameLabel());
        textField.setAllowBlank(true);
        textField.setValidateOnBlur(false);
        if (editUser != null && editUser.getUserFullName() != null)
        {
            textField.setValue(editUser.getUserFullName());
        }
        return textField;
    }

    private final TextField<String> createPasswordField()
    {
        final TextField<String> textField =
                createTextField(getMessageResources().getPasswordLabel());
        textField.setPassword(true);
        textField.setAllowBlank(true);
        textField.setValidateOnBlur(false);
        textField.setMinLength(4);
        return textField;
    }

    private final TextField<String> createValidatePasswordField()
    {
        final TextField<String> textField =
                createTextField(getMessageResources().getValidatePasswordLabel());
        textField.setPassword(true);
        textField.setAllowBlank(true);
        textField.setValidateOnBlur(false);
        textField.setMinLength(4);
        return textField;
    }

    private final TextArea createCommentArea()
    {
        final TextArea textArea = new TextArea();
        textArea.setAllowBlank(true);
        textArea.setFieldLabel(getMessageResources().getCommentLabel());
        textArea.setName("user-comment");
        textArea.setPreventScrollbars(true);
        textArea.setWidth(FIELD_WIDTH);
        return textArea;
    }

    private TextField<String> createMaxFileSizeField()
    {
        final TextField<String> textField =
                createTextField(getMessageResources().getMaxFileSizeLabel());
        textField.setValidator(new Validator()
            {
                public String validate(Field<?> field, String value)
                {
                    if (value == null)
                    {
                        return "Field required";
                    }
                    if (value.length() == 0)
                    {
                        return null;
                    }
                    if (Constants.UNLIMITED_VALUE.equals(value))
                    {
                        return null;
                    }
                    try
                    {
                        long size = Long.parseLong(value);
                        return size > 0 ? null : "No files specified";
                    } catch (NumberFormatException ex)
                    {
                        return "Incorrect number";
                    }
                }
            });
        if (editUser != null && editUser.isCustomMaxFileSizePerQuotaGroup())
        {
            if (editUser.getMaxFileSizePerQuotaGroupInMB() != null)
            {
                textField.setValue(editUser.getMaxFileSizePerQuotaGroupInMB().toString());
            } else
            {
                textField.setValue(Constants.UNLIMITED_VALUE);
            }
        }
        return textField;
    }

    private TextField<String> createMaxFileCountField()
    {
        final TextField<String> textField =
                createTextField(getMessageResources().getMaxFileCountLabel());
        textField.setValidator(new Validator()
            {
                public String validate(Field<?> field, String value)
                {
                    if (value == null)
                    {
                        return "Field required";
                    }
                    if (value.length() == 0)
                    {
                        return null;
                    }
                    if (Constants.UNLIMITED_VALUE.equals(value))
                    {
                        return null;
                    }
                    try
                    {
                        int size = Integer.parseInt(value);
                        return size > 0 ? null : "No files specified";
                    } catch (NumberFormatException ex)
                    {
                        return "Incorrect number";
                    }
                }
            });
        if (editUser != null && editUser.isCustomMaxFileCountPerQuotaGroup())
        {
            if (editUser.getMaxFileCountPerQuotaGroup() != null)
            {
                textField.setValue(editUser.getMaxFileCountPerQuotaGroup().toString());
            } else
            {
                textField.setValue(Constants.UNLIMITED_VALUE);
            }
        }
        return textField;
    }

    private TextField<String> createFileRetention()
    {
        final TextField<String> textField =
                createTextField(getMessageResources().getFileRetention());
        textField.setValidator(new Validator()
            {
                public String validate(Field<?> field, String value)
                {
                    if (value == null)
                    {
                        return "Field required";
                    }
                    if (value.length() == 0)
                    {
                        return null;
                    }
                    try
                    {
                        int duration = Integer.parseInt(value);
                        return duration > 0 ? null : "Incorrect duration";
                    } catch (NumberFormatException ex)
                    {
                        return "Incorrect number";
                    }
                }
            });
        if (editUser != null && editUser.isCustomFileRetention())
        {
            int fileRetentionDuration = editUser.getFileRetention().intValue();
            textField.setValue(Integer.toString(fileRetentionDuration));
        }
        return textField;
    }

    private TextField<String> createUserRetention()
    {
        final TextField<String> textField =
                createTextField(getMessageResources().getUserRetention());
        textField.setValidator(new Validator()
            {
                public String validate(Field<?> field, String value)
                {
                    if (value == null)
                    {
                        return "Field required";
                    }
                    if (value.length() == 0)
                    {
                        return null;
                    }
                    try
                    {
                        int duration = Integer.parseInt(value);
                        return duration > 0 ? null : "Incorrect duration";
                    } catch (NumberFormatException ex)
                    {
                        return "Incorrect number";
                    }
                }
            });
        if (editUser != null && editUser.isCustomUserRetention())
        {
            int userRetentionDuration = editUser.getUserRetention().intValue();
            textField.setValue(Integer.toString(userRetentionDuration));
        }
        return textField;
    }

    private final CheckBox createSendUserInformationCheckbox()
    {
        CheckBox checkbox = new CheckBox();
        checkbox.setBoxLabel(""); // WORKAROUND to align check box to left
        checkbox.setValue(true);
        checkbox.setName("send-user-information");
        checkbox.setFieldLabel(getMessageResources().getSendUserUpdateInformationLabel());
        checkbox.setWidth(FIELD_WIDTH);
        return checkbox;
    }

    private final CheckBox createUserIsActiveCheckbox()
    {
        CheckBox checkBox = new CheckBox();
        checkBox.setBoxLabel(""); // WORKAROUND to align check box to left
        if (editUser != null)
        {
            checkBox.setValue(editUser.isActive());

        } else
        {
            checkBox.setValue(true);
        }
        checkBox.setName("user-is-active");
        checkBox.setFieldLabel(getMessageResources().getUserActiveLabel());
        checkBox.setWidth(FIELD_WIDTH);
        return checkBox;
    }

    private final SimpleComboBox<String> createStatusComboBox()
    {

        SimpleComboBox<String> comboBox = new SimpleComboBox<String>();
        final String adminRoleName = getMessageResources().getAdminRoleName();
        final String permanentRoleName = getMessageResources().getPermanentRoleName();
        final String temporaryRoleName = getMessageResources().getTemporaryRoleName();
        comboBox.add(adminRoleName);
        comboBox.add(permanentRoleName);
        comboBox.add(temporaryRoleName);
        comboBox.setWidth(FIELD_WIDTH);
        comboBox.setTriggerAction(TriggerAction.ALL);
        comboBox.setFieldLabel(getMessageResources().getUserStatusLabel());
        comboBox.setForceSelection(true);
        comboBox.setEditable(false);
        comboBox.setAllowBlank(false);
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
        comboBox.setSimpleValue(value);
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