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

import com.google.gwt.http.client.URL;
import com.gwtext.client.core.Connection;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.core.Position;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.ColumnConfig;
import com.gwtext.client.widgets.form.FieldConfig;
import com.gwtext.client.widgets.form.Form;
import com.gwtext.client.widgets.form.FormConfig;
import com.gwtext.client.widgets.form.TextArea;
import com.gwtext.client.widgets.form.TextAreaConfig;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.TextFieldConfig;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.FileShareUploadDialog;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ServletPathConstants;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.utils.CifexValidator;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;
import ch.systemsx.cisd.cifex.client.dto.UserInfoDTO;
import ch.systemsx.cisd.cifex.shared.basic.Constants;

/**
 * <code>Form</code> extension to upload files and to send emails to specified recipients.
 * 
 * @author Franz-Josef Elmer
 */
public final class FileUploadWidget extends Form
{

    private static final int FIELD_WIDTH = 230;

    private static final int COLUMN_WIDTH = 360;

    private static final int WIDTH_OFS = 20;

    private static final int TOTAL_WIDTH = 3 * COLUMN_WIDTH + WIDTH_OFS;

    private static final int LABEL_WIDTH = 80;

    private static final int FILE_FIELD_NUMBER = 3;

    private static final String FILE_UPLOAD_PREFIX = "FileUpload-";

    private final UserTextArea userTextArea;

    private final ViewContext context;

    private Button submitButton;

    private Button validateButton;

    public FileUploadWidget(final ViewContext context)
    {
        super(Ext.generateId(FILE_UPLOAD_PREFIX), createFormConfig());
        this.context = context;
        userTextArea = new UserTextArea(createEmailAreaConfig());
        createForm();
    }

    private final static FormConfig createFormConfig()
    {
        final FormConfig formConfig = new FormConfig();
        formConfig.setWidth(TOTAL_WIDTH);
        formConfig.setLabelAlign(Position.LEFT);
        formConfig.setButtonAlign(Position.LEFT);
        formConfig.setLabelWidth(LABEL_WIDTH);
        formConfig.setFileUpload(true);
        formConfig.setUrl(ServletPathConstants.FILE_UPLOAD_SERVLET_NAME);
        formConfig.setMethod(Connection.POST);
        return formConfig;
    }

