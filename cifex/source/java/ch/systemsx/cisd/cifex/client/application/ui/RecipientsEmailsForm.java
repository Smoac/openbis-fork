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

import com.gwtext.client.core.Ext;
import com.gwtext.client.core.Position;
import com.gwtext.client.widgets.LayoutDialog;
import com.gwtext.client.widgets.form.Form;
import com.gwtext.client.widgets.form.FormConfig;
import com.gwtext.client.widgets.form.TextArea;
import com.gwtext.client.widgets.form.TextAreaConfig;
import com.gwtext.client.widgets.form.ValidationException;
import com.gwtext.client.widgets.form.Validator;

import ch.systemsx.cisd.cifex.client.application.Constants;
import ch.systemsx.cisd.cifex.client.application.IMessageResources;
import ch.systemsx.cisd.cifex.client.application.ViewContext;
import ch.systemsx.cisd.cifex.client.application.utils.StringUtils;

/**
 * Form to specify file sharers email addresses.
 * 
 * @author Izabela Adamczyk
 */
public final class RecipientsEmailsForm extends Form
{

    private static final String EMAIL_ADDRESSES = "email-addresses";

    private static final int FIELD_WIDTH = 330;

    private static final int COLUMN_WIDTH = 460;

    private static final int WIDTH_OFS = 20;

    private static final int TOTAL_WIDTH = COLUMN_WIDTH + WIDTH_OFS;

    private static final int LABEL_WIDTH = 80;

    private static final String EMAILS_TO_SHARE_FILE_WITH_PREFIX = "EmailsToShareFileWith-";

    private final ViewContext viewContext;

    private final ModelBasedGrid userGrid;

    private TextArea emailAddressesField;

    private final String fileId;

    private final LayoutDialog parentDialog;

    public RecipientsEmailsForm(final ViewContext context, ModelBasedGrid userGrid, String fileId,
            LayoutDialog parentDialog)
    {
        super(Ext.generateId(EMAILS_TO_SHARE_FILE_WITH_PREFIX), createFormConfig());
        this.viewContext = context;
        this.userGrid = userGrid;
        this.fileId = fileId;
        this.parentDialog = parentDialog;
        createForm();
    }

    private final static FormConfig createFormConfig()
    {
        final FormConfig formConfig = new FormConfig();
        formConfig.setWidth(TOTAL_WIDTH);
        formConfig.setLabelAlign(Position.LEFT);
        formConfig.setLabelWidth(LABEL_WIDTH);
        formConfig.setUrl(Constants.FILE_UPLOAD_SERVLET_NAME);
        return formConfig;
    }

    private final void createForm()
    {
        fieldset(viewContext.getMessageResources().getRecipientLegend());
        emailAddressesField = new TextArea(createEmailAreaConfig());
        add(emailAddressesField);
        end();

        render();
    }

    private final TextAreaConfig createEmailAreaConfig()
    {
        final TextAreaConfig textAreaConfig = new TextAreaConfig();
        textAreaConfig.setAllowBlank(false);
        final IMessageResources messageResources = viewContext.getMessageResources();
        textAreaConfig.setFieldLabel(messageResources.getRecipientFieldLabel());
        textAreaConfig.setName(EMAIL_ADDRESSES);
        textAreaConfig.setPreventScrollbars(false);
        textAreaConfig.setWidth(FIELD_WIDTH);
        textAreaConfig.setValidator(new Validator()
            {
                public final boolean validate(final String value) throws ValidationException
                {
                    final String[] result = value.split("[,\\s]+");
                    if (result.length == 0)
                    {
                        return false;
                    }
                    for (int i = 0; i < result.length; i++)
                    {
                        assert result[i] != null : "Must not be null.";
                        final String item = result[i].trim();
                        if (item.length() > 0
                                && StringUtils.matches(StringUtils.EMAIL_REGEX, item) == false)
                        {
                            return false;
                        }
                    }
                    return true;
                }
            });
        textAreaConfig.setInvalidText(messageResources.getRecipientFieldInvalidText());
        return textAreaConfig;
    }

    public final void submitForm()
    {
        if (isValid() == false)
        {
            return;
        }

        String userEmails = emailAddressesField.getText();
        viewContext.getCifexService()
                .createSharingLink(
                        fileId,
                        userEmails,
                        new FileShareUserGridRefresherCallback(fileId, viewContext, userGrid,
                                parentDialog));

    }
}
