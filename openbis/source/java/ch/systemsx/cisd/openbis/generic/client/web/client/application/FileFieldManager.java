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

package ch.systemsx.cisd.openbis.generic.client.web.client.application;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;

import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.widget.FieldUtil;

/**
 * Stores and manages {@link FileUploadField} fields.
 * 
 * @author Izabela Adamczyk
 */
public class FileFieldManager
{
    private final String fieldLabel;

    private static final String FIELD_NAME_TEMPLATE = "{0}_{1}";

    private final List<FileUploadField> fileFields;

    private final String sessionKey;

    public FileFieldManager(String sessionKey, int initialNumberOfFields, String fieldLabel)
    {
        this.sessionKey = sessionKey;
        this.fieldLabel = fieldLabel;
        fileFields = new ArrayList<FileUploadField>();
        for (int i = 0; i < initialNumberOfFields; i++)
        {
            fileFields.add(createFileUploadField(i));
        }
    }

    public void setMandatory()
    {
        assert fileFields.size() > 0;
        FileUploadField field = fileFields.get(0);
        field.setAllowBlank(false);
        FieldUtil.markAsMandatory(field);
    }

    public List<FileUploadField> getFields()
    {
        return fileFields;
    }

    public FileUploadField addField()
    {
        int counter = fileFields.size();
        FileUploadField field = createFileUploadField(counter);
        fileFields.add(field);
        return field;
    }

    public int filesDefined()
    {
        int i = 0;
        for (FileUploadField field : fileFields)
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