    private final void createForm()
    {
        column(createLeftColumnConfig());
        fieldset(context.getMessageResources().getFileUploadLegend());
        for (int i = 0; i < FILE_FIELD_NUMBER; i++)
        {
            add(new TextField(createFileFieldConfig(i)));
        }
        end();
        end();

        column(createMiddleColumnConfig());
        fieldset(context.getMessageResources().getRecipientLegend());

        add(userTextArea);
        end();
        end();

        column(createRightColumnConfig());
        fieldset(context.getMessageResources().getCommentLabel());
        add(new TextArea(createCommentAreaConfig()));
        end();
        end();

        validateButton = addButton(context.getMessageResources().getValidateUsersButtonLabel());
        validateButton.addButtonListener(getUserValidateButtonListener());

        submitButton = addButton(context.getMessageResources().getFileUploadButtonLabel());
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

    private final ButtonListenerAdapter getUserValidateButtonListener()
    {
        final ButtonListenerAdapter buttonListener = new ButtonListenerAdapter()
            {
                public final void onClick(final Button but, final EventObject e)
                {
                    final List existingUsers = new ArrayList();
                    final List newUsers = new ArrayList();
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
            final List existingUsers, final List newUsers)
    {
        context.getCifexService().tryFindUserByEmail(email, new AbstractAsyncCallback(context)
            {
                public void onSuccess(Object result)
                {
                    UserInfoDTO[] users = (UserInfoDTO[]) result;
                    if (users.length > 0)
                    {
                        for (int j = 0; j < users.length; j++)
                        {
                            existingUsers.add(users[j]);
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
            final FileShareUploadDialog dialog, final List existingUsers)
    {
        context.getCifexService().tryFindUserByUserCode(userCode,
                new AbstractAsyncCallback(context)
                    {
                        public void onSuccess(Object result)
                        {
                            UserInfoDTO existingUser = (UserInfoDTO) result;
                            if (existingUser != null)
                            {
                                existingUsers.add(existingUser);
                                dialog.refresh();
                            }
                        }
                    });
    }

    private final static ColumnConfig createLeftColumnConfig()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setWidth(COLUMN_WIDTH);
        columnConfig.setLabelWidth(LABEL_WIDTH);
        return columnConfig;
    }

    private final static ColumnConfig createMiddleColumnConfig()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setWidth(COLUMN_WIDTH);
        columnConfig.setLabelWidth(LABEL_WIDTH);
        columnConfig.setStyle("margin-left:10px;");
        return columnConfig;
    }

    private final static ColumnConfig createRightColumnConfig()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setWidth(COLUMN_WIDTH);
        columnConfig.setLabelWidth(LABEL_WIDTH);
        columnConfig.setStyle("margin-left:10px;");
        return columnConfig;
    }

    private final TextAreaConfig createEmailAreaConfig()
    {
        final TextAreaConfig textAreaConfig = new TextAreaConfig();
        textAreaConfig.setAllowBlank(false);
        final IMessageResources messageResources = context.getMessageResources();
        textAreaConfig.setFieldLabel(messageResources.getRecipientFieldLabel());
        textAreaConfig.setName("email-addresses");
        textAreaConfig.setPreventScrollbars(false);
        textAreaConfig.setWidth(FIELD_WIDTH);
        textAreaConfig.setValidator(CifexValidator.getUserFieldValidator());
        textAreaConfig.setInvalidText(messageResources.getRecipientFieldInvalidText());
        trySetInitialValueFromURL(textAreaConfig, Constants.RECIPIENTS_PARAMETER);
        return textAreaConfig;
    }

    private final TextAreaConfig createCommentAreaConfig()
    {
        final TextAreaConfig textAreaConfig = new TextAreaConfig();
        textAreaConfig.setAllowBlank(true);
        final IMessageResources messageResources = context.getMessageResources();
        textAreaConfig.setFieldLabel(messageResources.getCommentLabel());
        textAreaConfig.setName("upload-comment");
        textAreaConfig.setGrow(true);
        textAreaConfig.setPreventScrollbars(true);
        textAreaConfig.setWidth(FIELD_WIDTH);
        trySetInitialValueFromURL(textAreaConfig, Constants.COMMENT_PARAMETER);
        return textAreaConfig;
    }

    private final TextFieldConfig createFileFieldConfig(final int index)
    {
        final TextFieldConfig fileFieldConfig = new TextFieldConfig();
        fileFieldConfig.setFieldLabel(context.getMessageResources().getFileUploadFieldLabel(
                index + 1));
        fileFieldConfig.setName(getFilenameFieldName(index));
        fileFieldConfig.setInputType("file");
        fileFieldConfig.setWidth(FIELD_WIDTH);
        fileFieldConfig.setAllowBlank(index > 0);
        fileFieldConfig.setValidateOnBlur(false);
        return fileFieldConfig;
    }

    private void trySetInitialValueFromURL(FieldConfig fieldConfig, String paramKey)
    {
        String initialValueOrNull = tryGetUrlParamValue(paramKey);
        if (StringUtils.isBlank(initialValueOrNull) == false)
        {
            fieldConfig.setValue(URL.decode(initialValueOrNull));
            fieldConfig.setReadOnly(true);
        }
    }

    private String tryGetUrlParamValue(String paramKey)
    {
        return (String) context.getModel().getUrlParams().get(paramKey);
    }

    private final static String getFilenameFieldName(final int index)
    {
        return "upload-file-" + index;
    }

    private final String[] getFilePaths()
    {
        final List filePaths = new ArrayList(FILE_FIELD_NUMBER);
        for (int i = 0; i < FILE_FIELD_NUMBER; i++)
        {
            final String fieldValue = findField(getFilenameFieldName(i)).getValueAsString();
            assert fieldValue != null : "Must not be null.";
            final String filePath = fieldValue.trim();
            // Ignore duplicates.
            if (filePath.length() > 0 && filePaths.contains(filePath) == false)
            {
                filePaths.add(filePath);
            }
        }
        return (String[]) filePaths.toArray(StringUtils.EMPTY_STRING_ARRAY);
    }

    protected final void submitForm()
    {
        if (isValid() == false)
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
                new AbstractAsyncCallback(context)
                    {
                        //
                        // AbstractAsyncCallback
                        //

                        public final void onSuccess(final Object result)
                        {
                            submit();
                            context.getCifexService().getFileUploadFeedback(
                                    new FileUploadFeedbackCallback(context));
                        }

                        public final void onFailure(final Throwable caught)
                        {
                            super.onFailure(caught);
                            submitButton.enable();
                        }
                    });
    }
}
