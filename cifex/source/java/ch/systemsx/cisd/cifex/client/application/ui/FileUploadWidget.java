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

import com.gwtext.client.core.Connection;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.core.Position;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.MessageBox;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.ColumnConfig;
import com.gwtext.client.widgets.form.Form;
import com.gwtext.client.widgets.form.FormConfig;
import com.gwtext.client.widgets.form.TextArea;
import com.gwtext.client.widgets.form.TextAreaConfig;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.TextFieldConfig;
import com.gwtext.client.widgets.form.event.FormListenerAdapter;

import ch.systemsx.cisd.cifex.client.application.Constants;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.utils.DOMUtils;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;

/**
 * <code>Form</code> extension to upload files and to send emails to specified recipients.
 * 
 * @author Franz-Josef Elmer
 */
public class FileUploadWidget extends Form
{
    private static final int FIELD_WIDTH = 230;

    private static final int COLUMN_WIDTH = 342;

    private static final int LABEL_WIDTH = 60;

    private static final int FILE_FIELD_NUMBER = 3;

    private static final String FILE_UPLOAD_PREFIX = "FileUpload-";

    private final ViewContext context;

    private Button button;

    public FileUploadWidget(final ViewContext context)
    {
        super(Ext.generateId(FILE_UPLOAD_PREFIX), createFormConfig());
        this.context = context;
        createForm();
        addFormListenerListener(new FormListenerAdapter()
            {

                //
                // FormListenerAdapter
                //

                public final void onActionComplete(final Form form, final int httpStatus, final String responseText)
                {
                    final String response = DOMUtils.getElementValue("pre", responseText);
                    if (StringUtils.isBlank(response) == false)
                    {
                        final IMessageResources messageResources = context.getMessageResources();
                        final String title = messageResources.getMessageBoxWarningTitle();
                        MessageBox.alert(title, response);
                    }
                }

                public final void onActionFailed(final Form form, final int httpStatus, final String responseText)
                {
                    button.enable();
                }

            });
    }

    private final static FormConfig createFormConfig()
    {
        final FormConfig formConfig = new FormConfig();
        formConfig.setWidth(700);
        formConfig.setLabelAlign(Position.LEFT);
        formConfig.setButtonAlign(Position.RIGHT);
        formConfig.setLabelWidth(LABEL_WIDTH);
        formConfig.setFileUpload(true);
        formConfig.setUrl(Constants.FILE_UPLOAD_SERVLET_NAME);
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

        column(createRightColumnConfig());
        fieldset(context.getMessageResources().getRecipientLegend());
        add(new TextArea(createTextAreaConfig()));

        button = addButton(context.getMessageResources().getFileUploadButtonLabel());
        button.addButtonListener(new ButtonListenerAdapter()
            {

                //
                // ButtonListenerAdapter
                //

                public final void onClick(final Button but, final EventObject e)
                {
                    submitForm();
                }

            });
        end();
        render();
    }

    private final static ColumnConfig createLeftColumnConfig()
    {
        final ColumnConfig columnConfig = new ColumnConfig();
        columnConfig.setWidth(COLUMN_WIDTH);
        columnConfig.setLabelWidth(LABEL_WIDTH);
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

    private final TextAreaConfig createTextAreaConfig()
    {
        final TextAreaConfig textAreaConfig = new TextAreaConfig();
        textAreaConfig.setAllowBlank(false);
        textAreaConfig.setFieldLabel(context.getMessageResources().getRecipientFieldLabel());
        textAreaConfig.setName("email-addresses");
        textAreaConfig.setGrow(true);
        textAreaConfig.setPreventScrollbars(true);
        textAreaConfig.setWidth(FIELD_WIDTH);
        // TODO 2008-01-27, Christian Ribeaud: use regex validation here or our own validation schema.
        // textAreaConfig.setRegex(regex);
        // textAreaConfig.setRegexText(regexText);
        // textAreaConfig.setValidator(validator);
        return textAreaConfig;
    }

    private final TextFieldConfig createFileFieldConfig(final int index)
    {
        final TextFieldConfig fileFieldConfig = new TextFieldConfig();
        fileFieldConfig.setFieldLabel(context.getMessageResources().getFileUploadFieldLabel(index + 1));
        fileFieldConfig.setName("upload-file-" + index);
        fileFieldConfig.setInputType("file");
        fileFieldConfig.setWidth(FIELD_WIDTH);
        fileFieldConfig.setAllowBlank(index > 0);
        fileFieldConfig.setValidateOnBlur(false);
        fileFieldConfig.setReadOnly(true);
        return fileFieldConfig;
    }

    protected void submitForm()
    {
        submit();
        if (isValid())
        {
            button.disable();
        }
    }

}
