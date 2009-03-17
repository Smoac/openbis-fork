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

package ch.systemsx.cisd.openbis.plugin.generic.client.web.client.application.experiment;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;

/**
 * Stores and manages {@link FileUploadField} fields.
 * 
 * @author Izabela Adamczyk
 */
public class AttachmentManager
{
    private final String fieldLabel;

    private static final String FIELD_NAME_TEMPLATE = "{0}_{1}";

    private final List<FileUploadField> attachmentFields;

    private final String sessionKey;

    public AttachmentManager(String sessionKey, int initialNumberOfAttachments, String fieldLabel)
    {
        this.sessionKey = sessionKey;
        this.fieldLabel = fieldLabel;
        attachmentFields = new ArrayList<FileUploadField>();
        for (int i = 0; i < initialNumberOfAttachments; i++)
        {
            attachmentFields.add(createFileUploadField(i));
        }
    }

    public List<FileUploadField> getFields()
    {
        return attachmentFields;
    }

    public FileUploadField addField()
    {
        int counter = attachmentFields.size();
        FileUploadField field = createFileUploadField(counter);
        attachmentFields.add(field);
        return field;
    }

    public int attachmentsDefined()
    {
        int i = 0;
        for (FileUploadField field : attachmentFields)
        {
            Object value = field.getValue();
            if (value != null && String.valueOf(value).length() > 0)
            {
                i++;
            }
        }
        return i;
    }

    private final FileUploadField createFileUploadField(final int counter)
    {
        final FileUploadField file = new FileUploadField();
        final int number = counter + 1;
        file.setFieldLabel(fieldLabel);
        file.setName(Format.substitute(FIELD_NAME_TEMPLATE, sessionKey, number));
        return file;
    }

}
