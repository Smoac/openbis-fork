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

import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.CREATE_USER_COMMENT_LABEL;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.LAUNCH_JWS_APPLICATION_TITLE;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.UPLOAD_FILES_BROWSER_PANEL_TITLE;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.UPLOAD_FILES_DUPLICATES_MSG;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.UPLOAD_FILES_DUPLICATES_MSGBOX_TITLE;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.UPLOAD_FILES_FILE_FIELD_LABEL;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.UPLOAD_FILES_LAUNCH_WEBSTART_LABEL;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.UPLOAD_FILES_PANEL_TITLE;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.UPLOAD_FILES_RECIPIENT_FIELD_LABEL;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.UPLOAD_FILES_RECIPIENT_FIELD_TOOLTIP;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.UPLOAD_FILES_RESET_BUTTON_LABEL;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.UPLOAD_FILES_SUBMIT_BUTTON_LABEL;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.UPLOAD_FILES_WEBSTART_PANEL_TITLE;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.UPLOAD_FILES_WEBSTART_PROS_INFO;
import static ch.systemsx.cisd.cifex.client.application.utils.MessageDictionary.msg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.IconAlign;
import com.extjs.gxt.ui.client.Style.VerticalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Encoding;
import com.extjs.gxt.ui.client.widget.form.FormPanel.LabelAlign;
import com.extjs.gxt.ui.client.widget.form.FormPanel.Method;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.TableData;
import com.extjs.gxt.ui.client.widget.layout.TableLayout;
import com.extjs.gxt.ui.client.widget.layout.TableRowLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;

import ch.systemsx.cisd.cifex.client.application.AbstractAsyncCallback;
import ch.systemsx.cisd.cifex.client.application.FileShareUploadDialog;
import ch.systemsx.cisd.cifex.client.application.ServletPathConstants;
import ch.systemsx.cisd.cifex.client.application.UserUtils;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.utils.ImageUtils;
import ch.systemsx.cisd.cifex.client.application.utils.WindowUtils;
import ch.systemsx.cisd.cifex.shared.basic.Constants;
import ch.systemsx.cisd.cifex.shared.basic.dto.UserInfoDTO;
import ch.systemsx.cisd.common.shared.basic.string.StringUtils;

/**
 * <code>Form</code> extension to upload files and to send emails to specified recipients.
 * 
 * @author Franz-Josef Elmer
 */
public final class FileUploadWidget extends LayoutContainer
{
    private static boolean SUBSCRIBE_TO_WINDOW_CHANGES = false;

    private static final int FILE_FIELD_NUMBER = 3;

    private final UserTextArea recipientsTextArea;

    private final ViewContext context;

    private Button submitButton;

    private final FormPanel formPanel;

    private final ContentPanel uploaderPanel;

    private final List<FileUploadField> uploadFields;

    private final FieldSet uploadFilesFieldSet;

    public FileUploadWidget(final ViewContext context)
    {
        // Instantiate and initialize fields
        uploadFields = new ArrayList<FileUploadField>();
        this.context = context;
        TableRowLayout layout = new TableRowLayout();
        layout.setCellVerticalAlign(VerticalAlignment.TOP);
        layout.setWidth("100%");
        layout.setCellSpacing(2);
        setLayout(layout);
        setBorders(false);

        formPanel = new FormPanel();
        initializeFormPanel();

        recipientsTextArea = new UserTextArea();
        initializeRecipientsTextArea();

        uploadFilesFieldSet = new FieldSet();
        initializeUploadFilesFieldSet();

        createForm();

        uploaderPanel = new ContentPanel();
        initializeUploaderPanel();

        // Add top-level widgets to the container
        add(formPanel, new TableData("66%", ""));
        add(uploaderPanel, new TableData("34%", ""));

        if (SUBSCRIBE_TO_WINDOW_CHANGES)
            setMonitorWindowResize(true);
    }

    private void initializeRecipientsTextArea()
    {
        recipientsTextArea.setAllowBlank(false);
        recipientsTextArea.setFieldLabel(msg(UPLOAD_FILES_RECIPIENT_FIELD_LABEL));
        recipientsTextArea.setToolTip(msg(UPLOAD_FILES_RECIPIENT_FIELD_TOOLTIP));
        recipientsTextArea.setName("email-addresses");
        recipientsTextArea.setPreventScrollbars(false);
        
        trySetInitialValueFromURL(recipientsTextArea, Constants.RECIPIENTS_PARAMETER);
    }

