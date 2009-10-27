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

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.layout.ColumnData;
import com.extjs.gxt.ui.client.widget.layout.ColumnLayout;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.http.client.URL;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.FileShareUploadDialog;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ServletPathConstants;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.utils.CifexValidator;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;

/**
 * <code>Form</code> extension to upload files and to send emails to specified recipients.
 * 
 * @author Franz-Josef Elmer
 */
public final class FileUploadWidget extends LayoutContainer
{

    private static final int COLUMN_WIDTH = 360;

    private static final int WIDTH_OFS = 20;

    private static final int FIELD_WIDTH = COLUMN_WIDTH - 50;

    private static final int TOTAL_WIDTH = 3 * COLUMN_WIDTH + WIDTH_OFS;

    private static final int FILE_FIELD_NUMBER = 3;

    private final UserTextArea userTextArea;

    private final ViewContext context;

    private Button submitButton;

    private Button validateButton;

    private final FormPanel formPanel;

    private final List<FileUploadField> uploadFields;

    public FileUploadWidget(final ViewContext context)
    {
        uploadFields = new ArrayList<FileUploadField>();
        this.context = context;
        setLayout(new FlowLayout(5));
        setBorders(false);
        setScrollMode(Scroll.AUTO);
        setWidth(TOTAL_WIDTH);
        formPanel = new FormPanel();
        formPanel.setHeaderVisible(false);
        formPanel.setBodyBorder(false);
        formPanel.setBorders(false);
        formPanel.setWidth(-1);
        formPanel.setLabelAlign(LabelAlign.LEFT);
        formPanel.setButtonAlign(HorizontalAlignment.LEFT);
        formPanel.setAction(ServletPathConstants.FILE_UPLOAD_SERVLET_NAME);
        formPanel.setMethod(Method.POST);
        formPanel.setEncoding(Encoding.MULTIPART);
        userTextArea = new UserTextArea();
        userTextArea.setAllowBlank(false);
        userTextArea.setFieldLabel(context.getMessageResources().getRecipientFieldLabel());
        userTextArea.setName("email-addresses");
        userTextArea.setWidth(FIELD_WIDTH);
        userTextArea.setPreventScrollbars(false);
        userTextArea.setValidator(CifexValidator.getUserFieldValidator(context
                .getMessageResources()));
        trySetInitialValueFromURL(userTextArea, Constants.RECIPIENTS_PARAMETER);
        createForm();
    }

