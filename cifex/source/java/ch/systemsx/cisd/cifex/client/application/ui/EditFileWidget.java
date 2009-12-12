/*
 * Copyright 2009 ETH Zuerich, CISD
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
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import ch.systemsx.cisd.cifex.client.ICIFEXServiceAsync;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.shared.basic.dto.FileInfoDTO;

/**
 * A widget for editing files.
 * <p>
 * This widget is used when the user or administrator edits an uploaded file.
 * </p>
 * 
 * @author Bernd Rinn
 */
public final class EditFileWidget extends LayoutContainer
{
    public static final int WIDTH = 400;

    public static final int LABEL_WIDTH = 100;

    private static final int COMMENT_AREA_HEIGHT = 100;

    private final ViewContext context;

    private final FormPanel formPanel;

    private final FormData formData;

    private TextField<String> fileNameField;

    private TextArea commentField;

    private DateField expirationDateField;

    /** The values from this file are used to populate the fields. */
    protected final FileInfoDTO editFile;

    public EditFileWidget(final ViewContext context, final FileInfoDTO file)
    {
        this.context = context;
        this.editFile = file;
        formData = new FormData("-20");
        formPanel = createEditFileForm();
        add(formPanel);
    }

    private FormPanel createEditFileForm()
    {
        final FormPanel newFormPanel = new FormPanel();
        newFormPanel.setHeaderVisible(false);
        newFormPanel.setFrame(false);
        newFormPanel.setBodyBorder(false);
        newFormPanel.setBorders(false);
        newFormPanel.setButtonAlign(HorizontalAlignment.CENTER);

        newFormPanel.setBorders(false);
        newFormPanel.setScrollMode(Scroll.AUTO);
        newFormPanel.setWidth(WIDTH);
        newFormPanel.setLabelWidth(LABEL_WIDTH);

        newFormPanel.add(fileNameField = createFileNameField(), formData);
        newFormPanel.add(commentField = createCommentField(), formData);
        newFormPanel.add(expirationDateField = createExpirationDateField(), formData);

        return newFormPanel;
    }

    private TextField<String> createFileNameField()
    {
        final TextField<String> fieldConfig = new TextField<String>();
        fieldConfig.setFieldLabel(context.getMessageResources().getFileNameLabel());
        fieldConfig.setName("file-name");
        fieldConfig.setAllowBlank(false);
        fieldConfig.setValue(editFile.getName());
        return fieldConfig;
    }

    private final TextArea createCommentField()
    {
        final UserTextArea textAreaConfig = new UserTextArea();
        textAreaConfig.setAllowBlank(true);
        textAreaConfig.setFieldLabel(context.getMessageResources().getFileCommentLabel());
        textAreaConfig.setName("file-comment");
        textAreaConfig.setHeight(COMMENT_AREA_HEIGHT);
        textAreaConfig.setValue(editFile.getComment());
        return textAreaConfig;
    }

    private final DateField createExpirationDateField()
    {
        final IMessageResources messages = context.getMessageResources();
        final DateField dateField = new DateField();
        dateField.setFieldLabel(messages.getExpirationDateLabel());
        final long registrationTimeOrNow = editFile.getRegistrationDate().getTime();
        final Date minExpirationDate = new Date(registrationTimeOrNow);
        CalendarUtil.addDaysToDate(minExpirationDate, 1);
        dateField.setMinValue(minExpirationDate);
        if (context.getModel().getUser().isAdmin() == false)
        {
            final Date maxExpirationDate = new Date(registrationTimeOrNow);
            CalendarUtil.addDaysToDate(maxExpirationDate, context.getModel().getUser()
                    .getMaxFileRetention());
            dateField.setMaxValue(maxExpirationDate);
        }
        dateField.getPropertyEditor().setFormat(DateTimeFormat.getFormat("yyyy-MM-dd"));
        dateField.setAllowBlank(false);
        dateField.setValue(editFile.getExpirationDate());
        return dateField;
    }

    public void submitForm(final Button button, final AsyncCallback<Void> refreshCallback)
    {
        final ICIFEXServiceAsync cifexService = context.getCifexService();
        if (formPanel.isValid())
        {
            if (button != null)
            {
                button.disable();
            }
            final String name = fileNameField.getValue();
            final String comment = commentField.getValue();
            final Date expirationDate = expirationDateField.getValue();
            cifexService.updateFileUserData(editFile.getID(), name, comment, expirationDate,
                    refreshCallback);
        } else
        {
            MessageBox.alert("Error", "Invalid data", null);
        }
    }
}
