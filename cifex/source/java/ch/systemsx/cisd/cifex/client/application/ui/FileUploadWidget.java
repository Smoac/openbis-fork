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

import com.gwtext.client.core.EventObject;
import com.gwtext.client.core.Ext;
import com.gwtext.client.widgets.Button;
import com.gwtext.client.widgets.event.ButtonListenerAdapter;
import com.gwtext.client.widgets.form.Form;
import com.gwtext.client.widgets.form.FormConfig;
import com.gwtext.client.widgets.form.TextField;
import com.gwtext.client.widgets.form.TextFieldConfig;

import ch.systemsx.cisd.cifex.client.application.ViewContext;

/**
 * @author Franz-Josef Elmer
 */
public class FileUploadWidget extends Form
{
    private final static FormConfig createFormConfig()
    {
        final FormConfig formConfig = new FormConfig();
        formConfig.setWidth(300);
        formConfig.setLabelAlign("left");
        formConfig.setButtonAlign("right");
        formConfig.setLabelWidth(75);
        formConfig.setFileUpload(true);
        formConfig.setUrl("/cifex/upload");
        formConfig.setMethod("POST");
        formConfig.setWaitMsgTarget("Upload File...");
        return formConfig;
    }

    private final ViewContext context;

    public FileUploadWidget(final ViewContext context)
    {
        super(Ext.generateId("FileUpload-"), createFormConfig());
        this.context = context;
        createForm();
    }

    private void createForm()
    {
        final TextFieldConfig fileFieldConfig = new TextFieldConfig();
        fileFieldConfig.setFieldLabel("File");
        fileFieldConfig.setName("upload-file");
        fileFieldConfig.setInputType("file");
        fileFieldConfig.setWidth(300);
        fileFieldConfig.setAllowBlank(false);
        fileFieldConfig.setValidateOnBlur(false);

        add(new TextField(fileFieldConfig));

        Button button = addButton("upload");
        button.addButtonListener(new ButtonListenerAdapter()
            {
                public final void onClick(final Button but, final EventObject e)
                {
                    submit();
                }

            });
        end();
        render();
    }

}