    private final void createForm()
    {
        FormData formData = new FormData("95%");

        formPanel.addButton(validateButton =
                new Button(context.getMessageResources().getValidateUsersButtonLabel()));
        validateButton.addSelectionListener(getUserValidateButtonListener());

        formPanel.addButton(submitButton =
                new Button(context.getMessageResources().getFileUploadButtonLabel()));
        submitButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    submitForm();
                }

            });

        LayoutContainer main = new LayoutContainer();
        main.setWidth(-1);
        main.setLayout(new ColumnLayout());
        main.add(createLeftColumn(formData), new ColumnData(.34));
        main.add(createMiddleColumn(formData), new ColumnData(.33));
        main.add(createRightColumn(formData), new ColumnData(.33));

        formPanel.add(main);
        add(formPanel);
    }

    private FormColumn createRightColumn(FormData formData)
    {
        FormColumn rightColumn = new FormColumn(formData);
        FieldSet fieldSetRight = new FieldSet();
        fieldSetRight.setHeading(context.getMessageResources().getCommentLabel());
        fieldSetRight.add(createCommentField());
        rightColumn.addFieldSet(fieldSetRight);
        return rightColumn;
    }

    private FormColumn createMiddleColumn(FormData formData)
    {
        FormColumn middleColumn = new FormColumn(formData);
        FieldSet fieldSetMiddle = new FieldSet();
        fieldSetMiddle.setHeading(context.getMessageResources().getRecipientLegend());
        if (context.getModel().getUser().isPermanent() == false)
        {
            final String registratorUserCode =
                    context.getModel().getUser().getRegistrator().getUserCode();
            if (registratorUserCode != null)
            {
                userTextArea.setValue("id:" + registratorUserCode);
            }
        }
        fieldSetMiddle.add(userTextArea);
        middleColumn.addFieldSet(fieldSetMiddle);
        return middleColumn;
    }

    private FormColumn createLeftColumn(FormData formData)
    {
        FormColumn leftColumn = new FormColumn(formData);
        FieldSet fieldSet = new FieldSet();
        fieldSet.setHeading(context.getMessageResources().getFileUploadLegend());
        for (int i = 0; i < FILE_FIELD_NUMBER; i++)
        {
            FileUploadField field = createFileField(i);
            uploadFields.add(field);
            fieldSet.add(field);
        }
        leftColumn.addFieldSet(fieldSet);
        return leftColumn;
    }

    private final SelectionListener<ButtonEvent> getUserValidateButtonListener()
    {
        final SelectionListener<ButtonEvent> buttonListener = new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    final List<UserInfoDTO> existingUsers = new ArrayList<UserInfoDTO>();
                    final List<UserInfoDTO> newUsers = new ArrayList<UserInfoDTO>();
                    final String[] userEntries = userTextArea.getUserEntries();
                    final FileShareUploadDialog dialog =
                            new FileShareUploadDialog(context, existingUsers, newUsers,
                                    "Upload New Files", userTextArea);
                    for (int i = 0; i < userEntries.length; i++)
                    {
                        if (userEntries[i].startsWith(Constants.USER_ID_PREFIX))
                        {
                            String userCode =
                                    userEntries[i].substring(Constants.USER_ID_PREFIX.length());
                            tryFindUserByUserCode(userCode, dialog, existingUsers);
                        } else
                        {
                            tryFindUserByEmail(userEntries[i], dialog, existingUsers, newUsers);
                        }
                    }
                    dialog.show();
                }

            };
        return buttonListener;
    }

    private final void tryFindUserByEmail(final String email, final FileShareUploadDialog dialog,
            final List<UserInfoDTO> existingUsers, final List<UserInfoDTO> newUsers)
    {
        context.getCifexService().tryFindUserByEmail(email,
                new AbstractAsyncCallback<List<UserInfoDTO>>(context)
                    {
                        public void onSuccess(List<UserInfoDTO> result)
                        {
                            List<UserInfoDTO> users = result;
                            if (users.size() > 0)
                            {
                                for (int j = 0; j < users.size(); j++)
                                {
                                    existingUsers.add(users.get(j));
                                    dialog.refresh();
                                }
                            } else
                            {
                                final UserInfoDTO user = new UserInfoDTO();
                                user.setEmail(email);
                                newUsers.add(user);
                                dialog.refresh();
                            }

                        }

                    });
    }

    private final void tryFindUserByUserCode(final String userCode,
            final FileShareUploadDialog dialog, final List<UserInfoDTO> existingUsers)
    {
        context.getCifexService().tryFindUserByUserCode(userCode,
                new AbstractAsyncCallback<UserInfoDTO>(context)
                    {
                        public void onSuccess(UserInfoDTO result)
                        {
                            UserInfoDTO existingUser = result;
                            if (existingUser != null)
                            {
                                existingUsers.add(existingUser);
                                dialog.refresh();
                            }
                        }
                    });
    }

    private final TextArea createCommentField()
    {
        final UserTextArea textAreaConfig = new UserTextArea();
        textAreaConfig.setAllowBlank(true);
        final IMessageResources messageResources = context.getMessageResources();
        textAreaConfig.setFieldLabel(messageResources.getCommentLabel());
        textAreaConfig.setName("upload-comment");
        textAreaConfig.setPreventScrollbars(true);
        textAreaConfig.setWidth(FIELD_WIDTH);
        trySetInitialValueFromURL(textAreaConfig, Constants.COMMENT_PARAMETER);
        return textAreaConfig;
    }

    private final FileUploadField createFileField(final int index)
    {
        final FileUploadField fileFieldConfig = new FileUploadField();
        fileFieldConfig.setFieldLabel(context.getMessageResources().getFileUploadFieldLabel(
                index + 1));
        fileFieldConfig.setName(getFilenameFieldName(index));
        fileFieldConfig.setWidth(FIELD_WIDTH);
        fileFieldConfig.setAllowBlank(index > 0);
        fileFieldConfig.setValidateOnBlur(false);
        return fileFieldConfig;
    }

    private void trySetInitialValueFromURL(UserTextArea field, String paramKey)
    {
        String initialValueOrNull = tryGetUrlParamValue(paramKey);
        if (StringUtils.isBlank(initialValueOrNull) == false)
        {
            field.setValue(URL.decode(initialValueOrNull));
            field.setReadOnly(true);
        }
    }

    private String tryGetUrlParamValue(String paramKey)
    {
        return context.getModel().getUrlParams().get(paramKey);
    }

    private final static String getFilenameFieldName(final int index)
    {
        return "upload-file-" + index;
    }

    private final String[] getFilePaths()
    {
        final List<String> filePaths = new ArrayList<String>(FILE_FIELD_NUMBER);
        for (int i = 0; i < FILE_FIELD_NUMBER; i++)
        {
            final String fieldValue = uploadFields.get(i).getFileInput().getValue();
            assert fieldValue != null : "Must not be null.";
            final String filePath = fieldValue.trim();
            // Ignore duplicates.
            if (filePath.length() > 0 && filePaths.contains(filePath) == false)
            {
                filePaths.add(filePath);
            }
        }
        return filePaths.toArray(StringUtils.EMPTY_STRING_ARRAY);
    }

    protected final void submitForm()
    {
        if (formPanel.isValid() == false)
        {
            return;
        }
        submitButton.disable();
        final String[] filenames = getFilePaths();
        if (filenames.length == 0)
        {
            submitButton.enable();
            return;
        }
        context.getCifexService().registerFilenamesForUpload(filenames,
                new AbstractAsyncCallback<Void>(context)
                    {
                        public final void onSuccess(final Void result)
                        {
                            formPanel.submit();
                            context.getCifexService().getFileUploadFeedback(
                                    new FileUploadFeedbackCallback(context));
                        }

                        @Override
                        public final void onFailure(final Throwable caught)
                        {
                            super.onFailure(caught);
                            submitButton.enable();
                        }
                    });
    }
}