    private void initializeFormPanel()
    {
        formPanel.setHeading(msg(UPLOAD_FILES_BROWSER_PANEL_TITLE));
        formPanel.setHeaderVisible(true);
        formPanel.setBodyBorder(false);
        formPanel.setBorders(true);
        formPanel.setLabelAlign(LabelAlign.LEFT);
        formPanel.setButtonAlign(HorizontalAlignment.LEFT);
        formPanel.setAction(ServletPathConstants.FILE_UPLOAD_SERVLET_NAME);
        formPanel.setMethod(Method.POST);
        formPanel.setEncoding(Encoding.MULTIPART);
        formPanel.setHeight(335);
    }

    private void initializeUploaderPanel()
    {
        uploaderPanel.setHeading(msg(UPLOAD_FILES_WEBSTART_PANEL_TITLE));
        uploaderPanel.setHeaderVisible(true);
        uploaderPanel.setBodyBorder(false);
        uploaderPanel.setBorders(true);
        uploaderPanel.setHeight(335);

        final String webStartTitle = msg(LAUNCH_JWS_APPLICATION_TITLE);
        final String servletName = ServletPathConstants.FILE2GB_UPLOAD_SERVLET_NAME;
        final String buttonTitle = msg(UPLOAD_FILES_LAUNCH_WEBSTART_LABEL);
        final Button launchButton = new Button(buttonTitle, new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    WindowUtils.openNewDependentWindow(servletName);
                }
            });
        launchButton.setIcon(AbstractImagePrototype.create(ImageUtils.ICONS.getUploaderIcon()));
        launchButton.setTitle(webStartTitle);
        launchButton.setHeight(30);
        launchButton.setIconAlign(IconAlign.LEFT);

        uploaderPanel.add(launchButton, new FlowData(new Margins(20)));
        uploaderPanel.add(new Html(msg(UPLOAD_FILES_WEBSTART_PROS_INFO)), new FlowData(new Margins(
                0, 0, 0, 10)));
    }

    private final void createForm()
    {
        FormData formData = new FormData("95%");

        formPanel.addButton(submitButton = new Button(msg(UPLOAD_FILES_SUBMIT_BUTTON_LABEL)));
        submitButton.addSelectionListener(new SelectionListener<ButtonEvent>()
            {
                @Override
                public void componentSelected(ButtonEvent ce)
                {
                    submitForm();
                }

            });
        submitButton.setIcon(AbstractImagePrototype.create(ImageUtils.ICONS.getUploaderIcon()));

        formPanel.addButton(new Button(msg(UPLOAD_FILES_RESET_BUTTON_LABEL),
                new SelectionListener<ButtonEvent>()
                    {

                        @Override
                        public void componentSelected(ButtonEvent ce)
                        {
                            formPanel.reset();
                        }
                    }));

        LayoutContainer formContainer = new LayoutContainer();
        FormLayout layout = new FormLayout();
        layout.setLabelWidth(70);
        layout.setDefaultWidth(-1);
        formContainer.setLayout(layout);

        // Add the fields
        formContainer.add(createRecipientsField(), formData);
        formContainer.add(createCommentField(), formData);
        formContainer.add(uploadFilesFieldSet, formData);

        formPanel.add(formContainer);
    }

    private Widget createRecipientsField()
    {
        if (context.getModel().getUser().isPermanent() == false)
        {
            final UserInfoDTO registratorOrNull = context.getModel().getUser().getRegistrator();
            final String registratorUserCode =
                    (registratorOrNull != null) ? registratorOrNull.getUserCode() : null;
            if (registratorUserCode != null)
            {
                recipientsTextArea.addItem("id:" + registratorUserCode);
            }
        }
        return recipientsTextArea;
    }

    private void initializeUploadFilesFieldSet()
    {
        TableLayout layout = new TableLayout(1);
        layout.setCellVerticalAlign(VerticalAlignment.TOP);
        layout.setWidth("100%");
        layout.setCellSpacing(2);
        uploadFilesFieldSet.setLayout(layout);
        uploadFilesFieldSet.setHeading(msg(UPLOAD_FILES_PANEL_TITLE));
        for (int i = 0; i < FILE_FIELD_NUMBER; i++)
        {
            FileUploadField field = createFileField(i);
            uploadFields.add(field);
            uploadFilesFieldSet.add(field, new TableData("100%", ""));
        }
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
                    final String[] userEntries = recipientsTextArea.getItems();
                    final FileShareUploadDialog dialog =
                            new FileShareUploadDialog(context, existingUsers, newUsers,
                                    "Upload New Files", recipientsTextArea);
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
        context.getCifexService().findUserByEmail(email,
                new AbstractAsyncCallback<List<UserInfoDTO>>(context)
                    {
                        @Override
                        public void onSuccess(List<UserInfoDTO> users)
                        {
                            final UserInfoDTO requestUser = context.getModel().getUser();
                            UserUtils.removeUnsuitableUsersForSharing(requestUser, users);
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
                                user.setExpirationDate(UserUtils
                                        .getDefaultUserExpirationDate(context));
                                newUsers.add(user);
                                dialog.refresh();
                            }

                        }

                    });
    }

    private final void tryFindUserByUserCode(final String userCode,
            final FileShareUploadDialog dialog, final List<UserInfoDTO> existingUsers)
    {
        context.getCifexService().tryFindUserByUserCodeOrCreate(userCode,
                new AbstractAsyncCallback<UserInfoDTO>(context)
                    {
                        @Override
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

    private final Widget createCommentField()
    {
        final TextArea textAreaConfig = new TextArea();
        textAreaConfig.setAllowBlank(true);
        textAreaConfig.setFieldLabel(msg(CREATE_USER_COMMENT_LABEL));
        textAreaConfig.setName("upload-comment");
        textAreaConfig.setPreventScrollbars(false);
        trySetInitialValueFromURL(textAreaConfig, Constants.COMMENT_PARAMETER);
        return textAreaConfig;
    }

    private final FileUploadField createFileField(final int index)
    {
        final FileUploadField fileField = new FileUploadField()
            {
                @Override
                public void setReadOnly(boolean readOnly)
                {
                    // WORKAROUND to keep the button enabled after field reset
                    this.readOnly = readOnly;
                }
            };
        fileField.setFieldLabel(msg(UPLOAD_FILES_FILE_FIELD_LABEL, index + 1));
        fileField.setName(getFilenameFieldName(index));
        fileField.setWidth("100%");
        fileField.setAllowBlank(index > 0);
        fileField.setValidateOnBlur(false);
        Menu menu = new Menu();
        menu.add(new MenuItem(msg(UPLOAD_FILES_RESET_BUTTON_LABEL),
                new SelectionListener<MenuEvent>()
                    {

                        @Override
                        public void componentSelected(MenuEvent ce)
                        {
                            fileField.reset();
                        }
                    }));
        fileField.setContextMenu(menu);
        return fileField;
    }

    private void trySetInitialValueFromURL(TextArea field, String paramKey)
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

    private final List<String> getFilePaths()
    {
        final List<String> filePaths = new ArrayList<String>(FILE_FIELD_NUMBER);
        for (int i = 0; i < FILE_FIELD_NUMBER; i++)
        {
            final String fieldValue = uploadFields.get(i).getFileInput().getValue();
            assert fieldValue != null : "Must not be null.";
            final String filePath = fieldValue.trim();
            if (filePath.length() > 0)
            {
                filePaths.add(filePath);
            }
        }
        return filePaths;
    }

    private boolean noDuplicates(List<String> names)
    {
        final Set<String> set = new HashSet<String>(names);
        return names.size() == set.size();
    }

    protected final void submitForm()
    {
        if (formPanel.isValid() == false)
        {
            return;
        }
        submitButton.disable();
        final List<String> filenames = getFilePaths();
        if (filenames.size() == 0)
        {
            submitButton.enable();
            return;
        }
        if (noDuplicates(filenames))
        {
            registerFilenames(filenames);
        } else
        {
            final String title = msg(UPLOAD_FILES_DUPLICATES_MSGBOX_TITLE);
            final String msg = msg(UPLOAD_FILES_DUPLICATES_MSG);
            MessageBox.confirm(title, msg, new Listener<MessageBoxEvent>()
                {
                    @Override
                    public void handleEvent(MessageBoxEvent messageEvent)
                    {
                        if (messageEvent.getButtonClicked().getItemId().equals(Dialog.YES))
                        {
                            registerFilenames(filenames);
                        } else
                        {
                            submitButton.enable();
                        }
                    }
                });
        }
    }

    private void registerFilenames(final List<String> filenames)
    {
        context.getCifexService().registerFilenamesForUpload(
                filenames.toArray(StringUtils.EMPTY_STRING_ARRAY),
                new AbstractAsyncCallback<Void>(context)
                    {
                        @Override
                        public final void onSuccess(final Void result)
                        {
                            formPanel.submit();
                            context.getCifexService().getFileUploadFeedback(
                                    new FileUploadFeedbackCallback(context, submitButton));
                        }

                        @Override
                        public final void onFailure(final Throwable caught)
                        {
                            super.onFailure(caught);
                            submitButton.enable();
                        }
                    });
    }

    @Override
    protected void onWindowResize(int aWidth, int aHeight)
    {
        super.onWindowResize(aWidth, aHeight);
        // Don't resize the upload fields -- this causes more problems than it solves.
        // for (FileUploadField uploadField : uploadFields)
        // {
        // uploadField.setWidth((aWidth / 2));
        // }
        formPanel.layout(true);
        uploadFilesFieldSet.layout(true);
    }

    public void onOutermostContainerWindowResize(int aWidth, int aHeight)
    {
        onWindowResize(aWidth, aHeight);
    }
}
