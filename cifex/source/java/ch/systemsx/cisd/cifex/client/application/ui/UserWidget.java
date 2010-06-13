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

import java.util.Date;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.DateField;
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
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import ch.systemsx.cisd.cifex.client.Configuration;
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

    public static final int COLUMN_WIDTH = 320;

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

    protected CheckBox userIsExternallyAuthenticatedField;

    protected TextField<String> maxFileSizeField;

    protected TextField<String> maxFileCountField;

    protected TextField<String> fileRetentionField;

    protected TextField<String> userRetentionField;

    protected DateField expirationDateField;

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
            user.setID(editUser.getID());
            user.updateFrom(editUser);
        }
        user.setEmail(emailField.getValue());
        user.setUserFullName(usernameField.getValue());
        user.setUserCode(userCodeField.getValue());
        if (addStatusField)
        {
            user.setAdmin(isAdminStatus());
        }
        user.setExpirationDate(tryGetExpirationDate(userExpires()));
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
                user.setMaxFileRetention(config.getMaxFileRetention());
                user.setCustomMaxFileRetention(false);
            } else
            {
                user.setMaxFileRetention(new Integer(text));
                user.setCustomMaxFileRetention(true);
            }
        }
        if (userRetentionField != null)
        {
            String text = userRetentionField.getValue();
            if (StringUtils.isBlank(text))
            {
                user.setMaxUserRetention(config.getMaxUserRetention());
                user.setCustomMaxUserRetention(false);
            } else
            {
                user.setMaxUserRetention(new Integer(text));
                user.setCustomMaxUserRetention(true);
            }
        }
        if (userIsExternallyAuthenticatedField != null)
        {
            user.setExternallyAuthenticated(userIsExternallyAuthenticatedField.getValue());
        }
        return user;
    }

    private boolean userExpires()
    {
        // If we have a statusField then the editing user is admin and may freely choose whether the
        // edited user expires or not.
        if (statusField != null)
        {
            return isTemporaryStatus();
        }
        // If we have an editUser and there is _no_ statusField, we keep the status of the editUser.
        if (editUser != null)
        {
            return (editUser.isPermanent() == false);
        }
        // If there is _neither_ a statusField _nor_ an editUser, we are in the case where
        // a regular user creates a new temporary user (use case: Invite).
        return true;
    }

    private FormColumn createRigthColumn(FormData formData)
    {
        FormColumn right = new FormColumn(formData);
        right.setStyleAttribute("paddingRight", "20px");
        final UserInfoDTO currentUser = context.getModel().getUser();
        if (currentUser.isAdmin())
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
        if (currentUser.equals(editUser) == false)
        {
            right.addField(expirationDateField = createExpirationDateField());
        }
        // For creation we have more space on the right side, so we put this field to the right
        // column.
        if (editUser == null && currentUser.isAdmin())
        {
            right.addField(userIsActiveField = createUserIsActiveCheckbox());
        }
        return right;
    }

    private Date tryGetExpirationDate(boolean userExpires)
    {
        if (userExpires)
        {
            if (expirationDateField != null)
            {
                return expirationDateField.getValue();
            } else
            {
                return getInitialExpirationDate();
            }
        } else
        {
            return null;
        }
    }

    private FormColumn createLeftColumn(FormData formData)
    {
        FormColumn left = new FormColumn(formData);
        left.setStyleAttribute("paddingRight", "20px");
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

        final UserInfoDTO currentUser = context.getModel().getUser();
        if (currentUser.isAdmin())
        {
            left.addField(maxFileSizeField = createMaxFileSizeField());
            left.addField(maxFileCountField = createMaxFileCountField());
        }
        // For editing we have more space on the left side so we put this field to the left column.
        if (editUser != null && editingMyself() == false && currentUser.isAdmin())
        {
            left.addField(userIsActiveField = createUserIsActiveCheckbox());
        }

        // Admins can switch the external authentication state, though only if there is external
        // authentication available.
        if (editUser != null && editingMyself() == false && currentUser.isAdmin()
                && context.getModel().getConfiguration().getSystemHasExternalAuthentication())
        {
            left.addField(userIsExternallyAuthenticatedField =
                    createUserIsExternallyAuthenticatedCheckbox());
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
        main.add(createLeftColumn(formData), new ColumnData(COLUMN_WIDTH));
        main.add(createRigthColumn(formData), new ColumnData(COLUMN_WIDTH));

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
            MessageBox.alert(msg(MESSAGE_BOX_ERROR_TITLE), msg(CREATE_USER_PASSWD_MISMATCH_MSG),
                    null);
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
        return isStatus(msg(CREATE_USER_ROLE_TEMP_TAG));
    }

    protected final boolean isPermanentStatus()
    {
        return isStatus(msg(CREATE_USER_ROLE_REGULAR_TAG));
    }

    protected final boolean isAdminStatus()
    {
        return isStatus(msg(CREATE_USER_ROLE_ADMIN_TAG));
    }

    private final TextField<String> createUserCodeField()
    {
        final TextField<String> textField = createTextField(msg(USER_ID_LABEL));
        textField.setAllowBlank(false);
        textField.setValidator(CifexValidator.getUserCodeFieldValidator());
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
        final TextField<String> textField = createTextField(msg(LIST_USERS_EMAIL_COLUMN_HEADER));
        textField.setValidator(CifexValidator.getEmailFieldValidator());
        // Allow a blank email field if and only if the request user is an admin, it is a user
        // creation mode and the system has an external authentication service.
        final boolean allowBlankEmail =
                (editUser == null)
                        && context.getModel().getUser().isAdmin()
                        && context.getModel().getConfiguration()
                                .getSystemHasExternalAuthentication();
        textField.setAllowBlank(allowBlankEmail);
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
        final TextField<String> textField = createTextField(msg(LIST_USERS_FULLNAME_COLUMN_HEADER));
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
        final TextField<String> textField = createTextField(msg(CREATE_USER_PASSWD_LABEL));
        textField.setPassword(true);
        textField.setAllowBlank(true);
        textField.setValidateOnBlur(false);
        textField.setMinLength(4);
        return textField;
    }

    private final TextField<String> createValidatePasswordField()
    {
        final TextField<String> textField = createTextField(msg(CREATE_USER_REPEAT_PASSWD_LABEL));
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
        textArea.setFieldLabel(msg(CREATE_USER_COMMENT_LABEL));
        textArea.setName("user-comment");
        textArea.setPreventScrollbars(true);
        textArea.setWidth(FIELD_WIDTH);
        return textArea;
    }

    private TextField<String> createMaxFileSizeField()
    {
        final TextField<String> textField = createTextField(msg(CREATE_USER_MAX_FILESIZE_LABEL));
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
        final TextField<String> textField = createTextField(msg(CREATE_USER_MAX_FILECOUNT_LABEL));
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
        final TextField<String> textField = createTextField(msg(CREATE_USER_FILE_RETENTION_LABEL));
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
        if (editUser != null && editUser.isCustomMaxFileRetention())
        {
            int maxFileRetentionDuration = editUser.getMaxFileRetention().intValue();
            textField.setValue(Integer.toString(maxFileRetentionDuration));
        }
        return textField;
    }

    private TextField<String> createUserRetention()
    {
        final TextField<String> textField = createTextField(msg(CREATE_USER_USER_RETENTION_LABEL));
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
        if (editUser != null && editUser.isCustomMaxUserRetention())
        {
            int userRetentionDuration = editUser.getMaxUserRetention().intValue();
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
        checkbox.setFieldLabel(msg(EDIT_USER_NOTIFY_LABEL));
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
        checkBox.setFieldLabel(msg(USER_ACTIVE_LABEL));
        checkBox.setWidth(FIELD_WIDTH);
        return checkBox;
    }

    private final CheckBox createUserIsExternallyAuthenticatedCheckbox()
    {
        CheckBox checkBox = new CheckBox();
        checkBox.setBoxLabel(""); // WORKAROUND to align check box to left

        checkBox.setValue(editUser.isExternallyAuthenticated());

        checkBox.setName(Constants.EXTERNAL_AUTHENTICATION_ID);
        checkBox.setId(Constants.EXTERNAL_AUTHENTICATION_ID);
        checkBox.setFieldLabel(msg(EDIT_USER_EXTERNAL_AUTHENTICATION_LABEL));
        checkBox.setWidth(FIELD_WIDTH);
        return checkBox;
    }

    private final SimpleComboBox<String> createStatusComboBox()
    {

        final SimpleComboBox<String> comboBox = new SimpleComboBox<String>();
        final String adminRoleName = msg(CREATE_USER_ROLE_ADMIN_TAG);
        final String permanentRoleName = msg(CREATE_USER_ROLE_REGULAR_TAG);
        final String temporaryRoleName = msg(CREATE_USER_ROLE_TEMP_TAG);
        comboBox.add(adminRoleName);
        comboBox.add(permanentRoleName);
        comboBox.add(temporaryRoleName);
        comboBox.setWidth(FIELD_WIDTH);
        comboBox.setTriggerAction(TriggerAction.ALL);
        comboBox.setFieldLabel(msg(CREATE_USER_STATUS_LABEL));
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

    private final DateField createExpirationDateField()
    {
        final DateField dateField = new DateField();
        dateField.setFieldLabel(msg(CREATE_USER_EXPIRATION_DATE_LABEL));
        final long registrationTimeOrNow =
                (editUser != null) ? editUser.getRegistrationDate().getTime() : System
                        .currentTimeMillis();
        final Date minExpirationDate = new Date(registrationTimeOrNow);
        CalendarUtil.addDaysToDate(minExpirationDate, 1);
        dateField.setMinValue(minExpirationDate);
        if (context.getModel().getUser().isAdmin() == false)
        {
            final Date maxExpirationDate = new Date(registrationTimeOrNow);
            CalendarUtil.addDaysToDate(maxExpirationDate, context.getModel().getUser()
                    .getMaxUserRetention());
            dateField.setMaxValue(maxExpirationDate);
        }
        dateField.getPropertyEditor().setFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
        dateField.setToolTip(msg(CREATE_USER_EXPIRATION_DATE_TOOLTIP));
        dateField.setAllowBlank(false);
        if (statusField == null || isTemporaryStatus())
        {
            if (editUser != null)
            {
                dateField.setValue(editUser.getExpirationDate());
            } else
            {
                dateField.setValue(getInitialExpirationDate());
            }
        } else
        {
            dateField.disable();
        }
        if (dateField.getValue() == null)
        {
            dateField.setValue(getInitialExpirationDate());
        }
        if (statusField != null)
        {
            statusField.addListener(Events.Select, new Listener<BaseEvent>()
                {
                    public void handleEvent(BaseEvent be)
                    {
                        if (isTemporaryStatus())
                        {
                            dateField.enable();
                        } else
                        {
                            dateField.disable();
                        }
                    }
                });
        }
        return dateField;
    }

    private Date getInitialExpirationDate()
    {
        final Date initialExpirationDate = new Date();
        CalendarUtil.addDaysToDate(initialExpirationDate, context.getModel().getConfiguration()
                .getUserRetention());
        return initialExpirationDate;
    }

    //
    // Abstract methods
    //

    /** Returns the label of the form submit button. */
    abstract String getSubmitButtonLabel();

    /** Submits given form. */
    abstract void submitForm();

}